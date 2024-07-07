package org.kinetic.heap;

public interface IHeap<T extends Comparable<T>> {

  T extractMin();

  T getMin();

  void insert(T data);

  int size();

  void clear();
}
