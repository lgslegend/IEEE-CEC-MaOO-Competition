function Population = EnvironmentalSelection(Population,Global,pc)
% The environmental selection of GSRA

%--------------------------------------------------------------------------
% The copyright of the PlatEMO belongs to the BIMK Group. You are free to
% use the PlatEMO for research purposes. All publications which use this
% platform or any code in the platform should acknowledge the use of
% "PlatEMO" and reference "Ye Tian, Ran Cheng, Xingyi Zhang, and Yaochu
% Jin, PlatEMO: A MATLAB Platform for Evolutionary Multi-Objective
% Optimization, 2016".
%--------------------------------------------------------------------------

% Copyright (c) 2016-2017 BIMK Group
    K = Global.N;
    N      = length(Population);
    PopObj = Population.objs;
    
    %% Compute indicator values of I1 (epsilon+)
    I = zeros(N);
    for i = 1 : N
        for j = 1 : N
            I(i,j) = max(PopObj(i,:)-PopObj(j,:));
        end
    end
    I1 = sum(-exp(-I./0.05)) + 1;
    if (Global.GSRA_E==0), Global.GSRA_I1=I1;  end
    P1=Global.GSRA_I1./I1;
    Global.GSRA_I1=I1;
    
    %% Compute indicator values of I2 (SDE)
    Distance = inf(N);
    for i = 1 : N
        SPopObj = max(PopObj,repmat(PopObj(i,:),N,1));
        for j = 1 : i-1
            Distance(i,j) = norm(PopObj(i,:)-SPopObj(j,:));
            %Distance(i,j) = sum(PopObj(i,:)-SPopObj(j,:));
        end
    end
    I2 = min(Distance,[],2);

    if (Global.GSRA_E==0)
        Global.GSRA_I2=I2;
        Global.GSRA_E=1;
    end
    P2=Global.GSRA_I2./I2;
    Global.GSRA_I2=I2;

    %% Stochastic ranking based selection
    Rank = 1 : N;
    for sweepCounter = 1 : ceil(N/2)
        swapdone = false;
        for j = 1 : N-1
            e=1;
            if (rand < pc), e=0; end 
            if (P1(Rank(j))>1.5 || P2(Rank(j))>1.5)  
               if (P1(Rank(j))>P2(Rank(j))), e=1; end
            end
            
            if ( e==0 && I1(Rank(j))<I1(Rank(j+1)) )
                Rank     = Rank([1:j-1,j+1,j,j+2:end]);
                swapdone = true;
                end
            if ( e==1 && I2(Rank(j))<I2(Rank(j+1)) )
                Rank     = Rank([1:j-1,j+1,j,j+2:end]);
                swapdone = true;
                end
        end
        if ~swapdone
            break;
        end
    end
    Population = Population(Rank(1:K));
    %save afile.txt -ascii P1;
end