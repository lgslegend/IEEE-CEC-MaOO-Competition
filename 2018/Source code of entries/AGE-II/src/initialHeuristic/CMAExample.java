package initialHeuristic;
import fr.inria.optimization.cmaes.examples.*;
import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;
import jmetal.base.variable.Real;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

class Rosenbrock implements IObjectiveFunction { // meaning implements methods valueOf and isFeasible
	public double valueOf (double[] x) {
            	return 0;
	}
	public boolean isFeasible(double[] x) {
//      DTLZ 1-4: 
//      lowerLimit_[var] = 0.0;
//      upperLimit_[var] = 1.0;
            boolean result = true;
            
            double lowerLimit = 0.0;
            double upperLimit = 1.0;
            
            for (int i=0; i<x.length; i++) {
                if (x[i]<lowerLimit || x[i]>upperLimit) {
                    result = false;
                    break;
                }
            }
            
            return result; 
        } // entire R^n is feasible
}




public class CMAExample {
    
public static String[] D = new String[]{

};

public static void main(String[] args)  throws Exception {
    // DTLZ 
//    int[] indices = new int[]{3,4};
//    int[] indices = new int[]{2,3,4,5,6,8,10,12,14,16,18,20};
//    for (int i=0; i<indices.length; i++) {
//        mainDimensions(new String[]{"DTLZ1_"+indices[i]+"D"});
//        mainDimensions(new String[]{"DTLZ2_"+indices[i]+"D"});
//        mainDimensions(new String[]{"DTLZ3_"+indices[i]+"D"});
//        mainDimensions(new String[]{"DTLZ4_"+indices[i]+"D"});
//    }
    
//    //LZ09 
//    int[] indices = new int[]{1,2,3,4,5,6,7,8,9};
//    for (int i=0; i<indices.length; i++) {
//        mainDimensions(new String[]{"LZ09_F"+indices[i]});
//    }
    
//    //WFG
//    int[] indices = new int[]{1,2,3,4,5,6,7,8,9};
//    for (int i=0; i<indices.length; i++) {
//        mainDimensions(new String[]{"WFG"+indices[i]+"_2D"});//WFG4_2D
//        mainDimensions(new String[]{"WFG"+indices[i]+"_3D"});//WFG4_3D
//    }
    
    //ZDT
    mainDimensions(new String[]{"ZDT1"});//ZDT1
    mainDimensions(new String[]{"ZDT2"});
    mainDimensions(new String[]{"ZDT3"});
    mainDimensions(new String[]{"ZDT4"});
    mainDimensions(new String[]{"ZDT6"});
}

/* corners and centre:
 * - target: d+1
 * - cma.options.stopMaxFunEvals: 10000/(d+1)
 * - change filename!
 * - change "switch" below
 */





public static void mainDimensions(String[] args)  throws Exception {
        
        boolean debugPrint = true;
        
        boolean doTheComputations = true;
        
        if (args.length==0) {
                args = new String[]{"LZ09_F1"};
//                args = new String[]{"DTLZ1_2D"};
                System.exit(1);
//                args = new String[]{"DTLZ4_2D","0.0","1.0"};
        }
        
        
        int dimensions = (new ProblemFactory()).getProblem(args[0],new Object[]{"Real"}).getNumberOfObjectives();
//        int dimensions = Integer.parseInt( args[0].substring(  args[0].indexOf("_")+1, args[0].length()-1 ));
        
        
//        int target = 100; // old approach: 100 solutions
//        int target = 10; // new approach: 10 solutions (2000 evaluations each
        int target = dimensions+1; // new approach: d + 1, corners + centre
        
        Solution[] solutions = new Solution[target];
        
        int count = 0;
        
        int[][] wa = null; //FIX
        
        switch (dimensions) {
            case 2: wa=CornersAndCentre.wa2D;break;
            case 3: wa=CornersAndCentre.wa3D;break;
            case 4: wa=CornersAndCentre.wa4D;break;
            case 5: wa=CornersAndCentre.wa5D;break;
            case 6: wa=CornersAndCentre.wa6D;break;
            case 8: wa=CornersAndCentre.wa8D;break;
            case 10: wa=CornersAndCentre.wa10D;break;
            case 12: wa=CornersAndCentre.wa12D;break;
            case 14: wa=CornersAndCentre.wa14D;break;
            case 16: wa=CornersAndCentre.wa16D;break;
            case 18: wa=CornersAndCentre.wa18D;break;
            case 20: wa=CornersAndCentre.wa20D;break;
//            case 2: wa=LinearCombinations.wa2D;break;
//            case 3: wa=LinearCombinations.wa3D;break;
//            case 4: wa=LinearCombinations.wa4D;break;
//            case 5: wa=LinearCombinations.wa5D;break;
//            case 6: wa=LinearCombinations.wa6D;break;
//            case 8: wa=LinearCombinations.wa8D;break;
//            case 10: wa=LinearCombinations.wa10D;break;
//            case 12: wa=LinearCombinations.wa12D;break;
//            case 14: wa=LinearCombinations.wa14D;break;
//            case 16: wa=LinearCombinations.wa16D;break;
//            case 18: wa=LinearCombinations.wa18D;break;
//            case 20: wa=LinearCombinations.wa20D;break;
            default: System.out.println("incorrect number of dimensions");System.exit(1);
        }    
        
        for (int i = 0; i<target; i++) {
        
                
                String[] temp = new String[dimensions+1];
                temp[0] = args[0];
                for (int z=0; z<dimensions; z++) {
//System.out.println("% "+Arrays.toString(temp));
//System.out.println("% "+Arrays.toString(wa[i]));
                    temp[z+1] = wa[i][z]+"";
                }
                args = temp;

                
                if (debugPrint) System.out.println("current ("+i+"): +"+Arrays.toString(args));
                
                if (doTheComputations) solutions[i] = mainMinistudy(args)[0];
           
        }
                
        if (doTheComputations) {
            System.out.println("objective vectors for LINEAR COMINATIONS");
            for (int i=0; i<solutions.length; i++) {
                printDecisionVariable(solutions[i]);
            }
            
            
            SolutionSet s = new SolutionSet(target);
            for (int i=0; i<solutions.length; i++) {
                s.add(solutions[i]);
            }
            s.printVariablesToFile(args[0]+".cornersAndCentre.pop");
//            s.printVariablesToFile(args[0]+".linearCombos1kEvalsEach.pop");
        }
        
        
        
        
        
    }
    
    
    public static void mainGENERATING(String[] args)  throws Exception {
        
        boolean debugPrint = true;
        
        boolean doTheComputations = !true;
        
        if (args.length==0) {
                args = new String[]{"DTLZ1_2D"};
//                args = new String[]{"DTLZ4_2D","0.0","1.0"};
        }
        
        int dimensions = (new ProblemFactory()).getProblem(args[0],new Object[]{"Real"}).getNumberOfObjectives();
//        int dimensions = Integer.parseInt( args[0].substring(  args[0].indexOf("_")+1, args[0].length()-1 ));
        int[] weights = new int[dimensions];
        
        
//        int target = 100; // old approach: 100 linear combinations
        int target = dimensions+1; // new approach: d corners + centre
        
        
        Solution[] solutions = new Solution[target];
        
        int count = 0;
        
//        for (int i = 1; true; i++) {
        weights[0] = 1;
        while (true) {
            
            boolean breakflag = false;
            
            
//            for (int j = 0; j<=weights[0]; j++) {
            for (weights[1] = 0; weights[1] <=weights[0]; weights[1]++) {
                
//                args = new String[]{args[0],weights[0]+"",weights[1]+""};
                
                String[] temp = new String[dimensions+1];
                temp[0] = args[0];
                for (int z=0; z<weights.length; z++) temp[z+1] = weights[z]+"";
                args = temp;

                
                if (debugPrint) System.out.println("current ("+count+"): +"+Arrays.toString(args));
                
                if (doTheComputations) solutions[count] = mainMinistudy(args)[0];
                count++;
                
                if (count>=target) {
                    breakflag = true;
                    break;
                }
            }
            
            if (breakflag) break;
            
            for (weights[1] = weights[0]-1; weights[1]>=0; weights[1]--) {
                
                args = new String[]{args[0],weights[1]+"",weights[0]+""};
                
                if (debugPrint) System.out.println("current: -"+Arrays.toString(args));
                
                if (doTheComputations) solutions[count] = mainMinistudy(args)[0];
                count++;
                
                if (count>=target) {
                    breakflag = true;
                    break;
                }
            }
            
            if (breakflag) break;
            
            weights[0]++;
        }
        
        if (doTheComputations) {
            System.out.println("objective vectors for LINEAR COMINATIONS");
            for (int i=0; i<solutions.length; i++) {
                printDecisionVariable(solutions[i]);
            }
        }
        
        
    }
    
    public static Solution[] mainMinistudy(String args[]) throws Exception {
        int repetitions = 1;
        
        if (args.length==0) {
                args = new String[]{"DTLZ1_2D","0.0","1.0"};
            }
        
        int dimensions = (new ProblemFactory()).getProblem(args[0],new Object[]{"Real"}).getNumberOfObjectives();
//        int dimensions = Integer.parseInt( args[0].substring(  args[0].indexOf("_")+1, args[0].length()-1 ));
        
        Solution[] solutions = new Solution[repetitions];
        for (int i=0; i<repetitions; i++) {
            System.out.println("miniStudy iteration "+i);
            solutions[i] = mainOLD(args);
        }
        System.out.println("miniStudy: "+Arrays.toString(args));
        double[][] results = new double[dimensions][repetitions]; 
        for(int i=0; i<repetitions; i++) {
            Solution sTemp = solutions[i];
            for (int d=0; d<dimensions; d++) {
                results[d][i] = sTemp.getObjective(d);
            }
            printSolution(sTemp);
        }
        
        System.out.println("miniStudy: "+Arrays.toString(args));
        double[] means = new double[dimensions];
        for(int i=0; i<repetitions; i++) {
            for (int d=0; d<dimensions; d++) {
                means[d] += results[d][i];
            }
        }
        for (int d=0; d<dimensions; d++) {
            means[d] = means[d] / repetitions;
        }
        System.out.println("means: "+Arrays.toString(means));
        
        System.out.println("objective vectors");
        for (int i=0; i<solutions.length; i++) {
            printDecisionVariable(solutions[i]);
        }
        
        return solutions;
    }
    
    public static double calculateWeightedFitness(Solution s, String[] args) {
        double result = 0;
        try {    
            int dimensions = (new ProblemFactory()).getProblem(args[0],new Object[]{"Real"}).getNumberOfObjectives();
    //        int dimensions = Integer.parseInt( args[0].substring(  args[0].indexOf("_")+1, args[0].length()-1 ));
    //        int dimensions = Integer.parseInt(args[1]);
            
            for (int i=1; i<1+dimensions; i++) {
                result = result + Double.parseDouble(args[i]) * s.getObjective(i-1);
            }
        } catch (JMException ex) {
            Logger.getLogger(CMAExample.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    
	public static Solution mainOLD(String[] args) throws JMException, ClassNotFoundException {
	
            boolean debugPrint = true;
            
            IObjectiveFunction fitfun = new Rosenbrock();
            
            int dimensions = (new ProblemFactory()).getProblem(args[0],new Object[]{"Real"}).getNumberOfObjectives();
//            int dimensions = Integer.parseInt( args[0].substring(  args[0].indexOf("_")+1, args[0].length()-1 ));
            if (debugPrint) System.out.println(args[0]+":"+dimensions);
            
            Object [] params = {"Real"};
            Problem problem = (new ProblemFactory()).getProblem(args[0],params);
            
            

            // new a CMA-ES and set some initial values
            CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
//		cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
            
//            int inputs = 30;
            int inputs = (new ProblemFactory()).getProblem(args[0],new Object[]{"Real"}).getNumberOfVariables();
            
            cma.setDimension(inputs); // overwrite some loaded properties
            cma.setInitialX(0.5); // in each dimension, also setTypicalX can be used
            cma.setInitialStandardDeviation(0.1); // also a mandatory setting 

            
            
//            cma.parameters.setMu(2);cma.parameters.setPopulationSize(5);
            cma.parameters.setMu(2);cma.parameters.setPopulationSize(4);
            
            cma.options.stopFitness = 1e-14;       // optional setting
//            cma.options.stopMaxIter = 2000;
//            cma.options.stopMaxFunEvals = 1000000;  // old approach: until convergence
//            cma.options.stopMaxFunEvals = 1000;  // new approach: only 100 per solution
            cma.options.stopMaxFunEvals = 10000 / (dimensions+1);  // new approach: do until 10.000/d+1 per solution reached

            // initialize cma and get fitness array to fill in later
            double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];
            
            // initial output to files
            cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files

            // iteration loop
            while(cma.stopConditions.getNumber() == 0) {
//            int generations = 100000;while(cma.getCountIter()<generations ) {
                if (debugPrint && cma.getCountIter()%50==0) System.out.println("generation: "+cma.getCountIter());

                // --- core iteration step ---
                double[][] pop = cma.samplePopulation(); // get a new population of solutions
                for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                    // a simple way to handle constraints that define a convex feasible domain  
                    // (like box constraints, i.e. variable boundaries) via "blind re-sampling" 
                    // assumes that the feasible domain is convex, the optimum is  
                    while (!fitfun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,  
                        pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are  
                                                            //   sufficiently small to prevent quasi-infinite looping here
                    
                    
                    
                    // compute fitness/objective value	
                    
                    // convert double[] to jmetal.base.Solution
                    Solution sTemp = new Solution(problem);
                    sTemp.setDecisionVariables(doubleArrayToRealArray(pop[i]));
                    problem.evaluate(sTemp);
                    fitness[i] = calculateWeightedFitness(sTemp, args);
//                    fitness[i] = sTemp.getObjective(0) + sTemp.getObjective(1);
                    
                    
                    
//                    if (debugPrint && cma.getCountIter()%50==0) {
//                        printSolution(sTemp);
////                        System.out.println(Arrays.toString(pop[i])+":"+fitness[i]);
//                    }
                }
                
                
                
                
                cma.updateDistribution(fitness);         // pass fitness array to update search distribution
                // --- end core iteration step ---

                
                if (debugPrint && cma.getCountIter()%50==0) System.out.println(" "+cma.getBestSolution().getFitness());
                // output to files and console 
//                cma.writeToDefaultFiles();
//                int outmod = 150;
//                if (cma.getCountIter() % (15*outmod) == 1)
//                        cma.printlnAnnotation(); // might write file as well
//                if (cma.getCountIter() % outmod == 1)
//                        cma.println(); 
            }
            // evaluate mean value as it is the best estimator for the optimum
            
            Solution sTemp = new Solution(problem);
            sTemp.setDecisionVariables(doubleArrayToRealArray(cma.getMeanX()));
            problem.evaluate(sTemp);
            
            
//            cma.setFitnessOfMeanX(sTemp.getObjective(0)+sTemp.getObjective(1)); // updates the best ever solution 
//            cma.setFitnessOfMeanX(sTemp.getObjective(0)); // updates the best ever solution 
            cma.setFitnessOfMeanX(calculateWeightedFitness(sTemp, args)); // updates the best ever solution 
//            cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution 

            // final output
            cma.writeToDefaultFiles(1);
            cma.println();
            cma.println("Terminated due to");
            for (String s : cma.stopConditions.getMessages())
                cma.println("  " + s);
            cma.println("best function value " + cma.getBestFunctionValue() 
                        + " at evaluation " + cma.getBestEvaluationNumber());
            // we might return cma.getBestSolution() or cma.getBestX()
            
            
            sTemp = new Solution(problem);
            sTemp.setDecisionVariables(doubleArrayToRealArray(cma.getBestX()));
            problem.evaluate(sTemp);
            printSolution(sTemp);

            
            return sTemp;
    } // main  

    public static void printSolution(Solution s) {
//        Variable[] v = s.getDecisionVariables();
//        System.out.print("d: ");
//        for (int i=0; i<v.length; i++) {
//            try {
//                System.out.print(" "+v[i].getValue());
//            } catch (JMException ex) {
//                Logger.getLogger(CMAExample.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        System.out.println();
        System.out.print("o:");
        for (int i=0; i<s.numberOfObjectives(); i++) {
            System.out.print(" "+s.getObjective(i));
        }
        System.out.println();
        
    }
    
    public static void printDecisionVariable(Solution s) {
        Variable[] v = s.getDecisionVariables();
        for (int i=0; i<v.length-1; i++) {
            try {
                System.out.print(v[i].getValue()+" ");
            } catch (JMException ex) {
                Logger.getLogger(CMAExample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            System.out.println(v[v.length-1].getValue());
        } catch (JMException ex) {
            Logger.getLogger(CMAExample.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
        
    public static Real[] doubleArrayToRealArray(double[] d) {
        Real[] result = new Real[d.length];
        
        for (int i = 0; i<d.length; i++) {
            result[i] = new Real();
            result[i].setValue(d[i]);
        }
        
        return result;
    }
} 