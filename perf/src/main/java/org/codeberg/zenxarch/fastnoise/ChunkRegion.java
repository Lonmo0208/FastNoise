package org.codeberg.zenxarch.fastnoise;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import org.codeberg.zenxarch.fastnoise.mixin.ChunkAccessor;

public record ChunkRegion(ProtoChunk[] chunks, int minX, int minZ, int strideX)
    implements BiomeAccess.Storage {
  public static ChunkRegion of(
      TestWorld world, org.codeberg.zenxarch.fastnoise.BenchmarkSettings.ChunkRegion region) {
    var min = region.min();
    var max = region.max();
    var sizeX = max.x() + 1 - min.x();
    var sizeZ = max.z() + 1 - min.z();
    var chunks = new ProtoChunk[sizeX * sizeZ];

    var result = new ChunkRegion(chunks, min.x(), min.z(), sizeX);

    for (int z = min.z(); z <= max.z(); z++) {
      for (int x = min.x(); x <= max.x(); x++) {
        var index = result.getIndex(x, z);
        var pos = new ChunkPos(x, z);
        chunks[index] = world.createChunk(pos);
      }
    }

    return result;
  }

  @Override
  public RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
    return this.getChunk(biomeX >> 2, biomeZ >> 2).getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
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

      var destList = ((ChunkAccessor) dest).zenxarch$postProcessingLists();
      var srcList = ((ChunkAccessor) src).zenxarch$postProcessingLists();
      if (destList.length != srcList.length) {
        throw new IllegalStateException("Copying invalid data ");
      }
      for (int j = 0; j < srcList.length; j++) {
        destList[j] = copy(srcList[j]);
      }
    }
  }

  private static ShortList copy(ShortList src) {
    if (src == null) return null;
    if (src.isEmpty()) return null;
    var result = new ShortArrayList();
    result.addAll(src);
    return result;
  }

  public void copyBiomes(ChunkRegion region) {
    for (int i = 0; i < this.chunks.length; i++) {
      var dest = this.chunks[i];
      var src = region.getChunk(dest.getPos());

      var srcData = src.getSectionArray();
      var destData = dest.getSectionArray();

      for (int j = 0; j < srcData.length; j++) {
        destData[j] = new ChunkSection(destData[j].blockStateContainer, srcData[j].biomeContainer);
      }
    }
  }

  private int getIndex(int x, int z) {
    var cx = x - minX;
    var cz = z - minZ;
    if (cx < 0 || cx >= strideX) return -1;
    var index = cx + cz * strideX;
    if (index > this.chunks.length) return -1;
    return index;
  }

  public ProtoChunk getChunk(ChunkPos pos) {
    return getChunk(pos.x(), pos.z());
  }

  public ProtoChunk getChunk(int cx, int cz) {
    return this.chunks[this.getIndex(cx, cz)];
  }
}
