package org.kinetic.benchmark;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.kinetic.heap.Heap;
import org.kinetic.heap.IHeap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms15G", "-Xmx15G"})
public class TrivialHeapBenchmark {


  @State(Scope.Thread)
  public static class MyState {

    @Param({"10", "100", "1000", "10000", "100000", "1000000"})
    private int n;

    private List<Integer> data;
    private IHeap<Integer> heapInserts;
    private IHeap<Integer> heapRemoves;

    @Setup(Level.Trial)
    public void doSetup() {
      data = createData();
      heapRemoves = new Heap<>(null);
      heapInserts = new Heap<>(null);
      data.forEach( x -> heapRemoves.insert(x));
      System.out.println("Do Setup: heap for removal size" + heapRemoves.size());


    }

    @TearDown(Level.Trial)
    public void doTearDown() {
      System.out.println("Do TearDown");
    }

    private List<Integer> createData() {
      Random rand = ThreadLocalRandom.current();
      return rand.ints(n).boxed().toList();
    }


  }


  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
        .include(TrivialHeapBenchmark.class.getSimpleName())
        .forks(1)
        .build();

    new Runner(opt).run();
  }

  @Setup
  public void setup() {

  }

  @Benchmark
  public void inserts(MyState state, Blackhole bh) {
    for (int element: state.data) {
      state.heapInserts.insert(element);
    }
  }

  @Benchmark
  public void removes(MyState state, Blackhole bh) {
    while(true) {
      Integer element = state.heapRemoves.extractMin();
      if (element == null) {
        break;
      }
    }
  }




}
