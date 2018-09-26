/**
 * NSGAII.java
 * @author Juan J. Durillo
 * @version 1.0  
 */
package jmetal.metaheuristics.nsgaII;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.*;
import jmetal.metaheuristics.bfnw.BFNW;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.*;

/**
 * This class implements the NSGA-II algorithm. 
 */
public class NSGAII extends Algorithm {

  /**
   * stores the problem  to solve
   */
  private Problem problem_;

  /**
   * Constructor
   * @param problem Problem to solve
   */
  public NSGAII(Problem problem) {
    this.problem_ = problem;
  } // NSGAII

  /**   
   * Runs the NSGA-II algorithm.
   * @return a <code>SolutionSet</code> that is a set of non dominated solutions
   * as a result of the algorithm execution
   * @throws JMException 
   */
  public SolutionSet execute() throws JMException, ClassNotFoundException {
    int populationSize;
    int maxEvaluations;
    int evaluations;

    QualityIndicator indicators; // QualityIndicator object
    int requiredEvaluations; // Use in the example of use of the
    // indicators object (see below)

    SolutionSet population;
    SolutionSet offspringPopulation;
    SolutionSet union = null;

    Operator mutationOperator;
    Operator crossoverOperator;
    Operator selectionOperator;

    Distance distance = new Distance();

    //Read the parameters
    populationSize = ((Integer) getInputParameter("populationSize")).intValue();
    maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
    indicators = (QualityIndicator) getInputParameter("indicators");

    boolean doCrossover = ((Boolean)getInputParameter("doCrossover")).booleanValue();
    boolean doMutation = ((Boolean)getInputParameter("doMutation")).booleanValue();
    int infoPrinterHowOften;
    if (getInputParameter("infoPrinterHowOften")==null) infoPrinterHowOften=1000;
      else infoPrinterHowOften = ((Integer)getInputParameter("infoPrinterHowOften")).intValue();
    boolean doOnMPICluster;
    if (getInputParameter("doOnMPICluster")==null) doOnMPICluster = false;
        else doOnMPICluster = ((Boolean) getInputParameter("doOnMPICluster")).booleanValue();
    String infoPrinterSubDir = (String)getInputParameter("infoPrinterSubDir");


    //Initialize the variables
    population = new SolutionSet(populationSize);
    evaluations = 0;

    requiredEvaluations = 0;

    //Read the operators
    mutationOperator = operators_.get("mutation");
    crossoverOperator = operators_.get("crossover");
    selectionOperator = operators_.get("selection");

        
        
        
        
        
        
        boolean seedingPopulation = !true;    //entails: seeding the archive through Deepcopy       // seed SETUP #################################################
        boolean seedingArchive = !true;
        boolean seedingTargetArchive = !true;
        String seedFilenameProblem = problem_.getName() + "_" +problem_.getNumberOfObjectives()+"D";
        if (problem_.getName().startsWith("LZ")) seedFilenameProblem = problem_.getName();
        if (problem_.getName().startsWith("ZDT")) seedFilenameProblem = problem_.getName();
//        String seedFilename = "seeds\\"+seedFilenameProblem+".linearCombos1kEvalsEach.pop";
        String seedFilename = "seeds\\"+seedFilenameProblem+".cornersAndCentre.pop";
//        String seedFilename = "seeds\\"+seedFilenameProblem+".linearCombos.pop";
        System.out.println("seedFilename="+seedFilename+
                " seedingPopulation="+seedingPopulation+" seedingArchive="+seedingArchive+" seedingTargetArchive="+seedingTargetArchive);
        SolutionSet seeds = new SolutionSet(populationSize);
        if (seedingPopulation) {
            MetricsUtil util = new MetricsUtil();
            seeds = util.readSolutionSet(seedFilename, problem_);
        }
        
        
        
        
        
        
        
        
        
    // Create the initial solutionSet
    Solution newSolution;
    for (int i = 0; i < populationSize; i++) {
      newSolution = new Solution(problem_);
      
//            if (seedingPopulation && i<100) newSolution = seeds.get(i);                  // seed injection POPULATION #################################################
            if (seedingPopulation && i<problem_.getNumberOfObjectives()+1) newSolution = seeds.get(i);                  // seed injection POPULATION #################################################
            
      problem_.evaluate(newSolution);
      problem_.evaluateConstraints(newSolution);
      evaluations++;
      population.add(newSolution);
    } //for       

    
    System.out.println("initial: population.size()="+population.size());
        System.out.println("initial: eps(population)="+indicators.getEpsilon(population));
    

        
        boolean analysisWithTime = true;
        String ifile = indicators.paretoFrontFile;
        QualityIndicator indicatorsTemp = (analysisWithTime?new QualityIndicator(problem_,ifile.replace("1000.", "100000.")):null);
        
        
        
    // Generations ...
    while (evaluations <= maxEvaluations) {


        
        
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
            if (union == null) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, false);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, false);
            }
        } else {
            if (union == null) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, false, false);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, false, false);
            }
        }
        if (evaluations>=maxEvaluations) {
            if (doOnMPICluster) {
                if (union == null) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
                } else {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, true);
                }
            }
            break;
        }

        

      // Create the offSpring solutionSet      
      offspringPopulation = new SolutionSet(populationSize);
      Solution[] parents = new Solution[2];
      for (int i = 0; i < (populationSize / 2); i++) {
        if (evaluations < maxEvaluations) {
          //obtain parents
          parents[0] = (Solution) selectionOperator.execute(population);
          parents[1] = (Solution) selectionOperator.execute(population);
          Solution[] offSpring;
          if (doCrossover) offSpring = (Solution[]) crossoverOperator.execute(parents);
            else offSpring = parents;
          if (doMutation) mutationOperator.execute(offSpring[0]);
          if (doMutation) mutationOperator.execute(offSpring[1]);
          problem_.evaluate(offSpring[0]);
          problem_.evaluateConstraints(offSpring[0]);
          problem_.evaluate(offSpring[1]);
          problem_.evaluateConstraints(offSpring[1]);
          offspringPopulation.add(offSpring[0]);
          offspringPopulation.add(offSpring[1]);
          evaluations += 2;
        } // if                            
      } // for


      // Create the solutionSet union of solutionSet and offSpring
      union = ((SolutionSet) population).union(offspringPopulation);

      // Ranking the union
      Ranking ranking = new Ranking(union);

      int remain = populationSize;
      int index = 0;
      SolutionSet front = null;
      population.clear();

      // Obtain the next front
      front = ranking.getSubfront(index);

      while ((remain > 0) && (remain >= front.size())) {
        //Assign crowding distance to individuals
        distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives(), crowdingDistanceSwitch, null);
        //Add the individuals of this front
        for (int k = 0; k < front.size(); k++) {
          population.add(front.get(k));
        } // for

        //Decrement remain
        remain = remain - front.size();

        //Obtain the next front
        index++;
        if (remain > 0) {
          front = ranking.getSubfront(index);
        } // if        
      } // while

      // Remain is less than front(index).size, insert only the best one
      if (remain > 0) {  // front contains individuals to insert                        
        distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives(), crowdingDistanceSwitch, null);
        front.sort(new jmetal.base.operator.comparator.CrowdingComparator());
        for (int k = 0; k < remain; k++) {
          population.add(front.get(k));
        } // for

        remain = 0;
      } // if                               

      // This piece of code shows how to use the indicator object into the code
      // of NSGA-II. In particular, it finds the number of evaluations required
      // by the algorithm to obtain a Pareto front with a hypervolume higher
      // than the hypervolume of the true Pareto front.
      if ((indicators != null) &&
        (requiredEvaluations == 0)) {
        double HV = indicators.getHypervolume(population);
        if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
          requiredEvaluations = evaluations;
        } // if
      } // if



//     if (!doOnMPICluster)
//      if (evaluations % infoPrinterHowOften == 0) {
//        infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true);
//      }

      
      
      
      if (!true && evaluations % 1 == 0) {
          
          System.out.println("union eps="+indicators.getEpsilon(union)+":");
          
          Ranking r = new Ranking(union);
          System.out.println("subfronts="+r.getNumberOfSubfronts());
          for (int i=0; i<r.getNumberOfSubfronts(); i++) {
              System.out.println("-front"+i);
              SolutionSet s = r.getSubfront(i);
              for (int k=0; k<s.size(); k++) {
                  Solution c = s.get(k);
                  for (int j=0; j<problem_.getNumberOfObjectives(); j++) {
                      System.out.printf("%6.1f ",c.getObjective(j));
                  }
                  System.out.printf("[cd=%4.2f]",c.getCrowdingDistance());
                  System.out.println();
              }
          }
          
          
          System.out.println("population eps="+indicators.getEpsilon(population)+":");
          for (int i=0; i<population.size(); i++) {
              Solution c = population.get(i);
              for (int j=0; j<problem_.getNumberOfObjectives(); j++) {
                  System.out.printf("%5.1f ",c.getObjective(j));
              }
              System.out.printf("[cd=%4.2f]",c.getCrowdingDistance());
              System.out.println();
          }
      }
      

    } // while

    // Return as output parameter the required evaluations
    setOutputParameter("evaluations", requiredEvaluations);


    if (doOnMPICluster) {
        if (union == null) {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
        } else {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, true);
        }
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
  } // execute
  
  
  
  
  int crowdingDistanceSwitch = 2; // the archive versions do not work here


  public static void main(String[] args) {
        try {
            if (args.length==0) {
//            NSGAII_main.main(new String[]{"DTLZ1", "originalParetoFronts\\DTLZ1.2D.pf"});
//            NSGAII_main.main(new String[]{"Water", "originalParetoFronts\\DTLZ1.2D.pf"});
//            NSGAII_main.main(new String[]{"ZDT4","originalParetoFronts\\ZDT4.pf","10","true","true"});
//            NSGAII_main.main(new String[]{"DTLZ7", "nopf", "10", "true", "true","","true"});
//            NSGAII_main.main(new String[]{"Water", "originalParetoFronts\\Water.pf", "10", "true", "true","","true"});
//              BFWN_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection"});
//            NSGAII_main.main(new String[]{"LZ09_F5", "originalParetoFronts\\LZ09_F5.pf", "25", "true", "true", "0.01", "false","1000000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            NSGAII_main.main(new String[]{"DTLZ4_4D", "originalParetoFronts\\DTLZ2.4D.1000.pf", "100", "true", "true", "0.01", "false","1000000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
            NSGAII_main.main(new String[]{"ZDT4", "originalParetoFronts\\ZDT4.pf", "100", "true", "true", "0.01", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            NSGAII_main.main(new String[]{"DTLZ1", "originalParetoFronts\\DTLZ1.3D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection",""});
//            NSGAII_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection",""});
//            NSGAII_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf"});
            } else if (args.length==7) {
                // in case it is called via the commandline with parameters...
                NSGAII_main.main(new String[]{
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
            Logger.getLogger(NSGAII.class.getName()).log(Level.SEVERE, null, ex);
        }
  }

} // NSGA-II
