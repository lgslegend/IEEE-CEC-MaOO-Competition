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
import jmetal.experiments.settings.NSGAII_Settings;
import jmetal.experiments.util.RBoxplot;
import jmetal.experiments.util.RWilcoxon;
import jmetal.util.JMException;

/**
 * @author Antonio J. Nebro
 */
public class NSGAIIStudy extends Experiment {
  
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

      parameters[0].setProperty("crossoverProbability_", "1.0");
      parameters[1].setProperty("crossoverProbability_", "0.9");
      parameters[2].setProperty("crossoverProbability_", "0.8");
      parameters[3].setProperty("crossoverProbability_", "0.7"); 

      if ((!paretoFrontFile_[problemIndex].equals("")) || 
      		(paretoFrontFile_[problemIndex] == null)) {
        for (int i = 0; i < numberOfAlgorithms; i++)
          parameters[i].setProperty("paretoFrontFile_", 
          		                       paretoFrontFile_[problemIndex]);
      } // if
 
      for (int i = 0; i < numberOfAlgorithms; i++)
        algorithm[i] = new NSGAII_Settings(problem, commandLineArgs).configure(parameters[i]);
      
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(NSGAIIStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(NSGAIIStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch (JMException ex) {
      Logger.getLogger(NSGAIIStudy.class.getName()).log(Level.SEVERE, null, ex);
    }
  } // algorithmSettings
  
  public static void main(String[] args) throws JMException, IOException {
    NSGAIIStudy exp = new NSGAIIStudy() ; // exp = experiment
    
    exp.experimentName_  = "NSGAIIStudy" ;
    exp.algorithmNameList_   = new String[] {
      "NSGAIIa", "NSGAIIb", "NSGAIIc", "NSGAIId"} ;
    exp.problemList_     = new String[] {
      "ZDT1"
//              , "DTLZ1"
//              , "WFG2"
    } ;
    exp.paretoFrontFile_ = new String[] {
      //"ZDT1.pf", "ZDT2.pf", "ZDT3.pf","ZDT4.pf", "DTLZ1.2D.pf", "WFG2.2D.pf"} ;
        "ZDT1.pf"
//                , "DTLZ1.3D.pf"
//                , "WFG2.2D.pf"
    } ;
    exp.indicatorList_   = new String[] {"HV", "SPREAD", "IGD", "EPSILON"} ;
    
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
    
    //exp.independentRuns_ = 30 ;
    exp.independentRuns_ = 2 ;
    
    // Run the experiments
    int numberOfThreads ;
    exp.runExperiment(numberOfThreads = 1, args) ;
    
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
    problems = new String[]{"ZDT1"
//            , "DTLZ1"
//            , "WFG2"
    } ;

    boolean notch ;
    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch = true, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;  
  } // main
} // NSGAIIStudy


