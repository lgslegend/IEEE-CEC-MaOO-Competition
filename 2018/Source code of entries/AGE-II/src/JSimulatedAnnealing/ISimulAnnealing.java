package JSimulatedAnnealing;


/** 
 * @param <T>	The class of the genome
 * @author Stefan Lattner
 * 
 * Part of the JSimulatedAnnealing-package
 * (c) 2011, www.stefanlattner.at
 */
public interface ISimulAnnealing<T> {
	/**
	 * Transforms the given genome into a neighborhood solution (slight mutation).
	 * @param genome	The genome which will be mutated.
	 * @return			The mutated genome.
	 */
	public T move (T genome);
	/**
	 * The objective function which returns a fitness value of the given genome.
	 * @param genome	The genome which will be evaluated.
	 * @return			Fitness value for the given genome.
	 */
	public double objectiveFunction (T genome);
	/**
	 * At each step the annealer will call this method to provide the current genome.
	 * @param genome	The genome at the current step.
	 */
	public void currGenomeCallback(T genome);
	/**
	 * At each step the annealer will call this method to provide the current fitness.
	 * @param fitness	The fitness at the current step.
	 */
	public void currFitnessCallback(double fitness);
	/**
	 * If the current fitness value is beyond the last fitness value, the annealer will provide
	 * the probability on if the worse genome will be choosen. This probability depends on the
	 * current temperatur and the fitness distance from the last to the current genome.
	 * @param probability	The probability if the current, worse genome will be choosen.
	 */
	public void currProbabilityCallback(double probability);
	/**
	 * At each step the annealer will call this method to provide the current temperatur.
	 * @param temperatur
	 */
	public void currTemperaturCallback(double temperatur);
	/**
	 * Each time the genome has improved, the annealer will call this method to provide the best genome yet.
	 * @param genome
	 */
	public void bestGenomeCallback(T genome);
	/**
	 * Each time the genome has improved, the annealer will call this method to provide the best fitness yet.
	 * @param genome
	 */
	public void bestFitnessCallback(double fitness);
	/**
	 * Will be called each time, the evaluated genome has been accepted.
	 */
	public void genomeAcceptedCallback(T genome);
}
