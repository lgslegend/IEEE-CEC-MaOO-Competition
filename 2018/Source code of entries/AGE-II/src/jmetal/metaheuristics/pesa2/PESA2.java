/**
 * PESA2.java
 * @author Juan J. Durillo
 * @version 1.0
 * 
 */
package jmetal.metaheuristics.pesa2;

import jmetal.base.*;
import jmetal.util.archive.AdaptiveGridArchive;
import jmetal.base.operator.selection.PESA2Selection;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.InfoPrinter;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class implements the PESA2 algorithm. 
 */
public class PESA2 extends Algorithm{
  
  /**
   * Stores the problem to solve
   */
  private Problem problem_;
  
  /**
  * Constructor
  * Creates a new instance of PESA2
  */
  public PESA2(Problem problem) {
    problem_ = problem;
  } // PESA2
    
  /**   
  * Runs of the PESA2 algorithm.
  * @return a <code>SolutionSet</code> that is a set of non dominated solutions
  * as a result of the algorithm execution  
   * @throws JMException 
  */  
  public SolutionSet execute() throws JMException, ClassNotFoundException {        
    int archiveSize, bisections, maxEvaluations, evaluations, populationSize;        
    AdaptiveGridArchive archive;
    SolutionSet solutionSet;
    Operator crossover,mutation, selection;



    QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators"); // this line had to be added (mw)
        boolean doMutation = ((Boolean)getInputParameter("doMutation")).booleanValue();
        boolean doCrossover = ((Boolean)getInputParameter("doCrossover")).booleanValue();
        int infoPrinterHowOften;
        if (getInputParameter("infoPrinterHowOften")==null) infoPrinterHowOften=1000;
            else infoPrinterHowOften = ((Integer)getInputParameter("infoPrinterHowOften")).intValue();
        String infoPrinterSubDir = (String)getInputParameter("infoPrinterSubDir");
        double epsilonGridWidth = ((Double) getInputParameter("epsilonGridWidth")).doubleValue();
        boolean doOnMPICluster;
        if (getInputParameter("doOnMPICluster")==null) doOnMPICluster = false;
            else doOnMPICluster = ((Boolean) getInputParameter("doOnMPICluster")).booleanValue();



        
    // Read parameters
    populationSize = ((Integer)(inputParameters_.get("populationSize"))).intValue();
    archiveSize    = ((Integer)(inputParameters_.get("archiveSize"))).intValue()   ;
    bisections     = ((Integer)(inputParameters_.get("bisections"))).intValue()    ;
    maxEvaluations = ((Integer)(inputParameters_.get("maxEvaluations"))).intValue();
    
    // Get the operators
    crossover = operators_.get("crossover");
    mutation  = operators_.get("mutation");
            
    // Initialize the variables
    evaluations = 0;    
    archive = new AdaptiveGridArchive(archiveSize,bisections,
                                        problem_.getNumberOfObjectives());
    solutionSet  = new SolutionSet(populationSize);
    selection    = new PESA2Selection();

    //-> Create the initial individual and evaluate it and his constraints
    for (int i = 0; i < populationSize; i++){
      Solution solution = new Solution(problem_);
      problem_.evaluate(solution);        
      problem_.evaluateConstraints(solution);
      evaluations++;    
      solutionSet.add(solution);      
    }
    //<-                
        
    // Incorporate non-dominated solution to the archive
    for (int i = 0; i < solutionSet.size();i++){
      archive.add(solutionSet.get(i)); // Only non dominated are accepted by 
                                      // the archive
    }
    
    // Clear the init solutionSet
    solutionSet.clear();
    
    //Iterations....
    Solution [] parents = new Solution[2];
    do {


        if (infoPrinter==null) infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);
            if (doOnMPICluster) {
//                System.out.println("1");
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, false);
//                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
//                System.out.println("2 "+evaluations);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, false);
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
            } else {
//                System.out.println("1");
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, false, false);
//                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
//                System.out.println("2 "+evaluations);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, false, false);
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
            }
        if (evaluations>=maxEvaluations) {
            if (doOnMPICluster) {
            //                System.out.println("1");
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
//                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
//                System.out.println("2 "+evaluations);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
            }
            break;
        }



      //-> Create the offSpring solutionSet                    
      while (solutionSet.size() < populationSize){                        
        parents[0] = (Solution) selection.execute(archive);
        parents[1] = (Solution) selection.execute(archive);
        
        Solution [] offSpring = (Solution []) crossover.execute(parents);        
        mutation.execute(offSpring[0]);
        problem_.evaluate(offSpring[0]);
        problem_.evaluateConstraints(offSpring[0]);
        evaluations++;
        solutionSet.add(offSpring[0]);                
      }
            
      for (int i = 0; i < solutionSet.size(); i++)
        archive.add(solutionSet.get(i));
      
      // Clear the solutionSet
      solutionSet.clear();



       if (doOnMPICluster && evaluations > populationSize) {
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
            } else if (evaluations % infoPrinterHowOften == 0 && evaluations > populationSize) {
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
            }


    }while (evaluations <= maxEvaluations);



    //Return the  solutionSet of non-dominated individual

    if (doOnMPICluster) {
            //                System.out.println("1");
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
//                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
//                System.out.println("2 "+evaluations);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, archive, archive, indicators, true, true);
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators);
            }

    return archive;                
  } // execute






      /** Minimal function to quickly execute the algorithm
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {

//            BFWN_main.main(new String[]{"DTLZ7","originalParetoFronts\\DTLZ7.3D.pf","10", "true", "true", "0.01"});    // MAXIMAL 12D!!!
            PESA2_main.main(new String[]{"DTLZ3_12D", "originalParetoFronts\\DTLZ2.12D.10000.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"WFG3", "originalParetoFronts\\WFG3.2D.pf", "10", "true", "true", "0.01"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ7", "nopf", "10", "true", "true", "0.1", "true"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"Water", "originalParetoFronts\\Water.pf", "10", "true", "true", "0.001", "true"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"ZDT3", "originalParetoFronts\\ZDT3.pf", "10", "true", "true", "0.001"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf","50"});
//            BFWN_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf"});
            }

            else if (args.length==7) {
                // in case it is called via the commandline with parameters...
                PESA2_main.main(new String[]{
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
//            Logger.getLogger(BFNW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


} // PESA2
