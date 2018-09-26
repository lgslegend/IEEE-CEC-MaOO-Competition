/**
 * BFNW_Settings.java (IBEA_Settings.java based)
 *
 * @author Markus Wagner
 * @version 1.0
 *
 * BFNW_Settings class of algorithm BFNW
 */
package jmetal.experiments.settings;

import java.util.Properties;
import jmetal.base.Algorithm;
import jmetal.base.Operator;
import jmetal.base.Problem;
import jmetal.base.operator.comparator.FitnessComparator;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.BinaryTournament;
import jmetal.base.operator.selection.RandomSelection;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.bfnw.BFNW;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;
import jmetal.util.Configuration.*;

public class BFNW_Settings extends Settings{

	// Default settings
	public int populationSize_ = 100   ;                                    // 100 is too big (at least: takes quite long)
	public int maxEvaluations_ = 100000 ;
	public int archiveSize_    = 100 ;                                      // is not regarded

	public double mutationProbability_  = 1.0/problem_.getNumberOfVariables() ;
	public double crossoverProbability_ = 0.9 ;

	public double  distributionIndexForMutation_ = 20    ;
	public double  distributionIndexForCrossover_ = 20    ;

	/**
	 * Constructor
	 */
	public BFNW_Settings(Problem problem, String[] args) {
		super(problem, args) ;
	} // BFNW_Settings

	/**
	 * Configure BFNW with user-defined parameter settings
	 * @return A BFNW algorithm object
	 * @throws jmetal.util.JMException
	 */
	public Algorithm configure() throws JMException {
		Algorithm algorithm ;
		Operator  selection ;
		Operator  crossover ;
		Operator  mutation  ;

		QualityIndicator indicators ;

		// Creating the problem
		algorithm = new BFNW(problem_) ;

                algorithm.setInputParameter("doMutation", true);                //added by Markus Wagner
                algorithm.setInputParameter("doCrossover", true);               //added by Markus Wagner

		// Algorithm parameters
		algorithm.setInputParameter("populationSize", populationSize_);
		algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
		algorithm.setInputParameter("archiveSize", archiveSize_);

		// Mutation and Crossover for Real codification 
		crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover");                   
		crossover.setParameter("probability", crossoverProbability_);                   
		crossover.setParameter("distributionIndex",distributionIndexForCrossover_);

		mutation = MutationFactory.getMutationOperator("PolynomialMutation");                    
		mutation.setParameter("probability", mutationProbability_);
		mutation.setParameter("distributionIndex",distributionIndexForMutation_);    

		// Selection Operator 
//		selection = new RandomSelection();
		selection = new BinaryTournament(new FitnessComparator());

		// Add the operators to the algorithm
		algorithm.addOperator("crossover",crossover);
		algorithm.addOperator("mutation",mutation);
		algorithm.addOperator("selection",selection);



                algorithm.setInputParameter("doOnMPICluster", true);
                algorithm.setInputParameter("infoPrinterHowOften", 100);
                algorithm.setInputParameter("infoPrinterSubDir", "foo");
                algorithm.setInputParameter("epsilonGridWidth", 0.01);


		// Creating the indicator object
		if (! paretoFrontFile_.equals("")) {
			indicators = new QualityIndicator(problem_, paretoFrontFile_);
			algorithm.setInputParameter("indicators", indicators) ;  
		} // if
		return algorithm ;
	} // configure
} // BFNW_Settings
