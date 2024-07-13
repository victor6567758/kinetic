package org.kinetic.heap;

import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Heap<T extends Comparable<T>> implements IHeap<T> {

  private final List<T> heap = new ArrayList<>();

  private final IEventSink<T> eventSink;

  @Override
  public void clear() {
    heap.clear();
  }

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

  public void swap(int idx1, int idx2) {
    Collections.swap(heap, idx1, idx2);
  }


  public static int getParent(int idx) {
    if (idx == 0) {
      throw new IllegalArgumentException("Cannot get parent from root");
    }
    return (idx - 1) >> 1;
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

    return idx % 2 == 0 ? idx - 1 : idx + 1;
  }

  public T getValue(int idx) {
    return heap.get(idx);
  }

  public T setValue(T value, int idx) {
    return heap.set(idx, value);
  }

  public void appendValue(T value) {
    heap.add(value);
  }

  public int heapUp(int index) {
    int curIndex = index;
    while (curIndex > Heap.getRoot()) {
      int parentIndex = Heap.getParent(curIndex);
      if (getValue(curIndex).compareTo(getValue(parentIndex)) < 0) {
        if (eventSink != null) {
          eventSink.onBubbleUpEventBeforeSwap(this, curIndex, parentIndex);
        }
        swap(curIndex, parentIndex);
        if (eventSink != null) {
          eventSink.onBubbleUpEventAfterSwap(this, curIndex, parentIndex);
        }
      } else {
        break;
      }

      curIndex = parentIndex;
    }

    if (eventSink != null) {
      if (curIndex == index) {
        eventSink.onBubbleUpEventNoChange(this, curIndex);
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
          eventSink.onBubbleDownEventBeforeSwap(this, smallestIndex, curIndex);
        }
        swap(smallestIndex, curIndex);
        if (eventSink != null) {
          eventSink.onBubbleDownEventAfterSwap(this, smallestIndex, curIndex);
        }
      } else {
        break;
      }
      curIndex = smallestIndex;
    }

    if (eventSink != null) {
      if (curIndex == index) {
        eventSink.onBubbleDownEventNoChange(this, curIndex);
      }
    }
    return curIndex;
  }

  public T remove(int idx) {
    T old = heap.set(idx, heap.get(heap.size() - 1));
    heap.remove(heap.size() - 1);

    int index = heapDown(idx);
    if (index == idx && index < heap.size()) {
      index = heapUp(index);
    }

    return old;
  }

  public List<T> createListCopy() {
    return new ArrayList<>(heap);
  }


  @VisibleForTesting
  @SuppressWarnings("unchecked")
  T[] getHeapArray(Class<T> clazz) {
    return (T[]) heap.toArray((T[])Array.newInstance(clazz, heap.size()));
  }

  /*package*/
  @VisibleForTesting
  List<T> getHeapList() {
    return heap;
  }


}
