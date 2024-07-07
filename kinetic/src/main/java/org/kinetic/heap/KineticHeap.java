package org.kinetic.heap;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

public class KineticHeap implements IKineticHeap, IEventSink {

  @Getter
  private final Heap<KineticElement> heap = new Heap<>(this);

  private final Heap<Certificate> certificates = new Heap<>(null);

  private int curTime;


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
    curTime = nextTime;

    while (true) {

      while (certificates.getMin() != null && !certificates.getMin().isValid()) {
        certificates.extractMin();
      }

      Certificate minCertificate = certificates.getMin();
      if (minCertificate == null || certificates.getMin().getExpirationTime() >= curTime) {
        break;
      }

      // remove the current
      Certificate certificate = certificates.extractMin();

      int certElemIdx = certificate.getElementIdx();
      KineticElement kineticElement = heap.getHeapList().get(certElemIdx);


//      invalidateCertificates(certElemIdx);
//      Collections.swap(heap.getHeapList(), certElemIdx, Heap.getParent(certElemIdx));
//      insertCertificates(certElemIdx);

      heap.remove(certElemIdx);
      heap.insert(kineticElement);

    }
  }

  private void invalidateCertificates(int idx) {
    int parentElemIdx = Heap.getParent(idx);
    List<KineticElement> mainHeapArray = heap.getHeapList();

    mainHeapArray.get(parentElemIdx).invalidateCertificate();

    // up to 5 certificates need to be invalidated
    if (idx < heap.size()) {
      mainHeapArray.get(idx).invalidateCertificate();
    }

    int siblingCertIdx = Heap.getSibling(idx);
    if (siblingCertIdx < heap.size()) {
      mainHeapArray.get(siblingCertIdx).invalidateCertificate();
      int leftIdx = Heap.getLeftChild(idx);
      if (leftIdx < heap.size()) {
        mainHeapArray.get(leftIdx).invalidateCertificate();
        int rightIdx = Heap.getRightChild(idx);
        if (rightIdx < heap.size()) {
          mainHeapArray.get(rightIdx).invalidateCertificate();
        }
      }
    }

  }

  private void insertCertificates(int idx) {
    int parentElemIdx = Heap.getParent(idx);
    createAndMaybeAddCertificate(parentElemIdx, curTime);
    if (idx < heap.size()) {
      createAndMaybeAddCertificate(idx, curTime);
    }

    int siblingCertIdx = Heap.getSibling(idx);
    if (siblingCertIdx < heap.size()) {
      createAndMaybeAddCertificate(siblingCertIdx, curTime);
      int leftIdx = Heap.getLeftChild(idx);
      if (leftIdx < heap.size()) {
        createAndMaybeAddCertificate(leftIdx, curTime);
        int rightIdx = Heap.getRightChild(idx);
        if (rightIdx < heap.size()) {
          createAndMaybeAddCertificate(rightIdx, idx);
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

  @Override
  public void onBubbleUpEventBeforeSwap(int childIdx, int parentIdx) {
    invalidateCertificates(childIdx);
  }

  @Override
  public void onBubbleUpEventAfterSwap(int childIdx, int parentIdx) {
    insertCertificates(childIdx);
  }

  @Override
  public void onBubbleDownEventBeforeSwap(int childIdx, int parentIdx) {
    invalidateCertificates(childIdx);
  }

  @Override
  public void onBubbleDownEventAfterSwap(int childIdx, int parentIdx) {
    insertCertificates(childIdx);
  }
}
