function MatingPool = CVEA_MatingPool(Population,CrossP,Global)
    PopObj=Population.objs;
    [Num,M]=size(PopObj);
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
    [CV,Neighboors]=min(Similar,[],2);
    SLen=length(find(CV>=0));
    
    if Global.gen<M && SLen<0.8*Global.N
        Med=median(PopObj,1);
        FirstLevel=find(sum(PopObj<=repmat(Med,Num,1),2)==M);
        if isempty(FirstLevel)
            FirstLevel=find(sum(PopObj<=repmat(2*Med,Num,1),2)==M);
            if isempty(FirstLevel)
                FirstLevel         = find(CV>=0);
            end
        end
    else
        FirstLevel         = find(CV>=0);
    end
    
    
    Len_First=length(FirstLevel);
    Neighboors=Neighboors(FirstLevel);
    
    CrossNum=min([floor(Global.N/2),Len_First]);
    
    RandInd=randperm(Len_First);
    AllInd=RandInd(1:CrossNum);
    SpouseID=Neighboors(AllInd);
    ChnageInd=find(rand(1,CrossNum)>CrossP);
    SpouseID(ChnageInd)=FirstLevel(ceil(rand(1,length(ChnageInd))*Len_First));
    
    
    AllInd=reshape(FirstLevel(AllInd),1,CrossNum);
    SpouseID=reshape(SpouseID,1,CrossNum);
    MatingPool= [AllInd,SpouseID];
end