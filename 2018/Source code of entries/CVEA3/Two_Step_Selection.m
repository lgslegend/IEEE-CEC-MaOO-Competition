function Population = Two_Step_Selection(Population,N,Zmin,Type,Hard)
% The selection procedure in BCV

%--------------------------------------------------------------------------
% The copyright of the PlatEMO belongs to the BIMK Group. You are free to
% use the PlatEMO for research purposes. All publications which use this
% platform or any code in the platform should acknowledge the use of
% "PlatEMO" and reference "Ye Tian, Ran Cheng, Xingyi Zhang, and Yaochu
% Jin, PlatEMO: A MATLAB Platform for Evolutionary Multi-Objective
% Optimization, 2016".
%--------------------------------------------------------------------------

% Copyright (c) 2016-2017 BIMK Group

CV = sum(max(0,Population.cons),2);
if sum(CV==0) > N
    %% Selection among feasible solutions
    Population = Population(CV==0);
    % Non-dominated sorting
    Population=Two_Selection(Population,N,Zmin,Type,Hard);
else
    %% Selection including infeasible solutions
    [~,rank]   = sort(CV);
    Population = Population(rank(1:N));
end
end

function NextPopulation=Two_Selection(Population,N,Zmin,Type,Hard)
PopObj        = Population.objs;
[Num,M]       = size(PopObj);
if Hard
    Step1Num  = M*4;
else
    Step1Num  = ceil(N/2);
end
% Step1Num      = ceil(N/2); %%%  第一步需要选择的个体数目
alpha         = 1e-4; %%% 防止分母取值为0而确定的最小值
% Normalization
PopObj        = PopObj - repmat(Zmin,Num,1)+alpha;

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

ResultInd=zeros(N,1);
%%%%   确定第一层个体的数目  %%%%%
FirstLevel=find(min(Similar,[],2)>=0);
Len_First=length(FirstLevel);
if Len_First<=N
    ResultInd(1:Len_First)=FirstLevel;
    NeedAddNum=N-Len_First;
    if NeedAddNum>0
        ComValue=min(Similar(:,FirstLevel),[],2);
        ComValue(FirstLevel)=-Inf;
        for j=(Len_First+1):N
            [~,AddInd]=max(ComValue);
            ResultInd(j)=AddInd;
            ComValue=min([ComValue,Similar(:,AddInd)],[],2);
            ComValue(AddInd)=-Inf;
        end
    end
    NextPopulation=Population(ResultInd);
else
    %%%%%%   只在第一层中考虑    %%%%%%%%
    Population=Population(FirstLevel);
    PopObj=PopObj(FirstLevel,:);
    Now_Similar=Similar(FirstLevel,FirstLevel);
    Similar=Now_Similar;
    ResultInd=1:Len_First;
    %%%%%   第一步考虑收敛性   %%%%%%%%%
    Step1_DelectNum=Len_First - Step1Num;
    [Values,Neightboor]=min(Similar,[],2);
    CV_Values2=Values;
    CV_Neightboor2=Neightboor;
    Have_Delect=zeros(1,Step1_DelectNum);
    for k=1:Step1_DelectNum
        [~,Del_Ind]=min(Values);
        Have_Delect(k)=Del_Ind;
        Similar(Del_Ind,:)=Inf;
        Similar(:,Del_Ind)=Inf;
        Need_Updata=find(Neightboor==Del_Ind);
        L_Need=length(Need_Updata);
        if L_Need>0
            [Values(Need_Updata),Neightboor(Need_Updata)]=min(Similar(Need_Updata,:),[],2);
        end
        Values(Del_Ind)=Inf;
    end
    
    ResultInd(Have_Delect)=[];
    %%%%%   第二步考虑散布性   %%%%%%%%%%
    Zmax=max(PopObj(ResultInd,:),[],1);
    Zmax(Zmax<1)=1;
    PopObj=PopObj./repmat(Zmax,Len_First,1);
    
    %%%%%%%%  映射到超立方体之内   %%%%%%%%%%
    ChangeInd=find(max(PopObj,[],2)>1);
    PopObj(ChangeInd,:)=PopObj(ChangeInd,:)./repmat(sum(PopObj(ChangeInd,:),2),1,M)*alpha;
    if Type==1
        PopObj=PopObj+100;
    end
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%%%%%%%%%%%%   采用角度   %%%%%%%%%%%%%%
    AllInds=1:Len_First;
    DelectInd2=[];
    Normal_PopObj=PopObj./repmat(sqrt(sum(PopObj.^2,2)),1,M);
    CosValue=Normal_PopObj*Normal_PopObj';
    CosValue=CosValue-2*eye(Len_First);
    [Dis_Values,Dis_Neightboor]=max(CosValue,[],2);
    
    for l=1:(Len_First-N)
        [~,individual1]=max(Dis_Values);
        
        individual2=Dis_Neightboor(individual1);
        
        if CV_Values2(individual1)<CV_Values2(individual2)
            DelID=individual1;
        else
            DelID=individual2;
        end
        
        DelectInd2=[DelectInd2,DelID];
        CosValue(DelID,:)=-Inf;
        CosValue(:,DelID)=-Inf;
        Need_Updata_Cos=find(Dis_Neightboor==DelID);
        L_Need_Cos=length(Need_Updata_Cos);
        if L_Need_Cos>0
            [Dis_Values(Need_Updata_Cos),Dis_Neightboor(Need_Updata_Cos)]=max(CosValue(Need_Updata_Cos,:),[],2);
        end
        Dis_Values(DelectInd2)=-Inf;
        
        Now_Similar(DelID,:)=Inf;
        Now_Similar(:,DelID)=Inf;
        Need_Updata_CV=find(CV_Neightboor2==DelID);
        L_Need_CV=length(Need_Updata_CV);
        if L_Need_CV>0
            [CV_Values2(Need_Updata_CV),CV_Neightboor2(Need_Updata_CV)]=min(Now_Similar(Need_Updata_CV,:),[],2);
        end
    end
    AllInds(DelectInd2)=[];
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    NextPopulation=Population(AllInds);
end
end
