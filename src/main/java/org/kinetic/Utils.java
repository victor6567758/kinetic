package org.kinetic;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.kinetic.heap.KineticElement;
import org.kinetic.heap.KineticHeap;

@UtilityClass
public class Utils {


  public static double maxTimeForPermutations(KineticHeap kineticHeap) {

    double maxTime = -1;
    for (int i = 0; i < kineticHeap.size(); i++) {
      for (int j = 0; j < kineticHeap.size(); j++) {
        if (i != j) {
          double intersection = kineticHeap.getValue(i).getIntersectionTime(kineticHeap.getValue(j));
          if (intersection >= 0) {
            maxTime = Math.max(maxTime, intersection);
          }

        }
      }
    }

    return maxTime;
  }

  public static double maxTimeForPermutations(List<KineticElement> kineticList) {

    double maxTime = -1;
    for (int i = 0; i < kineticList.size(); i++) {
      for (int j = 0; j < kineticList.size(); j++) {
        if (i != j) {
          double intersection = kineticList.get(i).getIntersectionTime(kineticList.get(j));
          if (intersection >= 0) {
            maxTime = Math.max(maxTime, intersection);
          }

        }
      }
    }

    return maxTime;
  }

}
