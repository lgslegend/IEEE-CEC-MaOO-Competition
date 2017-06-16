function Offspring = RealMutation( Global, Parent)
% <operator> <real>
% mutation operator
% Author: Sun Haoran
    delta = (1 - Global.evaluated / (Global.evaluation + Global.N * 2)) * 0.7;
    ParentDec = Parent.decs;
    [N,D]     = size(ParentDec);
    
    %% Mutation
    indexM = find(rand(1,D) <= 1.0 / D) ;
    if isempty(indexM)
        indexM = floor(rand * D) + 1;
    end
    for j = 1:length(indexM)
        muValue = ParentDec(indexM(j));
%         arand = rand;
        rnd = 0.5 * (rand-0.5) * (1-rand^(-delta));
        muValue = muValue + rnd * (Global.upper(indexM(j)) - Global.lower(indexM(j)));
        if muValue > Global.upper(indexM(j))
            muValue = Global.upper(indexM(j)) - 0.5 * rand * (Global.upper(indexM(j)) - ParentDec(indexM(j)));
        elseif muValue < Global.lower(indexM(j)) 
            muValue = Global.lower(indexM(j)) + 0.5 * rand * (ParentDec(indexM(j)) - Global.lower(indexM(j)));
        end
        ParentDec(indexM(j)) = muValue ;
    end
    Offspring = INDIVIDUAL(ParentDec);
end

