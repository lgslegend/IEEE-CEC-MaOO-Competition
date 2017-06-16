function Population = AngleBasedSelect( NewPopulation, CornerPopulation, N ,idealPoint,nadPoint)
% ANGLEBASEDSELECT angle based selection
% Author: Sun Haoran
    Population = CornerPopulation;
    
    theta = getAngle(NewPopulation.objs, CornerPopulation.objs,idealPoint,nadPoint);
    while length(Population) < N
        [~,index] = min(theta);
        Ind = NewPopulation(index);
        Population = [Population, NewPopulation(index)];
        NewPopulation(index) = [];
        theta(index) = [];
        newTheta = getAngle(NewPopulation.objs,Ind.obj,idealPoint,nadPoint);
        theta = max(theta,newTheta);
    end
end



function [thetas]=getAngle(inds,weights,idealPoint,nadPoint)
    % inds : candidate solution
    % weight :  elitist solution
    lenInds=size(inds,1);
    normInds = (inds-repmat(idealPoint,lenInds,1))./repmat(nadPoint-idealPoint,lenInds,1);
    
    nWeight = size(weights,1);
    normWeight = (weights-repmat(idealPoint,nWeight,1))./repmat(nadPoint-idealPoint,nWeight,1);
    
    thetas = zeros(1,lenInds);
    uniWeights = sum(abs(normWeight).^2,2).^(1/2);
    for i=1:lenInds
        normInd = normInds(i,:);
        uniInd = norm(normInd);
        cosTheta = normWeight * normInd'./ (uniWeights * uniInd) ;
        thetas(i) = max(cosTheta);
    end
end