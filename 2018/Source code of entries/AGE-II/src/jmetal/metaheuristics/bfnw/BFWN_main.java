/**
 * BFWN_main.java
 *
 * @author Markus Wagner
 * @version 1.0
 */
package jmetal.metaheuristics.bfnw;

import java.io.IOException;
import java.util.Arrays;
import jmetal.base.*;
import jmetal.base.operator.crossover.*   ;
import jmetal.base.operator.mutation.*    ;
import jmetal.base.operator.selection.*   ;
import jmetal.problems.*                  ;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ09.* ;
import jmetal.qualityIndicator.QualityIndicator;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.base.operator.comparator.FitnessAndCrowdingDistanceComparator;
import jmetal.base.operator.comparator.FitnessComparator;
import jmetal.metaheuristics.ibea.IBEA_main;
import jmetal.problems.singleObjective.*;

public class BFWN_main {
//  public static Logger      logger_ ;      // Logger object
//  public static FileHandler fileHandler_ ; // FileHandler object

  /**
   * Usage: three choices
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main problemName
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main problemName paretoFrontFile
   */
  
    
    public static double median(double[] d) {
        Arrays.sort(d);
        int position = d.length/2;
        return d[position];
    }
    
    
  public static void main(String [] args) throws JMException, IOException, ClassNotFoundException {
      
      if (true) {
          BFWN_main.mainORIG(args);
          
      } else {
          double epsilonSum = 0;
          int repetitions = 10;
          String[] setup = null;
//          String[] setup = new String[]{"DTLZ4_4D", "originalParetoFronts\\DTLZ2.4D.1000.pf", "100", "true", "true", "0.01", "true","15000","RandomSelection","foo"};
//          String[] setup = new String[]{"DTLZ4_2D", "originalParetoFronts\\DTLZ2.2D.1000.pf", "100", "true", "true", "0.01", "true","15000","BinaryTournament3","foo"}; // BT3: dominance, fitness, crowding
          if (args.length==0) {
//              setup = new String[]{"LZ09_F5", "originalParetoFronts\\LZ09_F5.pf", "100", "true", "true", "0", "false","300000","BinaryTournament2","foo"};
                setup = new String[]{"WFG4_3D", "originalParetoFronts\\WFG4.3D.pf", "100", "true", "true", "0", "true","50000","BinaryTournament2","foo"}; // BT3: dominance, crowding
//                setup = new String[]{"DTLZ1_2D", "originalParetoFronts\\DTLZ1.2D.1000.pf", "100", "true", "true", "0", "true","100000","BinaryTournament2","foo"}; // BT3: dominance, crowding
//                setup = new String[]{"DTLZ4_2D", "originalParetoFronts\\DTLZ2.2D.1000.pf", "100", "true", "true", "0", "true","15000","RandomSelection","foo"}; // BT3: dominance, crowding
          } else {
              setup = args;
          }
          for (int i = 1; i<=repetitions; i++) {
              System.out.println("miniStudy: repetition "+i);
              double temp = BFWN_main.mainORIG(setup);
            epsilonSum += temp;   //problem, Pareto front, pop size, doCrossover, doMutation
            System.out.println("miniStudy: repetition "+(i) + "/"+repetitions+" result epsilon="+temp + " (avg so far: "+ epsilonSum/(i+0d) +")");
            System.gc();
          }
          double epsilonAvg = epsilonSum/repetitions;
          System.out.println("\n\nminiStudy\n"+
                  Arrays.toString(setup) + 
                  " --> avg of "+repetitions+" runs is "+ epsilonAvg);
      }
  }
  
  public static double mainORIG(String [] args) throws JMException, IOException, ClassNotFoundException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator

    QualityIndicator indicators ; // Object to get quality indicators

    // Logger object and file to store log messages
//    logger_      = Configuration.logger_ ;
//    fileHandler_ = new FileHandler("BFNW.log");
//    logger_.addHandler(fileHandler_) ;
    
    indicators = null ;
    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
    } // if
    else if (args.length == 2 || args.length >= 6) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
      indicators = new QualityIndicator(problem, args[1]) ;
    } // if
    else { // Default problem
//      problem = new Kursawe("Real", 3);
//      problem = new LinearFunction("Real");
      //problem = new Kursawe("BinaryReal", 3);
      //problem = new Water("Real");
      //problem = new ZDT1("ArrayReal", 100);
      //problem = new ConstrEx("Real");
//      problem = new Griewank("Real",1);
      problem = new DTLZ1("Real");
//      indicators = new QualityIndicator(problem, "originalParetoFronts\\DTLZ1.2D.pf") ;
    } // else

    algorithm = new BFNW(problem);

    // Algorithm parameters
//    algorithm.setInputParameter("populationSize",100);
//    algorithm.setInputParameter("archiveSize",100);
//    algorithm.setInputParameter("archiveSize",100);
//    algorithm.setInputParameter("maxEvaluations",500);
    algorithm.setInputParameter("maxEvaluations",25000);

    if (args.length >= 5) {
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

    // Mutation and Crossover for Real codification
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");
    crossover.setParameter("probability",0.9);
    crossover.setParameter("distribuitionIndex",20.0);
    mutation = MutationFactory.getMutationOperator("PolynomialMutation"); //orig
    mutation.setParameter("probability",1.0/problem.getNumberOfVariables());
    mutation.setParameter("distributionIndex",20.0);

    /* Mutation and Crossover Binary codification */
    /*
    crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover");
    crossover.setParameter("probability",0.9);
    mutation = MutationFactory.getMutationOperator("BitFlipMutation");
    mutation.setParameter("probability",1.0/80);
    */

    /* Selection Operator */
//    selection = new RandomSelection();
    selection = new BinaryTournament(new FitnessComparator());                  // minimizer!
//    selection = new BinaryTournament(new FitnessAndCrowdingDistanceComparator());
    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);
    algorithm.setInputParameter("indicators", indicators) ;
    algorithm.addOperator("selection", (Selection) SelectionFactory.getSelectionOperator(args[8]));

    // Execute the Algorithm
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;

    // Print the results
    System.out.println("Total execution time: "+estimatedTime + "ms");
//    logger_.info("Variables values have been writen to file VAR");
//    population.printVariablesToFile("VAR");
//    logger_.info("Objectives values have been writen to file FUN");
//    population.printObjectivesToFile("FUN");
  
    double epsilon = 0;
//    if (indicators != null) {
    if (indicators != null && Boolean.parseBoolean(args[6])==false) {
      System.out.println("Quality indicators") ;
      System.out.println("Hypervolume: " + indicators.getHypervolume(population)) ;
      System.out.println("HypervolumeCEC2018: " + indicators.getHypervolumeCEC2018(population)) ;
      System.out.println("GD         : " + indicators.getGD(population)) ;
      System.out.println("IGD        : " + indicators.getIGD(population)) ;
      System.out.println("Spread     : " + indicators.getSpread(population)) ;
//      logger_.info("Quality indicators") ;
//      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
//      logger_.info("GD         : " + indicators.getGD(population)) ;
//      logger_.info("IGD        : " + indicators.getIGD(population)) ;
//      logger_.info("Spread     : " + indicators.getSpread(population)) ;
      epsilon = indicators.getEpsilon(population);
      System.out.println("Epsilon    : " + epsilon) ;  
//      logger_.info("Epsilon    : " + epsilon) ;  
    } // if
    return indicators.getEpsilon(population);
  } //main
} // BFWN_main.java