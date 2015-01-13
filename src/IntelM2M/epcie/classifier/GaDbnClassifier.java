package IntelM2M.epcie.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import s2h.platform.node.PlatformMessage;
import s2h.platform.node.PlatformTopic;
import s2h.platform.node.Sendable;
import s2h.platform.support.JsonBuilder;
import s2h.platform.support.MessageUtils;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.bayes.net.BIFReader;
import weka.classifiers.bayes.net.EditableBayesNet;
import weka.classifiers.bayes.net.MarginCalculator;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import IntelM2M.algo.Classifier;
import IntelM2M.algo.Prior;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.ExpResult;
import IntelM2M.datastructure.SensorNode;
import IntelM2M.epcie.GaGenerator;
import IntelM2M.esdse.Esdse;

public class GaDbnClassifier implements Classifier {
	public EditableBayesNet[] classifier;
	/* Threshold for Single TraingData */
	// static final double inferThreshold=0.7;
	/* noise confidence */
	static final double inferThreshold = 0.8;
	/* Threshold for Cross Validation */
	// static final double inferThreshold=0.1;
	// static final double WatchingTVThreshold=0.1;
	// static final double ComeBackThreshold=0.1;
	// static final double ChattingThreshold=0.1;

	private ArrayList<String> preInferDBN = new ArrayList<String>();
	/* For Debug */
	public Map<String, String> allGaProb = new LinkedHashMap<String, String>();

	public void buildGaModel(GaGenerator GA, Boolean retrain) {
		Instances insts, attSelected;
		AttributeSelection attSelector;
		String options = "-Q weka.classifiers.bayes.net.search.global.TAN";
		String output_file_path = "./_output_results/";
		try {
			Set<String> gSet = GA.gaList.keySet();
			String[] gaList = (String[]) gSet.toArray(new String[0]);

			classifier = new EditableBayesNet[gaList.length];
			// ETCGenerator etc= new ETCGenerator();
			for (int index = 0; index < gaList.length; index++) {
				if (retrain) {

					insts = new Instances(new FileReader("./_weka_training_data/" + gaList[index] + ".arff"));
					int i = insts.numAttributes();
					insts.setClassIndex(insts.numAttributes() - 1); // the position of class in attribute
					classifier[index] = new EditableBayesNet(insts);

					// attribute selection
					attSelector = new AttributeSelection();
					attSelector.setEvaluator(new CfsSubsetEval());
					attSelector.setSearch(new BestFirst());
					attSelector.SelectAttributes(insts);
					attSelected = attSelector.reduceDimensionality(insts);
					// build
					classifier[index].buildClassifier(attSelected);
					SerializationHelper.write("./_weka_output_data/" + gaList[index] + ".model", classifier);
					classifier[index].setOptions(Utils.splitOptions(options));
					// output
					BufferedWriter writer = new BufferedWriter(new FileWriter("./_weka_output_data/selected_" + gaList[index] + ".arff"));
					writer.write(attSelected.toString());
					writer.flush();
					writer.close();
					writer = new BufferedWriter(new FileWriter("./_weka_output_data/" + gaList[index] + ".xml"));
					writer.write(classifier[index].toXMLBIF03());
					writer.flush();
					writer.close();
				} else { // read models
					/* read type 1 */
					// attSelected = new Instances(new FileReader("./_weka_output_data/selected_" + gaList[index]+".arff"));
					// attSelected.setClassIndex(attSelected.numAttributes() - 1);
					// classifier[index] = new EditableBayesNet(attSelected);
					// classifier[index].buildClassifier(attSelected);
					/* read type 2 */
					BIFReader br = new BIFReader();
					br.processFile("./_weka_output_data/" + gaList[index] + ".xml");
					classifier[index] = new EditableBayesNet(br);

				}
				// etc.buildETCList(output_file_path + "ETC.txt", activityList[index],classifier[index]); //build etc
				System.out.println("train model: " + gaList[index] + ", done!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public EditableBayesNet[] buildARModelwithAllFeature(String[] activityList, Boolean retrain) {
		Instances insts;
		EditableBayesNet[] classifier = null;
		try {
			// String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);
			classifier = new EditableBayesNet[activityList.length];
			// ETCGenerator etc= new ETCGenerator();
			for (int index = 0; index < activityList.length; index++) {
				if (retrain) {
					insts = new Instances(new FileReader("./_weka_training_data/" + activityList[index] + ".arff"));
					insts.setClassIndex(insts.numAttributes() - 1); // the position of class in attribute
					classifier[index] = new EditableBayesNet(insts);
					classifier[index].buildClassifier(insts);
					// output
					BufferedWriter writer = new BufferedWriter(new FileWriter("./_weka_output_data/all_feature_" + activityList[index] + ".xml"));
					writer.write(classifier[index].toXMLBIF03());
					writer.flush();
					writer.close();
				} else {
					/* read type 2 */
					BIFReader br = new BIFReader();
					br.processFile("./_weka_output_data/all_feature_" + activityList[index] + ".xml");
					classifier[index] = new EditableBayesNet(br);
				}

				System.out.println("train All Feature model: " + activityList[index] + ", done!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classifier;
	}

	public void allSetDefaultValue(GaGenerator GA) {
		Set<String> gSet = GA.gaList.keySet();
		String[] gaList = (String[]) gSet.toArray(new String[0]);
		try {
			for (int i = 0; i < classifier.length; i++) {
				for (int j = 0; j < classifier[i].getNrOfNodes() - 1; j++) {
					String sensorName = classifier[i].getNodeName(j);
					String sensorValue = setDefaultValue(i, j, sensorName);
					System.out.println(sensorName + "     " + sensorValue);

				}
			}
			/* Print result */
			double[] result;
			// String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);
			try {
				for (int actIndex = 0; actIndex < gaList.length; actIndex++) {
					String activity = gaList[actIndex];
					result = classifier[actIndex].getMargin(classifier[actIndex].getNode("class"));

					System.out.println(activity + ": " + result[1]);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String setDefaultValue(int index, int iNode, String nodeName) {
		Map<String, String> sensorState = EnvStructure.sensorState;
		String sensorValue = "";
		if (nodeName.contains("TV") || nodeName.contains("kinect") || nodeName.contains("fan")) {
			classifier[index].setEvidence(iNode, 1);
			if (nodeName.contains("accelerometer")) {
				sensorValue = "medium";
				sensorState.put(nodeName, sensorValue);
			} else {
				sensorValue = "standby";
				sensorState.put(nodeName, sensorValue);
			}
		} else if (nodeName.contains("AC")) {
			classifier[index].setEvidence(iNode, 2);
			sensorValue = "on";
			sensorState.put(nodeName, sensorValue);
		} else {
			classifier[index].setEvidence(iNode, 0);
			sensorValue = "off";
			sensorState.put(nodeName, sensorValue);
		}
		update(classifier[index]);

		return sensorValue;
	}
	
	// no longer used
	public void inference(String message, Sendable sender, GaGenerator GA) {

		JsonBuilder json = MessageUtils.jsonBuilder();
		SensorNode s = getSensorAttribute(message);
		Map<String, SensorNode> sensorList = EnvStructure.sensorList;

		// if(s.type.equals("reset")){
		// allSetDefaultValue(false);
		// return;
		// }

		if (sensorList.containsKey(s.type + "_" + s.id)) {

			ArrayList<String> inferDBN = new ArrayList<String>();

			System.out.println("receive: {\"" + s.name + "\":\"" + s.discreteValue + "\"}");
			try {

				/* Inference */
				inferDBN = GaDBNInference(GA, s.name, s.discreteValue);

				if (inferDBN.size() != 0) {
					System.out.print("DBN infer:");
					for (int i = 0; i < inferDBN.size(); i++) {
						System.out.print(" " + inferDBN.get(i));
						//json.reset();
						//sender.send(json.add("subject", "activity").add("value", inferDBN.get(i)).toJson(), PlatformTopic.CONTEXT);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			preInferDBN = (ArrayList<String>) inferDBN.clone();

		}

	}

	public Map<String, ExpResult> testing(GaGenerator GA, String testingDataPath, String resultPath) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(testingDataPath));
			FileWriter writer = new FileWriter(new File(resultPath));
			String read = null;
			Map<String, ArrayList<String>> sensorStatus = EnvStructure.sensorStatus;

			/* build gaList */
			Set<String> gSet = GA.gaList.keySet();
			ArrayList<String> gaList = new ArrayList<String>();
			for (String str : gSet) {
				gaList.add(str);
			}
			/* precision and recall for one activity */
			Map<String, ExpResult> expResult = new LinkedHashMap<String, ExpResult>();
			for (int i = 0; i < gaList.size(); i++) {
				ExpResult r = new ExpResult();
				expResult.put(gaList.get(i), r);
			}

			String preRead = "";
			try {
				while ((read = reader.readLine()) != null) {

					/* 兩種testing 方式 */
					/* 第一種 */
					// System.out.println();
					// String []tmpStr=read.split("#");
					// if(tmpStr[1].equals(preRead)){
					// continue;
					// }
					// preRead=tmpStr[1];
					/* 第二種 */
					// if(read.equals(preRead)){
					// continue;
					// }
					// preRead=read;

					// allSetDefaultValue(false);

					String[] sensorName = (String[]) sensorStatus.keySet().toArray(new String[0]);
					/* initial */
					Map<String, Boolean> GAinferResult = new LinkedHashMap<String, Boolean>();
					Map<String, Boolean> groundTruth = new LinkedHashMap<String, Boolean>();
					Map<String, Boolean> GAgroundTruth = new LinkedHashMap<String, Boolean>();
					ArrayList<String> activityList = EnvStructure.activityList;
					for (int i = 0; i < activityList.size(); i++) {

						groundTruth.put(activityList.get(i), false);
					}
					for (int i = 0; i < gaList.size(); i++) {
						GAinferResult.put(gaList.get(i), false);
						GAgroundTruth.put(gaList.get(i), false);
					}

					String[] split = read.split("#");
					String[] sensorContext = split[0].split(" ");
					ArrayList<String> rawFromDBN = new ArrayList<String>();
					ArrayList<String> inferDBN = new ArrayList<String>();
					Map<String, String> probDBN = new LinkedHashMap<String, String>();
					for (int i = 0; i < sensorContext.length; i++) {
						SensorNode s = new SensorNode(sensorName[i], sensorContext[i]);
						rawFromDBN = GaDBNInference(GA, s.name, s.discreteValue);
					}

					int humanNumber = split[1].split(" ").length;
					if (rawFromDBN.size() != 0) {
						/* prior Knowledge 處理 */
						rawFromDBN = Prior.priorForInference(rawFromDBN, humanNumber);

					}
					for (String str : rawFromDBN) {
						String[] splitActPb = str.split(" ");
						inferDBN.add(splitActPb[0]);
						probDBN.put(splitActPb[0], splitActPb[1]);
					}

					/* record result */
					for (String str : inferDBN) {
						GAinferResult.put(str, true);
					}

					if (inferDBN.size() != 0) {
						writer.write("DBN infer:");
						for (int i = 0; i < inferDBN.size(); i++) {

							writer.write(" " + inferDBN.get(i) + " " + probDBN.get(inferDBN.get(i)));

						}
					}
					/* record ground truth */
					String[] truth = split[1].split(" ");
					/* 去掉NO */
					ArrayList<String> tmpArr = new ArrayList<String>();
					for (String str : truth) {
						if (!str.equals("NO")) {
							tmpArr.add(str);
						}
					}
					truth = (String[]) tmpArr.toArray(new String[0]);

					for (String str : truth) {
						groundTruth.put(str, true);
					}

					/* 選擇要不要印truth */
					writer.write("| truth");
					// writer.write("  truth: "+ split[1]);
					writer.flush();

					/* record ground truth for GA */
					for (String str : truth) {
						// String gid=GA.getGID(str);
						ArrayList<String> gidArr = GA.getGID(str);
						for (String GID : gidArr) {
							GAgroundTruth.put(GID, true);
						}

					}

					writer.write(" | ");
					Set<String> keys = GAgroundTruth.keySet();
					for (String str : keys) {
						if (GAgroundTruth.get(str) == true) {
							String truthProb = allGaProb.get(str);
							writer.write(" " + str + " " + truthProb);
						}
					}

					// if(inferDBN.size()!=0){

					/* record tp tn fp fn for GA */
					for (String str : gaList) {
						if (GAgroundTruth.get(str) == true && GAinferResult.get(str) == true) {
							ExpResult r = expResult.get(str);
							r.tp += 1;
							expResult.put(str, r);
						} else if (GAgroundTruth.get(str) == false && GAinferResult.get(str) == true) {

							ExpResult r = expResult.get(str);
							r.fp += 1;
							expResult.put(str, r);
						} else if (GAgroundTruth.get(str) == true && GAinferResult.get(str) == false) {
							ExpResult r = expResult.get(str);
							r.fn += 1;
							expResult.put(str, r);
						} else if (GAgroundTruth.get(str) == false && GAinferResult.get(str) == false) {
							ExpResult r = expResult.get(str);
							r.tn += 1;
							expResult.put(str, r);
						}
					}

					// }
					writer.write("\r\n");
					writer.flush();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			/* write result */
			writer.write("####################\r\n\r\n");
			for (String str : gaList) {
				double precision = expResult.get(str).tp / (expResult.get(str).tp + expResult.get(str).fp);
				double recall = expResult.get(str).tp / (expResult.get(str).tp + expResult.get(str).fn);

				writer.write(str + ": Precision=" + precision + " Recall=" + recall + "\r\n");
				writer.flush();
			}
			/* write GA member */
			for (String str : gaList) {
				ArrayList<String> actList = GA.getGroupMember(str);
				writer.write(str + " :");
				for (String str2 : actList) {
					writer.write(str2 + "  ");
				}
				writer.write("\r\n");
				writer.flush();
			}

			System.out.println("Testing Finish");

			/* return test result */

			return expResult;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public ArrayList<String> GaDBNInference(GaGenerator GA, String sensorName, String sensorValue) {
		double[] result;
		ArrayList<String> strArray = new ArrayList<String>();

		boolean[] actPreState = EnvStructure.actPreState;
		Set<String> gSet = GA.gaList.keySet();
		String[] gaList = (String[]) gSet.toArray(new String[0]);

		set(sensorName, sensorValue);// update model

		try {
			for (int actIndex = 0; actIndex < gaList.length; actIndex++) {
				String activity = gaList[actIndex].substring(0, gaList[actIndex].length());
				result = classifier[actIndex].getMargin(classifier[actIndex].getNode("class"));

				/* 加上 Prior Knowledge */

				if (result[1] >= inferThreshold) // activity exist
				{

					String prob = Double.toString(result[1]);
					actPreState[actIndex] = true;
					strArray.add(activity + " " + prob);
					// getSender().send(json.add("subject","activity").add("value",activity ).add("prob",prob).toJson(),
					// PlatformTopic.CONTEXT);
				} else if (result[1] < inferThreshold) {
					actPreState[actIndex] = false;
				}
				allGaProb.put(activity, Double.toString(result[1]));
				// if(activity.equals("2")){
				// //System.out.println("Sensor:"+sensorName+" value"+sensorValue+" prob:"+Double.toString(result[1]));
				// //System.out.println("G2~~~~~");
				// for (int i = 0; i <classifier[actIndex].getNrOfNodes()-1 ; i ++)
				// {
				// String node=classifier[actIndex].getNodeName(i);
				// //System.out.println(node);
				// File dir = new File("./_weka_training_data");
				// Instances insts = new Instances(new FileReader("./_weka_training_data/" + dir.list()[0]));
				// int stateNum = insts.attribute(node).numValues();
				//
				// int index = -1;
				// double max_pe = 0.0, pe = 0.0;
				// for(int j = 0; j < stateNum; j ++)
				// {
				// pe = classifier[actIndex].getProbability(i, 1, j);
				// if((j == 0) || (pe > max_pe))
				// {
				// max_pe = pe;
				// index = j;
				// }
				// }
				// //String nodeValue = classifier[actIndex].getNodeValue(i, index);
				// //System.out.println(nodeValue+" "+max_pe);
				// }
				//
				//
				// }
			}
			/* 如果沒有infer任何東西，將低threshold在infer一次 */
			// if(strArray.size()==0){
			// for(int actIndex = 0; actIndex < gaList.length; actIndex ++)
			// {
			// String activity = gaList[actIndex].substring(0, gaList[actIndex].length() );
			// result = classifier[actIndex].getMargin(classifier[actIndex].getNode("class"));
			//
			// /*加上 Prior Knowledge*/
			//
			// if(result[1] >= inferThreshold-0.3) //activity exist
			// {
			//
			// String prob=Double.toString(result[1]);
			// actPreState[actIndex] = true;
			// strArray.add(activity+" "+prob);
			// //getSender().send(json.add("subject","activity").add("value",activity ).add("prob",prob).toJson(), PlatformTopic.CONTEXT);
			// }
			// else if(result[1] < inferThreshold-0.3)
			// {
			// actPreState[actIndex] = false;
			// }
			// allGaProb.put(activity, Double.toString(result[1]));
			// }
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strArray;
	}

	public void allSetDefaultValue(boolean print) {
		try {
			for (int i = 0; i < classifier.length; i++) {
				for (int j = 0; j < classifier[i].getNrOfNodes() - 1; j++) {
					String sensorName = classifier[i].getNodeName(j);
					String sensorValue = setDefaultValue(i, j, sensorName);
					System.out.println(sensorName + "     " + sensorValue);

				}
			}
			/* Print result */
			double[] result;
			String[] activityList = (String[]) EnvStructure.activityList.toArray(new String[0]);
			try {
				for (int actIndex = 0; actIndex < activityList.length; actIndex++) {
					String activity = activityList[actIndex].substring(0, activityList[actIndex].length() - 5);
					result = classifier[actIndex].getMargin(classifier[actIndex].getNode("class"));

					System.out.println(activity + ": " + result[1]);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void set(String sensorName, String sensorValue) {
		try {
			int attIndex = 0, iValue = getValue(sensorName, sensorValue);
			for (int i = 0; i < classifier.length; i++) {
				for (int j = 0; j < classifier[i].getNrOfNodes(); j++) {
					if (sensorName.equals(classifier[i].getNodeName(j))) {
						attIndex = classifier[i].getNode(sensorName);
						classifier[i].setEvidence(attIndex, iValue);

						update(classifier[i]);

						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(EditableBayesNet bayesNet) {
		try {
			MarginCalculator mc = new MarginCalculator();
			mc.calcMargins(bayesNet);

			// SerializedObject so = new SerializedObject(mc);
			// MarginCalculator mcWithEvidence = (MarginCalculator) so.getObject();

			for (int iNode = 0; iNode < bayesNet.getNrOfNodes(); iNode++) {

				if (bayesNet.getEvidence(iNode) >= 0) {

					mc.setEvidence(iNode, bayesNet.getEvidence(iNode));

					// mcWithEvidence.setEvidence(iNode, bayesNet.getEvidence(iNode));
				}

			}
			for (int iNode = 0; iNode < bayesNet.getNrOfNodes(); iNode++) {

				bayesNet.setMargin(iNode, mc.getMargin(iNode));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SensorNode getSensorAttribute(String message) {
		Map<String, SensorNode> sensorList = EnvStructure.sensorList;
		SensorNode s = new SensorNode();
		s.type = extractValue(message, "subject");
		s.id = extractValue(message, "id");
		s.name = sensorList.get(s.type + "_" + s.id).name;
		if (s.type.equals("socketmeter")) {
			s.rawValue = Double.parseDouble(extractValue(message, "ampere"));
		} else {
			s.rawValue = Double.parseDouble(extractValue(message, "value"));
		}
		System.out.println("----------------------------------------");
		System.out.println("receive: {\"" + s.type + "_" + s.id + "\":\"" + s.rawValue + "\"}");

		/**/
		if (sensorList.containsKey(s.type + "_" + s.id)) {
			// double[] thre = sensorList.get(s.name).threshold;
			// String[] status = sensorList.get(s.name).status;
			double[] thre = sensorList.get(s.type + "_" + s.id).threshold;
			String[] status = sensorList.get(s.type + "_" + s.id).status;

			for (int index = 0; index < thre.length; index++) {
				if (s.rawValue < thre[index]) {
					s.discreteValue = status[index];
					break;
				} else if (index + 1 == thre.length) {
					s.discreteValue = status[index + 1];
					break;
				}
			}
		}

		return s;

	}
	
	/* Set up feature according to sensor info from mq */
	public static SensorNode getSensorNode(String message) {
		
		Map<String, SensorNode> sensorList = EnvStructure.sensorList; // <type_id, sensorNode>
		
		// Build a sensorNode according to the message
		SensorNode s = new SensorNode();
		s.type = extractValue(message, "subject");
		s.id = extractValue(message, "id");
		
		// No such sensor type and sensor except for comfort sensor 
		// Since there is no comfort_sensor in xml
		if(!s.type.equals("comfort_sensor")){
			if (!sensorList.containsKey(s.type + "_" + s.id)) {
				return null;
			}
		}
		
		// What to do according to each sensor type
		// Receive context from current sensor 
		if(s.type.equals("current")){
			s.discreteValue = extractValue(message, "value");
			try{
				s.rawValue = Esdse.ezMeterAmpereReading.get(sensorList.get(s.type + "_" + s.id).name);
			}catch (Exception e){
				return null;
			}
			if(s.rawValue == -1){
				return null;
			}
		}
		else if(s.type.equals("socketmeter")){
			s.rawValue = Double.parseDouble(extractValue(message, "ampere"));
		}
		else if(s.type.equals("light")){
			// On off status
			s.discreteValue = extractValue(message, "status");
		}
		else if(s.type.equals("comfort_sensor")){
			String location = sensorList.get("light_" + s.id).name.split("_")[1];  // e.g., light_livingroom
			// Update comfort value according to different location
			Esdse.temperatureReading.put(location, Double.parseDouble(extractValue(message, "temperature")));
			Esdse.humidityReading.put(location, Double.parseDouble(extractValue(message, "humidity")));
			Esdse.illuminationReading.put(location, Double.parseDouble(extractValue(message, "lux")));
			// We deal with light_hallway separately
			if(location.equals("hallway")){
				s.type = "light";
				s.rawValue = Double.parseDouble(extractValue(message, "lux"));
				s.name = sensorList.get(s.type + "_" + s.id).name;
			}
			else{
				return null;
			}
		}
		else if(s.type.equals("switch") || s.type.equals("audio")){
			s.rawValue = Double.parseDouble(extractValue(message, "value"));
		}
		else{
			//System.err.println(message);
			s.rawValue = Double.parseDouble(extractValue(message, "value"));
		}
		
		// Set up sensor name
		// If it is comfort_sensor we only have to set name of hallway light
		if(!s.type.equals("comfort_sensor")){
			s.name = sensorList.get(s.type + "_" + s.id).name;
		}
		
		if (sensorList.containsKey(s.type + "_" + s.id)) {
			double[] thre = sensorList.get(s.type + "_" + s.id).threshold;
			String[] status = sensorList.get(s.type + "_" + s.id).status;
			int[] switchLevel = sensorList.get(s.type + "_" + s.id).switchLevel;
			double[] switchLux = sensorList.get(s.type + "_" + s.id).switchLux;
			
			// If sensor is not current type then we use rawValue to classify status
			// For example, thre = {10, 20}, status = {off, standby, on}
			// rawValue = 15, discreteValue = standby
			if(!s.type.equals("current") && !s.type.equals("light")){
				for(int index = 0; index < thre.length; index++){
					if(s.rawValue < thre[index]){
						s.discreteValue = status[index];
						break;
					}
					else if(index + 1 == thre.length){
						s.discreteValue = status[index + 1];
						break;
					}
				}
			}
			else if(s.name.equals("light_hallway")){
				for(int index = 0; index < thre.length; index++){
					if(s.rawValue < thre[index]){
						s.discreteValue = status[index];
						break;
					}
					else if(index + 1 == thre.length){
						s.discreteValue = status[index + 1];
						break;
					}
				}
			}
			else if(!s.type.equals("light")){
				// Because current sensor only sends on/off, we need check standby by ourselves
				// if status is off and power value > 0 then standby 
				if(s.discreteValue.equals("off") && s.rawValue > thre[0]){
					s.discreteValue = "standby";
				}
			}
			
			// Here we decide on level of each appliance according to ezmeter
			// execpt those we can directly get level of
			int i = 0;
			int bias = 0;
			if(s.type.equals("light") && s.discreteValue.equals("on")){
				// We deal with hallway and other place separately
				if(s.name.equals("light_hallway")){
					// If lamp in livingroom is open, bias will be used
					if(Esdse.sensorReading.get("current_lamp_livingroom").equals("on_1")){
						bias = 60;
					}
					// Find Switch-level according to Switch-lux
					for (i = 0; i < switchLevel.length - 1; i++) {
						if (s.rawValue < (switchLux[i] + bias)) {
							break;
						}
					}
					s.discreteValue = s.discreteValue + "_" + String.valueOf(switchLevel[i]);
				}
				else if(s.name.equals("light_livingroom")){
					String[] level = extractValue(message, "level").split("_");
					s.discreteValue = s.discreteValue + "_" + level[0] + "_" + level[1];
				}
				else{
					s.discreteValue = s.discreteValue + "_" + extractValue(message, "level");
				}
			}
			else if(s.name.equals("current_AC_livingroom") && s.discreteValue.equals("on")){
				s.discreteValue = s.discreteValue + "_" + Esdse.acTemperature_livingroom;
			}
			else if(s.name.equals("current_AC_bedroom") && s.discreteValue.equals("on")){
				s.discreteValue = s.discreteValue + "_" + Esdse.acTemperature_bedroom;
			}
			// Since the current of night lamp is unstable
			// We use ezmeter to decide whether it is on or not
			else if(s.name.equals("current_nightlamp_bedroom")){
				if(s.rawValue > 0.04){
					s.discreteValue = "on_1";
				} 
				else{
					s.discreteValue = "off";
				}
			}
			else if(s.name.contains("lamp") && s.discreteValue.equals("on")) {
				s.discreteValue = s.discreteValue + "_" + "1";
			}
			else if (s.name.contains("watercoldfan") && s.discreteValue.equals("on")) {
				// TODO
				if (s.rawValue <= 0.74) {
					s.discreteValue = s.discreteValue + "_" + "1";
				} else if (s.rawValue >= 0.75 && s.rawValue <= 0.79) {
					s.discreteValue = s.discreteValue + "_" + "2";
				} else if (s.rawValue >= 0.8) {
					s.discreteValue = s.discreteValue + "_" + "3";
				}
			}
			else if (s.name.contains("people") && s.discreteValue.equals("on")) {
					s.discreteValue = s.discreteValue + "_" + (int)s.rawValue;
			}
			// Notebook only has standby mode (No off mode)
			else if(s.name.equals("current_NB_study") && s.discreteValue.equals("off")){
				s.discreteValue = "standby";
			}
		}
		return s;
	}

	private static String extractValue(String message, String key) {
		return IntelM2M.mq.JsonBuilder.getValue(message, key);
	}

	/* DBNclaasifier */
	public int getValue(String sensorName, String sensorValue) {
		int iValue = -1;

		if (sensorName.contains("current")) {
			if (sensorValue.equals("off") || sensorValue.equals("Off"))
				iValue = 0;
			else if (sensorValue.equals("standby") || sensorValue.equals("Standby"))
				iValue = 1;
			else if (sensorValue.equals("on") || sensorValue.equals("On"))
				iValue = 2;
		} else if (sensorName.contains("temperature") || sensorName.contains("humidity")) {
			if (sensorValue.equals("low") || sensorValue.equals("Low")) // higher temperature
				iValue = 0;
			else if (sensorValue.equals("high") || sensorValue.equals("High"))
				iValue = 1;
		} else if (sensorName.equals("accelerometer")) {
			if (sensorValue.equals("low") || sensorValue.equals("Low"))
				iValue = 0;
			else if (sensorValue.equals("medium") || sensorValue.equals("Medium")) // higher temperature
				iValue = 1;
			else if (sensorValue.equals("high") || sensorValue.equals("High"))
				iValue = 2;
		} else if (sensorName.equals("audio_bathroom") || sensorName.equals("audio_kitchen")) {
			if (sensorValue.equals("off") || sensorValue.equals("Off"))
				iValue = 0;
			else if (sensorValue.equals("little") || sensorValue.equals("Little")) // higher temperature
				iValue = 1;
			else if (sensorValue.equals("loud") || sensorValue.equals("Loud"))
				iValue = 2;
		} else if (sensorName.contains("PIR") || sensorName.contains("camera") || sensorName.contains("light") || sensorName.contains("audio")) {
			if (sensorValue.equals("off") || sensorValue.equals("Off"))
				iValue = 0;
			else if (sensorValue.equals("on") || sensorValue.equals("On")) // higher temperature
				iValue = 1;
		} else if (sensorName.contains("switch")) {
			if (sensorValue.equals("off") || sensorValue.equals("Off"))
				iValue = 1;
			else if (sensorValue.equals("on") || sensorValue.equals("On")) // higher temperature
				iValue = 0;
		}

		return iValue;
	}
}
