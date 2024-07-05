package org.kinetic.heap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Heap<T extends Comparable<T>> implements IHeap<T> {

  private final List<T> heap = new ArrayList<>();

  private final IEventSink eventSink;

  @Override
  public void insert(T data) {
    if (data == null) {
      throw new IllegalArgumentException("Invalid data");
    }
    heap.add(data);
    heapUp(heap.size() - 1);
  }

  @Override
  public T extractMin() {
    T minElement = getMin();

    if (minElement != null) {
      remove(getRoot());
    }

    return minElement;
  }

  @Override
  public T getMin() {
    if (heap.isEmpty()) {
      return null;
    }
    return getValue(0);
  }

  @Override
  public int size() {
    return heap.size();
  }

  public List<T> getHeapList() {
    return heap;
  }

  public T[]  getHeapArray(Class<T> clazz) {
    return (T[]) heap.toArray((T[])Array.newInstance(clazz, heap.size()));
  }

  public static int getParent(int idx) {
    if (idx == 0) {
      throw new IllegalArgumentException("Cannot get parent from root");
    }
    return (idx - 1) / 2;
  }

  public static int getLeftChild(int idx) {
    return 2 * idx + 1;
  }

  public static int getRightChild(int idx) {
    return 2 * idx + 2;
  }

  public static int getRoot() {
    return 0;
  }

  public static int getSibling(int idx) {

    if (idx == Heap.getRoot()) {
      throw new IllegalArgumentException("Root cannot have siblings");
    }

    if (idx % 2 == 0) {
      return idx - 1;
    } else {
      return idx + 1;
    }

  }

  public T getValue(int idx) {
    return heap.get(idx);
  }

  public int heapUp(int index) {
    int curIndex = index;
    while (curIndex > getRoot()) {
      int parentIndex = getParent(curIndex);
      if (getValue(curIndex).compareTo(getValue(parentIndex)) < 0) {
        if (eventSink != null) {
          eventSink.onBubbleUpEventBeforeSwap(curIndex, parentIndex);
        }
        Collections.swap(heap, curIndex, parentIndex);
        if (eventSink != null) {
          eventSink.onBubbleUpEventAfterSwap(curIndex, parentIndex);
        }
      } else {
        break;
      }

      curIndex = parentIndex;
    }

    if (eventSink != null) {
      if (curIndex == index && curIndex != Heap.getRoot()) {
        eventSink.onBubbleUpEventBeforeSwap(curIndex, Heap.getParent(curIndex));
        eventSink.onBubbleUpEventAfterSwap(curIndex, Heap.getParent(curIndex));
      }
    }
    return curIndex;
  }

  public int heapDown(int index) {
    int curIndex = index;
    int size = heap.size();
    while (true) {
      int leftChildIndex = getLeftChild(curIndex);
      int rightChildIndex = getRightChild(curIndex);
      if (leftChildIndex >= size) {
        break;
      }

      boolean hasRight = rightChildIndex < size;
      int smallestIndex =
          hasRight && getValue(rightChildIndex).compareTo(getValue(leftChildIndex)) < 0
              ? rightChildIndex : leftChildIndex;

      if (getValue(smallestIndex).compareTo(getValue(curIndex)) < 0) {
        if (eventSink != null) {
          eventSink.onBubbleDownEventBeforeSwap(smallestIndex, curIndex);
        }
        Collections.swap(heap, smallestIndex, curIndex);
        if (eventSink != null && curIndex != Heap.getRoot()) {
          eventSink.onBubbleDownEventAfterSwap(curIndex, Heap.getParent(curIndex));
        }
      } else {
        break;
      }
      curIndex = smallestIndex;
    }

    if (eventSink != null) {
      if (curIndex == index && curIndex != Heap.getRoot()) {
        eventSink.onBubbleDownEventBeforeSwap(curIndex, Heap.getParent(curIndex));
        eventSink.onBubbleUpEventAfterSwap(curIndex, Heap.getParent(curIndex));
      }
    }
    return curIndex;
  }

  public void remove(int idx) {
    heap.set(idx, heap.get(heap.size() - 1));
    heap.remove(heap.size() - 1);

    int index = heapDown(idx);
    if (index == idx && index < heap.size()) {
      heapUp(index);
    }
  }


}
