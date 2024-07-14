package org.kinetic.heap;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;

public class KineticHeapTrivial implements IKineticHeap {

  private final Heap<KineticElement> heap = new Heap<>(null);

  private int curTime;

  @Override
  public void fastForward(int nextTime) {
    if (nextTime <= curTime) {
      return;
    }
    curTime = nextTime;

    List<KineticElement> copy = heap.createListCopy();
    heap.clear();
    for (KineticElement element : copy) {
      heap.insert(element);
    }

  }

  /*package*/
  @VisibleForTesting
  KineticElement getValue(int idx) {
    return heap.getValue(idx);
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
  }
}
