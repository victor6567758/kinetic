package org.kinetic.heap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HeapTest {

  private Heap<Integer> heap;
  private List<Integer> sourceData;

  @AllArgsConstructor
  static class HeapPriorityElement implements Comparable<HeapPriorityElement> {

    private int priority;

    @Override
    public int compareTo(HeapPriorityElement o) {
      return priority - o.priority;
    }
  }

  @RequiredArgsConstructor
  static class HeapRefElement implements Comparable<HeapRefElement> {

    private final int element;

    private int ownIdx = -1;

    @Override
    public int compareTo(HeapRefElement o) {
      return element - o.element;
    }
  }

  @BeforeEach
  public void setUp() {
    heap = new Heap<>(null);
    sourceData = ThreadLocalRandom.current().ints(100000).boxed().toList();
  }

  @Test
  public void moveElementWithIncreasedPriority() {
    Heap<HeapPriorityElement> heapPriority = new Heap<>(null);
    List<HeapPriorityElement> input = new ArrayList<>(
        IntStream.range(1, 10).boxed().map(HeapPriorityElement::new).toList());
    Collections.shuffle(input);
    input.forEach(e -> {
      heapPriority.insert(e);

    });

    HeapPriorityElement[] heapArray = heapPriority.getHeapArray(HeapPriorityElement.class);
    assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();

    int idx = 0;
    heapArray[idx].priority += 100;

    int index = heapPriority.heapDown(idx);
    if (index == idx && index < heapPriority.size()) {
      index = heapPriority.heapUp(index);
    }

    heapArray = heapPriority.getHeapArray(HeapPriorityElement.class);
    assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();

  }

  @Test
  public void moveElementWithIncreasedPriorityRandom() {
    Heap<HeapPriorityElement> heapPriority = new Heap<>(null);
    List<HeapPriorityElement> input = new ArrayList<>(
        IntStream.range(1, 1000).boxed().map(HeapPriorityElement::new).toList());
    Collections.shuffle(input);
    input.forEach(e -> {
      heapPriority.insert(e);

    });

    HeapPriorityElement[] heapArray = heapPriority.getHeapArray(HeapPriorityElement.class);
    assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();

    int cnt = 10;
    while (true) {

      if (cnt++ >= 100) {
        break;
      }

      int idx = ThreadLocalRandom.current().nextInt(0, heapPriority.size());
      heapArray[idx].priority += 100;

      int index = heapPriority.heapDown(idx);
      if (index == idx && index < heapPriority.size()) {
        index = heapPriority.heapUp(index);
      }

      heapArray = heapPriority.getHeapArray(HeapPriorityElement.class);
      assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();
    }

  }

  @Test
  public void testHeapWithSelfReferencedElements() {
    Heap<HeapRefElement> selfRefHeap = new Heap<>(null);
    IEventSink sink = new IEventSink() {

      Heap<HeapRefElement> pThis;

      IEventSink init(Heap<HeapRefElement> heap) {
        pThis = heap;
        return this;
      }

      @Override
      public void onBubbleUpEventBeforeSwap(int idx, int parentIdx) {

      }

      @Override
      public void onBubbleUpEventAfterSwap(int idx, int parentIdx) {
        pThis.getHeapList().get(parentIdx).ownIdx = parentIdx;
        pThis.getHeapList().get(idx).ownIdx = idx;
      }

      @Override
      public void onBubbleUpEventNoChange(int idx) {
        pThis.getHeapList().get(idx).ownIdx = idx;
      }

      @Override
      public void onBubbleDownEventBeforeSwap(int idx, int parentIdx) {

      }

      @Override
      public void onBubbleDownEventAfterSwap(int idx, int parentIdx) {
        pThis.getHeapList().get(parentIdx).ownIdx = parentIdx;
        pThis.getHeapList().get(idx).ownIdx = idx;
      }

      @Override
      public void onBubbleDownEventNoChange(int idx) {
        pThis.getHeapList().get(idx).ownIdx = idx;
      }
    }.init(selfRefHeap);
    selfRefHeap.setEventSink(sink);

    List<HeapRefElement> input = IntStream.range(1, 1000).boxed().map(HeapRefElement::new).toList();
    input.forEach(e -> {
      selfRefHeap.insert(e);
      assertSelfHeap(selfRefHeap);

      HeapRefElement[] heapArray = selfRefHeap.getHeapArray(HeapRefElement.class);
      assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();

    });

  }

  @Test
  public void testHeapWithSelfReferencedElementsRemoveAfterInserts() {
    Heap<HeapRefElement> selfRefHeap = new Heap<>(null);
    IEventSink sink = new IEventSink() {

      Heap<HeapRefElement> pThis;

      IEventSink init(Heap<HeapRefElement> heap) {
        pThis = heap;
        return this;
      }

      @Override
      public void onBubbleUpEventBeforeSwap(int idx, int parentIdx) {

      }

      @Override
      public void onBubbleUpEventAfterSwap(int idx, int parentIdx) {
        pThis.getHeapList().get(parentIdx).ownIdx = parentIdx;
        pThis.getHeapList().get(idx).ownIdx = idx;
      }

      @Override
      public void onBubbleUpEventNoChange(int idx) {
        pThis.getHeapList().get(idx).ownIdx = idx;
      }

      @Override
      public void onBubbleDownEventBeforeSwap(int idx, int parentIdx) {

      }

      @Override
      public void onBubbleDownEventAfterSwap(int idx, int parentIdx) {
        pThis.getHeapList().get(parentIdx).ownIdx = parentIdx;
        pThis.getHeapList().get(idx).ownIdx = idx;
      }

      @Override
      public void onBubbleDownEventNoChange(int idx) {
        pThis.getHeapList().get(idx).ownIdx = idx;
      }
    }.init(selfRefHeap);
    selfRefHeap.setEventSink(sink);

    List<HeapRefElement> input = IntStream.range(1, 1000).boxed().map(HeapRefElement::new).toList();
    for (int i = 0; i < input.size() - 1; i += 2) {
      selfRefHeap.insert(input.get(i));
      selfRefHeap.insert(input.get(i + 1));

      selfRefHeap.extractMin();
      assertSelfHeap(selfRefHeap);

      HeapRefElement[] heapArray = selfRefHeap.getHeapArray(HeapRefElement.class);
      assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();

    }

  }


  @Test
  public void testHeapWithSelfReferencedElementsRemoveAfterInsertsMiddle() {
    Heap<HeapRefElement> selfRefHeap = new Heap<>(null);
    IEventSink sink = new IEventSink() {

      Heap<HeapRefElement> pThis;

      IEventSink init(Heap<HeapRefElement> heap) {
        pThis = heap;
        return this;
      }

      @Override
      public void onBubbleUpEventBeforeSwap(int idx, int parentIdx) {

      }

      @Override
      public void onBubbleUpEventAfterSwap(int idx, int parentIdx) {
        pThis.getHeapList().get(parentIdx).ownIdx = parentIdx;
        pThis.getHeapList().get(idx).ownIdx = idx;
      }

      @Override
      public void onBubbleUpEventNoChange(int idx) {
        if (idx < pThis.getHeapList().size()) {
          pThis.getHeapList().get(idx).ownIdx = idx;
        }
      }

      @Override
      public void onBubbleDownEventBeforeSwap(int idx, int parentIdx) {

      }

      @Override
      public void onBubbleDownEventAfterSwap(int idx, int parentIdx) {
        pThis.getHeapList().get(parentIdx).ownIdx = parentIdx;
        pThis.getHeapList().get(idx).ownIdx = idx;
      }

      @Override
      public void onBubbleDownEventNoChange(int idx) {
        if (idx < pThis.getHeapList().size()) {
          pThis.getHeapList().get(idx).ownIdx = idx;
        }
      }
    }.init(selfRefHeap);
    selfRefHeap.setEventSink(sink);

    List<HeapRefElement> input = IntStream.range(1, 1000).boxed().map(HeapRefElement::new).toList();
    for (int i = 0; i < input.size() - 1; i += 2) {
      selfRefHeap.insert(input.get(i));
      selfRefHeap.insert(input.get(i + 1));

      int removeIdx = selfRefHeap.size() / 2;
      selfRefHeap.remove(removeIdx);
      assertSelfHeap(selfRefHeap);

      HeapRefElement[] heapArray = selfRefHeap.getHeapArray(HeapRefElement.class);
      assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();

    }


  }

  @Test
  public void checkSiblingsIdx() {
    List<Integer> input = Arrays.asList(1, 3, 8, 20, 21, 25, 27);
    //      0   1   2   3   4   5   6
    Collections.shuffle(input);
    input.forEach(e -> heap.insert(e));

    assertThat(Heap.getSibling(1)).isEqualTo(2);
    assertThat(Heap.getSibling(2)).isEqualTo(1);

    assertThat(Heap.getSibling(4)).isEqualTo(3);
    assertThat(Heap.getSibling(3)).isEqualTo(4);

    assertThat(Heap.getSibling(5)).isEqualTo(6);
    assertThat(Heap.getSibling(6)).isEqualTo(5);
  }

  @Test
  public void checkChildIdx() {
    List<Integer> input = Arrays.asList(1, 3, 8, 20, 21, 25, 27);
    //      0   1   2   3   4   5   6
    Collections.shuffle(input);
    input.forEach(e -> heap.insert(e));

    assertThat(Heap.getLeftChild(0)).isEqualTo(1);
    assertThat(Heap.getRightChild(0)).isEqualTo(2);

    assertThat(Heap.getLeftChild(1)).isEqualTo(3);
    assertThat(Heap.getRightChild(1)).isEqualTo(4);

    assertThat(Heap.getLeftChild(2)).isEqualTo(5);
    assertThat(Heap.getRightChild(2)).isEqualTo(6);

  }

  @Test
  public void checkMinHeapProperty() {
    List<Integer> input = Arrays.asList(1, 3, 8, 20, 21);
    Collections.shuffle(input);
    input.forEach(e -> heap.insert(e));

    assertThat(heap.extractMin()).isEqualTo(1);
    assertThat(heap.extractMin()).isEqualTo(3);
    assertThat(heap.extractMin()).isEqualTo(8);
    assertThat(heap.extractMin()).isEqualTo(20);
    assertThat(heap.extractMin()).isEqualTo(21);
    assertThat(heap.size()).isZero();

  }

  @Test
  public void checkOrderingVanillaTest() {
    PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();

    List<Integer> input = Arrays.asList(1, 3, 8, 20, 21);
    Collections.shuffle(input);

    priorityQueue.addAll(input);
    Collections.shuffle(input);
    input.forEach(e -> heap.insert(e));

    assertThat(heap.size()).isEqualTo(priorityQueue.size());
    assertThat(heap.getMin()).isEqualTo(priorityQueue.peek());

    while (!priorityQueue.isEmpty()) {
      assertThat(priorityQueue.poll()).isEqualTo(heap.extractMin());
    }

  }

  @Test
  public void bulkTest() {
    PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();

    for (int element : sourceData) {
      heap.insert(element);
      priorityQueue.add(element);
    }

    while (!priorityQueue.isEmpty()) {
      assertThat(priorityQueue.poll()).isEqualTo(heap.extractMin());
    }

    assertThat(heap.size()).isEqualTo(priorityQueue.size());
  }

  @Test
  public void checkValidMinHeap() {
    sourceData.forEach(e -> heap.insert(e));
    Integer[] heapArray = heap.getHeapArray(Integer.class);
    assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();
  }

  @Test
  public void checkValidMinHeapAfterRemoveFromMiddle() {
    sourceData.forEach(e -> heap.insert(e));

    heap.remove(2);
    heap.remove(2);

    Integer[] heapArray = heap.getHeapArray(Integer.class);
    assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();
  }

  private <T extends Comparable<T>> boolean heapChecker(T[] nums, int i, int n) {
    if (i >= (n - 1) / 2) {
      return true;
    }

    return nums[i].compareTo(nums[2 * i + 1]) <= 0 && nums[i].compareTo(nums[2 * i + 2]) <= 0 &&
        heapChecker(nums, 2 * i + 1, n) && heapChecker(nums, 2 * i + 2, n);
  }

  private void assertSelfHeap(Heap<HeapRefElement> sourceHeap) {
    HeapRefElement[] elements = sourceHeap.getHeapArray(HeapRefElement.class);

    for (int i = 0; i < elements.length; i++) {
      assertThat(elements[i].ownIdx).isNotEqualTo(-1);
      assertThat(elements[i].ownIdx).isEqualTo(i);
    }
  }


}