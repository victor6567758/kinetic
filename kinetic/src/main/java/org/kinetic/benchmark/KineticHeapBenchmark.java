package org.kinetic.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.kinetic.Utils;
import org.kinetic.heap.IKineticHeap;
import org.kinetic.heap.KineticElement;
import org.kinetic.heap.KineticHeap;
import org.kinetic.heap.KineticHeapTrivial;
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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms20G", "-Xmx20G"})
public class KineticHeapBenchmark {


  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
        .include(KineticHeapBenchmark.class.getSimpleName())
        .forks(1)
        .build();

    new Runner(opt).run();
  }

  @State(Scope.Thread)
  public static class MyState {

    @Param({"10", "100", "1000", "10000", "100000"})
    private int n;


    @Param({"100"})
    private int maxTimeSteps;

    private List<KineticElement> data;
    private IKineticHeap kineticHeapInserts;
    private IKineticHeap kineticHeapRemoves;
    private IKineticHeap heapInserts;
    private IKineticHeap heapRemoves;

    private int lastTime;
    private int timeStepDuration;

    @Setup(Level.Trial)
    public void doSetup() {
      data = createData();
      kineticHeapRemoves = new KineticHeap();
      kineticHeapInserts = new KineticHeap();
      heapInserts = new KineticHeapTrivial();
      heapRemoves = new KineticHeapTrivial();

      data.forEach(x -> kineticHeapRemoves.insert(x));
      data.forEach(x -> heapRemoves.insert(x));

      List<KineticElement[]> pairs = Utils.kineticElementsPermutations(data);
      lastTime = (int)pairs.stream().map(p -> p[0].getIntersectionTime(p[1]))
          .filter(p -> p >= 0).mapToDouble(x -> x).max().orElse(-1.0);

      timeStepDuration = lastTime / maxTimeSteps;
      if (timeStepDuration == 0) {
        timeStepDuration = 1;
      }

      System.out.println("Do Setup: heap for removal size: " + heapRemoves.size());
      System.out.println("Do Setup: kinetic heap for removal size: " + kineticHeapRemoves.size());
      System.out.println("Do Setup: last time detected: " + lastTime);
      System.out.println("Do Setup: Time step duration: " + timeStepDuration);
    }

    private List<KineticElement> createData() {
      List<KineticElement> kineticElements = new ArrayList<>();

      for (int id = 1; id <= n; id++) {
        kineticElements.add(
            new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
                ThreadLocalRandom.current().nextDouble(0.5, 2.0),
                () -> kineticHeapInserts.getCurTime()));
      }

      return kineticElements;
    }

  }

  @Benchmark
  public void kineticHeapInserts(MyState state, Blackhole bh) {
    for (KineticElement element : state.data) {
      state.kineticHeapInserts.insert(element);
    }
  }

  @Benchmark
  public void kineticHeapRemoves(MyState state, Blackhole bh) {
    while (true) {
      KineticElement element = state.kineticHeapRemoves.extractMin();
      if (element == null) {
        break;
      }
    }
  }

  @Benchmark
  public void trivialHeapInserts(MyState state, Blackhole bh) {
    for (KineticElement element : state.data) {
      state.heapInserts.insert(element);
    }
  }

  @Benchmark
  public void trivialHeapRemoves(MyState state, Blackhole bh) {
    while (true) {
      KineticElement element = state.heapRemoves.extractMin();
      if (element == null) {
        break;
      }
    }
  }

  @Benchmark
  public void trivialHeapAddTimeForward(MyState state, Blackhole bh) {
    state.data.forEach(e -> {
      state.heapInserts.insert(e);
    });

    int t = 0;
    while (t <= state.lastTime) {
      state.heapInserts.fastForward(t);
      t += state.timeStepDuration;
    }
  }

  @Benchmark
  public void kineticHeapAddTimeForward(MyState state, Blackhole bh) {
    state.data.forEach(e -> {
      state.kineticHeapInserts.insert(e);
    });

    int t = 0;
    while (t <= state.lastTime) {
      state.kineticHeapInserts.fastForward(t);
      t += state.timeStepDuration;
    }
  }

}
