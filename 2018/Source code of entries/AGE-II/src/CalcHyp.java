/**
 * Hypervolume.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */

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
public class CalcHyp {

//  MetricsUtilLocal utils_;
  
  /**
  * Constructor
  * Creates a new instance of MultiDelta
  */
//  public CalcHyp() {
//    utils_ = new MetricsUtilLocal();
//  } // Hypervolume
  
  /* 
   returns true if 'point1' dominates 'points2' with respect to the 
   to the first 'noObjectives' objectives 
   */
  static boolean  dominates(double  point1[], double  point2[], int  noObjectives) {
    int  i;
    int  betterInAnyObjective;

    betterInAnyObjective = 0;
    for (i = 0; i < noObjectives && point1[i] >= point2[i]; i++)
      if (point1[i] > point2[i])
	betterInAnyObjective = 1;
    
    return ((i >= noObjectives) && (betterInAnyObjective>0));
  } //Dominates

  static void  swap(double [][] front, int  i, int  j){
    double  [] temp;
    
    temp = front[i];
    front[i] = front[j];
    front[j] = temp;
  } // Swap 

  
  /* all nondominated points regarding the first 'noObjectives' dimensions
  are collected; the points referenced by 'front[0..noPoints-1]' are
  considered; 'front' is resorted, such that 'front[0..n-1]' contains
  the nondominated points; n is returned */
  static int  filterNondominatedSet(double [][] front, int  noPoints, int  noObjectives){
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
  public static double  surfaceUnchangedTo(double [][] front, int  noPoints, int  objective) {
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
  public static int  reduceNondominatedSet(double [][] front, int  noPoints, int  objective,
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

  public static double calculateHypervolume(double [][] front, int  noPoints,int  noObjectives){
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
    maximumValues = getMaximumValues(paretoTrueFront,numberOfObjectives);

    /*by Markus Wagner: insertion of different hypervolume reference point*/
    if (hypervolumePoint!=Double.NEGATIVE_INFINITY) {
        double[] artificialReferencePoint = new double[numberOfObjectives];
        for (int i = 0; i<numberOfObjectives; i++) {
            artificialReferencePoint[i] = hypervolumePoint;
        }
        maximumValues = artificialReferencePoint;
    }

    minimumValues = getMinimumValues(paretoTrueFront,numberOfObjectives);

    // STEP 2. Get the normalized front
    normalizedFront = getNormalizedFront(paretoFront,
                                                maximumValues,
                                                minimumValues);

    // STEP 3. Inverse the pareto front. This is needed because of the original
    //metric by Zitzler is for maximization problems

    invertedFront = invertedFront(normalizedFront);

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
    maximumValues = getMaximumValues(paretoTrueFront,numberOfObjectives);

//    double[] artificialReferencePoint = new double[numberOfObjectives];
//    for (int i = 0; i<numberOfObjectives; i++) {
//        artificialReferencePoint[i] = 100;
//    }
//    maximumValues = artificialReferencePoint;

    minimumValues = getMinimumValues(paretoTrueFront,numberOfObjectives);
    
    // STEP 2. Get the normalized front
    normalizedFront = getNormalizedFront(paretoFront,
                                                maximumValues,
                                                minimumValues);
    
    // STEP 3. Inverse the pareto front. This is needed because of the original
    //metric by Zitzler is for maximization problems

    invertedFront = invertedFront(normalizedFront);

//    System.out.println("Hypervolume:beforeCalc");
    if (false) System.out.println("hypervolume: marker. " + invertedFront.length + " " +paretoFront.length
            + " " + paretoTrueFront.length + " " + numberOfObjectives + " " +invertedFront[0].length);
    // STEP4. The hypervolumen (control is passed to java version of Zitzler code)
    return calculateHypervolume(invertedFront,invertedFront.length,numberOfObjectives);
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
//    if (args.length < 2) {
//      System.err.println("Error using delta. Type: \n java Hypervolume " +
//                         "<SolutionFrontFile>" +
//                         "<TrueFrontFile> + <numberOfObjectives>");
//      System.exit(1);
//        
//    }
    
    //Create a new instance of the metric
//    CalcHyp qualityIndicator = new CalcHyp();
    //Read the front from the files
    double [][] solutionFront = null; 
//    double [][] trueFront     = null; 
    int numberOfObjectives = 0;
    double[] maximumValues = null;
    double[] minimumValues = null;
    
    boolean convertOneOverValueForWind = false;
    int convertOneOverValueForWindIndex = 1;
    
    if (args.length>1) {
//        trueFront     = qualityIndicator.utils_.readFront(args[1]);
        numberOfObjectives = Integer.parseInt(args[0]);
        
        minimumValues = new double[numberOfObjectives];
        maximumValues = new double[numberOfObjectives];
        for (int i=0; i<numberOfObjectives; i++) {
            minimumValues[i] = Double.parseDouble(args[1+i]);                     // with 2 objectives:   1+0   2+0
            maximumValues[i] = Double.parseDouble(args[1+i+numberOfObjectives]);  //                      1+0+2 2+0+2
//            minimumValues[i] = Integer.parseInt(args[1+i]);                     // with 2 objectives:   1+0   2+0
//            maximumValues[i] = Integer.parseInt(args[1+i+numberOfObjectives]);  //                      1+0+2 2+0+2
        }
        solutionFront = readFront(args[args.length-1]);
    } else {
        solutionFront = readFront("NSGAII_2_70_50.txtPRI_1355966806265.fun");
//        solutionFront = readFront("NSGAII_2_70_50.txtPRI_1355968441887.fun");
        numberOfObjectives = 3;
        minimumValues = new double[]{8000000,-450000,22000};
        maximumValues = new double[]{9000000,-430000,25000};
        convertOneOverValueForWind=true;

        //        solutionFront = readFront("cyc10200NSGAIISingle.array.o967439.1.pop");numberOfObjectives = 2;minimumValues = new double[]{0,0};maximumValues = new double[]{300,0};
        
        
    }
    
    
    if (args.length>1 && args[args.length-2].contains("convertOneOverValueForWind")) {
        convertOneOverValueForWind = true;
        String temp = args[args.length-2];
        convertOneOverValueForWindIndex = Integer.parseInt(temp.substring(temp.length()-1));
    }
    
    if (convertOneOverValueForWind) {
        for (int i=0; i<solutionFront.length; i++) {
//            System.out.println(Arrays.toString(solutionFront[i]));
            solutionFront[i][convertOneOverValueForWindIndex] = -(1d/solutionFront[i][convertOneOverValueForWindIndex]);
//            System.out.println(Arrays.toString(solutionFront[i]));
        }
//                    minimumValues = getMinimumValues(solutionFront, numberOfObjectives);
//            maximumValues = getMaximumValues(solutionFront, numberOfObjectives);
//            System.out.println("min: "+Arrays.toString(minimumValues));System.out.println("max: "+Arrays.toString(maximumValues));
//min: [8091020.746928847, -450668.86690962873, 22797.1196938676]
//max: [8928570.883052545, -431504.28934318776, 24973.320345781616]
    }
   
    
    // STEP 1. Obtain the maximum and minimum values of the Pareto front
//OVERWRITTEN    maximumValues = qualityIndicator.utils_.getMaximumValues(trueFront,numberOfObjectives);
//    double[] artificialReferencePoint = new double[numberOfObjectives];
//    for (int i = 0; i<numberOfObjectives; i++) {
//        artificialReferencePoint[i] = 100;
//    }
//    maximumValues = artificialReferencePoint;

//OVERWRITTEN    minimumValues = qualityIndicator.utils_.getMinimumValues(trueFront,numberOfObjectives);
    // STEP 2. Get the normalized front
    double [][] normalizedFront = getNormalizedFront(solutionFront, maximumValues, minimumValues);
    // STEP 3. Inverse the pareto front. This is needed because of the original
    //metric by Zitzler is for maximization problems
    double [][] invertedFront = invertedFront(normalizedFront);

    // STEP4. The hypervolumen (control is passed to java version of Zitzler code)
    double value = calculateHypervolume(invertedFront,invertedFront.length,numberOfObjectives);
    
    System.out.println(value);  
  } // main

    public CalcHyp() {
    }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
    /**
   * This method reads a Pareto Front for a file.
   * @param path The path to the file that contains the pareto front
   * @return double [][] whit the pareto front
   **/
  public static double [][] readFront(String path) {
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
  public static double [] getMaximumValues(double [][] front, int noObjectives) {
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
  public static double [] getMinimumValues(double [][] front, int noObjectives) {
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
  public static double distance(double [] a, double [] b) {
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
  public static double distanceToClosedPoint(double [] point, double [][] front) {
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
  public static double distanceToNearestPoint(double [] point, double [][] front) {
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
  public static double [][] getNormalizedFront(double [][] front, 
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
  public static double[][] invertedFront(double [][] front) {
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
} // Hypervolume













//
//
//
//
//
//
//
//
//
//class MetricsUtilLocal {
//  
////  /**
////   * This method reads a Pareto Front for a file.
////   * @param path The path to the file that contains the pareto front
////   * @return double [][] whit the pareto front
////   **/
////  public static double [][] readFront(String path) {
////    try {
////      // Open the file
////      FileInputStream fis   = new FileInputStream(path)     ;
////      InputStreamReader isr = new InputStreamReader(fis)    ;
////      BufferedReader br      = new BufferedReader(isr)      ;
////      
////      List<double []> list = new ArrayList<double []>();
////      int numberOfObjectives = 0;
////      String aux = br.readLine();
////      while (aux!= null) {
////        StringTokenizer st = new StringTokenizer(aux);
////        int i = 0;
////        numberOfObjectives = st.countTokens();
////        double [] vector = new double[st.countTokens()];
////        while (st.hasMoreTokens()) {
////          double value = (new Double(st.nextToken())).doubleValue();
////          vector[i] = value;
////          i++;
////        }
////        list.add(vector);
////        aux = br.readLine();
////      }
////            
////      br.close();
////      
////      double [][] front = new double[list.size()][numberOfObjectives];
////      for (int i = 0; i < list.size(); i++) {
////        front[i] = list.get(i);
////      }
////      return front;
////      
////    } catch (Exception e) {
////      System.out.println("InputFacilities crashed reading for file: "+path);
////      e.printStackTrace();
////    }
////    return null;
////  } // readFront
////  
////  /** Gets the maximun values for each objectives in a given pareto
////   *  front
////   *  @param front The pareto front
////   *  @param noObjectives Number of objectives in the pareto front
////   *  @return double [] An array of noOjectives values whit the maximun values
////   *  for each objective
////   **/
////  public static double [] getMaximumValues(double [][] front, int noObjectives) {
////    double [] maximumValue = new double[noObjectives];
////    for (int i = 0; i < noObjectives; i++)
////      maximumValue[i] =  Double.NEGATIVE_INFINITY;
////    
////    
////    for (int i =0; i < front.length;i++ ) {
////      for (int j = 0; j < front[i].length; j++) {
////        if (front[i][j] > maximumValue[j])
////          maximumValue[j] = front[i][j];
////      }
////    }
////    
////    return maximumValue;
////  } // getMaximumValues
////  
////  
////  /** Gets the minimun values for each objectives in a given pareto
////   *  front
////   *  @param front The pareto front
////   *  @param noObjectives Number of objectives in the pareto front
////   *  @return double [] An array of noOjectives values whit the minimum values
////   *  for each objective
////   **/
////  public static double [] getMinimumValues(double [][] front, int noObjectives) {
////    double [] minimumValue = new double[noObjectives];
////    for (int i = 0; i < noObjectives; i++)
////      minimumValue[i] = Double.MAX_VALUE;
////    
////    for (int i = 0;i < front.length; i++) {
////      for (int j = 0; j < front[i].length; j++) {
////        if (front[i][j] < minimumValue[j]) 
////          minimumValue[j] = front[i][j];
////      }
////    }
////    return minimumValue;
////  } // getMinimumValues
////  
////  
////  /** 
////   *  This method returns the distance (taken the euclidean distance) between
////   *  two points given as <code>double []</code>
////   *  @param a A point
////   *  @param b A point
////   *  @return The euclidean distance between the points
////   **/
////  public static double distance(double [] a, double [] b) {
////    double distance = 0.0;
////    
////    for (int i = 0; i < a.length; i++) {
////      distance += Math.pow(a[i]-b[i],2.0);
////    }
////    return Math.sqrt(distance);
////  } // distance
////  
////  
////  /**
////   * Gets the distance between a point and the nearest one in
////   * a given front (the front is given as <code>double [][]</code>)
////   * @param point The point
////   * @param front The front that contains the other points to calculate the 
////   * distances
////   * @return The minimun distance between the point and the front
////   **/
////  public static double distanceToClosedPoint(double [] point, double [][] front) {
////    double minDistance = distance(point,front[0]);
////    
////    
////    for (int i = 1; i < front.length; i++) {
////      double aux = distance(point,front[i]);
////      if (aux < minDistance) {
////        minDistance = aux;
////      }
////    }
////    
////    return minDistance;
////  } // distanceToClosedPoint
////  
////  
////  /**
////   * Gets the distance between a point and the nearest one in
////   * a given front, and this distance is greater than 0.0
////   * @param point The point
////   * @param front The front that contains the other points to calculate the
////   * distances
////   * @return The minimun distances greater than zero between the point and
////   * the front
////   */
////  public static double distanceToNearestPoint(double [] point, double [][] front) {
////    double minDistance = Double.MAX_VALUE;
////    
////    for (int i = 0; i < front.length; i++) {
////      double aux = distance(point,front[i]);
////      if ((aux < minDistance) && (aux > 0.0)) {
////        minDistance = aux;
////      }
////    }
////    
////    return minDistance;
////  } // distanceToNearestPoint
////  
////  /** 
////   * This method receives a pareto front and two points, one with maximun values
////   * and the other with minimun values allowed, and returns a the normalized
////   * pareto front.
////   * @param front A pareto front.
////   * @param maximumValue The maximun values allowed
////   * @param minimumValue The mininum values allowed
////   * @return the normalized pareto front
////   **/
////  public static double [][] getNormalizedFront(double [][] front, 
////                                        double [] maximumValue,
////                                        double [] minimumValue) {
////     
////    double [][] normalizedFront = new double[front.length][];
////    
////    for (int i = 0; i < front.length;i++) {
////      normalizedFront[i] = new double[front[i].length];
////      for (int j = 0; j < front[i].length; j++) {
////        normalizedFront[i][j] = (front[i][j] - minimumValue[j]) /
////                                (maximumValue[j] - minimumValue[j]);
////      }
////    }
////    return normalizedFront;
////  } // getNormalizedFront
////  
////  
////  /**
////   * This method receives a normalized pareto front and return the inverted one.
////   * This operation needed for minimization problems
////   * @param front The pareto front to inverse
////   * @return The inverted pareto front
////   **/
////  public static double[][] invertedFront(double [][] front) {
////    double [][] invertedFront = new double[front.length][];
////    
////    for (int i = 0; i < front.length; i++) {
////      invertedFront[i] = new double[front[i].length];
////      for (int j = 0; j < front[i].length; j++) {
////        if (front[i][j] <= 1.0 && front[i][j]>= 0.0) {
////          invertedFront[i][j] = 1.0 - front[i][j];
////        } else if (front[i][j] > 1.0) {
////          invertedFront[i][j] = 0.0;
////        } else if (front[i][j] < 0.0) {
////          invertedFront[i][j] = 1.0;
////        }
////      }
////    }
////    return invertedFront;
////  } // invertedFront
//
//
////
////
////  /**
////   * Reads a set of non dominated solutions from a file
////   * @param path The path of the file containing the data
////   * @return A solution set WITHOUT the solutions that did not contain enough objective values (may happen when job is killed while file is written)
////   */
////  public SolutionSet readNonDominatedSolutionSet(String path, int numberOfObjectives) {
////    try {
//////        System.out.println("readNonDominatedSolutionSet on "+path);
////
////      /* Open the file */
////      FileInputStream fis   = new FileInputStream(path)     ;
////      InputStreamReader isr = new InputStreamReader(fis)    ;
////      BufferedReader br      = new BufferedReader(isr)      ;
////
////      SolutionSet solutionSet = new NonDominatedSolutionList();
////
////
////      String aux = br.readLine();
////      while (aux!= null) {
////          int fileObjCounter = 0;
////
////        StringTokenizer st = new StringTokenizer(aux);
////        int i = 0;
////        Solution solution = new Solution(st.countTokens());
////        while (st.hasMoreTokens()) {
////          double value = (new Double(st.nextToken())).doubleValue();
////          solution.setObjective(i,value);
////          i++;
////          fileObjCounter++;
////        }
////        if (fileObjCounter==numberOfObjectives) {
////            solutionSet.add(solution);
////          } else {
////            System.out.println("!!one solution dropped because objective values were missing");
////          }
//////        System.out.println("fileObjCounter="+fileObjCounter);
////        aux = br.readLine();
////      }
////      br.close();
//////        System.out.println("done");
////
//////        Ranking r = new Ranking(solutionSet);
//////        solutionSet = r.getSubfront(0);
////
////      return solutionSet;
////    } catch (Exception e) {
////      System.out.println("jmetal.qualityIndicator.util.readNonDominatedSolutionSet: "+path);
////      e.printStackTrace();
////    }
////    System.out.println("problem");
////    return null;
////  } // readNonDominatedSolutionSet
////
//
//
////
////
////  /**
////   * Reads a set of non dominated solutions from a file
////   * @param path The path of the file containing the data
////   * @return A solution set
////   */
////  public SolutionSet readNonDominatedSolutionSet(String path) {
////
////      File test = new File(".");
////                System.out.println("readNonDominatedSolutionSet "+test.getAbsolutePath() + "  "+path);
////                path = path.replace("\\", "//");
////                System.out.println("readNonDominatedSolutionSet "+test.getAbsolutePath() + "  "+path);
////
////    try {
////      /* Open the file */
////      FileInputStream fis   = new FileInputStream(path)     ;
////      InputStreamReader isr = new InputStreamReader(fis)    ;
////      BufferedReader br      = new BufferedReader(isr)      ;
////      
////      SolutionSet solutionSet = new NonDominatedSolutionList();
////      
////      String aux = br.readLine();
////      while (aux!= null) {
////        StringTokenizer st = new StringTokenizer(aux);
////        int i = 0;
////        Solution solution = new Solution(st.countTokens());
////        while (st.hasMoreTokens()) {
////          double value = (new Double(st.nextToken())).doubleValue();
////          solution.setObjective(i,value);
////          i++;
////        }
////        solutionSet.add(solution);
////        aux = br.readLine();
////      }
////      br.close();
////      return solutionSet;
////    } catch (Exception e) {
////      System.out.println("jmetal.qualityIndicator.util.readNonDominatedSolutionSet: "+path);
////      e.printStackTrace();
////    }
////    return null;
////  } // readNonDominatedSolutionSet
//  
//}