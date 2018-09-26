/**
 * SPEA2.java
 * @author Juan J. Durillo
 * @version 1.0
 */

package jmetal.metaheuristics.spea2;

import jmetal.base.*;
import java.util.Comparator;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.*;

/** 
 * This class representing the SPEA2 algorithm
 */
public class SPEA2 extends Algorithm{
          
  /**
   * Defines the number of tournaments for creating the mating pool
   */
  public static final int TOURNAMENTS_ROUNDS = 1;

  /**
   * Stores the problem to solve
   */
  private Problem problem_;    

  /**
  * Constructor.
  * Create a new SPEA2 instance
  * @param problem Problem to solve
  */
  public SPEA2(Problem problem) {                
    this.problem_ = problem;        
  } // Spea2
   
  /**   
  * Runs of the Spea2 algorithm.
  * @return a <code>SolutionSet</code> that is a set of non dominated solutions
  * as a result of the algorithm execution  
   * @throws JMException 
  */  
  public SolutionSet execute() throws JMException, ClassNotFoundException {   
    int populationSize, archiveSize, maxEvaluations, evaluations;
    Operator crossoverOperator, mutationOperator, selectionOperator;
    SolutionSet solutionSet, archive, offSpringSolutionSet;    


    QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators"); // this line had to be added (mw)
    boolean doMutation = ((Boolean)getInputParameter("doMutation")).booleanValue();
    boolean doCrossover = ((Boolean)getInputParameter("doCrossover")).booleanValue();
    int infoPrinterHowOften;
    if (getInputParameter("infoPrinterHowOften")==null) infoPrinterHowOften=1000;
        else infoPrinterHowOften = ((Integer)getInputParameter("infoPrinterHowOften")).intValue();
    String infoPrinterSubDir = (String)getInputParameter("infoPrinterSubDir");
    boolean doOnMPICluster;
    if (getInputParameter("doOnMPICluster")==null) doOnMPICluster = false;
        else doOnMPICluster = ((Boolean) getInputParameter("doOnMPICluster")).booleanValue();


    //Read the params
    populationSize = ((Integer)getInputParameter("populationSize")).intValue();
    archiveSize    = ((Integer)getInputParameter("archiveSize")).intValue();
    maxEvaluations = ((Integer)getInputParameter("maxEvaluations")).intValue();
        
    //Read the operators
    crossoverOperator = operators_.get("crossover");
    mutationOperator  = operators_.get("mutation");
    selectionOperator = operators_.get("selection");        
        
    //Initialize the variables
    solutionSet  = new SolutionSet(populationSize);
    archive     = new SolutionSet(archiveSize);
    evaluations = 0;
        
    
    
        
        
        
        
        
        
        boolean seedingPopulation = !true;    //entails: seeding the archive through Deepcopy       // seed SETUP #################################################
        boolean seedingArchive = !true;
        boolean seedingTargetArchive = !true;
        String seedFilenameProblem = problem_.getName() + "_" +problem_.getNumberOfObjectives()+"D";
        if (problem_.getName().startsWith("LZ")) seedFilenameProblem = problem_.getName();
        if (problem_.getName().startsWith("ZDT")) seedFilenameProblem = problem_.getName();
//        String seedFilename = "seeds\\"+seedFilenameProblem+".linearCombos1kEvalsEach.pop";
//        String seedFilename = "seeds\\"+seedFilenameProblem+".linearCombos.pop";
        String seedFilename = "seeds\\"+seedFilenameProblem+".cornersAndCentre.pop";
        System.out.println("seedFilename="+seedFilename+
                " seedingPopulation="+seedingPopulation+" seedingArchive="+seedingArchive+" seedingTargetArchive="+seedingTargetArchive);
        SolutionSet seeds = new SolutionSet(populationSize);
        if (seedingPopulation) {
            MetricsUtil util = new MetricsUtil();
            seeds = util.readSolutionSet(seedFilename, problem_);
        }
        
        
        
        
        
        
        
        
        
    
    
    
    //-> Create the initial solutionSet
    Solution newSolution;
    for (int i = 0; i < populationSize; i++) {
      newSolution = new Solution(problem_);
      
//            if (seedingPopulation && i<100) newSolution = seeds.get(i);               // seed injection POPULATION #################################################
            if (seedingPopulation && i<problem_.getNumberOfObjectives()+1) newSolution = seeds.get(i);               // seed injection POPULATION #################################################
            
            problem_.evaluate(newSolution);            
      problem_.evaluateConstraints(newSolution);
      evaluations++;
      solutionSet.add(newSolution);
    }                        
    
    System.out.println("initial: population.size()="+solutionSet.size());
        System.out.println("initial: eps(population)="+indicators.getEpsilon(solutionSet));
        
        
        
        boolean analysisWithTime = true;
        String ifile = indicators.paretoFrontFile;
        QualityIndicator indicatorsTemp = (analysisWithTime?new QualityIndicator(problem_,ifile.replace("1000.", "100000.")):null);
        
        
        
    while (evaluations <= maxEvaluations) {

        
        
        if (analysisWithTime) {
//        if (analysisWithTime && evaluations%5000==0) {
            int current10exp = (int)Math.floor(Math.log10(evaluations));
            int remainder = evaluations % (int)Math.pow(10, current10exp);
            if (remainder == 0 || (evaluations%5000==0&&evaluations<=100000))
                System.out.println(
                        "analysisWithTime:"+this.getClass().getSimpleName()+":"+problem_.getClass().getSimpleName()+" "+evaluations +
                        ",eps("+this.getClass().getSimpleName()+")="+indicatorsTemp.getEpsilon(solutionSet) +
                        ",hyp("+this.getClass().getSimpleName()+")="+indicatorsTemp.getHypervolume(solutionSet) + 
                        ",gd("+this.getClass().getSimpleName()+")="+indicatorsTemp.getGD(solutionSet)+
                        ",igd("+this.getClass().getSimpleName()+")="+indicatorsTemp.getIGD(solutionSet)+
                        ",spread("+this.getClass().getSimpleName()+")"+indicatorsTemp.getSpread(solutionSet)+
                        ",genspread("+this.getClass().getSimpleName()+")="+indicatorsTemp.getGeneralizedSpread(solutionSet)
                        );
            }
        
        
        

        if (infoPrinter==null) 
                if (doOnMPICluster) {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);
                } else {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir, infoPrinterHowOften, infoPrinterHowOften);
                }


        if (doOnMPICluster) {
            if (archive.size()==0) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, false);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
            }
        } else {
            if (archive.size()==0) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, false, false);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
            }
        }
        if (evaluations>=maxEvaluations) {
            if (doOnMPICluster) {
                if (archive.size()==0) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, true);
                } else {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
                }
            }
            break;
        }



      SolutionSet union = ((SolutionSet)solutionSet).union(archive);
      Spea2Fitness spea = new Spea2Fitness(union);
      spea.fitnessAssign();
      archive = spea.environmentalSelection(archiveSize);                       
      // Create a new offspringPopulation
      offSpringSolutionSet= new SolutionSet(populationSize);    
      Solution  [] parents = new Solution[2];
      while (offSpringSolutionSet.size() < populationSize){           
        int j = 0;
        do{
          j++;                
          parents[0] = (Solution)selectionOperator.execute(archive);
        } while (j < SPEA2.TOURNAMENTS_ROUNDS); // do-while                    
        int k = 0;
        do{
          k++;                
          parents[1] = (Solution)selectionOperator.execute(archive);
        } while (k < SPEA2.TOURNAMENTS_ROUNDS); // do-while
            
        //make the crossover 
        Solution [] offSpring = (Solution [])crossoverOperator.execute(parents);            
        mutationOperator.execute(offSpring[0]);            
        problem_.evaluate(offSpring[0]);
        problem_.evaluateConstraints(offSpring[0]);            
        offSpringSolutionSet.add(offSpring[0]);
        evaluations++;
      } // while
      // End Create a offSpring solutionSet
      solutionSet = offSpringSolutionSet;



//      if (!doOnMPICluster)
//      if (evaluations % infoPrinterHowOften == 0 && evaluations > populationSize) {
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true);
//      }
    } // while


    if (doOnMPICluster) {
        if (archive.size()==0) {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, solutionSet, indicators, true, true);
        } else {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
        }
    }


    Ranking ranking = new Ranking(archive);
    solutionSet = ranking.getSubfront(0);
if (analysisWithTime && evaluations%5000==0) {
                System.out.println(
                        "analysisWithTime:"+this.getClass().getSimpleName()+":"+problem_.getClass().getSimpleName()+" "+evaluations +
                        ",eps("+this.getClass().getSimpleName()+")="+indicatorsTemp.getEpsilon(solutionSet) +
                        ",hyp("+this.getClass().getSimpleName()+")="+indicatorsTemp.getHypervolume(solutionSet) + 
                        ",gd("+this.getClass().getSimpleName()+")="+indicatorsTemp.getGD(solutionSet)+
                        ",igd("+this.getClass().getSimpleName()+")="+indicatorsTemp.getIGD(solutionSet)+
                        ",spread("+this.getClass().getSimpleName()+")"+indicatorsTemp.getSpread(solutionSet)+
                        ",genspread("+this.getClass().getSimpleName()+")="+indicatorsTemp.getGeneralizedSpread(solutionSet)
                        );
            }
        return solutionSet;
  } // execute




  /** Minimal function to quickly execute the algorithm
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {

//            BFWN_main.main(new String[]{"DTLZ7","originalParetoFronts\\DTLZ7.3D.pf","10", "true", "true", "0.01"});
//            SPEA2_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","15000","RandomSelection",""});
              SPEA2_main.main(new String[]{"DTLZ1_10D", "originalParetoFronts\\DTLZ1.10D.10000.pf", "100", "true", "true", "0.01", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            SPEA2_main.main(new String[]{"DTLZ2_10D", "originalParetoFronts\\DTLZ2.10D.10000.pf", "100", "true", "true", "0.001", "false","20000","BinaryTournament",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            SPEA2_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"WFG3", "originalParetoFronts\\WFG3.2D.pf", "10", "true", "true", "0.01"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ7", "nopf", "10", "true", "true", "0.1", "true"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"Water", "originalParetoFronts\\Water.pf", "10", "true", "true", "0.001", "true"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"ZDT3", "originalParetoFronts\\ZDT3.pf", "10", "true", "true", "0.001"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf","50"});
//            BFWN_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf"});
            }

            else if (args.length==7) {
                // in case it is called via the commandline with parameters...
                SPEA2_main.main(new String[]{
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


} // Spea2
