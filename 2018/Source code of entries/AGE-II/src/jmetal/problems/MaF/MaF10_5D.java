package jmetal.problems.MaF;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.Variable;
import jmetal.base.solutionType.BinaryRealSolutionType;
import jmetal.base.solutionType.RealSolutionType;
import jmetal.util.JMException;

/** 
* Class representing problem MaF10 
*/
public class MaF10_5D extends Problem {   
/** 
* Creates a default MaF10 problem (12 variables and 3 objectives)
* @param solutionType The solution type must "Real" or "BinaryReal".
*/
public static int K10;
public MaF10_5D(String solutionType) throws ClassNotFoundException {
this(solutionType, 14, 5);
} // MaF10   

/** 
* Creates a MaF10 problem instance
* @param numberOfVariables Number of variables
* @param numberOfObjectives Number of objective functions
* @param solutionType The solution type must "Real" or "BinaryReal". 
*/
public MaF10_5D(String solutionType, 
           Integer numberOfVariables, 
		         Integer numberOfObjectives) throws ClassNotFoundException {
numberOfVariables_  = numberOfVariables;
numberOfObjectives_ = numberOfObjectives;
numberOfConstraints_= 0;
problemName_        = "MaF10";
K10=numberOfObjectives_-1;  
lowerLimit_ = new double[numberOfVariables_];
upperLimit_ = new double[numberOfVariables_];        
for (int var = 0; var < numberOfVariables; var++){
  lowerLimit_[var] = 0.0;
  upperLimit_[var] = 2.0*(var+1);
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
//MaF10 , WFG1
public void evaluate(Solution solution) throws JMException {
	
	Variable[] gen  = solution.getDecisionVariables();   
	double [] x = new double[numberOfVariables_];
	double [] f = new double[numberOfObjectives_];
	    
	for (int i = 0; i < numberOfVariables_; i++)
	  x[i] = gen[i].getValue();
	
//	evaluate zi,t1i,t2i,t3i,t4i,yi
	double[] z=new double[numberOfVariables_];
	double[] t1=new double[numberOfVariables_];
	double[] t2=new double[numberOfVariables_];
	double[] t3=new double[numberOfVariables_];
	double[] t4=new double[numberOfObjectives_];
	double[] y=new double[numberOfObjectives_];
	double sub1=0,sub2=0;
	int lb=0,ub=0;
	for(int i=0;i<K10;i++)
	{
		z[i]=x[i]/(2*i+2);
		t1[i]=z[i];
		t2[i]=t1[i];
		t3[i]=Math.pow(t2[i], 0.02);
	}
	for(int i=K10;i<numberOfVariables_;i++)
	{
		z[i]=x[i]/(2*i+2);
		t1[i]=Math.abs(z[i]-0.35)/(Math.abs(Math.floor(0.35-z[i])+0.35));
		t2[i]=0.8+0.8*(0.75-t1[i])*Math.min(0, Math.floor(t1[i]-0.75))/0.75-0.2*(t1[i]-0.85)*Math.min(0, Math.floor(0.85-t1[i]))/0.15;
		t2[i]=Math.round(t2[i]*1000000)/1000000.0;
		t3[i]=Math.pow(t2[i], 0.02);
	}
	for(int i=0;i<numberOfObjectives_-1;i++)
	{
		sub1=0;sub2=0;
		lb=i*K10/(numberOfObjectives_-1)+1;ub=(i+1)*K10/(numberOfObjectives_-1);
		for(int j=lb-1;j<ub;j++)
		{
			sub1+=(2*(j+1)*t3[j]);
			sub2+=(2*(j+1));
		}
		t4[i]=sub1/sub2;
	}
	lb=K10+1;ub=numberOfVariables_;sub1=0;sub2=0;
	for(int j=lb-1;j<ub;j++)
	{
		sub1+=(2*(j+1)*t3[j]);
		sub2+=(2*(j+1));
	}
	t4[numberOfObjectives_-1]=sub1/sub2;
	for(int i=0;i<numberOfObjectives_-1;i++)
		y[i]=(t4[i]-0.5)*Math.max(1, t4[numberOfObjectives_-1])+0.5;
	y[numberOfObjectives_-1]=t4[numberOfObjectives_-1];
//	------------------------------------------------------------------------------------------
//	for(int i=0;i<y.length;i++)
//		System.out.print(y[i]+",");
//	System.out.println();
//	
//	for(int i=0;i<t4.length;i++)
//		System.out.print(t4[i]+",");
//	System.out.println();
//	
//	for(int i=0;i<t3.length;i++)
//		System.out.print(t3[i]+",");
//	System.out.println();
//	
//	for(int i=0;i<t2.length;i++)
//		System.out.print(t2[i]+",");
//	System.out.println();
//	
//	for(int i=0;i<t1.length;i++)
//		System.out.print(t1[i]+",");
//	System.out.println();

//	for(int i=0;i<z.length;i++)
//		System.out.print(z[i]+",");
//	System.out.println();
//	------------------------------------------------------------------------------------------
//	evaluate fm,fm-1,...,2,f1
	double subf1=1;
	f[numberOfObjectives_-1]=y[numberOfObjectives_-1]+2*numberOfObjectives_*(1-y[0]-Math.cos(10*Math.PI*y[0]+Math.PI/2)/(10*Math.PI));
	for(int i=numberOfObjectives_-2;i>0;i--)
	{
		subf1*=(1-Math.cos(Math.PI*y[numberOfObjectives_-i-2]/2));
		f[i]=y[numberOfObjectives_-1]+2*(i+1)*subf1*(1-Math.sin(Math.PI*y[numberOfObjectives_-i-1]/2));
	}
	f[0]=y[numberOfObjectives_-1]+2*subf1*(1-Math.cos(Math.PI*y[numberOfObjectives_-2]/2));
	
	for (int i = 0; i < numberOfObjectives_; i++)
		  solution.setObjective(i,f[i]);	
	
}  

}

