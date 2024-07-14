package org.kinetic.heap;

import com.google.common.annotations.VisibleForTesting;

public class KineticHeap implements IKineticHeap {

  private final Heap<KineticElement> heap = new Heap<>(null);

  private final Heap<Certificate> certificates = new Heap<>(new CertificateEventSink());

  private int curTime;

  private class CertificateEventSink implements IEventSink<Certificate> {

    @Override
    public void onBubbleUpEventBeforeSwap(IHeap<Certificate> heap, int idx, int parentIdx) {

    }

    @Override
    public void onBubbleUpEventAfterSwap(IHeap<Certificate> heap, int idx, int parentIdx) {
      setCertificateIndex(idx);
      setCertificateIndex(parentIdx);
    }


    @Override
    public void onBubbleDownEventBeforeSwap(IHeap<Certificate> heap, int idx, int parentIdx) {

    }

    @Override
    public void onBubbleDownEventAfterSwap(IHeap<Certificate> heap, int idx, int parentIdx) {
      setCertificateIndex(idx);
      setCertificateIndex(parentIdx);
    }

    @Override
    public void onBubbleUpEventNoChange(IHeap<Certificate> heap, int idx) {
      setCertificateIndex(idx);
    }

    @Override
    public void onBubbleDownEventNoChange(IHeap<Certificate> heap, int idx) {
      setCertificateIndex(idx);
    }
  }


  @Override
  public void insert(KineticElement data) {
    if (data == null) {
      throw new IllegalArgumentException("Invalid data");
    }
    heap.appendValue(data);
    heapUp();
  }

  @Override
  public KineticElement extractMin() {
    KineticElement minElement = getMin();

    if (minElement != null) {
      int lastIdx = heap.size() - 1;

      heap.getValue(lastIdx).invalidateCertificate(certificates);
      KineticElement old = heap.setValue(heap.getValue(lastIdx), 0);
      heap.remove(heap.size() - 1);

      heapDown();
      return old;
    }

    return null;
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

    curTime = nextTime;

    while (true) {

      Certificate minCertificate = certificates.getMin();
      if (minCertificate == null || certificates.getMin().getExpirationTime() > nextTime) {
        break;
      }

      Certificate certificate = certificates.getMin();

      if (certificate.getOwnIdx() == -1) {
        throw new IllegalArgumentException();
      }

      int elemIdx = certificate.getElementIdx();
      int parentIdx = Heap.getParent(elemIdx);
      invalidateCertificates(elemIdx, parentIdx);

      heap.swap(elemIdx, parentIdx);

      insertCertificates(elemIdx, certificate.getExpirationTime());

    }
  }

  public KineticElement getValue(int idx) {
    return heap.getValue(idx);
  }

  /*package*/
  @VisibleForTesting
  Heap<Certificate> getCertificates() {
    return certificates;
  }

  /*package*/
  @VisibleForTesting
  Heap<KineticElement> getHeap() {
    return heap;
  }

  private void invalidateCertificates(int idx, int parentIdx) {
    if (idx < heap.size()) {
      heap.getValue(idx).invalidateCertificate(certificates);
    }

    if (idx != Heap.getRoot()) {
      heap.getValue(parentIdx).invalidateCertificate(certificates);
    }

    int siblingCertIdx = Heap.getSibling(idx);
    if (siblingCertIdx < heap.size()) {
      heap.getValue(siblingCertIdx).invalidateCertificate(certificates);
      int leftIdx = Heap.getLeftChild(idx);
      if (leftIdx < heap.size()) {
        heap.getValue(leftIdx).invalidateCertificate(certificates);
        int rightIdx = Heap.getRightChild(idx);
        if (rightIdx < heap.size()) {
          heap.getValue(rightIdx).invalidateCertificate(certificates);
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
      int smallestChildIndex =
          hasRight && heap.getValue(rightChildIndex).compareTo(heap.getValue(leftChildIndex)) < 0
              ? rightChildIndex : leftChildIndex;

      if (heap.getValue(smallestChildIndex).compareTo(heap.getValue(curIndex)) < 0) {
        invalidateCertificates(smallestChildIndex, curIndex);
        heap.swap(smallestChildIndex, curIndex);
        insertCertificates(smallestChildIndex, curTime);
      } else {
        break;
      }
      curIndex = smallestChildIndex;
    }

    return curIndex;
  }

  private int heapUp() {
    int curIndex = heap.size() - 1;
    while (curIndex > Heap.getRoot()) {
      int parentIndex = Heap.getParent(curIndex);
      if (heap.getValue(curIndex).compareTo(heap.getValue(parentIndex)) < 0) {
        if (curIndex != 0) {
          invalidateCertificates(curIndex, parentIndex);
        }

        heap.swap(curIndex, parentIndex);
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
    KineticElement thisElement = heap.getValue(idx);

    int parentIdx = Heap.getParent(idx);
    KineticElement parentElement = heap.getValue(parentIdx);

    double intersection = thisElement.getIntersectionTime(parentElement);
    if (intersection > newTime) {
      Certificate certificate = new Certificate(idx, intersection);
      thisElement.setCertificate(certificate);

      certificates.insert(certificate);
    }

  }


  private void setCertificateIndex(int idx) {
    if (idx < certificates.size()) {
      Certificate certificate = certificates.getValue(idx);
      certificate.setOwnIdx(idx);
    }
  }


}
