package org.kinetic.heap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrivialHeapTest {

  private Heap<Integer> heap;
  private List<Integer> sourceData;

  @BeforeEach
  public void setUp() {
    heap = new Heap<>(null);
    sourceData = ThreadLocalRandom.current().ints(100000).boxed().toList();
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

    for (int element: sourceData) {
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
    int[] heapArray = heap.getHeapList().stream().mapToInt(i -> i).toArray();
    assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();
  }

  @Test
  public void checkValidMinHeapAfterRemoveFromMiddle() {
    sourceData.forEach(e -> heap.insert(e));

    heap.remove(2);
    heap.remove(2);

    int[] heapArray = heap.getHeapList().stream().mapToInt(i -> i).toArray();
    assertThat(heapChecker(heapArray, 0, heapArray.length - 1)).isTrue();
  }

  private boolean heapChecker(int[] nums, int i, int n) {
    if (i >= (n - 1) / 2)
      return true;

    return nums[i] <= nums[2 * i + 1] && nums[i] <= nums[2 * i + 2] &&
        heapChecker(nums, 2 * i + 1, n) && heapChecker(nums, 2 * i + 2, n);
  }





}