function CVEA3(Global)
% <algorithm> <A-G>
% CVEAT:A Cost-Value-Based Evolutionary Many-Objective Optimization Algorithm
% With Two-Step Selection Strategy
% CrossP --- 0.7 ---    The probability of crossover between each region

%--------------------------------------------------------------------------
% The copyright of the PlatEMO belongs to the BIMK Group. You are free to
% use the PlatEMO for research purposes. All publications which use this
% platform or any code in the platform should acknowledge the use of
% "PlatEMO" and reference "Ye Tian, Ran Cheng, Xingyi Zhang, and Yaochu
% Jin, PlatEMO: A MATLAB Platform for Evolutionary Multi-Objective
% Optimization, 2016".
%--------------------------------------------------------------------------

% Copyright (c) 2016-2017 BIMK Group

%% Parameter setting
CrossP = Global.ParameterSet(0.7);

%% Generate the random population
Population   = Global.Initialization();
Zmin         = min(Population(all(Population.cons<=0,2)).objs,[],1);

PopObj=Population.objs;
[Num,~]=size(PopObj);
alpha=1e-4;
PopObj=PopObj+alpha;

Similar       = ones(Num,Num);%%%%%  相似度矩阵
for i=1:1:Num
    Xi=PopObj(i,:);
    CV=PopObj./repmat(Xi,Num,1)-1;  %%% 作比
    MaxCV=max(CV,[],2);
    MinCV=min(CV,[],2);
    DomInds=find(MaxCV<=0);
    MaxCV(DomInds)=MinCV(DomInds);
    Similar(i,:)=MaxCV';
    Similar(i,i)=Inf;
end

[CV,~]=min(Similar,[],2);

FirstLevel=find(CV>=0);
Len_First=length(FirstLevel);

if Len_First<(Global.M*4)
    Type=0;
else
    Type=1;
end
PM=1;
if Global.D<10
    PM=0.05*Global.D;
end

if Global.maxgen>3000
    Hard=1;
else
    Hard=0;
end
%% Optimization
while Global.NotTermination(Population)
    
    MatingPool = CVEA_MatingPool(Population,CrossP,Global);
    
    
%         MProb=min([3+floor(50*Global.gen/Global.maxgen),20]);
%         CProb=2*MProb;
%         Offspring = Global.Variation(Population(MatingPool));
    
    if Hard
        MProb=20;
        CProb=20;
        if mod(Global.gen,20)
           MProb=5;
        end
        if mod(Global.gen,25)
           CProb=40;
        end
    else
        MProb=20;
        CProb=20;
        if mod(Global.gen,20)
            if Global.D>10
                MProb=5;
            else
                CProb=5;
            end
        end
    end

    
    
    
    Offspring = Global.Variation(Population(MatingPool),length(MatingPool),@IEAreal,{1,CProb,PM,MProb});
    Zmin       = min([Zmin;Offspring(all(Offspring.cons<=0,2)).objs],[],1);
    
    Population = Two_Step_Selection([Population,Offspring],Global.N,Zmin,Type,Hard);
end
end