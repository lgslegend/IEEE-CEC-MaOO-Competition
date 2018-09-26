function Cluster = reduceClusters(Cluster, trainedClassifier, Global, threshold)
    old_cluster = Cluster;
    original_size = numel(Cluster);
    refpoints = cat(1, Cluster.ReferencePoint);
    [~, score] = classifierPredict(trainedClassifier, refpoints);
    if isempty(threshold)
        S = sort(score, 'descend');
        threshold = S(Global.N);
        reduce_index = find(score < threshold);
        Cluster(reduce_index) = [];
        fprintf('adaptively reduced %d clusters (%d to %d)\n', numel(reduce_index), original_size, numel(Cluster));
        return;
    end
    reduce_index = find(score < threshold);
    if numel(reduce_index) > 0
        Cluster(reduce_index) = [];
        if numel(Cluster) == 0
             S = sort(score, 'ascend');
             threshold = S(ceil(0.7 * numel(S)));
             reduce_index = find(score < 1.2 * threshold);
             Cluster = old_cluster;
             Cluster(reduce_index) = [];
        elseif numel(Cluster) > 100000
            refpoints = cat(1, Cluster.ReferencePoint);
            [~, score] = classifierPredict(trainedClassifier, refpoints);
            S = sort(score, 'ascend');
            threshold = S(ceil(0.7 * numel(S)));
            reduce_index = find(score < threshold);
            Cluster = old_cluster;
            Cluster(reduce_index) = [];
        end
        fprintf('reduced %d clusters (%d to %d)\n', numel(reduce_index), original_size, numel(Cluster));
    end
end

