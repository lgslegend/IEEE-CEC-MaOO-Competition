/**
 * NSGAIIStudy.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.experiments;

import java.io.File;
import java.util.logging.Logger;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.operator.selection.Selection;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.experiments.settings.BFNW_Settings;
import jmetal.experiments.settings.NSGAII_Settings;
import jmetal.experiments.util.RBoxplot;
import jmetal.experiments.util.RWilcoxon;
import jmetal.util.JMException;

/**
 * @author Antonio J. Nebro
 */
public class BFNWStudy extends Experiment {
  
  /**
   * Configures the algorithms in each independent run
   * @param problem The problem to solve
   * @param problemIndex
   * @param algorithm Array containing the algorithms to run
   * @throws ClassNotFoundException 
   */
  public synchronized void algorithmSettings(Problem problem, 
  		                                       int problemIndex, 
  		                                       Algorithm[] algorithm,
                                                       String[] commandLineArgs)
    throws ClassNotFoundException {  	
  	try {
      int numberOfAlgorithms = algorithmNameList_.length;

      Properties[] parameters = new Properties[numberOfAlgorithms];

      for (int i = 0; i < numberOfAlgorithms; i++) {
        parameters[i] = new Properties();
      } // for

//      parameters[0].setProperty("crossoverProbability_", "1.0");
//      parameters[1].setProperty("crossoverProbability_", "0.5");
//      parameters[2].setProperty("crossoverProbability_", "0.1");
//      parameters[3].setProperty("crossoverProbability_", "0.7");

      if ((!paretoFrontFile_[problemIndex].equals("")) || 
      		(paretoFrontFile_[problemIndex] == null)) {
        for (int i = 0; i < numberOfAlgorithms; i++)
          parameters[i].setProperty("paretoFrontFile_", 
          		                       paretoFrontFile_[problemIndex]);
      } // if
 
      for (int i = 0; i < numberOfAlgorithms; i++)
        algorithm[i] = new BFNW_Settings(problem, commandLineArgs).configure(parameters[i]);

      algorithm[0].addOperator("selection", (Selection) SelectionFactory.getSelectionOperator("RandomSelection"));
      algorithm[1].addOperator("selection", (Selection) SelectionFactory.getSelectionOperator("BinaryTournament"));
      algorithm[2].addOperator("selection", (Selection) SelectionFactory.getSelectionOperator("BinaryTournamentFitness"));
      algorithm[3].addOperator("selection", (Selection) SelectionFactory.getSelectionOperator("BinaryTournamentFitnessInverted"));


      algorithm[0].setInputParameter("infoPrinterHowOften", 1000000);
      algorithm[1].setInputParameter("infoPrinterHowOften", 1000000);
      algorithm[2].setInputParameter("infoPrinterHowOften", 1000000);
      algorithm[3].setInputParameter("infoPrinterHowOften", 1000000);

    } catch (IllegalArgumentException ex) {
      Logger.getLogger(BFNWStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(BFNWStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch (JMException ex) {
      Logger.getLogger(BFNWStudy.class.getName()).log(Level.SEVERE, null, ex);
    }
  } // algorithmSettings
  
  public static void main(String[] args) throws JMException, IOException {
    BFNWStudy exp = new BFNWStudy() ; // exp = experiment
    
    exp.experimentName_  = "BFNWStudy" ;
    exp.algorithmNameList_   = new String[] {
      "BFNWrandom", "BFNWtournament", "BFNWfitness", "BFNWfitnessinverted"} ;
//      "BFNWa", "BFNWb", "BFNWc", "BFNWd"} ;
    exp.problemList_     = new String[] {
      "DTLZ3_2D", "DTLZ3_3D"
//      "DTLZ1_2D", "DTLZ1_6D", "DTLZ1_10D", "DTLZ1_14D",
//      "DTLZ2_2D", "DTLZ2_6D", "DTLZ2_10D", "DTLZ2_14D",
//      "DTLZ3_2D", "DTLZ3_6D", "DTLZ3_10D", "DTLZ3_14D",
//      "DTLZ4_2D", "DTLZ4_6D", "DTLZ4_10D", "DTLZ4_14D"
//              , "DTLZ1"
//              , "WFG2"
    } ;
    exp.paretoFrontFile_ = new String[] {
      //"ZDT1.pf", "ZDT2.pf", "ZDT3.pf","ZDT4.pf", "DTLZ1.2D.pf", "WFG2.2D.pf"} ;
        "DTLZ2.2D.10000.pf", "DTLZ2.3D.10000.pf"
//        "DTLZ1.2D.10000.pf", "DTLZ1.6D.10000.pf", "DTLZ1.10D.10000.pf", "DTLZ1.14D.10000.pf",
//        "DTLZ2.2D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.10D.10000.pf", "DTLZ2.14D.10000.pf",
//        "DTLZ2.2D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.10D.10000.pf", "DTLZ2.14D.10000.pf",
//        "DTLZ2.2D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.10D.10000.pf", "DTLZ2.14D.10000.pf"
//                , "DTLZ1.3D.pf"
//                , "WFG2.2D.pf"
    } ;
    exp.indicatorList_   = new String[] {"HV", "EPSILON"} ;
//    exp.indicatorList_   = new String[] {"HV", "SPREAD", "IGD", "EPSILON"} ;
    
    int numberOfAlgorithms = exp.algorithmNameList_.length ;

//    exp.experimentBaseDirectory_ = "/Users/antonio/Softw/pruebas/jmetal/" + exp.experimentName_;
//    exp.paretoFrontDirectory_ = "/Users/antonio/Softw/pruebas/data/paretoFronts";
    //exp.experimentBaseDirectory_ = "D:/_mpi/svn/emo-bfnw/software/bfnw-jmetal/" + exp.experimentName_;

//  modifications by markus
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");
    String timestamp = sdf.format(cal.getTime());

//    exp.experimentBaseDirectory_ = "/Users/antonio/Softw/pruebas/jmetal/" +
//    exp.paretoFrontDirectory_ = "/Users/antonio/Softw/pruebas/data/paretoFronts";

// hardcoded version:
//    String base = "D:/_mpi/svn/emo-bfnw/software/bfnw-jmetal/";
//    System.out.println(base);

// BETTER: get current working directory
    File currentDir = new File(".");
    String base = currentDir.getCanonicalPath() + "/";
    System.out.println(base);

    // important: replace backslashes, as R complains about "escaped characters"
    base = base.replace("\\", "/");

    // create a unique directory name by adding a timestamp and the experiment's name
    exp.experimentBaseDirectory_ = base +
                                   timestamp + 
                                   exp.experimentName_;

    System.out.println("exp.experimentBaseDirectory_="+exp.experimentBaseDirectory_);

    //hardcoded
    exp.paretoFrontDirectory_ = base+ "originalParetoFronts/";

    exp.algorithmSettings_ = new Settings[numberOfAlgorithms] ;
    
    exp.independentRuns_ = 100 ;
//    exp.independentRuns_ = 2 ;
    
    // Run the experiments
    int numberOfThreads ;
    exp.runExperiment(numberOfThreads = 2, args) ;                             // note: for parallelization, the number of problems is limiting! (i.e. 6 problems = 6 thread maximum, number of algorithms unimportant)
    
    // Generate latex tables (comment this sentence is not desired)
    exp.generateLatexTables() ;
    
    // Configure the R scripts to be generated
    int rows  ;
    int columns  ;
    String prefix ;
    String [] problems ;

    rows = 2 ;
    columns = 3 ;
    prefix = new String("Problems");
    //problems = new String[]{"ZDT1", "ZDT2","ZDT3", "ZDT4", "DTLZ1", "WFG2"} ;
    problems = exp.problemList_;
//    problems = new String[]{"DTLZ3_2D", "DTLZ3_3D"
//            , "DTLZ1"
//            , "WFG2"
//    } ;

    boolean notch ;
    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch = true, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;  
  } // main
} // NSGAIIStudy


