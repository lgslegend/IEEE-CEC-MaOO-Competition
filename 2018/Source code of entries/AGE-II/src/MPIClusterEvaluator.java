
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.Problem;
import jmetal.base.SolutionSet;
import jmetal.experiments.Experiment;
import jmetal.experiments.StandardStudy;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.util.MetricsUtil;



/**
 *
 * @author mwagner
 */
public class MPIClusterEvaluator { 

    static String[] algorithmsA = { "BFNW", "IBEA", "NSGAII", "SMSEMOA", "SPEA2", "AGE2", "AGE3", "SteadyStateMOCMA", "MOCMA", "NSGAIII", "NSGAIIIAlternate" , "PAES", "SMPSO" /*NOTE: UPDATE DOWN AS WELL*/ };
    static String[] problemsA = {
                                    "LZ09_F1","LZ09_F2","LZ09_F3","LZ09_F4","LZ09_F5","LZ09_F6","LZ09_F7","LZ09_F8","LZ09_F9",
                                    "WFG1_2D","WFG2_2D","WFG3_2D","WFG4_2D","WFG5_2D","WFG6_2D","WFG7_2D","WFG8_2D","WFG9_2D",
                                    "WFG1_3D","WFG2_3D","WFG3_3D","WFG4_3D","WFG5_3D","WFG6_3D","WFG7_3D","WFG8_3D","WFG9_3D",
                                    "DTLZ1_2D","DTLZ1_3D","DTLZ1_4D","DTLZ1_5D","DTLZ1_6D","DTLZ1_7D","DTLZ1_8D","DTLZ1_9D","DTLZ1_10D","DTLZ1_11D","DTLZ1_12D","DTLZ1_13D","DTLZ1_14D","DTLZ1_15D","DTLZ1_16D","DTLZ1_17D","DTLZ1_18D","DTLZ1_19D","DTLZ1_20D",
                                    "DTLZ2_2D","DTLZ2_3D","DTLZ2_4D","DTLZ2_5D","DTLZ2_6D","DTLZ2_7D","DTLZ2_8D","DTLZ2_9D","DTLZ2_10D","DTLZ2_11D","DTLZ2_12D","DTLZ2_13D","DTLZ2_14D","DTLZ2_15D","DTLZ2_16D","DTLZ2_17D","DTLZ2_18D","DTLZ2_19D","DTLZ2_20D",
                                    "DTLZ3_2D","DTLZ3_3D","DTLZ3_4D","DTLZ3_5D","DTLZ3_6D","DTLZ3_7D","DTLZ3_8D","DTLZ3_9D","DTLZ3_10D","DTLZ3_11D","DTLZ3_12D","DTLZ3_13D","DTLZ3_14D","DTLZ3_15D","DTLZ3_16D","DTLZ3_17D","DTLZ3_18D","DTLZ3_19D","DTLZ3_20D",
                                    "DTLZ4_2D","DTLZ4_3D","DTLZ4_4D","DTLZ4_5D","DTLZ4_6D","DTLZ4_7D","DTLZ4_8D","DTLZ4_9D","DTLZ4_10D","DTLZ4_11D","DTLZ4_12D","DTLZ4_13D","DTLZ4_14D","DTLZ4_15D","DTLZ4_16D","DTLZ4_17D","DTLZ4_18D","DTLZ4_19D","DTLZ4_20D",
                                    "ZDT1","ZDT2","ZDT3","ZDT4","ZDT6",
//                                    "DTLZ2_2D","DTLZ2_3D","DTLZ2_4D","DTLZ2_5D","DTLZ2_6D","DTLZ2_8D","DTLZ2_10D","DTLZ2_12D","DTLZ2_14D","DTLZ2_16D","DTLZ2_18D","DTLZ2_20D",
//                                    "DTLZ3_2D","DTLZ3_3D","DTLZ3_4D","DTLZ3_5D","DTLZ3_6D","DTLZ3_8D","DTLZ3_10D","DTLZ3_12D","DTLZ3_14D","DTLZ3_16D","DTLZ3_18D","DTLZ3_20D",
//                                    "DTLZ4_2D","DTLZ4_3D","DTLZ4_4D","DTLZ4_5D","DTLZ4_6D","DTLZ4_8D","DTLZ4_10D","DTLZ4_12D","DTLZ4_14D","DTLZ4_16D","DTLZ4_18D","DTLZ4_20D",
        };
    static String[] indicatorsA = {"HV", "EPSILON"};

    public static void main (String[] args) throws Exception {

        Experiment exp = new StandardStudy(); // needed for printLatex
        exp.experimentName_ = "StandardStudy";
        exp.algorithmNameList_ = algorithmsA;
        exp.problemList_ = problemsA;
        exp.indicatorList_ = indicatorsA;
    double[][][] mean;
    double[][][] median;
    double[][][] stdDeviation;
    double[][][] iqr;
    double[][][] max;
    double[][][] min;
    int[][][] numberOfValues;
    mean = new double[exp.indicatorList_.length][][];
    median = new double[exp.indicatorList_.length][][];
    stdDeviation = new double[exp.indicatorList_.length][][];
    iqr = new double[exp.indicatorList_.length][][];
    min = new double[exp.indicatorList_.length][][];
    max = new double[exp.indicatorList_.length][][];
    numberOfValues = new int[exp.indicatorList_.length][][];
    for (int indicator = 0; indicator < exp.indicatorList_.length; indicator++) {
      // A data vector per problem
      mean[indicator] = new double[exp.problemList_.length][];
      median[indicator] = new double[exp.problemList_.length][];
      stdDeviation[indicator] = new double[exp.problemList_.length][];
      iqr[indicator] = new double[exp.problemList_.length][];
      min[indicator] = new double[exp.problemList_.length][];
      max[indicator] = new double[exp.problemList_.length][];
      numberOfValues[indicator] = new int[exp.problemList_.length][];
      for (int problem = 0; problem < exp.problemList_.length; problem++) {
        mean[indicator][problem] = new double[exp.algorithmNameList_.length];
        median[indicator][problem] = new double[exp.algorithmNameList_.length];
        stdDeviation[indicator][problem] = new double[exp.algorithmNameList_.length];
        iqr[indicator][problem] = new double[exp.algorithmNameList_.length];
        min[indicator][problem] = new double[exp.algorithmNameList_.length];
        max[indicator][problem] = new double[exp.algorithmNameList_.length];
        numberOfValues[indicator][problem] = new int[exp.algorithmNameList_.length];

//        for (int algorithm = 0; algorithm < exp.algorithmNameList_.length; algorithm++) {
//          Collections.sort(data[indicator][problem][algorithm]);
//
//          String directory = experimentBaseDirectory_;
//          directory += "/" + algorithmNameList_[algorithm];
//          directory += "/" + problemList_[problem];
//          directory += "/" + indicatorList_[indicator];
//
//          //System.out.println("----" + directory + "-----");
//          //calculateStatistics(data[indicator][problem][algorithm], meanV, medianV, minV, maxV, stdDeviationV, iqrV) ;
//          calculateStatistics(data[indicator][problem][algorithm], statValues);
//          /*
//          System.out.println("Mean: " + statValues.get("mean"));
//          System.out.println("Median : " + statValues.get("median"));
//          System.out.println("Std : " + statValues.get("stdDeviation"));
//          System.out.println("IQR : " + statValues.get("iqr"));
//          System.out.println("Min : " + statValues.get("min"));
//          System.out.println("Max : " + statValues.get("max"));
//          System.out.println("N_values: " + data[indicator][problem][algorithm].size()) ;
//           */
//          mean[indicator][problem][algorithm] = statValues.get("mean");
//          median[indicator][problem][algorithm] = statValues.get("median");
//          stdDeviation[indicator][problem][algorithm] = statValues.get("stdDeviation");
//          iqr[indicator][problem][algorithm] = statValues.get("iqr");
//          min[indicator][problem][algorithm] = statValues.get("min");
//          max[indicator][problem][algorithm] = statValues.get("max");
//          numberOfValues[indicator][problem][algorithm] = data[indicator][problem][algorithm].size();
//        }
      }
    }






        boolean debugPrint = false;

        long minimumTimeWindow = 0;  // in minutes
//        String subDir = "foo";
        String subDir = "mu25";
//        String subDir = "dtlz2mu25mini";
        int numberOfSampledPoints = 0;
//        boolean printCSVIndicatorHeaders = true;

        String meanMedian = "mean";
        
        String testKilledCheck = "false";
        String testCompletedCheck = "false";
        
        double hypervolumePoint = Double.NEGATIVE_INFINITY;//neutral: Double.NEGATIVE_INFINITY (means that the maximum of the points is used)

        if (args.length == 0) {
            System.out.println("arguments: <minAgeInMinutes> <subDir> <numberOfSampledPoints> <mean/median> "+
                    "[true/false for testKilledCheck] [true/false for testCompletedCheck]" +
                    "[hypPointAsSingleDouble]" + 
                    "\nNote: Shark results cannot be computed using the last two tests.\n");
            //System.exit(0);
            minimumTimeWindow = 0;
            numberOfSampledPoints = 1000;
        } else {
            minimumTimeWindow = Long.parseLong(args[0]);
            subDir = args[1];
            numberOfSampledPoints = Integer.parseInt(args[2]);
            meanMedian = args[3];
            
            if (args.length >= 5) testKilledCheck = args[4];
            if (args.length >= 6) testCompletedCheck = args[5];
            if (args.length >= 7) hypervolumePoint = Double.parseDouble(args[6]); // assuming a symmetric space
//            printCSVIndicatorHeaders = Boolean.parseBoolean(args[3]);
        }
        minimumTimeWindow = minimumTimeWindow * 60 * 1000;


        if (debugPrint) System.out.println("MPIClusterEvaluator.main()");
        File currentDir = new File(".");
        File currentDirClusterout = new File(".");
        if (subDir!="") currentDir = new File(subDir+"/");
        if (subDir!="") currentDirClusterout = new File(subDir+"-clusterout/");
//        if (subDir!="") currentDir = new File(subDir+System.getProperty("path.separator"));

//        currentDir = new File(currentDir.); 
        if (!debugPrint) System.out.println(currentDir.getCanonicalPath());
        File[] currentListLOG = currentDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".FUN");
            }});
        if (debugPrint) {
            for (File f:currentListLOG) System.out.println(f.getName());
        }

        System.out.print("// number of files in "+currentDir.getCanonicalPath()+": ");
        System.out.println(currentListLOG.length);
        
        if (currentListLOG.length == 0) {
            System.out.println("no suitable files in " + currentDir.getCanonicalPath());
            System.exit(0);
        }
        
        int count = 0;
        
        TreeMap top = new TreeMap();

        for (File f:currentListLOG) {
            String currentFile = f.getName();
            int posFirstMinus = currentFile.indexOf("-");
            int posSecondMinus = currentFile.indexOf("-", posFirstMinus+1);
            int posPoint = currentFile.indexOf(".");
            String currentProblem = currentFile.substring(0, posFirstMinus);
//            System.out.println(currentProblem);
            String currentAlgorithm = currentFile.substring(posFirstMinus+1, posSecondMinus);
//            long fileMillis = Long.parseLong(currentFile.substring(posSecondMinus+1, posPoint));
//            System.out.println(currentFile.substring(posSecondMinus+1, posSecondMinus+1+13));
            long fileMillis = Long.parseLong(currentFile.substring(posSecondMinus+1, posSecondMinus+1+13));

//            String currentTopKey = currentProblem;
//            String currentTopKey = currentProblem + "-"+ currentAlgorithm;

            // if file not old enough, skip it
            long currentTime = System.currentTimeMillis();
            if ( currentTime-fileMillis < minimumTimeWindow) {
                System.out.println("["+f.getName()+"] too young");
                continue;
            }
            System.out.println("["+f.getName()+"] old enough");
            
            
//            // here: insert test whether the ...-clusterout-file is complete, i.e. contains "INFO: Total execution time:"
//            // if not: continue;
//            if (testCompletedCheck.equalsIgnoreCase("true")) {
//                boolean test = testCompletedCheck(f, currentDirClusterout);
//                if (test) {
//                    // alright
//                    System.out.println("["+f.getName()+"] completed");
//                } else {
//                    // not good --> continue with next file
//                    System.out.println("["+f.getName()+"] NOT completed");
//                    continue;
//                }
//            }
            
            
            
            
            
            
            // here: insert test whether the process got killed, i.e. the logfile from stout contains "Killed"
            // if not: continue;
            if (testKilledCheck.equalsIgnoreCase("true")) {
                boolean test = testKilledCheck(f, currentDirClusterout);
                if (!test) {
                    // alright
                    System.out.println("["+f.getName()+"] not killed");
                } else {
                    // not good --> continue with next file
                    System.out.println("["+f.getName()+"] killed (skipping this file)");
                    continue;
                }
            }
            
            
            // here: insert test whether the ...-clusterout-file is complete, i.e. the logfile from stout contains "INFO: Total execution time:"
            // if not: continue;
            if (testCompletedCheck.equalsIgnoreCase("true")) {
                boolean test = testCompletedCheck(f, currentDirClusterout);
                if (test) {
                    // alright
                    System.out.println("["+f.getName()+"] completed");
                } else {
                    // not good --> continue with next file
                    System.out.println("["+f.getName()+"] not completed (skipping this file)");
                    continue;
                }
            }
            
            System.out.println("["+f.getName()+"] accepted");
            
            

            // --> add file to data structure
            count++;
            if (top.containsKey(currentProblem)) {
//            if (top.containsKey(currentTopKey)) {
                TreeMap algs = (TreeMap)top.get(currentProblem);
//                HashMap algs = (HashMap)top.get(currentProblem);

                if (algs.containsKey(currentAlgorithm)) {
                    ArrayList scenario2File = (ArrayList)algs.get(currentAlgorithm);
                    scenario2File.add(f);
                } else {
                    ArrayList scenario2File = new ArrayList();
                    scenario2File.add(f);
                    algs.put(currentAlgorithm, scenario2File);
                }
                                
            } else {
                ArrayList scenario2File = new ArrayList();
                scenario2File.add(f);

                TreeMap algs = new TreeMap();
//                HashMap algs = new HashMap();
                algs.put(currentAlgorithm, scenario2File);

                top.put(currentProblem, algs);

//                ArrayList scenario2File = new ArrayList();
//                scenario2File.add(f);
//                top.put(currentTopKey, scenario2File);
            }
        }
        System.out.println("// number of files in "+currentDir.getCanonicalPath()+" considered: "+count);

        // now the hashmap contains only old enough files

        Iterator it = top.keySet().iterator();

        String csvEPS = "QUALITY INDICATOR EPS ("+meanMedian+" values)\n";
        String csvEPSwoHeaders = "QUALITY INDICATOR EPS ("+meanMedian+" values)\n";
        String csvHYP = "QUALITY INDICATOR HYP ("+meanMedian+" values)\n";
        String csvHYPwoHeaders = "QUALITY INDICATOR HYP ("+meanMedian+" values)\n";

        /* used to collect all raw EPS and HYP of the populations in separate files,
         * as used ,e.g, for the AGE-EJOR article
         */
//        String csvLastvalues = ""; 

//        int dimensions = 20;
//        String[] algorithms = {"BFNW", "IBEA", "NSGAII", "SPEA2", "SMSEMOA"};
//        double[][][] allEpsValues = new double[4][dimensions][]; // 4* DTLZ x dimensions x algorithms

        TreeMap allData = new TreeMap();

        while (it.hasNext()) {

            if (debugPrint) System.out.println("-");
            String currentProblem = (String) it.next();

            csvEPS += currentProblem+": ";
            csvHYP += currentProblem+": ";

            TreeMap algsHashMap = (TreeMap)top.get(currentProblem);
//            HashMap algsHashMap = (HashMap)top.get(currentProblem);
//            ArrayList a = (ArrayList)top.get(currentProblem);

            Iterator itAlgs = algsHashMap.keySet().iterator();


            String csvEPSforThisProblem = "";
            String csvEPSforThisProblemwoHeaders = "";
            String csvHYPforThisProblem = "";
            String csvHYPforThisProblemwoHeaders = "";

//            String csvLastvaluesforThisProblem = "";

            while (itAlgs.hasNext()) {
                
                String currentAlg = (String) itAlgs.next();
                ArrayList algs = (ArrayList)algsHashMap.get(currentAlg);

//                if (printCSVIndicatorHeaders) {
                    csvEPS+=currentAlg+", ";
                    csvHYP+=currentAlg+", ";
//                }


                Object[] params = {"Real"};
                Problem problem = (new ProblemFactory()).getProblem(currentProblem, params);        // first argument: real problem name
                String pfFileName = pfFileName(problem, numberOfSampledPoints);

//                System.out.print("creating indicator");

                QualityIndicator indicators = new QualityIndicator(problem, pfFileName);    // second arguement: real PF file

//                System.out.println(" done");

                // read resulting solution set from file (*.fun)
                MetricsUtil utilities_ = new jmetal.qualityIndicator.util.MetricsUtil();


                //140320 for AGE EJOR: collect the results of each population so that Tobbi can run a statistical test later
                String csvCollectionOfAllPopulationsResults = "eps("+currentAlg+"),hyp("+currentAlg+")\n";
                

                Vector HYP = new Vector(algs.size());
                Vector EPSILON = new Vector(algs.size());

                for (int i = 0; i<algs.size(); i++) {
//                    System.out.println(i);
                    File f = (File)algs.get(i);
                    BufferedReader in = new BufferedReader(new FileReader(f));
                    String data = in.readLine();

//                    System.out.println(f.getName());

                    //it may happen that data==null, maybe because somebody is just rewriting the file
                    // in this case: skip it for now...
                    if (data==null) continue;

                    if (debugPrint) System.out.println(data);

                    /* now extract the important numbers
                     * two techniques:
                     * 1) extract from .log files, in case those have been properly written
                     * 2) calculate numbers based on the .FUN files
                     */
                    if (!true) {
                        int hypStart = data.indexOf("HYPpop:");
                        int followingComma = data.indexOf(",", hypStart);
                        HYP.add( Double.parseDouble(  data.substring( hypStart+7, followingComma) ) );
                        int epsStart = data.indexOf("EPSpop:");
                        followingComma = data.indexOf(",", epsStart);
                        EPSILON.add( Double.parseDouble( data.substring( epsStart+7, followingComma) ) );
                    } else {
//                        System.out.println("computing HYP and EPS");
                        String fFUN = f.getName();
                        fFUN = fFUN.replace("log", "FUN");
//                        System.out.println(subDir+"/"+fFUN);
                        SolutionSet experimentalResult = utilities_.readNonDominatedSolutionSet( subDir+"/"+fFUN, problem.getNumberOfObjectives());
//                        SolutionSet experimentalResult = utilities_.readNonDominatedSolutionSet( subDir+"/"+fFUN);
//                        SolutionSet experimentalResult = utilities_.readNonDominatedSolutionSet( subDir+"\\"+fFUN);
//                        System.out.println("HYP for popSize"+experimentalResult.size());


////                        Timer t = new Timer();
////                        double experimentalHypervolume = 0;
//                        long startTime = System.currentTimeMillis();
//
//                        double experimentalHypervolume = indicators.getHypervolume(experimentalResult);
//                        long t1 = System.currentTimeMillis() - startTime;
////                        long t1 = t.elapsed();t.restart();
//                        System.out.println(",");
//                        startTime = System.currentTimeMillis();
                        double experimentalHypervolume = indicators.getHypervolumeFPRAS(experimentalResult, true, hypervolumePoint);
//                        System.out.println(experimentalHypervolume);
                        if (Double.isNaN(experimentalHypervolume)) {
                            experimentalHypervolume=0;
                        }
//                        long t2 = System.currentTimeMillis() - startTime;
////                        long t2 = t.elapsed();
//                        System.out.println("hyp: old alg="+experimentalHypervolume+" ["+t1+"ms], fpras="+experimentalHypervolumeFPRAS+" ["+t2+"ms]");


//                        System.out.println("EPS...");
                        double experimentalApproximation = indicators.getEpsilon(experimentalResult);
                        HYP.add(experimentalHypervolume);
                        EPSILON.add(experimentalApproximation);
                        System.out.println(f.getName() + " hyp:"+experimentalHypervolume
                                + " eps:"+experimentalApproximation);
//                        System.out.println("computing HYP and EPS done");
                        
                        
                        
                        csvCollectionOfAllPopulationsResults += experimentalApproximation+","+experimentalHypervolume+"\n";
                    }

                    // now write csvCollectionOfAllPopulationsResults to disk, as all populations for a particula setup have been evaluated
                    String filename = currentAlg+"-"+currentProblem+".lastvalues.csv";
                    writeStringToFile(csvCollectionOfAllPopulationsResults,filename,currentDir);
                }

                // now do the calculations
                TreeMap<String, Double> statValues = new TreeMap<String, Double>();
//                HashMap<String, Double> statValues = new HashMap<String, Double>();
                statValues.put("mean", 0.0);
                statValues.put("median", 0.0);
                statValues.put("stdDeviation", 0.0);
                statValues.put("iqr", 0.0);
                statValues.put("max", 0.0);
                statValues.put("min", 0.0);
                Collections.sort(HYP);
                jmetal.experiments.Experiment.calculateStatistics(HYP, statValues);
                printStatistics(currentProblem+"-"+currentAlg, "HYP", statValues, algs.size());


              mean[getIndex(indicatorsA,"HV")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("mean");
              median[getIndex(indicatorsA,"HV")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("median");
              stdDeviation[getIndex(indicatorsA,"HV")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("stdDeviation");
              iqr[getIndex(indicatorsA,"HV")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("iqr");
              min[getIndex(indicatorsA,"HV")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("min");
              max[getIndex(indicatorsA,"HV")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("max");
              numberOfValues[getIndex(indicatorsA,"HV")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = HYP.size();

                csvHYPforThisProblem+=(Double)statValues.get(meanMedian)+",";
                csvHYPforThisProblemwoHeaders+=(Double)statValues.get(meanMedian)+",";

                addToMainDataStructure(allData, currentProblem, currentAlg, "HYP", (Double)statValues.get(meanMedian));

                statValues = new TreeMap<String, Double>();
                statValues.put("mean", 0.0);
                statValues.put("median", 0.0);
                statValues.put("stdDeviation", 0.0);
                statValues.put("iqr", 0.0);
                statValues.put("max", 0.0);
                statValues.put("min", 0.0);
                Collections.sort(EPSILON);
                jmetal.experiments.Experiment.calculateStatistics(EPSILON, statValues);
                printStatistics(currentProblem+"-"+currentAlg, "EPS", statValues, algs.size());


              mean[getIndex(indicatorsA,"EPSILON")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("mean");
              median[getIndex(indicatorsA,"EPSILON")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("median");
              stdDeviation[getIndex(indicatorsA,"EPSILON")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("stdDeviation");
              iqr[getIndex(indicatorsA,"EPSILON")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("iqr");
              min[getIndex(indicatorsA,"EPSILON")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("min");
              max[getIndex(indicatorsA,"EPSILON")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = statValues.get("max");
              numberOfValues[getIndex(indicatorsA,"EPSILON")][getIndex(problemsA, currentProblem)][getIndex(algorithmsA, currentAlg)] = EPSILON.size();


                csvEPSforThisProblem+=(Double)statValues.get(meanMedian)+",";
                csvEPSforThisProblemwoHeaders+=(Double)statValues.get(meanMedian)+",";

                addToMainDataStructure(allData, currentProblem, currentAlg, "EPS", (Double)statValues.get(meanMedian));
            }
            
            
            
            

//            if (printCSVIndicatorHeaders) {
                csvEPS += "\n"+csvEPSforThisProblem+"\n";
                csvHYP += "\n"+csvHYPforThisProblem+"\n";
//            } else {
                csvEPSwoHeaders += csvEPSforThisProblem+"\n";
                csvHYPwoHeaders += csvHYPforThisProblem+"\n";
//            }
        }

        System.out.println("\n\n"+csvEPS+"\n\n");
        System.out.println("\n\n"+csvHYP+"\n\n");
        System.out.println("\n\n"+csvEPSwoHeaders+"\n\n");
        System.out.println("\n\n"+csvHYPwoHeaders+"\n\n");

        generateCSVFiles(allData, currentDir);

        System.out.println("Information: EPS and HYP computed versus fronts with "+numberOfSampledPoints+ " sampled points");



        String latexDirectory_ = subDir;
        File latexOutput;
    latexOutput = new File(latexDirectory_);
    if (!latexOutput.exists()) {
      boolean result = new File(latexDirectory_).mkdirs();
      System.out.println("Creating " + latexDirectory_ + " directory");
    }
    System.out.println("Experiment name: " + exp.experimentName_);
    String latexFile = latexDirectory_ + "/" + exp.experimentName_ + ".tex";
    exp.printHeaderLatexCommands(latexFile);
    for (int i = 0; i < exp.indicatorList_.length; i++) {
      exp.printMeanStdDev(latexFile, i, mean, stdDeviation);
      exp.printMedianIQR(latexFile, i, median, iqr);
    } // for
    exp.printEndLatexCommands(latexFile);
    }

    
    public static void writeStringToFile(String text, String filename, File currentDir) {
        PrintWriter out;
        
        try {
            String completeFilename = currentDir.getCanonicalPath() + "/" + filename;
            out = new PrintWriter(completeFilename);
            out.println(text);
            out.close();
        } catch (Exception ex) {
            Logger.getLogger(MPIClusterEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    // return TRUE if completed
    public static boolean testCompletedCheck(File fileToCheck, File currentDirClusterout) {
        boolean debugPrint = !true;
        boolean result = false;
        
        try {
        
        // 1. remore file extension
        String identifier = fileToCheck.getName().substring(0, fileToCheck.getName().length()-4);
        
        // 2. get list of all files in 'clusterout'
        File[] currentListClusterout = currentDirClusterout.listFiles();
        
        if (debugPrint) { System.out.println("testCompletedCheck: '"+identifier+"' in "+currentListClusterout.length+" files"); }
        File fileFound = null;
        
        // 3. grep through all files for 'identifier'
        Grep g = null;
        for (File f:currentListClusterout) {
            if (debugPrint) { System.out.print(f.getName()+"?"); }
            g = new Grep();
            g.setFile(f);
            List matches = g.grep(identifier);
            if (matches.size()!=0) {
                if (debugPrint) {
                    for(Object o:matches) {
                        System.out.print(":");
                        System.out.println((String)o);
                    }
                }
                if (debugPrint) { System.out.print("!"); }
                
                fileFound = f;
                break;
            }
        }
        if (debugPrint) { System.out.println(); }
        if (debugPrint) { System.out.println("Checking "+fileFound.getCanonicalPath()); }
        
        // 4. check fileFound for completion
        try{
        // Open the file that is the first 
        // command line parameter
            FileInputStream fstream = new FileInputStream(fileFound);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
            // Print the content on the console
//                System.out.println (strLine);
                if (strLine.contains("INFO: Total execution time:")) {
                    result = true;
                    break;
                }
            }
            //Close the input stream
            in.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        
        
        
        
        
        } catch (Exception ex) {
            Logger.getLogger(MPIClusterEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    
    
    
    
    
    // return TRUE if completed
    public static boolean testKilledCheck(File fileToCheck, File currentDirClusterout) {
        boolean debugPrint = !true;
        boolean result = false;
        
        try {
        
        // 1. remore file extension
        String identifier = fileToCheck.getName().substring(0, fileToCheck.getName().length()-4);
        
        // 2. get list of all files in 'clusterout'
        File[] currentListClusterout = currentDirClusterout.listFiles();
        
        if (debugPrint) { System.out.println("testCompletedCheck: '"+identifier+"' in "+currentListClusterout.length+" files"); }
        File fileFound = null;
        
        // 3. grep through all files for 'identifier'
        Grep g = null;
        for (File f:currentListClusterout) {
            if (debugPrint) { System.out.print(f.getName()+"?"); }
            g = new Grep();
            g.setFile(f);
            List matches = g.grep(identifier);
            if (matches.size()!=0) {
                if (debugPrint) {
                    for(Object o:matches) {
                        System.out.print(":");
                        System.out.println((String)o);
                    }
                }
                if (debugPrint) { System.out.print("!"); }
                
                fileFound = f;
                break;
            }
        }
        if (debugPrint) { System.out.println(); }
        if (debugPrint) { System.out.println("Checking "+fileFound.getCanonicalPath()); }
        
        // 4. check fileFound for completion
        try{
        // Open the file that is the first 
        // command line parameter
            FileInputStream fstream = new FileInputStream(fileFound);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
            // Print the content on the console
//                System.out.println (strLine);
                if (strLine.contains("Killed")) {
                    result = true;
                    break;
                }
            }
            //Close the input stream
            in.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        
        
        
        
        
        } catch (Exception ex) {
            Logger.getLogger(MPIClusterEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    
    
    
    
    public static void printStatistics(String combo, String indicator, TreeMap map, int reps) {
        System.out.print(" "+combo + " ("+indicator+"): ");
        if (!true) {
            System.out.print( "mean:"+(Double)map.get("mean")
                    + " stdev:"+(Double)map.get("stdDeviation")
                    + " median:"+(Double)map.get("median")
                    + " iqr:"+(Double)map.get("iqr")
                    + " [reps:"+reps+"]"
                    );
        } else {
            System.out.printf("mean:%8.5f stdev:%8.5f median:%8.5f iqr:%8.5f", (Double)map.get("mean"), (Double)map.get("stdDeviation"), (Double)map.get("median"), (Double)map.get("iqr"));
            System.out.print(" [reps:"+reps+"]");
//            System.out.printf("-deltaForThisSolution: %8.5f ", deltaForThisSolution);
        }
        System.out.println("");
    }
    
    
    
    
    
    

    public static String pfFileName(Problem problem, int numberOfSampledPoints) {
        String result = "";
        String problemName = problem.getName();

        if (problemName.startsWith("DTLZ")) {
            String front = "";
            if (problemName.startsWith("DTLZ1")) front = "DTLZ1";
            else if (problemName.startsWith("DTLZ2") || problemName.startsWith("DTLZ3") ||
                    problemName.startsWith("DTLZ4")) front = "DTLZ2";

            result = "originalParetoFronts/" + front + "_" + problem.getNumberOfObjectives() + "D"
//            result = "originalParetoFronts\\" + front + "_" + problem.getNumberOfObjectives() + "D"
                    + "." + numberOfSampledPoints + ".pf";
    //        result = "..\\originalParetoFronts\\" + currentProblem + "." + numberOfSampledPoints + ".pf";
            result = result.replace("_", ".");
        } else if (problem.getName().startsWith("WFG")) {
            result = "originalParetoFronts/" + problemName + "." + problem.getNumberOfObjectives() + "D" + ".pf";
//            result = "originalParetoFronts\\" + problemName + "." + problem.getNumberOfObjectives() + "D" + ".pf";
        } else {
            result = "originalParetoFronts/" + problemName + ".pf";
//            result = "originalParetoFronts\\" + problemName + ".pf";
        }

        return result;      // e.g. "DTLZ1.8D.1000000.pf"
    }

    
    
    
    

    public static void addToMainDataStructure(
            TreeMap allData,
            String currentProblem,
            String currentAlg,
            String indicator, // HYP, EPS
            double value) {

        String currentProblemMainClass = "";
        String dimension = "";
        
        if (currentProblem.startsWith("ZDT")) {
            currentProblemMainClass = currentProblem.substring(0, 4);
            dimension = "2";
        } else {
            currentProblemMainClass = currentProblem.substring(0, currentProblem.indexOf("_"));
            dimension = currentProblem.substring(currentProblem.indexOf("_")+1, currentProblem.length()-1);
        }
//        int dimension = Integer.parseInt(currentProblem.substring(currentProblem.indexOf("_")+1, currentProblem.length()-1));
        

        boolean debugPrint = !true;

        if (allData.containsKey(currentProblemMainClass)) {

            if (debugPrint) System.out.println(" /"+currentProblemMainClass);

            TreeMap dimensions = (TreeMap)allData.get(currentProblemMainClass);
            if (dimensions.containsKey(dimension)) {

                if (debugPrint) System.out.println("  /"+dimension);

                TreeMap algs = (TreeMap)dimensions.get(dimension);
                if (algs.containsKey(currentAlg)) {

                    if (debugPrint) System.out.println("   /"+currentAlg);

                    TreeMap indicators = (TreeMap)algs.get(currentAlg);
                    if (indicators.containsKey(indicator)) {
                        if (debugPrint) System.out.println("    /"+indicator);
                        System.out.println("error, indicator value already there");
                    } else {
                        // indicator value missing
                        if (debugPrint) System.out.println("    +"+indicator);
                        indicators.put(indicator, value);
                    }
                } else {

                    if (debugPrint) System.out.println("   +"+currentAlg);

                    // algorithm missing
                    TreeMap indicators = new TreeMap();
                    indicators.put(indicator, value);

                    algs.put(currentAlg, indicators);
                }
            } else {

                if (debugPrint) System.out.println("  +"+dimension);

                // dimension missing
                TreeMap indicators = new TreeMap();
                indicators.put(indicator, value);
                TreeMap algs = new TreeMap();
                algs.put(currentAlg, indicators);

                dimensions.put(dimension, algs);
            }
        } else {

            if (debugPrint) System.out.println(" +"+currentProblemMainClass);

            // currentProblemMainClass missing
            TreeMap indicators = new TreeMap();
            indicators.put(indicator, value);
            TreeMap algs = new TreeMap();
            algs.put(currentAlg, indicators);
            TreeMap dimensions = new TreeMap();
            dimensions.put(dimension, algs);

            allData.put(currentProblemMainClass, dimensions);
        }
    }


    public static void generateCSVFiles(TreeMap allData, File currentDir) {
//        Set allDataSet = allData.keySet();
        Iterator itProblemClasses = allData.keySet().iterator();
//        for (int i=allDataSet.) {
        while (itProblemClasses.hasNext()) {
            String currentProblemMainClassKey = (String)itProblemClasses.next();
            TreeMap currentProblemMainClass = (TreeMap)allData.get(currentProblemMainClassKey);
            Iterator itDimensions = currentProblemMainClass.keySet().iterator();

            //for this class make a new file
            String baseFileName;
            try {
                baseFileName = currentDir.getCanonicalPath() + "/" + currentProblemMainClassKey;
                File currentProblemMainClassFileEPS = new File(baseFileName+"eps.csv");
                File currentProblemMainClassFileHYP = new File(baseFileName+"hyp.csv");

                String EPS = "";
                String HYP = "";

                // assume the following algorithms
                String algorithmsEPS = "eps(BFNW) , eps(IBEA) , eps(NSGAII) , eps(SMSEMOA) , eps(SPEA2), eps(AGE2), eps(AGE3), eps(SteadyStateMOCMA), eps(MOCMA), eps(NSGAIII) , eps(NSGAIIIAlternate) , eps(PAES), eps(SMPSO)";
                String algorithmsHYP = "hyp(BFNW) , hyp(IBEA) , hyp(NSGAII) , hyp(SMSEMOA) , hyp(SPEA2), hyp(AGE2), hyp(AGE3), hyp(SteadyStateMOCMA), hyp(MOCMA), hyp(NSGAIII) , hyp(NSGAIIIAlternate) , hyp(PAES), hyp(SMPSO)";
//                String[] algorithmsA = { "BFNW", "IBEA", "NSGAII", "SMSEMOA", "SPEA2" };

                EPS += "d , "+algorithmsEPS + "\n";
                HYP += "d , "+algorithmsHYP + "\n";


                TreeMap algs = null;
                int currentDim = 0;
                while (algs == null) {
                    currentDim++;
                    algs = (TreeMap)currentProblemMainClass.get(String.valueOf(currentDim));
                }
                
//                for (int d = 2; d<=20; d+=2) {
                while (algs!=null) {
                    String dimensionKey = String.valueOf(currentDim);
//                    String dimensionKey = (String)itDimensions.next();
//                    TreeMap algs = (TreeMap)currentProblemMainClass.get(dimensionKey);



                    double[] currentEPS = new double[algorithmsA.length];
                    double[] currentHYP = new double[algorithmsA.length];

                    
                    Iterator itAlgs = algs.keySet().iterator();

                    while (itAlgs.hasNext()) {
                        String algsKey = (String)itAlgs.next();
                        TreeMap indicators = (TreeMap)algs.get(algsKey);
                        Iterator itIndicators = indicators.keySet().iterator();

                        int currentIndex = getIndex(algorithmsA, algsKey);
                        if (currentIndex==Integer.MAX_VALUE) {
                            System.out.println("PROBLEM, alg not found in list");
                        } else {

                            while (itIndicators.hasNext()) {
                                String indicator = (String)itIndicators.next();
                                if (indicator.equalsIgnoreCase("EPS")) currentEPS[currentIndex] = (Double)indicators.get("EPS");
                                if (indicator.equalsIgnoreCase("HYP")) currentHYP[currentIndex] = (Double)indicators.get("HYP");

                            }

                        }
                    }

                    EPS += dimensionKey + " , ";
                    HYP += dimensionKey + " , ";
                    for (int i = 0; i<algorithmsA.length-1; i++) {
                        EPS += currentEPS[i] + " , ";
                        HYP += currentHYP[i] + " , ";
                    }
                    EPS += currentEPS[algorithmsA.length-1] + "\n";
                    HYP += currentHYP[algorithmsA.length-1] + "\n";



                    // eine dimension abgearbeitet, finde die naechste...
                    algs = null;
                    while (algs == null) {
                        currentDim++;
                        algs = (TreeMap)currentProblemMainClass.get(String.valueOf(currentDim));
                        if (currentDim >=21) {
                            break;
                        }
                    }
                    
                }



                BufferedWriter out = new BufferedWriter(new FileWriter(currentProblemMainClassFileEPS, false));  // use false to NOT APPEND
                out.write(EPS);
                out.close();
                System.out.println(currentProblemMainClassFileEPS.getCanonicalPath() + " written");
                               out = new BufferedWriter(new FileWriter(currentProblemMainClassFileHYP, false));  // use false to NOT APPEND
                out.write(HYP);
                out.close();
                System.out.println(currentProblemMainClassFileHYP.getCanonicalPath() + " written");



            } catch (IOException ex) {
                Logger.getLogger(MPIClusterEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            }


        }
    }

    public static int getIndex(String[] a, String c) {
        int result = Integer.MAX_VALUE;
        
        if (c.startsWith("ZDT")) c = c.substring(0,4);
        
        for (int i=0; i<a.length; i++) {
            if (a[i].equalsIgnoreCase(c)) {
                result = i;
                break;
            }
        }
        return result;
    }
}




class Timer {
    private long start = 0;
    public void Timer() { 
        start = System.currentTimeMillis();
    }
    public void restart() { 
        start = System.currentTimeMillis();
    }
    public long elapsed() {
        long end = System.currentTimeMillis();
        return (end - start);
    }
//    public void Timer() { start = System.nanoTime(); }
//    public void restart() { start = System.nanoTime(); }
//    public int elapsed() {
//        long end = System.nanoTime();
//        return (int)((end - start)/1000000d);
//    }
}
