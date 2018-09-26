/**
 * BinaryTournament2.java
 * @author Juan J. Durillo
 * @version 1.0
 */

package jmetal.base.operator.selection;

import java.util.Arrays;
import java.util.Comparator;
import jmetal.base.Solution;
import jmetal.base.Operator;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.DominanceComparator;
import jmetal.util.PseudoRandom;

/**
 * This class implements an opertor for binary selections using the same code
 * in Deb's NSGA-II implementation
 */
public class BinaryTournament2 extends Selection {
  
  /**
   * dominance_ store the <code>Comparator</code> for check dominance_
   */
  private Comparator dominance_;
  
  /**
   * a_ stores a permutation of the solutions in the solutionSet used
   */
  private int a_[];
  
  /**
   *  index_ stores the actual index for selection
   */
  private int index_ = 0;
    
  /**
   * Constructor
   * Creates a new instance of the Binary tournament operator (Deb's
   * NSGA-II implementation version)
   */
  public BinaryTournament2()
  {
    dominance_ = new DominanceComparator();              
  } // BinaryTournament2
    
  /**
  * Performs the operation
  * @param object Object representing a SolutionSet
  * @return the selected solution
  */
  public Object execute(Object object)    
  {
      boolean debugPrint = !true;
      if (debugPrint) System.out.println("execute START");
    SolutionSet population = (SolutionSet)object;
//    if (index_ == 0) //Create the permutation
//    {
      a_= (new jmetal.util.PermutationUtility()).intPermutation(population.size());
      if (debugPrint) System.out.println("permutation done");
//    }

      if (debugPrint) System.out.println(Arrays.toString(a_) + " " +index_);
        
    Solution solution1,solution2;
    if (debugPrint) System.out.println("execute get 1st");
    solution1 = population.get(a_[index_%population.size()]);
    if (debugPrint) System.out.println("execute get 2nd");
    solution2 = population.get(a_[(index_+1)%population.size()]%population.size());
    if (debugPrint) System.out.println("execute get solutions done");
        
    index_ = (index_ + 2) % population.size();

    if (debugPrint) System.out.println("execute dominance_.compare do");
    int flag = dominance_.compare(solution1,solution2);
    if (debugPrint) System.out.println("execute dominance_.compare done");
    if (flag == -1) {
        if (debugPrint) System.out.println("execute -1");
      return solution1; }
    else { if (flag == 1) {
        if (debugPrint) System.out.println("execute 1");
      return solution2; }
    else if (solution1.getCrowdingDistance() > solution2.getCrowdingDistance()) {
      if (debugPrint) System.out.println("execute solution1");
        return solution1; }
    else if (solution2.getCrowdingDistance() > solution1.getCrowdingDistance()) {
      if (debugPrint) System.out.println("execute solution2");
        return solution2; }
    else {
      if (PseudoRandom.randDouble()<0.5) {
        if (debugPrint) System.out.println("execute rand solution1");
          return solution1; }
      else {
        if (debugPrint) System.out.println("execute rand solution2");
          return solution2; }
      }
    }


  } // execute
} // BinaryTournament2
