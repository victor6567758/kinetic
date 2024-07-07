#!/bin/bash

mvn clean install
# java -jar target/benchmarks.jar TrivialHeapBenchmark
java -jar target/benchmarks.jar KineticHeapBenchmark