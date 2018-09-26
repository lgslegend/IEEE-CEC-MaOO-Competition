function [trainedClassifier, validationAccuracy] = trainClassifier(Active_Reference_Points, Inactive_Reference_Points, Global)
    vector = [Active_Reference_Points; Inactive_Reference_Points] - 1e-6;
	class = [ones(size(Active_Reference_Points, 1), 1); zeros(size(Inactive_Reference_Points, 1), 1)];      % 3x1апоРа©
	datatable = table(vector, class);
    if Global.M == 3
        [trainedClassifier, validationAccuracy] = trainClassifier_3(datatable);
    elseif Global.M == 5
        [trainedClassifier, validationAccuracy] = trainClassifier_5(datatable);
    elseif Global.M == 10
        [trainedClassifier, validationAccuracy] = trainClassifier_10(datatable);
    elseif Global.M == 15
        [trainedClassifier, validationAccuracy] = trainClassifier_15(datatable);
    end
end

