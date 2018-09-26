/**
 * IBEA.java
 *
 *
 * @author Juan J. Durillo
 * @version 1.0
 */

package jmetal.metaheuristics.ibea;

import java.util.ArrayList;
import jmetal.base.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.operator.comparator.DominanceComparator;
import jmetal.base.operator.comparator.FitnessComparator;
import jmetal.metaheuristics.bfnw.BFNW;
import jmetal.qualityIndicator.Epsilon;
//import jmetal.qualityIndicator.Hypervolume;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.*;

/**
 * This class representing the SPEA2 algorithm
 */
public class IBEA extends Algorithm{

  /**
   * Defines the number of tournaments for creating the mating pool
   */
  public static final int TOURNAMENTS_ROUNDS = 1;

  /**
   * Stores the problem to solve
   */
  private Problem problem_;

  /**
   * Stores the value of the indicator between each pair of solutions into
   * the solution set
   */
  private List<List<Double>> indicatorValues_;

  /**
   *
   */
  private double maxIndicatorValue_;
  /**
  * Constructor.
  * Create a new IBEA instance
  * @param problem Problem to solve
  */
  public IBEA(Problem problem) {
    this.problem_ = problem;
  } // Spea2

  /**
   * calculates the hypervolume of that portion of the objective space that
   * is dominated by individual a but not by individual b
   */
  double calcHypervolumeIndicator(Solution p_ind_a,
                                  Solution p_ind_b,
                                  int d,
                                  double maximumValues [],
                                  double minimumValues []) {
    double a, b, r, max;
    double volume = 0;
    double rho = 2;

    r = rho * (maximumValues[d-1] - minimumValues[d-1]);
    max = minimumValues[d-1] + r;


    a = p_ind_a.getObjective(d-1);
    if (p_ind_b == null)
      b = max;
    else
      b = p_ind_b.getObjective(d-1);

    if (d == 1)
    {
      if (a < b)
        volume = (b - a) / r;
      else
        volume = 0;
    }
    else
    {
      if (a < b)
      {
         volume = calcHypervolumeIndicator(p_ind_a, null, d - 1, maximumValues, minimumValues) *
         (b - a) / r;
         volume += calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues) *
         (max - b) / r;
      }
      else
      {
         volume = calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues) *
         (max - a) / r;
      }
    }

    return (volume);
  }



    /**
   * This structure store the indicator values of each pair of elements
   */
  public void computeIndicatorValuesHD(SolutionSet solutionSet,
                                     double [] maximumValues,
                                     double [] minimumValues) {
    SolutionSet A, B;
    // Initialize the structures
    indicatorValues_ = new ArrayList<List<Double>>();
    maxIndicatorValue_ = - Double.MAX_VALUE;

    for (int j = 0; j < solutionSet.size(); j++) {
      A = new SolutionSet(1);
      A.add(solutionSet.get(j));

      List<Double> aux = new ArrayList<Double>();
      for (int i = 0; i < solutionSet.size(); i++) {
        B = new SolutionSet(1);
        B.add(solutionSet.get(i));

        int flag = (new DominanceComparator()).compare(A.get(0), B.get(0));

        double value = 0.0;
        if (flag == -1) {
            value = - calcHypervolumeIndicator(A.get(0), B.get(0), problem_.getNumberOfObjectives(), maximumValues, minimumValues);
        } else {
            value = calcHypervolumeIndicator(B.get(0), A.get(0), problem_.getNumberOfObjectives(), maximumValues, minimumValues);
        }
        //double value = epsilon.epsilon(matrixA,matrixB,problem_.getNumberOfObjectives());

        
        //Update the max value of the indicator
        if (Math.abs(value) > maxIndicatorValue_)
          maxIndicatorValue_ = Math.abs(value);
        aux.add(value);
     }
     indicatorValues_.add(aux);
   }
  } // computeIndicatorValues



  /**
   * Calculate the fitness for the individual at position pos
   */
  public void fitness(SolutionSet solutionSet,int pos) {
      double fitness = 0.0;
      double kappa   = 0.05;
    
      for (int i = 0; i < solutionSet.size(); i++) {
        if (i!=pos) {
           fitness += Math.exp((-1 * indicatorValues_.get(i).get(pos)/maxIndicatorValue_) / kappa);
        }
      }
      solutionSet.get(pos).setFitness(fitness);
  }


  /**
   * Calculate the fitness for the entire population.
  **/
  public void calculateFitness(SolutionSet solutionSet) {
    // Obtains the lower and upper bounds of the population
    double [] maximumValues = new double[problem_.getNumberOfObjectives()];
    double [] minimumValues = new double[problem_.getNumberOfObjectives()];

    for (int i = 0; i < problem_.getNumberOfObjectives();i++) {
        maximumValues[i] = - Double.MAX_VALUE; // i.e., the minus maxium value
        minimumValues[i] =   Double.MAX_VALUE; // i.e., the maximum value
    }

    for (int pos = 0; pos < solutionSet.size(); pos++) {
        for (int obj = 0; obj < problem_.getNumberOfObjectives(); obj++) {
          double value = solutionSet.get(pos).getObjective(obj);
          if (value > maximumValues[obj])
              maximumValues[obj] = value;
          if (value < minimumValues[obj])
              minimumValues[obj] = value;
        }
    }

    computeIndicatorValuesHD(solutionSet,maximumValues,minimumValues);
    for (int pos =0; pos < solutionSet.size(); pos++) {
        fitness(solutionSet,pos);
    }
  }



  /** 
   * Update the fitness before removing an individual
   */
  public void removeWorst(SolutionSet solutionSet) {
   
    // Find the worst;
    double worst      = solutionSet.get(0).getFitness();
    int    worstIndex = 0;
    double kappa = 0.05;
     
    for (int i = 1; i < solutionSet.size(); i++) {
      if (solutionSet.get(i).getFitness() > worst) {
        worst = solutionSet.get(i).getFitness();
        worstIndex = i;
      }
    }

    //if (worstIndex == -1) {
    //    System.out.println("Yes " + worst);
    //}
    //System.out.println("Solution Size "+solutionSet.size());
    //System.out.println(worstIndex);

    // Update the population
    for (int i = 0; i < solutionSet.size(); i++) {
      if (i!=worstIndex) {
          double fitness = solutionSet.get(i).getFitness();
          fitness -= Math.exp((- indicatorValues_.get(worstIndex).get(i)/maxIndicatorValue_) / kappa);
          solutionSet.get(i).setFitness(fitness);
      }
    }

    // remove worst from the indicatorValues list
    indicatorValues_.remove(worstIndex); // Remove its own list
    Iterator<List<Double>> it = indicatorValues_.iterator();
    while (it.hasNext())
        it.next().remove(worstIndex);

    // remove the worst individual from the population
    solutionSet.remove(worstIndex);
  } // removeWorst


  /**
  * Runs of the IBEA algorithm.
  * @return a <code>SolutionSet</code> that is a set of non dominated solutions
  * as a result of the algorithm execution
  * @throws JMException
  */
  public SolutionSet execute() throws JMException, ClassNotFoundException{

    QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators");
    boolean doCrossover = ((Boolean)getInputParameter("doCrossover")).booleanValue();
    boolean doMutation = ((Boolean)getInputParameter("doMutation")).booleanValue();
    int infoPrinterHowOften;
    if (getInputParameter("infoPrinterHowOften")==null) infoPrinterHowOften=1000;
      else infoPrinterHowOften = ((Integer)getInputParameter("infoPrinterHowOften")).intValue();
    boolean doOnMPICluster;
    if (getInputParameter("doOnMPICluster")==null) doOnMPICluster = false;
        else doOnMPICluster = ((Boolean) getInputParameter("doOnMPICluster")).booleanValue();
    String infoPrinterSubDir = (String)getInputParameter("infoPrinterSubDir");

    int populationSize, archiveSize, maxEvaluations, evaluations;
    Operator crossoverOperator, mutationOperator, selectionOperator;
    SolutionSet solutionSet, archive, offSpringSolutionSet;

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
      
//            if (seedingPopulation && i<100) newSolution = seeds.get(i);                          // seed injection POPULATION #################################################
            if (seedingPopulation && i<problem_.getNumberOfObjectives()+1) newSolution = seeds.get(i);                          // seed injection POPULATION #################################################
            
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
        
        
    while (evaluations <= maxEvaluations){

        
        
        
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
      
//   Ranking rtemp = new Ranking(union); //new 161131
//   union = rtemp.getSubfront(0); //new 161131
      
      calculateFitness(union);
      archive = union;
      
      
      while (archive.size() > populationSize) {
        removeWorst(archive);
      }
      // Create a new offspringPopulation
      offSpringSolutionSet= new SolutionSet(populationSize);
      Solution  [] parents = new Solution[2];
      while (offSpringSolutionSet.size() < populationSize){
        int j = 0;
        do{
          j++;
          parents[0] = (Solution)selectionOperator.execute(archive);
        } while (j < IBEA.TOURNAMENTS_ROUNDS); // do-while
        int k = 0;
        do{
          k++;
          parents[1] = (Solution)selectionOperator.execute(archive);
        } while (k < IBEA.TOURNAMENTS_ROUNDS); // do-while

        //make the crossover
        Solution [] offSpring;
        if (doCrossover) offSpring = (Solution [])crossoverOperator.execute(parents);            // do XO
          else offSpring = parents;                                                    // do not do XO
        if (doMutation) mutationOperator.execute(offSpring[0]);
        problem_.evaluate(offSpring[0]);
        problem_.evaluateConstraints(offSpring[0]);
        offSpringSolutionSet.add(offSpring[0]);
        evaluations++;
      } // while
      // End Create a offSpring solutionSet
      
//    Ranking rtemp2 = new Ranking(offSpringSolutionSet);
//    solutionSet = rtemp2.getSubfront(0);
      solutionSet = offSpringSolutionSet;




//      if (!doOnMPICluster)
//    if (evaluations % infoPrinterHowOften == 0) {
//        infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true);
////        BFNW.printLotsOfValues(evaluations, solutionSet, archive, indicators);
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



      public static void main(String[] args) {
        try {
            if (args.length==0) {
//            IBEA_main.main(new String[]{"DTLZ1","originalParetoFronts\\DTLZ1.2D.pf"});
//            IBEA_main.main(new String[]{"WFG2", "originalParetoFronts\\WFG2.2D.pf", "10", "true", "true", "0.001", "true","150000","RandomSelection",""});
//IBEA_main.main(new String[]{"LZ09_F1", "originalParetoFronts\\LZ09_F1.pf", "100", "true", "true", "0", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//IBEA_main.main(new String[]{"WFG4_2D", "originalParetoFronts\\WFG4.2D.pf", "100", "true", "true", "0", "false","100000","BinaryTournament2","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
IBEA_main.main(new String[]{"DTLZ1_3D", "originalParetoFronts\\DTLZ1.3D.10000.pf", "100", "true", "true", "0.01", "false","200000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//                IBEA_main.main(new String[]{"DTLZ3_8D", "originalParetoFronts\\DTLZ2.8D.100000.pf", "10", "true", "true", "0.001", "true","1500","BinaryTournament",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            IBEA_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","150000","RandomSelection",""});
//            IBEA_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf","100","true","true","","true"});
//            IBEA_main.main(new String[]{"DTLZ7","nopf","100","true","true","","true"});
//            IBEA_main.main(new String[]{"Water","nopf","100","true","true","","true"});
//            IBEA_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf"});
            } else if (args.length==7) {
                // in case it is called via the commandline with parameters...
                IBEA_main.main(new String[]{
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
            Logger.getLogger(IBEA.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
} // Spea2
