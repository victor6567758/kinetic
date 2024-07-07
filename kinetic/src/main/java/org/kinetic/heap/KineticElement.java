package org.kinetic.heap;

import java.text.DecimalFormat;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class KineticElement implements Comparable<KineticElement> {


  private final int id;
  private final double initialPriority;
  private final double rate;
  private final Supplier<Integer> timeSupplier;

  @Setter
  private Certificate certificate;

  public double getPriority() {
    return initialPriority + rate * timeSupplier.get();
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
  public int compareTo(KineticElement other) {
    double thisPriority = getPriority();
    double otherPriority = other.getPriority();

    return Double.compare(thisPriority, otherPriority);
  }


}
