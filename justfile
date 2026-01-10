default: build

build:
    ./gradlew build

run:
    ./gradlew runClient

sources:
    ./gradlew genSources

wrapper:
    ./gradlew wrapper --gradle-version=latest && ./gradlew wrapper

tidy:
    #!/bin/bash

    # find -iname "*.java" | xargs clang-format -i
    find -iname "*.java" | xargs google-java-format -i #--fix-imports-only -i

    # cd nim
    # find -iname "*.nim" | xargs nimpretty
    
test:
    ./gradlew test

view_test_results:
    cd build/reports/tests/test/ && python -m http.server

bench:
    ./gradlew jmh

stop:
    ./gradlew --stop

clean:
    ./gradlew clean

prod:
    ./gradlew prodClient

perfNoise:
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaNoise" -Pzworldname="overworld"
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaNoise" -Pzworldname="nether"
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaNoise" -Pzworldname="end"

    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedNoise" -Pzworldname="overworld"
    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedNoise" -Pzworldname="nether"
    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedNoise" -Pzworldname="end"

perfBiomes:
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaBiomes" -Pzworldname="overworld"
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaBiomes" -Pzworldname="nether"
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaBiomes" -Pzworldname="end"

    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedBiomes" -Pzworldname="overworld"
    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedBiomes" -Pzworldname="nether"
    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedBiomes" -Pzworldname="end"

perfNoiseAsync:
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaNoise" -Pzworldname="overworld"
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaNoise" -Pzworldname="nether"
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaNoise" -Pzworldname="end"

    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedNoise" -Pzworldname="overworld"
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedNoise" -Pzworldname="nether"
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedNoise" -Pzworldname="end"

perfBiomesAsync:
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaBiomes" -Pzworldname="overworld"
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaBiomes" -Pzworldname="nether"
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaBiomes" -Pzworldname="end"

    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedBiomes" -Pzworldname="overworld"
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedBiomes" -Pzworldname="nether"
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedBiomes" -Pzworldname="end"

parity:
    ./gradlew :perf:runServer -Pzperfbenchmark="parity"

task taskName:
    ./gradlew {{taskName}}