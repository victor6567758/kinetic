package org.kinetic.heap;

public interface IEventSink<T extends Comparable<T>> {

  void onBubbleUpEventBeforeSwap(IHeap<T> heap, int idx, int parentIdx);

  void onBubbleUpEventAfterSwap(IHeap<T> heap, int idx, int parentIdx);

  void onBubbleUpEventNoChange(IHeap<T> heap, int idx);

  void onBubbleDownEventBeforeSwap(IHeap<T> heap, int idx, int parentIdx);

  void onBubbleDownEventAfterSwap(IHeap<T> heap, int idx, int parentIdx);

  void onBubbleDownEventNoChange(IHeap<T> heap, int idx);
}
