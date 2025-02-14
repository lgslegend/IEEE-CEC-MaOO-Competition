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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import jmetal.base.*;
import jmetal.base.operator.comparator.DominanceComparator;
import jmetal.base.operator.comparator.EqualSolutions;
import jmetal.base.operator.selection.SelectionFactory;
import static jmetal.metaheuristics.age.pAGE.isSolutionInPreferred;
import static jmetal.metaheuristics.age.pAGE.printDouble2DArray;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.*;

/**
 * This class representing the SPEA2 algorithm
 */
public class BFNW extends Algorithm {

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
     * Create a new BFNW instance
     * @param problem Problem to solve
     */
    public BFNW(Problem problem) {
        this.problem_ = problem;
    }

    public static Solution convertSolutionToEpsilonGridVectorFLOOR(Solution s, double epsilonGridWidth) {
        Solution result = new Solution(s.numberOfObjectives());
        for (int i=0; i<s.numberOfObjectives(); i++) {
                double v = s.getObjective(i);
                result.setObjective(i, epsilonGridWidth*Math.floor( v/epsilonGridWidth)  );
        }
        return result;
    }
    public static Solution convertSolutionToEpsilonGridVectorCEILING(Solution s, double epsilonGridWidth) {
        Solution result = new Solution(s.numberOfObjectives());
        for (int i=0; i<s.numberOfObjectives(); i++) {
                double v = s.getObjective(i);
                result.setObjective(i, epsilonGridWidth*Math.ceil( v/epsilonGridWidth)  );
        }
        return result;
    }
    
    public static Solution moveEpsilonGridVectorOnceFurtherAway(Solution s, double epsilonGridWidth) {
        Solution result = new Solution(s.numberOfObjectives());
        for (int i=0; i<s.numberOfObjectives(); i++) {
                double v = s.getObjective(i);
                result.setObjective(i, (v/epsilonGridWidth + 1) * epsilonGridWidth  );
        }
        return result;
    }
    
    /**
     * Runs the BFNW algorithm.
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
        
        /* or: initialise with epsilonboxes, where a point is in the box */
        boolean useEpsilonBoxesArchive;
        
        if (epsilonGridWidth==0) useEpsilonBoxesArchive = false;
            else useEpsilonBoxesArchive = true;
        
        System.out.println("useEpsilonBoxesArchive="+useEpsilonBoxesArchive+" epsilonGridWidth="+epsilonGridWidth);
        
        if (useEpsilonBoxesArchive) {
            archive = new SolutionSet(populationSize);
//            archive = new SolutionSet(populationSize);
            for (int i = 0; i<populationSize; i++) {
                Solution converted = convertSolutionToEpsilonGridVectorFLOOR(solutionSet.get(i),epsilonGridWidth);
                archive.add(converted); 
           }
        }
        
//        Ranking rtemp = new Ranking(archive);
//        archive = rtemp.getSubfront(0);
        System.out.println("initial: population.size()="+solutionSet.size()+" archive.size()="+archive.size());

//        EpsilonDominanceComparatorGridBasedAdditive c = new EpsilonDominanceComparatorGridBasedAdditive(epsilonGridWidth);
//        Comparator cNormal = new EqualSolutions();
        Comparator cNormal = new DominanceComparator();


//        File mpiClusterFile = new File(".");


        boolean debugEarlyBreak = false;
        
        int newPointIsDominatedByOldArchiveTakeNeverthelessCounter = 0;
        int newPointIsDominatedByOldArchiveCounter = 0;
        
        System.out.println("\\\\\\\\\\ IJCAI2013: AGE Version //////////");


        // main loop starts...
        while (evaluations <= maxEvaluations) {
            
            if (debugEarlyBreak) System.out.println("d "+evaluations);

            /* START debug printouts */
            if (infoPrinter==null) 
                if (doOnMPICluster) {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir); 
                } else {
                    infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir, infoPrinterHowOften, infoPrinterHowOften);
                }
            
//            if (infoPrinter==null) infoPrinter = new InfoPrinter(this, problem_, infoPrinterSubDir);

            if (evaluations%1000==0)
                if (doOnMPICluster) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
                } else {
                    if (debugEarlyBreak) System.out.println("d toFile...:"+solutionSet.size());
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
                    if (debugEarlyBreak) System.out.println("d toScreen...");
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
                }
            if (evaluations>=maxEvaluations) {
                if (doOnMPICluster) {
                    infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true); //correct line
//                    infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, false);
                }
                break;
            }
            /* END debug printouts */
            /* END debug printouts */

            if (debugEarlyBreak) System.out.println("d p1");


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

            /* BFNW 1. step: generate one new solution */
            offSpringSolutionSet = new SolutionSet(populationSize);                          // generate mu solutions
            SolutionSet offSpringSolutionSetForArchive = new SolutionSet(populationSize);    // generate mu solutions
            Solution[] parents = new Solution[2];
            Solution[] offSpring = null;

            boolean localDebug = !true;

            
            
            if (!true) {
                //take first front(s)
                        Ranking tempr = new Ranking(solutionSet);
                        SolutionSet t = new SolutionSet();
                        //if (evaluations%100==0)System.out.println("solutionSet.size()="+solutionSet.size()+" fronts="+tempr.getNumberOfSubfronts()+" front(0).size()="+tempr.getSubfront(0).size());
                        //solutionSet = tempr.getSubfront(0);
                        t = t.union(tempr.getSubfront(0));
                        
                        
//                        System.out.print(t.size() + " " + t.getMaxSize());t.remove(0);System.out.println(" "+t.size() + " " + t.getMaxSize());
                        
                        if (tempr.getNumberOfSubfronts()>1) {
                            t = t.union( tempr.getSubfront(1+ (int) (Math.random()*tempr.getNumberOfSubfronts()-2)) );
//                            t = t.union(tempr.getSubfront(1));
                        }
                        
                        solutionSet = t;
            }
            if (!true) {
                //take first front and the next ones with decreasing probability    NOT VERY REASONABLE to take full front or not
                        Ranking tempr = new Ranking(solutionSet);
                        SolutionSet t = new SolutionSet();
                        //if (evaluations%100==0)System.out.println("solutionSet.size()="+solutionSet.size()+" fronts="+tempr.getNumberOfSubfronts()+" front(0).size()="+tempr.getSubfront(0).size());
                        t = t.union(tempr.getSubfront(0));
                        
                        for (int i=1; i<tempr.getNumberOfSubfronts(); i++) {
//                            if (Math.random()<Math.pow(1d/i,50d)) {
                            if (Math.random()<1d/i) {
                                t=t.union(tempr.getSubfront(i));
                            }
                        }
                        solutionSet = t;
            }
            if (!true) {
            //  crowding distances over complete population        !NONSENSE!    
                Distance distance = new Distance();
                distance.crowdingDistanceAssignment(solutionSet, problem_.getNumberOfObjectives(), 0, archive);     // careful: use correct switch!
            }
            
            
            
            
            
            boolean thinningAndCrowding = true;
            
            
// population reduction
          
/*14 nov 2012 deactivated*/            
/*!!!*/     if (thinningAndCrowding) {
                // take first front(s) and extreme points
                boolean debugSelection = !true;
                
                if (debugSelection) System.out.print("orig:"+solutionSet.size()+ " ");
                
                Distance distance = new Distance();
                
                Ranking tempr = new Ranking(solutionSet);
                int crowdingDistanceSwitch = -1;                                // for -1 for AGE-specific variants
                
                SolutionSet t = new SolutionSet(populationSize);
                
                if (debugSelection) System.out.print(" fronts:"+tempr.getNumberOfSubfronts()+ " ");
                t = t.union(tempr.getSubfront(0));                              // definitely take first front
                int included = 1;
//                if (tempr.getNumberOfSubfronts()>1) {                           // take second front if possible
//                    t = t.union(tempr.getSubfront(1));                          
//                    included++;
//                }
                if (debugSelection) System.out.print(" 1st:"+t.size()+ " ");
                
                
                for (int i = included; i<tempr.getNumberOfSubfronts(); i++) {   // from next fronts take probabilistically
                    SolutionSet x = tempr.getSubfront(i);
                    distance.crowdingDistanceAssignment(x, problem_.getNumberOfObjectives(), crowdingDistanceSwitch, archive);
                    
//                    System.out.println(" old:"+t.size());
                    
                    // to force the extreme points to be stay in the population - not good for DTLZ4
//                    if (x.size()==1) {
//                        t.add(x.get(0));
//                    } else {
//                        t.add(x.get(0));
//                        t.add(x.get(x.size()-1));
//                    }
//                    for (int j = 1; j<x.size()-1; j++) {
                    
                    for (int j = 0; j<x.size(); j++) {
/* changed from below to true 12/12/12*/ if (true) { 
//                        if (x.get(j).getCrowdingDistance()==Double.POSITIVE_INFINITY) {

//                            if (Math.random()< (1d/(i)) ) {                   // (used for 120216)
//                            if (Math.random()< (1d/(.6)) ) {                                 
                            if (Math.random()< (1d/(i+1)) ) {                 // decreasing probability depending on the rank number
                                t.add(x.get(j));
                                if (debugSelection) System.out.print("+");
                            } else { if (debugSelection) System.out.print("/");}
                            
                        } else {
                            if (debugSelection) System.out.print("-");
                        }
                    }
                    if (debugSelection) System.out.print(",");
//                    System.out.println(" new:"+t.size());
                }
//                System.out.println();
//                for (int i = 0; i<solutionSet.size(); i++) {
//                    if (solutionSet.get(i).getCrowdingDistance()==Double.POSITIVE_INFINITY) {
//                        t.add(solutionSet.get(i));
////                        if (debugSelection) System.out.print("+");
//                    } else {
////                        if (debugSelection) System.out.print("-");
//                    }
////                    if (debugSelection) System.out.print(",");
//                }
                solutionSet = t;
                if (debugSelection) System.out.println(" new:"+solutionSet.size());
            }
            
            
            
            
            
            
            
            
            
/*14 nov 2012 deactivated*/   
            if (thinningAndCrowding) {
                // crowding distances for each front
                Distance distance = new Distance();
                Ranking tempr = new Ranking(solutionSet);
                for (int i = 0; i<tempr.getNumberOfSubfronts(); i++) {
                    distance.crowdingDistanceAssignment(tempr.getSubfront(i), problem_.getNumberOfObjectives(), 0, archive);    // careful: use correct switch!
                }
            }
            
            if (!true) {      // set crowding distance to inverted front number USE BT2
                // ranking over each front 
                //Distance distance = new Distance();
                Ranking tempr = new Ranking(solutionSet);
                int subs = tempr.getNumberOfSubfronts();
                for (int i = 0; i<subs; i++) {
                    SolutionSet t = tempr.getSubfront(i);
                    for (int j=0; j<t.size(); j++) {
                        t.get(j).setCrowdingDistance(subs-i);                   // first front highest fitness, second front has second highest fitness, ... 
                    }
                }
            }
            
            
            if (!true) {    // use ranknumber as fitness (higher better), if equal then crowding (higher better) [use with BinaryTournament3]
                // ranking over each front 
                Distance distance = new Distance();
                Ranking tempr = new Ranking(solutionSet);
                int subs = tempr.getNumberOfSubfronts();
                for (int i = 0; i<subs; i++) {
                    SolutionSet t = tempr.getSubfront(i);
                    for (int j=0; j<t.size(); j++) {
                        t.get(j).setFitness(subs-i);                   // first front highest fitness, second front has second highest fitness, ... 
                    }
                    distance.crowdingDistanceAssignment(t, problem_.getNumberOfObjectives(), 0, archive);  
                }
            }
            
            
            
            
            
            
//            System.out.println("pre offspring");
            
            
            
            
            
   /*mu+1*/         boolean forceInsertion = true;//use this line if a single offspring shall be generated and this should later be considered for selection --> (mu+1)-style
//  /*mu+1*/      for (int kk = 0; kk<1 ; kk++){
   /*mu+mu*/    for (int kk = 0; kk<populationSize; kk++){                     // loop condition: generate lambda inividuals
//              for (int kk = 0; kk<1 || offSpringSolutionSet.size()<1; kk++){
//              for (int kk = 0; kk<1 || offSpringSolutionSet.size()<2&&solutionSet.size()<2; kk++){
//              for (int kk = 0; kk<1 || offSpringSolutionSet.size()+solutionSet.size()<populationSize+1; kk++){
//              while (offSpringSolutionSet.size() < populationSize){        // loop condition: are lambda better points already generated?
//                    int j = 0;
                    if (localDebug) System.out.println("a"+solutionSet.size());
//                    SolutionSet parentsA = new SolutionSet(BFNW.TOURNAMENTS_ROUNDS);
//                    SolutionSet parentsB = new SolutionSet(BFNW.TOURNAMENTS_ROUNDS);
                    
                    
                    
//                    do {
//                        j++;
                        parents[0] = (Solution) selectionOperator.execute(solutionSet); // carefull! the operator may work on the fitness values (which we did not really have in the beginning)
//                        parentsA.add(parents[0]);
//                    } while (j < BFNW.TOURNAMENTS_ROUNDS); // do-while                  // alternatively: choose a point at random and mutate that
//
//                    int k = 0;
//                    do {
//                        k++;
                        parents[1] = (Solution) selectionOperator.execute(solutionSet);
//                        parents[1] = (Solution) SelectionFactory.getSelectionOperator("RandomSelection").execute(solutionSet);
//                        parentsB.add(parents[0]);
//                    } while (k < BFNW.TOURNAMENTS_ROUNDS); // do-while


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

                    
                    
                    
                    
///*does not seem to be beneficial: ignore offspring if dominated by the current population*/
//                    boolean newPointIsDominatedByOldPopulation = false;
//                    for (int i = 0; i<solutionSet.size(); i++) {
//                            /*
//                            * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
//                            *          non-dominated, or solution1 is dominated by solution2, respectively.
//                            */
//                            int result = cNormal.compare(solutionSet.get(i), offSpring[0]);
//                            // break if once dominated
//                            if (result==-1) {
//                                newPointIsDominatedByOldPopulation = true;
//                                break;
//                            }
//                            /*
//                            * the following 4 lines have the potential to cause a population to contain fewer than mu individuals
//                            */
//                            // remove populationpoint if new point dominates that one
////                            if (result==1) {
////                                solutionSet.remove(i);
////                                if (localDebug) System.out.println("r"+solutionSet.size());
////                                i--; // reduce i as we have just modified the list
////                            }
//                        }
//                    if (newPointIsDominatedByOldPopulation) continue; 
//////                    else {
//////                        if (!newPointIsDominatedByOldPopulation) {
//////                            // add point to archive
//////                            SolutionSet temp = new SolutionSet(1);
//////                            temp.add(offSpring[0]);
//////                            solutionSet = solutionSet.union(temp);
//////                        }
//////                    }
//                    
                    
                    
                    
                    
                    
                    /* START check if new offSpring is not (epsilon) dominated by an archive point */
                    boolean newPointIsDominatedByOldArchive = false;
                    boolean newPointIsDominatedByOldArchiveTakeNevertheless = false;
                    
                    boolean debugPrintEpsilon = false;
                    
                    for (int i = 0; i<archive.size(); i++) {
                        Solution archiveSolution = archive.get(i);
                        /*
                         * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
                         *          non-dominated, or solution1 is dominated by solution2, respectively.
                         */                        
                        int result = cNormal.compare(archiveSolution, offSpring[0]);
                        
                        
                        if (debugPrintEpsilon) System.out.println(archiveSolution.toString() + " " + offSpring[0].toString() + " -> "+result);
                        
                            
                        int offspringVsArchiveByAtMostEpsilon = Integer.MAX_VALUE;
                        if (useEpsilonBoxesArchive) {
                            // floor offspring to the "lower left corner"
                            result = cNormal.compare(archiveSolution, convertSolutionToEpsilonGridVectorFLOOR(offSpring[0], epsilonGridWidth));
                            Solution archivePointMovedAway = moveEpsilonGridVectorOnceFurtherAway(archiveSolution, epsilonGridWidth);
                            offspringVsArchiveByAtMostEpsilon = cNormal.compare(archivePointMovedAway, offSpring[0]);
                            
                            
                            if (debugPrintEpsilon) System.out.println(" " +archiveSolution.toString() + " " + convertSolutionToEpsilonGridVectorFLOOR(offSpring[0], epsilonGridWidth) + " -> "+result);
                            if (debugPrintEpsilon) System.out.println(" " +archivePointMovedAway + " " + offSpring[0] + " -> "+offspringVsArchiveByAtMostEpsilon);
                        }
                        
                        
                        if (result==-1 || result==2) {
                            // break if an archive point dominates the new point
                            newPointIsDominatedByOldArchive = true;
                            newPointIsDominatedByOldArchiveCounter++;
//                            System.out.println("dominated by i="+i+" of "+archive.size());
                            
                            if (useEpsilonBoxesArchive 
                                     && offspringVsArchiveByAtMostEpsilon==1
//                                    || offspringVsArchiveByAtMostEpsilon==0 /* 13/12/12 */
//                                    )
//                                    && !(offspringVsArchiveByAtMostEpsilon==-1)
                                    ) {
                                newPointIsDominatedByOldArchiveTakeNevertheless = true;
                                if (debugPrintEpsilon) System.out.println("newPointIsDominatedByOldArchiveTakeNevertheless");
//                                System.out.println("newPointIsDominatedByOldArchiveTakeNevertheless");
                            }
                            
                            break;
                        }
                        if (result==1) {
                            // remove archive point if new point dominates that one
//                            System.out.println("remove i="+i+" of "+archive.size());
                            archive.remove(i);
                            i--;
                        }
                    }
                    /* END check if new offSpring is not (epsilon) dominated by an archive point */

                    
                    
                    /* the following can be done unconditionally, as we would have 
                       - stopped if offSpring[0] is dominated by the current population
                       - 
                     */
                    if (newPointIsDominatedByOldArchive) {
                        // in case of !useEpsilonBoxesArchive: forget this point
                        
                        if (/*useEpsilonBoxesArchive &&*/ newPointIsDominatedByOldArchiveTakeNevertheless) {
                            // use if the domination by the archive is not too bad (within the epsilon box)
                            offSpringSolutionSet.add(offSpring[0]);
                            newPointIsDominatedByOldArchiveTakeNeverthelessCounter++;
                        }
                        
                        continue;
                        
                    } else {
                        offSpringSolutionSet.add(offSpring[0]);

                        if (useEpsilonBoxesArchive) {
                            offSpring[0] = convertSolutionToEpsilonGridVectorFLOOR(offSpring[0], epsilonGridWidth);
                        }
                        
                        SolutionSet temp = new SolutionSet(1);
                        temp.add(offSpring[0]);
                        archive = archive.union(temp);
                    }  
                    
                    
                    
                    
                    
                    
//   /*21 Nov*/                 boolean newPointFLOOREDIsDominatedByOldArchive = false; // if false in the end: then this point can be kept in the population
                    
//                if (useEpsilonBoxesArchive) {
//                        Solution converted = convertSolutionToEpsilonGridVectorFLOOR(offSpring[0], epsilonGridWidth);
//                        for (int i = 0; i<archive.size(); i++) {
//                            /*
//                            * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
//                            *          non-dominated, or solution1 is dominated by solution2, respectively.
//                            */
//                            int result = cNormal.compare(archive.get(i), converted);
//                            if (result==-1 || result==2) {
//                                // break if an archive point dominates the new point, or if the new point is not new
//                                newPointFLOOREDIsDominatedByOldArchive = true;
//                                break;
//                            }
//                            if (result==1) {
//                                // remove archive point if new point dominates that one
//                                archive.remove(i);
////                                System.out.println("removed "+i);
//                                i--;
//                            }
//                        }
//                }
                    
                
                
                    
//       /*21 Nov*/             if (useEpsilonBoxesArchive) {
//                        if (!newPointFLOOREDIsDominatedByOldArchive) {// add point to archive
//                            Solution converted = convertSolutionToEpsilonGridVectorFLOOR(offSpring[0], epsilonGridWidth);
//                            
//                            //new: add archive point to archive immediately!
////                            offSpringSolutionSetForArchive.add(converted);
//                            SolutionSet temp = new SolutionSet(1);
//                            temp.add(converted);
//                            archive = archive.union(temp);
////                            System.out.println(evaluations+ " " + archive.size());
//                        } 
//                        offSpringSolutionSet.add(offSpring[0]);
////                        if (!newPointIsDominatedByOldArchive) {
////                            offSpringSolutionSetForArchive.add(convertSolutionToEpsilonGridVectorFLOOR(offSpring[0], epsilonGridWidth)); // if useEpsilonBoxesArchive then add to archive if not dominated
////                        } else {
////                            continue;
////                        }
//                    } else 
//                    
//                    
//                    
//                    {
//
//                        // define behavior: add offspring to archive
//                        if (newPointIsDominatedByOldArchive) {
//                            // forget this point
//                            continue;
//                        } else {
//                            offSpringSolutionSet.add(offSpring[0]);
//                            
//                            
//           //       offSpringSolutionSetForArchive.add(offSpring[0]);
//                            
//                        }   
//                    
//                    }
                    
                    
                    
//  OLD BLOCK, CONTAINS A PROBLEM?                    
//                    /* START check if new offSpring is not (epsilon) dominated by a population point */
//                    boolean newPointIsDominatedByOldPopulation = false;
//                    
////                    if (forceInsertion==false) {
//                        for (int i = 0; i<solutionSet.size(); i++) {
//                            /*
//                            * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
//                            *          non-dominated, or solution1 is dominated by solution2, respectively.
//                            */
//                            int result = cNormal.compare(solutionSet.get(i), offSpring[0]);
//                            // break if once dominated
//                            if (result==-1) {
//                                newPointIsDominatedByOldPopulation = true;
//                                break;
//                            }
//                            /*
//                            * the following 4 lines have the potential to cause a population to contain fewer than mu individuals
//                            */
//                            // remove populationpoint if new point dominates that one
//    //                        if (result==1) {
//    //                            solutionSet.remove(i);
//    //                            if (localDebug) System.out.println("r"+solutionSet.size());
//    //                            i--; // reduce i as we have just modified the list
//    //                        }
//                        }
////                    }
//                    if (forceInsertion) {
//                        //nop
//                    } else {
//                        if (newPointIsDominatedByOldPopulation) {
//                            // just forget this point
//                            continue;
//                        } else {
//
//                        }
//                    }
//                    
//                    /* END check if new offSpring is not epsilon dominated by a population point */
//
//
//                    /* START check if new offSpring is not (epsilon) dominated by an archive point */
//                    boolean newPointIsDominatedByOldArchive = false;
//                    for (int i = 0; i<archive.size(); i++) {
//                        /*
//                         * COMPARE: return -1, or 0, or 1 if solution1 dominates solution2, both are
//                         *          non-dominated, or solution1 is dominated by solution2, respectively.
//                         */
//                        int result = cNormal.compare(archive.get(i), offSpring[0]);
//                        if (result==-1) {
//                            // break if an archive point dominates the new point
//                            newPointIsDominatedByOldArchive = true;
//                            break;
//                        }
//                        if (result==1) {
//                            // remove archive point if new point dominates that one
//                            archive.remove(i);
//                            i--;
//                        }
//                    }
//                    /* END check if new offSpring is not epsilon dominated by an archive point */
//
//                    // define behavior: add offspring to archive
//                    
//                    if (forceInsertion) {
//                        offSpringSolutionSet.add(offSpring[0]);
//                        offSpringSolutionSetForArchive.add(offSpring[0]);
//                    } else {
//                        if (newPointIsDominatedByOldArchive) {
//                            // forget this point
//                            continue;
//                        } else {
//                            offSpringSolutionSet.add(offSpring[0]);
//                            offSpringSolutionSetForArchive.add(offSpring[0]);
//                        }
//                    }
                    
                    
                    
                    
              }
            /*END generate lambda invididuals*/

//System.out.print("post offspring generation "+offSpringSolutionSet.size()+" "+offSpringSolutionSetForArchive.size());
   
   
   
   
   
   
   
   
   
   /*16 nov 4.23pm*/
//     Ranking r = null;
//try {
//            if (offSpringSolutionSetForArchive.size()>0) {
//                
////                System.out.println("offSpringSolutionSetForArchive.size():"+offSpringSolutionSetForArchive.size());
////16 Nov not necessary with immediate addition                
//                r = new Ranking(offSpringSolutionSetForArchive);
//                offSpringSolutionSetForArchive = r.getSubfront(0);
//                
////                r = new Ranking(offSpringSolutionSet);
////                offSpringSolutionSet = r.getSubfront(0);
//            } 
//            if (offSpringSolutionSet.size()>0) {
//                r = new Ranking(offSpringSolutionSet);
//                offSpringSolutionSet = r.getSubfront(0);
////                offSpringSolutionSetForArchive = offSpringSolutionSet;
////                offSpringSolutionSet = offSpringSolutionSetForArchive; //? why did I do this?
//            }
//            if (offSpringSolutionSet.size()==0 && offSpringSolutionSetForArchive.size()==0) {
//                continue; //nothing to do, as no new interesting points were found
//            }
//            
//                        } catch (Exception e) {
//System.out.println(e.fillInStackTrace());
//System.exit(1);
//}
   

/*BACKUP*/
//   Ranking r = null;
//try {
//            
//    
//            if (offSpringSolutionSetForArchive.size()>0) {
//                r = new Ranking(offSpringSolutionSetForArchive);
////                System.out.println("offSpringSolutionSetForArchive.size():"+offSpringSolutionSetForArchive.size());
//                offSpringSolutionSetForArchive = r.getSubfront(0);
//                offSpringSolutionSet = r.getSubfront(0);
//            } else {
//                offSpringSolutionSet = offSpringSolutionSetForArchive;
//            }
//            
//                        } catch (Exception e) {
//System.out.println(e.fillInStackTrace());
//System.exit(1);
//}

   
   
   
   
   
   
   
   
            /* technically important: add all non-dominated points to the archive. */
//16 Nov not necessary with immediate addition            
//if (!useEpsilonBoxesArchive) archive = archive.union(offSpringSolutionSetForArchive);

/* merge population with offSpringSolutionSet */
            solutionSet = solutionSet.union(offSpringSolutionSet);              // would it be neccessary to take just the first subfront?

            
            
            
//System.out.println(" "+solutionSet.size()+" "+archive.size());
            
            
            
//            r = new Ranking(solutionSet);solutionSet = r.getSubfront(0);

            /* START select mu auf of mu+lambda */
//            boolean debugPrintDifferentSelectionImplementations = false;
//            if (debugPrintDifferentSelectionImplementations) System.out.println("-current      : "+BFNW.computeApproximation(solutionSet, archive)[0]
//                    +"     "+solutionSet.size());
//            SolutionSet solutionSetImprovedSpeed = (SolutionSet)DeepCopy.copy(solutionSet);
//            long startTimeImprovedSpeed = System.nanoTime();
            if (debugEarlyBreak) System.out.println("d rp "+solutionSet.size()+ " " + archive.size() + " " + populationSize);
            reducePopulationToSize(solutionSet, archive, populationSize);
            if (debugEarlyBreak) System.out.println("d rp "+solutionSet.size()+ " " + archive.size() + " " + populationSize);
//            long stopTimeImprovedSpeed = System.nanoTime();
//            if (debugPrintDifferentSelectionImplementations) System.out.println("-imp selection: app="+BFNW.computeApproximation(solutionSetImprovedSpeed, archive)[0]
//                    +" "+ Math.round((stopTimeImprovedSpeed - startTimeImprovedSpeed)/1000000d) +"ms "+solutionSetImprovedSpeed.size());
//            solutionSet = solutionSetImprovedSpeed;
            /* END select mu auf of mu+lambda */

            
            
            if (evaluations%10000==5000) System.out.println(evaluations+": newPointIsDominatedByOldArchiveCounter="+newPointIsDominatedByOldArchiveCounter + 
                " newPointIsDominatedByOldArchiveTakeNeverthelessCounter="+newPointIsDominatedByOldArchiveTakeNeverthelessCounter);
            
        } // end of main loop

        // preparation of the result of the optimization process
//        Ranking ranking = new Ranking(solutionSet);
//        Ranking ranking = new Ranking(archive);                               // returning the archive may be regarded as unfair, due to the enourmous size
//        return ranking.getSubfront(0);

        if (doOnMPICluster) {
            infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true);
            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, true); //correct line
//            infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, true, false);
        }

        
        if (true) { 
            for (int i=0; i<1; i++) {System.out.print("sample archive point: ");
//            for (int i=0; i<archive.size(); i++) {
                Solution s = archive.get(i);
                for (int j = 0; j<s.numberOfObjectives(); j++) {
                    System.out.print(s.getObjective(j)+" ");
                }
                System.out.println();
            }
        }
        
        System.out.println("newPointIsDominatedByOldArchiveCounter="+newPointIsDominatedByOldArchiveCounter + 
                " newPointIsDominatedByOldArchiveTakeNeverthelessCounter="+newPointIsDominatedByOldArchiveTakeNeverthelessCounter);
        
System.out.println(" #### IJCAI 2013 Version ####");




































        if (!useEpsilonBoxesArchive) { 
            System.out.println("this is AGE2_offline (reduction of archive to the preferred region AFTER the runtime)");
        } else System.exit(1);
        /* oAGE_online is */
        
        /* pAGE_offline:
        1. cut it down so that only the preferred regions are left
        2. reduce it with reducePopulationToSize so that only the best populationSize many are left for that reduced set
        Note that the population effectively only plays the role of growing the archive.
        */
        // 1. step
        if (!useEpsilonBoxesArchive) {
            boolean debugPageOffline = true;
            // 2. step
            if (debugPageOffline) System.out.println("DeepCopy done");
            solutionSet = (SolutionSet)DeepCopy.copy(archive);
            if (debugPageOffline) {
                System.out.println("old solutionSet (based on archive): size="+solutionSet.size());
                //printDouble2DArray(solutionSet.writeObjectivesToMatrix());
            }
            reducePopulationToSize(solutionSet, archive, 240);
            if (debugPageOffline) {
                System.out.println("new solutionSet (reduced to size): size="+solutionSet.size());
                //printDouble2DArray(solutionSet.writeObjectivesToMatrix());
            }
        } // XXXX
        
        
        
        
        //print objective values
        System.out.println("solutionSet: size="+solutionSet.size());
//        printDouble2DArray(solutionSet.writeObjectivesToMatrix());
//        System.out.println("archive: size="+archive.size());
//        printDouble2DArray(archive.writeObjectivesToMatrix());

// write results to file
        infoPrinter.printLotsOfValuesToFile(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, true);
        infoPrinter.printLotsOfValues(this, problem_, operators_, inputParameters_, evaluations, solutionSet, archive, indicators, false, true);

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
//            if (Math.abs(temp) > delta) delta = Math.abs(temp);// consider absolute distance instead of additive approximation: does not converge with higher dimension
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
        
        if (population.size()==1) return;
        
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
//    // the following is based on computeApproximation/2
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


    
    
public static void arrayJobs(){
    // target:
    // ./bfnw.sh MaF08_5D MaF08PF_M5.txt 50 100000 BinaryTournament2 0 foo
    String[] problems = new String[]{"MaF01_5D","MaF02_5D","MaF03_5D","MaF04_5D","MaF05_5D","MaF06_5D","MaF07_5D","MaF08_5D","MaF09_5D","MaF10_5D","MaF11_5D","MaF12_5D","MaF13_5D","MaF14_5D","MaF15_5D",
        "MaF01_10D","MaF02_10D","MaF03_10D","MaF04_10D","MaF05_10D","MaF06_10D","MaF07_10D","MaF08_10D","MaF09_10D","MaF10_10D","MaF11_10D","MaF12_10D","MaF13_10D","MaF14_10D","MaF15_10D",
        "MaF01_15D","MaF02_15D","MaF03_15D","MaF04_15D","MaF05_15D","MaF06_15D","MaF07_15D","MaF08_15D","MaF09_15D","MaF10_15D","MaF11_15D","MaF12_15D","MaF13_15D","MaF14_15D","MaF15_15D"};
    String[] pffiles = new String[]{"MaF01PF_M5.txt","MaF02PF_M5.txt","MaF03PF_M5.txt","MaF04PF_M5.txt","MaF05PF_M5.txt","MaF06PF_M5.txt","MaF07PF_M5.txt","MaF08PF_M5.txt","MaF09PF_M5.txt","MaF10PF_M5.txt","MaF11PF_M5.txt","MaF12PF_M5.txt","MaF13PF_M5.txt","MaF14PF_M5.txt","MaF15PF_M5.txt",
        "MaF01PF_M10.txt","MaF02PF_M10.txt","MaF03PF_M10.txt","MaF04PF_M10.txt","MaF05PF_M10.txt","MaF06PF_M10.txt","MaF07PF_M10.txt","MaF08PF_M10.txt","MaF09PF_M10.txt","MaF10PF_M10.txt","MaF11PF_M10.txt","MaF12PF_M10.txt","MaF13PF_M10.txt","MaF14PF_M10.txt","MaF15PF_M10.txt",
        "MaF01PF_M15.txt","MaF02PF_M15.txt","MaF03PF_M15.txt","MaF04PF_M15.txt","MaF05PF_M15.txt","MaF06PF_M15.txt","MaF07PF_M15.txt","MaF08PF_M15.txt","MaF09PF_M15.txt","MaF10PF_M15.txt","MaF11PF_M15.txt","MaF12PF_M15.txt","MaF13PF_M15.txt","MaF14PF_M15.txt","MaF15PF_M15.txt"};
    int[] evals =new int[]{100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 
    100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 100000, 
    150000, 150000, 150000, 150000, 150000, 150000, 150000, 150000, 150000, 150000, 150000, 150000, 150000, 150000, 150000};
    
    int reps = 1;
    int mu=3200;
    
   
    for (int i=0; i<problems.length; i++) {
        for (int r=0; r<reps; r++) {
            System.out.println("./bfnw.sh "+problems[i]+" "+pffiles[i]+" "+mu+" "+evals[i]+" BinaryTournament2 0 cec2018.mu"+mu+"."+problems[i]);
        }
    }
    
    System.out.println("stack test");
    StackTraceElement[] s = new Throwable().getStackTrace();
    for (int i=0; i<s.length; i++) {
        System.out.println(s[i]+", method contains test="+ (s[i].getMethodName().contains("test")) );
    }
    System.out.println("stack test end ");
    
    System.exit(1);
}
    
    /** Minimal function to quickly execute the algorithm
     * @param args
     */
    public static void main(String[] args) {
        arrayJobs();
        try {
            if (args.length == 0) {


                File test = new File(".");
                System.out.println(test.getAbsolutePath());


//            BFWN_main.main(new String[]{"DTLZ7","originalParetoFronts\\DTLZ7.3D.pf","10", "true", "true", "0.01"});
//            BFWN_main.main(new String[]{"WFG1", "originalParetoFronts\\WFG1.2D.pf", "100", "true", "true", "0.001", "false","15000","BinaryTournament","foo"});
//            BFWN_main.main(new String[]{"DTLZ1_3D", "originalParetoFronts\\DTLZ1.3D.pf", "50", "true", "true", "0", "false","100000","BinaryTournament2","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"WFG1_2D", "originalParetoFronts\\WFG1.2D.pf", "10", "true", "true", "0", "false","25000","BinaryTournament2","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ4_10D", "originalParetoFronts\\DTLZ2.10D.1000.pf", "50", "true", "true", "0.01", "false","100000","RandomSelection","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ4_3D", "originalParetoFronts\\DTLZ2.3D.10000.pf", "100", "true", "true", "0.01", "false","1000000","RandomSelection","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//                BFWN_main.main(new String[]{"DTLZ4_3D", "originalParetoFronts\\DTLZ2.3D.10000.pf", "25", "true", "true", "0.01", "false","100000","RandomSelection","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ4_12D", "originalParetoFronts\\DTLZ2.12D.10000.pf", "100", "true", "true", "0.01", "false","10000","RandomSelection","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"WFG1_3D", "originalParetoFronts\\WFG1.3D.pf", "50", "true", "true", "0.01", "false","100000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ2_10D", "originalParetoFronts\\DTLZ2.10D.10000.pf", "100", "true", "true", "0", "false","20000","BinaryTournament2","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ1_12D", "originalParetoFronts\\DTLZ1.12D.1000.pf", "10", "true", "true", "0.01", "true","15000","BinaryTournament","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//            BFWN_main.main(new String[]{"DTLZ1_4D", "originalParetoFronts\\DTLZ1.4D.10000.pf", "50", "true", "true", "0", "false","25000","BinaryTournament2","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
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
//              BFWN_main.main(new String[]{"DTLZ4_4D", "originalParetoFronts\\DTLZ2.4D.1000.pf", "100", "true", "true", "0.01", "true","100000","RandomSelection","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//              BFWN_main.mainORIG(new String[]{"DTLZ4_6D", "originalParetoFronts\\DTLZ2.6D.1000.pf", "100", "true", "true", "0.01", "false","100000","BinaryTournament3","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//              BFWN_main.main(new String[]{"DTLZ3_3D", "originalParetoFronts\\DTLZ2.3D.1000.pf", "100", "true", "true", "0.01", "false","100000","BinaryTournament2","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation
//              BFWN_main.main(new String[]{"DTLZ4_3D", "originalParetoFronts\\DTLZ2.3D.1000.pf", "100", "true", "true", "0.01", "false","100000","RandomSelection","foo"});   //problem, Pareto front, pop size, doCrossover, doMutation

//BFWN_main.main(new String[]{"MaF01_5D", "originalParetoFronts\\MaF01PF_M5.txt", "50", "true", "true", "0", "false","25000","BinaryTournament2","fooCEC2018"});
BFWN_main.main(new String[]{"MaF03_5D", "originalParetoFronts\\MaF03PF_M5.txt", "50", "true", "true", "0", "false","35000","BinaryTournament2","fooCEC2018"});
//BFWN_main.main(new String[]{"MaF08_5D", "originalParetoFronts\\MaF08PF_M5.txt", "50", "true", "true", "0", "false","50000","BinaryTournament2","fooCEC2018"});
//BFWN_main.main(new String[]{"MaF13_5D", "originalParetoFronts\\MaF13PF_M5.txt", "50", "true", "true", "0", "false","5000","BinaryTournament2","fooCEC2018"});
//BFWN_main.main(new String[]{"MaF15_10D", "originalParetoFronts\\MaF15PF_M10.txt", "50", "true", "true", "0", "false","15000","BinaryTournament2","fooCEC2018"});
            }
            else if (args.length == 7) {
                // in case it is called via the commandline with parameters...
                BFWN_main.mainORIG(new String[]{
                            args[0], // problem
                            "originalParetoFronts/" + args[1], // pf-file, without the directory
                            args[2], // mu
                            "true",  // doXO
                            "true",  // doMUT
                            args[5], // epsilonGridWidth
                            "false",  // do outputs for runs on MPI cluster
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