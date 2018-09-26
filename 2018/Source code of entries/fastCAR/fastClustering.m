function [Population, Active_Reference_Points, Inactive_Reference_Points, Cluster] = BiCC_Selection(Population, Global, Z, Cluster)
	%% Find the non-dominated solutions, NDSort is too costly
    PopObj = [Population.objs];
    PopCon = [Population.cons];
    [FrontNo, ~] = SNDSort(PopObj, PopCon, Global.N);
    
	%% Find the non-dominated solutions
    Non_Dominated_index = find(FrontNo == 1);
	Dominated_index = setdiff(1: numel(FrontNo), Non_Dominated_index);
    Violated = find(PopCon > 0);
    if ~isempty(Violated)
        Dominated_index = setdiff(Dominated_index, Violated);
    end

    %% Normalize
	PopObj = Normalize(PopObj, Global);
    
    %% Associate each solution with one reference point
    P = PopObj(Non_Dominated_index, :);
    % Calculate the distance of each solution to each reference vector
    Cosine = 1 - pdist2(P, Z, 'cosine');
    Distance2ReferencePoints = repmat(sqrt(sum(P .^ 2, 2)), 1, size(Z, 1)) .* sqrt(1 - Cosine .^ 2);
    
    %% Assign the non-dominated solutions to reference points
    Allocation = zeros(1, numel(Non_Dominated_index));
    for i = 1: numel(Non_Dominated_index)
        [~, Allocation(i)] = min(Distance2ReferencePoints(i, :));
        Cluster(Allocation(i)).ND_index = [Cluster(Allocation(i)).ND_index; Non_Dominated_index(i)];
        %Cluster(Allocation(i)).ND_indicator = [Cluster(Allocation(i)).ND_indicator, sqrt(norm(PopObj(Non_Dominated_index(i), :)) ^ 2 - Distance2ReferencePoints(i, Allocation(i)) ^ 2) + 5 * Distance2ReferencePoints(i, Allocation(i))];
        Cluster(Allocation(i)).ND_indicator = [Cluster(Allocation(i)).ND_indicator, mean(PopObj(Non_Dominated_index(i), :)) + 5 * Distance2ReferencePoints(i, Allocation(i))];
    end
    %% Calculate PDI and sort the ND solutions
	inactive_clusters_index = [];
    active_clusters_index = [];
    for i = 1: numel(Cluster)
        if isempty(Cluster(i).ND_index) && isempty(Cluster(i).ND_indicator)
            inactive_clusters_index = [inactive_clusters_index; i];
            Cluster(i).Active = false;
        else
            active_clusters_index = [active_clusters_index; i];
            [Cluster(i).ND_indicator, ascend_index] = sort(Cluster(i).ND_indicator, 'ascend');
            Cluster(i).ND_index = Cluster(i).ND_index(ascend_index);
            Cluster(i).center = PopObj(Cluster(i).ND_index(1), :);
            Cluster(i).active_counter = Cluster(i).active_counter + 1;
        end
    end
    Inactive_Reference_Points = zeros(numel(inactive_clusters_index), size(PopObj, 2));
    Active_Reference_Points = zeros(numel(active_clusters_index), size(PopObj, 2));
    
    for i = 1: numel(inactive_clusters_index)
        Inactive_Reference_Points(i, :) = Inactive_Reference_Points(i, :) + Cluster(inactive_clusters_index(i)).ReferencePoint;
    end
    for i = 1: numel(active_clusters_index)
        Active_Reference_Points(i, :) = Active_Reference_Points(i, :) + Cluster(active_clusters_index(i)).ReferencePoint;
    end
    Active_Cluster = Cluster(active_clusters_index);
    Inactive_Cluster = Cluster(inactive_clusters_index);
    
    fprintf('Active Clusters: %d / %d\n', numel(Active_Cluster), numel(Active_Cluster) + numel(Inactive_Cluster));
    
    %% Assign the dominated objs
	Distance2Centers = zeros(numel(Dominated_index), numel(Active_Cluster));
    Allocation = zeros(1, numel(Dominated_index));
    for i = 1: numel(Dominated_index)
        for j = 1: numel(Active_Cluster)
            Distance2Centers(i, j) = norm(PopObj(Dominated_index(i), :) - Active_Cluster(j).center);
        end
        [min_distance, Allocation(i)] = min(Distance2Centers(i, :));
        Active_Cluster(Allocation(i)).D_index = [Active_Cluster(Allocation(i)).D_index; Dominated_index(i)];
        Active_Cluster(Allocation(i)).D_distance = [Active_Cluster(Allocation(i)).D_distance; min_distance];
    end
    for i = 1: numel(active_clusters_index)
        [Active_Cluster(i).D_distance, ascend_index] = sort(Active_Cluster(i).D_distance, 'ascend');
        Active_Cluster(i).D_index = Active_Cluster(i).D_index(ascend_index);
        if Global.evaluated < Global.evaluation || numel(Non_Dominated_index) < Global.N
            Active_Cluster(i).SelectQueue = [Active_Cluster(i).ND_index; Active_Cluster(i).D_index];
        else
            Active_Cluster(i).SelectQueue = Active_Cluster(i).ND_index;
        end
    end
    j = 1;
    next_pop_index = [];
    Active_Cluster = Active_Cluster(randperm(numel(Active_Cluster)));
    for i = 1: Global.N
        while isempty(Active_Cluster(j).SelectQueue)
            j = j + 1;
            if j > numel(Active_Cluster)
                j = 1;
            end
        end
        next_pop_index = [next_pop_index, Active_Cluster(j).SelectQueue(1)];
        Active_Cluster(j).SelectQueue(1) = [];
        j = j + 1;
        if j > numel(Active_Cluster)
            j = 1;
        end
    end
    Population = Population(next_pop_index);
    Cluster = [Active_Cluster; Inactive_Cluster];
end