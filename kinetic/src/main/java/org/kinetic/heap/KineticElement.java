package org.kinetic.heap;

import com.google.common.annotations.VisibleForTesting;
import java.text.DecimalFormat;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

@RequiredArgsConstructor
@Getter
public class KineticElement implements Comparable<KineticElement> {

  private final static DecimalFormat FORMATTER = new DecimalFormat("#0.00");


  private final int id;
  private final double initialPriority;
  private final double rate;
  private final Supplier<Integer> timeSupplier;

  @Setter
  private Certificate certificate;

  public double getPriority() {
    return initialPriority + rate * timeSupplier.get();
  }


  public void invalidateCertificate(Heap<Certificate> certificateHeap) {
    if (certificate != null) {
      certificateHeap.remove(certificate.getOwnIdx());
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
  public int compareTo(KineticElement other) {
    return Double.compare(getPriority(), other.getPriority());
  }


  @Override
  public String toString() {
    return "[" + id + "] " + "P" + timeSupplier.get() + ": " + getPriority() + ", R:"
        + FORMATTER.format(getRate()) + ", C: " + (certificate != null ? certificate : "N/A");
  }

  @VisibleForTesting
  public String toRow() {
    return id + "," + rate + "," + initialPriority;
  }



  public KineticElement createCopy(Supplier<Integer> timeSupplier) {
    return new KineticElement(id, rate, initialPriority, timeSupplier);
  }
}
