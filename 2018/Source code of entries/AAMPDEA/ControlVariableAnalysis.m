function [PV, DV] = ControlVariableAnalysis(Global, NCA)
% Control variable analysis
    
    Fno = zeros(1, Global.D);
    for i = 1 : Global.D
        x      = 0.2*ones(1, Global.D).*(Global.upper-Global.lower) + Global.lower; %rand(1, Global.D).*(Global.upper-Global.lower) + Global.lower;
        
        S      = repmat(x, NCA, 1);
        
        %inter = (0.95-0.05)/(NCA-1);
        %tempA = 0.05: inter: 0.96;
        %S(:, i) = tempA'*(Global.upper(i)-Global.lower(i)) + Global.lower(i);
        S(:, i) = ((1:NCA)'-1+rand(NCA,1))/NCA*(Global.upper(i)-Global.lower(i)) + Global.lower(i);
        
        S      = INDIVIDUAL(S);
        [~, MaxFNo] = NDSort(S.objs, inf);
        
        Fno(i) = MaxFNo;
                
    end
    
    PV = find(Fno == 1); % Indexes of position variables
    DV = find(Fno == NCA); % Indexes of distance variables
    
    MixV = setdiff(1: Global.D, [PV, DV]);
    if ~isempty(MixV)
        [~, I] = sort(MixV);
        PVinMixV = min(length(I), Global.M-1- length(PV));
        
        PV = sort([PV, MixV(I(1: PVinMixV))]);
        DV = sort([DV, MixV(I(PVinMixV+1: end))]);
    end
    
end