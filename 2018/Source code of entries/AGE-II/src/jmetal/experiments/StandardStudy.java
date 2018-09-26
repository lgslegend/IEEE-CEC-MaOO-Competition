/*
 * 1) works with:
 *      mwagner@lap-11-02 /cygdrive/d/mpi/svn/emo-bfnw/software/bfnw-jmetal/build/classes
 *      $ java jmetal.experiments.StandardStudy
 * 2) make sure that "originalParetoFronts" is under "...bfnw-jmetal/build/classes"
 *
 * 3) has 8 input parameters:
 *   # max workerThreads
 *   # max evaluations
 *   selector as String (using SelectionFactory.getSelectionOperator), e.g. "BinaryTournament", "BinaryTournament2", "PESA2Selection", "RandomSelection", "RankingAndCrowdingSelection", "DifferentialEvolutionSelection"
 *   crossover (true/false)
 *   mutation (true/false)
 *   # independent runs
 *   trailing info-string for experiment-directory
 *   epsilonGridWidth for BFNW-"archive reduction"
 *
 *  --> example:   java -Xmx32g jmetal.experiments.StandardStudy 16 50000 RandomSelection false true 15 50000randomfalsetrue15 > 50000randomfalsetrue15.log
 * (default crossover: SBX, mutation: polynomial)
 */





/**
 * StandardStudy.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.experiments;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.operator.selection.Selection;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.experiments.settings.BFNW_Settings;
import jmetal.experiments.settings.IBEA_Settings;
import jmetal.experiments.settings.NSGAII_Settings;
import jmetal.util.JMException;

/**
 * @author Antonio J. Nebro
 */
public class StandardStudy extends Experiment {

  /**
   * Configures the algorithms in each independent run
   * @param problem The problem to solve
   * @param problemIndex
   * @throws ClassNotFoundException 
   */
  public void algorithmSettings(Problem problem, 
  		                          int problemIndex, 
  		                          Algorithm[] algorithm
                                          ,String[] commandLineArgs
                                          ) throws ClassNotFoundException {
    try {
      int numberOfAlgorithms = algorithmNameList_.length;

      Properties[] parameters = new Properties[numberOfAlgorithms];

      for (int i = 0; i < numberOfAlgorithms; i++) {
        parameters[i] = new Properties();
      } // for

      if (!paretoFrontFile_[problemIndex].equals("")) {
        for (int i = 0; i < numberOfAlgorithms; i++)
          parameters[i].setProperty("paretoFrontFile_", paretoFrontFile_[problemIndex]);
        } // if

        algorithm[0] = new NSGAII_Settings(problem, commandLineArgs).configure(parameters[0]);
        algorithm[1] = new IBEA_Settings(problem, commandLineArgs).configure(parameters[1]);
        algorithm[2] = new BFNW_Settings(problem, commandLineArgs).configure(parameters[2]);
//        algorithm[1] = new SPEA2_Settings(problem).configure(parameters[1]);
//        algorithm[2] = new MOCell_Settings(problem).configure(parameters[2]);
//        algorithm[3] = new SMPSO_Settings(problem).configure(parameters[3]);
//        algorithm[4] = new GDE3_Settings(problem).configure(parameters[4]);
      } catch (IllegalArgumentException ex) {
      Logger.getLogger(StandardStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(StandardStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch  (JMException ex) {
      Logger.getLogger(StandardStudy.class.getName()).log(Level.SEVERE, null, ex);
    }

    // test whether algorithm settings can be overwritten: (should be, are hashmaps)
    for (Algorithm a: algorithm) {
        a.setInputParameter("maxEvaluations", Integer.parseInt(commandLineArgs[1]));

        try { 
            a.addOperator("selection", (Selection) SelectionFactory.getSelectionOperator(commandLineArgs[2]));
        } catch (JMException ex) {
            Logger.getLogger(StandardStudy.class.getName()).log(Level.SEVERE, null, ex);
        }

        a.setInputParameter("doCrossover", Boolean.parseBoolean(commandLineArgs[3]));
        a.setInputParameter("doMutation", Boolean.parseBoolean(commandLineArgs[4]));
        a.setInputParameter("epsilonGridWidth", Double.parseDouble(commandLineArgs[7]));
        
    }


  } // algorithmSettings

  /**
   * Main method
   * @param args
   * @throws JMException
   * @throws IOException
   */
  public static void main(String[] args) throws JMException, IOException {
      int commandLineNumberOfWorkerThreads = 2;

    StandardStudy exp = new StandardStudy();



    exp.experimentName_ = "StandardStudy";
    exp.algorithmNameList_ = new String[]{
                                //"NSGAII", "SPEA2", "MOCell", "SMPSO", "GDE3"};
                                "NSGAII", "IBEA"
                                ,"BFNW"
    };
    exp.problemList_ = new String[]{"ZDT1", "ZDT2","ZDT3",
                                    "ZDT4","ZDT6",
                                    "WFG1","WFG2",
                                    "WFG3","WFG4","WFG5","WFG6",
                                    "WFG7","WFG8","WFG9",
                                    "DTLZ1","DTLZ2","DTLZ3","DTLZ4","DTLZ5",
                                    "DTLZ6",
                                    "DTLZ7"};
    exp.paretoFrontFile_ = new String[]{"ZDT1.pf", "ZDT2.pf","ZDT3.pf",
                                    "ZDT4.pf","ZDT6.pf",
                                    "WFG1.2D.pf","WFG2.2D.pf",
                                    "WFG3.2D.pf",
                                    "WFG4.2D.pf","WFG5.2D.pf","WFG6.2D.pf",
                                    "WFG7.2D.pf","WFG8.2D.pf","WFG9.2D.pf",
                                    "DTLZ1.3D.pf","DTLZ2.3D.pf","DTLZ3.3D.pf",
                                    "DTLZ4.3D.pf","DTLZ5.3D.pf",
                                    "DTLZ6.3D.pf",
                                    "DTLZ7.3D.pf"};

    exp.indicatorList_ = new String[]{"HV", "SPREAD", "EPSILON"};

    int numberOfAlgorithms = exp.algorithmNameList_.length;

//    exp.experimentBaseDirectory_ = "/Users/antonio/Softw/pruebas/jmetal/" +
//                                   exp.experimentName_;
//    exp.paretoFrontDirectory_ = "/Users/antonio/Softw/pruebas/data/paretoFronts";
// hardcoded version:
//    String base = "D:/_mpi/svn/emo-bfnw/software/bfnw-jmetal/";
//    System.out.println(base);

    //  modifications by markus
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");
    String timestamp = sdf.format(cal.getTime());
    
// BETTER: get current working directory
    File currentDir = new File(".");
    String base = currentDir.getCanonicalPath() + "/";
    System.out.println("current dir: "+base);

    // important: replace backslashes, as R complains about "escaped characters"
    base = base.replace("\\", "/");

    // create a unique directory name by adding a timestamp and the experiment's name
    exp.experimentBaseDirectory_ = base +
                                   timestamp +
                                   exp.experimentName_;

    if (args.length>=0) {
        exp.experimentBaseDirectory_ = exp.experimentBaseDirectory_ + "_" + args[6];
    }

    System.out.println("exp.experimentBaseDirectory_="+exp.experimentBaseDirectory_);

    //hardcoded
    exp.paretoFrontDirectory_ = base+ "originalParetoFronts/";


//    exp.experimentBaseDirectory_ = "D:/_mpi/svn/emo-bfnw/software/bfnw-jmetal" +
//                                   exp.experimentName_;
//    exp.paretoFrontDirectory_ = "D:/_mpi/svn/emo-bfnw/software/bfnw-jmetal/originalParetoFronts";

    System.out.println(exp.experimentBaseDirectory_);
    System.out.println(exp.paretoFrontDirectory_);


    exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

//    exp.independentRuns_ = 100;
    exp.independentRuns_ = 15;


    if (args.length>=0) {
          commandLineNumberOfWorkerThreads = Integer.parseInt(args[0]);
          exp.independentRuns_ = Integer.parseInt(args[5]);
      }


    // Run the experiments
    int numberOfThreads ;
    exp.runExperiment(numberOfThreads = commandLineNumberOfWorkerThreads, args) ;

    // Generate latex tables
    exp.generateLatexTables() ;

    // Configure the R scripts to be generated
    int rows  ;
    int columns  ;
    String prefix ;
    String [] problems ;
    boolean notch ;

    // Configuring scripts for ZDT
    rows = 3 ;
    columns = 2 ;
    prefix = new String("ZDT");
    problems = new String[]{"ZDT1", "ZDT2","ZDT3"
            , "ZDT4","ZDT6"
    } ;
    
    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch = false, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;

    // Configure scripts for DTLZ
    rows = 3 ;
    columns = 3 ;
    prefix = new String("DTLZ");
    problems = new String[]{
        "DTLZ1","DTLZ2","DTLZ3","DTLZ4","DTLZ5",
                                    "DTLZ6",
                                    "DTLZ7"} ;

    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch=false, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;

    // Configure scripts for WFG
    rows = 3 ;
    columns = 3 ;
    prefix = new String("WFG");
    problems = new String[]{"WFG1","WFG2"
            ,"WFG3","WFG4","WFG5","WFG6",
                            "WFG7","WFG8","WFG9"
    } ;

    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch=false, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;
  } // main
} // StandardStudy


