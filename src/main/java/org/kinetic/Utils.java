package org.kinetic;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.kinetic.heap.KineticElement;

@UtilityClass
public class Utils {


  public static double maxTimeForPermutations(List<KineticElement> elements) {

    double maxTime = -1;
    for (int i = 0; i < elements.size(); i++) {
      for (int j = 0; j < elements.size(); j++) {
        if (i != j) {
          double intersection = elements.get(i).getIntersectionTime(elements.get(j));
          if (intersection >= 0) {
            maxTime = Math.max(maxTime, intersection);
          }

        }
      }
    }

    return maxTime;
  }

}
