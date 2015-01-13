package IntelM2M.algo;

import weka.classifiers.bayes.net.EditableBayesNet;

public interface Classifier {
	public EditableBayesNet[] buildARModelwithAllFeature(String []activityList, Boolean retrain);
}
