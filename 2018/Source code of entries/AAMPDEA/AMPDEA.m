function AMPDEA(Global)
% <algorithm> <A-G>
% For ECE 2018 Competition
% nPer --- 50 --- Number of perturbations on each solution for decision variable clustering
    
    %% Detect the group of each distance variable
    nPer = 200;
    nPerGroup = 35; % the group size for separative variables
    
    Bound = zeros(2, Global.D);                
    Bound(1, :) = Global.lower;
    Bound(2, :) = Global.upper;
    
    [PV, DV] = ControlVariableAnalysis(Global, nPer); 
    if ~isempty(DV)
        Groups = GroupDV(Global, DV, PV, nPerGroup); % the groups for variables
        
        % V: random unit vectors in diversity space (H. Chen)
        popuN = 4; %10; % the number of population
        V = rand(popuN, length(PV)); %0.05 + 0.9*rand(popuN, length(PV));
        % extend to the diversity space (H. Chen)
        V = repmat(Global.lower(PV), popuN, 1) + V.*(repmat((Global.upper(PV)-Global.lower(PV)), popuN, 1));
        
        % CMA-ES parameters (H. Chen)
        popSizeCMA = 6 + floor(3*log(nPerGroup)); % the population size for CMA-ES
        tempParaOnePopu = LoadCMAESparameters(Global, Groups, popSizeCMA);
        CMAParaMPopu = repmat(tempParaOnePopu, popuN, 1);
        
        BigPopulation = cell(1, popuN);
        popSize = 20;
        for i = 1: popuN
            tempDecs = zeros(popSize, Global.D);
            tempDecs(:, PV) = repmat(V(i, :), popSize, 1);
            tempDecs(:, DV) = repmat(Global.lower(DV), popSize, 1) + rand(popSize, length(DV)).*repmat((Global.upper(DV) - Global.lower(DV)), popSize, 1);
            BigPopulation{i} = INDIVIDUAL(tempDecs);
            Archive = BigPopulation{i};
        end
    
    else
        % Initialize a population when the DV is empty
        Decs = rand(Global.N, Global.D);
        Decs = repmat(Global.lower, Global.N, 1) + Decs.*(repmat(Global.upper - Global.lower, Global.N, 1));
        Archive = INDIVIDUAL(Decs);
    end
    
    %% Optimization
    Gen = 1;
    UpperThre = 0;
    while Global.NotTermination(Archive)
        
        if Global.evaluated < 0.9*Global.evaluation && ~isempty(DV) % Stage One
            % Evolve each subpopulation
            for p = 1: popuN 
                % Evolve the each group of convergence-related variables by assemble of DE and CMA-ES
                for g = 1:  length(Groups)
                    dim_index = Groups{g};
                    %[BigPopulation{p}, CMAParaMPopu(p, g)] = MixDEandCMAEngine(BigPopulation{p}, dim_index, Bound, CMAParaMPopu(p, g));
                    BigPopulation{p} = DEEngine(BigPopulation{p}, dim_index, Bound);
                end
            end
            
            % Use CMA-ES for local Search
            if mod(Gen, 50) == 0
                selPopI = randperm(popuN, 1);
                for g = 1:  length(Groups)
                    dim_index = Groups{g};
                    num = 1;
                    while true
                        tempBestFit = min(sum(BigPopulation{selPopI}.objs, 2));
                        [BigPopulation{selPopI}, CMAParaMPopu(selPopI, g)] = CMAEngine(BigPopulation{selPopI}, dim_index, CMAParaMPopu(selPopI, g));
                        newBseFit = min(sum(BigPopulation{selPopI}.objs, 2));
                        if (tempBestFit - newBseFit < 0.1) || (num > 3)
                            break;
                        end
                        num = num + 1;
                    end
                    
                end
            end
            
            Archive = [];
            for p = 1: popuN                
                tempPop = BigPopulation{p};
                fitness = sum(tempPop.objs, 2);
                [~, Index] = min(fitness);
                Archive = [Archive, tempPop(Index)];
            end
            
            Gen = Gen + 1;
        else % Stage Two: improve the diversity
            tempDecs = Archive.decs;            
            newPVDecs = PVDE(Global, PV, tempDecs(:, PV));            
            tempDecs(1: size(newPVDecs, 1), PV) = newPVDecs;
            newPopulation = INDIVIDUAL(tempDecs); 
            
            
            if UpperThre == 0
                FitArchive = sum(Archive.objs, 2);
                UpperThre = max(FitArchive);
            end
            FitNewPop = sum(newPopulation.objs, 2);
            
            newPopulation(FitNewPop > 10*UpperThre) = [];
            
            Archive = EnvironmentalSelectionAA(Global, [Archive, newPopulation]);
        end
    end