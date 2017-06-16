function RSEA(Global)
% <algorithm> <A Radial Space Division Based Evolutionary Algorithm for Many-Objective Optimization>

% Copyright 2015-2017 Cheng He

    Population = Global.Initialization();
    Range      = inf(2,Global.M);
    while Global.NotTermination(Population)
        Range(1,:) = min([Range(1,:);Population.objs],[],1);
        Range(2,:) = max(Population(NDSort(Population.objs,1)==1).objs,[],1);
        MatingPool = MatingSelection(Population.objs,Range,ceil(Global.N/2)*2);
        Offspring  = Global.Variation(Population(MatingPool));
        Population = EnvironmentalSelection(Global,[Population,Offspring],Range,Global.N);
    end
end