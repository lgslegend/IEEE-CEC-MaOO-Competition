/**
 * SMSEMOA.java
 * @author Simon Wessing
 * @version 1.0  
 */
package jmetal.metaheuristics.smsemoa;

import java.util.Arrays;
import java.util.LinkedList;
import jmetal.base.*;
import jmetal.base.operator.comparator.CrowdingDistanceComparator;
import jmetal.qualityIndicator.Hypervolume;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.*;

/**
 * This class implements the SMS-EMOA algorithm, as described in
 *
 * Michael Emmerich, Nicola Beume, and Boris Naujoks.
 * An EMO algorithm using the hypervolume measure as selection criterion.
 * In C. A. Coello Coello et al., Eds., Proc. Evolutionary Multi-Criterion Optimization,
 * 3rd Int'l Conf. (EMO 2005), LNCS 3410, pp. 62-76. Springer, Berlin, 2005.
 *
 * and
 * 
 * Boris Naujoks, Nicola Beume, and Michael Emmerich.
 * Multi-objective optimisation using S-metric selection: Application to
 * three-dimensional solution spaces. In B. McKay et al., Eds., Proc. of the 2005
 * Congress on Evolutionary Computation (CEC 2005), Edinburgh, Band 2, pp. 1282-1289.
 * IEEE Press, Piscataway NJ, 2005.
 */
public class SMSEMOA extends Algorithm {

    /**
     * stores the problem  to solve
     */
    private Problem problem_;
    private MetricsUtil utils_;
    private Hypervolume hv_;

    /**
     * Constructor
     * @param problem Problem to solve
     */
    public SMSEMOA(Problem problem) {
        this.problem_ = problem;
        this.utils_ = new jmetal.qualityIndicator.util.MetricsUtil();
        this.hv_ = new Hypervolume();
    } // SMSEMOA

    /**
     * Runs the SMS-EMOA algorithm.
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize;
        int maxEvaluations;
        int evaluations;
        double offset = 100.0;



//        QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators"); // this line had to be added (mw)
        boolean doMutation = ((Boolean)getInputParameter("doMutation")).booleanValue();
        boolean doCrossover = ((Boolean)getInputParameter("doCrossover")).booleanValue();
        int infoPrinterHowOften;
        if (getInputParameter("infoPrinterHowOften")==null) infoPrinterHowOften=1000;
            else infoPrinterHowOften = ((Integer)getInputParameter("infoPrinterHowOften")).intValue();
        String infoPrinterSubDir = (String)getInputParameter("infoPrinterSubDir");
        boolean doOnMPICluster;
        if (getInputParameter("doOnMPICluster")==null) doOnMPICluster = false;
            else doOnMPICluster = ((Boolean) getInputParameter("doOnMPICluster")).booleanValue();


        QualityIndicator indicators; // QualityIndicator object
        int requiredEvaluations; // Use in the example of use of the indicators object (see below)

        SolutionSet population;
        SolutionSet offspringPopulation;
        SolutionSet union = null;

        Operator mutationOperator;
        Operator crossoverOperator;
        Operator selectionOperator;

        //Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
        indicators = (QualityIndicator) getInputParameter("indicators");
        offset = (Double) getInputParameter("offset");


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
        String seedFilename = "seeds\\"+seedFilenameProblem+".linearCombos1kEvalsEach.pop"; // from paper: 1k per seed
//        String seedFilename = "seeds\\"+seedFilenameProblem+".cornersAndCentre.pop";
//        String seedFilename = "seeds\\"+seedFilenameProblem+".linearCombos.pop"; //????????
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
            
//            if (seedingPopulation && i<100) newSolution = seeds.get(i);       // seed injection POPULATION #################################################
            if (seedingPopulation && i<problem_.getNumberOfObjectives()+1) newSolution = seeds.get(i);       // seed injection POPULATION #################################################
            
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
     // if (evaluations>1000)
            if (doOnMPICluster) {
                if (union==null) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, false);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, false);
                    } else {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, false);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, false);
                }
            } else {
                if (union==null) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, false, false);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, false, false);
                    } else {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, false, false);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, false, false);
                }
            }
            if (evaluations>=maxEvaluations) {
                if (doOnMPICluster) {
                    if (union==null) {
                        infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
                        infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, population, indicators, true, true);
                        } else {
                        infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, true);
                        infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, true);
                    }
                }
                break;
            }




            // select parents
            offspringPopulation = new SolutionSet(populationSize);
            LinkedList<Solution> selectedParents = new LinkedList<Solution>();
            Solution[] parents = new Solution[0];
            while (selectedParents.size() < 2) {
                Object selected = selectionOperator.execute(population);
                try {
                    Solution parent = (Solution) selected;
                    selectedParents.add(parent);
                } catch (ClassCastException e) {
                    parents = (Solution[]) selected;
                    for (Solution parent : parents) {
                        selectedParents.add(parent);
                    }
                }
            }
            parents = selectedParents.toArray(parents);

            // crossover
            Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);

            // mutation
            mutationOperator.execute(offSpring[0]);

            // evaluation
            problem_.evaluate(offSpring[0]);
            problem_.evaluateConstraints(offSpring[0]);

            // insert child into the offspring population
            offspringPopulation.add(offSpring[0]);

            evaluations++;

            // Create the solutionSet union of solutionSet and offSpring
            union = ((SolutionSet) population).union(offspringPopulation);

            // Ranking the union (non-dominated sorting)
            Ranking ranking = new Ranking(union);

            // ensure crowding distance values are up to date
            // (may be important for parent selection)
            for (int j = 0; j < population.size(); j++) {
                population.get(j).setCrowdingDistance(0.0);
            }

            SolutionSet lastFront = ranking.getSubfront(ranking.getNumberOfSubfronts() - 1);
            if (lastFront.size() > 1) {
                double[][] frontValues = lastFront.writeObjectivesToMatrix();
                int numberOfObjectives = problem_.getNumberOfObjectives();
                // STEP 1. Obtain the maximum and minimum values of the Pareto front
                double[] maximumValues = utils_.getMaximumValues(union.writeObjectivesToMatrix(), numberOfObjectives);
                double[] minimumValues = utils_.getMinimumValues(union.writeObjectivesToMatrix(), numberOfObjectives);
                // STEP 2. Get the normalized front
                double[][] normalizedFront = utils_.getNormalizedFront(frontValues, maximumValues, minimumValues);
                // compute offsets for reference point in normalized space
                double[] offsets = new double[maximumValues.length];
                for (int i = 0; i < maximumValues.length; i++) {
                    offsets[i] = offset / (maximumValues[i] - minimumValues[i]);
                }
                // STEP 3. Inverse the pareto front. This is needed because the original
                //metric by Zitzler is for maximization problems
                double[][] invertedFront = utils_.invertedFront(normalizedFront);
                // shift away from origin, so that boundary points also get a contribution > 0
                for (double[] point : invertedFront) {
                    for (int i = 0; i < point.length; i++) {
                        point[i] += offsets[i];
                    }
                }

                // calculate contributions and sort
                double[] contributions = hvContributions(invertedFront);
                for (int i = 0; i < contributions.length; i++) {
                    // contribution values are used analogously to crowding distance
                    lastFront.get(i).setCrowdingDistance(contributions[i]);
                }
                
//                if (evaluations%10001<=10) System.out.println(Arrays.toString(contributions));

                lastFront.sort(new CrowdingDistanceComparator());
            }

            // all but the worst are carried over to the survivor population
            SolutionSet front = null;
            population.clear();
            for (int i = 0; i < ranking.getNumberOfSubfronts() - 1; i++) {
                front = ranking.getSubfront(i);
                for (int j = 0; j < front.size(); j++) {
                    population.add(front.get(j));
                }
            }
            for (int i = 0; i < lastFront.size() - 1; i++) {
                population.add(lastFront.get(i));
            }

            if (!true) {
                System.out.println(indicators.getHypervolume(population)+ " "
                        + indicators.getHypervolumeFPRAS(population, false, Double.NEGATIVE_INFINITY));
            }

            // This piece of code shows how to use the indicator object into the code
            // of SMS-EMOA. In particular, it finds the number of evaluations required
            // by the algorithm to obtain a Pareto front with a hypervolume higher
            // than the hypervolume of the true Pareto front.
            if (indicators != null && requiredEvaluations == 0) {
                double HV = indicators.getHypervolume(population);
                if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
                    requiredEvaluations = evaluations;
                } // if
            } // if


//            if (!doOnMPICluster)
//            if (evaluations % infoPrinterHowOften == 0 && evaluations > populationSize) {
//                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, population, union, indicators, true, false);
//            }

        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);


        if (doOnMPICluster) {
            if (union==null) {
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
if (analysisWithTime && evaluations%5000==0) {
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
        return population;
    } // execute

    /**
     * Calculates how much hypervolume each point dominates exclusively. The points
     * have to be transformed beforehand, to accommodate the assumptions of Zitzler's
     * hypervolume code.
     * @param front transformed objective values
     * @return HV contributions
     */
    private double[] hvContributions(double[][] front) {
        int numberOfObjectives = problem_.getNumberOfObjectives();
        double[] contributions = new double[front.length];
        double[][] frontSubset = new double[front.length - 1][front[0].length];
        LinkedList<double[]> frontCopy = new LinkedList<double[]>();
        for (double[] point : front) {
            frontCopy.add(point);
        }
        double[][] totalFront = frontCopy.toArray(frontSubset);
//        double totalVolume = hv_.calculateHypervolumeFPRAS(totalFront, totalFront.length, numberOfObjectives, false);
        double totalVolume = hv_.calculateHypervolume(totalFront, totalFront.length, numberOfObjectives);
        for (int i = 0; i < front.length; i++) {
            double[] evaluatedPoint = frontCopy.remove(i);
            frontSubset = frontCopy.toArray(frontSubset);
            // STEP4. The hypervolume (control is passed to java version of Zitzler code)
//          double hv = hv_.calculateHypervolumeFPRAS(frontSubset, frontSubset.length, numberOfObjectives, false);
            double hv = hv_.calculateHypervolume(frontSubset, frontSubset.length, numberOfObjectives);
            double contribution = totalVolume - hv;
            if (contribution <0) contribution=0; // as approx hyp can be negative
            contributions[i] = contribution;
            // put point back
            frontCopy.add(i, evaluatedPoint);
        }
        return contributions;
    }



    /** Minimal function to quickly execute the algorithm
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
//java jmetal.metaheuristics.bfnw.BFNW DTLZ1_20D DTLZ1.20D.1000.pf 10 100000 BinaryTournament 0.01 foo
//            BFWN_main.main(new String[]{"DTLZ7","originalParetoFronts\\DTLZ7.3D.pf","10", "true", "true", "0.01"});
//            SMSEMOA_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection"});   //problem, Pareto front, pop size, doCrossover, doMutation
//        SMSEMOA_main.main(new String[]{"DTLZ1_20D", "originalParetoFronts\\DTLZ1.20D.1000.pf", "10", "true", "true", "0.01", "true","50000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//        SMSEMOA_main.main(new String[]{"LZ09_F5", "originalParetoFronts\\LZ09_F5.pf", "100", "true", "true", "0.01", "false","150000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
SMSEMOA_main.main(new String[]{"DTLZ1_2D", "originalParetoFronts\\DTLZ1.2D.10000.pf", "100", "true", "true", "0.01", "false","200000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation        
//SMSEMOA_main.main(new String[]{"DTLZ1_2D", "originalParetoFronts\\DTLZ1.2D.10000.pf", "100", "true", "true", "0.01", "false","10000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//         SMSEMOA_main.main(new String[]{"DTLZ3_10D", "originalParetoFronts\\DTLZ2.10D.100000.pf", "10", "true", "true", "0.001", "true","1500","BinaryTournament",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            SMSEMOA_main.main(new String[]{"WFG2", "originalParetoFronts\\WFG2.2D.pf", "10", "true", "true", "0.001", "true","150000","RandomSelection",""});
//            SMSEMOA_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","1500","RandomSelection",""});
//            BFWN_main.main(new String[]{"WFG3", "originalParetoFronts\\WFG3.2D.pf", "10", "true", "true", "0.01"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ7", "nopf", "10", "true", "true", "0.1", "true"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"Water", "originalParetoFronts\\Water.pf", "10", "true", "true", "0.001", "true"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"ZDT3", "originalParetoFronts\\ZDT3.pf", "10", "true", "true", "0.001"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf","50"});
//            BFWN_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf"});
            }

            else if (args.length==7) {
                // in case it is called via the commandline with parameters...
                SMSEMOA_main.main(new String[]{
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


} // SMSEMOA
