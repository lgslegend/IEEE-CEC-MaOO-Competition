/**
 * WFG3.java
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.problems.WFG;

import jmetal.base.Solution;
import jmetal.base.Variable;
import jmetal.util.JMException;

/**
 * This class implements the WFG3 problem
 * Reference: Simon Huband, Luigi Barone, Lyndon While, Phil Hingston
 *            A Scalable Multi-objective Test Problem Toolkit.
 *            Evolutionary Multi-Criterion Optimization: 
 *            Third International Conference, EMO 2005. 
 *            Proceedings, volume 3410 of Lecture Notes in Computer Science
 */
public class WFG3_3D extends WFG{

  /**
   * Creates a default WFG3 instances with 
   * 2 position-related parameters 
   * 4 distance-related parameters
   * and 3 objectives
   * @param solutionType The solution type must "Real" or "BinaryReal".
   */
  public WFG3_3D(String solutionType) throws ClassNotFoundException {
    this(solutionType, 2, 4, 3) ;
  } // WFG3

 /**
  * Creates a WFG3 problem instance
  * @param k Number of position parameters
  * @param l Number of distance parameters
  * @param M Number of objective functions
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */
  public WFG3_3D (String solutionType, Integer k, Integer l, Integer M) throws ClassNotFoundException {
    super(solutionType, k,l,M);
    problemName_ = "WFG3";
        
    S_ = new int[M_];
    for (int i = 0; i < M_; i++) {
      S_[i] = 2 * (i+1);
    }
    
    A_ = new int[M_-1];        
    A_[0] = 1;
    for (int i = 1; i < M_-1; i++) {
      A_[i] = 0;               
    }
  } // WFG3

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
      result [m-1] = D_*x[M_-1] + S_[m-1] * (new Shapes()).linear(x,m);                
    }
        
    return result;
  } // evaluate
    
  /**
   * WFG3 t1 transformation
   */  
  public float [] t1(float [] z, int k){
    float [] result = new float[z.length];
        
    for (int i = 0; i < k; i++) {
      result[i] = z[i];
    }
        
    for (int i = k; i < z.length; i++) {
      result[i] = (new Transformations()).s_linear(z[i],(float)0.35);
    }
        
    return result;      
  } // t1

  
  /**
   * WFG3 t2 transformation
   */ 
  public float [] t2(float [] z, int k){
    float [] result = new float[z.length];
        
    for (int i = 0; i < k; i++) {
      result[i] = z[i];
    }
        
    int l = z.length - k;        
    for (int i = k+1; i <= k + l/2; i++){
      int head = k + 2*(i - k) - 1;
      int tail = k + 2*(i - k);              
      float [] subZ = subVector(z,head-1,tail-1);
            
      result[i-1] = (new Transformations()).r_nonsep(subZ,2);
    }        
    return result;
  } // t2

  /**
  * WFG3 t3 transformation
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
        
    int l = z.length - k;
    int head = k + 1;
    int tail = k + l / 2;              
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
} // WFG3
