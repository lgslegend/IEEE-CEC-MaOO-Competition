function [Population, CornerPopulation] = DSASelection(Population,N,W)
% DSASELECTION Dominated Space-reduction Angle based selection
% Author: Sun Haoran
    [FrontNo,MaxFNo] = NDSort(roundn(Population.objs,-4),N);
    idealPoint = min(Population.objs,[],1);
    NondominatedPopulation = Population(FrontNo == 1);
    CornerPopulation = SelectCorner(NondominatedPopulation,W,idealPoint);
    nadPoint = max(CornerPopulation.objs,[],1);
    
    %% selection
    NewPopulation = NondominatedPopulation;
    if length(NewPopulation) > N
        nPopSize = size(NewPopulation,1);
        compareFlag = NewPopulation.objs > repmat(nadPoint,nPopSize,1);
        A = sum(compareFlag,2) > 0;
        NewPopulation = NewPopulation(A == 0);
        if length(NewPopulation) < N
            UnselectedPopulation = NondominatedPopulation(A==1);
            nUnselSize = length(UnselectedPopulation);
            normObjs = UnselectedPopulation.objs - repmat(idealPoint,nUnselSize,1);
            dist = sum(abs(normObjs).^2,2).^(1/2);
            [~,index] = sort(dist);
            remainCount = N - length(NewPopulation);
            Population = [NewPopulation, UnselectedPopulation(index(1:remainCount))];
        elseif length(NewPopulation) > N
            Population = AngleBasedSelect(NewPopulation, CornerPopulation,N,idealPoint,nadPoint);
        else 
            Population = NewPopulation;
        end
    elseif length(NewPopulation) < N
        UnselectedPopulation = Population(FrontNo>1);
        nUnselSize = length(UnselectedPopulation);
        normObjs = UnselectedPopulation.objs - repmat(idealPoint,nUnselSize,1);
        dist = sum(abs(normObjs).^2,2).^(1/2);
        [~,index] = sort(dist);
        remainCount = N - length(NewPopulation);
        Population = [NewPopulation, UnselectedPopulation(index(1:remainCount))];
    else 
        Population = NewPopulation ;
    end
end

