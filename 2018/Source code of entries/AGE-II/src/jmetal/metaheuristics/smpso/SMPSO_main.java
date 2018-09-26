/**
 * SMPSO_main.java
 * 
 * @author Juan J. Durillo
 * @version 1.0
 * This class executes the SMPSO algorithm described in:
 * A.J. Nebro, J.J. Durillo, J. Garcia-Nieto, C.A. Coello Coello, F. Luna and E. Alba
 * "SMPSO: A New PSO-based Metaheuristic for Multi-objective Optimization". 
 * IEEE Symposium on Computational Intelligence in Multicriteria Decision-Making 
 * (MCDM 2009), pp: 66-73. March 2009
 */

package jmetal.metaheuristics.smpso;

import java.io.IOException;
import jmetal.base.*;
import jmetal.base.operator.mutation.Mutation;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.problems.*;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ09.* ;
import jmetal.util.Configuration;
import jmetal.util.JMException ;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.selection.RandomSelection;
import jmetal.base.operator.selection.Selection;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.qualityIndicator.QualityIndicator;

public class SMPSO_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object

  /**
   * @param args Command line arguments. The first (optional) argument specifies 
   *             the problem to solve.
   * @throws JMException 
   * @throws IOException 
   * @throws SecurityException 
   * Usage: three options
   *      - jmetal.metaheuristics.mocell.MOCell_main
   *      - jmetal.metaheuristics.mocell.MOCell_main problemName
   *      - jmetal.metaheuristics.mocell.MOCell_main problemName ParetoFrontFile
   */
  public static void main(String [] args) throws JMException, IOException, ClassNotFoundException {
    Problem   problem   ;  // The problem to solve
    Algorithm algorithm ;  // The algorithm to use
    Mutation  mutation  ;  // "Turbulence" operator
    
    QualityIndicator indicators ; // Object to get quality indicators
        
    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
//    fileHandler_ = new FileHandler("SMPSO_main.log"); 
//    logger_.addHandler(fileHandler_) ;
    
    indicators = null ;
    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
    } // if
    else if (args.length == 2|| args.length >= 5) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
      indicators = new QualityIndicator(problem, args[1]) ;
    } // if
    else { // Default problem
      problem = new Kursawe("Real", 3); 
      //problem = new Water("Real");
      //problem = new ZDT1("ArrayReal", 1000);
      //problem = new ZDT4("BinaryReal");
      //problem = new WFG1("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    } // else
    
    algorithm = new SMPSO(problem) ;
    
    // Algorithm parameters
    algorithm.setInputParameter("swarmSize",100);
    algorithm.setInputParameter("archiveSize",100);
    algorithm.setInputParameter("maxIterations",250);
    
    
    
    if (args.length >= 7) {
        algorithm.setInputParameter("populationSize",Integer.parseInt(args[2]));
        algorithm.setInputParameter("swarmSize",Integer.parseInt(args[2]));
        algorithm.setInputParameter("archiveSize",Integer.parseInt(args[2]));
        algorithm.setInputParameter("doCrossover", Boolean.parseBoolean(args[3]));
        algorithm.setInputParameter("doMutation", Boolean.parseBoolean(args[4]));
        algorithm.setInputParameter("epsilonGridWidth", Double.parseDouble(args[5]));
        algorithm.setInputParameter("doOnMPICluster", Boolean.parseBoolean(args[6]));
        algorithm.setInputParameter("maxEvaluations",Integer.parseInt(args[7]));

    } else {
        algorithm.setInputParameter("populationSize",100);
        algorithm.setInputParameter("swarmSize",100);
        algorithm.setInputParameter("archiveSize",100);
        algorithm.setInputParameter("doMutation", true);
        algorithm.setInputParameter("doCrossover", true);
    }
    algorithm.setInputParameter("infoPrinterHowOften", 100);
    if (args.length >= 7) {
        algorithm.setInputParameter("infoPrinterSubDir", args[9]);
    } else {
        algorithm.setInputParameter("infoPrinterSubDir", ".");
    }

    
    
    
    mutation = MutationFactory.getMutationOperator("PolynomialMutation");                    
    mutation.setParameter("probability",1.0/problem.getNumberOfVariables());
    mutation.setParameter("distributionIndex",20.0);

    if (args.length >= 7) {
    algorithm.addOperator("selection", (Selection) SelectionFactory.getSelectionOperator(args[8]));
    } else {
    algorithm.addOperator("selection", (Selection) SelectionFactory.getSelectionOperator("BinaryTournament"));
    }
    
    algorithm.setInputParameter("indicators", indicators) ;
    
    Operator  crossover ;
    Operator  selection ;
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
    selection = new RandomSelection();
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);

    // Execute the Algorithm 
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;
    
    // Result messages 
    logger_.info("Total execution time: "+estimatedTime + "ms");
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");      
    
    if (indicators != null) {
      logger_.info("Quality indicators") ;
      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
      logger_.info("GD         : " + indicators.getGD(population)) ;
      logger_.info("IGD        : " + indicators.getIGD(population)) ;
      logger_.info("Spread     : " + indicators.getSpread(population)) ;
      logger_.info("Epsilon    : " + indicators.getEpsilon(population)) ;
    } // if                   
  } //main
} // SMPSO_main
