package org.codeberg.zenxarch.fastnoise;

import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;

public record ChunkRegion(ProtoChunk[] chunks, Object2IntMap<ChunkPos> chunkGetter)
    implements BiomeAccess.Storage {
  public static ChunkRegion of(
      TestWorld world, org.codeberg.zenxarch.fastnoise.BenchmarkSettings.ChunkRegion region) {
    var chunks = new ProtoChunk[region.pos().length];
    var map =
        new Object2IntAVLTreeMap<ChunkPos>(
            (a, b) -> {
              var res = IntComparators.NATURAL_COMPARATOR.compare(a.z(), b.z());
              if (res != 0) return IntComparators.NATURAL_COMPARATOR.compare(a.x(), b.x());
              return res;
            });
    for (int i = 0; i < region.pos().length; i++) {
      chunks[i] = world.createChunk(region.pos()[i]);
      map.put(region.pos()[i], i);
    }
    return new ChunkRegion(chunks, map);
  }

  @Override
  public RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
    var chunk = chunks[chunkGetter.applyAsInt(new ChunkPos(biomeX >> 2, biomeZ >>> 2))];
    return chunk.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
  }

  public void copyNoiseAndHeightmap(ChunkRegion region) {
    for (int i = 0; i < this.chunks.length; i++) {
      var dest = this.chunks[i];
      var src = region.getChunk(dest.getPos());

      var srcData = src.getSectionArray();
      var destData = dest.getSectionArray();

      for (int j = 0; j < srcData.length; j++) {
        destData[j] =
            new ChunkSection(srcData[j].blockStateContainer.copy(), destData[j].biomeContainer);
      }

      for (var heightmap : src.getHeightmaps()) {
        dest.setHeightmap(heightmap.getKey(), heightmap.getValue().asLongArray());
      }
    }
  }

  public ProtoChunk getChunk(ChunkPos pos) {
    return this.chunks[this.chunkGetter.getInt(pos)];
  }
}
