package JSimulatedAnnealing;


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.variable.Real;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

/**
 * 
 * If you don't want to implement the whole ISimulAnnealing-Interface,
 * simply extend this class and override desired methods.
 * 
 * Methods you will have to override:
 * public Double[] move(Double[] genome);
 * public double objectiveFunction(Double[] genome);
 * 
 * @author Stefan Lattner
 * 
 * Part of the JSimulatedAnnealing-package
 * (c) 2011, www.stefanlattner.at
 *
 */
public class SADemonstration implements ISimulAnnealing<Double[]>{

	private Random random = new Random();
	private Integer steps = 0;
	// Any number
	private double desiredResult = 0;//1633.34;
	
	public static void main(String[] args) {
        try {
            new SADemonstration();
        } catch (JMException ex) {
            Logger.getLogger(SADemonstration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SADemonstration.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
        
        String[] args = new String[]{"DTLZ3_2D"};
	
        
        
        
	public SADemonstration() throws JMException, ClassNotFoundException {
		// Print question
//		System.out.println("Solve equation:");
//		System.out.println("(a^3 + b^2 + c) / d = " + desiredResult);
//		System.out.println("a = ?, b = ?, c = ?, d = ?");
		System.out.println("--------------------------");
		
		// Initialize the annealer.
		SimulAnnealing<Double[]> annealer = new SimulAnnealing<Double[]>(0.9998, 100, 1, this);
		annealer.setTerminateAtTemperatur(0.0001);
		
		// Setup initial genome
		Double[] initialGenome = new Double[30];for (int i=0; i<initialGenome.length; i++) { initialGenome[i] = new Double(Math.random()); };
//		Double[] initialGenome = new Double[]{.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,.5,};
                
                
                
                
		Double[] solution = annealer.anneal(initialGenome);
		
		// Print answer
		System.out.println("\nAny solution found:");
		Double a = solution[0];
		Double b = solution[1];
		Double c = solution[2];
		Double d = solution[3];
                printGenome(solution);
                
                
                Object [] params = {"Real"};
                Problem problem = (new ProblemFactory()).getProblem(args[0],params);

                Solution sTemp = new Solution(problem);
                sTemp.setDecisionVariables(DoubleArrayToRealArray(solution));
                problem.evaluate(sTemp);
                System.out.println("o: "+sTemp.getObjective(0) + " " + sTemp.getObjective(1));
                
                
	}


//	private double getResult(double a, double b, double c, double d) {
//		return (Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2)) / Math.pow(d, 2);
//	}
	private double getResult(Double[] d) {
//		return (Math.pow(d[0], 2) + Math.pow(d[1], 2) + Math.pow(d[2], 2)) / Math.pow(d[3], 2);
            
            boolean debugPrint=!true;
            
            double result = 0;
            
            
            
            int dimensions = Integer.parseInt( args[0].substring(  args[0].indexOf("_")+1, args[0].length()-1 ));
            if (debugPrint) System.out.println(args[0]+":"+dimensions);
            
            Object [] params = {"Real"};
            try {
                Problem problem = (new ProblemFactory()).getProblem(args[0],params);

                Solution sTemp = new Solution(problem);
                sTemp.setDecisionVariables(DoubleArrayToRealArray(d));
                problem.evaluate(sTemp);
                System.out.println("o: "+sTemp.getObjective(0) + " " + sTemp.getObjective(1));
                result= sTemp.getObjective(0) + sTemp.getObjective(1); // minimizer!
//                result=  sTemp.getObjective(1); // minimizer!
//                result=  sTemp.getObjective(0); // minimizer!
            } catch (Exception ex) {
                Logger.getLogger(SADemonstration.class.getName()).log(Level.SEVERE, null, ex);
            }

            return result;
        }
        
    public static Real[] DoubleArrayToRealArray(Double[] d) {
        Real[] result = new Real[d.length];
        
        for (int i = 0; i<d.length; i++) {
            result[i] = new Real();
            result[i].setValue(d[i]);
        }
        
        return result;
    }


	private void printGenome(Double[] genome) {
		System.out.print("[ ");
		for (Double d : genome)
			System.out.print(d + " ");
		System.out.print("]\n");
	}

	// Implemented methods of interface ISimulAnnealing
	
	/**
	 * Transforms the given genome into a neighborhood solution (slight mutation).
	 * Slightly change the genome. (e.g. up to +- 5)
	 * @param genome	The genome which will be mutated.
	 * @return			The mutated genome.
	 */
	public Double[] move(Double[] genome) {
		int upTo = 1;
		// changeBy range : [0, 5]
		double changeBy = random.nextDouble() * upTo * 0.3;
                
                if (Math.random() < 0.5) changeBy = 0.1*changeBy;
                if (Math.random() < 0.5) changeBy = 0.1*changeBy;
                if (Math.random() < 0.5) changeBy = 0.1*changeBy;
                
		// changeBy range : [-5, 5]
		if (random.nextBoolean())
			changeBy *= -1;
		// change one of the 4 parameters
                int position = random.nextInt(genome.length);
		genome[position] += changeBy;
                
                if (genome[position] > 1.0)
                    genome[position] = Math.random();
//                    genome[position] = 1.0;
                
                if (genome[position] < 0.0)
                    genome[position] = Math.random();
//                    genome[position] = 0.0;
                
		return genome;
	}

	/**
	 * 
	 * The objective function which returns a fitness value of the given genome.
	 * Evaluate parameters - give a higher number for a better solution
	 * (i.e. solution near to the desiredResult)
	 * 
	 * @param genome	The genome which will be evaluated.
	 * @return			Fitness value for the given genome.
	 *
	 */
	public double objectiveFunction(Double[] genome) {
//		double a = genome[0];
//		double b = genome[1];
//		double c = genome[2];
//		double d = genome[3];
		
		double currentResult = getResult(genome);
		
		// The distance accounts negatively to the fitness
		return currentResult;
//		return Math.abs(desiredResult - currentResult) * -1;
	}

	/**
	 * At each step the annealer will call this method to provide the current genome.
	 * @param genome	The genome at the current step.
	 */
	public void currGenomeCallback(Double[] genome) {
		this.steps++;
	}

	/**
	 * At each step the annealer will call this method to provide the current fitness.
	 * @param fitness	The fitness at the current step.
	 */
	public void currFitnessCallback(double fitness) {}

	/**
	 * If the current fitness value is beyond the last fitness value, the annealer will provide
	 * the probability on if the worse genome will be choosen. This probability depends on the
	 * current temperatur and the fitness distance from the last to the current genome.
	 * @param probability	The probability if the current, worse genome will be choosen.
	 */
	public void currProbabilityCallback(double probability) {
		// Commented out, otherwise messes up console output
		// System.out.println("Take genome with a probability of " + probability);
	}

	/**
	 * At each step the annealer will call this method to provide the current temperatur.
	 * @param temperatur
	 */
	public void currTemperaturCallback(double temperatur) {
		if (steps % 100 == 0)
			System.out.println(steps + " Current temperatur: " + temperatur);
	}

	/**
	 * Each time the genome has improved, the annealer will call this method to provide the best genome yet.
	 * @param genome
	 */
	public void bestGenomeCallback(Double[] genome) {
		System.out.print("Currently best genome: ");
		printGenome(genome);
	}

	/**
	 * Each time the genome has improved, the annealer will call this method to provide the best fitness yet.
	 * @param genome
	 */
	public void bestFitnessCallback(double fitness) {
		System.out.println("Fitness decreased to: " + fitness + "\n");
	}
	
	/**
	 * Will be called each time, the evaluated genome has been accepted.
	 */
	public void genomeAcceptedCallback(Double[] genome) {
//		System.out.print("\nGenome ");
//		printGenome(genome);
//		System.out.println("..accepted." );
	}
}