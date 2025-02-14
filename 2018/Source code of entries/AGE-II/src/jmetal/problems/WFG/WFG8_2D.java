/**
 * WFG8.java
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.problems.WFG;

import jmetal.base.Solution;
import jmetal.base.Variable;
import jmetal.util.JMException;

/**
* Creates a default WFG8 problem with 
* 2 position-related parameters, 
* 4 distance-related parameters,
* and 2 objectives
* @param solutionType The solution type must "Real" or "BinaryReal".
*/
public class WFG8_2D extends WFG{
           
 /**
  * Creates a default WFG8 with 
  * 2 position-related parameters, 
  * 4 distance-related parameters,
  * and 2 objectives
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */
  public WFG8_2D(String solutionType) throws ClassNotFoundException {
    this(solutionType, 2, 4, 2) ;
  } // WFG8

 /**
  * Creates a WFG8 problem instance
  * @param k Number of position parameters
  * @param l Number of distance parameters
  * @param M Number of objective functions
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */
  public WFG8_2D (String solutionType, Integer k, Integer l, Integer M) throws ClassNotFoundException {
    super(solutionType, k,l,M);
    problemName_ = "WFG8";
        
    S_ = new int[M_];
    for (int i = 0; i < M_; i++)
      S_[i] = 2 * (i+1);
        
    A_ = new int[M_-1];          
    for (int i = 0; i < M_-1; i++)
      A_[i] = 1;                  
  } // WFG8           
    
  /** 
  * Evaluates a solution 
  * @param z The solution to evaluate
  * @return double [] with the evaluation results
  */ 
  public float [] evaluate(float [] z){                
    float [] y;        
        
    y = normalise(z);        
    y = t1(y,k_);
    y = t2(y,k_);        
    y = t3(y,k_,M_);    
        
    float [] result = new float[M_];
    float [] x = calculate_x(y);        
    for (int m = 1; m <= M_ ; m++) {
      result [m-1] = D_*x[M_-1] + S_[m-1] * (new Shapes()).concave(x,m);                
    }        
    
    return result;
  } // evaluate
    
  
  /**
   * WFG8 t1 transformation
   */
  public float [] t1(float [] z, int k){
    float [] result = new float[z.length];
    float [] w      = new float[z.length];
        
    for (int i = 0; i < w.length; i++) {
      w[i] = (float)1.0;
    }
        
    for (int i = 0; i < k; i++) {
      result[i] = z[i];
    }
        
    for (int i = k; i < z.length; i++){
      int head = 0;
      int tail = i - 1;
      float [] subZ = subVector(z,head,tail);
      float [] subW = subVector(w,head,tail);            
      float aux = (new Transformations()).r_sum(subZ,subW);
            
      result[i] = (new Transformations()).b_param(z[i],aux,(float)0.98/(float)49.98,(float)0.02,50);
    }
        
    return result;
  } // t1
    
  /**
   * WFG8 t2 transformation
   */  
  public float [] t2(float [] z, int k){
    float [] result = new float[z.length];
        
    for (int i = 0; i < k; i++) {
      result[i] = z[i];
    }
        
    for (int i = k; i < z.length; i++) {
      result[i] = (new Transformations()).s_linear(z[i],(float)0.35);
    }
        
    return result;      
  } // t2
    
  /**
   * WFG8 t3 transformation
   */    
  public float [] t3(float [] z, int k, int M){
    float [] result = new float[M];
    float [] w      = new float[z.length];        
        
    for (int i = 0; i < z.length; i++) {
      w[i] = (float)1.0;
    }
        
    for (int i = 1; i <= M-1; i++){
      int head = (i - 1)*k/(M-1) + 1;
      int tail = i * k / (M - 1);                                   
      float [] subZ = subVector(z,head-1,tail-1);
      float [] subW = subVector(w,head-1,tail-1);
            
      result[i-1] = (new Transformations()).r_sum(subZ,subW);            
    }
                
    int head = k + 1;
    int tail = z.length;              
    float [] subZ = subVector(z,head-1,tail-1);      
    float [] subW = subVector(w,head-1,tail-1);        
    result[M-1] = (new Transformations()).r_sum(subZ,subW);
                        
    return result;
  } // t3
  
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */     
  public final void evaluate(Solution solution) throws JMException {
    float [] variables = new float[getNumberOfVariables()];
    Variable[] dv = solution.getDecisionVariables();
        
    for (int i = 0; i < getNumberOfVariables(); i++) {
      variables[i] = (float)dv[i].getValue();    
    }
        
    float [] sol = evaluate(variables);
        
    for (int i = 0; i < sol.length; i++) {
      solution.setObjective(i,sol[i]);
    }
  } // evaluate
} // WFG8


