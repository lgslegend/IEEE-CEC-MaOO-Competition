package JSimulatedAnnealing;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

/**
 * This class provides a generic frame for optimizing a given genome by simulated annealing.
 * 
 * @param <T> The class of the genome
 * @author Stefan Lattner
 * 
 * Part of the JSimulatedAnnealing-package
 * (c) 2011, www.stefanlattner.at
 */
public class SimulAnnealing<T extends Serializable> {
	
	private double 				coolingRate;
	private int 				stepsBeforeCooling;
	private ISimulAnnealing<T> 	manager;

	private Random				random;
	private double 				temperatur;
	private double 				lowestTemperatur = 7.9E-90;
	private double 				bestFitness = 1000000;
	
	/**
	 * Creates an simulated annealer.
	 * @param coolingRate			Has to be in range [0,1[. Temperatur will be reduced by multiplying with this value (linear falloff).
	 * @param stepsBeforeCooling	Amount of steps without temperatur falloff at the beginning (with high temperatur -> greedy search).
	 * @param initialTemperatur		Steer the sensitivity of choosing a worse genome by this value.
	 * 								Lower values -> higher sensitivity -> less probability of the genome to be choosen.
	 * 								Note that multiplying your fitness value by a constant > 1 also increases the sensitivity. 
	 * 								If the initial temperatur is 1, a fitness value between 0 and 40 is a pretty good range. 
	 * 								Watch the probability-callback value for getting a feeling of the current sensitivity.
	 * @param manager				Class which implements the IAnnealing interface.
	 */
	public SimulAnnealing(double coolingRate, int stepsBeforeCooling, double initialTemperatur, ISimulAnnealing<T> manager) {
		if (coolingRate >= 1 || coolingRate < 0)
			throw new IllegalArgumentException("CoolingRate has to be in range [0,1[.");
		this.coolingRate 		= coolingRate;
		this.stepsBeforeCooling	= stepsBeforeCooling;
		this.manager 			= manager;
		this.random				= new Random();
		this.temperatur			= initialTemperatur;
	}
	
	/**
	 * Optimizes a given genome by simulated annealing, trying to maximize the objective Function.
	 * @param genome	The genome which has to be optimized.
	 * @return
	 */
	public T anneal(T genome){
		T bestGenome = cloneGenome(genome);
		T lastGenome = cloneGenome(genome);
		double lastFitness = getObjectiveValue(genome);
		this.bestFitness = lastFitness;
		manager.bestFitnessCallback(bestFitness);
		int step = 0;
		while (temperatur > lowestTemperatur){
			if (step == stepsBeforeCooling)
				genome = bestGenome;
			manager.currTemperaturCallback(temperatur);
			genome = manager.move(genome);
			manager.currGenomeCallback(genome);
			double currFitness = getObjectiveValue(genome);
			manager.currFitnessCallback(currFitness);
			if (allowStep(lastFitness, currFitness, temperatur)){
				lastFitness = currFitness;
				manager.genomeAcceptedCallback(genome);
			}else{				
				genome = lastGenome;
			}
			lastGenome = cloneGenome(genome);
			if (currFitness < bestFitness){
//			if (currFitness > bestFitness){
				bestFitness	= currFitness;
				bestGenome	= cloneGenome(genome);
				manager.bestFitnessCallback(bestFitness);
				manager.bestGenomeCallback(bestGenome);
			}
			decreaseTemperatur(step++);
		}
		return bestGenome;
	}
	
	private double getObjectiveValue (T genome){
		return manager.objectiveFunction(genome);
	}

	private void decreaseTemperatur(int step) {
		if (step > stepsBeforeCooling){
			temperatur *= coolingRate;
		}
	}

	private boolean allowStep(double lastFitness, double currFitness, double currTemperatur) {
		if (currFitness <= lastFitness){
//		if (currFitness >= lastFitness){
			return true;
		}
		return getprobability(lastFitness, currFitness, currTemperatur) > random.nextDouble();
	}

	private double getprobability(double lastFitness, double currFitness, double currTemperatur) {
		double prob = Math.exp(-1 * ((lastFitness - currFitness) / currTemperatur));
		manager.currProbabilityCallback(prob);
		return prob;
	}
	
	@SuppressWarnings("unchecked")
	private T cloneGenome(T genome) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
			new ObjectOutputStream(baos).writeObject(genome);
	    	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			return  (T)new ObjectInputStream(bais).readObject();
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}
	
	// Getters, Setters
	
	public double getCoolingRate() {
		return coolingRate;
	}

	/**
	 * @param coolingRate	Has to be in range [0,1[. Temperatur will be reduced by multiplying with this value (linear falloff).
	 */
	public void setCoolingRate(int coolingRate) {
		this.coolingRate = coolingRate;
	}
	
	/**
	 * Amount of steps without temperatur falloff at the beginning
	 * @return Steps before cooling.
	 */
	public int getStepsBeforeCooling() {
		return stepsBeforeCooling;
	}

	/**
	 * @param stepsBeforeCooling	Amount of steps without temperatur falloff at the beginning.
	 */
	public void setStepsBeforeCooling(int stepsBeforeCooling) {
		this.stepsBeforeCooling = stepsBeforeCooling;
	}
	
	/**
	 * If temperatur reaches this value, the annealing process will be terminated.
	 * Default value is Double.MIN_VALUE.
	 */
	public void setTerminateAtTemperatur(double temperatur){
		this.lowestTemperatur = temperatur;
	}

	/**
	 * Returns the best fitness yet.
	 * @return
	 */
	public double getBestFitness() {
		return bestFitness;
	}
	
}
