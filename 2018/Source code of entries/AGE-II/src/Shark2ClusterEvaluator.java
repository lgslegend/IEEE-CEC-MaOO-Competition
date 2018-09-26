
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author wagner
 */
public class Shark2ClusterEvaluator {

    
    public static void main (String[] args) throws Exception {


        boolean debugPrint = !false;

        String subDir = "mu25";
        
        FilenameFilter isFun = new FilenameFilter() {    
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        };
        FilenameFilter isDir = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean result = true;
                try {
                    result = dir.getCanonicalFile().isDirectory()
                            && !name.endsWith("tex") && !name.endsWith("csv") && !name.endsWith("FUN") && !name.endsWith("DS_Store") && !name.contains("svn")// this line should be unnecessary, but the isDirectory() fails regularly
                            ;
                } catch (IOException ex) {
                    Logger.getLogger(Shark2ClusterEvaluator.class.getName()).log(Level.SEVERE, null, ex);
                }
                return result;
            }   
        };
        
        if (args.length == 0) {
            System.out.println("arguments: <subDir> (default:"+subDir+")\n\n");
        } if (args.length == 1) {
            subDir = args[0];
        }

        File currentDir = new File(".");
        File currentDirClusterout = new File(".");
        if (subDir!="") currentDir = new File(subDir+"/");
//        if (subDir!="") currentDir = new File(subDir+System.getProperty("path.separator"));

//        currentDir = new File(currentDir.); 
        if (!debugPrint) System.out.println(currentDir.getCanonicalPath());
        
        File[] algorithmList = currentDir.listFiles(isDir);
        if (debugPrint) {
            for (File f:algorithmList) System.out.println(f.getName());
        }

        System.out.println("// number of files in "+currentDir.getCanonicalPath()+": "+algorithmList.length);
        
        if (algorithmList.length == 0) {
            System.out.println("no suitable files in " + currentDir.getCanonicalPath());
            System.exit(0);
        }
        
        int count = 0;
        
        TreeMap top = new TreeMap();

        //do algorithm level
        for (File a:algorithmList) {
            
            System.out.println("xx"+a.getName());
            
            File[] functionList = a.listFiles(isDir);
            
            if (debugPrint) {
                for (File i:functionList) System.out.println(" "+i.getName());
            }
            
            //do function level
            for (File f:functionList) {
                
                File[] inputVariablesList = f.listFiles(isDir);
                
                if (debugPrint) {
                    for (File i:inputVariablesList) System.out.println("  "+i.getName());
                }
                
                //do input variable numbers
                for (File iv:inputVariablesList) {
                    
                    File[] objectiveVariablesList = iv.listFiles(isDir);
                    
                    if (debugPrint) {
                        for (File i:objectiveVariablesList) System.out.println("   "+i.getName());
                    }
                    
                    //do objective number list
                    for (File o:objectiveVariablesList) {
                        
                        File[] repetitionsList = o.listFiles(isDir);

                        if (debugPrint) {
                            for (File i:repetitionsList) System.out.println("    "+i.getName());
                        }
                        
                        //do repetitions list
                        for (File run:repetitionsList) {
                            
                            File[] populations = run.listFiles(isFun);
                            
                            if (debugPrint) {
                                for (File i:populations) System.out.println("     "+i.getName());
                            }
                            
                            
                            //now do the actual work
                            
                            //1. determine latest file
                            int newestEvaluations = 0;
                            int newestFileIndex = 0;
                            for (int i = 0; i<populations.length; i++) {
                                String tName = populations[i].getName();
                                
                                if (tName.startsWith("Final")) {
                                    newestFileIndex = i;
                                    break;
                                }
                                
                                int tEvals = Integer.parseInt(tName.substring(0, tName.indexOf(".")));
                                if (tEvals>newestEvaluations) {
                                    newestEvaluations = tEvals;
                                    newestFileIndex = i;
                                }
                            } //latest file now in populations[newestFileIndex]
                            
                            //2. make target file
                            String ts = f.getName() + "_" + o.getName() + "D-" + a.getName() + "-";
                            ts += System.currentTimeMillis() //"-" + iv.getName() 
                                    + "-" + run.getName() + "-"+populations[newestFileIndex].getName();
                            ts += ".FUN";
                            File source = populations[newestFileIndex];
                            File target = new File(currentDir.getCanonicalFile()+"/"+ts);
                            
                            if (true || debugPrint) {
                                System.out.println(source.getCanonicalPath());
                                System.out.print(target.getCanonicalPath());
                            }
                            
                            //3. copy to currentDir, ommitting the first line
                            try{
                                //source file
                                FileInputStream fstream = new FileInputStream(source);
                                DataInputStream in = new DataInputStream(fstream);
                                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                                //target file
                                PrintWriter out = new PrintWriter(new FileWriter(target));  
                                
                                //Read File Line By Line, but ommit first line
                                String strLine;
                                br.readLine();
                                while ((strLine = br.readLine()) != null)   {
                                    //skip empty lines
                                    if (strLine.length()==0) {
                                        continue;
                                    } else {
                                        out.println(strLine);
                                    }
                                }
                                //Close the input stream
                                in.close();
                                out.close();  

                                
                            }catch (Exception e){//Catch exception if any
                                System.err.println("Error: " + e.getMessage());
                            }
                            
                            System.out.println(" -> created");
                            
                            
                            
                            
                        }
                    }
                    
                }
            }
            
            
        }
        
        
    }//end of main
}
