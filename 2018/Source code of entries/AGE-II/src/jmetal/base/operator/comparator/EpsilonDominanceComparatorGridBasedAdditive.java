/**
 * based on EpsilonDominanceComparator.java
 *
 * @author Markus Wagner
 * @version 1.0
 */
package jmetal.base.operator.comparator;

import jmetal.base.Solution;
import java.util.Comparator;

/**
 * This class implements a <code>Comparator</code> (a method for comparing
 * <code>Solution</code> objects) based on epsilon dominance.
 */
public class EpsilonDominanceComparatorGridBasedAdditive implements Comparator{
   
  /**
   * Stores the value of eta, needed for epsilon-dominance.
   */
  private double eta_;
  
  /** 
   * stores a comparator for check the OverallConstraintComparator
   */
  private static final Comparator overallConstraintViolationComparator_ =
                              new OverallConstraintViolationComparator();
  
  /**
   * Constructor.
  *  @param eta Value for epsilon-dominance.
  */
  public EpsilonDominanceComparatorGridBasedAdditive(double eta) {
    eta_ = eta;
  }
  
 /** 
  * Compares two solutions.
  * @param solution1 Object representing the first <code>Solution</code>.
  * @param solution2 Object representing the second <code>Solution</code>.
  * @return -1, or 0, or 1 if solution1 dominates solution2, both are 
  * non-dominated, or solution1 is dominated by solution2, respectively.
  */
  public int compare(Object object1, Object object2) {
    if (object1==null)
      return 1;
    else if (object2 == null)
      return -1;    
        
    int dominate1 ; // dominate1 indicates if some objective of solution1 
                    // dominates the same objective in solution2. dominate2
    int dominate2 ; // is the complementary of dominate1.

    dominate1 = 0 ; 
    dominate2 = 0 ;   
    
    Solution solution1 = (Solution)object1;
    Solution solution2 = (Solution)object2;
    
    int flag; 
    Comparator constraint = new jmetal.base.operator.comparator.OverallConstraintViolationComparator();
    flag = constraint.compare(solution1,solution2);
    
    if (flag != 0) {      
      return flag;
    }

    int flagSum = 0;

    double value1, value2;
    // Idem number of violated constraint. Apply a dominance Test
    for (int i = 0; i < ((Solution)solution1).numberOfObjectives(); i++) {
      value1 = solution1.getObjective(i);
//      value1 = Math.ceil(value1/eta_)*eta_;
      value2 = solution2.getObjective(i);
//      value2 = Math.ceil(value2/eta_)*eta_;

      //Objetive implements comparable!!!                                       // START modification by MW
//      if (value1/(1 + eta_) < value2) {
//      if ( (Math.ceil(value1/eta_)+eta_) < (Math.ceil(value2/eta_)+eta_) ) {
      boolean debugPrint = !true;
      if (debugPrint) System.out.print("value1:"+value1+" value2:"+value2 +
              " "+(Math.ceil(value1/eta_)*eta_) +" "+ (Math.ceil(value2/eta_)*eta_));
      if ( (Math.floor(value1/eta_)*eta_) < (Math.floor(value2/eta_)*eta_) ) {
          if (debugPrint) System.out.println(" -> -1");
        flag = -1;
      } else if ( (Math.floor(value1/eta_)*eta_) > (Math.floor(value2/eta_)*eta_) ) {
          if (debugPrint) System.out.println(" -> +1");
        flag = 1;
      } else {
          if (debugPrint) System.out.println(" ->  0");
        flag =  0;
      }                                                                         // END modification by MW
      
      if (flag == -1) {
        dominate1 = 1;
      }
      
      if (flag == 1) {
        dominate2 = 1;
      }

      flagSum = flagSum + Math.abs(flag);
    }

//    // p and q are very similar
//    if (flagSum == 0) {
//        // now go through both solutions and check whether one is better (smaller) in one of the positions
////        System.out.println("xxx0");
//        for (int i = 0; i < ((Solution)solution1).numberOfObjectives(); i++) {
//          value1 = solution1.getObjective(i);
//          value2 = solution2.getObjective(i);
//
//          if ( value1 < value2 ) {
//            return -1;
//          } else if (value1 > value2) {
//            return 1;
//          } else {
//    //          if (debugPrint) System.out.println(" ->  0");
//            continue;
//          }
//        }
//    }

    
//    else System.out.println("yyy0");
            
    if (dominate1 == dominate2) {
      return 0; // No one dominates the other
    }
    
    if (dominate1 == 1) {
      return -1; // solution1 dominates
    }
    
    return 1;    // solution2 dominates
  } // compare
} // EpsilonDominanceComparator
