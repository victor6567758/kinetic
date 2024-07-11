package org.kinetic.heap;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

public class KineticHeap implements IKineticHeap {

  @Getter
  private final Heap<KineticElement> heap = new Heap<>(null);

  @Getter
  private final Heap<Certificate> certificates = new Heap<>(new CertificateEventSink());

  private int prevTime;

  private int curTime;


  private class CertificateEventSink implements IEventSink {

    @Override
    public void onBubbleUpEventBeforeSwap(int idx, int parentIdx) {

    }

    @Override
    public void onBubbleUpEventAfterSwap(int idx, int parentIdx) {
      setCertificateIndex(idx);
      setCertificateIndex(parentIdx);
    }


    @Override
    public void onBubbleDownEventBeforeSwap(int idx, int parentIdx) {

    }

    @Override
    public void onBubbleDownEventAfterSwap(int idx, int parentIdx) {
      setCertificateIndex(idx);
      setCertificateIndex(parentIdx);
    }

    @Override
    public void onBubbleUpEventNoChange(int idx) {
      setCertificateIndex(idx);
    }

    @Override
    public void onBubbleDownEventNoChange(int idx) {
      setCertificateIndex(idx);
    }
  }




  @Override
  public void insert(KineticElement data) {
    if (data == null) {
      throw new IllegalArgumentException("Invalid data");
    }
    heap.getHeapList().add(data);
    heapUp();
  }

  @Override
  public KineticElement extractMin() {
    KineticElement minElement = getMin();

    if (minElement != null) {
      int lastIdx = heap.getHeapList().size() - 1;

      // remove certificate from the last
      heap.getHeapList().get(lastIdx).invalidateCertificate(certificates);
      KineticElement old = heap.getHeapList().set(0, heap.getHeapList().get(lastIdx));
      heap.remove(heap.size() - 1);

      int index = heapDown();
      return old;
    }

    return minElement;
  }

  @Override
  public int getCurTime() {
    return curTime;
  }

  @Override
  public KineticElement getMin() {
    return heap.getMin();
  }


  @Override
  public void clear() {
    heap.clear();
    certificates.clear();
  }

  @Override
  public void fastForward(int nextTime) {
    if (nextTime <= curTime) {
      return;
    }

    prevTime = curTime;
    curTime = nextTime;

    while (true) {

      Certificate minCertificate = certificates.getMin();
      if (minCertificate == null || certificates.getMin().getExpirationTime() > nextTime) {
        break;
      }

      // remove the current
      Certificate certificate = certificates.getMin();

      if (certificate.getOwnIdx() == -1) {
        throw new IllegalArgumentException();
      }

      int elemIdx = certificate.getElementIdx();

      // 1 invalidate certificates
      invalidateCertificates(elemIdx);

      // 2 swap
      Collections.swap(heap.getHeapList(), elemIdx, Heap.getParent(elemIdx));

      // 3
      insertCertificates(elemIdx, certificate.getExpirationTime());

//      curTime = prevTime;
//      KineticElement old = heap.remove(elemIdx);
//
//      curTime = nextTime;
//      heap.insert(old);

//      int moveIndex = heap.heapDown(elemIdx);
//      if (moveIndex == elemIdx && moveIndex < heap.size()) {
//        moveIndex = heap.heapUp(moveIndex);
//      }

    }
  }


  private void invalidateCertificates(int idx) {
    // no kinetic elements, so do not care about the time
    List<KineticElement> heapList = heap.getHeapList();

    if (idx < heap.size()) {
      heapList.get(idx).invalidateCertificate(certificates);
    }

    if (idx != Heap.getRoot()) {
      heapList.get(Heap.getParent(idx)).invalidateCertificate(certificates);
    }

    int siblingCertIdx = Heap.getSibling(idx);
    if (siblingCertIdx < heap.size()) {
      heapList.get(siblingCertIdx).invalidateCertificate(certificates);
      int leftIdx = Heap.getLeftChild(idx);
      if (leftIdx < heap.size()) {
        heapList.get(leftIdx).invalidateCertificate(certificates);
        int rightIdx = Heap.getRightChild(idx);
        if (rightIdx < heap.size()) {
          heapList.get(rightIdx).invalidateCertificate(certificates);
        }
      }
    }


  }

  private void insertCertificates(int idx, double time) {

    // must be new time
    if (idx < heap.size()) {
      createAndMaybeAddCertificate(idx, time);
    }

    if (idx != Heap.getRoot()) {
      createAndMaybeAddCertificate(Heap.getParent(idx), time);
    }

    int siblingCertIdx = Heap.getSibling(idx);
    if (siblingCertIdx < heap.size()) {
      createAndMaybeAddCertificate(siblingCertIdx, time);
      int leftIdx = Heap.getLeftChild(idx);
      if (leftIdx < heap.size()) {
        createAndMaybeAddCertificate(leftIdx, time);
        int rightIdx = Heap.getRightChild(idx);
        if (rightIdx < heap.size()) {
          createAndMaybeAddCertificate(rightIdx, time);
        }
      }
    }

  }

  @Override
  public int size() {
    return heap.size();
  }

  private int heapDown() {
    int curIndex = 0;
    int size = heap.size();
    while (true) {
      int leftChildIndex = Heap.getLeftChild(curIndex);
      int rightChildIndex = Heap.getRightChild(curIndex);
      if (leftChildIndex >= size) {
        break;
      }

      boolean hasRight = rightChildIndex < size;
      int smallestIndex =
          hasRight && heap.getValue(rightChildIndex).compareTo(heap.getValue(leftChildIndex)) < 0
              ? rightChildIndex : leftChildIndex;

      if (heap.getValue(smallestIndex).compareTo(heap.getValue(curIndex)) < 0) {
        invalidateCertificates(smallestIndex);
        Collections.swap(heap.getHeapList(), smallestIndex, curIndex);
        insertCertificates(smallestIndex, curTime);
      } else {
        break;
      }
      curIndex = smallestIndex;
    }

    return curIndex;
  }

  private int heapUp() {
    int curIndex = heap.size() - 1;
    while (curIndex > Heap.getRoot()) {
      int parentIndex = Heap.getParent(curIndex);
      if (heap.getValue(curIndex).compareTo(heap.getValue(parentIndex)) < 0) {
        if (curIndex != 0) {
          invalidateCertificates(curIndex);
        }

        Collections.swap(heap.getHeapList(), curIndex, parentIndex);
        insertCertificates(curIndex, curTime);

      } else {
        break;
      }

      curIndex = parentIndex;
    }

    if (curIndex == heap.size() - 1) {
      createAndMaybeAddCertificate(curIndex, curTime);
    }
    return curIndex;
  }


  private void createAndMaybeAddCertificate(int idx, double newTime) {
    if (idx == Heap.getRoot()) {
      return;
    }
    KineticElement thisElement = heap.getHeapList().get(idx);

    int parentIdx = Heap.getParent(idx);
    KineticElement parentElement = heap.getHeapList().get(parentIdx);

    double intersection = thisElement.getIntersectionTime(parentElement);
    if (intersection > newTime
        /*|| intersection == newTime && thisElement.getRate() < parentElement.getRate() */) {
      Certificate certificate = new Certificate(idx, intersection);
      thisElement.setCertificate(certificate);
      certificates.insert(certificate);

    }

  }


  private void setCertificateIndex(int idx) {
    if (idx < certificates.size()) {
      Certificate certificate = certificates.getHeapList().get(idx);
      certificate.setOwnIdx(idx);
    }
  }


}
