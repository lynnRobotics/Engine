package IntelM2M.agent.visual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import s2h.platform.node.PlatformTopic;
import s2h.platform.node.Sendable;
import s2h.platform.support.JsonBuilder;
import s2h.platform.support.MessageUtils;

import IntelM2M.agent.thermal.PMVCalculate;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.epcie.GAinference;
import IntelM2M.esdse.Esdse;
import IntelM2M.esdse.Optimizer;
import IntelM2M.exp.ExpRecorder;
import IntelM2M.mq.Producer;

public class VisualAgent {

	double initConstraint = 1.0;
	double secConstraint = 2.0;
	final int iterateLimit = 20;
	final double incrementConstraint = 0.1;
	ArrayList<Double> constraintList = null;

	// final double initConstraint=2.0;
	// final double secConstraint=2.5;
	private Map<String, Double> maxLuxTable = new HashMap<String, Double>();
	private Map<String, Double> marginIdealLux = new HashMap<String, Double>();
	private Map<String, Double> illEvaluationResult = new HashMap<String, Double>(); // Map<location, ill>
	private Map<String, String> locationActivity = new HashMap<String, String>();
	private Map<String, Double> optimalIll = new HashMap<String, Double>();
	
	public VisualAgent(){
		// Initialize the max lux level of each light 
		maxLuxTable.put("livingroom", 200.0);
		maxLuxTable.put("livingroom_central", 160.0);
		maxLuxTable.put("livingroom_ring", 40.0);
		maxLuxTable.put("kitchen", 85.0);
		maxLuxTable.put("study", 135.0);
		maxLuxTable.put("bedroom", 165.0);
	}
	
	/* Get <activity, appList> */
	public Map<String, ArrayList<AppNode>> getActAppList(ArrayList<AppNode> decisionList, GAinference gaInference) {

		// Get single activity from GA
		Set<String> singleAct = gaInference.actInferResultSet;

		// Get location list
		ArrayList<String> locationList = getActLocationList(singleAct);
		// insert appliance into actAppList based on locationList
		// Map<String, ArrayList<AppNode>> actAppList=getActAppList2(singleAct,locationList,decisionList);

		Map<String, ArrayList<AppNode>> actAppList = new LinkedHashMap<String, ArrayList<AppNode>>();
		// Insert appliance into appList based on locationList
		int i = 0;
		for(String act : singleAct){
			ArrayList<AppNode> appList = new ArrayList<AppNode>();
			for(AppNode app : decisionList){
				if(locationList.get(i).equals(app.location)){
					appList.add(app);
				}
			}
			actAppList.put(act, appList);
			i += 1;
		}

		// /*remove appliance not "on" status with act*/
		// actAppList=removeNotOnApp(actAppList,gaInference);

		return actAppList;
	}

	/* Add location of inferred activities into locationList */
	public ArrayList<String> getActLocationList(Set<String> singleAct) {
		ArrayList<String> locationList = new ArrayList<String>();

		Set<String> location_actSet = EnvStructure.actAppList.keySet();
		for(String act : singleAct){
			for(String location_act : location_actSet){
				String activity = location_act.split("_")[1];
				if(activity.equals(act)){
					String location = location_act.split("_")[0];
					locationList.add(location);
				}
			}
		}
		if(locationList.size() != singleAct.size()){
			return null;
		} 
		else{
			return locationList;
		}
	}

	/* Get single location according to single activity */
	public String getActLocation(String act) {
		Set<String> location_actSet = EnvStructure.actAppList.keySet();
		for(String location_act : location_actSet){
			if(location_act.contains(act)){
				String location = location_act.split("_")[0];
				return location;
			}
		}
		return null;
	}

	// public Map<String, ArrayList<AppNode>> getActAppList2( Set<String> singleAct, ArrayList<String> locationList,ArrayList<AppNode>decisionList ){
	// Map<String, ArrayList<AppNode>> actAppList = new LinkedHashMap<String, ArrayList<AppNode>>();
	// int i=0;
	// for(String act:singleAct){
	// ArrayList<AppNode> appList= new ArrayList<AppNode>();
	// for(AppNode app:decisionList){
	// if(locationList.get(i).equals(app.location)){
	// appList.add(app.copyAppNode(app));
	// }
	// }
	// actAppList.put(act, appList);
	// i+=1;
	// }
	// return actAppList;
	// }

	// public Map<String, ArrayList<AppNode>> removeNotOnApp(Map<String, ArrayList<AppNode>> actAppList,GAinference gaInference){
	//
	// Map<String, RelationTable> ga0ActAppList=gaInference.GaEscList.get(0).actAppList;
	// Map<String, RelationTable> ga0ActAppList2= new HashMap <String, RelationTable>();
	// GaGenerator ga0Generator=gaInference.GaGeneratorList.get(0);
	// Set<String> ga0ActAppListKey=ga0ActAppList.keySet();
	// for(String gaName:ga0ActAppListKey){
	// String actName=ga0Generator.getGroupMember(gaName).get(0);
	// RelationTable tmp=ga0ActAppList.get(actName);
	// ga0ActAppList2.put(actName, tmp);
	//
	// }
	//
	// Set<String> actAppListKey=actAppList.keySet();
	// Set<String> ga0Key=ga0ActAppList2.keySet();
	//
	// for(String actName:actAppListKey){
	//
	// ArrayList<AppNode> appList=actAppList.get(actName);
	// ArrayList<AppNode> eusList=ga0ActAppList2.get(actName).appList;
	//
	// ArrayList<AppNode> newAppList= new ArrayList<AppNode>();
	//
	// for(AppNode app:appList){
	// for(AppNode eus:eusList){
	// if(eus.appName.equals(app.appName)){
	// if(!eus.state.equals("off")){
	// newAppList.add(app);
	// }
	// }
	// }
	// }
	//
	// actAppList.put(actName, newAppList);
	//
	//
	// }
	//
	// return actAppList;
	//
	// }

	/* Calculate ILL-List according to context from status of appliance */
	public ArrayList<Double> getIllList(Map<String, ArrayList<AppNode>> actAppList) {
		ArrayList<Double> illList = new ArrayList<Double>();
		Set<String> keySet = actAppList.keySet();
		for (String str : keySet) {
			String location = getActLocation(str);
			VisualComfortTable vct = EnvStructure.visualComfortTableList.get(location);
			ArrayList<AppNode> appList = actAppList.get(str);
			// 燈光的搭配組合取得lux
			double lux = vct.getLuxFromTable(appList);
			// get real lux level
			double realLuxLevel = LuxLevel.transformLuxLevel(lux);
			// get ideal lux level
			double idealLuxLevel = IdealLuxLevel.getIdealLuxLevel(str);
			//double ILL = (realLuxLevel - idealLuxLevel) / 3;
			double ILL = Math.log(1 + Math.abs(realLuxLevel - idealLuxLevel)) / Math.log(2);
			if ((realLuxLevel - idealLuxLevel) < 0) ILL *= -1;
			illList.add(ILL);
		}
		return illList;
	}
	
	/* Calculate ILL-List according to context from environment */
	public ArrayList<Double> getIllListFromEnvironment(Map<String, ArrayList<AppNode>> actAppList) {
		ArrayList<Double> illList = new ArrayList<Double>();
		Set<String> keySet = actAppList.keySet();
		for(String act : keySet){
			String location = getActLocation(act);
			//VisualComfortTable vct = EnvStructure.visualComfortTableList.get(location);
			//ArrayList<AppNode> appList = actAppList.get(act);
			// Get lux from sensor reading of that location
			double lux = Esdse.illuminationReading.get(location);
			// get real lux level
			double realLuxLevel = LuxLevel.transformLuxLevel(lux);
			// get ideal lux level
			double idealLuxLevel = IdealLuxLevel.getIdealLuxLevel(act);
			//double ILL = (realLuxLevel - idealLuxLevel) / 3;
			double ILL = Math.log(1 + Math.abs(realLuxLevel - idealLuxLevel)) / Math.log(2);
			if ((realLuxLevel - idealLuxLevel) < 0) ILL *= -1;
			illList.add(ILL);
		}
		return illList;
	}

	/* Get final pmvList according to context from appList */
	public ArrayList<Double> getComfortArray(ArrayList<AppNode> decisionList, GAinference gaInference) {
		// getVisualApp
		// ArrayList<AppNode> visualAppList=getVisualApp(decisionList);
		// Act-app List by location
		Map<String, ArrayList<AppNode>> actAppList = getActAppList(decisionList, gaInference);
		// Cal ILL for each act-app
		ArrayList<Double> illList = getIllList(actAppList);
		
		return illList;
	}
	
	/* Get final pmvList according to context from environment */
	public ArrayList<Double> getComfortArrayFromEnvironment(ArrayList<AppNode> decisionList, GAinference gaInference) {

		// /*getVisualApp*/
		// ArrayList<AppNode> visualAppList=getVisualApp(decisionList);
		
		// Act-app List by location
		Map<String, ArrayList<AppNode>> actAppList = getActAppList(decisionList, gaInference);

		// cal ILL for each act-app
		ArrayList<Double> illList = getIllListFromEnvironment(actAppList);
		
		return illList;

	}

	/* optimization */
	public ArrayList<AppNode> getOptVisualList(ArrayList<AppNode> eusList, GAinference gaInference, Producer producer) {
		ArrayList<AppNode> optVisualList = new ArrayList<AppNode>();
		// get visual List
		ArrayList<AppNode> visualAppList = new ArrayList<AppNode>();
		ArrayList<AppNode> visualRawList = new ArrayList<AppNode>();
		for (AppNode app : eusList) {
			if (app.agentName.equals("visual")) {
				AppNode app2 = app.copyAppNode(app);
				visualAppList.add(app2);
				AppNode app3 = app.copyAppNode(app);
				visualRawList.add(app3);
			}
		}
		
		// Visual App list有可能是空的
		if(visualAppList.size() == 0){
			if (producer == null){
				System.err.println("sender is null");
			} 
			else{
				sendILLToMQ(visualRawList, gaInference, producer);
			}
			return new ArrayList<AppNode>();
		} 
		else{
			if(producer == null){
				System.err.println("sender is null");
			} 
			else{
				sendILLToMQ(visualRawList, gaInference, producer);
			}
			
			// 1. Get lux from light sensor
			// 2. Calculate lux provided by light
			// 3. lux from nature = 1 - 2
			// 4. Calculate neededLux = idealLux - 3
			// 5. If neededLux > 0, adjust light, else turn off the light
			for(String location : marginIdealLux.keySet()){
				AppNode appNode = null;
				// Find correspond light appliance node
				for(AppNode app : visualAppList){
					System.err.println("visualAppName: " + app.appName);
					if(app.appName.contains(location)) appNode = app;
				}
				// ILL donesn't fulfills the constraint
				if(marginIdealLux.get(location) != -1){	
					// Get lux from light sensor
					double lightSensorReading = Esdse.illuminationReading.get(location);
					double lightStatusLux = 0;
					// Calculate lux provided by light
					if(appNode.envContext.contains("on")){
						if(location.equals("livingroom")){
							double lightLevel_central = Double.valueOf(appNode.envContext.split("_")[1]);
							double lightLevel_ring = Double.valueOf(appNode.envContext.split("_")[2]);
							lightStatusLux = maxLuxTable.get(location + "_central") * (lightLevel_central / 99)
											+ maxLuxTable.get(location + "_ring") * (lightLevel_ring / 99);
						}
						else{
							double lightLevel = Double.valueOf(appNode.envContext.split("_")[1]);
							lightStatusLux = maxLuxTable.get(location) * (lightLevel / 99);
						}
					}
					else{
						lightStatusLux = 0;
					}
					// Calculate lux from nature
					double luxWithoutLight = lightSensorReading - lightStatusLux;
					// Calculate neededLux
					double neededLux = marginIdealLux.get(location) - luxWithoutLight;
					if(neededLux > 0){
						int lightLevel = 0;
						if(location.equals("livingroom")){
							lightLevel = (int)Math.round((neededLux / maxLuxTable.get(location)) * 199);
							if(lightLevel >= 199) lightLevel = 198;
						}
						else{
							lightLevel = (int)Math.round((neededLux / maxLuxTable.get(location)) * 99);
							if(lightLevel >= 100) lightLevel = 99;
						}
						appNode.envContext = "on_" + String.valueOf(lightLevel);
						optVisualList.add(appNode.copyAppNode(appNode));
					}
					else{
						appNode.envContext = "off";
						optVisualList.add(appNode.copyAppNode(appNode));
					}
				}
				else{
					optVisualList.add(appNode.copyAppNode(appNode));
				}
			}
			marginIdealLux.clear();
			return optVisualList;
			
			
			
			// 從eusList中和visual有關的電氣，找出所有的狀態排列組合
			/*Optimizer op = new Optimizer();
			ArrayList<String> candidateList = op.buildCandidateList(visualAppList);

			int iterateCounter = 0;
			ArrayList<AppNode> bestAnswer = null;
			while((bestAnswer == null || bestAnswer.size() == 0) && iterateCounter < iterateLimit){
				// For debug
				if(bestAnswer != null && bestAnswer.size() == 0){
					int aa = 0;
					aa++;
				}
				bestAnswer = visualIterate(candidateList, visualAppList, visualRawList, gaInference, iterateCounter);
				iterateCounter++;
				//System.out.println("iterateCounter = " + iterateCounter);
			}
			
			if(bestAnswer.size() == 0){
				System.out.println("No best answer found for visual control!");
				return visualRawList;
			} 
			else{
				return bestAnswer;
			}*/
		}
	}
	
	/* optimization without MQ for M-CHESS online */
	public ArrayList<AppNode> getOptVisualListForOnline(ArrayList<AppNode> eusList, GAinference gaInference) {
		// get visual List
		ArrayList<AppNode> visualAppList = new ArrayList<AppNode>();
		ArrayList<AppNode> visualRawList = new ArrayList<AppNode>();
		for (AppNode app : eusList) {
			if (app.agentName.equals("visual")) {
				AppNode app2 = app.copyAppNode(app);
				visualAppList.add(app2);
				AppNode app3 = app.copyAppNode(app);
				visualRawList.add(app3);
			}
		}
		
		// visual App list有可能是空的
		if (visualAppList.size() == 0) {
			return new ArrayList<AppNode>();
		} else {
			// 從eusList中和visual有關的電氣，找出所有的狀態排列組合
			Optimizer op = new Optimizer();
			ArrayList<String> candidateList = op.buildCandidateList(visualAppList);

			int iterateCounter = 0;
			ArrayList<AppNode> bestAnswer = null;
			while ((bestAnswer == null || bestAnswer.size() == 0) && iterateCounter < iterateLimit) {
				// for debug
				if (bestAnswer != null && bestAnswer.size() == 0) {
					int aa = 0;
					aa++;
				}
				bestAnswer = visualIterate(candidateList, visualAppList, visualRawList, gaInference, iterateCounter);
				iterateCounter++;
				//System.out.println("iterateCounter = " + iterateCounter);
			}
			
			if (bestAnswer.size() == 0) {
				System.out.println("No best answer found for visual control!");
				return visualRawList;
			} else {
				return bestAnswer;
			}
		}

	}
	
	public void computeILLForEachLocation(ArrayList<AppNode> decisionList, GAinference gaInference) {
		Map<String, ArrayList<AppNode>> actAppList = getActAppList(decisionList, gaInference);
		
		Set<String> keySet = actAppList.keySet();
		for (String act : keySet) {
			String location = getActLocation(act);
			VisualComfortTable vct = EnvStructure.visualComfortTableList.get(location);
			ArrayList<AppNode> appList = actAppList.get(act);
			// 燈光的搭配組合取得lux
			//double lux = vct.getLuxFromTable(appList);
			// Get lux from sensor reading of that location
			double lux = Esdse.illuminationReading.get(location);
			// get real lux level
			double realLuxLevel = LuxLevel.transformLuxLevel(lux);
			// get ideal lux level
			double idealLuxLevel = IdealLuxLevel.getIdealLuxLevel(act);
			double ILL = (realLuxLevel - idealLuxLevel) / 3;
			
			Map<String, Integer> priorityList = new HashMap<String, Integer>();
			setActivityPriority(priorityList);
			if (locationActivity.get(location) == null || priorityList.get(act) < priorityList.get(locationActivity.get(location))) {
				illEvaluationResult.put(location, ILL);
				locationActivity.put(location, act);
			}
		}
		
		for (String location : locationActivity.keySet()) {
			if (locationActivity.get(location) == null) {
				String act = null; // dummy activity
				if (location.equals("hallway")) act = "ComeBack";
				else if (location.equals("livingroom")) act = "WatchingTV";
				else if (location.equals("kitchen")) act = "PreparingFood";
				else if (location.equals("bedroom")) act = "UsingPC";
				else if (location.equals("study")) act = "Studying";
				ArrayList<AppNode> appList = actAppList.get(act);
				
				double lux = Esdse.illuminationReading.get(location);
				// get real lux level
				double realLuxLevel = LuxLevel.transformLuxLevel(lux);
				// get ideal lux level
				double idealLuxLevel = 7; // Temporary
				double ILL = (realLuxLevel - idealLuxLevel) / 3;
				
				illEvaluationResult.put(location, ILL);
				locationActivity.put(location, act);
			}
		}
	}
	
	/* Calculate ILL according to each activity and record <location_activity, pmv> 
	 * Get ideal lux to make ILL to be -1
	 * */
	public void computeILLForEachLocation2(ArrayList<AppNode> decisionList, GAinference gaInference) {
		Map<String, ArrayList<AppNode>> actAppList = getActAppList(decisionList, gaInference);
		
		Set<String> keySet = actAppList.keySet();
		for (String act : keySet) {
			String location = getActLocation(act);
			//VisualComfortTable vct = EnvStructure.visualComfortTableList.get(location);
			//ArrayList<AppNode> appList = actAppList.get(act);
			// Get lux from sensor reading of that location
			double lux = Esdse.illuminationReading.get(location);
			// get real lux level
			double realLuxLevel = LuxLevel.transformLuxLevel(lux);
			// get ideal lux level
			double idealLuxLevel = IdealLuxLevel.getIdealLuxLevel(act);
			double ILL = (realLuxLevel - idealLuxLevel) / 3;
			//double ILL = Math.log(1 + Math.abs(realLuxLevel - idealLuxLevel)) / Math.log(2);
			//if ((realLuxLevel - idealLuxLevel) < 0) ILL *= -1;
			illEvaluationResult.put(location + "_" + act, ILL);
			
			// If ILL doesn't fulfill the constraint we get ideal lux
			// else we put -1, which means we don't need to control the light
			if(Math.abs(ILL) > initConstraint){
				// According to each activity, how much lux we need if we want ILL to be -1
				double idealLux = IdealLuxLevel.getMarginIdealLux(act);
				if(marginIdealLux.keySet().contains(location)){
					if(marginIdealLux.get(location) > idealLux){
						marginIdealLux.put(location, idealLux);
					}
				}
				else{
					marginIdealLux.put(location, idealLux);
				}
			}
			else{
				marginIdealLux.put(location, -1.0);
			}
		}
	}
	
	/* Send ILL to MQ */
	public void sendILLToMQ(ArrayList<AppNode> thermalRawList, GAinference gaInference, Producer producer) {
		JsonBuilder json = MessageUtils.jsonBuilder();
		String location;
		String activity;
		computeILLForEachLocation2(thermalRawList, gaInference);
		for (String key : illEvaluationResult.keySet()) {
			String[] split = key.split("_");
			location = split[0];
			activity = split[1];
			producer.sendOut(json.add("subject", "ill").add("location", location).add("activity", activity).add("value", String.valueOf(illEvaluationResult.get(key))).toJson(), "ssh.CONTEXT");
			//System.err.println(location + " ill :" + illEvaluationResult.get(location));
		}
	}

	private ArrayList<Double> relaxConstraint(Set<String> actInferResultSet, ArrayList<Double> constraintList, int counter) {
		Map<String, Integer> priorityList = new HashMap<String, Integer>();
		setActivityPriority(priorityList);

		Map<String, Integer> inferPriorityList = new HashMap<String, Integer>();

		for (String str : actInferResultSet) {
			inferPriorityList.put(str, priorityList.get(str));
		}

		List<Map.Entry<String, Integer>> listData = new ArrayList<Map.Entry<String, Integer>>(inferPriorityList.entrySet());

		Collections.sort(listData, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue() - o1.getValue());
			}
		});
		// for(Map.Entry<String, Integer> entry:listData){
		// System.out.println(entry.getKey()+" "+inferPriorityList.get(entry.getKey()));
		// }

		for (int i = 0; i < actInferResultSet.size(); i++) {
			constraintList.add(initConstraint);
		}

		int i = 0;
		for (Map.Entry<String, Integer> entry : listData) {
			String key = entry.getKey();
			int j = 0;
			for (String str : actInferResultSet) {
				if (key.equals(str)) {
					break;
				}
				j++;
			}
			if (i == 0) {
				constraintList.set(j, 1 + 0.7 * counter);
			} else if (i == 1) {
				constraintList.set(j, 1 + 0.3 * counter);
			}
			i++;
		}

		return constraintList;
	}
	
	private void setActivityPriority(Map<String, Integer> priorityList) {
		/* Simulator */
//		priorityList.put("GoOut", 17);
//		priorityList.put("ComeBack", 16);
//		priorityList.put("WatchingTV", 3);
//		priorityList.put("PlayingKinect", 6);
//		priorityList.put("Chatting", 5);
//		priorityList.put("ReadingBook", 4);
//		priorityList.put("Cleaning", 10);
//		priorityList.put("Cooking", 13);
//		priorityList.put("WashingDishes", 9);
//		priorityList.put("Laundering", 15);
//		priorityList.put("Studying", 7);
//		priorityList.put("Sleeping", 1);
//		priorityList.put("ListeningMusic", 8);
//		priorityList.put("UsingPC", 2);
//		priorityList.put("TakingBath", 14);
//		priorityList.put("UsingRestroom", 12);
//		priorityList.put("BrushingTooth", 11);
		
		/* BL313 */
		priorityList.put("GoOut", 10);
		priorityList.put("ComeBack", 9);
		priorityList.put("WatchingTV", 4);
		priorityList.put("PlayingKinect", 7);
		priorityList.put("Chatting", 6);
		priorityList.put("Reading", 5);
		priorityList.put("PreparingFood", 8);
		priorityList.put("Sleeping", 2);
		priorityList.put("AllSleeping", 1);
		priorityList.put("UsingPC", 3);
		priorityList.put("UsingNoteBook", 11);
		priorityList.put("Studying", 12);
	}

	private ArrayList<AppNode> visualIterate(ArrayList<String> candidateList, ArrayList<AppNode> visualAppList, ArrayList<AppNode> visualRawList, GAinference gaInference, int counter) {

		// 原始環境的pmv和power consumption
		// ThermalAgent ta=new ThermalAgent();
		ArrayList<Double> rawIllList = getComfortArrayFromEnvironment(visualRawList, gaInference);
		double rawAmp = Optimizer.calEnergyConsumption(visualRawList);

		// best answer
		VisualSolution vs = new VisualSolution();

		//ArrayList<Double> constraintList = new ArrayList<Double>();

		if (counter == 0) {
			// initial constraint array
			constraintList = new ArrayList<Double>();
			for (int i = 0; i < gaInference.actInferResultSet.size(); i++) {
				constraintList.add(initConstraint);
			}
		} else {
			/* 調整constraint */

			/* 方法一 */
//			for (int i = 0; i < gaInference.actInferResultSet.size(); i++) {
//				constraintList.add(secConstraint);
//			}
			/* 方法二 根據活動的priority */
			//relaxConstraint(gaInference.actInferResultSet,constraintList,counter);
			/* 方法三 */
//			initConstraint = initConstraint + incrementConstraint;
//			for (int i = 0; i < gaInference.actInferResultSet.size(); i++) {
//				constraintList.add(initConstraint);
//			}

			/* for experiment record conflict的次數 */
			//ExpRecorder.exp.setVisualConflictCount();
			/* exp record end */
		}
		
		ArrayList<Integer> notMetConstraintList = new ArrayList<Integer>();
		// iterate
		for(String candidate : candidateList){
			notMetConstraintList.clear();
			Optimizer.updateState(visualAppList, candidate);

			ArrayList<Double> illList = getComfortArray(visualAppList, gaInference);
			double amp = Optimizer.calEnergyConsumption(visualAppList);
			// 判斷illList是否全部都在constraint內
			boolean flag = true;
			for (int i = 0; i < illList.size(); i++) {
				double ill = illList.get(i);
				double constraint = constraintList.get(i);
				if (Math.abs(ill) > constraint) {
					//System.out.println("no good candidate = " + candidate + "rawILL = " + illList.get(0) + "constraint = " + constraintList);
					flag = false;
					notMetConstraintList.add(i);
					//break;
				}
			}
			if (flag) {
				vs = visualListEvaluation(visualAppList, illList, amp, vs, rawIllList, rawAmp);
				System.out.println("candidate = " + candidate + "rawILL list = " + illList + " constraint = " + constraintList);
			}
		}
		
		if (vs.solution.size() == 0) {
			for (Integer i : notMetConstraintList) {
				constraintList.set(i, constraintList.get(i) + incrementConstraint);
			}
		}

		return vs.solution;
	}

	private VisualSolution visualListEvaluation(ArrayList<AppNode> visualAppList, ArrayList<Double> illList, double amp, VisualSolution vs, ArrayList<Double> rawIllList, double rawAmp) {
		if (vs.setFlag == false) {

			vs.copy(visualAppList, illList, amp);
			vs.setFlag = true;
		} else {
			/* evaluate best answer的條件 */
			if (amp < vs.totalAmp) {
				vs.copy(visualAppList, illList, amp);
			}
		}

		return vs;

	}

	class VisualSolution {
		ArrayList<AppNode> solution = new ArrayList<AppNode>();
		ArrayList<Double> solutionILL = new ArrayList<Double>();
		double totalAmp = 0;
		Boolean setFlag = false;

		public void copy(ArrayList<AppNode> visualAppList, ArrayList<Double> illList, double amp) {
			solution.clear();
			solutionILL.clear();
			for (AppNode app : visualAppList) {
				solution.add(app.copyAppNode(app));
			}
			for (Double pmv : illList) {
				solutionILL.add(pmv);
			}
			totalAmp = amp;
		}

	}
}
