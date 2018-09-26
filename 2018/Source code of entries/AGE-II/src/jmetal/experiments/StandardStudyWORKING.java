///*
// * 1) works with:
// *      mwagner@lap-11-02 /cygdrive/d/mpi/svn/emo-bfnw/software/bfnw-jmetal/build/classes
// *      $ java jmetal.experiments.StandardStudy
// * 2) make sure that "originalParetoFronts" is under "...bfnw-jmetal/build/classes"
// */
//
//
//
//
//
///**
// * StandardStudy.java
// *
// * @author Antonio J. Nebro
// * @version 1.0
// */
//package jmetal.experiments;
//
//import java.io.File;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Properties;
//import jmetal.base.Algorithm;
//import jmetal.base.Problem;
//import jmetal.experiments.settings.BFNW_Settings;
//import jmetal.experiments.settings.IBEA_Settings;
//import jmetal.experiments.settings.NSGAII_Settings;
//import jmetal.util.JMException;
//
///**
// * @author Antonio J. Nebro
// */
//public class StandardStudyWORKING extends Experiment {
//
//  /**
//   * Configures the algorithms in each independent run
//   * @param problem The problem to solve
//   * @param problemIndex
//   * @throws ClassNotFoundException
//   */
//  public void algorithmSettings(Problem problem,
//  		                          int problemIndex,
//  		                          Algorithm[] algorithm) throws ClassNotFoundException {
//    try {
//      int numberOfAlgorithms = algorithmNameList_.length;
//
//      Properties[] parameters = new Properties[numberOfAlgorithms];
//
//      for (int i = 0; i < numberOfAlgorithms; i++) {
//        parameters[i] = new Properties();
//      } // for
//
//      if (!paretoFrontFile_[problemIndex].equals("")) {
//        for (int i = 0; i < numberOfAlgorithms; i++)
//          parameters[i].setProperty("paretoFrontFile_", paretoFrontFile_[problemIndex]);
//        } // if
//
//        algorithm[0] = new NSGAII_Settings(problem).configure(parameters[0]);
//        algorithm[1] = new IBEA_Settings(problem).configure(parameters[1]);
//        algorithm[2] = new BFNW_Settings(problem).configure(parameters[2]);
////        algorithm[1] = new SPEA2_Settings(problem).configure(parameters[1]);
////        algorithm[2] = new MOCell_Settings(problem).configure(parameters[2]);
////        algorithm[3] = new SMPSO_Settings(problem).configure(parameters[3]);
////        algorithm[4] = new GDE3_Settings(problem).configure(parameters[4]);
//      } catch (IllegalArgumentException ex) {
//      Logger.getLogger(StandardStudyWORKING.class.getName()).log(Level.SEVERE, null, ex);
//    } catch (IllegalAccessException ex) {
//      Logger.getLogger(StandardStudyWORKING.class.getName()).log(Level.SEVERE, null, ex);
//    } catch  (JMException ex) {
//      Logger.getLogger(StandardStudyWORKING.class.getName()).log(Level.SEVERE, null, ex);
//    }
//  } // algorithmSettings
//
//  /**
//   * Main method
//   * @param args
//   * @throws JMException
//   * @throws IOException
//   */
//  public static void main(String[] args) throws JMException, IOException {
//    StandardStudyWORKING exp = new StandardStudyWORKING();
//
//    exp.experimentName_ = "StandardStudy";
//    exp.algorithmNameList_ = new String[]{
//                                //"NSGAII", "SPEA2", "MOCell", "SMPSO", "GDE3"};
//                                "NSGAII", "IBEA"
//                                ,"BFNW"
//    };
//    exp.problemList_ = new String[]{"ZDT1", "ZDT2","ZDT3",
////                                    "ZDT4","ZDT6",
////                                    "WFG1","WFG2","WFG3","WFG4","WFG5","WFG6",
////                                    "WFG7","WFG8","WFG9",
////                                    "DTLZ1","DTLZ2","DTLZ3","DTLZ4","DTLZ5",
////                                    "DTLZ6",
//                                    "DTLZ7"};
//    exp.paretoFrontFile_ = new String[]{"ZDT1.pf", "ZDT2.pf","ZDT3.pf",
////                                    "ZDT4.pf","ZDT6.pf",
////                                    "WFG1.2D.pf","WFG2.2D.pf","WFG3.2D.pf",
////                                    "WFG4.2D.pf","WFG5.2D.pf","WFG6.2D.pf",
////                                    "WFG7.2D.pf","WFG8.2D.pf","WFG9.2D.pf",
////                                    "DTLZ1.2D.pf","DTLZ2.2D.pf","DTLZ3.2D.pf",
////                                    "DTLZ4.2D.pf","DTLZ5.2D.pf",
////                                    "DTLZ6.2D.pf",
//                                    "DTLZ7.2D.pf"};
//
//    exp.indicatorList_ = new String[]{"HV", "SPREAD", "EPSILON"};
//
//    int numberOfAlgorithms = exp.algorithmNameList_.length;
//
////    exp.experimentBaseDirectory_ = "/Users/antonio/Softw/pruebas/jmetal/" +
////                                   exp.experimentName_;
////    exp.paretoFrontDirectory_ = "/Users/antonio/Softw/pruebas/data/paretoFronts";
//// hardcoded version:
////    String base = "D:/_mpi/svn/emo-bfnw/software/bfnw-jmetal/";
////    System.out.println(base);
//
//    //  modifications by markus
//    Calendar cal = Calendar.getInstance();
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");
//    String timestamp = sdf.format(cal.getTime());
//
//// BETTER: get current working directory
//    File currentDir = new File(".");
//    String base = currentDir.getCanonicalPath() + "/";
//    System.out.println(base);
//
//    // important: replace backslashes, as R complains about "escaped characters"
//    base = base.replace("\\", "/");
//
//    // create a unique directory name by adding a timestamp and the experiment's name
//    exp.experimentBaseDirectory_ = base +
//                                   timestamp +
//                                   exp.experimentName_;
//
//    System.out.println("exp.experimentBaseDirectory_="+exp.experimentBaseDirectory_);
//
//    //hardcoded
//    exp.paretoFrontDirectory_ = base+ "originalParetoFronts/";
//
//
////    exp.experimentBaseDirectory_ = "D:/_mpi/svn/emo-bfnw/software/bfnw-jmetal" +
////                                   exp.experimentName_;
////    exp.paretoFrontDirectory_ = "D:/_mpi/svn/emo-bfnw/software/bfnw-jmetal/originalParetoFronts";
//
//    System.out.println(exp.experimentBaseDirectory_);
//    System.out.println(exp.paretoFrontDirectory_);
//
//
//    exp.algorithmSettings_ = new Settings[numberOfAlgorithms];
//
////    exp.independentRuns_ = 100;
//    exp.independentRuns_ = 2;
//
//    // Run the experiments
//    int numberOfThreads ;
//    exp.runExperiment(numberOfThreads = 2) ;
//
//    // Generate latex tables
//    exp.generateLatexTables() ;
//
//    // Configure the R scripts to be generated
//    int rows  ;
//    int columns  ;
//    String prefix ;
//    String [] problems ;
//    boolean notch ;
//
//    // Configuring scripts for ZDT
//    rows = 3 ;
//    columns = 2 ;
//    prefix = new String("ZDT");
//    problems = new String[]{"ZDT1", "ZDT2","ZDT3"
////            , "ZDT4","ZDT6"
//    } ;
//
//    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch = false, exp) ;
//    exp.generateRWilcoxonScripts(problems, prefix, exp) ;
//
//    // Configure scripts for DTLZ
//    rows = 3 ;
//    columns = 3 ;
//    prefix = new String("DTLZ");
//    problems = new String[]{
////        "DTLZ1","DTLZ2","DTLZ3","DTLZ4","DTLZ5",
////                                    "DTLZ6",
//                                    "DTLZ7"} ;
//
//    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch=false, exp) ;
//    exp.generateRWilcoxonScripts(problems, prefix, exp) ;
//
//    // Configure scripts for WFG
//    rows = 3 ;
//    columns = 3 ;
//    prefix = new String("WFG");
//    problems = new String[]{"WFG1","WFG2","WFG3","WFG4","WFG5","WFG6",
//                            "WFG7","WFG8","WFG9"} ;
//
//    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch=false, exp) ;
//    exp.generateRWilcoxonScripts(problems, prefix, exp) ;
//  } // main
//} // StandardStudy
//
//
