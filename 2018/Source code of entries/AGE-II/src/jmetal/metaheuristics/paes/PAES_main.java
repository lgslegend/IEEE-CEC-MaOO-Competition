/**
 * PAES_main.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.paes;

import java.io.IOException;
import jmetal.base.*                      ;
import jmetal.base.operator.mutation.*    ; 
import jmetal.problems.*                  ;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ09.* ;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.selection.Selection;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.qualityIndicator.QualityIndicator;

public class PAES_main {
  public static Logger      logger_ ;      // Logger object
//  public static FileHandler fileHandler_ ; // FileHandler object

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
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  mutation  ;         // Mutation operator
    
    QualityIndicator indicators ; // Object to get quality indicators

    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
//    fileHandler_ = new FileHandler("PAES_main.log");
//    logger_.addHandler(fileHandler_) ;
    
    indicators = null ;
    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
    } // if
    else if (args.length == 2 || args.length >=5) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
      indicators = new QualityIndicator(problem, args[1]) ;
    } // if
    else { // Default problem
      problem = new Kursawe("ArrayReal", 3); 
      //problem = new Fonseca("Real"); 
      //problem = new Kursawe("BinaryReal",3);
      //problem = new Water("Real");
      //problem = new ZDT4("Real", 1000);
      //problem = new WFG1("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    } // else
    
    algorithm = new PAES(problem);
    
    
    
    
    
    if (args.length >= 7) {
        algorithm.setInputParameter("populationSize",Integer.parseInt(args[2]));
        algorithm.setInputParameter("doCrossover", Boolean.parseBoolean(args[3]));
        algorithm.setInputParameter("doMutation", Boolean.parseBoolean(args[4]));
        algorithm.setInputParameter("epsilonGridWidth", Double.parseDouble(args[5]));
        algorithm.setInputParameter("doOnMPICluster", Boolean.parseBoolean(args[6]));
        algorithm.setInputParameter("maxEvaluations",Integer.parseInt(args[7]));

    } else {
        algorithm.setInputParameter("populationSize",100);
        algorithm.setInputParameter("doMutation", true);
        algorithm.setInputParameter("doCrossover", true);
    }
    algorithm.setInputParameter("infoPrinterHowOften", 100);
    algorithm.setInputParameter("infoPrinterSubDir", args[9]);
    
    
    
    
    
    
    // Algorithm parameters
    algorithm.setInputParameter("archiveSize",Integer.parseInt(args[2]));
//    algorithm.setInputParameter("archiveSize",100);                           // jMetal default value
    algorithm.setInputParameter("biSections",1);
    
    /* number of bisections: (AdaptiveGrid --> java.lang.OutOfMemoryError
     *   2D-5D: 5 
     *      6D: 4
     *      8D: 3
     * 10D-14D: 2
     */
    if (true) {
        //extract dimensionality from problem and set the biSections correspondingly
        int objectives = problem.getNumberOfObjectives();
        int biSections = 0;
        if (objectives<=5) 
            biSections = 5;
        else if (objectives==6)
            biSections = 4;
        else if (objectives==8)
            biSections = 3;
        else if (objectives>=10 && objectives<=14)
            biSections = 2;
        else
            biSections = 1;
        algorithm.setInputParameter("biSections",biSections);
    }
    
    
//    algorithm.setInputParameter("maxEvaluations",25000);
      
    // Mutation (Real variables)
    mutation = MutationFactory.getMutationOperator("PolynomialMutation");                    
    mutation.setParameter("probability",1.0/problem.getNumberOfVariables());
    mutation.setParameter("distributionIndex",20.0);
    
    // Mutation (BinaryReal variables)
    //mutation = MutationFactory.getMutationOperator("BitFlipMutation");                    
    //mutation.setParameter("probability",0.1);
    
    // Add the operators to the algorithm
    algorithm.addOperator("mutation", mutation);
    
    
    
    Operator crossover = CrossoverFactory.getCrossoverOperator("NoCrossover");           // PAES does originally not do any crossover
    algorithm.addOperator("crossover",crossover);
    
    
    algorithm.setInputParameter("indicators", indicators) ;
    algorithm.addOperator("selection", (Selection) SelectionFactory.getSelectionOperator(args[8]));
    
    
    
    // Execute the Algorithm 
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;
    
    // Result messages 
    // STEP 8. Print the results
    logger_.info("Total execution time: "+estimatedTime + "ms");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");    
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
  
//    if (indicators != null) {
    if (indicators != null && Boolean.parseBoolean(args[6]) == false) {
      logger_.info("Quality indicators") ;
      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
      logger_.info("GD         : " + indicators.getGD(population)) ;
      logger_.info("IGD        : " + indicators.getIGD(population)) ;
      logger_.info("Spread     : " + indicators.getSpread(population)) ;
      logger_.info("Epsilon    : " + indicators.getEpsilon(population)) ;  
    } // if
  }//main
} // PAES_main
