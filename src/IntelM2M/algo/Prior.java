package IntelM2M.algo;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import IntelM2M.datastructure.EnvStructure;
import IntelM2M.epcie.GaGenerator;

public class Prior {

    static final double WatchingTVThreshold=0.1;
    static final double ComeBackThreshold=0.1;
    static final double ChattingThreshold=0.1;
    
	static  public ArrayList<String>  priorForInference( ArrayList<String> rawFromDBN , int humanNumber){		
			/*Prior Knowledge: 取機率前n高的活動*/
			while(rawFromDBN.size()>humanNumber){
				int lowestIndex=0;
				double preProb=1;
				for(int i=0; i<rawFromDBN.size();i++){
					String []str=rawFromDBN.get(i).split(" ");
					double prob= Double.parseDouble(str[1]);
					if(prob<preProb){
						lowestIndex=i;
						preProb=prob;
					}
				}
				rawFromDBN.remove(lowestIndex);
			}
				
			
			return rawFromDBN;
	}
	 
	static  public ArrayList<String>  priorForInferenceGA( ArrayList<String> rawFromDBN , int humanNumber,GaGenerator GA){		
			/*Prior Knowledge: 取機率前n高的活動*/
			while(rawFromDBN.size()>humanNumber){
				int lowestIndex=0;
				double preProb=1;
				for(int i=0; i<rawFromDBN.size();i++){
					String []str=rawFromDBN.get(i).split(" ");
					double prob= Double.parseDouble(str[1]);
					if(prob<preProb){
						lowestIndex=i;
						preProb=prob;
					}
				}
				rawFromDBN.remove(lowestIndex);
			}
			
			/*如果只infer 出 all Sleeping與其他活動 就改為  Sleeping*/	
//			Boolean haveAllSleep=false;
//			Boolean haveOther=false;
//			int allSleepIndex=0;
//			for(int i=0;i<rawFromDBN.size();i++){
//				String []str=rawFromDBN.get(i).split(" ");
//				ArrayList<String> memberList=GA.getGroupMember(str[0]);
//				for(String str2:memberList){
//					if(str2.equals("AllSleeping") ){
//						haveAllSleep=true;
//						allSleepIndex=i;
//					}
//
//				}		
//			}
//			if(rawFromDBN.size()>1){
//				haveOther=true;
//			}
//			if(haveAllSleep && haveOther){
//				rawFromDBN.remove(allSleepIndex);
//				
//				String str=GA.getGID("Sleeping").get(0);
//				rawFromDBN.add(str+" "+0.00);
//				
//			}
			
			return rawFromDBN;
	}
	
	static public ArrayList<String> priorForInferenceGA(ArrayList<String> rawFromDBN, int humanNumber, Map<String, String> sensorReading, ArrayList<GaGenerator> GaGeneratorList) {		
		/* If the GA contains activity where there are no people present in that location 
		 * then remove it. */
//		for(int i=0; i<rawFromDBN.size();i++){
//			String []str=rawFromDBN.get(i).split(" ");
//			String GA = str[0];
//			ArrayList<String> gaActivityList = new ArrayList<String>();
//			for(GaGenerator gaGenerator: GaGeneratorList){
//				if (gaGenerator.gaList.containsKey(GA) ){
//					ArrayList <String> actMemberList=gaGenerator.gaList.get(GA).actMemberList;
//					for(String str2:actMemberList){
//						gaActivityList.add(str2);
//					}
//				}
//			}
//			String location = EnvStructure.actRoomList.get(gaActivityList.get(0));
//			if (sensorReading.get("camera_" + location).equals("off")) {
//				rawFromDBN.remove(i);
//				i--;
//			}
//		}
		
		Map<String, Integer> GANumber = new LinkedHashMap<String, Integer>();
		for (String room : EnvStructure.roomList) {
			GANumber.put(room, 0);
		}
		for(int i=0; i<rawFromDBN.size();i++){
			String []str=rawFromDBN.get(i).split(" ");
			String GA = str[0];
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
			GANumber.put(GAlocation, GANumber.get(GAlocation) + 1);
		}
		for (String location : EnvStructure.roomList) {
			int cameraReading = (sensorReading.get("people_" + location).equals("off")) ? 0 : Integer.parseInt(sensorReading.get("people_" + location).split("_")[1]);
			while(GANumber.get(location) > cameraReading){
				int lowestIndex=0;
				double preProb=1;
				for(int i=0; i<rawFromDBN.size();i++){
					String []str=rawFromDBN.get(i).split(" ");
					String GA = str[0];
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
					double prob= Double.parseDouble(str[1]);
					if(prob<preProb){
						lowestIndex=i;
						preProb=prob;
					}
				}
				rawFromDBN.remove(lowestIndex);
				GANumber.put(location, GANumber.get(location) - 1);
			}
		}
		
		/*Prior Knowledge: 取機率前n高的活動*/
//		while(rawFromDBN.size()>humanNumber){
//			int lowestIndex=0;
//			double preProb=1;
//			for(int i=0; i<rawFromDBN.size();i++){
//				String []str=rawFromDBN.get(i).split(" ");
//				double prob= Double.parseDouble(str[1]);
//				/* If the GA contains activity where there are no people present in that location 
//				 * then remove it. */
////				String GA = str[0];
////				ArrayList<String> gaActivityList = new ArrayList<String>();
////				for(GaGenerator gaGenerator: GaGeneratorList){
////					if (gaGenerator.gaList.containsKey(GA) ){
////						ArrayList <String> actMemberList=gaGenerator.gaList.get(GA).actMemberList;
////						for(String str2:actMemberList){
////							gaActivityList.add(str2);
////						}
////					}
////				}
////				String location = EnvStructure.actRoomList.get(gaActivityList.get(0));
////				if (sensorReading.get("camera_" + location).equals("off")) {
////					prob = 0;
////				}
//				if(prob<preProb){
//					lowestIndex=i;
//					preProb=prob;
//				}
//			}
//			rawFromDBN.remove(lowestIndex);
//		}
		
		/*如果只infer 出 all Sleeping與其他活動 就改為  Sleeping*/	
//		Boolean haveAllSleep=false;
//		Boolean haveOther=false;
//		int allSleepIndex=0;
//		for(int i=0;i<rawFromDBN.size();i++){
//			String []str=rawFromDBN.get(i).split(" ");
//			ArrayList<String> memberList=GA.getGroupMember(str[0]);
//			for(String str2:memberList){
//				if(str2.equals("AllSleeping") ){
//					haveAllSleep=true;
//					allSleepIndex=i;
//				}
//
//			}		
//		}
//		if(rawFromDBN.size()>1){
//			haveOther=true;
//		}
//		if(haveAllSleep && haveOther){
//			rawFromDBN.remove(allSleepIndex);
//			
//			String str=GA.getGID("Sleeping").get(0);
//			rawFromDBN.add(str+" "+0.00);
//			
//		}
		
		return rawFromDBN;		
   }
	
	    public ArrayList<String>  priorForInference2( ArrayList<String> rawFromDBN, int humanNumber){
			
			for(int i=0;i < rawFromDBN.size();i++){
				String []str=rawFromDBN.get(i).split(" ");
				/*Prior Knowledge: 去掉不會同時發生的活動 ex: GoOut*/
				if(str[0].equals("GoOut") && rawFromDBN.size()>1){
					rawFromDBN.remove(i);
				}
				/*Prior Knowledge: 去掉threshold過低的活動*/
				double prob= Double.parseDouble(str[1]);
				if(str[0].equals("WatchingTV") && prob<WatchingTVThreshold){
					rawFromDBN.remove(i);
				}
				else if(str[0].equals("ComeBack") && prob<ComeBackThreshold){
					rawFromDBN.remove(i);
				}
				else if(str[0].equals("Chatting") && prob<ChattingThreshold){
					rawFromDBN.remove(i);
				}
				
			}		
			/*play Kinect watchTV 取一個*/
			if(rawFromDBN.size()>=2){
				boolean watchTV=false;
				boolean playKinect=false;
				int watchTVIndex=0;
				for(int i=0;i<rawFromDBN.size();i++){
					String []str=rawFromDBN.get(i).split(" ");
					if(str[0].equals("WatchingTV")){
						watchTV=true;
						watchTVIndex=i;
					}
					if(str[0].equals("PlayingKinect")){
						playKinect=true;
					}
				}
				if(watchTV && playKinect){
					rawFromDBN.remove(watchTVIndex);
				}
			}
			/*如果只infer 出 sleeping 或 all Sleeping 就取為 all Sleeping*/
//			Boolean haveSleep=false;
//			Boolean haveOther=false;
//			int sleepIndex=0;
//			for(int i=0;i<rawFromDBN.size();i++){
//				String []str=rawFromDBN.get(i).split(" ");
//				if(str[0].equals("Sleeping") ){
//					haveSleep=true;
//					sleepIndex=i;
//				}
//				else if(str[0].equals("AllSleeping")){
//					haveSleep=true;
//					
//				}
//				else{
//					haveOther=true;
//				}			
//			}
//			if(haveSleep && !haveOther){
//				rawFromDBN.remove(sleepIndex);
//				
//			}

			/*Prior Knowledge: 取機率前n高的活動*/
			while(rawFromDBN.size()>humanNumber){
				int lowestIndex=0;
				double preProb=1;
				for(int i=0; i<rawFromDBN.size();i++){
					String []str=rawFromDBN.get(i).split(" ");
					double prob= Double.parseDouble(str[1]);
					if(prob<preProb){
						lowestIndex=i;
						preProb=prob;
					}
				}
				rawFromDBN.remove(lowestIndex);
			}
				
			
			return rawFromDBN;
	    }
	    
	    static public void priorForTrainingData(String rawData,String activity,FileWriter writer){
	    	
			try {
				String[] split = rawData.split("#");
				String[] split2 = split[1].split(" ");
				Boolean thisAct = false;
				for (String str : split2) {
					if (str.equals(activity))
						thisAct = true;
				}
				
				if (thisAct == true) {
					writer.write(split[0] + activity + "\r\n");
				}
//				else if (activity.equals("WatchingTV")&& split[1].contains("PlayingKinect")) {
//					/*Do Nothing*/
//					return;
//				} 
				else {
					writer.write(split[0] + "OtherActivity\r\n");
				}
				
			} catch (Exception e) {
				// TODO: handle exception
			}
	    }
	    
	    static public void priorForTrainingData(String rawData,String activity,FileWriter writer,GaGenerator GA){
	    	
			try {
				String[] split = rawData.split("#");
				String[] split2 = split[1].split(" ");
				Boolean thisAct = false;
				for (String str : split2) {
					if (str.equals(activity))
						thisAct = true;
				}
				if (thisAct == true) {
					writer.write(split[0] + activity + "\r\n");
					
					if(GA.gaList.get(activity).actMemberList.contains("WatchingTV")){
						for(int i=0;i<7;i++){
							writer.write(split[0] + activity + "\r\n");
						}
					}
					else	if(GA.gaList.get(activity).actMemberList.contains("GoOut")){
						for(int i=0;i<8;i++){
							writer.write(split[0] + activity + "\r\n");
						}
					}
					else	if(GA.gaList.get(activity).actMemberList.contains("Sleeping")){
						for(int i=0;i<3;i++){
							writer.write(split[0] + activity + "\r\n");
						}
					} 
					
					return;
				}
//				else if (GA.gaList.get(activity).actMemberList.contains("WatchingTV")) {
//					for(String str:split2){
//						if(GA.gaList.get(str).actMemberList.contains("PlayingKinect")){
//							/*Do Nothing*/
//							return;
//						}
//					}
//				} 
				
				writer.write(split[0] + "OtherActivity\r\n");
				
			} catch (Exception e) {
				// TODO: handle exception
			}
	    }
}
