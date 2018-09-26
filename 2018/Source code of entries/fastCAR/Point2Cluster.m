function Cluster = Point2Cluster(RefPoint)
    Cluster = [];
    Cluster.ReferencePoint = [];
    Cluster.active_counter = 0;
    Cluster.ND_index = []; %index of the non-dominated objs
    Cluster.ND_indicator = []; %PBI of the non-dominated objs
    Cluster.D_index = []; %index of the dominated objs
    Cluster.D_distance = []; %index of the dominated objs
    Cluster.center = [];
    Cluster.SelectQueue = [];
    Cluster = repmat(Cluster, size(RefPoint, 1), 1);
    for i = 1: size(RefPoint, 1)
        Cluster(i).ReferencePoint = RefPoint(i, :);
    end
end

