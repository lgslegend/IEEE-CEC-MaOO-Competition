/* COPY OF BFNW.java */






/**
 * BFNW.java (IBEA.java based)
 *
 *
 * @author Markus Wagner
 * @version 1.1
 */
package jmetal.metaheuristics.bfnw;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import jmetal.base.*;
import jmetal.base.operator.comparator.DominanceComparator;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.*;

/**
 * This class representing the SPEA2 algorithm
 */
public class AGE2 extends Algorithm {

    static boolean debugPrintGlobal = !true;
    boolean debugPrintBFNW = !true;
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
     * Create a new AGE2 instance
     * @param problem Problem to solve
     */
    public AGE2(Problem problem) {
        this.problem_ = problem;
    }

    /**
     * Runs the AGE2 algorithm.
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators"); // this line had to be added (mw)
        boolean doMutation = ((Boolean)getInputParameter("doMutation")).booleanValue();
        boolean doCrossover = ((Boolean)getInputParameter("doCrossover")).booleanValue();
        int infoPrinterHowOften;
        if (getInputParameter("infoPrinterHowOften")==null) infoPrinterHowOften=1000;
            else infoPrinterHowOften = ((Integer)getInputParameter("infoPrinterHowOften")).intValue();
        String infoPrinterSubDir = (String)getInputParameter("infoPrinterSubDir");
        double epsilonGridWidth = ((Double) getInputParameter("epsilonGridWidth")).doubleValue();
        boolean doOnMPICluster;
        if (getInputParameter("doOnMPICluster")==null) doOnMPICluster = false;
            else doOnMPICluster = ((Boolean) getInputParameter("doOnMPICluster")).booleanValue();

//        int populationSize, archiveSize, maxEvaluations, evaluations;
        int populationSize, maxEvaluations, evaluations;
        Operator crossoverOperator, mutationOperator, selectionOperator;
        SolutionSet solutionSet, archive, offSpringSolutionSet;

        //Read the params
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
//        archiveSize = ((Integer) getInputParameter("archiveSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

        //Read the operators
        crossoverOperator = operators_.get("crossover");
        mutationOperator = operators_.get("mutation");
        selectionOperator = operators_.get("selection");
        System.out.println("selector:"+selectionOperator.toString());

        //Initialize the variables
        solutionSet = new SolutionSet(populationSize);
//        archive = new SolutionSet(archiveSize);
        evaluations = 0;

        //////// AGE2 START
        int endPopulationSize = populationSize;
        double logPop = Math.log10(endPopulationSize);
        double oneOverObjDims = 1d/problem_.getNumberOfObjectives();
        double l = Math.log(populationSize)/ (Math.log(problem_.getNumberOfObjectives()));
        int minPopulationSize = (int)(endPopulationSize*(1d-Math.sqrt(oneOverObjDims)));
//        int minPopulationSize = (int)(0.5d*endPopulationSize);
        populationSize = (int)Math.pow(populationSize,
////                2
//                1d/ (Math.log(problem_.getNumberOfObjectives())/Math.log(populationSize))
////                1d+(1d/ (problem_.getNumberOfObjectives() ))
//                1d+(1d/ (problem_.getNumberOfObjectives() -1d ))

                //=(  1+   (1/( LOG(L$4;10))  + 1/(G5)^( LOG(L$4;10))   )     )
//                1d + (1d/logPop) + Math.pow(oneOverObjDims, logPop)

                //=(  1+   (1/( LOG(G$4;10))^(B5)  + 1/(B5)^( LOG(G$4;10))   )     )
                1d + Math.pow(1d/logPop, problem_.getNumberOfObjectives()) + Math.pow(oneOverObjDims, logPop)

                );
//        populationSize = (int)(populationSize*)));
//        populationSize = (int)Math.pow(populationSize,1d+oneOverObjDims);
        System.out.println("popSize: init="+populationSize+" min="+minPopulationSize+" end="+endPopulationSize);
//        populationSize = (int)Math.ceil(populationSize);
//        populationSize = (int)Math.ceil(minPopulationSize);
//        populationSize = (int)Math.ceil(endPopulationSize);
        solutionSet = new SolutionSet(populationSize);
        //////// AGE2 END

        //-> Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations++;
            solutionSet.add(newSolution);
        }

        /* Initialize the archive with the solutionSet. In subsequent iterations, 
         * the newly constructed point will first be added to the archive, and then
         * the best mu points out of mu+1 are selected that approximate the new 
         * archive best.
         */
        archive = (SolutionSet)DeepCopy.copy(solutionSet);

//        EpsilonDominanceComparatorGridBasedAdditive c = new EpsilonDominanceComparatorGridBasedAdditive(epsilonGridWidth);
        DominanceComparator cNormal = new DominanceComparator();


//        File mpiClusterFile = new File(".");

        boolean d1 = !true;


        // main loop starts...
        while (evaluations <= maxEvaluations) {

            if (d1) System.out.println(evaluations);

            try {
            /* START debug printouts */
            if (infoPrinter==null) 
                if (doOnMPICluster) {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);
                } else {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir, infoPrinterHowOften, infoPrinterHowOften);
                }
//            if (infoPrinter==null) infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);
            if (doOnMPICluster) {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
            } else {
                infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
                infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
            }
            } catch(Exception e) {
                System.out.println("some exception thrown in infoPrinter");
//                System.exit(0);
            }

            if (evaluations>=maxEvaluations) {
                if (doOnMPICluster) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
                }
                break;
            }
            /* END debug printouts */


            if (d1) System.out.println("point1");

            /* START measure time needed per iteration */
//            boolean debugTimes = false;
//            long outerMillisStart = 0;
//            long outerMillisEnd = 0;
//            if (evaluations % 50 == 0 && evaluations > populationSize) debugTimes = true; else debugTimes = false;
//            if (debugTimes) outerMillisStart = System.currentTimeMillis();
//            if (debugPrintBFNW && evaluations > populationSize) {
//                double approximation = BFNW.computeApproximation(solutionSet, archive)[0];
//                System.out.println("[eval:" + evaluations + "] " + "LOOP: OLD approximation " + approximation + " " + solutionSet
////                        + " (HYPmy:" + indicators.getHypervolume(solutionSet)
//                        + ",HYPreal:" + indicators.getTrueParetoFrontHypervolume() + ")");
//            }
//            if (debugTimes) System.out.print(evaluations + " ");
            /* END measure time needed per iteration */

            /* BFNW: Create a new offspringPopulation */

            /* START bfnw block */
//            if (debugPrintBFNW) {
//                double approximation = BFNW.computeApproximation(solutionSet, archive)[0];
//                System.out.println("BFNW: OLD approximation " + approximation + " " + solutionSet);
//            }


            //////// AGE2 START
            populationSize = Math.max( (int)Math.ceil(
                    (double)evaluations/(double)maxEvaluations*(double)endPopulationSize),
                    minPopulationSize);
            //////// AGE2 END

            if (d1) System.out.println("point2");

            /* BFNW 1. step: generate one new solution */
            offSpringSolutionSet = new SolutionSet(populationSize);                          // generate mu solutions
            SolutionSet offSpringSolutionSetForArchive = new SolutionSet(populationSize);    // generate mu solutions
            Solution[] parents = new Solution[2];
            Solution[] offSpring = null;

            boolean localDebug = !true;

              for (int kk = 0; kk<populationSize; kk++){                     // loop condition: generate lambda inividuals
        //      while (offSpringSolutionSet.size() < populationSize){        // loop condition: are lambda better points already generated?
                  if (d1) System.out.println("generate new kk="+kk);
                    int j = 0;
                    if (localDebug) System.out.println("a"+solutionSet.size());
//                    SolutionSet parentsA = new SolutionSet(BFNW.TOURNAMENTS_ROUNDS);
//                    SolutionSet parentsB = new SolutionSet(BFNW.TOURNAMENTS_ROUNDS);
                    do {
                        j++;
                        parents[0] = (Solution) selectionOperator.execute(solutionSet); // carefull! the operator may work on the fitness values (which we did not really have in the beginning)
//                        parentsA.add(parents[0]);
                    } while (j < AGE2.TOURNAMENTS_ROUNDS); // do-while                  // alternatively: choose a point at random and mutate that

                    int k = 0;
                    do {
                        k++;
                        parents[1] = (Solution) selectionOperator.execute(solutionSet);
//                        parents[1] = (Solution) SelectionFactory.getSelectionOperator("RandomSelection").execute(solutionSet);
//                        parentsB.add(parents[0]);
                    } while (k < AGE2.TOURNAMENTS_ROUNDS); // do-while

                    if (d1) System.out.println("END tournament");

//                    parents[0] = (Solution) SelectionFactory.getSelectionOperator("BestSolutionSelectionFitnessInverted").execute(parentsA);
//                    parents[1] = (Solution) SelectionFactory.getSelectionOperator("BestSolutionSelectionFitnessInverted").execute(parentsB);
//                    parents[0] = (Solution) SelectionFactory.getSelectionOperator("BestSolutionSelectionFitness").execute(parentsA);
//                    parents[1] = (Solution) SelectionFactory.getSelectionOperator("BestSolutionSelectionFitness").execute(parentsB);

                    if (localDebug) System.out.println("b"+solutionSet.size());

                    //make the crossover and generate a single child
                    if (doCrossover) offSpring = (Solution [])crossoverOperator.execute(parents);    // 2 parents are XOed
                        else offSpring = parents;                                                    // no XO
                    if (doMutation) mutationOperator.execute(offSpring[0]);                          // mutation

                    // FITNESS EVALUATION - note: this does not set fitness, just runs the problem functions
                    problem_.evaluate(offSpring[0]);
                    problem_.evaluateConstraints(offSpring[0]);
                    evaluations++;

                    if (d1) System.out.println("END new point evaluated");

                    /* START check if new offSpring is not (epsilon) dominated by a population point */
                    boolean newPointIsDominatedByOldPopulation = false;
                    for (int i = 0; i<solutionSet.size(); i++) {
                        /*
                         * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
                         *          non-dominated, or solution1 is dominated by solution2, respectively.
                         */
                        int result = cNormal.compare(solutionSet.get(i), offSpring[0]);
                        // break if once dominated
                        if (result==-1) {
                            newPointIsDominatedByOldPopulation = true;
                            break;
                        }


                        /*
                         * the following 4 lines have the potential to cause a population to contain fewer than mu individuals
                         */
                        // remove populationpoint if new point dominates that one
//                        if (result==1) {
//                            solutionSet.remove(i);
//                            if (localDebug) System.out.println("r"+solutionSet.size());
//                            i--; // reduce i as we have just modified the list
//                        }


                    }

                    if (newPointIsDominatedByOldPopulation) {
                        // just forget this point
                        continue;
                    } else {

                    }
                    /* END check if new offSpring is not epsilon dominated by a population point */


                    /* START check if new offSpring is not (epsilon) dominated by an archive point */
                    boolean newPointIsDominatedByOldArchive = false;
                    for (int i = 0; i<archive.size(); i++) {
                        /*
                         * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
                         *          non-dominated, or solution1 is dominated by solution2, respectively.
                         */
                        int result = cNormal.compare(archive.get(i), offSpring[0]);
                        if (result==-1) {
                            // break if an archive point dominates the new point
                            newPointIsDominatedByOldArchive = true;
                            break;
                        }
                        if (archive.size()>=2)
                            if (result==1) {
                                // remove archive point if new point dominates that one
                                archive.remove(i);
                                i--;
                            }
                    }
                    /* END check if new offSpring is not epsilon dominated by an archive point */

                    // define behavior: add offspring to archive
                    if (newPointIsDominatedByOldArchive) {
                        // forget this point
                        continue;
                    } else {
                        offSpringSolutionSet.add(offSpring[0]);
                        offSpringSolutionSetForArchive.add(offSpring[0]);
                  }
              }
            if (d1) System.out.println("END generate lambda invididuals");
            /*END generate lambda invididuals*/


            /* technically important: add all non-dominated points to the archive. */
            archive = archive.union(offSpringSolutionSetForArchive);
            /* merge population with offSpringSolutionSet */
            solutionSet = solutionSet.union(offSpringSolutionSet);              // would it be neccessary to take just the first subfront?


            /* START select mu auf of mu+lambda */
//            boolean debugPrintDifferentSelectionImplementations = false;
//            if (debugPrintDifferentSelectionImplementations) System.out.println("-current      : "+BFNW.computeApproximation(solutionSet, archive)[0]
//                    +"     "+solutionSet.size());
//            SolutionSet solutionSetImprovedSpeed = (SolutionSet)DeepCopy.copy(solutionSet);
//            long startTimeImprovedSpeed = System.nanoTime();

            if (d1) System.out.println(solutionSet.size() + " " + archive.size()
                    + " " + populationSize);

            reducePopulationToSize(solutionSet, archive, populationSize);
//            long stopTimeImprovedSpeed = System.nanoTime();
//            if (debugPrintDifferentSelectionImplementations) System.out.println("-imp selection: app="+BFNW.computeApproximation(solutionSetImprovedSpeed, archive)[0]
//                    +" "+ Math.round((stopTimeImprovedSpeed - startTimeImprovedSpeed)/1000000d) +"ms "+solutionSetImprovedSpeed.size());
//            solutionSet = solutionSetImprovedSpeed;
            /* END select mu auf of mu+lambda */




//            //////// AGE2 START
//            Distance distance = new Distance();
//            distance.crowdingDistanceAssignment(solutionSet, problem_.getNumberOfObjectives());
//            //////// AGE2 END



        } // end of main loop

        // preparation of the result of the optimization process
//        Ranking ranking = new Ranking(solutionSet);
//        Ranking ranking = new Ranking(archive);                               // returning the archive may be regarded as unfair, due to the enourmous size
//        return ranking.getSubfront(0);

        if (doOnMPICluster) {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
        }

        return solutionSet;
    } // execute

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static boolean lexicographicallyLessOrEqual(double[] a, double[] b) {
        int min = Math.min(a.length, b.length);
        boolean debugPrint = false;
        if (debugPrint) System.out.println(Arrays.toString(a));
        if (debugPrint) System.out.println(Arrays.toString(b));
        for (int i = 0; i<min; i++) {
            if (a[i]<b[i]) {
//                System.out.println(i);
                if (debugPrint) System.out.println("lex<"+i);
                return true;
            }
            else if (a[i]==b[i]) {
                continue;
            } else {
                if (debugPrint) System.out.println("lex>"+i);
                return false;
            }
        }
        if (debugPrint) System.out.println("lex=");
        return true;
    }

    /** Compute how well the population approximates the archive
     *
     * @param population
     * @param archive
     * @return a sorted double[] where the maximal approximation (best approximation of the
     *   worstly approximated archive point) is in the first field
     */
    public static double[] computeApproximation(SolutionSet population, SolutionSet archive) {
        boolean debugPrint = true && debugPrintGlobal;
        if (debugPrint) System.out.println("computeApproximation/2: ");

        SolutionSet archiveFront = null;

        // get non-dominated archive set
        // note: makes the archive a little bit smaller for higher efficiency without effecting the result, BUT: n^2 needed to determine set of non-dominated points.
//      Ranking ranking = new Ranking(archive);
//      archiveFront = ranking.getSubfront(0);
        archiveFront = archive;
        
        if (debugPrint) System.out.println("population.size=" + population.size() + " archive.size=" + archive.size() + " front.size=" + archiveFront.size());

        /* store all the "pop app arc" in the following array:
         * for each archive point it is stored how well a population point approximates it
         */
//        double[][] approximations = new double[archiveFront.size()][population.size()];
        

        /* store all minimums of approximations per archive point in this array */
        double[] results = new double[archiveFront.size()];

        // compute approximation for each non-dominated point of the archive
        // to find out how well it is approximated by the population points
        for (int i = 0; i < archiveFront.size(); i++) {
            Solution s = archiveFront.get(i);   // non-dominated archive-point

            double deltaForThisSolution = Double.MAX_VALUE; //minimize this value
            for (int j = 0; j < population.size(); j++) {
                Solution p = population.get(j);

                // note: it does not matter if i and j are the same: then the approximation is 0
//                if (identicalSolutions(s, p)) {
////                  System.out.println("1");
//                    continue;
//                }

                // compute how an element p of the front(population) approximates an element s of the archive in domain and image
                double deltaForThisSolutionCurrent = computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s

//                approximations[i][j] = deltaForThisSolutionCurrent;

                if (deltaForThisSolutionCurrent < deltaForThisSolution) {
                    deltaForThisSolution = deltaForThisSolutionCurrent;
                }

                // optimized runtime: if an archive point is 0-approximated, don't look any further for that point...
//                if (deltaForThisSolution == 0) {
//                    break;
//                }
            }

            // save the minimal approximation
            results[i] = deltaForThisSolution;

            if (debugPrint) {
                System.out.printf("-deltaForThisSolution: %8.5f ", deltaForThisSolution);
                System.out.println("(=" + deltaForThisSolution + ")"); // same output as line before, just more precise
            }
        }

        if (debugPrint) System.out.println("sorted deltas: " + Arrays.toString(results));

        Arrays.sort(results);        // biggest approximation is now at the end
//        ArrayUtils.reverse(results); // biggest approximation is now at the beginning
        reverse(results);

        if (debugPrint) System.out.println("->deltaForThisSolutionSet(approximation): " + results[0]);

        return results;              // maximal approximation is now in results[0];
    }

    public static void reverse(double[] b) {
   int left  = 0;          // index of leftmost element
   int right = b.length-1; // index of rightmost element

   while (left < right) {
      // exchange the left and right elements
      double temp = b[left];
      b[left]  = b[right];
      b[right] = temp;

      // move the bounds toward the center
      left++;
      right--;
   }
}


    /** Checks whether two solutions contain identical decision variable values
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean identicalSolutions(Solution a, Solution b) {
        Variable[] aVars = a.getDecisionVariables();
        Variable[] bVars = b.getDecisionVariables();
        /* if at least one variable value is different: stop the checking
         * and return false, else return true
         */
        for (int i = 0; i < aVars.length; i++) {
            if (aVars[i] != bVars[i]) return false;
        }
        return true;
    }

    /** Compute how well a solution p approximates a solution s. This is done by
     * determining the approximation for all decision variables and objective
     * variables and then taking the maximum of these approximations (i.e. the worst).
     * @param p
     * @param s
     * @return max(all approximations "p app s")
     */
    public static double computeApproximationSolutionForSolution(Solution p, Solution s) {
        boolean debugPrint = true && debugPrintGlobal;
      if (debugPrint) System.out.print("computeApproximationSolutionForSolution/2: ");

        double delta = 0; // in order to maximize delta

        /* DV = decision variable, O = objective variable */
//        double[] sDV = solutionDecisionVariablesToDoubleArray(s);
//        double[] pDV = solutionDecisionVariablesToDoubleArray(p);

        double[] sOV = solutionObjectivesToDoubleArray(s);
        double[] pOV = solutionObjectivesToDoubleArray(p);

//        if (debugPrint) {
//            System.out.print("sDV[");
//            for (double d : sDV) System.out.printf(d + " ");
//            System.out.print("[");
//            for (double d : sOV) System.out.printf(d + " ");
//            System.out.print("]");
//            System.out.printf("] f(sDV)=" + s.getFitness() + "   ");
//
//            System.out.print(" pDV[");
//            for (double d : pDV) System.out.print(d + " ");
//            System.out.print("[");
//            for (double d : pOV) System.out.print(d + " ");
//            System.out.print("]");
//            System.out.print("] f(pDV)=" + p.getFitness() + "   ");
//
//            System.out.print(" >> deltas: ");
//        }

        double temp;

//        /* 1. compute the approximation of s by p in domain;
//         * additive:       temp = pDV[i]-sDV[i];
//         * multiplicative: temp = pDV[i]/sDV[i]; // problematic with negative numbers!
//         *
//         * additive approximation, based on Friedrich/Bringmann/Voß/Igel FOGA 2011
//         * -> for the two vectors p,s find the largest distance between bost
//         */
//        for (int i = 0; i < sDV.length; i++) {
//            temp = pDV[i] - sDV[i];
//            if (temp > delta) delta = temp;
//            if (debugPrint) System.out.print(temp + " ");
//        }

        // 2. compute the approximation of s by p in image (problem-specific)
        for (int i = 0; i < sOV.length; i++) {
            temp = pOV[i] - sOV[i];
            if (temp > delta) delta = temp;
            if (debugPrint) System.out.printf(temp + " ");
        }

        if (debugPrint) System.out.print(" maxDelta:" + delta + "\n");
//      if (debugPrint) System.out.printf(" maxDelta:%8.5f\n",delta);
        return delta;
    }

    /** Returns the decision variables of a solution
     * @param s
     * @return double[]
     */
    public static double[] solutionDecisionVariablesToDoubleArray(Solution s) {
        Variable[] varsV = s.getDecisionVariables();
        double[] result = new double[varsV.length];
        for (int j = 0; j < varsV.length; j++) {
            try {
                result[j] = varsV[j].getValue();
            } catch (JMException ex) {
                System.out.println("problem in solutionDecisionVariablesToDoubleArray");
//                Logger.getLogger(BFNW.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    /** Returns the objective variables of a solution
     * @param s
     * @return
     */
    public static double[] solutionObjectivesToDoubleArray(Solution s) {
        int numberOfObjectives = s.numberOfObjectives();
        double[] result = new double[numberOfObjectives];
        for (int j = 0; j < numberOfObjectives; j++) {
            result[j] = s.getObjective(j);
        }
        return result;
    }

    /** Prints a double[][] on screen.
     * @param a
     */
    public static void printDouble2DArray(double[][] a) {
        for (double[] row : a) {
            for (double d : row) {
                System.out.printf("%8.5f ", d);
            }
            System.out.println();
        }
    }




    // the followin is based on computeFitnesses/2
    /** Compute how well the population approximates the archive, see Karl's email from 07.12.2010
     *
     * @param population
     * @param archive
//     * @return
     */
    public void reducePopulationToSize(SolutionSet population, SolutionSet archive, int targetSize) {
//        boolean debugPrint = true;
        boolean debugPrint = true && debugPrintGlobal;
        boolean debugPrintAdditional = false;
        if (debugPrint) System.out.println("computeFitnesses/2: ");

        // the following array stores the maximum approximations for which a population point is responsible
        int[] whichPopPointIsResponsible = new int[archive.size()];
        int[] whichPopPointIsResponsibleSecondBest = new int[archive.size()];

        SolutionSet archiveFront = archive;

        double[] results = new double[archiveFront.size()];

        if (debugPrint) System.out.println("population.size=" + population.size() + " archive.size=" + archive.size() + " front.size=" + archiveFront.size());

        /* store all the "pop app arc" in the following array:
         * for each archive point it is stored how well a population point approximates it
         */
//        double[][] approximations = new double[archiveFront.size()][population.size()];


        /* store all minimums of approximations per archive point in this array */
        double[] eps1 = new double[archiveFront.size()];
        double[] eps2 = new double[archiveFront.size()];

        // compute approximation for each non-dominated point of the archive
        // to find out how well it is approximated by the population points
        for (int i = 0; i < archiveFront.size(); i++) {
            Solution s = archiveFront.get(i);   // non-dominated archive-point

            double deltaForThisSolution = Double.MAX_VALUE; //minimize this value
            double deltaForThisSolutionSecondBest = Double.MAX_VALUE; //minimize this value
            for (int j = 0; j < population.size(); j++) {
                Solution p = population.get(j);

                // compute how an element p of the front(population) approximates an element s of the archive in domain and image
                double deltaForThisSolutionCurrent = computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s

//                approximations[i][j] = deltaForThisSolutionCurrent;

                // if better then currentBest
                if (deltaForThisSolutionCurrent < deltaForThisSolution) {
                    deltaForThisSolutionSecondBest = deltaForThisSolution;      // old best becomes secondBest
                    deltaForThisSolution = deltaForThisSolutionCurrent;         // new best becomes best
                    whichPopPointIsResponsibleSecondBest[i] = whichPopPointIsResponsible[i];
                    whichPopPointIsResponsible[i] = j;
                } else {
                // if better then currentSecondBest
                    if (deltaForThisSolutionCurrent < deltaForThisSolutionSecondBest) {
                        deltaForThisSolutionSecondBest = deltaForThisSolutionCurrent;
                        whichPopPointIsResponsibleSecondBest[i] = j;
                    }
                }

//                 optimized runtime: if an archive point is 0-approximated, don't look any further for that point...
//                if (deltaForThisSolution == 0) {
//                    break;
//                }
            }

            // save the minimal approximation
            results[i] = deltaForThisSolution;
            eps1[i] = deltaForThisSolution;
            eps2[i] = deltaForThisSolutionSecondBest;

            if (debugPrint) {
                System.out.printf("-deltaForThisSolution: %8.5f ", deltaForThisSolution);
                System.out.println("(=" + deltaForThisSolution + ")"); // same output as line before, just more precise
            }
        }

//        if (debugPrint) System.out.println("eps1: " + Arrays.toString(eps1));
//        if (debugPrint) System.out.println("eps2: " + Arrays.toString(eps2));

//        // determine whether p is p1(a) for some a \in A
//        boolean[] pIsP1a = new boolean[population.size()];
//        for (int i = 0; i<pIsP1a.length; i++)
//            pIsP1a[i] = false;
//        for (int i = 0; i<whichPopPointIsResponsible.length; i++) {
//            int p = whichPopPointIsResponsible[i];
//            pIsP1a[p] = true;
//        }
        // keep track whether some point is still in the population
        boolean[] pIsInCurrentPop = new boolean[population.size()];
        for (int i = 0; i<pIsInCurrentPop.length; i++)
            pIsInCurrentPop[i] = true;


        //now determine the val(p)
        double[] val = new double[population.size()];
        double minVal = Double.MAX_VALUE;
        int minValIndex = 0;
        for (int i = 0; i<eps1.length; i++) {
            double eps2a = eps2[i];
            int p = whichPopPointIsResponsible[i];
                if (eps2a>val[p])
                    val[p] = eps2a;            
        }

        for (int i=0; i<val.length; i++) {
                if (val[i]<minVal) {
                        minVal = val[i];
                        minValIndex = i;
                }
        }
        
        int popCounter=population.size();
        if (debugPrintAdditional) System.out.println("popCounter"+popCounter);
//        population.remove(minValIndex);
        pIsInCurrentPop[minValIndex] = false;
        popCounter--;
        if (debugPrintAdditional) System.out.println("popCounter"+popCounter);

        if (debugPrintAdditional) if (true) {
            System.out.println("unsorted eps1:    " + Arrays.toString(eps1));
            System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsible));
            System.out.println("unsorted eps2:    " + Arrays.toString(eps2));
            System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsibleSecondBest));
            System.out.println("val        :      " + Arrays.toString(val));
            System.out.println("pIsInCurrentPop:  " + Arrays.toString(pIsInCurrentPop));
//            System.out.println("pIsP1a     :      " + Arrays.toString(pIsP1a));
            System.out.println("minVal="+minVal + " index=" +minValIndex + " population.size()now="+ (population.size()));
//            System.out.println("maxAppForPopPoint:" + Arrays.toString(maxAppForPopPoint));
        }

        while (popCounter > targetSize) {
        //while (population.size() > targetSize) {
            HashSet whichEpsToUpdate = new HashSet();

            for (int i = 0; i<whichPopPointIsResponsible.length; i++) {
                if (minValIndex == whichPopPointIsResponsible[i]) whichEpsToUpdate.add(i);
                if (minValIndex == whichPopPointIsResponsibleSecondBest[i]) whichEpsToUpdate.add(i);
            }

            Iterator it = whichEpsToUpdate.iterator();
            while (it.hasNext()) {
                int i = (Integer)it.next();
                if (debugPrintAdditional) System.out.print(i+" ");
            }
            if (debugPrintAdditional) System.out.println("");


            it = whichEpsToUpdate.iterator();
            while (it.hasNext()) {
                int i = (Integer)it.next();

                Solution s = archiveFront.get(i);   // non-dominated archive-point

                double deltaForThisSolution = Double.MAX_VALUE; //minimize this value
                double deltaForThisSolutionSecondBest = Double.MAX_VALUE; //minimize this value
                for (int j = 0; j < population.size(); j++) {

                    // skip those of the old population that are no longer in the current population
                    if (!pIsInCurrentPop[j]) continue;

                    Solution p = population.get(j);

                    // compute how an element p of the front(population) approximates an element s of the archive in domain and image
                    double deltaForThisSolutionCurrent = computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s

    //                approximations[i][j] = deltaForThisSolutionCurrent;

                    // if better then currentBest
                    if (deltaForThisSolutionCurrent < deltaForThisSolution) {
                        deltaForThisSolutionSecondBest = deltaForThisSolution;      // old best becomes secondBest
                        deltaForThisSolution = deltaForThisSolutionCurrent;         // new best becomes best
                        whichPopPointIsResponsibleSecondBest[i] = whichPopPointIsResponsible[i];
                        whichPopPointIsResponsible[i] = j;
                    } else {
                    // if better then currentSecondBest
                        if (deltaForThisSolutionCurrent < deltaForThisSolutionSecondBest) {
                            deltaForThisSolutionSecondBest = deltaForThisSolutionCurrent;
                            whichPopPointIsResponsibleSecondBest[i] = j;
                        }
                    }

                    // optimized runtime: if an archive point is 0-approximated, don't look any further for that point...
    //                if (deltaForThisSolution == 0) {
    //                    break;
    //                }
                }

                // save the minimal approximation
                results[i] = deltaForThisSolution;
                eps1[i] = deltaForThisSolution;
                eps2[i] = deltaForThisSolutionSecondBest;

                if (debugPrint) {
                    System.out.printf("-deltaForThisSolution: %8.5f ", deltaForThisSolution);
                    System.out.println("(=" + deltaForThisSolution + ")"); // same output as line before, just more precise
                }
            }

            //now determine the val(p)
    //        double[] val = new double[population.size()];
            minVal = Double.MAX_VALUE;
            minValIndex = 0;
            for (int i = 0; i<eps1.length; i++) {
                double eps2a = eps2[i];
                int p = whichPopPointIsResponsible[i];
                    if (eps2a>val[p])
                        val[p] = eps2a;
            }

            for (int i=0; i<val.length; i++) {
                if (pIsInCurrentPop[i])
                    if (val[i]<minVal) {
                            minVal = val[i];
                            minValIndex = i;
                    }

            }
    //        population.remove(minValIndex);
            popCounter--;
            if (debugPrintAdditional) System.out.println("popCounter"+popCounter);
            pIsInCurrentPop[minValIndex] = false;

            if (debugPrintAdditional) if (true) {
                System.out.println("unsorted eps1:    " + Arrays.toString(eps1));
                System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsible));
                System.out.println("unsorted eps2:    " + Arrays.toString(eps2));
                System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsibleSecondBest));
                System.out.println("val        :      " + Arrays.toString(val));
                System.out.println("pIsInCurrentPop:  " + Arrays.toString(pIsInCurrentPop));
    //            System.out.println("pIsP1a     :      " + Arrays.toString(pIsP1a));
                System.out.println("minVal="+minVal + " index=" +minValIndex + " population.size()now="+ (population.size()));
    //            System.out.println("maxAppForPopPoint:" + Arrays.toString(maxAppForPopPoint));
            }
        }
       

  // set fitness in this function as well

                //now determine the maximum approximation for which a popPoint is responsible:
                double[] maxAppForPopPoint = new double[population.size()];
                for (int i = 0; i<whichPopPointIsResponsible.length; i++) {
                    int popPointResponsible = whichPopPointIsResponsible[i];
                    if (pIsInCurrentPop[popPointResponsible]) {
                        if (maxAppForPopPoint[popPointResponsible] < results[i]) { // update if point is responsible for a "worse approximation"
                            maxAppForPopPoint[popPointResponsible] = results[i];

                            // set fitness here...
//                            population.get(popPointResponsible).setFitness(Math.pow( 1d/results[i], this.problem_.getNumberOfObjectives()  ) );
//                            population.get(popPointResponsible).setFitness(Math.pow( results[i], this.problem_.getNumberOfObjectives()  ) );
//                            population.get(popPointResponsible).setFitness(Math.pow( 1d/results[i], 1) );
                            population.get(popPointResponsible).setFitness(Math.pow( results[i], 1) );
                        }
                    }
        }

        // form new population:
        for (int i = pIsInCurrentPop.length - 1; i>=0; i--) {
            if (!pIsInCurrentPop[i]) population.remove(i);
            if (debugPrintAdditional) System.out.println("population.size()"+population.size());
        }
    } //end



//
//
//    // the followin is based on computeApproximation/2
//    /** Compute how well the population approximates the archive
//     *
//     * @param population
//     * @param archive
//     */
//    public void computeFitnesses(SolutionSet population, SolutionSet archive) {
////        boolean debugPrint = true;
//        boolean debugPrint = true && debugPrintGlobal;
//        if (debugPrint) System.out.println("computeFitnesses/2: ");
//
//        // the following array stores the maximum approximations for which a population point is responsible
//        int[] whichPopPointIsResponsible = new int[archive.size()];
//
//        SolutionSet archiveFront = archive;
//
//        if (debugPrint) System.out.println("population.size=" + population.size() + " archive.size=" + archive.size() + " front.size=" + archiveFront.size());
//
//        /* store all the "pop app arc" in the following array:
//         * for each archive point it is stored how well a population point approximates it
//         */
////        double[][] approximations = new double[archiveFront.size()][population.size()];
//
//
//        /* store all minimums of approximations per archive point in this array */
//        double[] results = new double[archiveFront.size()];
//
//        // compute approximation for each non-dominated point of the archive
//        // to find out how well it is approximated by the population points
//        for (int i = 0; i < archiveFront.size(); i++) {
//            Solution s = archiveFront.get(i);   // non-dominated archive-point
//
//            double deltaForThisSolution = Double.MAX_VALUE; //minimize this value
//            for (int j = 0; j < population.size(); j++) {
//                Solution p = population.get(j);
//
//                // compute how an element p of the front(population) approximates an element s of the archive in domain and image
//                double deltaForThisSolutionCurrent = computeApproximationSolutionForSolution(p, s); // population element p, archiveFront element s
//
////                approximations[i][j] = deltaForThisSolutionCurrent;
//
//                if (deltaForThisSolutionCurrent < deltaForThisSolution) {
//                    deltaForThisSolution = deltaForThisSolutionCurrent;
//
//                    whichPopPointIsResponsible[i] = j;
//                }
//
//                // optimized runtime: if an archive point is 0-approximated, don't look any further for that point...
////                if (deltaForThisSolution == 0) {
////                    break;
////                }
//            }
//
//            // save the minimal approximation
//            results[i] = deltaForThisSolution;
//
//            if (debugPrint) {
//                System.out.printf("-deltaForThisSolution: %8.5f ", deltaForThisSolution);
//                System.out.println("(=" + deltaForThisSolution + ")"); // same output as line before, just more precise
//            }
//        }
//
//        if (debugPrint) System.out.println("sorted deltas: " + Arrays.toString(results));
//
//        //now determine the maximum approximation for which a popPoint is responsible:
//        double[] maxAppForPopPoint = new double[population.size()];
//        for (int i = 0; i<whichPopPointIsResponsible.length; i++) {
//            int popPointResponsible = whichPopPointIsResponsible[i];
//            if (maxAppForPopPoint[popPointResponsible] < results[i]) {
//                maxAppForPopPoint[popPointResponsible] = results[i];
//
//                // set fitness here...
//                population.get(popPointResponsible).setFitness(Math.pow( 1d/results[i], this.problem_.getNumberOfObjectives()  ) );
////                population.get(popPointResponsible).setFitness(Math.pow( results[i], this.problem_.getNumberOfObjectives()  ) );
////                population.get(popPointResponsible).setFitness(Math.pow( 1d/results[i], 1) );
//            }
//        }
//
//        if (!true) {
//            System.out.println("unsorted deltas:  " + Arrays.toString(results));
//            System.out.println("responsible:      " + Arrays.toString(whichPopPointIsResponsible));
//            System.out.println("maxAppForPopPoint:" + Arrays.toString(maxAppForPopPoint));
//        }
//
////        Arrays.sort(results);        // biggest approximation is now at the end
//////        ArrayUtils.reverse(results); // biggest approximation is now at the beginning
////        reverse(results);
////
////        if (debugPrint) System.out.println("->deltaForThisSolutionSet(approximation): " + results[0]);
////        return results;              // maximal approximation is now in results[0];
//    }



    /** Minimal function to quickly execute the algorithm
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {


                File test = new File(".");
                System.out.println(test.getAbsolutePath());


//            BFWN_main.main(new String[]{"DTLZ7","originalParetoFronts\\DTLZ7.3D.pf","10", "true", "true", "0.01"});
//            BFWN_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "100", "true", "true", "0.001", "false","15000","BinaryTournament","foo"});
//            BFWN_main.main(new String[]{"DTLZ1_3D", "originalParetoFronts\\DTLZ1.3D.pf", "50", "true", "true", "0.001", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"WFG1_2D", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "false","25000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ4_10D", "originalParetoFronts\\DTLZ2.10D.1000.pf", "50", "true", "true", "0.01", "false","100000","RandomSelection","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
            AGE2_main.main(new String[]{"DTLZ1_4D", "originalParetoFronts\\DTLZ1.4D.10000.pf", "100", "true", "true", "0.01", "true","100000","BinaryTournament2","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            AGE2_main.main(new String[]{"DTLZ4_2D", "originalParetoFronts\\DTLZ2.2D.10000.pf", "100", "true", "true", "0.01", "false","10000","RandomSelection","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"WFG1_3D", "originalParetoFronts\\WFG1.3D.pf", "50", "true", "true", "0.01", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFNW_main.main(new String[]{"DTLZ2_10D", "originalParetoFronts\\DTLZ2.10D.10000.pf", "100", "true", "true", "0.01", "false","20000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ1_12D", "originalParetoFronts\\DTLZ1.12D.1000.pf", "10", "true", "true", "0.01", "true","15000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ1_4D", "originalParetoFronts\\DTLZ1.4D.10000.pf", "50", "true", "true", "0.01", "false","25000","BinaryTournament",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ3_10D", "originalParetoFronts\\DTLZ2.10D.10000.pf", "50", "true", "true", "0.01", "else","25000","BinaryTournament",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ1_4D", "originalParetoFronts\\DTLZ1.4D.1000.pf", "10", "true", "true", "0.01", "true","150000","BinaryTournament",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ1", "originalParetoFronts\\DTLZ1.3D.own.pf", "10", "true", "true", "0.001", "true","15000","RandomSelection",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0.001", "true","15000","RandomSelection",""});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"WFG3", "originalParetoFronts\\WFG3.2D.pf", "10", "true", "true", "0.01"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ7", "nopf", "10", "true", "true", "0.1", "true"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"Water", "originalParetoFronts\\Water.pf", "10", "true", "true", "0.001", "true"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"ZDT3", "originalParetoFronts\\ZDT3.pf", "10", "true", "true", "0.001"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"Kursawe","originalParetoFronts\\Kursawe.pf","50"});
//            BFWN_main.main(new String[]{"WFG1","originalParetoFronts\\WFG1.2D.pf"});
            }
            else if (args.length == 7) {
                // in case it is called via the commandline with parameters...
                AGE2_main.main(new String[]{
                            args[0], // problem
                            "originalParetoFronts/" + args[1], // pf-file, without the directory
                            args[2], // mu
                            "true",  // doXO
                            "true",  // doMUT
                            args[5], // epsilonGridWidth
                            "true",  // do outputs for runs on MPI cluster
                            args[3], // max evaluations
                            args[4], // selection strategy
                            args[6]  // subDirectory name for infoprinter
                        });
            } else {
                System.out.println("unsuitable number of parameters. EXIT.");
            }

        } catch (Exception ex) {
        }
    }
}