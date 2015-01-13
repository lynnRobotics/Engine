package IntelM2M.exp;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import IntelM2M.agent.thermal.ThermalAgent;
import IntelM2M.agent.visual.VisualAgent;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.epcie.Epcie;
import IntelM2M.epcie.GAinference;
import IntelM2M.esdse.Optimizer;
import IntelM2M.test.SimulatorTest;

public class ExpRecorder {
	
	ExpRecorder(){
		
		for(String str:EnvStructure.activityList){
			RecRate r= new RecRate();
			actExpResult.put(str, r);
		}
	}
	
	static public ExpRecorder exp= new ExpRecorder();



	/*raw noise consumption*/
	 double noiseConsumption=0;

	/*raw total consumption*/
	double rawTotalConsumption=0;
	/*after saving total consumption*/
	double mchessTotalConsumption=0;

	/*thermal  energy saving*/
	double thermalAgentSave=0;  
	double thermalTypeSave=0;
	/*visual total energy saving*/
	double visualAgentSave=0;
	double visualTypeSave=0;
	/*ap total energy saving*/
	double apAgentSave=0;
	double apTypeSave=0;
	/*total save*/
	double totalSave=0;  //ok
	/*ap  close wrong time*/
	double apWrongCount=0;   //ok
	/*ap close wrong consumption*/
	double apAgentWrongSave=0;  //ok
	/*ap close right consumption*/
	double apAgentRightSaveFromOn=0;  
	double apAgentSaveFromStandby=0;
	double apAgentSaveFromOn=0;
	
	double apTypeWrongSave=0;
	double apTypeRightSaveFromOn=0;
	double apTypeSaveFromStandby=0;
	double apTypeSaveFromOn=0;
	
	/*service time*/
	/*to be finish*/
	double serviceTimeWithGA=0;
	double serviceTimeWithOutGA=0;
	double totalTime=0;
	
	/*location based*/
	//Boolean locationBasedTest=false;
	double locationBasedTotalConsumption=0;
	double locationBasedRightSaveFromOn=0;
	double locationBasedWrongCount=0;
	double locationBasedTotalSave=0;
	double locationBasedWrongSave=0;
	/*for debug*/
	double totalConsumption2=0;
	
	ArrayList<AppNode> standbyList= new ArrayList<AppNode>();

	
	
	/*raw comfort result*/

	
	ArrayList<VisualComfort> rawVisualComfortList=new ArrayList<VisualComfort>();
	ArrayList<VisualComfort> visualComfortList= new ArrayList<VisualComfort>();
	ArrayList<ThermalComfort> rawThermalComfortList= new ArrayList<ThermalComfort>();
	ArrayList<ThermalComfort> thermalComfortList= new ArrayList<ThermalComfort>();
	
	double rawAvgVC=0;
	double rawAvgVC_count=0;
	
	double AvgVC=0;
	double AvgVC_count=0;
	
	double rawAvgTC=0;
	double rawAvgTC_count=0;
	
	double AvgTC=0;
	double AvgTC_count=0;
	
	/*precision and recall evaluate*/
	
	Map<String,RecRate> actExpResult= new LinkedHashMap<String,RecRate>();
	
	/*for debug*/
	

	
	
	
	/*thermal conflict time*/
	double thermalConflictCount=0;  //ok
	/*visual conflict time*/
	double visualConflictCount=0;  //ok
	/*activity truth*/
	ArrayList<Activity> actTruth= new ArrayList<Activity>();
	/*activity infer*/
	ArrayList<Activity> actInfer= new ArrayList<Activity>();
	/*ga infer*/
	ArrayList<Activity> gaInfer= new ArrayList<Activity>();
	/*raw  infer*/
	ArrayList<Activity> rawGaInfer= new ArrayList<Activity>();
	ArrayList<Activity> rawActInfer= new ArrayList<Activity>();
	
	/*行數*/
	ArrayList<Integer> rowNum=new ArrayList<Integer>();
	
	/*control record*/
	ArrayList<ApRecord> apRecord= new ArrayList<ApRecord>();
	
	ArrayList<Integer> durationList= new ArrayList<Integer>();
	

	
	class ThermalComfort{
		ArrayList<Double> pmv=new ArrayList<Double>();
		ArrayList<AppNode> thermalApp= new ArrayList<AppNode>(); // appliance,state
	}
	class VisualComfort{
		
		ArrayList<Double> ill=new ArrayList<Double>();
		ArrayList<AppNode> visualApp= new ArrayList<AppNode>(); // appliance,state
	}
	
	class Activity{
		ArrayList<String> activityList= new ArrayList<String>();
	}
	
	class ApRecord{
		ArrayList<String> truthList= new ArrayList<String>();
		ArrayList<String> controlFromONsList= new ArrayList<String>();
	}
	
//	class DayRecord{
//		/*For 耗電量比較 by day*/
//		ArrayList <Double> rawConsumption= new ArrayList<Double>(); //每一個存每一天的耗電量
//		ArrayList <Double> mchessConsumption = new ArrayList<Double>();
//		ArrayList <Double> locationBasedConsumption= new ArrayList<Double>();
//	}
	
//	private void setUpDayRecord(){
//		/*設定每一天在側資中到第幾行*/
//		dayFlag.add(288);
//		dayFlag.add(576);
//		dayFlag.add(864);
//		dayFlag.add(1152);
//		dayFlag.add(1452);
//		dayFlag.add(1745);
//		dayFlag.add(2036);
//		
//	}
	
	private void setRowNum(int i){
		rowNum.add(i);
	}
	
	private void setActTruth(String read){
		String[] split = read.split("#");
		Activity act = new Activity();
		String[] split2= split[1].split(" ");
		for(String str:split2){
			act.activityList.add(str);
		}
		actTruth.add(act);
	}
	
	private void setActInfer(Set<String> singleAct){
		Activity act= new Activity();
		for(String str:singleAct){
			act.activityList.add(str);
		}
		actInfer.add(act);
	}
	
	private void setGaInfer(ArrayList <String> singleAct){
		Activity act= new Activity();
		for(String str:singleAct){
			act.activityList.add(str);
		}
		gaInfer.add(act);
		

	}
	
	private void setRawInfer(ArrayList<String> rawGAinferResultList,Set<String> rawActInferResultSet){
		Activity act= new Activity();
		for(String str:rawGAinferResultList){
			act.activityList.add(str);
		}
		rawGaInfer.add(act);
		
		Activity act2= new Activity();
		for(String str:rawActInferResultSet){
			act2.activityList.add(str);
		}
		rawActInfer.add(act2);
		
	}
	
	private void setCousumption(ArrayList<AppNode> eusList,ArrayList<AppNode>  decisionList){
		
		
		
		double rawAmp= Optimizer.calEnergyConsumptionForSimulator(eusList);
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(decisionList);
		
		rawTotalConsumption+= rawAmp;
		mchessTotalConsumption+=newAmp;
		totalSave+= (rawAmp-newAmp);
		
		/*exp for day*/
		ExpRecorderByDay.expByDay.setConsumption(rawAmp,newAmp);
	}
	

	
	private void setThermalTypeSave(ArrayList<AppNode> eusList,ArrayList<AppNode>  decisionList){
		
		ArrayList<AppNode> thermalRawList= new ArrayList<AppNode> ();
		for(AppNode app:eusList){

			if(app.comfortType.equals("thermal")){
				thermalRawList.add(app);
			}
		}
		ArrayList<AppNode> thermalAppList= new ArrayList<AppNode>();
		for(AppNode app:decisionList){

			if(app.comfortType.equals("thermal")){
				thermalAppList.add(app);
			}
		}
		
		double rawAmp= Optimizer.calEnergyConsumptionForSimulator(thermalRawList);
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(thermalAppList);
		
		thermalTypeSave+=(rawAmp-newAmp);
		ExpRecorderByDay.expByDay.setTypeSave((rawAmp-newAmp), "thermal");
	}
	
	private void setThermalAgentSave(ArrayList<AppNode> eusList,ArrayList<AppNode>  decisionList){
		
		ArrayList<AppNode> thermalRawList= new ArrayList<AppNode> ();
		for(AppNode app:eusList){
			if(app.agentName.equals("thermal")){
			
				thermalRawList.add(app);
			}
		}
		ArrayList<AppNode> thermalAppList= new ArrayList<AppNode>();
		for(AppNode app:decisionList){
			if(app.agentName.equals("thermal")){
		
				thermalAppList.add(app);
			}
		}
		
		double rawAmp= Optimizer.calEnergyConsumptionForSimulator(thermalRawList);
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(thermalAppList);
		
		thermalAgentSave+=(rawAmp-newAmp);
		
		
	}

	private void setVisualTypeSave(ArrayList<AppNode> eusList,ArrayList<AppNode>  decisionList){
		
		ArrayList<AppNode> visualRawList= new ArrayList<AppNode> ();
		for(AppNode app:eusList){
			
			if(app.comfortType.equals("visual")){
				visualRawList.add(app);
			}
		}
		ArrayList<AppNode> visualAppList= new ArrayList<AppNode>();
		for(AppNode app:decisionList){
			
			if(app.comfortType.equals("visual")){
				visualAppList.add(app);
			}
		}
		
		double rawAmp= Optimizer.calEnergyConsumptionForSimulator(visualRawList);
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(visualAppList);
		
		visualTypeSave+=(rawAmp-newAmp);
		ExpRecorderByDay.expByDay.setTypeSave((rawAmp-newAmp), "visual");
	}
	
	private void setVisualAgentSave(ArrayList<AppNode> eusList,ArrayList<AppNode>  decisionList){
		
		ArrayList<AppNode> visualRawList= new ArrayList<AppNode> ();
		for(AppNode app:eusList){
			if(app.agentName.equals("visual")){
			//if(app.comfortType.equals("visual")){
				visualRawList.add(app);
			}
		}
		ArrayList<AppNode> visualAppList= new ArrayList<AppNode>();
		for(AppNode app:decisionList){
			if(app.agentName.equals("visual")){
			//if(app.comfortType.equals("visual")){
				visualAppList.add(app);
			}
		}
		
		double rawAmp= Optimizer.calEnergyConsumptionForSimulator(visualRawList);
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(visualAppList);
		
		visualAgentSave+=(rawAmp-newAmp);
	}
	
	private void setAPTypeSave(ArrayList<AppNode> eusList,ArrayList<AppNode>  decisionList){
		
		ArrayList<AppNode> APRawList= new ArrayList<AppNode> ();
		for(AppNode app:eusList){

			if(app.comfortType.equals("thermal") || app.comfortType.equals("visual")){
				
			}else{
				APRawList.add(app);
			}
		}
		ArrayList<AppNode>APAppList= new ArrayList<AppNode>();
		for(AppNode app:decisionList){

			if(app.comfortType.equals("thermal") || app.comfortType.equals("visual")){
				
			}else{
				APAppList.add(app);
			}
		}
		
		double rawAmp= Optimizer.calEnergyConsumptionForSimulator(APRawList);
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(APAppList);
		
		apTypeSave+=(rawAmp-newAmp);
		ExpRecorderByDay.expByDay.setTypeSave((rawAmp-newAmp), "ap");
		/*for debug*/
		if(eusList.size()!=decisionList.size()){

		}else{
			/*set APsave form standby*/
			APRawList.clear();
			APAppList.clear();
			for(int i=0;i<eusList.size();i++){
				AppNode app1=eusList.get(i);
				for(int j=0;j<decisionList.size();j++){
					AppNode app2=decisionList.get(j);
					if(app1.comfortType.equals("thermal") || app1.comfortType.equals("visual")){
						
					}
					else if(app1.appName.equals(app2.appName)){
						if(app1.envContext.equals("standby") && app1.agentName.equals("ap")){
							if(app2.envContext.equals("off") && app2.agentName.equals("ap")){
								APRawList.add(app1);
								APAppList.add(app2);
								break;
							}
						}
					}
				}
			}
			rawAmp=Optimizer.calEnergyConsumptionForSimulator(APRawList);
			newAmp=Optimizer.calEnergyConsumptionForSimulator(APAppList);
			apTypeSaveFromStandby+=(rawAmp-newAmp);
			
			/*set APsave from on*/
			APRawList.clear();
			APAppList.clear();
			for(int i=0;i<eusList.size();i++){
				AppNode app1=eusList.get(i);
				for(int j=0;j<decisionList.size();j++){
					AppNode app2=decisionList.get(j);
					if(app1.comfortType.equals("thermal") || app1.comfortType.equals("visual")){
						
					}
					else if(app1.appName.equals(app2.appName)){
						if(app1.envContext.contains("on") && app1.agentName.equals("ap")){
							if(app2.envContext.equals("off") && app2.agentName.equals("ap")){
								APRawList.add(app1);
								APAppList.add(app2);
								break;
							}
						}
					}
				}
			}
			

			
			rawAmp=Optimizer.calEnergyConsumptionForSimulator(APRawList);
			newAmp=Optimizer.calEnergyConsumptionForSimulator(APAppList);
			apTypeSaveFromOn+=(rawAmp-newAmp);
		}
		

		
	}

	
	private void setAPAgentSave(ArrayList<AppNode> eusList,ArrayList<AppNode>  decisionList){
		
		ArrayList<AppNode> APRawList= new ArrayList<AppNode> ();
		for(AppNode app:eusList){
			if(app.agentName.equals("ap")){

				APRawList.add(app);
			}

		}
		ArrayList<AppNode>APAppList= new ArrayList<AppNode>();
		for(AppNode app:decisionList){
			if(app.agentName.equals("ap")){

				APAppList.add(app);
			}

		}
		
		double rawAmp= Optimizer.calEnergyConsumptionForSimulator(APRawList);
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(APAppList);
		
		apAgentSave+=(rawAmp-newAmp);
		/*for debug*/
		if(eusList.size()!=decisionList.size()){
			int aa=0;
			aa++;
		}else{
			/*set APsave form standby*/
			APRawList.clear();
			APAppList.clear();
			for(int i=0;i<eusList.size();i++){
				AppNode app1=eusList.get(i);
				for(int j=0;j<decisionList.size();j++){
					AppNode app2=decisionList.get(j);
					if(app1.appName.equals(app2.appName)){
						if(app1.envContext.equals("standby") && app1.agentName.equals("ap")){
							if(app2.envContext.equals("off") && app2.agentName.equals("ap")){
								APRawList.add(app1);
								APAppList.add(app2);
								break;
							}
						}
					}
				}
			}
			/*for debug*/
			for(AppNode app:APRawList){
				standbyList.add(app);
			}
			
			
			rawAmp=Optimizer.calEnergyConsumptionForSimulator(APRawList);
			newAmp=Optimizer.calEnergyConsumptionForSimulator(APAppList);
			apAgentSaveFromStandby+=(rawAmp-newAmp);
			
			/*set APsave from on*/
			APRawList.clear();
			APAppList.clear();
			for(int i=0;i<eusList.size();i++){
				AppNode app1=eusList.get(i);
				for(int j=0;j<decisionList.size();j++){
					AppNode app2=decisionList.get(j);
					if(app1.appName.equals(app2.appName)){
						if(app1.envContext.contains("on") && app1.agentName.equals("ap")){
							if(app2.envContext.equals("off") && app2.agentName.equals("ap")){
								APRawList.add(app1);
								APAppList.add(app2);
								break;
							}
						}
					}
				}
			}
			

			
			rawAmp=Optimizer.calEnergyConsumptionForSimulator(APRawList);
			newAmp=Optimizer.calEnergyConsumptionForSimulator(APAppList);
			apAgentSaveFromOn+=(rawAmp-newAmp);
		}
		

		
	}
	
	private void setLocationBased(ArrayList<AppNode> decisionList,ArrayList<AppNode> envList,String read2){
		double wrongSave=0;
		if(!read2.equals("")){
			String split[]=read2.split(" ");
			
			for(AppNode app:decisionList){
				/*取得原本環境的context*/
				String context=null;
				for(AppNode app2:envList){
					if(app.appName.equals(app2.appName)){
						context=app2.envContext;
					}
				}
				
				if(app.haveAPControlFromOn){
					Boolean wrongFlag=true;
					for(String str:split){
						if(app.appName.equals(str)){
							/*取得on的消耗，這邊有可能錯*/

							ArrayList<AppNode> appList = new ArrayList<AppNode>();
							AppNode app2=app.copyAppNode(app);
							app2.envContext=context;
							appList.add(app2);
							
							double amp=Optimizer.calEnergyConsumptionForSimulator(appList);
							locationBasedRightSaveFromOn+=amp;
							ExpRecorderByDay.expByDay.setNoiseSave(amp, "locationBased");

							
							wrongFlag=false;
							break;
						}

					}
					if(wrongFlag){

						/*關錯了*/
						locationBasedWrongCount+=1;
						ExpRecorderByDay.expByDay.setWrongControl(1, "locationBased");

						/*取得on的消耗，這邊有可能錯*/
						ArrayList<AppNode> appList = new ArrayList<AppNode>();
						AppNode app2=app.copyAppNode(app);
						app2.envContext=context;
						appList.add(app2);
						double amp=Optimizer.calEnergyConsumptionForSimulator(appList);
						locationBasedWrongSave+=amp;
						wrongSave=amp;
					
					}
				}
			}
			

		}
		/*add total consumption*/
		double rawAmp=Optimizer.calEnergyConsumptionForSimulator(envList);
		
		double newAmp=Optimizer.calEnergyConsumptionForSimulator(decisionList)+wrongSave;
		
		totalConsumption2+=rawAmp;
		locationBasedTotalConsumption+=newAmp;
		/*加上關錯的那些電器*/
		locationBasedTotalSave+=(rawAmp-newAmp);
		
		ExpRecorderByDay.expByDay.setConsumptionForLocationBased(newAmp);
		
	}
	
	private void setApAgentWrongData(ArrayList<AppNode> decisionList,ArrayList<AppNode> eusList,String read2){
		if(!read2.equals("")){
			String split[]=read2.split(" ");
			
			/*record result*/
			ApRecord apR= new ApRecord();
			for(String str:split){
				apR.truthList.add(str);
			}
			for(AppNode app:decisionList){
				if(app.haveAPControlFromOn){
					apR.controlFromONsList.add(app.appName);
				}
			}
			apRecord.add(apR);
			
			
			
			
			for(AppNode app:decisionList){
				/*取得原本環境的context*/
				String context=null;
				for(AppNode app2:eusList){
					if(app.appName.equals(app2.appName)){
						context=app2.envContext;
					}
				}
				
				if(app.haveAPControlFromOn){
					Boolean wrongFlag=true;
					for(String str:split){
						if(app.appName.equals(str)){
							/*取得on的消耗，這邊有可能錯*/

							ArrayList<AppNode> appList = new ArrayList<AppNode>();
							AppNode app2=app.copyAppNode(app);
							app2.envContext=context;
							appList.add(app2);
							double amp=Optimizer.calEnergyConsumptionForSimulator(appList);
							
							//double amp=app.ampere.get(context);
							apAgentRightSaveFromOn+=amp;
							
							ExpRecorderByDay.expByDay.setNoiseSave(amp, "mchess");
							
							wrongFlag=false;
							break;
						}

					}
					if(wrongFlag){

	
						/*關錯了*/
						
						apWrongCount+=1;
						ExpRecorderByDay.expByDay.setWrongControl(1, "mchess");
						/*取得on的消耗，這邊有可能錯*/
						ArrayList<AppNode> appList = new ArrayList<AppNode>();
						AppNode app2=app.copyAppNode(app);
						app2.envContext=context;
						appList.add(app2);
						double amp=Optimizer.calEnergyConsumptionForSimulator(appList);
						
						//double amp=app.ampere.get(context);
						apAgentWrongSave+=amp;
						

					}
				}
			}
			

		}else{
			/*record result*/
			ApRecord apR= new ApRecord();

			for(AppNode app:decisionList){
				if(app.haveAPControlFromOn){
					apR.controlFromONsList.add(app.appName);
				}
			}
			apRecord.add(apR);
		}

		
	}
	
	private void setApTypeWrongData(ArrayList<AppNode> decisionList,ArrayList<AppNode> eusList,String read2){
		if(!read2.equals("")){
			String split[]=read2.split(" ");
			
//			/*record result*/
//			ApRecord apR= new ApRecord();
//			for(String str:split){
//				apR.truthList.add(str);
//			}
//			for(AppNode app:decisionList){
//				if(app.comfortType.equals("thermal") || app.comfortType.equals("visual")){
//					
//				}
//				else if(app.haveAPControlFromOn){
//					apR.controlFromONsList.add(app.appName);
//				}
//			}
//			apRecord.add(apR);
			
			
			
			
			for(AppNode app:decisionList){
				/*取得原本環境的context*/
				String context=null;
				for(AppNode app2:eusList){
					if(app.appName.equals(app2.appName)){
						context=app2.envContext;
					}
				}
				if(app.comfortType.equals("thermal") || app.comfortType.equals("visual")){
				
				}
				else if(app.haveAPControlFromOn){
					Boolean wrongFlag=true;
					for(String str:split){
						if(app.appName.equals(str)){
							/*取得on的消耗，這邊有可能錯*/
							ArrayList<AppNode> appList = new ArrayList<AppNode>();
							AppNode app2=app.copyAppNode(app);
							app2.envContext=context;
							appList.add(app2);
							double amp=Optimizer.calEnergyConsumptionForSimulator(appList);
							
							//double amp=app.ampere.get(context);
							apTypeRightSaveFromOn+=amp;
							wrongFlag=false;
							break;
						}

					}
					if(wrongFlag){

						
						//apWrongCount+=1;
						/*取得on的消耗，這邊有可能錯*/
						ArrayList<AppNode> appList = new ArrayList<AppNode>();
						AppNode app2=app.copyAppNode(app);
						app2.envContext=context;
						appList.add(app2);
						double amp=Optimizer.calEnergyConsumptionForSimulator(appList);
						
						//double amp=app.ampere.get(context);
						apTypeWrongSave+=amp;
						

					}
				}
			}
			

		}

		
	}
	
	private void setNoiseConsumption(String read2,ArrayList<AppNode> eusList){
		/*for debug*/
		
		
		if(!read2.equals("")){
			ArrayList<AppNode> noiseList= new ArrayList<AppNode> ();
			String split[]= read2.split(" ");
			for(String str:split){
				for(AppNode app:eusList){
					if(app.appName.equals(str)){
						noiseList.add(app);
						
						break;
					}
				}
			}
	
			
			double noiseAmp= Optimizer.calEnergyConsumptionForSimulator(noiseList);
			
			noiseConsumption+= noiseAmp;
			
			ExpRecorderByDay.expByDay.setNoiseSave(noiseAmp, "noise");

		}

		
	}
	

	
	public void setThermalConflictCount(){
		thermalConflictCount++;
	}
	
	public void setVisualConflictCount(){
		visualConflictCount++;
	}
	

	
	
	public void setThermalComfort_new(ArrayList<AppNode> eusList,ArrayList<AppNode> decisionList,GAinference gaInference){
		ArrayList<AppNode> thermalAppList= new ArrayList<AppNode>();
		ArrayList<AppNode> thermalRawList= new ArrayList<AppNode> ();
		for(AppNode app:eusList){
			if(app.agentName.equals("thermal")){
				thermalRawList.add(app);
					}
		}
		for(AppNode app:decisionList){
			if(app.agentName.equals("thermal")){
				thermalAppList.add(app);
					}
		}
		ThermalAgent ta= new ThermalAgent();
		
		ArrayList<Double> rawPmvList= ta.getComfortArray(thermalRawList, gaInference);
		ArrayList<Double> PmvList= ta.getComfortArray(thermalAppList, gaInference);
		
		ThermalComfort rawTc = new ThermalComfort();
		rawTc.pmv=rawPmvList;
		rawTc.thermalApp= thermalRawList;
		rawThermalComfortList.add(rawTc);
		
		ThermalComfort tc = new ThermalComfort();
		tc.pmv=PmvList;
		tc.thermalApp= thermalAppList;
		thermalComfortList.add(tc);
		
		
		/*raw_thermal avg record*/
		final int maxLevel=3;
		for(Double comfort:rawTc.pmv){
			double diff=Math.abs(comfort);
			if(diff>maxLevel){
				diff=maxLevel;
			}
			rawAvgTC+=diff;
		}
		/*count*/
		rawAvgTC_count+=rawTc.pmv.size();
		
		/*thermal avg record*/

		for(Double comfort:tc.pmv){
			double diff=Math.abs(comfort);
			if(diff>maxLevel){
				diff=maxLevel;
			}
			AvgTC+=diff;
		}
		/*count*/
		AvgTC_count+=tc.pmv.size();
		
		
		
		
		ExpRecorderByDay.expByDay.setComfort(rawTc.pmv, "raw_thermal");
		ExpRecorderByDay.expByDay.setComfort(tc.pmv, "thermal");
		
	}
	
	public void setVisualComfort_new(ArrayList<AppNode> eusList,ArrayList<AppNode> decisionList,GAinference gaInference){
		ArrayList<AppNode> visualAppList= new ArrayList<AppNode>();
		ArrayList<AppNode> visualRawList= new ArrayList<AppNode> ();
		for(AppNode app:eusList){
			if(app.agentName.equals("visual")){
				visualRawList.add(app);
					}
		}
		for(AppNode app:decisionList){
			if(app.agentName.equals("visual")){
				visualAppList.add(app);
					}
		}
		VisualAgent va= new VisualAgent();
		
		ArrayList<Double> rawIllList= va.getComfortArray(visualRawList, gaInference);
		ArrayList<Double> IllList= va.getComfortArray(visualAppList, gaInference);
		
		VisualComfort rawVc = new VisualComfort();
		rawVc.ill=rawIllList;
		rawVc.visualApp= visualRawList;
		rawVisualComfortList.add(rawVc);
		
		VisualComfort vc = new VisualComfort();
		vc.ill=IllList;
		vc.visualApp= visualAppList;
		visualComfortList.add(vc);
		
		/*raw_visual avg record*/
		final int maxLevel=3;
		for(Double comfort:rawVc.ill){
			double diff=Math.abs(comfort);
			if(diff>maxLevel){
				diff=maxLevel;
			}
			rawAvgVC+=diff;
		}
		/*count*/
		rawAvgVC_count+=rawVc.ill.size();
				
		/* visual avg record*/
		for(Double comfort:vc.ill){
			double diff=Math.abs(comfort);
			if(diff>maxLevel){
				diff=maxLevel;
			}
			AvgVC+=diff;
		}
		/*count*/
		AvgVC_count+=rawVc.ill.size();
		
		ExpRecorderByDay.expByDay.setComfort(rawVc.ill, "raw_visual");
		ExpRecorderByDay.expByDay.setComfort(vc.ill, "visual");
		
	}
	
	private Boolean checkListSame(ArrayList<String> inferList,ArrayList<String> truthList){
		if(inferList.size()!=truthList.size()){
			return false;
		}else {
			for(String str:inferList){
				Boolean flag=false;
				for(String str2:truthList){
					if(str.equals(str2)){
						flag=true;
					}
				}
				if(flag==false){
					return false;
				}
			}
			
		}
		return true;
	}
	
	private void calServingTime(GAinference gaInference){
		ArrayList<String> gaAllInfer=rawGaInfer.get(rawGaInfer.size()-1).activityList;
		ArrayList<String> actAllInfer=rawActInfer.get(rawActInfer.size()-1).activityList;
		ArrayList<String> truth=actTruth.get(actTruth.size()-1).activityList;
		ArrayList<String> actInferFromL1=new ArrayList<String>();
		
		int duration=ExpRecorder.exp.getDuration();
		
		/*get act in layer1*/
		for(String str:gaAllInfer){
			String []split=str.split("-");
			if(split[0].equals("g1")){
				String singleAct=gaInference.GaGeneratorList.get(0).getGroupMember(str).get(0);
				actInferFromL1.add(singleAct);
			}
		}
		
		/*check serving time without GA*/
		if(actInferFromL1.size()>=truth.size()){
			Boolean allSame=true;
			for(String str:truth){
				Boolean same=false;
				for(String str2:actInferFromL1){
					if(str.equals(str2)){
						same=true;
						break;
					}
				}
				if(same==false){
					/**/
					allSame=false;
				}
			}
			if(allSame==true){
				serviceTimeWithOutGA+= (1*duration*5);
				ExpRecorderByDay.expByDay.setServingTime(1*duration*5, "withoutGA");
			}
		}
		
		/*check serving time with GA*/
		if(actAllInfer.size()>=truth.size()){
			Boolean allSame=true;
			for(String str:truth){
				Boolean same=false;
				for(String str2:actAllInfer){
					if(str.equals(str2)){
						same=true;
						break;
					}
				}
				if(same==false){
					allSame=false;
				}
			}
			if(allSame==true){
				serviceTimeWithGA+= (1*duration*5);
				ExpRecorderByDay.expByDay.setServingTime(1*duration*5, "withGA");
			}
		}
		
		/*total time*/
		totalTime+=(1*duration*5);
		
		
		
		
	}
	
	public void writeDebugInformation(){
		Map<String,AppNode> appList=EnvStructure.appList;

	
		
		try{
			FileWriter writer = new FileWriter(new File("./_output_results/debug.txt"),false);
			for(AppNode app:standbyList){
			
				AppNode rawApp=appList.get(app.appName);
				Double amp=rawApp.ampere.get(app.envContext);
				writer.write(app.appName+" "+amp+"\r\n"); 
				writer.flush();
				
			}
			writer.close();
		}catch(Exception ex){
			
		}
	}
	
	public void setRecRate(Set <String> inferAct,String read){
		Set<String> key=actExpResult.keySet();
		String []split= read.split("#");
		String []actTruth=split[1].split(" ");
		
		for(String str:key){
			RecRate r= actExpResult.get(str);
			Boolean truthContain=false;
			for(String str2:actTruth){
				if(str2.equals(str)){
					truthContain=true;
					break;
				}
			}
			
			Boolean inferContain=false;
			for(String str2:inferAct){
				if(str2.equals(str)){
					inferContain=true;
					break;
				}
			}
			
			if(truthContain==true && inferContain==true){
				r.tp+=1;
			}else if(truthContain==false && inferContain==true){
				r.fp+=1;
			}else if(truthContain==false && inferContain==false){
				r.tn+=1;
			}else if(truthContain==true && inferContain==false ){
				r.fn+=1;
			}
		}
	}
	

	
	public void writeOutARResult(){
		try{
			FileWriter writer = new FileWriter(new File("./_output_results/arResult.txt"),false);
		
			
	
			
//			for(int i=0;i<actTruth.size();i++){
//				
//				
//				writer.write("row Num: "+ rowNum.get(i)+"\r\n");
//				
//				
//				writer.write("raw thermal APP:");
//				for(AppNode app: rawThermalComfortList.get(i).thermalApp){
//					writer.write(app.appName+" :"+app.envContext+", ");
//				}
//				double c3=Optimizer.calEnergyConsumptionForSimulator(rawThermalComfortList.get(i).thermalApp);
//				writer.write("comsuption: "+c3);
//				writer.write("\r\n");
//				
//				
//				writer.write("thermal APP:");
//				for(AppNode app: thermalComfortList.get(i).thermalApp){
//					writer.write(app.appName+" :"+app.envContext+", ");
//				}
//				double c4=Optimizer.calEnergyConsumptionForSimulator(thermalComfortList.get(i).thermalApp);
//				writer.write("comsuption: "+c4);
//				writer.write("\r\n");
//				
//				writer.write("thermal save: "+(c3-c4)+"\r\n");
//				
//				
//				writer.write("raw thermal comfort: ");
//				for(Double pmv: rawThermalComfortList.get(i).pmv){
//					writer.write(pmv+", ");
//				}
//				writer.write("  |  ");
//				writer.write("thermal comfort: ");
//				for(Double pmv: thermalComfortList.get(i).pmv){
//					writer.write(pmv+", ");
//				}
//				writer.write("\r\n\r\n");
//				/*for debug*/
//				
//				/*visual result*/
//				writer.write("raw visual APP:");
//				for(AppNode app: rawVisualComfortList.get(i).visualApp){
//					writer.write(app.appName+" :"+app.envContext+", ");
//				}
//				double c1=Optimizer.calEnergyConsumptionForSimulator(rawVisualComfortList.get(i).visualApp);
//				writer.write("comsuption: "+c1);
//				writer.write("\r\n");
//				
//				writer.write("visual APP: ");
//				for(AppNode app: visualComfortList.get(i).visualApp){
//					writer.write(app.appName+" :"+app.envContext+", ");
//				}
//				double c2=Optimizer.calEnergyConsumptionForSimulator(visualComfortList.get(i).visualApp);
//				writer.write("comsuption: "+c2);
//				writer.write("\r\n");
//				
//				writer.write("visual save: "+(c1-c2)+"\r\n");
//				
//				writer.write("raw visual comfort: ");
//				for(Double ill: rawVisualComfortList.get(i).ill){
//					writer.write(ill+", ");
//				}
//				writer.write("  |  ");
//				writer.write("visual comfort: ");
//				for(Double ill: visualComfortList.get(i).ill){
//					writer.write(ill+", ");
//				}
//				writer.write("\r\n\r\n");
//				
//				
//				
//				
//				writer.write("raw GA infer:");
//				for(String str:rawGaInfer.get(i).activityList){
//					writer.write(str+",");
//				}
//				writer.write("\r\n");
//				
//				writer.write("raw single infer:");
//				for(String str:rawActInfer.get(i).activityList){
//					writer.write(str+",");
//				}
//				writer.write("\r\n");
//				
//				
//				writer.write("Infer: ");
//				for(String str:gaInfer.get(i).activityList){
//					writer.write(str+",");
//				}
//				writer.write("|");
//				
//				for(String str:actInfer.get(i).activityList){
//					writer.write(str+",");
//				}
//				
//				writer.write("| truth:");
//				for(String str: actTruth.get(i).activityList){
//					writer.write(str+",");
//				}
//				
//				/*check*/
//				Boolean same=checkListSame(actInfer.get(i).activityList,actTruth.get(i).activityList);
//				if(!same){
//					writer.write("   xxxxxxxxxxxxxx infer wrong xxxxxxxxxxxxxxxxx");
//				}
//				
//				
//				writer.write("\r\n");
//				writer.flush();
//				/*write ap control*/
//				writer.write("turn off: ");
//				if(apRecord.get(i).controlFromONsList.size()>0){
//					for(String str:apRecord.get(i).controlFromONsList){
//						writer.write(str+",");
//					}
//				}
//	
//				
//				writer.write("  |Noise: ");
//				if(apRecord.get(i).truthList.size()>0){
//					for(String str:apRecord.get(i).truthList){
//						writer.write(str+",");
//					}
//				}
//				
//				same=checkListSame(apRecord.get(i).controlFromONsList,apRecord.get(i).truthList);
//				if(!same){
//					writer.write("   xxxxxxxxxxxxxxxx control wrong xxxxxxxxxxxx");
//				}
//				
//				writer.write("\r\n");
//				writer.write("\r\n");
//				writer.write("\r\n");
//				writer.flush();
//				
//
//				
//			}
			
			Set<String> key= actExpResult.keySet();
			for(String str:key){
				RecRate r= actExpResult.get(str);
				double precision= r.tp/(r.tp+r.fp);
				double recall = r.tp/(r.tp+r.fn);
				double fMeasure= 2* (precision*recall)/(precision+recall);
				writer.write("Act:"+str+"\r\n");
				writer.write("precision:"+precision+"\r\n");
				writer.write("recall:"+recall+"\r\n");
				writer.write("F-MEASURE:"+fMeasure+"\r\n\r\n");
				writer.flush();
				
			}
			

			
			/*not finish*/
			
			/*cal service time*/
			
			/*cal precision*/
			
			/*cal recall*/
			
			writer.close();
			

	
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	

	
	public void wirteOutESResult(){
		try{
			FileWriter writer = new FileWriter(new File("./_output_results/esResult.txt"),false);
			/*Serving time*/
			writer.write("total time: "+totalTime+"\r\n"   );
			writer.write("serving time withoutGA: "+serviceTimeWithOutGA+"\r\n");
			writer.write("serving time withGA: "+serviceTimeWithGA+"\r\n\r\n");
			
			/*comfort evaluation*/
			writer.write("raw avg thermal :"+(rawAvgTC/rawAvgTC_count)+"\r\n");
			writer.write("avg thermal :"+(AvgTC/AvgTC_count)+"\r\n");
			writer.write("raw avg visual :"+(rawAvgVC/rawAvgVC_count)+"\r\n");
			writer.write("avg visual :"+(AvgVC/AvgVC_count)+"\r\n\r\n");
			
			/*type energy consumption evaluation*/
			writer.write("thermal type save : "+thermalTypeSave+"\r\n");
			writer.write("visual type  save : "+visualTypeSave+"\r\n");				
			writer.write("ap type save : "+apTypeSave+"\r\n\r\n");
			
			/*agent energy consumption evaluation*/
			writer.write("thermal agent save : "+thermalAgentSave+"\r\n");
			writer.write("visual agent  save : "+visualAgentSave+"\r\n");				
			writer.write("ap agent save : "+apAgentSave+"\r\n\r\n");
			
			/*noise saving*/
			writer.write("noise consumption : "+noiseConsumption+"\r\n");
			writer.write("location based noise save:"+(noiseConsumption-locationBasedRightSaveFromOn)+"\r\n");
			writer.write("ap agent right save from noise : "+(noiseConsumption-apAgentRightSaveFromOn)+"\r\n\r\n");
			
			/*total energy saving result*/
			writer.write("raw total consumption : "+rawTotalConsumption+"\r\n");
			writer.write("location based consumption:"+locationBasedTotalConsumption+"\r\n");
			writer.write("mchess total consumption : "+mchessTotalConsumption+"\r\n\r\n");
			
//			writer.write("thermal type save : "+thermalTypeSave+"\r\n");
//			writer.write("visual type  save : "+visualTypeSave+"\r\n");				
//			writer.write("ap type save : "+apTypeSave+"\r\n");
//			writer.write("ap type save from on: " +apTypeSaveFromOn+"\r\n");
//			writer.write("ap  type save from standby: "+apTypeSaveFromStandby+ "\r\n");
//			writer.write("noise consumption : "+noiseConsumption+"\r\n");
//			writer.write("ap type right save from noise : "+apTypeRightSaveFromOn+"\r\n");
//			writer.write("ap type wrong save from noise: "+apTypeWrongSave+"\r\n\r\n");
//			
//			
//			writer.write("thermal agent save : "+thermalAgentSave+"\r\n");
//			writer.write("visual agent  save : "+visualAgentSave+"\r\n");				
//			writer.write("ap agent save : "+apAgentSave+"\r\n");
//			writer.write("ap agent save from on: " +apAgentSaveFromOn+"\r\n");
//			writer.write("ap agent save from standby: "+apAgentSaveFromStandby+"\r\n");
//			writer.write("noise consumption : "+noiseConsumption+"\r\n");
//			writer.write("ap agent right save from noise : "+apAgentRightSaveFromOn+"\r\n");
//			writer.write("ap agent wrong save from noise: "+apAgentWrongSave+"\r\n");
//			writer.write("ap wrong count : "+apWrongCount+"\r\n\r\n");
//
//			
//			writer.write("total save : "+totalSave+"\r\n");
//			writer.write("thermal conflict : "+thermalConflictCount+"\r\n");
//			writer.write("visual conflict : "+visualConflictCount+"\r\n\r\n");
//			
//			writer.write("serving time withoutGA: "+serviceTimeWithOutGA+"\r\n");
//			writer.write("serving time withGA: "+serviceTimeWithGA+"\r\n\r\n");
//			
//
//			writer.write("location based consumption:"+locationBasedTotalConsumption+"\r\n");
//			writer.write("location based noise save:"+locationBasedRightSaveFromOn+"\r\n");
//			writer.write("location based wrong count:"+locationBasedWrongCount+"\r\n");
//			writer.write("location total save:"+locationBasedTotalSave+"\r\n");
//			writer.write("location wrong save:"+locationBasedWrongSave+"\r\n");
//			writer.write("total2:"+totalConsumption2+"\r\n");
			
			writer.flush();
			writer.close();

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void setDuration(int in){
		durationList.add(in);
	}
	

	
	public int getDuration(){
		int duration=durationList.get( durationList.size()-1);
		return duration;
	}

	
	
	public void processEXPForLocationBased(String read,String read2, Epcie epcie,ArrayList<AppNode> decisionList,ArrayList<AppNode> envList){

		
	
		

		/*noise saving*/
		ExpRecorder.exp.setLocationBased(decisionList,envList,read2);
		/*total consumption after location*/
		//ExpRecorder.exp.setConsumptionForLocationBased(envList, decisionList);
			
	}
	
	public void processEXP(String read,String read2, Epcie epcie,ArrayList<AppNode> decisionList,ArrayList<AppNode>eusList){
		/*set duration*/
		ExpRecorder.exp.setDuration(SimulatorTest.duration);
		/*set day*/
		ExpRecorderByDay.expByDay.setDay(SimulatorTest.rowNum);
		/*5.4 record infer result*/
		ExpRecorder.exp.setRowNum(SimulatorTest.rowNum);
		ExpRecorder.exp.setActTruth(read);
		ExpRecorder.exp.setActInfer(epcie.gaInference.actInferResultSet);
		ExpRecorder.exp.setGaInfer(epcie.gaInference.gaInferResultList);
		ExpRecorder.exp.setRawInfer(epcie.gaInference.rawGAinferResultList,epcie.gaInference.rawActInferResultSet);
		/*record total consumption*/
		ExpRecorder.exp.setCousumption(eusList, decisionList);
		/*record thermal save*/
		ExpRecorder.exp.setThermalAgentSave(eusList, decisionList);
		/*record visual save*/
		ExpRecorder.exp.setVisualAgentSave(eusList, decisionList);
		/*record visual save*/
		ExpRecorder.exp.setAPAgentSave(eusList, decisionList);
		/*5.1. for experiment record 關錯的電器*/
		ExpRecorder.exp.setApAgentWrongData(decisionList,eusList, read2);
		/*5.2 record noise consumption*/
		ExpRecorder.exp.setNoiseConsumption(read2,eusList);

		ExpRecorder.exp.setApTypeWrongData(decisionList, eusList, read2);
		ExpRecorder.exp.setAPTypeSave(eusList, decisionList);
		ExpRecorder.exp.setThermalTypeSave(eusList, decisionList);
		ExpRecorder.exp.setVisualTypeSave(eusList, decisionList);
		
		ExpRecorder.exp.setThermalComfort_new(eusList, decisionList, epcie.gaInference);
		ExpRecorder.exp.setVisualComfort_new(eusList, decisionList, epcie.gaInference);
		
		ExpRecorder.exp.calServingTime(epcie.gaInference);
		ExpRecorder.exp.setRecRate(epcie.gaInference.actInferResultSet,read);
		//ExpRecorder.exp.setRecRate(epcie.gaInference.rawActInferResultSet,read);
		
		
		/*exp record end*/
	}
}
