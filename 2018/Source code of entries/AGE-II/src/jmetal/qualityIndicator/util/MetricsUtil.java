/**
 * MetricsUtil.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */

package jmetal.qualityIndicator.util;

import java.io.*;
import java.util.*;
import jmetal.base.Problem;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;
import jmetal.base.variable.Real;
import jmetal.util.NonDominatedSolutionList;
import jmetal.util.Ranking;

/**
 * This class provides some facilities for metrics. 
 **/
public class MetricsUtil {
  
  /**
   * This method reads a Pareto Front for a file.
   * @param path The path to the file that contains the pareto front
   * @return double [][] whit the pareto front
   **/
  public double [][] readFront(String path) {
    try {
      // Open the file
      FileInputStream fis   = new FileInputStream(path)     ;
      InputStreamReader isr = new InputStreamReader(fis)    ;
      BufferedReader br      = new BufferedReader(isr)      ;
      
      List<double []> list = new ArrayList<double []>();
      int numberOfObjectives = 0;
      String aux = br.readLine();
      while (aux!= null) {
        StringTokenizer st = new StringTokenizer(aux);
        int i = 0;
        numberOfObjectives = st.countTokens();
        double [] vector = new double[st.countTokens()];
        while (st.hasMoreTokens()) {
          double value = (new Double(st.nextToken())).doubleValue();
          vector[i] = value;
          i++;
        }
        list.add(vector);
        aux = br.readLine();
      }
            
      br.close();
      
      double [][] front = new double[list.size()][numberOfObjectives];
      for (int i = 0; i < list.size(); i++) {
        front[i] = list.get(i);
      }
      return front;
      
    } catch (Exception e) {
      System.out.println("InputFacilities crashed reading for file: "+path);
      e.printStackTrace();
    }
    return null;
  } // readFront
  
  /** Gets the maximun values for each objectives in a given pareto
   *  front
   *  @param front The pareto front
   *  @param noObjectives Number of objectives in the pareto front
   *  @return double [] An array of noOjectives values whit the maximun values
   *  for each objective
   **/
  public double [] getMaximumValues(double [][] front, int noObjectives) {
    double [] maximumValue = new double[noObjectives];
    for (int i = 0; i < noObjectives; i++)
      maximumValue[i] =  Double.NEGATIVE_INFINITY;
    
    
    for (int i =0; i < front.length;i++ ) {
      for (int j = 0; j < front[i].length; j++) {
        if (front[i][j] > maximumValue[j])
          maximumValue[j] = front[i][j];
      }
    }
    
    return maximumValue;
  } // getMaximumValues
  
  
  /** Gets the minimun values for each objectives in a given pareto
   *  front
   *  @param front The pareto front
   *  @param noObjectives Number of objectives in the pareto front
   *  @return double [] An array of noOjectives values whit the minimum values
   *  for each objective
   **/
  public double [] getMinimumValues(double [][] front, int noObjectives) {
    double [] minimumValue = new double[noObjectives];
    for (int i = 0; i < noObjectives; i++)
      minimumValue[i] = Double.MAX_VALUE;
    
    for (int i = 0;i < front.length; i++) {
      for (int j = 0; j < front[i].length; j++) {
        if (front[i][j] < minimumValue[j]) 
          minimumValue[j] = front[i][j];
      }
    }
    return minimumValue;
  } // getMinimumValues
  
  
  /** 
   *  This method returns the distance (taken the euclidean distance) between
   *  two points given as <code>double []</code>
   *  @param a A point
   *  @param b A point
   *  @return The euclidean distance between the points
   **/
  public double distance(double [] a, double [] b) {
    double distance = 0.0;
    
    for (int i = 0; i < a.length; i++) {
      distance += Math.pow(a[i]-b[i],2.0);
    }
    return Math.sqrt(distance);
  } // distance
  
  
  /**
   * Gets the distance between a point and the nearest one in
   * a given front (the front is given as <code>double [][]</code>)
   * @param point The point
   * @param front The front that contains the other points to calculate the 
   * distances
   * @return The minimun distance between the point and the front
   **/
  public double distanceToClosedPoint(double [] point, double [][] front) {
    double minDistance = distance(point,front[0]);
    
    
    for (int i = 1; i < front.length; i++) {
      double aux = distance(point,front[i]);
      if (aux < minDistance) {
        minDistance = aux;
      }
    }
    
    return minDistance;
  } // distanceToClosedPoint
  
  
  /**
   * Gets the distance between a point and the nearest one in
   * a given front, and this distance is greater than 0.0
   * @param point The point
   * @param front The front that contains the other points to calculate the
   * distances
   * @return The minimun distances greater than zero between the point and
   * the front
   */
  public double distanceToNearestPoint(double [] point, double [][] front) {
    double minDistance = Double.MAX_VALUE;
    
    for (int i = 0; i < front.length; i++) {
      double aux = distance(point,front[i]);
      if ((aux < minDistance) && (aux > 0.0)) {
        minDistance = aux;
      }
    }
    
    return minDistance;
  } // distanceToNearestPoint
  
  /** 
   * This method receives a pareto front and two points, one with maximun values
   * and the other with minimun values allowed, and returns a the normalized
   * pareto front.
   * @param front A pareto front.
   * @param maximumValue The maximun values allowed
   * @param minimumValue The mininum values allowed
   * @return the normalized pareto front
   **/
  public double [][] getNormalizedFront(double [][] front, 
                                        double [] maximumValue,
                                        double [] minimumValue) {
     
    double [][] normalizedFront = new double[front.length][];
    
    for (int i = 0; i < front.length;i++) {
      normalizedFront[i] = new double[front[i].length];
      for (int j = 0; j < front[i].length; j++) {
        normalizedFront[i][j] = (front[i][j] - minimumValue[j]) /
                                (maximumValue[j] - minimumValue[j]);
      }
    }
    return normalizedFront;
  } // getNormalizedFront
  
  
  /**
   * This method receives a normalized pareto front and return the inverted one.
   * This operation needed for minimization problems
   * @param front The pareto front to inverse
   * @return The inverted pareto front
   **/
  public double[][] invertedFront(double [][] front) {
    double [][] invertedFront = new double[front.length][];
    
    for (int i = 0; i < front.length; i++) {
      invertedFront[i] = new double[front[i].length];
      for (int j = 0; j < front[i].length; j++) {
        if (front[i][j] <= 1.0 && front[i][j]>= 0.0) {
          invertedFront[i][j] = 1.0 - front[i][j];
        } else if (front[i][j] > 1.0) {
          invertedFront[i][j] = 0.0;
        } else if (front[i][j] < 0.0) {
          invertedFront[i][j] = 1.0;
        }
      }
    }
    return invertedFront;
  } // invertedFront




  /**
   * Reads a set of non dominated solutions from a file
   * @param path The path of the file containing the data
   * @return A solution set WITHOUT the solutions that did not contain enough objective values (may happen when job is killed while file is written)
   */
  public SolutionSet readNonDominatedSolutionSet(String path, int numberOfObjectives) {
    try {
//        System.out.println("readNonDominatedSolutionSet on "+path);

      /* Open the file */
      FileInputStream fis   = new FileInputStream(path)     ;
      InputStreamReader isr = new InputStreamReader(fis)    ;
      BufferedReader br      = new BufferedReader(isr)      ;

      SolutionSet solutionSet = new NonDominatedSolutionList();


      String aux = br.readLine();
      while (aux!= null) {
          int fileObjCounter = 0;

        StringTokenizer st = new StringTokenizer(aux);
        int i = 0;
        Solution solution = new Solution(st.countTokens());
        while (st.hasMoreTokens()) {
          double value = (new Double(st.nextToken())).doubleValue();
          solution.setObjective(i,value);
          i++;
          fileObjCounter++;
        }
        if (fileObjCounter==numberOfObjectives) {
            solutionSet.add(solution);
          } else {
            System.out.println("!!one solution dropped because objective values were missing");
          }
//        System.out.println("fileObjCounter="+fileObjCounter);
        aux = br.readLine();
      }
      br.close();
//        System.out.println("done");

//        Ranking r = new Ranking(solutionSet);
//        solutionSet = r.getSubfront(0);

      return solutionSet;
    } catch (Exception e) {
      System.out.println("jmetal.qualityIndicator.util.readNonDominatedSolutionSet: "+path);
      e.printStackTrace();
    }
    System.out.println("problem");
    return null;
  } // readNonDominatedSolutionSet




  /**
   * Reads a set of non dominated solutions from a file
   * @param path The path of the file containing the data
   * @return A solution set
   */
  public SolutionSet readSolutionSet(String path, Problem p) {

      File test = new File(".");
                System.out.println("readSolutionSet "+test.getAbsolutePath() + "  "+path);
                path = path.replace("\\", "//");
                System.out.println("readSolutionSet "+test.getAbsolutePath() + "  "+path);

    try {
      /* Open the file */
      FileInputStream fis   = new FileInputStream(path)     ;
      InputStreamReader isr = new InputStreamReader(fis)    ;
      BufferedReader br      = new BufferedReader(isr)      ;
      
      int counted = 0;
      
      
      
     
//      SolutionSet solutionSet = new SolutionSet(counted);
      String aux = br.readLine();
      while (aux!= null) {
        StringTokenizer st = new StringTokenizer(aux);
        int i = 0;
        Solution solution = new Solution(p);
//        Solution solution = new Solution(st.countTokens());      
        while (st.hasMoreTokens()) {
          double value = (new Double(st.nextToken())).doubleValue();
//          solution.setObjective(i,value);
          i++;
        }
        counted++;//solutionSet.add(solution);
        aux = br.readLine();
      }
      br.close();
      
//      System.out.println("readSolutionSet "+test.getAbsolutePath() + "  "+path + " rows:" + counted);
      
      fis   = new FileInputStream(path)     ;
      isr = new InputStreamReader(fis)    ;
      br      = new BufferedReader(isr)      ;
      
      
      SolutionSet solutionSet = new SolutionSet(counted);
      
      aux = br.readLine();
      while (aux!= null) {
        StringTokenizer st = new StringTokenizer(aux);
        int i = 0;
//        Solution solution = new Solution(p);
//        Solution solution = new Solution(st.countTokens());
        
        ArrayList aList = new ArrayList();
        
        while (st.hasMoreTokens()) {
          double value = (new Double(st.nextToken())).doubleValue();
          aList.add(value);
//          solution.setObjective(i,value);
          i++;
        }
        
//        System.out.println("readSolutionSet "+test.getAbsolutePath() + "  "+path + " inputVars:" + i);
        
        Variable[] vars = new Variable[i];
        for (int j = 0; j<vars.length; j++) {
            vars[j] = new Real(p.getLowerLimit(j),p.getUpperLimit(j));// ((Double)aList.get(j)) ;
            vars[j].setValue(  ((Double)aList.get(j)).doubleValue()  );
        }
        
        Solution solution = new Solution(p);
        solution.setDecisionVariables(vars);
//        solution.setDecisionVariables(vars);
        
        
        solutionSet.add(solution);
        aux = br.readLine();
      }
      br.close();
      return solutionSet;
    } catch (Exception e) {
      System.out.println("jmetal.qualityIndicator.util.readNonDominatedSolutionSet: "+path);
      e.printStackTrace();
    }
    return null;
  } // readNonDominatedSolutionSet
  
  

  /**
   * Reads a set of non dominated solutions from a file
   * @param path The path of the file containing the data
   * @return A solution set
   */
  public SolutionSet readNonDominatedSolutionSet(String path) {

      File test = new File(".");
                System.out.println("readNonDominatedSolutionSet "+test.getAbsolutePath() + "  "+path);
                path = path.replace("\\", "//");
                System.out.println("readNonDominatedSolutionSet "+test.getAbsolutePath() + "  "+path);

    try {
      /* Open the file */
      FileInputStream fis   = new FileInputStream(path)     ;
      InputStreamReader isr = new InputStreamReader(fis)    ;
      BufferedReader br      = new BufferedReader(isr)      ;
      
      SolutionSet solutionSet = new NonDominatedSolutionList();
      
      String aux = br.readLine();
      while (aux!= null) {
        StringTokenizer st = new StringTokenizer(aux);
        int i = 0;
        Solution solution = new Solution(st.countTokens());
        while (st.hasMoreTokens()) {
          double value = (new Double(st.nextToken())).doubleValue();
          solution.setObjective(i,value);
          i++;
        }
        solutionSet.add(solution);
        aux = br.readLine();
      }
      br.close();
      return solutionSet;
    } catch (Exception e) {
      System.out.println("jmetal.qualityIndicator.util.readNonDominatedSolutionSet: "+path);
      e.printStackTrace();
    }
    return null;
  } // readNonDominatedSolutionSet
} // MetricsUtil
