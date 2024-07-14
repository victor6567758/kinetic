package org.kinetic.heap;

import java.text.DecimalFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class Certificate implements Comparable<Certificate> {

  private final static DecimalFormat FORMATTER = new DecimalFormat("#0.00");

  private final int elementIdx;
  private final double expirationTime;

  @Setter
  private int ownIdx = -1;

  @Override
  public int compareTo(Certificate other) {
    return Double.compare(expirationTime, other.expirationTime);
  }

  @Override
  public String toString() {
    return "Heap idx: " + ownIdx + ", element idx: " + elementIdx + ", expiration: " + FORMATTER.format(expirationTime);
  }


}
