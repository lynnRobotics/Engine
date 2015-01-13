package IntelM2M.exp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.RelationTable;
import IntelM2M.epcie.Epcie;
import IntelM2M.esdse.Optimizer;

public class LocationBasedSaving {
	
	private ArrayList<AppNode> buildEnvList(String read){
		
		ArrayList<AppNode> envList= new ArrayList<AppNode>();
		
		/*build envList*/
		Map<String,AppNode> appList=EnvStructure.appList;
		for(String str:appList.keySet()){
			AppNode app= appList.get(str);
			AppNode app2=app.copyAppNode(app);
			envList.add(app2);
		}
		
		
		//update envList from sensorReading
		Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
		String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
		String[] split = read.split("#");
		String []sensorContext=split[0].split(" ");
		
		
		for(AppNode eus:envList){
			for(int i=0;i<sensorName.length;i++){
				if(eus.appName.equals(sensorName[i])){
					eus.envContext=sensorContext[i];
				}
			}
		}
		
		return envList;
	}
	
	private ArrayList<AppNode> buildDecisionList(String read,ArrayList<AppNode> envList){
		/*build decisionList*/
		ArrayList<AppNode> decisionList= new ArrayList<AppNode>();
		for(AppNode app:envList){
			AppNode app2=app.copyAppNode(app);
			decisionList.add(app2);
		}
		
		/*get  location have people or activity 方法一*/
//		String[] split = read.split("#");
//		String []actTruth=split[1].split(" ");
//
//		String [] locationActList=(String[])EnvStructure.actAppList.keySet().toArray(new String[0]);
//		ArrayList <String>locationList= new ArrayList<String>();
//		for(String str:actTruth){
//			for(String str2:locationActList){
//				String[]split2=str2.split("_");
//				if(str.equals(split2[1])){
//					locationList.add(split2[0]);
//				}
//			}
//		}
		/*方法二*/
		String[] split = read.split("#");
		String []split2=split[0].split(" ");
		ArrayList <String>locationList= new ArrayList<String>();
		if(split2[36].equals("on")){
			locationList.add("hallway");
		}
		if(split2[37].equals("on")){
			locationList.add("livingroom");
		}
		if(split2[38].equals("on")){
			locationList.add("bedroom");
		}
		if(split2[39].equals("on")){
			locationList.add("kitchen");
		}
		if(split2[40].equals("on")){
			locationList.add("hallway");
		}
		
		
			
		/*turn off appliance in location array*/

		
		//Map<String, RelationTable> actAppList=EnvStructure.actAppList;
		String [] appList=(String[])EnvStructure.appList.keySet().toArray(new String[0]);
		

		for(int i=0;i<appList.length;i++){
			String str=appList[i];
			Boolean contain=false;
			for(String str2:locationList){
				if(str.contains(str2)){
					contain=true;
				}
			}
			if(contain==false ){			
				
				for(AppNode app:decisionList){
					if(app.appName.equals(str) && !app.appName.equals("current_AC_livingroom")){
						if(app.envContext.contains("on")){
							app.envContext="off";
							app.haveAPControlFromOn=true;
						}else if(app.envContext.contains("standby")){
							app.envContext="off";
						}						
					}
				}
			}
		}
		
		
		
		return decisionList;
	}

	public void processForSimulator(Epcie epcie,String read,String read2){
		ArrayList<AppNode> envList= buildEnvList(read);
		ArrayList<AppNode> decisionList=buildDecisionList(read,envList);
		

		double rawAmp= Optimizer.calEnergyConsumptionForSimulator(envList);
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(decisionList);
		
		
		ExpRecorder.exp.processEXPForLocationBased(read, read2, epcie, decisionList,envList);
		
		
	}
	
	
}
