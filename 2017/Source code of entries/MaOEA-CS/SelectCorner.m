function CornerPopulation = SelectCorner(NodominatedPopulation, W,idealPoint)
% corner selection
% Author: Sun Haoran

%% The first kind of corner solution
    CornerPopulation = [];
    selIndex = [] ;
    for i = 1:size(W,2)
        nPopSize = length(NodominatedPopulation);
        k = (NodominatedPopulation.objs - repmat(idealPoint,nPopSize,1)) * W(:,i)./ norm(W(:,i));
        perpenVecs = NodominatedPopulation.objs - repmat(idealPoint,nPopSize,1) - k.* repmat(W(:,i)',nPopSize,1);
        perpDist = sum(abs(perpenVecs).^2,2).^(1/2);
        [~,index] = min(perpDist);
        CornerPopulation = [CornerPopulation NodominatedPopulation(index)];
        selIndex = [selIndex,index];
    end
    
    %% 面边界解
    %% The second type of corner solution
    NodominatedPopulation(selIndex) = [];
    [~,index] = min(NodominatedPopulation.objs,[],1);
    index = unique(index) ;
    PreAddPop = NodominatedPopulation(index);
    sel = [];
    maxPoint = max(CornerPopulation.objs,[],1);
    for i = 1:length(PreAddPop)
        if sum(PreAddPop(i).obj) < 1.5 * sum(maxPoint) && sum(PreAddPop(i).obj > maxPoint) > 0 
            sel = [sel, i];
        end
    end
   CornerPopulation = [CornerPopulation, PreAddPop(sel)] ;
   
   %% 去重
   %% reduce the same corner solution
   [~,ia,~] = unique(CornerPopulation.objs,'rows');
   CornerPopulation = CornerPopulation(ia);
end

