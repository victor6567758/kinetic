package org.kinetic.heap;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import lombok.Getter;

public class KineticHeap implements IKineticHeap {

  @Getter
  private final Heap<KineticElement> heap = new Heap<>(new HeapEventSink());

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


  private class HeapEventSink implements IEventSink {

    @Override
    public void onBubbleUpEventBeforeSwap(int idx, int parentIdx) {
      //invalidateCertificates(idx);
    }

    @Override
    public void onBubbleUpEventAfterSwap(int idx, int parentIdx) {
      //insertCertificates(idx);
    }


    @Override
    public void onBubbleDownEventBeforeSwap(int idx, int parentIdx) {
      //invalidateCertificates(idx);
    }

    @Override
    public void onBubbleDownEventAfterSwap(int idx, int parentIdx) {
      //insertCertificates(idx);
    }

    @Override
    public void onBubbleUpEventNoChange(int idx) {
      //invalidateCertificates(idx);
      //insertCertificates(idx);
    }

    @Override
    public void onBubbleDownEventNoChange(int idx) {
      //invalidateCertificates(idx);
      //insertCertificates(idx);
    }


  }

  @Override
  public KineticElement extractMin() {
    return heap.extractMin();
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
  public void insert(KineticElement data) {
    heap.insert(data);
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

      // 3

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

  }

  private void insertCertificates(int idx) {

    // must be new time
    if (idx < heap.size()) {
      createAndMaybeAddCertificate(idx, curTime);
    }

    if (idx != Heap.getRoot()) {
      createAndMaybeAddCertificate(Heap.getParent(idx), curTime);
      int siblingCertIdx = Heap.getSibling(idx);
      if (siblingCertIdx < heap.size()) {
        createAndMaybeAddCertificate(siblingCertIdx, curTime);
        int leftIdx = Heap.getLeftChild(idx);
        if (leftIdx < heap.size()) {
          createAndMaybeAddCertificate(leftIdx, curTime);
          int rightIdx = Heap.getRightChild(idx);
          if (rightIdx < heap.size()) {
            createAndMaybeAddCertificate(rightIdx, curTime);
          }
        }
      }
    }
  }

  @Override
  public int size() {
    return heap.size();
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
        || intersection == newTime && thisElement.getRate() < parentElement.getRate()) {
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
