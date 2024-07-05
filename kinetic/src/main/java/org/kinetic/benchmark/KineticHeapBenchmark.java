package org.kinetic.benchmark;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.kinetic.heap.Heap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms15G", "-Xmx15G"})
public class KineticHeapBenchmark {
  @Param({"100000"})
  private int N;

  private List<Integer> data;
  private Heap<Integer> heapInserts;
  private Heap<Integer> heapRemoves;


  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
        .include(TrivialHeapBenchmark.class.getSimpleName())
        .forks(1)
        .build();

    new Runner(opt).run();
  }

  @Setup
  public void setup() {
    data = createData();
    heapRemoves = new Heap<>(null);
    heapInserts = new Heap<>(null);
    for (int element: data) {
      heapInserts.insert(element);
    }
  }

  @Benchmark
  public void inserts(Blackhole bh) {
    for (int element: data) {
      heapInserts.insert(element);
    }
  }

  @Benchmark
  public void removes(Blackhole bh) {
    while(true) {
      Integer element = heapRemoves.extractMin();
      if (element == null) {
        break;
      }
    }
  }


  private List<Integer> createData() {
    Random rand = ThreadLocalRandom.current();
    return rand.ints(N).boxed().toList();
  }
}
