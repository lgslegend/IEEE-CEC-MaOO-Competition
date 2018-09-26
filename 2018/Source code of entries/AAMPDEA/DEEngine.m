function Population = DEEngine(Pop, dim_index, Bound)
    %
    CR = 0.5;
    F = 0.5;
    proM = 1;
    disM = 20;
    Decs = Pop.decs;
    Fitness = sum(Pop.objs, 2); % Fitness of the original population
    Parent = Decs(:, dim_index);
    [N, D]     = size(Parent);
    
    Lower = repmat(Bound(1, dim_index), N, 1); % Lower boundary
    Upper = repmat(Bound(2, dim_index), N, 1); % Upper boundary
    
    %% Differental evolution
    Parent1Dec   = Parent(randperm(N), :);
    Parent2Dec   = Parent(randperm(N), :);
    Parent3Dec   = Parent(randperm(N), :);
    OffspringDec = Parent1Dec;
    Site = rand(N, D) < CR;
    OffspringDec(Site) = OffspringDec(Site) + F*(Parent2Dec(Site)-Parent3Dec(Site));
    
    %% Polynomial mutation
    Site  = rand(N, D) < proM/D;
    mu    = rand(N, D);
    temp  = Site & mu<=0.5;
    OffspringDec(temp) = OffspringDec(temp)+(Upper(temp)-Lower(temp)).*((2.*mu(temp)+(1-2.*mu(temp)).*...
        (1-(OffspringDec(temp)-Lower(temp))./(Upper(temp)-Lower(temp))).^(disM+1)).^(1/(disM+1))-1);
    temp = Site & mu>0.5;
    OffspringDec(temp) = OffspringDec(temp)+(Upper(temp)-Lower(temp)).*(1-(2.*(1-mu(temp))+2.*(mu(temp)-0.5).*...
        (1-(Upper(temp)-OffspringDec(temp))./(Upper(temp)-Lower(temp))).^(disM+1)).^(1/(disM+1)));
    
    %% Obtain new population
    newDecs = Pop.decs;
    newDecs(:, dim_index) = OffspringDec;
    newPop = INDIVIDUAL(newDecs);
    newFitness = sum(newPop.objs, 2); % Fitness of new population
    
    %% Update population
    Population = [Pop(Fitness < newFitness), newPop(Fitness >= newFitness)];
    
end 