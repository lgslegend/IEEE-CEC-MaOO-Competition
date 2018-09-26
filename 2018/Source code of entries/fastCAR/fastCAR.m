function fastCAR(Global)
% <algorithm> <A-G>
% A Many Objective Evolutionary Algorithm with Fast Clustering and Reference Point Redistribution
% Mingde Zhao, Hongwei Ge, Hongyan Han, Liang Sun
global training_flag;
rng('shuffle');
%% Initialization
Global.N = 240;
%% Generate random population
Population = Global.Initialization();
refnum = Global.N;
[RefPoint, Global.N] = UniformPoint(refnum, Global.M);
Cluster = Point2Cluster(RefPoint - 1e-6);
training_flag = 0;
Active_Reference_Points = [];
Inactive_Reference_Points = [];
Sample_Point = 0.5;
direction = '';
%% Optimization
while Global.NotTermination(Population)
    MatingPool = TournamentSelection(2,Global.N,sum(max(0,Population.cons),2));
    Offspring = Global.Variation(Population(MatingPool));
    [Population, new_Active_Reference_Points, new_Inactive_Reference_Points, ~] = fastClustering([Population, Offspring], Global, RefPoint, Cluster);
    if training_flag == 0
        if Global.evaluated >= Sample_Point * Global.evaluation
            training_flag = 1;
            if isempty(Active_Reference_Points)
                Sample_Point = Sample_Point + 0.1;
                training_flag = 0;
                continue;
            end
            [trainedClassifier, ~] = trainClassifier(Active_Reference_Points, Inactive_Reference_Points, Global);
            step = 1;
            appropriate_reference_points = 0;
            while appropriate_reference_points < Global.N
                total_points = nchoosek(step + Global.M - 1, Global.M - 1);
                [RefPoint, ~] = UniformPoint(total_points, Global.M);
                [yfit, score] = classifierPredict(trainedClassifier, RefPoint);
                appropriate_reference_points = sum(yfit);
                fprintf('Total: %d, Appropriate: %d\n', size(RefPoint, 1), appropriate_reference_points);
                step = step + 1;
                if size(RefPoint, 1) > 500000
                    break;
                end
            end
            if appropriate_reference_points < Global.N
                Cluster = Point2Cluster(RefPoint);
                Cluster = reduceClusters(Cluster, trainedClassifier, Global, []);
            else
                RefPoint = RefPoint(score > 0.45, :);
                Cluster = Point2Cluster(RefPoint);
                Cluster = reduceClusters(Cluster, trainedClassifier, Global, []);
            end
            RefPoint = Cluster2Point(Cluster, Global);
            RefPoint = unique([RefPoint; Active_Reference_Points], 'rows');
            Cluster = Point2Cluster(RefPoint);
            direction = 'clear';
            continue;
        end
        status = AdaptiveStatus(RefPoint, new_Active_Reference_Points, Global, direction);
        direction = '';
        fprintf('%s\n', status);
        if strcmp(status, 'stable')
            if size(new_Active_Reference_Points, 1) >= 0.85 * Global.N
                training_flag = 0;
                Sample_Point = 0;
            end
            Active_Reference_Points = unique([Active_Reference_Points; new_Active_Reference_Points], 'rows');
            shuffle_index = randperm(size(new_Inactive_Reference_Points, 1));
            new_Inactive_Reference_Points = new_Inactive_Reference_Points(shuffle_index, :);
            new_Inactive_Reference_Points = new_Inactive_Reference_Points(1: min(1000, ceil(0.5 * size(new_Inactive_Reference_Points, 1))), :);
            Inactive_Reference_Points = unique([Inactive_Reference_Points; new_Inactive_Reference_Points], 'rows');
            if size(new_Active_Reference_Points, 1) < Global.N
                step = 1;
                total_points = refnum;
                while total_points <= refnum && total_points <= 500000
                    step = step + 1;
                    total_points = nchoosek(step + Global.M - 1, Global.M - 1);
                end
                [RefPoint, refnum] = UniformPoint(total_points, Global.M);
                if size(RefPoint, 1) > 50000
                    Cluster = Point2Cluster(RefPoint);
                    [trainedClassifier, ~] = trainClassifier(Active_Reference_Points, Inactive_Reference_Points, Global);
                    Cluster = reduceClusters(Cluster, trainedClassifier, Global, []);
                    RefPoint = Cluster2Point(Cluster, Global);
                end
                RefPoint = unique([RefPoint; Active_Reference_Points], 'rows');
                Cluster = Point2Cluster(RefPoint);
                direction = 'clear';
            end
        elseif strcmp(status, 'ideal')
            training_flag = 1;
        end
    end
end
end