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
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, jvmArgs = {"-Xms10G", "-Xmx10G"})
@Threads(value = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class KineticHeapBenchmark {



  @State(Scope.Thread)
  public static class StateHolder {
    @Param({"10", "100", "1000", "10000"})
    private int n;


    @Param({"100"})
    private int maxTimeSteps;

    private List<KineticElement> initialData;

    private IKineticHeap kineticHeapInserts;
    private IKineticHeap kineticHeapRemoves;

    private IKineticHeap heapInserts;
    private IKineticHeap heapRemoves;

    private int lastTime;
    private int timeStepDuration;


    @Setup(Level.Invocation)
    public void doSetup() {
      kineticHeapRemoves = new KineticHeap();
      heapRemoves = new KineticHeapTrivial();

      kineticHeapInserts = new KineticHeap();
      heapInserts = new KineticHeapTrivial();

      initialData = createKineticInsertsData();

      initialData.forEach(x -> kineticHeapRemoves.insert(x.createCopy(() -> kineticHeapRemoves.getCurTime())));
      initialData.forEach(x -> heapRemoves.insert(x.createCopy(() -> -1)));

      lastTime = (int)Utils.maxTimeForPermutations(initialData);

      timeStepDuration = lastTime / maxTimeSteps;
      if (timeStepDuration == 0) {
        timeStepDuration = 1;
      }

    }

    @TearDown(Level.Trial)
    public void doTearDown() {
    }

    private List<KineticElement> createKineticInsertsData() {
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

  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
        .include(KineticHeapBenchmark.class.getSimpleName())
        .build();

    new Runner(opt).run();
  }



  @Benchmark
  public int kineticHeapInserts(StateHolder stateHolder,  Blackhole bh) {

    for (KineticElement element : stateHolder.initialData) {
      stateHolder.kineticHeapInserts.insert(element);
    }
    return stateHolder.kineticHeapInserts.size();
  }

  @Benchmark
  public int trivialHeapInserts(StateHolder stateHolder, Blackhole bh) {
    for (KineticElement element : stateHolder.initialData) {
      stateHolder.heapInserts.insert(element);
    }
    return stateHolder.kineticHeapInserts.size();
  }

  @Benchmark
  public int kineticHeapRemoves(StateHolder stateHolder, Blackhole bh) {
    while (true) {
      KineticElement element = stateHolder.kineticHeapRemoves.extractMin();
      if (element == null) {
        break;
      }
    }
    return stateHolder.kineticHeapRemoves.size();
  }

  @Benchmark
  public int trivialHeapRemoves(StateHolder stateHolder, Blackhole bh) {
    while (true) {
      KineticElement element = stateHolder.heapRemoves.extractMin();
      if (element == null) {
        break;
      }
    }
    return stateHolder.heapRemoves.size();
  }



  @Benchmark
  public void trivialHeapAddTimeForward(StateHolder stateHolder, Blackhole bh) {
    stateHolder.initialData.forEach(e -> {
      stateHolder.heapInserts.insert(e);
    });

    int t = 0;
    while (t <= stateHolder.lastTime) {
      stateHolder.heapInserts.fastForward(t);
      t += stateHolder.timeStepDuration;
    }
  }

  @Benchmark
  public void kineticHeapAddTimeForward(StateHolder stateHolder, Blackhole bh) {
    stateHolder.initialData.forEach(e -> {
      stateHolder.kineticHeapInserts.insert(e);
    });

    int t = 0;
    while (t <= stateHolder.lastTime) {
      stateHolder.kineticHeapInserts.fastForward(t);
      t += stateHolder.timeStepDuration;
    }
  }




}
