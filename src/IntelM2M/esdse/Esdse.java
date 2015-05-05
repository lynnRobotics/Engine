package IntelM2M.esdse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import s2h.platform.node.PlatformMessage;
import s2h.platform.node.PlatformTopic;
import s2h.platform.node.Sendable;
import s2h.platform.support.JsonBuilder;
import s2h.platform.support.MessageUtils;
import IntelM2M.agent.control.ControlAgent;
import IntelM2M.agent.thermal.ThermalAgent;
import IntelM2M.agent.visual.VisualAgent;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.RelationTable;
import IntelM2M.datastructure.SensorNode;
import IntelM2M.epcie.Epcie;
import IntelM2M.epcie.GAinference;
import IntelM2M.epcie.classifier.GaDbnClassifier;
import IntelM2M.epcie.erc.GaEscGenerator;
import IntelM2M.exp.ExpRecorder;
import IntelM2M.mchess.Mchess;
import IntelM2M.mq.Producer;
import IntelM2M.preference.PreferenceAgent;
import IntelM2M.preference.PreferenceAgent.PreferenceModel;
import IntelM2M.test.SimulatorTest;

/**
 * 
 * @author Mao (2012.06)
 * Revised by Shu-Fan, 2013/11/19
 */

public class Esdse {
	
	/* MQ related */
	public Producer producer = new Producer();
	private JsonBuilder json = MessageUtils.jsonBuilder();
	private final String MQ_URL = "tcp://140.112.49.154:61616";
	private int reconnect_counter = 0;

	/* Preference and Control agent */
	private PreferenceAgent pr = new PreferenceAgent();
	public ControlAgent controlAgent = new ControlAgent(producer);
	
	/* Different kinds of reading */
	public static Map<String, String> sensorReading = new LinkedHashMap<String, String>();
	public static Map<String, Double> ezMeterAmpereReading = new LinkedHashMap<String, Double>();
	public static Map<String, Double> temperatureReading = new LinkedHashMap<String, Double>();
	public static Map<String, Double> humidityReading = new LinkedHashMap<String, Double>();
	public static Map<String, Double> illuminationReading = new LinkedHashMap<String, Double>();
	
	/* Maintain AC temperature */
	public static int acTemperature_livingroom = 20;
	public static int acTemperature_bedroom = 20;
	
	/* Check whether all the sensors are collected */
	private ArrayList<String> updatedSensorList = new ArrayList<String>();
	public Date firstSensorArrivalTime = null;
	/* sensorReading.size() - "current" - "camera" - "audio"
	 * 24 - 12 - 5 - 1 = 6
	 * */
	int sensorCount = 6;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
	
	/* Activity intensity usage */
	public static double accumulatedCalorie = 0;
	public ArrayList<Double> calorieTimeWindow = new ArrayList<Double>();
	public final int calorieTimeWindowMaxSize = 300;
	public static boolean isPlayingKinect = false;
	
	/* Control the appliance or not */
	private boolean doControl = true;
	private boolean initialization = true;
	
	/* Some parameters for rule based condition*/
	private boolean activityChanged = false;
	private boolean wakeUpFlag = false;
	private boolean AllSleeping = false;
	public static boolean TVControlOn = false;
	//private boolean standbyOff = false;
	
	/* Communicate with web page for confirming the control */
	public boolean signal = true;
	private Date noSignalStartTime = null;
	private final int maxNoSignalDuration = 45; // unit is second
	public boolean reject = false;
	public boolean backToLive = false;
	
	/* Hallway activity recognition usage */
	private int NumOfPeople = 0;
	private int OldNumOfPeople = 0;
	//private boolean ToBeGoOut = false; // Assume the initial condition is GoOut
	private boolean standbyAllOn = false; // Assume the initial condition is Standby Off
	private int hallwayLightLuxTreshold = 30;
	private boolean doorLightOn = false;
	
	/* For new testing data collection (Yi-Hsiu's experiment usage)*/
	private String sensorDataVector;
	private String sensorDataVectorStoredPath;
	private FileWriter sensorDataVectorWriter;
	
	
	public Esdse() {
		// MQ producer send initialization signal to web interface
		producer.setURL(MQ_URL);
		while(!producer.connect());
		producer.getSendor();
		json.reset();
		producer.sendOut(json.add("subject", "signal").add("initialization", "start").toJson(), "ssh.CONTEXT");
		
		
		/* 
		 Initialization of status of each appliance according to sensor number 
			0 light_livingroom
			1 light_hallway
			2 light_bedroom
			3 light_kitchen
			4 light_study
			5 switch_door_hallway
			6 audio_livingroom
			7 current_watercoldfan_livingroom
			8 current_lamp_bedroom
			9 current_PC_bedroom
			10 current_TV_livingroom
			11 current_lamp_livingroom
			12 current_xbox_livingroom
			13 current_microwave_kitchen
			14 current_AC_bedroom
			15 current_AC_livingroom
			16 current_lamp_study
			17 current_NB_study
			18 current_nightlamp_bedroom
			19 people_hallway
			20 people_livingroom
			21 people_bedroom
			22 people_kitchen
			23 people_study
		 */
		// A map to store status of each sensor and <Name, on off...>
		Map<String, ArrayList<String>> sensorStatus = EnvStructure.sensorStatus;
		// Extract list of sensor name from sensorStatus
		String[] sensorNameArray = (String[]) sensorStatus.keySet().toArray(new String[0]);
		int i = 0;
		for (String sensorName : sensorNameArray) {
			if (i == 7 || i == 9 || i == 13 || i == 14 || i == 15 || i == 17) {
				sensorReading.put(sensorName, "standby");
			} else {
				sensorReading.put(sensorName, "off");
			}
			i++;
		}
		
		// Initialization of comfort sensor reading according to location
		for (String location : EnvStructure.roomList) {
			temperatureReading.put(location, null);
			humidityReading.put(location, null);
			illuminationReading.put(location, null);
		}
		
		// Initialization of ezmeter reading of each appliance
		for (String appliance : EnvStructure.applianceNameList) {
			ezMeterAmpereReading.put(appliance, -1.0);
		}
		
		// Initialization of each calorie value to zero in the time window 
		for (i = 0; i < calorieTimeWindowMaxSize; i++) {
			calorieTimeWindow.add(0.0);
		}
		
		// Initialization of testing data store path (Yi-Hsiu's experiment usage)
		sensorDataVectorStoredPath = "./_output_results/collected_data/collectedData_" + new Date() + ".txt";
		sensorDataVectorStoredPath = sensorDataVectorStoredPath.replace(":", " ");
		try {
			sensorDataVectorWriter = new FileWriter(new File(sensorDataVectorStoredPath), false);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/* If there's a GA we need to merge them (no longer use) */
	private ArrayList<AppNode> eusAggregation(GAinference gaInference, String envContext) {
		ArrayList<String> gaInferResultList = gaInference.gaInferResultList;
		/* Get eus */
		ArrayList<AppNode> eusAggregationList = new ArrayList<AppNode>();
		/* There might be nothing after inference */
		if (gaInferResultList.size() == 0) {
			/* Copy app from appList */
			Map<String, AppNode> appList = EnvStructure.appList;
			Set<String> keySet = appList.keySet();
			for (String str : keySet) {
				AppNode app = appList.get(str);
				AppNode newApp = app.copyAppNode(app);
				eusAggregationList.add(newApp);
			}
			// Update EUS from sensorReading (I don't really understand)
			/* Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
			   String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
			   String[] split = envContext.split("#");
			   String []sensorContext=split[0].split(" "); */
		} 
		else {
			for (String str : gaInferResultList) {
				for (GaEscGenerator gaEsc : gaInference.GaEscList) {
					boolean containKey = gaEsc.actAppList.containsKey(str);
					if (containKey) {
						ArrayList<AppNode> tmpList = gaEsc.actAppList.get(str).appList;
						for (AppNode tmp : tmpList) {
							AppNode app = tmp.copyAppNode(tmp);

							int same = -1;
							for (int i = 0; i < eusAggregationList.size(); i++) {
								if (eusAggregationList.get(i).appName.equals(app.appName)) same = i;
							}

							if (same < 0) eusAggregationList.add(app);
							else {
								int newPriority = getPriority(app);
								int oldPriority = getPriority(eusAggregationList.get(same));
								if (newPriority < oldPriority) {
									eusAggregationList.get(same).state = app.state;
									eusAggregationList.get(same).escType = app.escType;
									eusAggregationList.get(same).confidence = app.confidence;
								}
							}
						}
					}
				}
			}
		}

		/* Update EUS from sensorReading */
		/* A map to store status of each sensor and extract list of sensor name from sensorStatus  */
		Map<String, ArrayList<String>> sensorStatus = EnvStructure.sensorStatus;
		String[] sensorName = (String[]) sensorStatus.keySet().toArray(new String[0]);
		/* Extract environment context */
		String[] split = envContext.split("#");
		String[] sensorContext = split[0].split(" ");

		for (AppNode eus : eusAggregationList) {
			for (int i = 0; i < sensorName.length; i++) {
				if (eus.appName.equals(sensorName[i])) {
					eus.envContext = sensorContext[i];
				}
			}
		}
		
		return eusAggregationList;
	}

	/* If there's a GA we need to merge them (real-time version) */
	private ArrayList<AppNode> eusAggregationForRealTime(GAinference gaInference, Map<String, String> sensorReading){

		ArrayList<String> gaInferResultList = gaInference.gaInferResultList;
		
		ArrayList<AppNode> eusAggregationList = new ArrayList<AppNode>();
		// Inference result set might be empty 
		if(gaInferResultList.size() == 0){
			// Copy app from appList
			Map<String, AppNode> appList = EnvStructure.appList;
			Set<String> keySet = appList.keySet();
			for(String str : keySet){
				AppNode app = appList.get(str);
				AppNode newApp = app.copyAppNode(app);
				eusAggregationList.add(newApp);
			}
		}
		else{
			for(String str : gaInferResultList){
				for (GaEscGenerator gaEsc : gaInference.GaEscList) {
					boolean containKey = gaEsc.actAppList.containsKey(str);
					if(containKey){
						ArrayList<AppNode> tmpList = gaEsc.actAppList.get(str).appList;
						for (AppNode tmp : tmpList) {
							AppNode app = tmp.copyAppNode(tmp);
							int same = -1;
							for (int i = 0; i < eusAggregationList.size(); i++) {
								if(eusAggregationList.get(i).appName.equals(app.appName)) same = i;
							}
							if(same < 0) eusAggregationList.add(app); 
							else {
								int newPriority = getPriority(app);
								int oldPriority = getPriority(eusAggregationList.get(same));
								if (newPriority < oldPriority) {
									eusAggregationList.get(same).state = app.state;
									eusAggregationList.get(same).escType = app.escType;
									eusAggregationList.get(same).confidence = app.confidence;
								}
							}
						}
					}
				}
			}
		}

		// Update EUS from sensorReading
		// A map to store status of each sensor and extract list of sensor name from sensorStatus
		Map<String, ArrayList<String>> sensorStatus = EnvStructure.sensorStatus;
		String[] sensorName = (String[]) sensorStatus.keySet().toArray(new String[0]);

		for(AppNode eus : eusAggregationList){
			for(int i = 0; i < sensorName.length; i++){
				if (eus.appName.equals(sensorName[i])) eus.envContext = sensorReading.get(sensorName[i]);
			}
		}
		return eusAggregationList;
	}

	/* This is appliance agent dispatcher (no longer use) */
	private void eusDispatch(ArrayList<AppNode> eusList, GAinference gaInference) {
		/* get single activity set */
		Set<String> actInferResultSet = gaInference.actInferResultSet;

		/* get act location list */
		Set<String> actLocation = new HashSet<String>();

		Map<String, RelationTable> actAppList = EnvStructure.actAppList;
		Set<String> actRoomSet = actAppList.keySet();

		for (String str : actInferResultSet) {
			for (String str2 : actRoomSet) {
				if (str2.contains(str)) {
					String location = str2.split("_")[0];
					actLocation.add(location);
				}
			}
		}
		//
		for (AppNode app : eusList) {
			/*
			 * 設定冷氣 這行可能有bug 不在家的活動要拿掉冷氣:GoOut
			 */
			//FIXME checked
			app.agentName = "ap"; // Set before in case no matching
			if (app.comfortType.equals("thermal") && app.global && !actInferResultSet.contains("GoOut")) {
				app.agentName = "thermal";
			} else if (app.state.equals("on") && app.comfortType.equals("visual")) {
				for (String str : actLocation) {
					if (app.location.equals(str)) {
						app.agentName = "visual";
					}
				}
			} else if (app.state.equals("on") && app.comfortType.equals("thermal")) {
				for (String str : actLocation) {
					if (app.location.equals(str)) {
						app.agentName = "thermal";
					}
				}
			} else {
				app.agentName = "ap";
			}
		}
	}
	
	/* This is appliance agent(thermal, visual) dispatcher (new version) */
	private void eusDispatch_new(ArrayList<AppNode> eusList, GAinference gaInference) {
		// Get inferred activity set
		Set<String> actInferResultSet = gaInference.actInferResultSet;
		// Store location of inferred activity
		Set<String> actLocation = new HashSet<String>();
		// Get all set of combination of room and activity
		Map<String, RelationTable> actAppList = EnvStructure.actAppList;
		Set<String> actRoomSet = actAppList.keySet();
		
		// Match inferred activity with room-activity set 
		// If there's a match then store it in actLocation
		for (String str : actInferResultSet) {
			for (String str2 : actRoomSet) {
				if (str2.contains(str)) {
					String location = str2.split("_")[0];
					actLocation.add(location);
				}
			}
		}
		
		for (AppNode app : eusList) {
			// Set before in case no matching
			app.agentName = "ap";
			if (app.comfortType.equals("thermal") && app.global && !actInferResultSet.contains("GoOut")){
				// Since current_AC_livingroom is in global, we need to make sure it won't be dispatched to
				// thermal agent if activity only happens in bedroom
				if(!actLocation.contains("bedroom") || actLocation.size() != 1){
					app.agentName = "thermal";
				}
			}
			else if(app.comfortType.equals("visual")){
				for (String str : actLocation) {
					if (app.location.equals(str)) {
						app.agentName = "visual";
					}
				}
			} 
			else if(app.comfortType.equals("thermal")){
				for (String str : actLocation) {
					if (app.location.equals(str)) {
						app.agentName = "thermal";
					}
				}
			} 
			else {
				app.agentName = "ap";
			}
		}
	}

	/* This function is for simulator (No longer use) */
	public void processForSimulator(Epcie epcie, String read, String read2, String updatedRead) {

		/* Infer GA */
		String processRead = SimulatorTest.rawDataPreprocessing(updatedRead);
		epcie.gaInferenceForSimulator(processRead);

		/* 1. EUS aggregation */
		ArrayList<AppNode> eusList = eusAggregation(epcie.gaInference, read);/* 這邊怪怪，我沒有用process的read function裡面自己有process */
		ArrayList<AppNode> decisionList = null;
		/* infer 有可能沒有結果 */
		if (epcie.gaInference.gaInferResultList.size() == 0) {
			decisionList = eusList;
		} else {

			/* 2. EUS dispatch */
			eusDispatch(eusList, epcie.gaInference);/* update eusList */

			/* 3 optimization */
			Optimizer op = new Optimizer();
			decisionList = op.getOptDecisionList(eusList, epcie.gaInference);
		}

		/* update decision for lastRead */
		/* todo2 :check if error */
		SimulatorTest.updateDecisionForMchess(decisionList);
		SimulatorTest.setLastDataForMchess(read2, decisionList);

		/* 5 record result */
		ExpRecorder.exp.processEXP(read, read2, epcie, decisionList, eusList);

	}
	
	/* This function is for simulator (No longer use) */
	public void processForSimulatorWithPR_new(Epcie epcie, String read, String read2, String updatedRead) {

		/* 更新READ的資料 */
		/*
		 * 我上次"關掉的電器中"，如果"確認是上次的noise"，且也是"這次的noise"， 那我就直接重這次的noise中刪除這些電器，且加入我的turn off 陣列中
		 */

		/* Infer GA */

		String processRead = SimulatorTest.rawDataPreprocessing(updatedRead); // 去掉電器的狀態，例如on_19後面的_19會被去掉
		epcie.gaInferenceForSimulator(processRead);

		/* 1. EUS aggregation */
		ArrayList<AppNode> eusList = null;
		ArrayList<AppNode> decisionList = null;

		/* infer 結果可能為空 */
		if (epcie.gaInference.gaInferResultList.size() == 0) {
			eusList = eusAggregation(epcie.gaInference, read);
			decisionList = eusList;
		} else if (pr.inferPRmodel(epcie.gaInference) == null) {
			/* 1.EUS aggregation */
			eusList = eusAggregation(epcie.gaInference, read);
			/* 2. EUS dispatch */
			eusDispatch(eusList, epcie.gaInference);/* update eusList */
			/* 3 optimization */
			Optimizer op = new Optimizer();
			decisionList = op.getOptDecisionList(eusList, epcie.gaInference);
			/* 這邊可能有錯，檢查decisionList和gaInference是否有被update */
			/* todo4 : chek if error */
			pr.buildPRModel(decisionList, epcie.gaInference);
		} else {
			PreferenceModel prM = pr.inferPRmodel(epcie.gaInference);
			pr.updateInferResult(epcie.gaInference, prM);
			/* 1.EUS aggregation */
			eusList = eusAggregation(epcie.gaInference, read);
			/* 2. EUS dispatch */
			eusDispatch(eusList, epcie.gaInference);/* update eusList */
			/* 3 optimization */
			Optimizer op = new Optimizer();
			decisionList = op.getOptDecisionList(eusList, epcie.gaInference);
		}

		/* update decision for lastRead */
		/* todo2 :check if error */
		SimulatorTest.updateDecisionForMchess(decisionList);
		SimulatorTest.setLastDataForMchess(read2, decisionList);

		/* 5 record result */
		ExpRecorder.exp.processEXP(read, read2, epcie, decisionList, eusList);

		/* update prModel with feedback */
		pr.userFeedback_new(epcie.gaInference, read, read2);

	}
	
	/* Go through all process (Recognition -> Provide Service) */
	public void processForRealTime(Epcie epcie, String message) {
		// If still in the process of initialization then we don't infer
		if (initialization) {
			return;
		}
		
		SensorNode sensorNode = GaDbnClassifier.getSensorNode(message);
		// Do not process unknown sensor node
		if (sensorNode == null) {
			return;
		}
		
		// Compute human number
		int humanNumber = 0;
		for (String sensorName : sensorReading.keySet()) {
			if (sensorName.contains("people") && sensorReading.get(sensorName).startsWith("on")) {
				humanNumber += Integer.parseInt(sensorReading.get(sensorName).split("_")[1]);
			}
		}
		NumOfPeople = humanNumber;
		
		// Old version of Check Comeback or GoOut
		/*
		// Set parameter for checking Whether GoOut or not
		// If there's no people at home and previous people count > present people count
		// and sensorNode is switch_door_hallway, value is on
		//if ((NumOfPeople == 0) && (OldNumOfPeople > NumOfPeople)) {
		if (NumOfPeople == 0){ // for demo
		    //if (sensorNode.name.equals("switch_door_hallway") && sensorNode.discreteValue.equals("on")) {
		    if (sensorNode.discreteValue.equals("on")){ // for demo, ToBeGoOut=0 person
			    ToBeGoOut = true;
		        OldNumOfPeople = NumOfPeople;
		    }
		}
		else {
		    OldNumOfPeople = NumOfPeople;
		}
		
		// Rule-based check and control ComeBack & GoOut
		checkComeBackAndGoOut(sensorNode);
		*/
		
		// New version of Check ComeBack or GoOut
		// 1. Check Hallway Activity (Door-light)
		checkHallwayActivity(sensorNode);
		// 2. Check ComeBack or GoOut (Standby)
		checkComeBackandGoOut(sensorNode);
		// 3. Update Number of People
		if (NumOfPeople>0)
			OldNumOfPeople = NumOfPeople;
		else if (sensorNode.discreteValue.equals("on"))
			OldNumOfPeople = NumOfPeople;
		
		
		// Update sensor reading
		sensorReading.put(sensorNode.name, sensorNode.discreteValue);
		// If this sensor hasn't shown at this round then add to updatedSensorList
		if (!updatedSensorList.contains(sensorNode.name)) {
			updatedSensorList.add(sensorNode.name);
			// For debugging, print out how many sensor's already updated during this round
			//if(!initialization) System.out.print(updatedSensorList.size() + " ");
			if (updatedSensorList.size() == 1) {
				firstSensorArrivalTime = new Date();
			}
		}
		
		// Original initialization return position
		
		// If the received sensor number isn't enough and waiting time is still under boundary (1.5s)
		// then we don't infer
		if (updatedSensorList.size() < sensorCount) {
			//System.out.print(updatedSensorList.size() + " ");
			if (((new Date().getTime() - firstSensorArrivalTime.getTime()) / 1000.0) < 3) return;
		}

		// For debugging, print out the name of last added sensor
		// System.out.println("last add = " + sensorNode.name);
		
		updatedSensorList.clear();
		
		// For debugging, print out sensor readings
		Map<String, ArrayList<String>> sensorStatus = EnvStructure.sensorStatus;
		String[] sensorNameArray = (String[]) sensorStatus.keySet().toArray(new String[0]);
		
		if(epcie == null) { // jump out if it is in a learning mode
			for (String sensorName : sensorNameArray) {
				String featureString = sensorReading.get(sensorName).split("_")[0];
				System.out.println("featureString"+ featureString + " ");
			}
			/////
			// print the Activity Label System.out.print("#"+...);
			/////
			System.out.println();
			return;
		}
		////////////////////// running mode //////////////////////

		System.out.println(); // start with "@" for logging
		System.out.println("@ ===================================="); // start with "@" for logging
		for (String sensorName : sensorNameArray) {
			System.out.println(sensorName + ":" + sensorReading.get(sensorName) + ",");
		}
		System.out.println();
		
		// Show comfort sensor readings according to each location
		System.out.println("@@ ==================================="); // start with "@" for logging
		for (String location : EnvStructure.roomList) {
			System.out.println(location + ":" + temperatureReading.get(location) + " C,");
			System.out.println(location + ":" + humidityReading.get(location) + " %,");
			System.out.println(location + ":" + illuminationReading.get(location) + " Lux,");
		}
		System.out.println();
		
		// Print out number of human in this iteration
		System.out.println("#humanNumer = " + humanNumber);
		
		// Infer GA
		epcie.gaInferenceForRealTime_new(sensorReading, humanNumber);
		
		// Eliminate some impossible activities
		activityPostProcessing(epcie);
		
		// Flag for PlayingKinect
		isPlayingKinect = false; 
		// Print out inferred activity
		if (epcie.gaInference.actInferResultSet.size() == 0) {
			System.out.println("@@@Infer: NoActivity");
		}
		else {
			for(String activity : epcie.gaInference.actInferResultSet){
				if (activity.equals("PlayingKinect")) {
					isPlayingKinect = true;
				}
				System.out.println("@@@Infer: " + activity);
			}
		}
		System.out.println(","+sdf.format(Calendar.getInstance().getTime()));
		
		// Deal with playingKinect, Reset playingKinet related parameters
		if (!isPlayingKinect) {
			accumulatedCalorie = 0;
			EnvStructure.actAppList.get("livingroom_PlayingKinect").intensity = 60;
			calorieTimeWindow.clear();
			for (int i = 0; i < calorieTimeWindowMaxSize; i++) {
				calorieTimeWindow.add(0.0);
			}
		}
		// Check unwanted (ComeBack & GoOut) activity
		// Now we address ComeBack & GoOut with some rules
		if (epcie.gaInference.actInferResultSet.contains("ComeBack") || epcie.gaInference.actInferResultSet.contains("GoOut")) {
			return;
		}
		
		// Check inferred activity set with previous time 
		// If there's one activity not including in the previous inferred set, set sameInferResult = false; 
		epcie.currentActInferResultSet = new ArrayList<String>(epcie.gaInference.actInferResultSet);
		boolean sameInferResult = true;
		for (String activity : epcie.currentActInferResultSet) {
			if (!epcie.previousActInferResultSet.contains(activity)) {
				sameInferResult = false;
				break;
			}
		}
		
		if (epcie.currentActInferResultSet.contains("WatchingTV") || epcie.currentActInferResultSet.contains("PlayingKinect"))
			TVControlOn = true;
		
		// If inferred activity set equals to previous time
		Date currentTime = new Date();
		if (epcie.currentActInferResultSet.size() == epcie.previousActInferResultSet.size() && sameInferResult) {
			epcie.duration = (currentTime.getTime() - epcie.startTime.getTime()) / 1000.0;
			activityChanged = false;
			// Make sure activity changed is not because some noise
			if (epcie.duration < epcie.threshold) {
				activityChanged = true;
				System.err.println("Still in the non-stable period!");
				System.out.println("===========================================");
				return;
			}
		}
		else {
			System.err.println("Activity change happened!");
			// Decide wake up or not
			if (epcie.previousActInferResultSet.contains("AllSleeping")) {
				wakeUpFlag = true;
			}
			epcie.previousActInferResultSet = new ArrayList<String>(epcie.gaInference.actInferResultSet);
			epcie.startTime = currentTime;
			System.out.println("===========================================");
			return;
		}
		
		// Update previous result set
		epcie.previousActInferResultSet = new ArrayList<String>(epcie.gaInference.actInferResultSet);
		
		// Check previous activity for reject mode 
		// If activity set is different between present set and reject set 
		// activity've changed, reject = false;
		if(epcie.gaInference.actInferResultSet.size() != epcie.previousActInferResultSetForReject.size()){
			reject = false;
		}
		else{
			for(String activity : epcie.gaInference.actInferResultSet){
				if(!epcie.previousActInferResultSetForReject.contains(activity)){
					sameInferResult = false;
					System.err.println("Inferred activity is different from the one in reject mode!");
					reject = false;
					break;
				}
			}
		}
		
		// Reject period
		if (reject && sameInferResult) {
			epcie.previousActInferResultSetForReject = new ArrayList<String>(epcie.gaInference.actInferResultSet);
			sendInferedActivityToMQ(epcie);
			System.err.println("In the rejected period!");
			return;
		}
		
		epcie.previousActInferResultSetForReject = new ArrayList<String>(epcie.gaInference.actInferResultSet);
		
		// Send inferred activity to MQ
		sendInferedActivityToMQ(epcie);
		
		// For testing data recording (Yi-Hsiu)
		recordSensorDataVector(epcie);
		
		ArrayList<AppNode> eusList = null;
		ArrayList<AppNode> decisionList = null;
		
		// Optimization step
		// Inferred result set might be empty
		if(epcie.gaInference.actInferResultSet.size() == 0){
			eusList = eusAggregationForRealTime(epcie.gaInference, sensorReading);
			decisionList = eusList;
		} 
		else {
			// 1.EUS aggregation
			eusList = eusAggregationForRealTime(epcie.gaInference, sensorReading);
			// 2.EUS dispatch
			eusDispatch_new(eusList, epcie.gaInference); 
			// 3.Optimization
			Optimizer op = new Optimizer(producer);
			// 4.Get appliance control decision list
			decisionList = op.getOptDecisionList(eusList, epcie.gaInference);
		}
		
		// Debug print out
		System.out.println("Origin eusList");
		for (AppNode app : eusList) {
			System.out.print(app.appName + ":" + app.envContext + ", ");
		}
		System.out.println();	
		System.out.println("eusList size = " + eusList.size());
	
		System.out.println("desicionList");
		for(AppNode app : decisionList){
			System.out.print(app.appName + ":" + app.envContext + ", ");
		}
		System.out.println();
		System.out.println("desicionList size = " + decisionList.size());
		
		// Control appliance or not
		if (!doControl) {
			System.out.println("=================================================");
			return;
		} 
		// If wake up then open all standby power
		else if(wakeUpFlag){
			wakeUpFlag = false;
			AllSleeping = false;
			if(!standbyAllOn){
				controlAgent.turnOnStandbyPower();
				standbyAllOn = true;
			}
			return;
		}  
		// If only one activity which is AllSleeping then close all of the standby power
		/*else if (epcie.gaInference.actInferResultSet.contains("AllSleeping") && epcie.gaInference.actInferResultSet.size() == 1) {
			ArrayList<String> exceptionID = new ArrayList<String>();
			// Not to turn off night light
			exceptionID.add("16");
			if (!standbyOff) {
				standbyOff = true;
				controlAgent.turnOffStandbyPower(exceptionID);
				json.reset();
				producer.sendOut(json.add("value", "ALL_OFF").toJson(), "ssh.COMMAND");
			}
			return;
		}*/
		// If there is no activity
		else if (epcie.gaInference.actInferResultSet.size() == 0) {
			return;
		}
		else {
			if(epcie.gaInference.actInferResultSet.contains("AllSleeping") && epcie.gaInference.actInferResultSet.size() == 1){
				AllSleeping = true;
				if (standbyAllOn == true){
				// Turn off all the appliances
				json.reset();
				producer.sendOut(json.add("value", "ALL_OFF").toJson(), "ssh.COMMAND");
				try
				   {
				   Thread.sleep( 1000 ); // 1000 milliseconds
				   }
				catch ( InterruptedException e )
				   {
					System.err.println( "awakened prematurely after ALL_OFF" );
				   }
				// Turn off all the standby power
				ArrayList<String> exceptionID = new ArrayList<String>();
				exceptionID.add("16");
				if (standbyAllOn) {
					controlAgent.turnOffStandbyPower(exceptionID);
					standbyAllOn = false;
				}
				// Turn off all lights
				json.reset();
				producer.sendOut(json.add("change", "Darken").add("value", "study-light_0").toJson(), "ssh.COMMAND");
				try
				   {
				   Thread.sleep( 1000 ); // 1000 milliseconds
				   }
				catch ( InterruptedException e )
				   {
					System.err.println( "awakened prematurely after study-light_0" );
				   }
				json.reset();
				producer.sendOut(json.add("change", "Darken").add("value", "livingroom-central-light_0").toJson(), "ssh.COMMAND");
				try
				   {
				   Thread.sleep( 1000 ); // 1000 milliseconds
				   }
				catch ( InterruptedException e )
				   {
					System.err.println( "awakened prematurely after livingroom-central-light_0" );
				   }
				json.reset();
				producer.sendOut(json.add("change", "Darken").add("value", "livingroom-ring-light_0").toJson(), "ssh.COMMAND");
				try
				   {
				   Thread.sleep( 1000 ); // 1000 milliseconds
				   }
				catch ( InterruptedException e )
				   {
					System.err.println( "awakened prematurely after livingroom-ring-light_0" );
				   }
				json.reset();
				producer.sendOut(json.add("change", "Darken").add("value", "bedroom-light_0").toJson(), "ssh.COMMAND");
				}
			}
			System.out.println("Control starts!");
			
			//json.reset();
			//producer.sendOut(json.add("TV_CONTROL_ON", Boolean.toString(TVControlOn)).toJson(), "ssh.CONTEXT");
			boolean controlExistence = controlAgent.controlAppliance(decisionList, eusList, TVControlOn);
			TVControlOn = controlAgent.returnTVControlOn();
			System.out.println("Control finishes!");
			// If controlExistence is true means control finish
			// Set signal to false and wait for signal from log engine
			if(controlExistence){
				signal = false;
			}
			// Even if control not finish or fail, we still need to send signal to web and log engine
			else if(activityChanged){
				//reject = false;
				//System.err.println("activity changed!");
				controlAgent.sendControlStartSignal();
				controlAgent.sendControlEndSignal();
			}
		}
		System.out.println("====================================================");
	}

	/* Call by default MQ processMsg(String m) in Mchess.java */
	public void processMQMessage(Epcie epcie, String message) {
		// Receive over 50000 message then restart producer
		reconnect_counter++;
		if(reconnect_counter > 50000) {
			producer.disconnect();
			while(!producer.connect());
			producer.getSendor();
			reconnect_counter = 0;
		}
		
		// Process received message : reject, recover, accept
		String subject = extractValue(message, "subject");
		String value;
		if(subject.equals("signal")){
			value = extractValue(message, "value");
			if(extractValue(message, "current_resend").equals("end")){
				backToLive = false;
			}
			
			if(value.equals("")){
				return;
			} 
			else if(value.equals("reject")){
				signal = true;
				reject = true;
			} 
			else if(value.equals("recover")){
				reject = true;
				json.reset();
				producer.sendOut(json.add("subject", "signal").add("value", "recover_ack").toJson(), "ssh.RAW_DATA");
			}
			else if (value.equals("accept")) {
				signal = true;
				reject = false;
			}
			else {
				return;
			}
			// For debugging
			System.err.println("reject = " + reject);
		}
		// Message from socketmeter
		else if(subject.equals("socketmeter")){
			String ampere = extractValue(message, "ampere");
			double ampere_value = Double.parseDouble(ampere);
			String type = subject;                   
			String id = extractValue(message, "id"); 
			SensorNode node = EnvStructure.applianceList.get(type + "_" + id);
			if (node != null) {
				// Get corresponding appliance name according to ezMeter id
				String applianceName = EnvStructure.applianceList.get(type + "_" + id).name;
				// Put newest ampere info into ezMete reading
				ezMeterAmpereReading.put(applianceName, ampere_value);
			}

			// If id = 20 (AC_bedroom), set temperature of bedroom 
			// else if id = 21 (AC_livingroom), set temperature of livingroom
			if(id.equals("20")){
				int temperature = Integer.parseInt(extractValue(message, "target_temperature"));
				Esdse.acTemperature_bedroom = temperature;
			}
			else if(id.equals("21")){
				int temperature = Integer.parseInt(extractValue(message, "target_temperature"));
				Esdse.acTemperature_livingroom = temperature;
			}

			// If all the ezmeter reading is updated then end the initialization 
			// Because zeMeter is the slowest sensor node during info retrieval
			if(initialization){
				// Dump those not ready ezMeters
				Set<String> keySet = ezMeterAmpereReading.keySet();
				for(String ezMeter : keySet){
					if(ezMeterAmpereReading.get(ezMeter).equals(-1.0)){
						System.err.print(ezMeter + ", ");
					}
				}
				System.out.println();
				if (!ezMeterAmpereReading.containsValue(-1.0)) {
					initialization = false;
					producer.sendOut(json.add("subject", "signal").add("initialization", "end").toJson(), "ssh.CONTEXT");
				}
			}
			return;
		}
		else if (subject.equals("people")) {
			SensorNode sensorNode = GaDbnClassifier.getSensorNode(message);
			if (sensorNode != null) {
				sensorReading.put(sensorNode.name, sensorNode.discreteValue);
			}
			return;
	    }
		else if (subject.equals("current")){
	    	SensorNode sensorNode = GaDbnClassifier.getSensorNode(message);
			if(sensorNode != null){
				sensorReading.put(sensorNode.name, sensorNode.discreteValue);
			}
		} 
		// For playKinet to accumulate calorie
		else if (subject.equals("kinect") && isPlayingKinect) {
			value = extractValue(message, "value");
			double calorie = Double.parseDouble(value);
			if (calorie < 0) calorie = 0;
			// Accumulate calorie in the window
			accumulatedCalorie -= calorieTimeWindow.get(0);
			calorieTimeWindow.remove(0);
			accumulatedCalorie += calorie;
			calorieTimeWindow.add(calorie);
			//if (accumulatedCalorie < 0) accumulatedCalorie = 0;
			// Increase the intensity of playingKinect in order to quickly turn on the waterColdFan
			EnvStructure.actAppList.get("livingroom_PlayingKinect").intensity = 60 + 0.17 * accumulatedCalorie;
			//if (accumulatedCalorie < 0) accumulatedCalorie = 0;
			//else if (accumulatedCalorie > 200) {
			//	EnvStructure.actAppList.get("livingroom_PlayingKinect").intensity = 100;
			//	reject = false;
			//} else {
			//	EnvStructure.actAppList.get("livingroom_PlayingKinect").intensity = 60;
			//}
			
			// For debugging 
			// Pinrt out the accumulatedCalorie 
			//System.err.println("accumulatedCalorie = " + accumulatedCalorie);
			return;
		}
		// If receive signal from web then start the next iteration's recognition
		if(signal){
			//Calendar cl = Calendar.getInstance();
			//while(time_substract(cl, Calendar.getInstance()) < 20);
			noSignalStartTime = null;
			processForRealTime(epcie, message);
		} 
		else{
			if(noSignalStartTime == null){
				noSignalStartTime = new Date();
			} 
			else {
				checkNoSignalDuration();
			}
		}
	}
	
	/* Send inferred activity to MQ */
	private void sendInferedActivityToMQ(Epcie epcie) {
		json.reset();
		if (epcie.gaInference.actInferResultSet.size() != 0) {
			String inferedActivity = "";
			String inferedGA = "";
			// Concat all inferred activities
	    	for (String activity : epcie.gaInference.actInferResultSet) {
	    		inferedActivity = inferedActivity.concat(activity + " ");
	    	}
	    	inferedActivity = inferedActivity.trim().replace(' ', '#');
	    	// Concat all inferred GAs
	    	for(String GA : epcie.gaInference.gaInferResultList){
	    		inferedGA = inferedGA.concat(GA + " ");
	    	}
	    	inferedGA = inferedGA.trim().replace(' ', '#');
	    	// Send message to MQ
	    	if (!inferedActivity.equals("")) {
	    		producer.sendOut(json.add("subject", "activity").add("name", inferedActivity).add("GA", inferedGA).toJson(), "ssh.CONTEXT");
	    	}
		} 
		else {
			producer.sendOut(json.add("subject", "activity").add("name", "NoActivity").add("GA", "NoGA").toJson(), "ssh.CONTEXT");
		}
	}
	
	/* Return priority according to the combination of status and preference type (i.e., explicit or implicit) 
	 * The higher priority the lower number
	 * */
	private int getPriority(AppNode app) {
		if (app.state.equals("on") && app.escType.equals("explicit")) {
			return 1;
		} else if (app.state.equals("on") && app.escType.equals("implicit")) {
			return 2;
		} else if (app.state.equals("standby") && app.escType.equals("explicit")) {
			return 3;
		} else if (app.state.equals("standby") && app.escType.equals("implicit")) {
			return 4;
		} else if (app.state.equals("off") && app.escType.equals("explicit")) {
			return 5;
		} else if (app.state.equals("off") && app.escType.equals("implicit")) {
			return 6;
		}
		return 7;
	}

	/* Remove some impossible activities from actInferResultSet */
	private void activityPostProcessing(Epcie epcie) {
		// WatchingTV and PlayingKinect, We want playingkinect
		if(epcie.gaInference.actInferResultSet.contains("WatchingTV") && epcie.gaInference.actInferResultSet.contains("PlayingKinect")){
			epcie.gaInference.actInferResultSet.remove("WatchingTV");
		}
		if(epcie.gaInference.actInferResultSet.contains("Reading") && !sensorReading.get("current_lamp_livingroom").startsWith("on")){
			epcie.gaInference.actInferResultSet.remove("Reading");
		}
		// PlayingKinect but XOBX is not on
		if(epcie.gaInference.actInferResultSet.contains("PlayingKinect") && (!sensorReading.get("current_xbox_livingroom").startsWith("on") || !sensorReading.get("current_TV_livingroom").startsWith("on"))){
			epcie.gaInference.actInferResultSet.remove("PlayingKinect");
		}
		// WatchingTV but TV is not on
		if(epcie.gaInference.actInferResultSet.contains("WatchingTV") && !sensorReading.get("current_TV_livingroom").startsWith("on")){
			epcie.gaInference.actInferResultSet.remove("WatchingTV");
		}
		// AllSleeping and Sleeping, We want AllSleeping
		if(epcie.gaInference.actInferResultSet.contains("AllSleeping") && epcie.gaInference.actInferResultSet.contains("Sleeping")){
			epcie.gaInference.actInferResultSet.remove("Sleeping");
		}
	}

	/* Rule-based check ComeBack and GoOut */
	/* Old Version
	private void checkComeBackAndGoOut(SensorNode sensorNode) {
		if(sensorNode.name.equals("switch_door_hallway") && sensorNode.discreteValue.equals("on")){
			// Door is opend by someone, HallwayActivity happens
			if (!doorOpen) doorOpen = true;
			json.reset();
			producer.sendOut(json.add("subject", "activity").add("name", "HallwayActivity").add("GA", "g1-6").toJson(), "ssh.CONTEXT");
			
			//controlAgent.sendControlStartSignal();
			
			// If door's open and illumination isn't enough then turn on hallway light
			// TODO: Do we really need to check doorOpen?
			//if((illuminationReading.get("hallway") < hallwayLightLuxTreshold) && doorOpen){
			if (doorOpen){ // for demo. If door's open, turn on hallway light
				json.reset();
				producer.sendOut(json.add("value", "DOOR-LIGHT_ON").toJson(), "ssh.COMMAND");
		    }
			
			// ComeBack and turn on standby power 
			// 1. Not going out 2. There's no one in the house 3. Standby power is all off
		    //if((ToBeGoOut == false) && (OldNumOfPeople == 0) && (standbyAllOn == false)){
			if(OldNumOfPeople == 0){ // for demo. If there was not people, but the door opens
		    	System.err.println("ToBeGoOut: " + ToBeGoOut);
		    	System.err.println("Come Back! Turn on all standy power!");
		    	controlAgent.turnOnStandbyPower();
		    	standbyAllOn = true; 
		    }
		    //controlAgent.sendControlEndSignal();
		} 
		else if(ToBeGoOut == true && sensorNode.name.equals("switch_door_hallway") && sensorNode.discreteValue.equals("off")){
			System.err.println("ToBeGoOut: " + ToBeGoOut);
	    	System.err.println("Go Out! Turn off all standy power!");
			if(doorOpen) doorOpen = false;
			
			// Send GoOut information to MQ
			json.reset();
			producer.sendOut(json.add("subject", "activity").add("name", "GoOut").add("GA", "g1-7").toJson(), "ssh.CONTEXT");
			
			// Turn off all the standby power
			controlAgent.turnOffStandbyPower(null);
			standbyAllOn = false;
			
			// Turn off all lights
			json.reset();
			producer.sendOut(json.add("value", "ALL_OFF").toJson(), "ssh.COMMAND");
			try
			   {
			   Thread.sleep( 1000 ); // 1000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after ALL_OFF" );
			   }
			json.reset();
			producer.sendOut(json.add("value", "study-light_0").toJson(), "ssh.COMMAND");
			try
			   {
			   Thread.sleep( 1000 ); // 1000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after study-light_0" );
			   }
			json.reset();
			producer.sendOut(json.add("value", "livingroom-central-light_0").toJson(), "ssh.COMMAND");
			try
			   {
			   Thread.sleep( 1000 ); // 1000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after livingroom-central-light_0" );
			   }
			json.reset();
			producer.sendOut(json.add("value", "livingroom-ring-light_0").toJson(), "ssh.COMMAND");
			try
			   {
			   Thread.sleep( 1000 ); // 1000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after livingroom-ring-light_0" );
			   }
			json.reset();
			// kitchen not working
			//json.reset();
			//producer.sendOut(json.add("value", "kitchen-light_0").toJson(), "ssh.COMMAND");
			//try{
			//   Thread.sleep( 1000 ); // 1000 milliseconds
			//   }
			//catch ( InterruptedException e ){
			//	System.err.println( "awakened prematurely after livingroom-ring-light_0" );
			//   }
			json.reset();
			producer.sendOut(json.add("value", "bedroom-light_0").toJson(), "ssh.COMMAND");
			ToBeGoOut = false;
			standbyAllOn = false;
		}
	}
	*/

	// New Version
	private void checkHallwayActivity(SensorNode sensorNode){
		if(sensorNode.name.equals("switch_door_hallway")){
			if ( sensorNode.discreteValue.equals("on") && doorLightOn == false){
				/*
				json.reset();
				producer.sendOut(json.add("value", "DOOR-LIGHT_ON").toJson(), "ssh.COMMAND");
				doorLightOn = true;*/
			
				try{
					Thread.sleep( 5000 ); // 5000 milliseconds
				}
				catch ( InterruptedException e ){
					System.err.println( "awakened prematurely before DOOR-LIGHT_ON" );
				}
				json.reset();
				producer.sendOut(json.add("value", "DOOR-LIGHT_ON").toJson(), "ssh.COMMAND");
			}
			else if (sensorNode.discreteValue.equals("off") && doorLightOn == true){
				try{
					Thread.sleep( 5000 ); // 5000 milliseconds
				}
				catch ( InterruptedException e ){
					System.err.println( "awakened prematurely before DOOR-LIGHT_OFF" );
				}
				json.reset();
				producer.sendOut(json.add("value", "DOOR-LIGHT_OFF").toJson(), "ssh.COMMAND");
				doorLightOn = false;
			}
		}
		
	}
	
	private void checkComeBackandGoOut(SensorNode sensorNode){
		if (NumOfPeople > 0 && standbyAllOn == false && AllSleeping == false){ // Come back
	    		System.err.println("Come Back! Turn on all standy power!");
	    		controlAgent.turnOnStandbyPower();
	    		standbyAllOn = true;
	    	try // Rest for ten seconds for the meters to be stable
			   {
			   Thread.sleep( 10000 ); // 10000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after all_open" );
			   }
	    	
		}
		else if (NumOfPeople == 0 && standbyAllOn == true){ // Go out
			// Turn off all the appliances
			json.reset();
			producer.sendOut(json.add("value", "ALL_OFF").toJson(), "ssh.COMMAND");
			try
			   {
			   Thread.sleep( 1000 ); // 1000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after ALL_OFF" );
			   }
			// Turn off all the standby power
			controlAgent.turnOffStandbyPower(null);
			standbyAllOn = false;
			// Turn off all lights
			json.reset();
			producer.sendOut(json.add("change", "Darken").add("value", "study-light_0").toJson(), "ssh.COMMAND");
			try
			   {
			   Thread.sleep( 1000 ); // 1000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after study-light_0" );
			   }
			json.reset();
			producer.sendOut(json.add("change", "Darken").add("value", "livingroom-central-light_0").toJson(), "ssh.COMMAND");
			try
			   {
			   Thread.sleep( 1000 ); // 1000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after livingroom-central-light_0" );
			   }
			json.reset();
			producer.sendOut(json.add("change", "Darken").add("value", "livingroom-ring-light_0").toJson(), "ssh.COMMAND");
			try
			   {
			   Thread.sleep( 1000 ); // 1000 milliseconds
			   }
			catch ( InterruptedException e )
			   {
				System.err.println( "awakened prematurely after livingroom-ring-light_0" );
			   }
			json.reset();
			producer.sendOut(json.add("change", "Darken").add("value", "bedroom-light_0").toJson(), "ssh.COMMAND");
		}
	}
	
	
	
	/* 
	 * Check the duration of not receving feedback signal from web interface 
	 * Duration > 45s, Directly set signal = true 
	 * Pretend like we received signal 
	 * */
	private void checkNoSignalDuration() {
		Date currentTime = new Date();
		double duration = (currentTime.getTime() - noSignalStartTime.getTime()) / 1000.0;
		if(duration > maxNoSignalDuration){
			signal = true;
			//noSignalStartTime = null;
			System.err.println("Long time no signal detected!");
		}
	}
	
	/* Extract value of key from message */
	private String extractValue(String message, String key) {
		return IntelM2M.mq.JsonBuilder.getValue(message, key);
	}
	
	/* Yi-Hsiu experiment usage */
	private void recordSensorDataVector(Epcie epcie) {
		Map<String, ArrayList<String>> sensorStatus = EnvStructure.sensorStatus;
		sensorDataVector = "";
		String[] sensorNameArray = (String[]) sensorStatus.keySet().toArray(new String[0]);
		for (String sensorName : sensorNameArray) {
			sensorDataVector = sensorDataVector.concat(sensorReading.get(sensorName) + " ");
		}
		sensorDataVector = sensorDataVector.concat("#");
		if (epcie.gaInference.actInferResultSet.size() != 0) {
			String inferedActivity = "";
	    	for (String activity : epcie.gaInference.actInferResultSet) {
	    		inferedActivity = inferedActivity.concat(activity + " ");
	    	}
	    	inferedActivity = inferedActivity.trim();
	    	sensorDataVector = sensorDataVector.concat(inferedActivity);
		} else {
			sensorDataVector = sensorDataVector.concat("NoActivity");
		}
		try {
			sensorDataVectorWriter.write(sensorDataVector + " @" + new Date() + "\r\n");
			sensorDataVectorWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
