function RefPoints = Cluster2Point(Cluster, Global)
    RefPoints = zeros(numel(Cluster), Global.M);
    for i = 1: numel(Cluster)
        RefPoints(i, :) = Cluster(i).ReferencePoint;
    end
end
