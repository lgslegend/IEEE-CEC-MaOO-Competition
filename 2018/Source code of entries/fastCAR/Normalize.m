function PopObj = Normalize(PopObj, Global)
    if strcmp(func2str(Global.problem), 'MaF1')
        return;
    elseif strcmp(func2str(Global.problem), 'MaF3')
        return;
    elseif strcmp(func2str(Global.problem), 'MaF4')
        return;
    elseif strcmp(func2str(Global.problem), 'MaF6')
        return;
    elseif strcmp(func2str(Global.problem), 'MaF9')
        return;
    elseif strcmp(func2str(Global.problem), 'MaF13')
        return;
    elseif strcmp(func2str(Global.problem), 'MaF14')
        return;
    elseif strcmp(func2str(Global.problem), 'MaF15')
        return;
    else
        %% Normalization
        [N, ~] = size(PopObj);
        a = Intercepts(PopObj);
        if any(isnan(a))
            a = max(PopObj, [], 1);
        end
        % Normalization
        PopObj = PopObj./repmat(a, N, 1);
    end
end
function a = Intercepts(PopObj)
    [N, M] = size(PopObj);
    %% Find the extreme points
    [~, Choosed(1:M)] = min(PopObj, [], 1);
    L2NormABO = zeros(N, M);
    for i = 1 : M
        L2NormABO(:, i) = sum(PopObj(:, [1: i - 1, i + 1: M]) .^ 2, 2);
    end
    [~, Choosed(M + 1: 2 * M)] = min(L2NormABO, [], 1);
    [~, Extreme] = max(PopObj(Choosed, :), [], 1);
    Extreme = unique(Choosed(Extreme));

    %% Calculate the intercepts
    if length(Extreme) < M
        a = max(PopObj, [], 1);
    else
        Hyperplane = PopObj(Extreme,:) \ ones(M, 1);
        a = 1 ./ Hyperplane';
    end
end