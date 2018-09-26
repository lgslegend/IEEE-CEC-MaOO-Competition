function [Pop, para] = CMAEngine(Pop, dim_index, para)

    % Select the best individual for the CMA
    Fitness = sum(Pop.objs, 2);
    [~, Index] = min(Fitness);
    bestmem = Pop(Index).dec;
    
    %% CMA-ES
    % --------------------  Initialization --------------------------------
    % User defined input parameters (need to be edited)
    %strfitnessfct = para.strfitnessfct; %'fsphere'; % 'Rastrigin'; % 'Griewank'; %'Ackley'; %'Schwefel'; %'fsphere'; % 'frosenbrock'; %'fsphere';   % name of objective/fitness function
    N = para.N;  % number of objective variables/problem dimension
    xmean = para.xmean; % 10*rand(N, 1);    % ?? objective variables initial point
    sigma = para.sigma; %0.1;          % ?? coordinate wise standard deviation (step size)
    %stopeval = 5000*N; %1e3*N^2; stop after stopeval number of function evaluations
    
    % Strategy parameter setting: Selection
    lambda = para.lambda; % 4 + floor(3*log(N));  % population size, offspring number
    %mu = lambda/2;               % number of parents/points for recombination
    %weights = log(mu+1/2) - log(1:mu)'; % muXone array for weighted recombination
    mu = para.mu; % floor(mu);
    weights = para.weights; % weights/sum(weights);     % normalize recombination weights array
    mueff = para.mueff; % sum(weights)^2/sum(weights.^2); % variance-effectiveness of sum w_i x_i
    
    % Strategy parameter setting: Adaptation
    cc = para.cc; % (4 + mueff/N) / (N+4 + 2*mueff/N); % time constant for cumulation for C
    cs = para.cs; % (mueff+2) / (N+mueff+5);  % t-const for cumulation for sigma control
    c1 = para.c1; % 2 / ((N+1.3)^2+mueff);    % learning rate for rank-one update of C
    cmu = para.cmu; % min(1-c1, 2 * (mueff-2+1/mueff) / ((N+2)^2+mueff));  % and for rank-mu update
    damps = para.damps; % 1 + 2*max(0, sqrt((mueff-1)/(N+1))-1) + cs; % damping for sigma
    chiN = para.chiN; % N^0.5*(1-1/(4*N)+1/(21*N^2));  % expectation of ||N(0,I)|| == norm(randn(N,1))
    
    
    % Initialize dynamic (internal) strategy parameters and constants
    pc = para.pc;  % zeros(N, 1);
    ps = para.ps; % zeros(N, 1);   % evolution paths for C and sigma
    B = para.B; % eye(N, N);                       % B defines the coordinate system
    D = para.D; % ones(N, 1);                      % diagonal D defines the scaling
    C = para.C; % B * diag(D.^2) * B';            % covariance matrix C
    invsqrtC = B * diag(D.^-1) * B';    % C^-1/2
    eigeneval = para.eigeneval; % 0;                      % track update of B and D
    counteval = para.counteval;
    
    GenCMA = 1;
    Tag = true;
    while GenCMA < 50
        % Generate new population
        PopCMA = repmat(xmean, 1, lambda) + sigma * B * (repmat(D, 1, lambda) .* randn(N, lambda));
        
        tempDecs = repmat(bestmem, lambda, 1);
        tempDecs(:, dim_index) = PopCMA';
        population = INDIVIDUAL(tempDecs);
        counteval = counteval + lambda;
        
        FitCMA = sum(population.objs, 2); % Fitness of each individual
        
        % Record the good solutions
        if Tag % isempty(PopLS)
            PopLS = PopCMA;
            FitLS = FitCMA;
            Tag = false;
        else
            % Update population
            UpdateIndex = FitLS > FitCMA;
            PopLS(:, UpdateIndex) =  PopCMA(:, UpdateIndex);
            FitLS(UpdateIndex) =  FitCMA(UpdateIndex);
        end
        
        % Sort by fitness and compute weighted mean into xmean
        [~, arindex] = sort(FitCMA);  % minimization
        xold = xmean;
        xmean = PopCMA(:, arindex(1:mu)) * weights;  % recombination, new mean value
        
        % Cumulation: Update evolution paths
        ps = (1-cs) * ps ...
            + sqrt(cs*(2-cs)*mueff) * invsqrtC * (xmean-xold) / sigma; %对应公式（24）
        hsig = sum(ps.^2)/(1-(1-cs)^(2*counteval/lambda))/N < 2 + 4/(N+1);
        pc = (1-cc) * pc ...
            + hsig * sqrt(cc*(2-cc)*mueff) * (xmean-xold) / sigma;
        
        % Adapt covariance matrix C
        artmp = (1/sigma) * (PopCMA(:,arindex(1:mu)) - repmat(xold,1,mu));  % mu difference vectors
        C = (1-c1-cmu) * C ...                   % regard old matrix
            + c1 * (pc * pc' ...                % plus rank one update
            + (1-hsig) * cc*(2-cc) * C) ... % minor correction if hsig==0
            + cmu * artmp * diag(weights) * artmp'; % 公式（30） plus rank mu update
        
        % Adapt step size sigma
        sigma = sigma * exp((cs/damps)*(norm(ps)/chiN - 1));
        
        % Update B and D from C
        if counteval - eigeneval > lambda/(c1+cmu)/N/10  % to achieve O(N^2)
            eigeneval = counteval;
            C = triu(C) + triu(C,1)'; % enforce symmetry
            if any(any(isnan(C))) || any(any(C==Inf))
                C = B * diag(D.^2) * B';
                C = triu(C) + triu(C,1)'; % enforce symmetry
                %C = para.C;
            end
            
            [B, D] = eig(C);           % eigen decomposition, B==normalized eigenvectors
            %D = sqrt(diag(D));        % D contains standard deviations now
            D = sqrt(max(diag(D), 0));
            
            invsqrtC = B * diag(D.^-1) * B';            
        end
        GenCMA = GenCMA + 1;
    end
    
    % Update the population
    % Determine the maximum replace number
    UpdateNum = min(floor(length(Pop)/3), floor(size(PopLS, 2)/2));
    [~, I] = sort(FitLS);
    
    % Select the candidate solutions from results by local search
    CandSolutions = PopLS(:, I(1: UpdateNum))';
    
    CanPop = Pop(1:UpdateNum);
    RemainPop = Pop(UpdateNum+1: end);
    newDecs = CanPop.decs;
    newDecs(:, dim_index) = CandSolutions; % Replace 
    newPop = INDIVIDUAL(newDecs); % New population
    
    Fitness = sum(CanPop.objs, 2);
    newFitness = sum(newPop.objs, 2);
    
    Pop = [RemainPop, CanPop(Fitness < newFitness), newPop(Fitness >= newFitness)];    

    % Update the parameters
    para.xmean = xmean;  % ?? objective variables initial point
    para.sigma = sigma;  % ?? coordinate wise standard deviation (step size)
    para.pc = pc;  % evolution paths for C and sigma
    para.ps = ps;   % evolution paths for C and sigma
    para.B = B;
    para.D = D;
    para.C = C; % B * diag(D.^2) * B';            % covariance matrix C
    para.eigeneval = eigeneval;
    para.counteval = counteval;
end 