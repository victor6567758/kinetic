package org.kinetic.heap;

import java.text.DecimalFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class KineticElement implements Comparable<KineticElement> {

  private final static DecimalFormat FORMATTER = new DecimalFormat("#0.00");

  private final int id;
  private final double initialPriority;
  private final double rate;
  private final KineticHeap heap;

  @Setter
  private Certificate certificate;

  public double getPriority() {
    return initialPriority + rate * heap.getCurTime();
  }


  public void invalidateCertificate() {
    if (certificate != null) {
      certificate.setValid(false);
      certificate = null;
    }
  }


  public double getIntersectionTime(KineticElement other) {
    if (rate - other.rate == 0) {
      return Double.NEGATIVE_INFINITY;
    }
    return (other.initialPriority - initialPriority) / (rate - other.rate);
  }

  @Override
  public String toString() {
    return "[" + id + "]" + "[" + FORMATTER.format(rate) + "]" + "  (P0: " + FORMATTER.format(
        initialPriority) + ") " + " (P"
        + heap.getCurTime() + ": " + FORMATTER.format(getPriority()) + ") C: " + certToString();
  }


  @Override
  public int compareTo(KineticElement other) {
    double thisPriority = getPriority();
    double otherPriority = other.getPriority();

    return Double.compare(thisPriority, otherPriority);
  }

  private String certToString() {
    if (certificate == null || certificate.getElementIdx() == Heap.getRoot()) {
      return "N/A";
    }

    Heap<KineticElement> mainHeap = heap.getHeap();
    return "[" + mainHeap.getValue(Heap.getParent(certificate.getElementIdx())).id + "]<[" + id
        + "] I: "
        + FORMATTER.format(certificate.getExpirationTime());
  }


}
