/**
 * NSGAII.java
 * @author Juan J. Durillo
 * @version 1.0  
 */
package jmetal.metaheuristics.nsgaII;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.*;
import jmetal.base.operator.comparator.DominanceComparator;
import jmetal.metaheuristics.bfnw.BFNW;
import jmetal.metaheuristics.bfnw.DeepCopy;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.*;

/**
 * This class implements the NSGA-II algorithm. 
 */
public class NSGAIIArchive extends Algorithm {

  /**
   * stores the problem  to solve
   */
  private Problem problem_;

  /**
   * Constructor
   * @param problem Problem to solve
   */
  public NSGAIIArchive(Problem problem) {
    this.problem_ = problem;
  } // NSGAII

  /**   
   * Runs the NSGA-II algorithm.
   * @return a <code>SolutionSet</code> that is a set of non dominated solutions
   * as a result of the algorithm execution
   * @throws JMException 
   */
  public SolutionSet execute() throws JMException, ClassNotFoundException {
      
      boolean debug = false;
      
      
    int populationSize;
    int maxEvaluations;
    int evaluations;

    QualityIndicator indicators; // QualityIndicator object
    int requiredEvaluations; // Use in the example of use of the
    // indicators object (see below)

    SolutionSet solutionSet;
    SolutionSet offSpringSolutionSet;
    SolutionSet union = null;

    Operator mutationOperator;
    Operator crossoverOperator;
    Operator selectionOperator;

    Distance distance = new Distance();

    //Read the parameters
    populationSize = ((Integer) getInputParameter("populationSize")).intValue();
    maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
    indicators = (QualityIndicator) getInputParameter("indicators");

    boolean doCrossover = ((Boolean)getInputParameter("doCrossover")).booleanValue();
    boolean doMutation = ((Boolean)getInputParameter("doMutation")).booleanValue();
    int infoPrinterHowOften;
    if (getInputParameter("infoPrinterHowOften")==null) infoPrinterHowOften=1000;
      else infoPrinterHowOften = ((Integer)getInputParameter("infoPrinterHowOften")).intValue();
    boolean doOnMPICluster;
    if (getInputParameter("doOnMPICluster")==null) doOnMPICluster = false;
        else doOnMPICluster = ((Boolean) getInputParameter("doOnMPICluster")).booleanValue();
    String infoPrinterSubDir = (String)getInputParameter("infoPrinterSubDir");

    if (getInputParameter("crowdingDistanceSwitch")==null)  {
        System.out.println("crowdingDistanceSwitch default value "+crowdingDistanceSwitch);
        // take value from defined at the end of this file;
    } else {
        crowdingDistanceSwitch = ((Integer)getInputParameter("crowdingDistanceSwitch")).intValue();
        System.out.println("crowdingDistanceSwitch new value "+crowdingDistanceSwitch);
    }
            

    //Initialize the variables
    solutionSet = new SolutionSet(populationSize);
    evaluations = 0;

    requiredEvaluations = 0;

    //Read the operators
    mutationOperator = operators_.get("mutation");
    crossoverOperator = operators_.get("crossover");
    selectionOperator = operators_.get("selection");

    // Create the initial solutionSet
    Solution newSolution;
    for (int i = 0; i < populationSize; i++) {
      newSolution = new Solution(problem_);
      problem_.evaluate(newSolution);
      problem_.evaluateConstraints(newSolution);
      evaluations++;
      solutionSet.add(newSolution);
    } //for       

    
    //added START
    SolutionSet archive = (SolutionSet)DeepCopy.copy(solutionSet);
    SolutionSet offSpringSolutionSetForArchive = new SolutionSet(populationSize);
    DominanceComparator cNormal = new DominanceComparator();
    //added END

    // Generations ...
    while (evaluations <= maxEvaluations) {


//        if (infoPrinter==null) 
//                if (doOnMPICluster) {
//                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);
//                } else {
//                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir, infoPrinterHowOften, infoPrinterHowOften);
//                }
//        if (doOnMPICluster) {
//            if (union == null) {
//                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, false);
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, false);
//            } else {
//                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, union, indicators, true, false);
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, union, indicators, true, false);
//            }
//        } else {
//            if (union == null) {
//                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, false, false);
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, false, false);
//            } else {
//                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, union, indicators, false, false);
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, union, indicators, false, false);
//            }
//        }
//        if (evaluations>=maxEvaluations) {
//            if (doOnMPICluster) {
//                if (union == null) {
//                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, true);
//                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, true);
//                } else {
//                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, union, indicators, true, true);
//                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, union, indicators, true, true);
//                }
//            }
//            break;
//        }
        if (infoPrinter==null) 
                if (doOnMPICluster) {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir); 
                } else {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir, infoPrinterHowOften, infoPrinterHowOften);
                }
            
//            if (infoPrinter==null) infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);

            if (doOnMPICluster) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
            }
            if (evaluations>=maxEvaluations) {
                if (doOnMPICluster) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true); //correct line
//                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
                }
                break;
            }

        
        boolean innerDebug = !true && debug;
        

      // Create the offSpring solutionSet      
      offSpringSolutionSet = new SolutionSet(populationSize);
      
      
      
      //added STARTED
      offSpringSolutionSetForArchive = new SolutionSet(populationSize);
      //added END
      
      
      
      Solution[] parents = new Solution[2];
      for (int i = 0; i < (populationSize ); i++) {
          
if (innerDebug) System.out.println("start loop at evaluation="+evaluations);
if (innerDebug) System.out.println("solutionSet="+solutionSet.size()+" offSpring="+offSpringSolutionSet.size()+" archive="+archive.size());
          
          
//      for (int i = 0; i < (populationSize / 2); i++) {
        if (evaluations < maxEvaluations) {
          //obtain parents
          parents[0] = (Solution) selectionOperator.execute(solutionSet);
          parents[1] = (Solution) selectionOperator.execute(solutionSet);
          Solution[] offSpring;
          if (doCrossover) offSpring = (Solution[]) crossoverOperator.execute(parents);
            else offSpring = parents;
          if (doMutation) mutationOperator.execute(offSpring[0]);
//          if (doMutation) mutationOperator.execute(offSpring[1]);
          problem_.evaluate(offSpring[0]);
          problem_.evaluateConstraints(offSpring[0]);
//          problem_.evaluate(offSpring[1]);
//          problem_.evaluateConstraints(offSpring[1]);
          //offSpringSolutionSet.add(offSpring[0]); // comment this line so that "archive" has an effect
//          offSpringSolutionSet.add(offSpring[1]);
          evaluations += 1;
//          evaluations += 2;
          
          
          
          
if (innerDebug) System.out.println("generated");

          
          
          
          
          
          
          
          
          
          
          boolean forceInsertion = !false;
           /* START check if new offSpring is not (epsilon) dominated by a population point */
                    boolean newPointIsDominatedByOldPopulation = false;
                    
                    
if (innerDebug) System.out.println("check: dom by old pop");
                    
                    for (int ii = 0; ii<solutionSet.size(); ii++) {
                        /*
                         * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
                         *          non-dominated, or solution1 is dominated by solution2, respectively.
                         */
                        int result = cNormal.compare(solutionSet.get(ii), offSpring[0]);
                        // break if once dominated
                        if (result==-1) {
                            newPointIsDominatedByOldPopulation = true;
                            break;
                        }

                        /*
                         * the following 4 lines have the potential to cause a population to contain fewer than mu individuals
                         */
                        // remove populationpoint if new point dominates that one
//                        if (result==1) {
//                            solutionSet.remove(i);
//                            if (localDebug) System.out.println("r"+solutionSet.size());
//                            i--; // reduce i as we have just modified the list
//                        }
                    }

                    if (forceInsertion) {
                        
                    } else {
                        if (newPointIsDominatedByOldPopulation) {
                            // just forget this point
                            continue;
                        } else {

                        }
                    }
                    
                    /* END check if new offSpring is not epsilon dominated by a population point */


                    /* START check if new offSpring is not (epsilon) dominated by an archive point */
                    boolean newPointIsDominatedByOldArchive = false;
                    
                    
if (innerDebug) System.out.println("check: dom by archive");
                    
                    
                    for (int ii = 0; ii<archive.size(); ii++) {
                        /*
                         * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
                         *          non-dominated, or solution1 is dominated by solution2, respectively.
                         */
                        int result = cNormal.compare(archive.get(ii), offSpring[0]);
                        if (result==-1) {
                            // break if an archive point dominates the new point
                            newPointIsDominatedByOldArchive = true;
                            break;
                        }
                        if (result==1) {
                            // remove archive point if new point dominates that one
                            archive.remove(ii);
                            ii--;
                        }
                    }
                    /* END check if new offSpring is not epsilon dominated by an archive point */

                    // define behavior: add offspring to archive
                    
                    
if (innerDebug) System.out.println("if all ok: add to ");
                    
                    if (forceInsertion) {
                        offSpringSolutionSet.add(offSpring[0]);
                        offSpringSolutionSetForArchive.add(offSpring[0]);
                    } else {
                        if (newPointIsDominatedByOldArchive) {
                            // forget this point
                            continue;
                        } else {

if (innerDebug) System.out.println("-offSpringSolutionSet");
                            
                            offSpringSolutionSet.add(offSpring[0]);
                            
if (innerDebug) System.out.println("-offSpringSolutionSetForArchive");
                            
                            offSpringSolutionSetForArchive.add(offSpring[0]);
                        }
                    }
          
          
          
          
          
if (innerDebug) System.out.println("end loop");
          
          
          
        } // if                            
      } // for

      
      boolean indDebug = true && debug;
      
      if (indDebug) System.out.println("offSpringSolutionSet eps="+indicators.getEpsilon(offSpringSolutionSet) + " size="+offSpringSolutionSet.size());
      if (indDebug) System.out.println("offSpringSolutionSetForArchive eps="+indicators.getEpsilon(offSpringSolutionSetForArchive) + " size="+offSpringSolutionSetForArchive.size());//may be less useful

      
      if (indDebug) System.out.println("solutionSet old eps="+indicators.getEpsilon(solutionSet) + " size="+solutionSet.size());
      if (indDebug) if (union!=null) {
          System.out.println("union old eps="+indicators.getEpsilon(union) + " size="+union.size());
          Ranking rTemp = new Ranking(union);
          for (int ii = 0; ii< rTemp.getNumberOfSubfronts(); ii++) {
              System.out.println("subfront="+ii+" size="+rTemp.getSubfront(ii).size());
          }
      }
      // Create the solutionSet union of solutionSet and offSpring
      union = ((SolutionSet) solutionSet).union(offSpringSolutionSet);
      if (indDebug) System.out.println("union new eps="+indicators.getEpsilon(union) + " size="+union.size());
      
      
      if (indDebug) System.out.println("archive old eps="+indicators.getEpsilon(archive) + " size="+archive.size());
      //added START
      archive = archive.union(offSpringSolutionSetForArchive);
      //added END
      if (indDebug ||true && evaluations%1110000==0) System.out.println("archive new eps="+indicators.getEpsilon(archive) + " size="+archive.size());
      
      
      

      // Ranking the union
      Ranking ranking = new Ranking(union);

      int remain = populationSize;
      int index = 0;
      SolutionSet front = null;
      solutionSet.clear();

      // Obtain the next front
      front = ranking.getSubfront(index);

      while ((remain > 0) && (remain >= front.size())) {
        //Assign crowding distance to individuals
          distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives(), crowdingDistanceSwitch, archive);
//        distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
        //Add the individuals of this front
        for (int k = 0; k < front.size(); k++) {
          solutionSet.add(front.get(k));
        } // for

        //Decrement remain
        remain = remain - front.size();

        //Obtain the next front
        index++;
        if (remain > 0) {
          front = ranking.getSubfront(index);
        } // if        
      } // while

      // Remain is less than front(index).size, insert only the best one
      if (remain > 0) {  // front contains individuals to insert                        
          distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives(), crowdingDistanceSwitch, archive);
//        distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
        front.sort(new jmetal.base.operator.comparator.CrowdingComparator());
        for (int k = 0; k < remain; k++) {
//            System.out.println(front.get(k).getCrowdingDistance());
          solutionSet.add(front.get(k));
        } // for
        
        
        // test from markus: alternative selection (recalculation of crowding distances when one point removed
//        while(remain>0) {
//            distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
//            front.sort(new jmetal.base.operator.comparator.CrowdingComparator());
//          solutionSet.add(front.get(0));
//          front.remove(0);
//          remain--;
//        } // for

        remain = 0;
      } // if                               

      // This piece of code shows how to use the indicator object into the code
      // of NSGA-II. In particular, it finds the number of evaluations required
      // by the algorithm to obtain a Pareto front with a hypervolume higher
      // than the hypervolume of the true Pareto front.
      if ((indicators != null) &&
        (requiredEvaluations == 0)) {
        double HV = indicators.getHypervolume(solutionSet);
        if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
          requiredEvaluations = evaluations;
        } // if
      } // if



//     if (!doOnMPICluster)
//      if (evaluations % infoPrinterHowOften == 0) {
//        infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true);
//      }

      
      
      if (indDebug) System.out.println("solutionSet new eps="+indicators.getEpsilon(solutionSet) + " size="+solutionSet.size());
      
      

    } // while

    // Return as output parameter the required evaluations
    setOutputParameter("evaluations", requiredEvaluations);


//    if (doOnMPICluster) {
//        if (union == null) {
//            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, true);
//            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, true);
//        } else {
//            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, union, indicators, true, true);
//            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, union, indicators, true, true);
//        }
//    }
    if (doOnMPICluster) {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true); //correct line
//            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
    }
    

    // Return the first non-dominated front
    Ranking ranking = new Ranking(solutionSet);
    return ranking.getSubfront(0);
  } // execute

  
  
  
  int crowdingDistanceSwitch = 1;

  public static void main(String[] args) {
        try {
            if (args.length==0) {
//            NSGAII_main.main(new String[]{"DTLZ1", "originalParetoFronts\\DTLZ1.2D.pf"});
//            NSGAII_main.main(new String[]{"Water", "originalParetoFronts\\DTLZ1.2D.pf"});
//            NSGAII_main.main(new String[]{"ZDT4","originalParetoFronts\\ZDT4.pf","10","true","true"});
//            NSGAII_main.main(new String[]{"DTLZ7", "nopf", "10", "true", "true","","true"});
//            NSGAII_main.main(new String[]{"Water", "originalParetoFronts\\Water.pf", "10", "true", "true","","true"});
//              BFWN_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection"});
            NSGAIIArchive_main.main(new String[]{"DTLZ2_4D", "originalParetoFronts\\DTLZ2.4D.10000.pf", "100", "true", "true", "0.01", "true","200000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            NSGAII_main.main(new String[]{"DTLZ1", "originalParetoFronts\\DTLZ1.3D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection",""});
//            NSGAII_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection",""});
//            NSGAII_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf"});
            } else if (args.length==7) {
                // in case it is called via the commandline with parameters...
                NSGAIIArchive_main.main(new String[]{
                    args[0], // problem
                    "originalParetoFronts/"+args[1], // pf-file, without the directory
                    args[2], // mu
                    "true",  // doXO
                    "true",  // doMUT
                    args[5], // epsilonGridWidth
                    "true",  // do outputs for runs on MPI cluster
                    args[3], // max evaluations
                    args[4], // selection strategy
                    args[6]  // subDirectory name for infoprinter
                });
            } else System.out.println("unsuitable number of parameters. EXIT.");
        } catch (Exception ex) {
            Logger.getLogger(NSGAIIArchive.class.getName()).log(Level.SEVERE, null, ex);
        }
  }

} // NSGA-II
