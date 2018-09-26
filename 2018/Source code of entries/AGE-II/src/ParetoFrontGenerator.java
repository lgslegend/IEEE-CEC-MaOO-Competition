
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mwagner
 */
public class ParetoFrontGenerator {

    static boolean debugPrint = false;

    public static void main(String[] args) {

        if (!true) {
            mainOLD(args);
        } else {

        String[] functions = { "DTLZ1", "DTLZ2" };
//        int[] dims = { 2 };
//        int[] dims = { 3,5 };
        int[] dims = { Integer.parseInt(args[0]) };
//        int[] samples = { 1000 };
        int[] samples = { 1000, 10000, 100000, 1000000 };

        for(int i=0; i<functions.length; i++)
            for(int j=0; j<dims.length; j++)
                for(int k=0; k<samples.length; k++) {
                    String[] currentSetting = new String[] { functions[i], String.valueOf(dims[j]), String.valueOf(samples[k]) };
                    System.out.println(Arrays.toString(currentSetting));
                    mainOLD(currentSetting);
                }

        }
    }



    public static void mainOLD(String[] args) {
        String functionName = "";
        int dimensions = 0;
        int numberOfPoints = 0;
        String targetFileName;

        if (args.length == 0) {
            functionName = "DTLZ2";
            dimensions = 3;
            numberOfPoints = 1000;
        } else if (args.length == 3) {
            functionName = args[0];
            dimensions = Integer.parseInt(args[1]);
            numberOfPoints = Integer.parseInt(args[2]);
        } else {
            System.out.println("incorrect numnber of parameters. EXIT");
            System.exit(0);
        }


        targetFileName = functionName + "." + dimensions + "D." + numberOfPoints + ".pf";
//        targetFileName = "originalParetoFronts//";
        File targetFile = new File(targetFileName);

        String currentPoint = "";

        if (targetFile.exists()) {
            try {
                System.out.println(targetFile.getCanonicalPath() + ": file exists already.");
            } catch (IOException ex) {
                Logger.getLogger(ParetoFrontGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(targetFile, false)); // use false to NOT APPEND
            for (int i = 0; i<numberOfPoints; i++) {
                if (functionName=="DTLZ1") {
//                    currentPoint = generateDTLZ1PointKarl(dimensions);
                    currentPoint = generateDTLZ1PointDirichlet(dimensions);
                } else if (functionName=="DTLZ2") {
                    currentPoint = generateDTLZ2Point(dimensions);
                } else {
                    System.out.println("unknown function. EXIT");
                    System.exit(0);
                }
                
                out.write(currentPoint + "\n");
            }
            out.close();



        } catch (IOException ex) {
            Logger.getLogger(ParetoFrontGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String generateDTLZ1PointKarl(int dimensions) {
        String result = "";
        double[] point = new double[dimensions];

        point = generateDTLZ1PointRecursive(point, dimensions);

        // divide by 2 to go from 1-simplex to 0.5-simplex
        double sumOverAllCoordinates = 0;
        for (int i=0; i<point.length; i++) {
            point[i] = point[i]/2.0;
            sumOverAllCoordinates += point[i];
        }
        if (debugPrint) System.out.println(sumOverAllCoordinates);

        for (double d : point) result+=d+" ";
        return result;
    }

    // karl

public static double[] generateDTLZ1PointRecursive(double[] y, int d) {
//       if (d==1) {
       if (d==0) {
           y[0] = 1;
       } else {
           y[d-1] = 1- Math.pow( (1.0-Math.random()), 1.0/d)  ;
           y = generateDTLZ1PointRecursive(y, d-1);

           for (int i=0; i<d-1; i++) {
               y[i] = (1.0-y[d-1])*y[i];
           }
       }

       return y;
   }



    public static String generateDTLZ1PointDirichlet(int dimensions) {
        String result = "";
        double[] point = new double[dimensions];

//    * Generate K unit-exponential distributed random draws x1, ..., xK.
//          o This can be done by generating K uniform random draws yi from the open interval (0,1] and setting xi=-ln(yi).
//    * Set S to be the sum of all the xi.
//    * The K coordinates t1, ..., tK of the final point on the unit simplex are given by ti=xi/S.

        double sum = 0;
        for (int i=0; i<point.length; i++) {
            point[i] = Math.random();
            point[i] = - Math.log(point[i]);
            sum += point[i];
        }
        for (int i=0; i<point.length; i++) {
            point[i] = point[i]/sum;
        }


        // divide by 2 to go from 1-simplex to 0.5-simplex
        double sumOverAllCoordinates = 0;
        for (int i=0; i<point.length; i++) {
            point[i] = point[i]/2.0;
            sumOverAllCoordinates += point[i];
        }
        if (debugPrint) System.out.println(sumOverAllCoordinates);

        for (double d : point) result+=d+" ";
        return result;
    }










    public static double rand01() {
        return Math.random();
    }

    public static double randn() {
    double V1 = 0,V2;
    double S=2.;
    while(S>=1.){
        V1=2.*rand01()-1.;
        V2=2.*rand01()-1.;
        S=Math.pow(V1,2)+Math.pow(V2,2);
    }
    double X1=V1*Math.sqrt((-2.*Math.log(S))/S);            // log: which base?
    return X1;
    }


    public static String generateDTLZ2Point(int dimensions) {
        String result = "";
        double[] point = new double[dimensions];

        // TODO
    
    // fast uniform sphere
//    REP(i,n) {                                    //#define REP(i,n) for(int i=0;i<(n);++i)
//    for(int i=0;i<n;++i) {
        double s=0.;
//        REP(j,d) {
            for (int j=0; j<point.length; j++) {

            point[j]=Math.abs(randn());
            s+=point[j]*point[j];
//            s+=front[i][j]*front[i][j];
        }
        s=Math.sqrt(s);
//        REP(j,d) front[i][j]/=s;
        for (int j=0; j<point.length; j++)  point[j]/=s;
//    }


        double sumOverAllCoordinatesSquares = 0;
        for (int i=0; i<point.length; i++) {
//            point[i] = point[i]/2.0;
            sumOverAllCoordinatesSquares += point[i] * point[i];
        }
        
        for (double d : point) result+=d+" ";
        if (debugPrint) System.out.println(sumOverAllCoordinatesSquares + ": "+result);
        return result;
    }



    




/*
    define REP(i,n) for(int i=0;i<(n);++i)

inline double rand01(void) {
    return double(rand())/(RAND_MAX+1.0);
}

inline double randn(void) {
    double V1,V2;
    double S=2.;
    while(S>=1.){
        V1=2.*rand01()-1.;
        V2=2.*rand01()-1.;
        S=pow(V1,2)+pow(V2,2);
    }
    double X1=V1*sqrt((-2.*log(S))/S);
    return X1;
}


switch(fronttype) {
case 0:  // slow uniform plane
    REP(i,n) {
        do{
            front[i][d-1]=1.;
            REP(j,d-1) {
                front[i][j]=rand()/(RAND_MAX+1.0);
                front[i][d-1]-=front[i][j];
            }
        }while(front[i][d-1]<0);
    }
    break;
case 1:  // fast uniform sphere
    REP(i,n) {
        double s=0.;
        REP(j,d) {
            front[i][j]=fabs(randn());
            s+=front[i][j]*front[i][j];
        }
        s=sqrt(s);
        REP(j,d) front[i][j]/=s;
    }
    break;
case 2: // fast non-uniform plane
    REP(i,n) {
        double s=0.;
        REP(j,d) {
            front[i][j]=fabs(randn());
            s+=front[i][j];
        }
        REP(j,d) front[i][j]/=s;
    }
    break;
case 3: // sparse non-dominated points randomly chosen in [0,1]^d
    nn=0;
    do{
        FOR(i,nn,n-1) REP(j,d) front[i][j]=fabs(rand01());
        nn = FilterNondominatedSet(front, n, d-1);
    }while(nn!=n);
    break;
case 4: // sparse non-dominated points randomly chosen in [0,1]^d
    nn=0;
    do{
        FOR(i,nn,n-1) REP(j,d) front[i][j]=fabs(randn());
        nn = FilterNondominatedSet(front, n, d-1);
    }while(nn!=n);
    break;
case 5:  // fast non-uniform concave front
    REP(i,n) {
        double s=0.;
        REP(j,d) {
            front[i][j]=fabs(randn());
            s+=sqrt(front[i][j]);
        }
        s=s*s;
        REP(j,d) front[i][j]/=s;
    }
    break;
}

*/

}
