/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author wagner
 */
public class TestDELETABLE {
    public static void main (String[] args) {
        int[] dims = new int[]{100,1000,10000,15112,18512,33810,85900};
//        int[] dims = new int[]{100,1000,10000};
        
        for (int d:dims) {
            
            double[][] coords = new double[d][2];
            
            //populate with random x-y coordinates
            for (int i=0; i<coords.length; i++) {
                coords[i][0] = Math.random();
                coords[i][1] = Math.random();
            }
            
            long starttime = System.currentTimeMillis();
            
            // calculate all distances
            double[][] dm = new double[d][d];
            for (int i=0; i<dm.length; i++)
                for (int j=0; j<dm[i].length; j++) {
                    dm[i][j] = Math.sqrt( 
                            Math.pow( coords[i][0]-coords[j][0] , 2) + 
                            Math.pow( coords[i][1]-coords[j][1] , 2) 
                    );
                }
            
            long stoptime = System.currentTimeMillis();
            System.out.println(d+" cities took ms "+(stoptime-starttime)+" for distance matrix calculation");
            starttime = System.currentTimeMillis();
            
            // do an inefficient second pass to determine the max value
            double max = 0;
            for (int i=0; i<dm.length; i++)
                for (int j=0; j<dm[i].length; j++) {
                    max=(dm[i][j]>max?dm[i][j]:max);
                }
                    
            stoptime = System.currentTimeMillis();
            System.out.println(d+" cities took ms "+(stoptime-starttime)+" for one pass through distance matrix to determine max distance max="+max);
        }
    }
}
