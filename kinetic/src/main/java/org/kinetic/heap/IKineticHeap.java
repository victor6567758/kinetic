package org.kinetic.heap;

public interface IKineticHeap<T extends Comparable<T>> extends IHeap<T> {

  void fastForward(int timeToForward);
}
