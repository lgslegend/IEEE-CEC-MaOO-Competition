/**
 * MOEAD.java
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.metaheuristics.moead;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import jmetal.base.*;
import jmetal.util.*;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.metaheuristics.bfnw.BFWN_main;
import jmetal.metaheuristics.ibea.IBEA;
import jmetal.metaheuristics.ibea.IBEA_main;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.PseudoRandom;

public class MOEAD extends Algorithm {


  private Problem problem_;
  /**
   * Population size
   */
  private int populationSize;
  /**
   * Stores the population
   */
  private SolutionSet population;
  /**
   * Z vector (ideal point)
   */
  double[] z_;
  /**
   * Lambda vectors
   */
  //Vector<Vector<Double>> lambda_ ; 
  double[][] lambda_;
  /**
   * T: neighbour size
   */
  int T_;
  /**
   * Neighborhood
   */
  int[][] neighborhood_;
  /**
   * delta: probability that parent solutions are selected from neighbourhood
   */
  double delta_;
  /**
   * nr: maximal number of solutions replaced by each child solution
   */
  int nr_;
  Solution[] indArray_;
  String functionType_;
  int evaluations;
  /**
   * Operators
   */
  Operator crossover_;
  Operator mutation_;

  String dataDirectory_;

  /** 
   * Constructor
   * @param problem Problem to solve
   */
  public MOEAD(Problem problem) {
    problem_ = problem;

    functionType_ = "_TCHE1";

  } // DMOEA

  public SolutionSet execute() throws JMException, ClassNotFoundException {
    int maxEvaluations;

    evaluations = 0;
    maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();
    populationSize = ((Integer) this.getInputParameter("populationSize")).intValue();
    dataDirectory_ = this.getInputParameter("dataDirectory").toString();

    population = new SolutionSet(populationSize);
    indArray_ = new Solution[problem_.getNumberOfObjectives()];

    
    
    
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
    
    
    
    
    
    T_ = 20;
    delta_ = 0.9;
    nr_ = 2;

    T_ = (int) (0.1 * populationSize);
    delta_ = 0.9;
    nr_ = (int) (0.01 * populationSize);

    neighborhood_ = new int[populationSize][T_];

    z_ = new double[problem_.getNumberOfObjectives()];
    //lambda_ = new Vector(problem_.getNumberOfObjectives()) ;
    lambda_ = new double[populationSize][problem_.getNumberOfObjectives()];

    crossover_ = operators_.get("crossover"); // default: DE crossover
    mutation_ = operators_.get("mutation");  // default: polynomial mutation

    // STEP 1. Initialization
    // STEP 1.1. Compute euclidean distances between weight vectors and find T
    initUniformWeight();

    initNeighborhood();

    // STEP 1.2. Initialize population
    initPopulation();

    // STEP 1.3. Initialize z_
    initIdealPoint();

    
    
    System.out.println("initial: population.size()="+population.size());
    System.out.println("initial: eps(population)="+indicators.getEpsilon(population));
    
    boolean analysisWithTime = true;
    String ifile = indicators.paretoFrontFile;
    QualityIndicator indicatorsTemp = (analysisWithTime?new QualityIndicator(problem_,ifile.replace("1000.", "100000.")):null);
        
    
    
    
    // STEP 2. Update
    while (evaluations < maxEvaluations) {
        
        
        System.out.println("testtttttttttttttttttttttttttttttttttttttttttttttttttttttttt");
        
        if (analysisWithTime) {
//        if (analysisWithTime && evaluations%5000==0) {
            int current10exp = (int)Math.floor(Math.log10(evaluations));
            int remainder = evaluations % (int)Math.pow(10, current10exp);
            if (remainder == 0 || (evaluations%5000==0&&evaluations<=100000))
                System.out.println(
                        "analysisWithTime:"+this.getClass().getSimpleName()+":"+problem_.getClass().getSimpleName()+" "+evaluations +
                        ",eps("+this.getClass().getSimpleName()+")="+indicatorsTemp.getEpsilon(population) +
                        ",hyp("+this.getClass().getSimpleName()+")="+indicatorsTemp.getHypervolume(population) + 
                        ",gd("+this.getClass().getSimpleName()+")="+indicatorsTemp.getGD(population)+
                        ",igd("+this.getClass().getSimpleName()+")="+indicatorsTemp.getIGD(population)+
                        ",spread("+this.getClass().getSimpleName()+")"+indicatorsTemp.getSpread(population)+
                        ",genspread("+this.getClass().getSimpleName()+")="+indicatorsTemp.getGeneralizedSpread(population)
                        );
            }
        
        
        
        
        
        
        if (infoPrinter==null) 
                if (doOnMPICluster) {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);
                } else {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir, infoPrinterHowOften, infoPrinterHowOften);
                }
        if (doOnMPICluster) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, false);
        } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, false, false);
        }
        if (evaluations>=maxEvaluations) {
            if (doOnMPICluster) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
            }
            break;
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
      int[] permutation = new int[populationSize];
      Utils.randomPermutation(permutation, populationSize);

      for (int i = 0; i < populationSize; i++) {
        //int n = permutation[i]; // or int n = i;
        int n = i ; // or int n = i;
        int type;
        double rnd = PseudoRandom.randDouble();

        // STEP 2.1. Mating selection based on probability
        if (rnd < delta_) // if (rnd < realb)    
        {
          type = 1;   // neighborhood
        } else {
          type = 2;   // whole population
        }
        Vector<Integer> p = new Vector<Integer>();
        matingSelection(p, n, 2, type);

        // STEP 2.2. Reproduction
        Solution child;
        Solution[] parents = new Solution[3];

        parents[0] = population.get(p.get(0));
        parents[1] = population.get(p.get(1));
        parents[2] = population.get(n);

        // Apply DE crossover 
        child = (Solution) crossover_.execute(new Object[]{population.get(n), parents});

        // Apply mutation
        mutation_.execute(child);

        // Evaluation
        problem_.evaluate(child);      
        
        evaluations++;

        // STEP 2.3. Repair. Not necessary

        // STEP 2.4. Update z_
        updateReference(child);

        // STEP 2.5. Update of solutions
        updateProblem(child, n, type);
      } // for 
    } 

    
    
    
    
    


    if (doOnMPICluster) {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
    }
    
    // Return the first non-dominated front
    Ranking ranking = new Ranking(population);
    population = ranking.getSubfront(0);
if (analysisWithTime && evaluations%5000==0 || evaluations==populationSize) {
//if (analysisWithTime && evaluations%5000==0) {
                System.out.println(
                        indicatorsTemp.getEpsilon(population) +
                        ","+indicatorsTemp.getHypervolume(population) + 
                        ","+indicatorsTemp.getGD(population)+
                        ","+indicatorsTemp.getIGD(population)+
                        ","+indicatorsTemp.getSpread(population)+
                        ","+indicatorsTemp.getGeneralizedSpread(population)
//                        "analysisWithTime:"+this.getClass().getSimpleName()+":"+problem_.getClass().getSimpleName()+" "+evaluations +
//                        ",eps("+this.getClass().getSimpleName()+")="+indicatorsTemp.getEpsilon(population) +
//                        ",hyp("+this.getClass().getSimpleName()+")="+indicatorsTemp.getHypervolume(population) + 
//                        ",gd("+this.getClass().getSimpleName()+")="+indicatorsTemp.getGD(population)+
//                        ",igd("+this.getClass().getSimpleName()+")="+indicatorsTemp.getIGD(population)+
//                        ",spread("+this.getClass().getSimpleName()+")"+indicatorsTemp.getSpread(population)+
//                        ",genspread("+this.getClass().getSimpleName()+")="+indicatorsTemp.getGeneralizedSpread(population)
                        );
            }
        return population;
    
    
  }

 
  /**
   * initUniformWeight
   */
  public void initUniformWeight() {
    if ((problem_.getNumberOfObjectives() == 2) && (populationSize < 300)) {
      for (int n = 0; n < populationSize; n++) {
        double a = 1.0 * n / (populationSize - 1);
        lambda_[n][0] = a;
        lambda_[n][1] = 1 - a;
      } // for
    } // if
    else {
      String dataFileName;
      dataFileName = "W" + problem_.getNumberOfObjectives() + "D_" +
        populationSize + ".dat";

   
      try {
        // Open the file
        FileInputStream fis = new FileInputStream(dataDirectory_ + "/" + dataFileName);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        int numberOfObjectives = 0;
        int i = 0;
        int j = 0;
        String aux = br.readLine();
        while (aux != null) {
          StringTokenizer st = new StringTokenizer(aux);
          j = 0;
          numberOfObjectives = st.countTokens();
          while (st.hasMoreTokens()) {
            double value = (new Double(st.nextToken())).doubleValue();
            lambda_[i][j] = value;
            //System.out.println("lambda["+i+","+j+"] = " + value) ;
            j++;
          }
          aux = br.readLine();
          i++;
        }
        br.close();
      } catch (Exception e) {
        System.out.println("initUniformWeight: failed when reading for file: " + dataDirectory_ + "/" + dataFileName);
        e.printStackTrace();
      }
    } // else

    //System.exit(0) ;
  } // initUniformWeight
  /**
   * 
   */
  public void initNeighborhood() {
    double[] x = new double[populationSize];
    int[] idx = new int[populationSize];

    for (int i = 0; i < populationSize; i++) {
      // calculate the distances based on weight vectors
      for (int j = 0; j < populationSize; j++) {
        x[j] = Utils.distVector(lambda_[i], lambda_[j]);
        //x[j] = dist_vector(population[i].namda,population[j].namda);
        idx[j] = j;
      //System.out.println("x["+j+"]: "+x[j]+ ". idx["+j+"]: "+idx[j]) ;
      } // for

      // find 'niche' nearest neighboring subproblems
      Utils.minFastSort(x, idx, populationSize, T_);
      //minfastsort(x,idx,population.size(),niche);

      for (int k = 0; k < T_; k++) {
        neighborhood_[i][k] = idx[k];
      //System.out.println("neg["+i+","+k+"]: "+ neighborhood_[i][k]) ;
      }
    } // for
  } // initNeighborhood

  /**
   * 
   */
  public void initPopulation() throws JMException, ClassNotFoundException {
    for (int i = 0; i < populationSize; i++) {
      Solution newSolution = new Solution(problem_);

      problem_.evaluate(newSolution);
      evaluations++;
      population.add(newSolution) ;
    } // for
  } // initPopulation

  /**
   * 
   */
  void initIdealPoint() throws JMException, ClassNotFoundException {
    for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
      z_[i] = 1.0e+30;
      indArray_[i] = new Solution(problem_);
      problem_.evaluate(indArray_[i]);
      evaluations++;
    } // for

    for (int i = 0; i < populationSize; i++) {
      updateReference(population.get(i));
    } // for
  } // initIdealPoint

  /**
   * 
   */
  public void matingSelection(Vector<Integer> list, int cid, int size, int type) {
    // list : the set of the indexes of selected mating parents
    // cid  : the id of current subproblem
    // size : the number of selected mating parents
    // type : 1 - neighborhood; otherwise - whole population
    int ss;
    int r;
    int p;

    ss = neighborhood_[cid].length;
    while (list.size() < size) {
      if (type == 1) {
        r = PseudoRandom.randInt(0, ss - 1);
        p = neighborhood_[cid][r];
      //p = population[cid].table[r];
      } else {
        p = PseudoRandom.randInt(0, populationSize - 1);
      }
      boolean flag = true;
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i) == p) // p is in the list
        {
          flag = false;
          break;
        }
      }

      //if (flag) list.push_back(p);
      if (flag) {
        list.addElement(p);
      }
    }
  } // matingSelection

  /**
   * 
   * @param individual
   */
  void updateReference(Solution individual) {
    for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
      if (individual.getObjective(n) < z_[n]) {
        z_[n] = individual.getObjective(n);

        indArray_[n] = individual;
      }
    }
  } // updateReference

  /**
   * @param individual
   * @param id
   * @param type
   */
  void updateProblem(Solution indiv, int id, int type) {
    // indiv: child solution
    // id:   the id of current subproblem
    // type: update solutions in - neighborhood (1) or whole population (otherwise)
    int size;
    int time;

    time = 0;

    if (type == 1) {
      size = neighborhood_[id].length;
    } else {
      size = population.size();
    }
    int[] perm = new int[size];

    Utils.randomPermutation(perm, size);

    for (int i = 0; i < size; i++) {
      int k;
      if (type == 1) {
        k = neighborhood_[id][perm[i]];
      } else {
        k = perm[i];      // calculate the values of objective function regarding the current subproblem
      }
      double f1, f2;

      f1 = fitnessFunction(population.get(k), lambda_[k]);
      f2 = fitnessFunction(indiv, lambda_[k]);

      if (f2 < f1) {
        population.replace(k, new Solution(indiv));
        //population[k].indiv = indiv;
        time++;
      }
      // the maximal number of solutions updated is not allowed to exceed 'limit'
      if (time >= nr_) {
        return;
      }
    }
  } // updateProblem

  double fitnessFunction(Solution individual, double[] lambda) {
    double fitness;
    fitness = 0.0;

    if (functionType_.equals("_TCHE1")) {
      double maxFun = -1.0e+30;

      for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
        double diff = Math.abs(individual.getObjective(n) - z_[n]);

        double feval;
        if (lambda[n] == 0) {
          feval = 0.0001 * diff;
        } else {
          feval = diff * lambda[n];
        }
        if (feval > maxFun) {
          maxFun = feval;
        }
      } // for

      fitness = maxFun;
    } // if
    else {
      System.out.println("MOEAD.fitnessFunction: unknown type " + functionType_);
      System.exit(-1);
    }
    return fitness;
  } // fitnessEvaluation
  
  
  
  public static void main(String[] args) {
        try {
            if (args.length==0) {
//            IBEA_main.main(new String[]{"DTLZ1","originalParetoFronts\\DTLZ1.2D.pf"});
//            IBEA_main.main(new String[]{"WFG2", "originalParetoFronts\\WFG2.2D.pf", "10", "true", "true", "0.001", "true","150000","RandomSelection",""});
//IBEA_main.main(new String[]{"LZ09_F1", "originalParetoFronts\\LZ09_F1.pf", "100", "true", "true", "0", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//IBEA_main.main(new String[]{"WFG4_2D", "originalParetoFronts\\WFG4.2D.pf", "100", "true", "true", "0", "false","100000","BinaryTournament2","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
MOEAD_main.main(new String[]{"DTLZ1_4D", "originalParetoFronts\\DTLZ1.4D.10000.pf", "100", "true", "true", "0.01", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//                IBEA_main.main(new String[]{"DTLZ3_8D", "originalParetoFronts\\DTLZ2.8D.100000.pf", "10", "true", "true", "0.001", "true","1500","BinaryTournament",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            IBEA_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","150000","RandomSelection",""});
//            IBEA_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf","100","true","true","","true"});
//            IBEA_main.main(new String[]{"DTLZ7","nopf","100","true","true","","true"});
//            IBEA_main.main(new String[]{"Water","nopf","100","true","true","","true"});
//            IBEA_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf"});
            } else if (args.length==7) {
                // in case it is called via the commandline with parameters...
                MOEAD_main.main(new String[]{
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
} // MOEAD

