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

modrinth ptype="alpha":
    touch changelog.md
    $EDITOR changelog.md
    ./gradlew modrinth -Pzmtype="{{ptype}}"
    rm changelog.md

# Available props zperfbenchmark zuseasync zworldname zbenchmode zwarmuptime zmeasuretime zwarmups zmeasures"
perfNoise +args="":
    ./gradlew :perf:runServer -Pzperfbenchmark="noisegen" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="noisegen" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="noisegen" -Pzworldname="end" {{args}}

    ./gradlew :perf:runServer -Pzperfbenchmark="noisegen" -Pzmod="true" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="noisegen" -Pzmod="true" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="noisegen" -Pzmod="true" -Pzworldname="end" {{args}}

# Available props zperfbenchmark zuseasync zworldname zbenchmode zwarmuptime zmeasuretime zwarmups zmeasures"
perfBiomes +args="":
    ./gradlew :perf:runServer -Pzperfbenchmark="biomegen" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="biomegen" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="biomegen" -Pzworldname="end" {{args}}

    ./gradlew :perf:runServer -Pzperfbenchmark="biomegen" -Pzmod="true" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="biomegen" -Pzmod="true" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzperfbenchmark="biomegen" -Pzmod="true" -Pzworldname="end" {{args}}

# Available props zperfbenchmark zuseasync zworldname zbenchmode zwarmuptime zmeasuretime zwarmups zmeasures"
perfNoiseAsync +args="":
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="noisegen" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="noisegen" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="noisegen" -Pzworldname="end" {{args}}

    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzmod="true" -Pzperfbenchmark="noisegen" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzmod="true" -Pzperfbenchmark="noisegen" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzmod="true" -Pzperfbenchmark="noisegen" -Pzworldname="end" {{args}}

# Available props zperfbenchmark zuseasync zworldname zbenchmode zwarmuptime zmeasuretime zwarmups zmeasures"
perfBiomesAsync +args="":
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="biomegen" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="biomegen" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzperfbenchmark="biomegen" -Pzworldname="end" {{args}}

    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzmod="true" -Pzperfbenchmark="biomegen" -Pzworldname="overworld" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzmod="true" -Pzperfbenchmark="biomegen" -Pzworldname="nether" {{args}}
    ./gradlew :perf:runServer -Pzuseasync="/opt/async-profiler/lib/libasyncProfiler.so" -Pzmod="true" -Pzperfbenchmark="biomegen" -Pzworldname="end" {{args}}

parity:
    ./gradlew :perf:runServer -Pzperfbenchmark="parity"

task taskName:
    ./gradlew {{taskName}}