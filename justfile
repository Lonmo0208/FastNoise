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

perfNoise args="":
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaNoise" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaNoise" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaNoise" -Pzworldname="end" {{args}}

    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedNoise" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedNoise" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedNoise" -Pzworldname="end" {{args}}

perfBiomes args="":
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaBiomes" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaBiomes" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="vanillaBiomes" -Pzworldname="end" {{args}}

    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedBiomes" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedBiomes" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="optimizedBiomes" -Pzworldname="end" {{args}}

perfNoiseAsync args="":
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaNoise" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaNoise" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaNoise" -Pzworldname="end" {{args}}

    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedNoise" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedNoise" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedNoise" -Pzworldname="end" {{args}}

perfBiomesAsync args="":
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaBiomes" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaBiomes" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="vanillaBiomes" -Pzworldname="end" {{args}}

    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedBiomes" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedBiomes" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="optimizedBiomes" -Pzworldname="end" {{args}}

parity:
    ./gradlew :perf:runServer -Pzperfbenchmark="parity"

task taskName:
    ./gradlew {{taskName}}