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
import jmetal.experiments.settings.NSGAIIArchive_Settings;
import jmetal.experiments.util.RBoxplot;
import jmetal.experiments.util.RWilcoxon;
import jmetal.util.JMException;

/**
 * @author Antonio J. Nebro
 */
public class NSGAIIArchiveStudy extends Experiment {
  
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

      // the following does not work for INPUTPARAMETERS!!!
      parameters[0].setProperty("crowdingDistanceSwitch", "0");
      parameters[1].setProperty("crowdingDistanceSwitch", "1");
      parameters[2].setProperty("crowdingDistanceSwitch", "2");
      parameters[3].setProperty("crowdingDistanceSwitch", "3");
      parameters[4].setProperty("crowdingDistanceSwitch", "4");
      parameters[5].setProperty("crowdingDistanceSwitch", "5");
      parameters[6].setProperty("crowdingDistanceSwitch", "6");
      
      if ((!paretoFrontFile_[problemIndex].equals("")) || 
      		(paretoFrontFile_[problemIndex] == null)) {
        for (int i = 0; i < numberOfAlgorithms; i++)
          parameters[i].setProperty("paretoFrontFile_", 
          		                       paretoFrontFile_[problemIndex]);
      } // if
 
      for (int i = 0; i < numberOfAlgorithms; i++) {
        algorithm[i] = new NSGAIIArchive_Settings(problem, commandLineArgs).configure(parameters[i]);
        algorithm[i].setInputParameter("crowdingDistanceSwitch", i);                                        // set here
      }
      
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(NSGAIIArchiveStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(NSGAIIArchiveStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch (JMException ex) {
      Logger.getLogger(NSGAIIArchiveStudy.class.getName()).log(Level.SEVERE, null, ex);
    }
  } // algorithmSettings
  
  public static void main(String[] args) throws JMException, IOException {
    NSGAIIArchiveStudy exp = new NSGAIIArchiveStudy() ; // exp = experiment
    
    exp.experimentName_  = "NSGAIIArchiveStudy" ;
    exp.algorithmNameList_   = new String[] {
      "orig", "XP", "XPs", "XA", "XAs", "RP", "RA"} ;
    exp.problemList_     = new String[] {
        "DTLZ1_2D", "DTLZ1_4D", "DTLZ1_6D", "DTLZ1_8D", "DTLZ1_10D", "DTLZ1_12D", 
        "DTLZ2_2D", "DTLZ2_4D", "DTLZ2_6D", "DTLZ2_8D", "DTLZ2_10D", "DTLZ2_12D", 
        "DTLZ3_2D", "DTLZ3_4D", "DTLZ3_6D", "DTLZ3_8D", "DTLZ3_10D", "DTLZ3_12D", 
        "DTLZ4_2D", "DTLZ4_4D", "DTLZ4_6D", "DTLZ4_8D", "DTLZ4_10D", "DTLZ4_12D"
//        "DTLZ1_2D", "DTLZ1_6D", "DTLZ1_10D",
//        "DTLZ2_2D", "DTLZ2_6D", "DTLZ2_10D",
//        "DTLZ3_2D", "DTLZ3_6D", "DTLZ3_10D",
//        "DTLZ4_2D", "DTLZ4_6D", "DTLZ4_10D"
//              , "DTLZ1"
//              , "WFG2"
    } ;
    exp.paretoFrontFile_ = new String[] {
      //"ZDT1.pf", "ZDT2.pf", "ZDT3.pf","ZDT4.pf", "DTLZ1.2D.pf", "WFG2.2D.pf"} ;
      "DTLZ1.2D.10000.pf", "DTLZ1.4D.10000.pf", "DTLZ1.6D.10000.pf", "DTLZ1.8D.10000.pf", "DTLZ1.10D.10000.pf", "DTLZ1.12D.10000.pf", 
      "DTLZ2.2D.10000.pf", "DTLZ2.4D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.8D.10000.pf", "DTLZ2.10D.10000.pf", "DTLZ2.12D.10000.pf",
      "DTLZ2.2D.10000.pf", "DTLZ2.4D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.8D.10000.pf", "DTLZ2.10D.10000.pf", "DTLZ2.12D.10000.pf",
      "DTLZ2.2D.10000.pf", "DTLZ2.4D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.8D.10000.pf", "DTLZ2.10D.10000.pf", "DTLZ2.12D.10000.pf"
//      "DTLZ1.2D.10000.pf", "DTLZ1.4D.10000.pf", "DTLZ1.6D.10000.pf", "DTLZ1.10D.10000.pf", 
//      "DTLZ2.2D.10000.pf", "DTLZ2.4D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.10D.10000.pf", 
//      "DTLZ2.2D.10000.pf", "DTLZ2.4D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.10D.10000.pf", 
//      "DTLZ2.2D.10000.pf", "DTLZ2.4D.10000.pf", "DTLZ2.6D.10000.pf", "DTLZ2.10D.10000.pf"
//                , "DTLZ1.3D.pf"
//                , "WFG2.2D.pf"
    } ;
    exp.indicatorList_   = new String[] {"HV", "EPSILON", "SPREAD", "GSPREA", "IGD", "GD"} ;
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
    
    exp.independentRuns_ = 30 ;
//    exp.independentRuns_ = 2 ;
    
    // Run the experiments
    int numberOfThreads ;
    exp.runExperiment(numberOfThreads = 2, args) ;
    
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
//    problems = new String[]{"ZDT1"
////            , "DTLZ1"
////            , "WFG2"
//    } ;
    
    problems = exp.problemList_;

    boolean notch ;
    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch = true, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;  
  } // main
} // NSGAIIStudy


