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

perf:
    ./gradlew :perf:runServer -Pzperfbenchmark="overworldVanilla"
    ./gradlew :perf:runServer -Pzperfbenchmark="netherVanilla"
    ./gradlew :perf:runServer -Pzperfbenchmark="endVanilla"
    ./gradlew :perf:runServer -Pzperfbenchmark="overworldOptimized"
    ./gradlew :perf:runServer -Pzperfbenchmark="netherOptimized"
    ./gradlew :perf:runServer -Pzperfbenchmark="endOptimized"

task taskName:
    ./gradlew {{taskName}}