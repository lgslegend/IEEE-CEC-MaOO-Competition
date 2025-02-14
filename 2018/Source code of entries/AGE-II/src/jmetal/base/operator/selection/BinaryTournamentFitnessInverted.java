/**
 * BinaryTournament.java
 * @author Markus Wagner
 * @version 1.0
 */
package jmetal.base.operator.selection;


import java.util.Comparator;
import jmetal.base.Solution;
import jmetal.base.Operator;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.*;
import jmetal.util.PseudoRandom;

/**
 * This class implements an opertor for binary selections
 */
public class BinaryTournamentFitnessInverted extends Selection {

  /**
   * Stores the <code>Comparator</code> used to compare two
   * solutions
   */
  private Comparator comparator_;

  /**
   * Constructor
   * Creates a new Binary tournament operator using a BinaryTournamentComparator
   */
  public BinaryTournamentFitnessInverted(){
    comparator_ = new FitnessComparatorInverted();
  } // BinaryTournament

  
  /**
  * Constructor
  * Creates a new Binary tournament with a specific <code>Comparator</code>
  * @param comparator The comparator
  */
  public BinaryTournamentFitnessInverted(Comparator comparator) {
    comparator_ = comparator;
  } // Constructor

  
  /**
  * Performs the operation
  * @param object Object representing a SolutionSet
  * @return the selected solution
  */
  public Object execute(Object object){
//       System.out.println("test1");

    SolutionSet SolutionSet = (SolutionSet)object;
    Solution solution1,solution2;

//    System.out.println("test11");

//    solution1 = SolutionSet.get( (int)Math.round( Math.random()*(SolutionSet.size()-1d) ));
    solution1 = SolutionSet.get(PseudoRandom.randInt(0,SolutionSet.size()-1));
//    solution2 = SolutionSet.get( (int)Math.round( Math.random()*(SolutionSet.size()-1d) ));
    solution2 = SolutionSet.get(PseudoRandom.randInt(0,SolutionSet.size()-1));

//      System.out.println("test2");

    int flag = comparator_.compare(solution1,solution2);
    if (flag == -1)
      return solution1;                                                         // kleinere solution bevorzugt!!! (da INVERTED in comparator: nun groessere bevorzugt)
    else if (flag == 1)
      return solution2;
    else
      if (PseudoRandom.randDouble()<0.5)
        return solution1;
      else
        return solution2;                       
  } // execute
} // BinaryTournament
