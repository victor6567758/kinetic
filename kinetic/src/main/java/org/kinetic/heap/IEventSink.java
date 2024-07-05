package org.kinetic.heap;

public interface IEventSink {

  void onBubbleUpEventBeforeSwap(int childIdx, int parentIdx);

  void onBubbleUpEventAfterSwap(int childIdx, int parentIdx);

  void onBubbleDownEventBeforeSwap(int childIdx, int parentIdx);

  void onBubbleDownEventAfterSwap(int childIdx, int parentIdx);
}
