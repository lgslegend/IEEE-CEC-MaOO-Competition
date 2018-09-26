/**
 * Paes.java
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.paes;

import jmetal.base.*;
import jmetal.util.archive.AdaptiveGridArchive;
import jmetal.base.operator.comparator.*;
import jmetal.util.JMException;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.InfoPrinter;

/**
 * This class implements the NSGA-II algorithm. 
 */
public class PAES extends Algorithm {        
    
  /**
   * Stores the problem to solve
   */
  private Problem problem_;  
   
  /** 
  * Create a new PAES instance for resolve a problem
  * @param problem Problem to solve
  */                 
  public PAES(Problem problem) {                
    problem_ = problem;        
  } // Paes
    
  /**
   * Tests two solutions to determine which one becomes be the guide of PAES
   * algorithm
   * @param solution The actual guide of PAES
   * @param mutatedSolution A candidate guide
   */
  public Solution test(Solution solution, 
                       Solution mutatedSolution, 
                       AdaptiveGridArchive archive){  
    
    int originalLocation = archive.getGrid().location(solution);
    int mutatedLocation  = archive.getGrid().location(mutatedSolution); 

    if (originalLocation == -1) {
      return new Solution(mutatedSolution);
    }
    
    if (mutatedLocation == -1) {
      return new Solution(solution);
    }
        
    if (archive.getGrid().getLocationDensity(mutatedLocation) < 
        archive.getGrid().getLocationDensity(originalLocation)) {
      return new Solution(mutatedSolution);
    }
    
    return new Solution(solution);          
  } // test
    
  /**   
  * Runs of the Paes algorithm.
  * @return a <code>SolutionSet</code> that is a set of non dominated solutions
  * as a result of the algorithm execution  
   * @throws JMException 
  */    
  public SolutionSet execute() throws JMException, ClassNotFoundException {     
    int bisections, archiveSize, maxEvaluations, evaluations;
    AdaptiveGridArchive archive;
    Operator mutationOperator;
    Comparator dominance;
    
    //Read the params
    bisections     = ((Integer)this.getInputParameter("biSections")).intValue();
    archiveSize    = ((Integer)this.getInputParameter("archiveSize")).intValue();
    maxEvaluations = ((Integer)this.getInputParameter("maxEvaluations")).intValue();

    
    
    QualityIndicator indicators;
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
    
    
    
    
    
    
    
    
    
    //Read the operators        
    mutationOperator = this.operators_.get("mutation");        

    //Initialize the variables                
    evaluations = 0;
    archive     = new AdaptiveGridArchive(archiveSize,bisections,problem_.getNumberOfObjectives());        
    dominance = new DominanceComparator();           
            
    //-> Create the initial solution and evaluate it and his constraints
    Solution solution = new Solution(problem_);
    problem_.evaluate(solution);        
    problem_.evaluateConstraints(solution);
    evaluations++;
        
    // Add it to the archive
    archive.add(new Solution(solution));            
   
    //Iterations....
    do {
        
        
        
        
        if (infoPrinter==null) 
                if (doOnMPICluster) {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);
                } else {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir, infoPrinterHowOften, infoPrinterHowOften);
                }
        if (doOnMPICluster) {
            if (archive == null) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, false);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, false);
            }
        } else {
            if (archive == null) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, false, false);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, false, false);
            }
        }
        if (evaluations>=maxEvaluations) {
            if (doOnMPICluster) {
                if (archive == null) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
                } else {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
                }
            }
            break;
        }
        
        
        
        
        
      // Create the mutate one
      Solution mutatedIndividual = new Solution(solution);  
      mutationOperator.execute(mutatedIndividual);
            
      problem_.evaluate(mutatedIndividual);                     
      problem_.evaluateConstraints(mutatedIndividual);
      evaluations++;
      //<-
            
      // Check dominance
      int flag = dominance.compare(solution,mutatedIndividual);            
            
      if (flag == 1) { //If mutate solution dominate                  
        solution = new Solution(mutatedIndividual);                
        archive.add(mutatedIndividual);                
      } else if (flag == 0) { //If none dominate the other                               
        if (archive.add(mutatedIndividual)) {                    
          solution = test(solution,mutatedIndividual,archive);
        }                                
      }                                              
    } while (evaluations < maxEvaluations);                                    
        
    
    
    
    
    if (doOnMPICluster) {
        if (archive == null) {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
        } else {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
        }
    }
    
    
    
    
    
    //Return the  ^ of non-dominated solution
    return archive;                
  }  // execute  
  
  
  
  
  
  
  
  
  
  
    public static void main(String[] args) {
        try {
            if (args.length==0) {
//            NSGAII_main.main(new String[]{"DTLZ1", "originalParetoFronts\\DTLZ1.2D.pf"});
//            NSGAII_main.main(new String[]{"Water", "originalParetoFronts\\DTLZ1.2D.pf"});
//            NSGAII_main.main(new String[]{"ZDT4","originalParetoFronts\\ZDT4.pf","10","true","true"});
//            NSGAII_main.main(new String[]{"DTLZ7", "nopf", "10", "true", "true","","true"});
//            NSGAII_main.main(new String[]{"Water", "originalParetoFronts\\Water.pf", "10", "true", "true","","true"});
//              BFWN_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection"});
            PAES_main.main(new String[]{"DTLZ1_16D", "originalParetoFronts\\DTLZ1.16D.1000.pf", "100", "true", "true", "0.01", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            NSGAII_main.main(new String[]{"DTLZ1", "originalParetoFronts\\DTLZ1.3D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection",""});
//            NSGAII_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection",""});
//            NSGAII_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf"});
            } else if (args.length==7) {
                // in case it is called via the commandline with parameters...
                PAES_main.main(new String[]{
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
            Logger.getLogger(PAES.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
} // PAES
