#! /bin/bash

# WICHTIG: bei Definitionen keine Leerzeichen lassen

maxEvals=100000
mu=( 10 )
epsGridWidth=( 0.001 )

algList=(bfnw.sh ibea.sh nsga2.sh pesa2.sh smsemoa.sh spea2.sh)

probList=( WFG2       ZDT1    DTLZ1       )
pfList=(   WFG2.2D.pf ZDT1.pf DTLZ1.3D.pf )
len=${#probList[*]}
i=0

selections=( BinaryTournament )  # or RandomSelection

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
        base='/home/mwagner/bfnw/'
        location=${base}${alg}
   #    qsub -l h_rt=00:00:10 -o /home/koetzing/clustertest/clusterout -j y /home/koetzing/clustertest/doit.sh
        qsub -l h_rt=4:00:00 -o /home/mwagner/bfnw/clusterout -j y $location ${probList[$i]} ${pfList[$i]} $m $maxEvals $s $eps $1    
   # $1 is the subDirID as cmdLineParameter
        done
      done
    done
  done
  let i++
done

