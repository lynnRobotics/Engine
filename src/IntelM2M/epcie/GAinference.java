package IntelM2M.epcie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import s2h.platform.node.PlatformMessage;
import s2h.platform.node.Sendable;
import s2h.platform.support.JsonBuilder;
import s2h.platform.support.MessageUtils;

import IntelM2M.algo.Prior;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.SensorNode;
import IntelM2M.epcie.classifier.GaDbnClassifier;
import IntelM2M.epcie.erc.GaEscGenerator;

public class GAinference {


	private String read;
	public int humanNumber = 1; //todo: should provided by camera
	
	public ArrayList<GaGenerator> GaGeneratorList;
	ArrayList<GaDbnClassifier> GaDbnList;
	public ArrayList<GaEscGenerator> GaEscList;
	/*infer result*/
	public ArrayList<String> gaInferResultList;
	public Set<String> actInferResultSet;
	public ArrayList<String> rawGAinferResultList;
	public Set<String> rawActInferResultSet;
	
	private void buildActInferResultSet(){
		
		actInferResultSet= new HashSet<String>();
		for(String str:gaInferResultList){
			for(GaGenerator gaGenerator: GaGeneratorList){
				if (gaGenerator.gaList.containsKey(str) ){
					ArrayList <String> actMemberList=gaGenerator.gaList.get(str).actMemberList;
					for(String str2:actMemberList){
						actInferResultSet.add(str2);
					}
				}
			}
		}
	}
	
	private void buildActInferResultSet(Map<String, String> sensorReading) {
		ArrayList<String> toBeRemovedGAList = new ArrayList<String>();
		actInferResultSet= new HashSet<String>();
		for(String str:gaInferResultList){
			for(GaGenerator gaGenerator: GaGeneratorList){
				if (gaGenerator.gaList.containsKey(str) ){
					ArrayList <String> actMemberList=gaGenerator.gaList.get(str).actMemberList;
					for(String str2:actMemberList){
						String location = EnvStructure.actRoomList.get(str2);
						if (sensorReading.get("camera_" + location).equals("off")) {
							toBeRemovedGAList.add(str);
							break;
						}
						actInferResultSet.add(str2);
					}
				}
			}
		}
		for (String GA : toBeRemovedGAList) {
			gaInferResultList.remove(GA);
		}
	}
	
	private void buildRawActInferResultSet(){
		rawActInferResultSet= new HashSet<String>();
		for(String str:rawGAinferResultList){
			for(GaGenerator gaGenerator: GaGeneratorList){
				if (gaGenerator.gaList.containsKey(str) ){
					ArrayList <String> actMemberList=gaGenerator.gaList.get(str).actMemberList;
					for(String str2:actMemberList){
						rawActInferResultSet.add(str2);
					}
				}
			}
		}
	}

	public GAinference(ArrayList<GaGenerator> in1,ArrayList<GaDbnClassifier> in2,ArrayList<GaEscGenerator> in3,String in4){
		GaGeneratorList=in1;
		GaDbnList=in2;
		GaEscList=in3;
		read=in4;
	}
	
	public GAinference(ArrayList<GaGenerator> in1,ArrayList<GaDbnClassifier> in2,ArrayList<GaEscGenerator> in3){
		GaGeneratorList=in1;
		GaDbnList=in2;
		GaEscList=in3;
	}
	
	public GAinference(ArrayList<GaGenerator> in1,ArrayList<GaDbnClassifier> in2,ArrayList<GaEscGenerator> in3, int humanNumber){
		GaGeneratorList=in1;
		GaDbnList=in2;
		GaEscList=in3;
		this.humanNumber = humanNumber;
	}
	
	public void buildInferResult(){
		ArrayList<Map <String,Boolean>> GAinferResultList= new ArrayList<Map <String,Boolean> >();
		Boolean continueFlag=true;
		Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
		
		
		/*k層有k個gaList*/
		ArrayList<ArrayList<String>> kGaList=new ArrayList<ArrayList<String>>();
		for(int k=0;k<GaGeneratorList.size();k++){
			/*build gaList*/
			Set<String> gSet=GaGeneratorList.get(k).gaList.keySet();
	  		ArrayList<String> gaList= new ArrayList<String>();
	   		for(String str:gSet){
	   			gaList.add(str);
	   		}
	   		kGaList.add(gaList);	   
		}
		
		
		for(int k=0;k<GaGeneratorList.size()&&continueFlag;k++){
			ArrayList<String> gaList=kGaList.get(k);
			GaGenerator GA=GaGeneratorList.get(k);
			GaDbnClassifier GaDBN=GaDbnList.get(k);
			
			String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
			/*initial */
			Map <String,Boolean> GAinferResult=new LinkedHashMap<String,Boolean>();
			Map <String,Boolean> groundTruth=new LinkedHashMap<String,Boolean>();
			Map <String,Boolean> GAgroundTruth=new LinkedHashMap<String,Boolean>();
			ArrayList<String> activityList=EnvStructure.activityList;
			for(int i=0;i<activityList.size();i++){

				groundTruth.put(activityList.get(i), false);
			}
			for(int i=0;i<gaList.size();i++){
				GAinferResult.put(gaList.get(i), false);
				GAgroundTruth.put(gaList.get(i), false);
			}
			//		
			String[] split = read.split("#");
			String []sensorContext=split[0].split(" ");
			ArrayList<String> rawFromDBN = new ArrayList<String>();
			ArrayList<String> inferDBN = new ArrayList<String>();
			Map<String,String> probDBN = new LinkedHashMap<String,String>();
			/*infer GA*/
			for(int i=0 ;i<sensorContext.length;i++){
				SensorNode s= new SensorNode(sensorName[i],sensorContext[i]);
				rawFromDBN = GaDBN.GaDBNInference(GA,s.name, s.discreteValue);
			}
			Map <String,String> allGaProb=GaDBN.allGaProb;

			int humanNumber= split[1].split(" ").length;
			if(rawFromDBN.size()!=0){
				/*prior Knowledge 處理*/
				rawFromDBN= Prior.priorForInferenceGA(rawFromDBN,humanNumber,GA);
						
			}
			for(String str:rawFromDBN){
				String []splitActPb=str.split(" ");
				inferDBN.add(splitActPb[0]);
				probDBN.put(splitActPb[0], splitActPb[1]);
			}
			//
		
			/*record result*/
			for(String str:inferDBN){
				GAinferResult.put(str, true);
			}
			//

			/*record ground truth*/
			String []truth=split[1].split(" ");
			/*去掉NO*/
			ArrayList<String>tmpArr= new ArrayList<String>();
			for(String str:truth){
				if(!str.equals("NO")){
					tmpArr.add(str);
				}
			}
			truth=(String[])tmpArr.toArray(new String[0]);
										

			for(String str:truth){
				groundTruth.put(str, true);
			}
			//
			
			/*record ground truth for GA*/			
			for(String str:truth){
				//String gid=GA.getGID(str);
				ArrayList<String> gidArr=GA.getGID(str);
				for(String GID:gidArr){
					GAgroundTruth.put(GID, true);
				}								
			}							

			GAinferResultList.add(GAinferResult);
		}
		/*for experiment we record gaInferResult*/
		rawGAinferResultList= new ArrayList<String>();
		for(Map <String,Boolean> gaMap:GAinferResultList){
			Set<String> keySet= gaMap.keySet();
			for(String str:keySet){
				if(gaMap.get(str)){
					rawGAinferResultList.add(str);
				}
			}
		}
		buildRawActInferResultSet();
		
		
		/*GA selection*/
		ArrayList<String> selectedGA=GAselection(GAinferResultList);
		this.gaInferResultList=selectedGA;
		buildActInferResultSet();
	
		

	}
	
	public void buildInferResultForRealTime(String message, Sendable sender){
		ArrayList<Map <String,Boolean>> GAinferResultList= new ArrayList<Map <String,Boolean> >();
		Boolean continueFlag=true;
		Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
		
		
		/*k層有k個gaList*/
		ArrayList<ArrayList<String>> kGaList=new ArrayList<ArrayList<String>>();
		for(int k=0;k<GaGeneratorList.size();k++){
			/*build gaList*/
			Set<String> gSet=GaGeneratorList.get(k).gaList.keySet();
	  		ArrayList<String> gaList= new ArrayList<String>();
	   		for(String str:gSet){
	   			gaList.add(str);
	   		}
	   		kGaList.add(gaList);	   
		}
		
		
		for(int k=0;k<GaGeneratorList.size()&&continueFlag;k++){
			ArrayList<String> gaList=kGaList.get(k);
			GaGenerator GA=GaGeneratorList.get(k);
			GaDbnClassifier GaDBN=GaDbnList.get(k);
			
			String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
			/*initial */
			Map <String,Boolean> GAinferResult=new LinkedHashMap<String,Boolean>();
			Map <String,Boolean> groundTruth=new LinkedHashMap<String,Boolean>();
			Map <String,Boolean> GAgroundTruth=new LinkedHashMap<String,Boolean>();
			ArrayList<String> activityList=EnvStructure.activityList;
			for(int i=0;i<activityList.size();i++){

				groundTruth.put(activityList.get(i), false);
			}
			for(int i=0;i<gaList.size();i++){
				GAinferResult.put(gaList.get(i), false);
				GAgroundTruth.put(gaList.get(i), false);
			}
			//
			//String[] split = read.split("#");
			//String []sensorContext=split[0].split(" ");
			
			JsonBuilder json = MessageUtils.jsonBuilder();
			SensorNode s = GaDBN.getSensorAttribute(message);
			
			ArrayList<String> rawFromDBN = new ArrayList<String>();
			ArrayList<String> inferDBN = new ArrayList<String>();
			Map<String,String> probDBN = new LinkedHashMap<String,String>();
			/*infer GA*/
			rawFromDBN = GaDBN.GaDBNInference(GA,s.name, s.discreteValue);
//			for(int i=0 ;i<sensorContext.length;i++){
//				SensorNode s= new SensorNode(sensorName[i],sensorContext[i]);
//				rawFromDBN = GaDBN.GaDBNInference(GA,s.name, s.discreteValue);
//			}
			Map <String,String> allGaProb=GaDBN.allGaProb;

			//int humanNumber= split[1].split(" ").length;
			if(rawFromDBN.size()!=0){
				/*prior Knowledge 處理*/
				rawFromDBN= Prior.priorForInferenceGA(rawFromDBN,humanNumber,GA);
						
			}
			for(String str:rawFromDBN){
				String []splitActPb=str.split(" ");
				inferDBN.add(splitActPb[0]);
				probDBN.put(splitActPb[0], splitActPb[1]);
			}
			//
		
			/*record result*/
			for(String str:inferDBN){
				GAinferResult.put(str, true);
			}
			//

			/*record ground truth*/
//			String []truth=split[1].split(" ");
//			/*去掉NO*/
//			ArrayList<String>tmpArr= new ArrayList<String>();
//			for(String str:truth){
//				if(!str.equals("NO")){
//					tmpArr.add(str);
//				}
//			}
//			truth=(String[])tmpArr.toArray(new String[0]);
//										
//
//			for(String str:truth){
//				groundTruth.put(str, true);
//			}
//			//
//			
//			/*record ground truth for GA*/			
//			for(String str:truth){
//				//String gid=GA.getGID(str);
//				ArrayList<String> gidArr=GA.getGID(str);
//				for(String GID:gidArr){
//					GAgroundTruth.put(GID, true);
//				}								
//			}							

			GAinferResultList.add(GAinferResult);
		}
		/*for experiment we record gaInferResult*/
		rawGAinferResultList= new ArrayList<String>();
		for(Map <String,Boolean> gaMap:GAinferResultList){
			Set<String> keySet= gaMap.keySet();
			for(String str:keySet){
				if(gaMap.get(str)){
					rawGAinferResultList.add(str);
				}
			}
		}
		buildRawActInferResultSet();
		
		
		/*GA selection*/
		ArrayList<String> selectedGA=GAselection(GAinferResultList);
		for(String GA : selectedGA) {
			System.out.println(GA);
		}
		this.gaInferResultList=selectedGA;
		buildActInferResultSet();
	
		

	}
	
	public void buildInferResultForRealTime_New(Map<String, String> sensorReading){
		ArrayList<Map <String,Boolean>> GAinferResultList= new ArrayList<Map <String,Boolean> >();
		Boolean continueFlag=true;
		Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
		
		
		/*k層有k個gaList*/
		ArrayList<ArrayList<String>> kGaList=new ArrayList<ArrayList<String>>();
		for(int k=0;k<GaGeneratorList.size();k++){
			/*build gaList*/
			Set<String> gSet=GaGeneratorList.get(k).gaList.keySet();
	  		ArrayList<String> gaList= new ArrayList<String>();
	   		for(String str:gSet){
	   			gaList.add(str);
	   		}
	   		kGaList.add(gaList);	   
		}
		
		
		for(int k=0;k<GaGeneratorList.size()&&continueFlag;k++){
			ArrayList<String> gaList=kGaList.get(k);
			GaGenerator GA=GaGeneratorList.get(k);
			GaDbnClassifier GaDBN=GaDbnList.get(k);
			
			String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
			/*initial */
			Map <String,Boolean> GAinferResult=new LinkedHashMap<String,Boolean>();
			Map <String,Boolean> groundTruth=new LinkedHashMap<String,Boolean>();
			Map <String,Boolean> GAgroundTruth=new LinkedHashMap<String,Boolean>();
			ArrayList<String> activityList=EnvStructure.activityList;
			for(int i=0;i<activityList.size();i++){

				groundTruth.put(activityList.get(i), false);
			}
			for(int i=0;i<gaList.size();i++){
				GAinferResult.put(gaList.get(i), false);
				GAgroundTruth.put(gaList.get(i), false);
			}
			//
			//String[] split = read.split("#");
			//String []sensorContext=split[0].split(" ");
			
			ArrayList<String> rawFromDBN = new ArrayList<String>();
			ArrayList<String> inferDBN = new ArrayList<String>();
			Map<String,String> probDBN = new LinkedHashMap<String,String>();
			/*infer GA*/
			for(int i=0 ;i<sensorReading.size();i++){
				SensorNode s= new SensorNode(sensorName[i],sensorReading.get(sensorName[i]));
				rawFromDBN = GaDBN.GaDBNInference(GA,s.name, s.discreteValue.split("_")[0]);
			}
			Map <String,String> allGaProb=GaDBN.allGaProb;

			//int humanNumber= split[1].split(" ").length;
			if(rawFromDBN.size()!=0){
				/*prior Knowledge 處理*/
				//rawFromDBN= Prior.priorForInferenceGA(rawFromDBN,humanNumber,GA);
				rawFromDBN = Prior.priorForInferenceGA(rawFromDBN, humanNumber, sensorReading, GaGeneratorList);
			}
			
			for(String str:rawFromDBN){
				String []splitActPb=str.split(" ");
				inferDBN.add(splitActPb[0]);
				probDBN.put(splitActPb[0], splitActPb[1]);
			}
			
			System.out.println(allGaProb);
			
			/*record result*/
			for(String str:inferDBN){
				GAinferResult.put(str, true);
			}
			//

			/*record ground truth*/
//			String []truth=split[1].split(" ");
//			/*去掉NO*/
//			ArrayList<String>tmpArr= new ArrayList<String>();
//			for(String str:truth){
//				if(!str.equals("NO")){
//					tmpArr.add(str);
//				}
//			}
//			truth=(String[])tmpArr.toArray(new String[0]);
//										
//
//			for(String str:truth){
//				groundTruth.put(str, true);
//			}
//			//
//			
//			/*record ground truth for GA*/			
//			for(String str:truth){
//				//String gid=GA.getGID(str);
//				ArrayList<String> gidArr=GA.getGID(str);
//				for(String GID:gidArr){
//					GAgroundTruth.put(GID, true);
//				}								
//			}							

			GAinferResultList.add(GAinferResult);
		}
		/*for experiment we record gaInferResult*/
		rawGAinferResultList= new ArrayList<String>();
		for(Map <String,Boolean> gaMap:GAinferResultList){
			Set<String> keySet= gaMap.keySet();
			for(String str:keySet){
				if(gaMap.get(str)){
					rawGAinferResultList.add(str);
				}
			}
		}
		buildRawActInferResultSet();
		
		
		/*GA selection*/
		//ArrayList<String> selectedGA=GAselection(GAinferResultList);
		ArrayList<String> selectedGA=GAselection(GAinferResultList, sensorReading);
		// TODO System.out
		for(String GA : selectedGA) {
			System.out.println(GA);
		}
		this.gaInferResultList=selectedGA;
		buildActInferResultSet();
		//buildActInferResultSet(sensorReading);
	}
	
	private ArrayList<String> GAselection(ArrayList<Map <String,Boolean>> rawInferData){	
		for(int k=0;k<GaGeneratorList.size();k++){
			Map <String,Boolean> GAinferResult=rawInferData.get(k);
			if(GAinferResult.size()!=0){
				Set <String> gaSet=GAinferResult.keySet();
				for(String str:gaSet){
					if(GAinferResult.get(str)){
						/*砍掉有包含這個GA成員 且階層比k大的其他GA*/
						GaGenerator GA=GaGeneratorList.get(k);
						ArrayList<String> memberList=GA.getGroupMember(str);
						/*往後面階層檢查，有包含成員的GA infer result就刪除*/
						for(int j=k+1;j<GaGeneratorList.size();j++){
							Map <String,Boolean> GAinferResultHigherLevel=rawInferData.get(j);
							Set <String> highLevelGaSet=GAinferResultHigherLevel.keySet();
							for(String str2:highLevelGaSet){
								GaGenerator higherGA=GaGeneratorList.get(j);
								ArrayList<String> higherMemberList=higherGA.getGroupMember(str2);
								Boolean repeat=checkMemberRepeat(memberList,higherMemberList);
								if(repeat){
									GAinferResultHigherLevel.remove(str2);
									break;
								}
							}
						}
					}
				}
			}
		}
		
		ArrayList<String> selectedGA=getGAname(rawInferData);
		//String []split1=read.split("#");
		//int humanNumber =split1[1].split(" ").length;
		while(selectedGA.size()>humanNumber){
			selectedGA.remove(selectedGA.size()-1);
		}
		//this.selectedGA=selectedGA;
	
		return selectedGA;
	}
	
	private ArrayList<String> GAselection(ArrayList<Map <String,Boolean>> rawInferData, Map<String, String> sensorReading){	
		for(int k=0;k<GaGeneratorList.size();k++){
			Map <String,Boolean> GAinferResult=rawInferData.get(k);
			if(GAinferResult.size()!=0){
				Set <String> gaSet=GAinferResult.keySet();
				for(String str:gaSet){
					if(GAinferResult.get(str)){
						/*砍掉有包含這個GA成員 且階層比k大的其他GA*/
						GaGenerator GA=GaGeneratorList.get(k);
						ArrayList<String> memberList=GA.getGroupMember(str);
						/*往後面階層檢查，有包含成員的GA infer result就刪除*/
						for(int j=k+1;j<GaGeneratorList.size();j++){
							Map <String,Boolean> GAinferResultHigherLevel=rawInferData.get(j);
							Set <String> highLevelGaSet=GAinferResultHigherLevel.keySet();
							for(String str2:highLevelGaSet){
								GaGenerator higherGA=GaGeneratorList.get(j);
								ArrayList<String> higherMemberList=higherGA.getGroupMember(str2);
								Boolean repeat=checkMemberRepeat(memberList,higherMemberList);
								if(repeat){
									GAinferResultHigherLevel.remove(str2);
									break;
								}
							}
						}
					}
				}
			}
		}
		/* Using human number in each location to select GA */
		ArrayList<String> selectedGA=getGAname(rawInferData);
		ArrayList<String> selectedGAbyHumanNumber = new ArrayList<String>();
		Map<String, Integer> GANumber = new LinkedHashMap<String, Integer>();
		for (String room : EnvStructure.roomList) {
			GANumber.put(room, 0);
		}
		//String []split1=read.split("#");
		//int humanNumber =split1[1].split(" ").length;
		for (String location : EnvStructure.roomList) {
			int cameraReading = (sensorReading.get("people_" + location).equals("off")) ? 0 : Integer.parseInt(sensorReading.get("people_" + location).split("_")[1]);
			for (String GA : selectedGA) {
				ArrayList<String> gaActivityList = new ArrayList<String>();
				for(GaGenerator gaGenerator: GaGeneratorList){
					if (gaGenerator.gaList.containsKey(GA) ){
						ArrayList <String> actMemberList=gaGenerator.gaList.get(GA).actMemberList;
						for(String str2:actMemberList){
							gaActivityList.add(str2);
						}
					}
				}
				String GAlocation = EnvStructure.actRoomList.get(gaActivityList.get(0));
				if (!GAlocation.equals(location)) continue;
				if (GANumber.get(location) < cameraReading) {
					selectedGAbyHumanNumber.add(GA);
					GANumber.put(GAlocation, GANumber.get(GAlocation) + 1);
				}
			}
		}
//		while(selectedGA.size()>humanNumber){
//			selectedGA.remove(selectedGA.size()-1);
//		}
//		//this.selectedGA=selectedGA;
//	
//		return selectedGA;
		return selectedGAbyHumanNumber;
	}

	private ArrayList<String> getGAname(ArrayList<Map <String,Boolean>> rawInferData){
		ArrayList<String> gaName=new ArrayList<String>();
		for(Map<String,Boolean> GAresult: rawInferData ){
			for(String str:GAresult.keySet()){
				if(GAresult.get(str)==true){
					gaName.add(str);
				}
			}
		}
		
		return gaName;
		
	}
	
	 private Boolean checkMemberRepeat(ArrayList<String> memberList,ArrayList<String> higherMemberList){
			/*檢查member是否有重複*/
			for(String str3:higherMemberList){
				for(String str4:memberList){
					if(str3.equals(str4)){
						return true;
					}
				}
			}
			return false;
	  }
}
