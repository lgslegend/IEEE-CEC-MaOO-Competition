#! /bin/bash

# WICHTIG: bei Definitionen keine Leerzeichen lassen

maxEvals=100000
mu=( 10 )
epsGridWidth=( 0.01 )

algList=(bfnw.sh ibea.sh nsga2.sh pesa2.sh smsemoa.sh spea2.sh)

#probList=( DTLZ1_4D  DTLZ1_6D  DTLZ1_8D    DTLZ2_4D  DTLZ2_6D  DTLZ2_8D  DTLZ2_10D  )
probList=(  DTLZ1_4D  DTLZ1_6D  DTLZ1_8D    DTLZ2_4D  DTLZ2_6D  DTLZ2_8D  DTLZ2_10D    DTLZ3_4D  DTLZ3_6D  DTLZ3_8D  DTLZ3_10D    DTLZ4_4D  DTLZ4_6D  DTLZ4_8D  DTLZ4_10D )  
#pfList=(   DTLZ1.4D.1000.pf  DTLZ1.6D.1000.pf  DTLZ1.8D.1000.pf    DTLZ2.4D.1000.pf  DTLZ2.6D.1000.pf  DTLZ2.8D.1000.pf  DTLZ2.10D.1000.pf   )
#pfList=(   DTLZ1.4D.10000.pf DTLZ1.6D.10000.pf DTLZ1.8D.10000.pf   DTLZ2.4D.10000.pf DTLZ2.6D.10000.pf DTLZ2.8D.10000.pf DTLZ2.10D.10000.pf   )  
pfList=(   DTLZ1.4D.100000.pf DTLZ1.6D.100000.pf DTLZ1.8D.100000.pf  DTLZ2.4D.100000.pf DTLZ2.6D.100000.pf DTLZ2.8D.100000.pf DTLZ2.10D.100000.pf   DTLZ2.4D.100000.pf DTLZ2.6D.100000.pf DTLZ2.8D.100000.pf DTLZ2.10D.100000.pf    DTLZ2.4D.100000.pf DTLZ2.6D.100000.pf DTLZ2.8D.100000.pf DTLZ2.10D.100000.pf   )

len=${#probList[*]}
i=0

selections=( BinaryTournament )  # or RandomSelection

base='/home/mwagner/bfnw/'

clusterout=${base}$1'-clusterout'
mkdir $clusterout 2>/dev/null

while [ $i -lt $len ]; 
  do
  for m in ${mu[@]}
    do
    for s in ${selections[@]}
      do
      for eps in ${epsGridWidth[@]}
        do
        for alg in ${algList[@]}
          do
        location=${base}${alg}
   #    qsub -l h_rt=00:00:10 -o /home/koetzing/clustertest/clusterout -j y /home/koetzing/clustertest/doit.sh
        qsub -l h_rt=4:00:00 -o $clusterout -j y $location ${probList[$i]} ${pfList[$i]} $m $maxEvals $s $eps $1    
   # $1 is the subDirID as cmdLineParameter
        done
      done
    done
  done
  let i++
done

