package org.kinetic.heap;

public interface IEventSink {

  void onBubbleUpEventBeforeSwap(int idx, int parentIdx);

  void onBubbleUpEventAfterSwap(int idx, int parentIdx);

  void onBubbleUpEventNoChange(int idx);

  void onBubbleDownEventBeforeSwap(int idx, int parentIdx);

  void onBubbleDownEventAfterSwap(int idx, int parentIdx);

  void onBubbleDownEventNoChange(int idx);
}
