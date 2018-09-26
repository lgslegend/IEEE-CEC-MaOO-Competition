/**
 * QualityIndicator.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 * 
 * This class provides methods for calculating the values of quality indicators
 * from a solution set. After creating an instance of this class, which requires
 * the file containing the true Pareto of the problem as a parementer, methods
 * such as getHypervolume(), getSpread(), etc. are available
 */

package jmetal.qualityIndicator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.Problem;
import jmetal.base.SolutionSet;

/**
 * QualityIndicator class
 */
public class QualityIndicator {
  SolutionSet trueParetoFront_ ;
  double      trueParetoFrontHypervolume_ ;
  Problem     problem_ ; 
  jmetal.qualityIndicator.util.MetricsUtil utilities_  ;
  
  public String paretoFrontFile = "";
  
  /**
   * Constructor
   * @param paretoFrontFile
   */
  public QualityIndicator(Problem problem, String paretoFrontFile)  {
      boolean debugPrint = false;

      this.paretoFrontFile = paretoFrontFile;
      
    problem_ = problem ;
    utilities_ = new jmetal.qualityIndicator.util.MetricsUtil() ;
        try {
            if (debugPrint) System.out.println((new File(".")).getCanonicalPath());
        } catch (IOException ex) {
            Logger.getLogger(QualityIndicator.class.getName()).log(Level.SEVERE, null, ex);
        }

    if (paretoFrontFile.endsWith("nopf")) {

        // generate a dummy paretoFrontFile

        int problemObjectives = problem.getNumberOfObjectives();

        String newPFname = problem.getClass().getSimpleName() + "-"+ problemObjectives+".pf";
        File newPF = new File(newPFname);

//        if (!newPF.exists()) {
            BufferedWriter out;
                try {
                    out = new BufferedWriter(new FileWriter(newPF, false)); // use false to NOT APPEND
                    // create "front"
                    String print = "";
                    for (int i=0; i<problemObjectives; i++) print += "0 ";
                    print += "\n";
                    for (int i=0; i<problemObjectives; i++) print += "50 ";
//                    for (int i=0; i<problemObjectives; i++) print += "10000 ";

                    out.write(print);
                    out.close();

                } catch (IOException ex) {
                    Logger.getLogger(QualityIndicator.class.getName()).log(Level.SEVERE, null, ex);

                }
//        }
        trueParetoFront_ = utilities_.readNonDominatedSolutionSet(newPFname);
    } else { // business as usual
        trueParetoFront_ = utilities_.readNonDominatedSolutionSet(paretoFrontFile);
    }

if (debugPrint) System.out.println("QualityIndicator:beforeHV");
String problemSimpleName = problem_.getClass().getSimpleName();
if ( (problemSimpleName.startsWith("DTLZ") ||
        problemSimpleName.startsWith("MaF") )
        &&
        problemSimpleName.contains("_")) {
    if (debugPrint) System.out.println("QualityIndicator: skipping the computation of the 'real' Hypervolume (i.e. HYP(sampledFront)) because of enormous time consumption");
    trueParetoFrontHypervolume_ = 0d;
} else {
    long startTime, stopTime;
    startTime = System.currentTimeMillis();
    if (debugPrint) System.out.print("QualityIndicator: computation of the 'real' Hypervolume (i.e. HYP(sampledFront))...");
    trueParetoFrontHypervolume_ = new Hypervolume().hypervolume(
                 trueParetoFront_.writeObjectivesToMatrix(),
                 trueParetoFront_.writeObjectivesToMatrix(),
                 problem_.getNumberOfObjectives());
    stopTime = System.currentTimeMillis();
    if (debugPrint) System.out.println(" done in " + (stopTime-startTime)/1000d +"s");
}

if (debugPrint) System.out.println("QualityIndicator:afterHV");

  } // Constructor 
  
  /**
   * Returns the hypervolume of solution set
   * @param solutionSet
   * @return The value of the hypervolume indicator
   */
  public double getHypervolume(SolutionSet solutionSet) {
    return new Hypervolume().hypervolume(solutionSet.writeObjectivesToMatrix(),
                                         trueParetoFront_.writeObjectivesToMatrix(),
//                                         solutionSet.writeObjectivesToMatrix(),
                                         problem_.getNumberOfObjectives());
  } // getHypervolume
  
  public double getHypervolumeCEC2018(SolutionSet solutionSet) {
    return new Hypervolume().hypervolumeCEC2018(solutionSet.writeObjectivesToMatrix(),
                                         trueParetoFront_.writeObjectivesToMatrix(),
//                                         solutionSet.writeObjectivesToMatrix(),
                                         problem_.getNumberOfObjectives());
  } // getHypervolume

  
  public double getHypervolumeFPRAS(SolutionSet solutionSet, boolean doExactForFinalEvaluation, 
          double hypervolumePoint) {
    return new Hypervolume().hypervolumeFPRAS(solutionSet.writeObjectivesToMatrix(),
                                         trueParetoFront_.writeObjectivesToMatrix(),
//                                         solutionSet.writeObjectivesToMatrix(),
                                         problem_.getNumberOfObjectives(),
                                         doExactForFinalEvaluation, hypervolumePoint);
  } // getHypervolume

    
  /**
   * Returns the hypervolume of the true Pareto front
   * @return The hypervolume of the true Pareto front
   */
  public double getTrueParetoFrontHypervolume() {
    return trueParetoFrontHypervolume_ ;
  }
  
  /**
   * Returns the inverted generational distance of solution set
   * @param solutionSet
   * @return The value of the hypervolume indicator
   */
  public double getIGD(SolutionSet solutionSet) {
    return new InvertedGenerationalDistance().invertedGenerationalDistance(
                    solutionSet.writeObjectivesToMatrix(),
                    trueParetoFront_.writeObjectivesToMatrix(),
                    problem_.getNumberOfObjectives());
  } // getIGD
  
 /**
   * Returns the generational distance of solution set
   * @param solutionSet
   * @return The value of the hypervolume indicator
   */
  public double getGD(SolutionSet solutionSet) {
    return new GenerationalDistance().generationalDistance(
                    solutionSet.writeObjectivesToMatrix(),
                    trueParetoFront_.writeObjectivesToMatrix(),
                    problem_.getNumberOfObjectives());
  } // getGD
  
  /**
   * Returns the spread of solution set
   * @param solutionSet
   * @return The value of the hypervolume indicator
   */
  public double getSpread(SolutionSet solutionSet) {
    return new Spread().spread(solutionSet.writeObjectivesToMatrix(),
                               trueParetoFront_.writeObjectivesToMatrix(),
                               problem_.getNumberOfObjectives());
  } // getSpread
  
  
  public double getGeneralizedSpread(SolutionSet solutionSet) {
    return new GeneralizedSpread().generalizedSpread(solutionSet.writeObjectivesToMatrix(),
                               trueParetoFront_.writeObjectivesToMatrix(),
                               problem_.getNumberOfObjectives());
  } // getGeneralizedSpread
  
    /**
   * Returns the epsilon indicator of solution set
   * @param solutionSet
   * @return The value of the hypervolume indicator
   */
  public double getEpsilon(SolutionSet solutionSet) {
    return new Epsilon().epsilon(solutionSet.writeObjectivesToMatrix(),
                                 trueParetoFront_.writeObjectivesToMatrix(),
                                 problem_.getNumberOfObjectives());
  } // getEpsilon
} // QualityIndicator
