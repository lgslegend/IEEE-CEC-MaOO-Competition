function [yfit, score] = classifierPredict(trainedClassifier, RefPoint)
    vector = RefPoint - 1e-6;
	reftable = table(vector);
	[yfit, score] = trainedClassifier.predictFcn(reftable);
    score = score(:, 2);
	score = 0.5 + 0.5 * elliotsig(score);
end