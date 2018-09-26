/**
 * Hypervolume.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.qualityIndicator;

import java.util.*;
import java.io.*;

/**
 * This class implements the hypervolume metric. The code is the a Java version
 * of the orginal metric implementation by Eckart Zitzler.
 * It can be used also as a command line program just by typing
 * $java Hypervolume <solutionFrontFile> <trueFrontFile> <numberOfOjbectives>
 * Reference: E. Zitzler and L. Thiele
 *           Multiobjective Evolutionary Algorithms: A Comparative Case Study 
 *           and the Strength Pareto Approach,
 *           IEEE Transactions on Evolutionary Computation, vol. 3, no. 4, 
 *           pp. 257-271, 1999.
 */
public class Hypervolume {

  jmetal.qualityIndicator.util.MetricsUtil utils_;
  
  /**
  * Constructor
  * Creates a new instance of MultiDelta
  */
  public Hypervolume() {
    utils_ = new jmetal.qualityIndicator.util.MetricsUtil();
  } // Hypervolume
  
  /* 
   returns true if 'point1' dominates 'points2' with respect to the 
   to the first 'noObjectives' objectives 
   */
  boolean  dominates(double  point1[], double  point2[], int  noObjectives) {
    int  i;
    int  betterInAnyObjective;

    betterInAnyObjective = 0;
    for (i = 0; i < noObjectives && point1[i] >= point2[i]; i++)
      if (point1[i] > point2[i])
	betterInAnyObjective = 1;
    
    return ((i >= noObjectives) && (betterInAnyObjective>0));
  } //Dominates

  void  swap(double [][] front, int  i, int  j){
    double  [] temp;
    
    temp = front[i];
    front[i] = front[j];
    front[j] = temp;
  } // Swap 

  
  /* all nondominated points regarding the first 'noObjectives' dimensions
  are collected; the points referenced by 'front[0..noPoints-1]' are
  considered; 'front' is resorted, such that 'front[0..n-1]' contains
  the nondominated points; n is returned */
  int  filterNondominatedSet(double [][] front, int  noPoints, int  noObjectives){
    int  i, j;
    int  n;

    n = noPoints;
    i = 0;
    while (i < n) {
      j = i + 1;
      while (j < n) {
        if (dominates(front[i], front[j], noObjectives)) {
	/* remove point 'j' */
	  n--;
	  swap(front, j, n);
        } else if (dominates(front[j], front[i], noObjectives)) {
	/* remove point 'i'; ensure that the point copied to index 'i'
	   is considered in the next outer loop (thus, decrement i) */
	  n--;
	  swap(front, i, n);
	  i--;
	  break;
        } else
	  j++;
      }
      i++;
    }
    return n;
  } // FilterNondominatedSet 


  /* calculate next value regarding dimension 'objective'; consider
     points referenced in 'front[0..noPoints-1]' */
  double  surfaceUnchangedTo(double [][] front, int  noPoints, int  objective) {
    int     i;
    double  minValue, value;

    if (noPoints < 1)  
      System.err.println("run-time error");
    
    minValue = front[0][objective];
    for (i = 1; i < noPoints; i++) {
      value = front[i][objective];
      if (value < minValue)
        minValue = value;
    }
    return minValue;
  } // SurfaceUnchangedTo 

  /* remove all points which have a value <= 'threshold' regarding the
     dimension 'objective'; the points referenced by
     'front[0..noPoints-1]' are considered; 'front' is resorted, such that
     'front[0..n-1]' contains the remaining points; 'n' is returned */
  int  reduceNondominatedSet(double [][] front, int  noPoints, int  objective,
			   double  threshold){
    int  n;
    int  i;

    n = noPoints;
    for (i = 0; i < n; i++)
      if (front[i][objective] <= threshold) {
        n--;
        swap(front, i, n);
      }
  
    return n;
  } // ReduceNondominatedSet

  public double calculateHypervolume(double [][] front, int  noPoints,int  noObjectives){
//      System.out.println("calculateHypervolume, noObjectives="+noObjectives);
//      System.out.println("calculateHypervolume, noPoints="+noPoints);
    int     n;
    double  volume, distance;

    volume = 0;
    distance = 0;
    n = noPoints;
    while (n > 0) {
      int     noNondominatedPoints;
      double  tempVolume, tempDistance;

      noNondominatedPoints = filterNondominatedSet(front, n, noObjectives - 1);
      tempVolume = 0;
      if (noObjectives < 3) {
        if (noNondominatedPoints < 1)  
          System.err.println("run-time error");
      
        tempVolume = front[0][0];
      } else
        tempVolume = calculateHypervolume(front,
                                          noNondominatedPoints,
                                          noObjectives - 1);
      
      tempDistance = surfaceUnchangedTo(front, n, noObjectives - 1);
      volume += tempVolume * (tempDistance - distance);
      distance = tempDistance;
      n = reduceNondominatedSet(front, n, noObjectives - 1, distance);
    }
//      System.out.println("return");
    return volume;
  } // CalculateHypervolume

   
  /* merge two fronts */
  double [][] mergeFronts(double [][] front1, int  sizeFront1,
		 double [][] front2, int  sizeFront2, int  noObjectives)
  {
    int  i, j;
    int  noPoints;
    double [][] frontPtr;

    /* allocate memory */
    noPoints = sizeFront1 + sizeFront2;
    frontPtr = new double[noPoints][noObjectives];
    /* copy points */
    noPoints = 0;
    for (i = 0; i < sizeFront1; i++) {
      for (j = 0; j < noObjectives; j++)
        frontPtr[noPoints][j] = front1[i][j];
      noPoints++;
    }
    for (i = 0; i < sizeFront2; i++) {
      for (j = 0; j < noObjectives; j++)
        frontPtr[noPoints][j] = front2[i][j];
      noPoints++;
    }

    return frontPtr;
  } // MergeFronts










   /**
   *
    * NOTE: copy of hypervolume, except for last line
    *
    * Returns the hypevolume value of the paretoFront. This method call to the
   * calculate hipervolume one
   * @param paretoFront The pareto front
   * @param paretoTrueFront The true pareto front
   * @param numberOfObjectives Number of objectives of the pareto front
   */
  public double hypervolumeFPRAS(double [][] paretoFront,
                            double [][] paretoTrueFront,
                            int numberOfObjectives,
                            boolean doExactForFinalEvaluation,
                            double hypervolumePoint) {

    /**
     * Stores the maximum values of true pareto front.
     */
    double[] maximumValues;

    /**
     * Stores the minimum values of the true pareto front.
     */
    double[] minimumValues;

    /**
     * Stores the normalized front.
     */
    double [][] normalizedFront;

    /**
     * Stores the inverted front. Needed for minimization problems
     */
    double [][] invertedFront;

    // STEP 1. Obtain the maximum and minimum values of the Pareto front
    maximumValues = utils_.getMaximumValues(paretoTrueFront,numberOfObjectives);

    /*by Markus Wagner: insertion of different hypervolume reference point*/
    if (hypervolumePoint!=Double.NEGATIVE_INFINITY) {
        double[] artificialReferencePoint = new double[numberOfObjectives];
        for (int i = 0; i<numberOfObjectives; i++) {
            artificialReferencePoint[i] = hypervolumePoint;
        }
        maximumValues = artificialReferencePoint;
    }

    minimumValues = utils_.getMinimumValues(paretoTrueFront,numberOfObjectives);

    // STEP 2. Get the normalized front
    normalizedFront = utils_.getNormalizedFront(paretoFront,
                                                maximumValues,
                                                minimumValues);

    // STEP 3. Inverse the pareto front. This is needed because of the original
    //metric by Zitzler is for maximization problems

    invertedFront = utils_.invertedFront(normalizedFront);

//    System.out.println("Hypervolume:beforeCalc");
    if (false) System.out.println("hypervolume: marker. " + invertedFront.length + " " +paretoFront.length
            + " " + paretoTrueFront.length + " " + numberOfObjectives + " " +invertedFront[0].length);
    // STEP4. The hypervolumen (control is passed to java version of Zitzler code)
    double t = this.calculateHypervolumeFPRAS(invertedFront,invertedFront.length,numberOfObjectives,
            doExactForFinalEvaluation);

    if (Double.isNaN(t)) {
                            t=0;
                        }
    return t;

  }// hypervolume





  public double calculateHypervolumeFPRAS(double [][] front,
//                            double [][] paretoTrueFront,
                              int noPoints,
                            int noObjectives,
                            boolean doExactForFinalEvaluation) {

//                            int noPoints = front.length;
      double eps=0.01;

  // runtime
  int maxsamples=0;
  if (doExactForFinalEvaluation)  {
      maxsamples = (int)(30.*noPoints/eps/eps);
  }                  // used for evaluation
  else {
      maxsamples = (int)(noPoints/eps/eps);
  }
//  int maxsamples=(int)(noPoints/eps/eps);

  // calc separate volume of each box
  double[] vol = new double[noPoints];
  for (int i = 0; i < noPoints; i++) {
    vol[i]=front[i][0];
    for (int objective = 1; objective < noObjectives; objective++) vol[i]*=front[i][objective];
  }

  // calc total volume and partial sum
  double T=0;
  for (int i = 0; i < noPoints; i++) {
    vol[i]+=T;
    T+=vol[i]-T;
  }

  double volume=0;
  double r;
  double[] rndpoint = new double[noObjectives];
  int j;
  int samples_sofar=0;
  int round=0;

  while(true) {
    r=T * Math.random();//rand()/(RAND_MAX+1.0);

    // j-th point is randomly chosen with probability proportional to volume
    for (j = 0; j < noPoints; j++) if(r<=vol[j]) break;

    // calc rnd point
    for (int k = 0; k < noObjectives; k++) 
        rndpoint[k] = front[j][k] *Math.random();//* rand()/(RAND_MAX+1.0);

        do {
            if(samples_sofar>=maxsamples)
            //OLD and WORKING!!!!
            //return (double)(maxsamples*T)/(double)noPoints/(double)round;
            {
                double t = (double)(maxsamples*T)/(double)noPoints/(double)round;
//                System.out.println("t="+t);
                if (Double.isNaN(t)) {
                            t=0;
                        }
                return t;
            }

            j= (int) (Math.random()*noPoints);
            samples_sofar++;
        } while (!Dominates(front[j],rndpoint,noObjectives));

        round++;
      }
}

  public boolean Dominates(double  point1[], double  point2[], int  noObjectives)
     /* returns true if 'point1' dominates 'points2' with respect to the
	first 'noObjectives' objectives */
{
  int  i;
  boolean  betterInAnyObjective;

  betterInAnyObjective = false;
  for (i = 0; i < noObjectives && point1[i] >= point2[i]; i++)
      if (point1[i] > point2[i])
	      betterInAnyObjective = true;
  return ( (i >= noObjectives) && betterInAnyObjective);
} /* Dominates */



  /** 
   * Returns the hypevolume value of the paretoFront. This method call to the
   * calculate hipervolume one
   * @param paretoFront The pareto front
   * @param paretoTrueFront The true pareto front
   * @param numberOfObjectives Number of objectives of the pareto front
   */
  public double hypervolume(double [][] paretoFront, 
                            double [][] paretoTrueFront,
                            int numberOfObjectives) {
    
    /**
     * Stores the maximum values of true pareto front.
     */
    double[] maximumValues;
    
    /**
     * Stores the minimum values of the true pareto front.
     */
    double[] minimumValues;
    
    /**
     * Stores the normalized front.
     */
    double [][] normalizedFront;
    
    /**
     * Stores the inverted front. Needed for minimization problems
     */
    double [][] invertedFront;
    
    // STEP 1. Obtain the maximum and minimum values of the Pareto front
    maximumValues = utils_.getMaximumValues(paretoTrueFront,numberOfObjectives);

//    double[] artificialReferencePoint = new double[numberOfObjectives];
//    for (int i = 0; i<numberOfObjectives; i++) {
//        artificialReferencePoint[i] = 100;
//    }
//    maximumValues = artificialReferencePoint;

    minimumValues = utils_.getMinimumValues(paretoTrueFront,numberOfObjectives);
    
    // STEP 2. Get the normalized front
    normalizedFront = utils_.getNormalizedFront(paretoFront,
                                                maximumValues,
                                                minimumValues);
    
    // STEP 3. Inverse the pareto front. This is needed because of the original
    //metric by Zitzler is for maximization problems

    invertedFront = utils_.invertedFront(normalizedFront);

//    System.out.println("Hypervolume:beforeCalc");
    if (false) System.out.println("hypervolume: marker. " + invertedFront.length + " " +paretoFront.length
            + " " + paretoTrueFront.length + " " + numberOfObjectives + " " +invertedFront[0].length);
    // STEP4. The hypervolumen (control is passed to java version of Zitzler code)
    return this.calculateHypervolume(invertedFront,invertedFront.length,numberOfObjectives);
  }// hypervolume
  
  
  public double hypervolumeCEC2018(double [][] paretoFront, 
                            double [][] paretoTrueFront,
                            int numberOfObjectives) {
    
    /**
     * Stores the maximum values of true pareto front.
     */
    double[] maximumValues;
    
    /**
     * Stores the minimum values of the true pareto front.
     */
    double[] minimumValues;
    
    /**
     * Stores the normalized front.
     */
    double [][] normalizedFront;
    
    /**
     * Stores the inverted front. Needed for minimization problems
     */
    double [][] invertedFront;
    
    // STEP 1. Obtain the maximum and minimum values of the Pareto front
    maximumValues = utils_.getMaximumValues(paretoTrueFront,numberOfObjectives);

//    double[] artificialReferencePoint = new double[numberOfObjectives];
//    for (int i = 0; i<numberOfObjectives; i++) {
//        artificialReferencePoint[i] = 100;
//    }
//    maximumValues = artificialReferencePoint;
    for (int i=0; i<maximumValues.length; i++) {
        maximumValues[i] = maximumValues[i]*1.1;
    }

    minimumValues = utils_.getMinimumValues(paretoTrueFront,numberOfObjectives);
    
    // STEP 2. Get the normalized front
    normalizedFront = utils_.getNormalizedFront(paretoFront,
                                                maximumValues,
                                                minimumValues);
    
    // STEP 3. Inverse the pareto front. This is needed because of the original
    //metric by Zitzler is for maximization problems

    invertedFront = utils_.invertedFront(normalizedFront);

//    System.out.println("Hypervolume:beforeCalc");
    if (false) System.out.println("hypervolume: marker. " + invertedFront.length + " " +paretoFront.length
            + " " + paretoTrueFront.length + " " + numberOfObjectives + " " +invertedFront[0].length);
    // STEP4. The hypervolumen (control is passed to java version of Zitzler code)
    return this.calculateHypervolume(invertedFront,invertedFront.length,numberOfObjectives);
  }// hypervolume
  
  /**
   * This class can be invoqued from the command line. Three params are required:
   * 1) the name of the file containing the front,  
   * 2) the name of the file containig the true Pareto front
   * 3) the number of objectives
   */
  public static void main(String args[]) {
//    if (args.length < 2) {
//      System.err.println("Error using delta. Type: \n java Hypervolume " +
//                         "<SolutionFrontFile>" +
//                         "<TrueFrontFile> + <numberOfObjectives>");
//      System.exit(1);
//    }
//    
//    //Create a new instance of the metric
//    Hypervolume qualityIndicator = new Hypervolume();
//    //Read the front from the files
//    double [][] solutionFront = qualityIndicator.utils_.readFront(args[0]);
//    double [][] trueFront     = qualityIndicator.utils_.readFront(args[1]);
//    
//    //Obtain delta value
//    double value = qualityIndicator.hypervolume(solutionFront,
//                                  trueFront,
//                                  (new Integer(args[2])).intValue());
//    
//    System.out.println(value);  
    if (args.length < 2) {
      System.err.println("Error using delta. Type: \n java Hypervolume " +
                         "<SolutionFrontFile>" +
                         "<TrueFrontFile> + <numberOfObjectives>");
      System.exit(1);
    }
    
    //Create a new instance of the metric
    Hypervolume qualityIndicator = new Hypervolume();
    //Read the front from the files
    double [][] solutionFront = qualityIndicator.utils_.readFront(args[0]);
    double [][] trueFront     = qualityIndicator.utils_.readFront(args[1]);
    
    int numberOfObjectives = Integer.parseInt(args[2]);
    
    double[] maximumValues;
    double[] minimumValues;
    double [][] normalizedFront;
    double [][] invertedFront;
    
    // STEP 1. Obtain the maximum and minimum values of the Pareto front
    maximumValues = qualityIndicator.utils_.getMaximumValues(trueFront,numberOfObjectives);
//    double[] artificialReferencePoint = new double[numberOfObjectives];
//    for (int i = 0; i<numberOfObjectives; i++) {
//        artificialReferencePoint[i] = 100;
//    }
//    maximumValues = artificialReferencePoint;

    minimumValues = qualityIndicator.utils_.getMinimumValues(trueFront,numberOfObjectives);
    // STEP 2. Get the normalized front
    normalizedFront = qualityIndicator.utils_.getNormalizedFront(solutionFront, maximumValues, minimumValues);
    // STEP 3. Inverse the pareto front. This is needed because of the original
    //metric by Zitzler is for maximization problems
    invertedFront = qualityIndicator.utils_.invertedFront(normalizedFront);

    // STEP4. The hypervolumen (control is passed to java version of Zitzler code)
    double value = qualityIndicator.calculateHypervolume(invertedFront,invertedFront.length,numberOfObjectives);
    
    System.out.println(value);  
  } // main
} // Hypervolume
