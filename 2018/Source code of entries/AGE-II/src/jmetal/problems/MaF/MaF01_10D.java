package jmetal.problems.MaF;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.Variable;
import jmetal.base.solutionType.BinaryRealSolutionType;
import jmetal.base.solutionType.RealSolutionType;
import jmetal.util.JMException;

/** 
* Class representing problem MaF01 
*/
public class MaF01_10D extends Problem {   
/** 
* Creates a default MaF01 problem (7 variables and 3 objectives)
* @param solutionType The solution type must "Real" or "BinaryReal". 
*/
public MaF01_10D(String solutionType) throws ClassNotFoundException {
this(solutionType, 19, 10);
} // MaF01   

/** 
* Creates a MaF01 problem instance
* @param numberOfVariables Number of variables
* @param numberOfObjectives Number of objective functions
* @param solutionType The solution type must "Real" or "BinaryReal". 
*/
public MaF01_10D(String solutionType, 
           Integer numberOfVariables, 
		         Integer numberOfObjectives) throws ClassNotFoundException {
numberOfVariables_  = numberOfVariables;
numberOfObjectives_ = numberOfObjectives;
numberOfConstraints_= 0;
problemName_        = "MaF01";
    
lowerLimit_ = new double[numberOfVariables_];
upperLimit_ = new double[numberOfVariables_];        
for (int var = 0; var < numberOfVariables; var++){
  lowerLimit_[var] = 0.0;
  upperLimit_[var] = 1.0;
} //for

if (solutionType.compareTo("BinaryReal") == 0)
	solutionType_ = new BinaryRealSolutionType(this) ;
else if (solutionType.compareTo("Real") == 0)
	solutionType_ = new RealSolutionType(this) ;
else {
	System.out.println("Error: solution type " + solutionType + " invalid") ;
	System.exit(-1) ;
}            
}            

/** 
* Evaluates a solution 
* @param solution The solution to evaluate
* @throws JMException 
*/  
//MaF01 , modified inverted DTLZ1
public void evaluate(Solution solution) throws JMException {
	
	Variable[] gen  = solution.getDecisionVariables();   
	double [] x = new double[numberOfVariables_];
	double [] f = new double[numberOfObjectives_];
	    
	for (int i = 0; i < numberOfVariables_; i++)
	  x[i] = gen[i].getValue();
	
	double g=0,subf1=1,subf3;
//		evaluate g(xm)
	for(int j=numberOfObjectives_-1;j<numberOfVariables_;j++)
		g+=(Math.pow(x[j]-0.5,2));
	subf3=1+g;
//		evaluate objectives
//		fi=(1+g)(1-(x1x2...x[m-i])(1-x[m-i+1])),fi=subf3*(1-subf1*subf2)
//		evaluate fm,f2~m-1,f1,
	f[numberOfObjectives_-1]=x[0]*subf3;
	for(int i=numberOfObjectives_-2;i>0;i--)
	{
		subf1*=x[numberOfObjectives_-i-2];
		f[i]=subf3*(1-subf1*(1-x[numberOfObjectives_-i-1]));
	}
	f[0]=(1-subf1*x[numberOfObjectives_-2])*subf3;
	
	for (int i = 0; i < numberOfObjectives_; i++)
		  solution.setObjective(i,f[i]);
}  

}

