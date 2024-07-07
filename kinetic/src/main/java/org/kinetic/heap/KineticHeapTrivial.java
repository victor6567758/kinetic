package org.kinetic.heap;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class KineticHeapTrivial implements IKineticHeap {

  @Getter
  private final Heap<KineticElement> heap = new Heap<>(null);

  private int curTime;

  @Override
  public void fastForward(int nextTime) {
    if (nextTime <= curTime) {
      return;
    }
    curTime = nextTime;

    List<KineticElement> copy = new ArrayList<>(heap.getHeapList());
    heap.clear();
    for (KineticElement element : copy) {
      heap.insert(element);
    }

  }

  @Override
  public int getCurTime() {
    return curTime;
  }

  @Override
  public KineticElement extractMin() {
    return heap.extractMin();
  }

  @Override
  public KineticElement getMin() {
    return heap.getMin();
  }

  @Override
  public void insert(KineticElement data) {
    heap.insert(data);
  }

  @Override
  public int size() {
    return heap.size();
  }

  @Override
  public void clear() {
    heap.clear();
    heap.clear();
  }
}
