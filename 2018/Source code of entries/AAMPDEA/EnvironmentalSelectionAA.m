function Archive = EnvironmentalSelectionAA(Global, Population)
% The environmental selection using adaptive granularity grid

     %% Remove the dominated solutions
     N = Global.N;
%      [FrontNo, MaxFNo] = NDSort(Population.objs, N);
%      Population = Population(FrontNo<=MaxFNo);
     
     if strcmpi(func2str(Global.problem), 'MaF8') || strcmpi(func2str(Global.problem), 'MaF9')
         [FrontNo, MaxFNo] = NDSort(Population.objs, N);
         Population = Population(FrontNo<=MaxFNo);
     else
         Population = Population(NDSort(Population.objs,1)==1);
     end
     
     if length(Population) <= N % Applied adaptive granularity grid
         Archive = Population;
     else
         
         Objs = Population.objs;
         
         GridCell = cell(2, N); % Each column of cells corresponds to one grid
         GridCell{1, 1} = 1: size(Objs, 1);
         GridCell{2, 1} = [min(Objs)-0.000001; max(Objs)+0.000001]';
         
         GridCount = 1;
         
         DimCount = 0;
         DimBoundary = size(Objs, 2);
         termCount = 0;
         while GridCount < N
             maxI = GridCount;
             dimI = mod(DimCount, DimBoundary) + 1;
             for i = 1: maxI
                 SoluI = GridCell{1, i};
                 ObjAdimI = Objs(SoluI, dimI);
                 
                 UpperBound = GridCell{2, i}(dimI, 2);
                 LowerBound = GridCell{2, i}(dimI, 1);
                 MeanB = mean(GridCell{2, i}(dimI, :));
                 
                 FirstI = find(ObjAdimI >= LowerBound & ObjAdimI < MeanB);
                 SecondI = find(ObjAdimI >= MeanB & ObjAdimI < UpperBound);
                 
                 if ~isempty(FirstI)
                     GridCell{1, i} = SoluI(FirstI);
                     GridCell{2, i}(dimI, :) = [LowerBound MeanB];
                     if ~isempty(SecondI) % Add a new grid
                         GridCell{1, GridCount + 1} = SoluI(SecondI);
                         GridCell{2, GridCount + 1} = GridCell{2, i};
                         
                         GridCell{2, GridCount + 1}(dimI, :) = [MeanB UpperBound];
                         GridCount = GridCount + 1;
                         if GridCount >= N
                             break;
                         end
                     end
                 else
                     GridCell{1, i} = SoluI(SecondI);
                     GridCell{2, i}(dimI, :) = [MeanB UpperBound];
                 end
                 
             end
             DimCount = DimCount + 1;
             
             termCount = termCount + 1;
             if termCount > 20*DimBoundary
                 break;
             end
             
         end
         
         MapMatrix = zeros(size(Objs, 1), size(GridCell, 2));
         for i = 1: size(GridCell, 2)
             indexGrid = GridCell{1, i};
             if ~isempty(indexGrid)
                 MapMatrix(indexGrid, i) = 1;
             end
         end
         
         % Select evenly from each dimension
         fSel = []; % Indexes of selected solutions
         unCanSel = []; % Indexes of solutions in the same grids with selected solutions
         M = size(Objs, 2); % Objective number
         nEachObj = floor((2/3)*(N/M)); % floor(N/M);
         for i = 1: M
             tempObjS = Objs(:, i);
             maxObj  = max(tempObjS);
             minObj = min(tempObjS);
             interval = (maxObj - minObj)/nEachObj;
             inIndex = ceil((tempObjS - minObj + 10^-5)./interval);
             
             for f = unique(inIndex)'
                 current1 = find(inIndex == f);
                 
                 if f < nEachObj/3
                     candI = setdiff(current1, fSel); % The indexes of candidate solutions                     
                     if ~isempty(candI)
                         [~, I] = min(tempObjS(candI));
                         fSel = [fSel, candI(I)];
                     end
                 else
                     candI = setdiff(current1, unCanSel); % The indexes of candidate solutions
                     
                     if ~isempty(candI)
                         [~, I] = min(tempObjS(candI));
                         fSel = [fSel, candI(I)];
                         
                         IsameGrid = find(MapMatrix(:, MapMatrix(candI(I), :)==1)==1);
                         unCanSel = [unCanSel, IsameGrid'];
                     end
                 end
                 
                 
             end
         end
         
         rSelIndex = setdiff(1: size(Objs, 1), fSel); % The indexs of the un-selected solutions
         
         selObjM = Objs(fSel, :); %Population(fSel).objs;  % The selected objectives
         unSelObjM = Objs(rSelIndex, :); %Population(rSelIndex).objs;
         PopObj = [selObjM; unSelObjM];
         
         %% Normalization
         Zmin   = min(PopObj, [], 1);
         Zmax   = max(PopObj, [], 1);
         PopObj = (PopObj - repmat(Zmin, size(PopObj, 1), 1))./repmat(Zmax-Zmin, size(PopObj, 1), 1);
         
         %% Angle between each two solutions
         angle = acos(1-pdist2(PopObj, PopObj, 'cosine'));
         
         %% Calculate the fitness value of each solution
         fit = sum(PopObj, 2);
         
         % select solutions
         tempChoose = [true(1, length(fSel)), false(1, length(rSelIndex))];
         combineIndex = [fSel, rSelIndex];
         while sum(tempChoose) < N
             % Maximum vector angle first
             Select  = find(tempChoose);
             Remain  = find(~tempChoose);
             
             [~, rho] = max(min(angle(Remain, Select), [], 2));
             tempChoose(Remain(rho)) = true;
             
             % Worse elimination
             if ~all(tempChoose)
                 Select      = [Select, Remain(rho)];
                 Remain(rho) = [];
                 [~, mu]      = min(min(angle(Remain, Select), [], 2));
                 [theta, r]   = min(angle(Remain(mu), Select));
                 if theta < pi/2/(N+1) && fit(Select(r)) > fit(Remain(mu))
                     tempChoose(Select(r))  = false;
                     tempChoose(Remain(mu)) = true;
                 end
             end
             
             
         end
         
         Next = combineIndex(tempChoose); % [fSel, rSelIndex(updateChoose)];
         
         Archive = Population(Next);
     end
end