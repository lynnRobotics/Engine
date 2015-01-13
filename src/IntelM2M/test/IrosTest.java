package IntelM2M.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import IntelM2M.algo.PRClassifier;
import IntelM2M.algo.Prior;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.ESExpResult;
import IntelM2M.datastructure.ESService;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.ExpResult;
import IntelM2M.datastructure.PRmodel;
import IntelM2M.datastructure.RelationTable;
import IntelM2M.datastructure.SensorNode;
import IntelM2M.epcie.GaGenerator;
import IntelM2M.epcie.classifier.GaDbnClassifier;
import IntelM2M.epcie.erc.GaEscGenerator;

public class IrosTest {
	 
	public ArrayList<Map <String,Boolean>> getInferResult(ArrayList<GaGenerator> GaGeneratorList,ArrayList<GaDbnClassifier> GaDbnList,ArrayList<ArrayList<String>> kGaList,String read){
		ArrayList<Map <String,Boolean>> GAinferResultList= new ArrayList<Map <String,Boolean> >();
		Boolean continueFlag=true;
		Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
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
					
			String[] split = read.split("#");
			String []sensorContext=split[0].split(" ");
			ArrayList<String> rawFromDBN = new ArrayList<String>();
			ArrayList<String> inferDBN = new ArrayList<String>();
			Map<String,String> probDBN = new LinkedHashMap<String,String>();
			for(int i=0 ;i<sensorContext.length;i++){
				SensorNode s= new SensorNode(sensorName[i],sensorContext[i]);
				rawFromDBN = GaDBN.GaDBNInference(GA,s.name, s.discreteValue);
			}

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
		
			/*record result*/
			for(String str:inferDBN){
				GAinferResult.put(str, true);
			}


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
		return GAinferResultList;
	}
	
	public ArrayList<String> getRoomBasedControlList(String read){
		ArrayList<String> controlList=new ArrayList<String>();
		/*room based rule*/
		String []split=read.split("#");
		
		String []actArr=split[1].split(" ");
		if(actArr.length>1){
			int i=0;
			i++;
		}
		
		/*build location list*/
		String [] locationActList=(String[])EnvStructure.actAppList.keySet().toArray(new String[0]);
		ArrayList <String>locationList= new ArrayList<String>();
		for(String str:actArr){
			for(String str2:locationActList){
				String[]split2=str2.split("_");
				if(str.equals(split2[1])){
					locationList.add(split2[0]);
				}
			}
		}
		/*build controlList*/
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
			if(!contain && !controlList.contains(str)){
				
				String []allContext=split[0].split(" ");
				String context=getSensorContext(str,allContext);
				
				if(context.equals("on") || context.equals("standby"))
					controlList.add(str);
			}
		}
		

		return controlList;
	}
	public String getSensorContext(String appName,String []allContext){
		String [] sensorName=(String[])EnvStructure.sensorStatus.keySet().toArray(new String[0]);

		/*get sensor context*/
		int index=-1;
		for(int j=0;j<sensorName.length;j++){
			if(sensorName[j].equals(appName)){
				index=j;
				break;
			}
		}
		String context=allContext[index];
		return context;
	}
	
	public String [] getSensorContextAfterControl(String []sensorContext,ArrayList<String> allControlList){
		String [] sensorName=(String[])EnvStructure.sensorStatus.keySet().toArray(new String[0]);
		String [] sensorContextAfterEss=sensorContext.clone();
		for(String str:allControlList){
			int index=-1;
			for(int i=0;i<sensorName.length;i++){
				String str2=sensorName[i];
				if(str.equals(str2)){
					index=i;
					break;
				}
			}
			sensorContextAfterEss[index]="off";
			
		}
		return sensorContextAfterEss;
	}
	public  void addStandbySaving(ESExpResult exp,ArrayList<String> allControlList,String []sensorContext){
		Map<String,AppNode> appList=EnvStructure.appList;
		String [] sensorName=(String[])EnvStructure.sensorStatus.keySet().toArray(new String[0]);
		for(String str:allControlList){
			/*get sensor context*/
			int index=-1;
			for(int j=0;j<sensorName.length;j++){
				if(sensorName[j].equals(str)){
					index=j;
					break;
				}
			}
			String context=sensorContext[index];
			if(context.equals("standby")){
				Map<String, Double> ampere=appList.get(str).ampere;
				//exp.standbySaving+=ampere.get("standby")*110*5/1000/60;
				exp.standbySaving+=1;
			}
		}
	}
	public  void addNoiseSaving(ESExpResult exp,ArrayList<String> allControlList,String []sensorContext,String []noiseList){
		Map<String,AppNode> appList=EnvStructure.appList;
		String [] sensorName=(String[])EnvStructure.sensorStatus.keySet().toArray(new String[0]);
		for(String str:allControlList){
			Boolean isNoise=false;
			for(String str2:noiseList){
				if(str.equals(str2)){
					isNoise=true;
				}
			}
			if(isNoise){
				/*get sensor context*/
				int index=-1;
				for(int j=0;j<sensorName.length;j++){
					if(sensorName[j].equals(str)){
						index=j;
						break;
					}
				}
				String context=sensorContext[index];
				if(context.equals("on")){
					Map<String, Double> ampere=appList.get(str).ampere;
					//exp.noiseSaving+=ampere.get("on")*110*5/1000/60;
					exp.noiseSaving+=1;
				}
			}
		}
	}
	private void addAllConsumption(ESExpResult exp,String []sensorContext){
		Map<String,AppNode> appList=EnvStructure.appList;
		String [] sensorName=(String[])EnvStructure.sensorStatus.keySet().toArray(new String[0]);
		for(int i=0;i<sensorContext.length;i++){
			String context=sensorContext[i];
			AppNode app=appList.get(sensorName[i]);
			if(app!=null){
				if(context.equals("on") || context.equals("standby")){
					double amp=app.ampere.get(context);
					exp.totalComsumption+=amp*110*5/1000/60;
					//exp.totalComsumption+=1;
				}
			}
		
		}
	}
	private void addAfterEssConsumption(ESExpResult exp,String[]sensorContextAfterEss){
		Map<String,AppNode> appList=EnvStructure.appList;
		String [] sensorName=(String[])EnvStructure.sensorStatus.keySet().toArray(new String[0]);
		for(int i=0;i<sensorContextAfterEss.length;i++){
			String context=sensorContextAfterEss[i];
			AppNode app=appList.get(sensorName[i]);
			if(app!=null){
				if(context.equals("on") || context.equals("standby")){
					
					double amp=app.ampere.get(context);
					//exp.afterEssComsuption+=amp*110*5/1000/60;
					exp.afterEssComsuption+=1;
				}
			}
		}
	}
	private void addNoiseConsumption(ESExpResult exp,String []noiseList){
		Map<String,AppNode> appList=EnvStructure.appList;
		
		for(String str:noiseList){
			AppNode app=appList.get(str);
			if(app!=null){
				try {
					
					double amp=app.ampere.get("on");
					//exp.noiseConsumption+=amp*110*5/1000/60;
					exp.noiseConsumption+=1;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	public void addRuleSaving(ESExpResult exp,PRmodel prModel,String [] sensorContext,String []noiseList){
		Map<Integer,ArrayList<AppNode>> rule12=prModel.rule12;
		Map<String,AppNode> appList=EnvStructure.appList;
		Set<Integer> ruleKey=rule12.keySet();
		for(Integer i:ruleKey){
			ArrayList<AppNode> ruleAppList=rule12.get(i);
			for(AppNode app:ruleAppList){
				String context=getSensorContext(app.appName,sensorContext);
				Boolean containNoise=false;
				for(String str:noiseList){
					if(str.equals(app.appName)){
						containNoise=true;
						break;
					}
				}
				if(containNoise){

					//double amp= appList.get(app.appName).ampere.get(context)*110*5/1000/60;
					double amp= 1;
					double raw=exp.ruleSaving.get(i);
					exp.ruleSaving.put(i, amp+raw);
				}
			}
		}
	}
	public void addRoomRuleSaving(ESExpResult exp,PRmodel prModel,String [] sensorContext,String []noiseList){
		Map<Integer,ArrayList<AppNode>> rule12=prModel.rule12;
		ArrayList<String> allControlList=exp.roomControlList;
		Map<String,AppNode> appList=EnvStructure.appList;
		Set<Integer> ruleKey=rule12.keySet();
		for(String str:allControlList){
			/*find belonging rule*/
			int ruleNumber=-1;
			Boolean same=false;
			for(Integer i:ruleKey){
				ArrayList<AppNode> ruleAppList=rule12.get(i);
				for(AppNode app:ruleAppList){
					if(str.equals(app.appName)){
						ruleNumber=i;
						same=true;
						break;
					}
				}
				if(same){
					break;
				}
			}
			String context=getSensorContext(str,sensorContext);
			if(ruleNumber!=-1){
				
				Boolean containNoise=false;
				for(String str2:noiseList){
					if(str2.equals(str)){
						containNoise=true;
						break;
					}
				}
				if(containNoise){
				//	double amp = appList.get(str).ampere.get(context) * 110 * 5/ 1000 / 60;
					double amp = 1;
					double raw = exp.ruleSaving.get(ruleNumber);
					exp.ruleSaving.put(ruleNumber, amp + raw);
				}
			
			}
			else{
				//double amp = appList.get(str).ampere.get(context) * 110 * 5/ 1000 / 60;
				double amp = 1;
				double raw = exp.ruleSaving.get(13);
				exp.ruleSaving.put(13, amp + raw);
				
				exp.roomNo++;
			}
		}
	}

	public void writeRoomBasedResult(FileWriter writer,String read,String appNoiseGroundTruth,ArrayList<String> adjustableAppList,ESExpResult exp){
		try {
			String []split1=read.split("#");
			String []sensorContext=split1[0].split(" ");
			ArrayList<String> allControlList=getRoomBasedControlList(read);

			String []noiseList=appNoiseGroundTruth.split(" ");
			String []sensorContextAfterEss=getSensorContextAfterControl(sensorContext,allControlList);
			addStandbySaving(exp, allControlList,sensorContext);
			addNoiseSaving(exp,allControlList,sensorContext,noiseList);
			addAllConsumption(exp,sensorContext);
			addAfterEssConsumption(exp,sensorContextAfterEss);
			addNoiseConsumption(exp,noiseList);
			/*check over close*/
			ArrayList<String> overCloseList= new ArrayList<String>();
			for(String str:allControlList){
				for(String str2:adjustableAppList){
					if(str.equals(str2)){
						overCloseList.add(str);
					}
				}
			}

			
			exp.wrong+=overCloseList.size();
			exp.roomWrongList=overCloseList;
			exp.roomControlList=allControlList;
	
			
			writer.write(read+"\r\n");
			writer.write(" Close: ");
			for(int i=0;i<allControlList.size();i++){
				String str=allControlList.get(i);

				writer.write(str+" ");
				
			}
			writer.write("\r\n");
			writer.write(" wrong: ");
			for(int i=0;i<overCloseList.size();i++){
				String str=overCloseList.get(i);

				writer.write(str+" ");
				
			}
			writer.write("\r\n");
			writer.write(exp.noiseConsumption+"\r\n");
			writer.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void writeESSResult(FileWriter writer,PRmodel prModel,String read,String appNoiseGroundTruth,ArrayList<String> nonAdjustableAppList,ESExpResult exp,ESExpResult exp2){
		try {
			/*make decision*/
			/*判斷每個service和環境狀態*/
			ArrayList<String> controlList0=new ArrayList<String>();
			ArrayList<String> list0Status= new ArrayList<String>();
			ArrayList<String> controlList1=new ArrayList<String>();
			ArrayList<String> controlList2=new ArrayList<String>();
			String []split1=read.split("#");
			String []sensorContext=split1[0].split(" ");
			String [] sensorName=(String[])EnvStructure.sensorStatus.keySet().toArray(new String[0]);
			for(int i=0;i<prModel.esServiceList.size();i++){
				ESService es=prModel.esServiceList.get(i);
				for(AppNode app:es.appList){
					int index=-1;
					for(int j=0;j<sensorName.length;j++){
						if(sensorName[j].equals(app.appName)){
							index=j;
							break;
						}
					}
					/*service1*/
					if(i==0){
						if(sensorContext[index].equals("on") || sensorContext[index].equals("standby")){
							controlList0.add(app.appName);
							list0Status.add(sensorContext[index]);
						}
					}else if(i==1){
						if(sensorContext[index].equals("on") || sensorContext[index].equals("standby")){
							controlList1.add(app.appName);
						}
						
					}else if(i==2){
						controlList2.add(app.appName);
					}

				}
			}	
	
			/*get ESS1 candidate List*/
			ArrayList<String>appClossInEss0andEss1= new ArrayList<String>();
			for(int i=0;i<controlList0.size();i++){
				String str=controlList0.get(i);
				appClossInEss0andEss1.add(str);
			}
			String []noiseList=appNoiseGroundTruth.split(" ");
			ArrayList<String> appNotClossInEss0= new ArrayList<String>();
			for(String str:noiseList){
				Boolean same=false;
				int i=0;
				for(String str2:controlList0){
					if(str.equals(str2)){					
						same=true;
						break;
					}
					i++;
				}

				if(same==false){
					appNotClossInEss0.add(str);
				}
			}
			//appNotClossInEss0.add("current_AC_livingroom");
			
			
			/*Get ESS 1 List and Not Closs List*/
			ArrayList<String>appNotClossInEss0andEss1= new ArrayList<String>();
			ArrayList<String> ess1List= new ArrayList<String>();			
			for(String str:appNotClossInEss0){/*In noise but not in ESS0*/
				Boolean same=false;
				for(String str2:controlList1){/*ESS1 list*/
					if(str.equals(str2)){
						ess1List.add(str);
						same=true;
						break;
					}
				}
				if(same==false){
					appNotClossInEss0andEss1.add(str);
				}
			}
			ArrayList<String> allControlList= new ArrayList<String>();
			for(String str:controlList0){
				allControlList.add(str);
			}
			for(String str:ess1List){
				allControlList.add(str);
			}
			
			String []sensorContextAfterEss=getSensorContextAfterControl(sensorContext,allControlList);
			addStandbySaving(exp, allControlList,sensorContext);
			addNoiseSaving(exp,allControlList,sensorContext,noiseList);
			addAllConsumption(exp,sensorContext);
			addAfterEssConsumption(exp,sensorContextAfterEss);
			addNoiseConsumption(exp,noiseList);
			addRuleSaving(exp,prModel,sensorContext,noiseList);
			
			addRoomRuleSaving(exp2,prModel,sensorContext,noiseList);
			


			/*over close List*/
			ArrayList<String> overCloseList= new ArrayList<String>();
			for(String str:controlList0){
				for(String str2: nonAdjustableAppList){
					if(str.equals(str2)){
						overCloseList.add(str);
					}
					
				}
				
			}
			/*feedback*/
			if(ess1List.size()>0 || appNotClossInEss0andEss1.size()>0 || overCloseList.size()>0){
				userFeedback(prModel,ess1List,appNotClossInEss0andEss1,overCloseList);
			}
			/*write*/
			writer.write(read+"\r\n");
//			/*write service content*/
//			for(int i=0;i<prModel.esServiceList.size();i++){
//				writer.write("ESS"+i+": ");
//				ESService es=prModel.esServiceList.get(i);
//				for(AppNode app:es.appList){
//					writer.write(app.appName+" "+app.state+" ");
//				}
//				writer.write("\r\n");
//			}
//			
			/*write feature and selected feature*/
			writer.write("all infer result:");
			for(String str:prModel.prFeature){
				writer.write(str+" ");
			}
			writer.write("\r\n");
//			
//			writer.write("all selected result:");
//			for(String str:prModel.selectedPrFeature){
//				writer.write(str+" ");
//			}
//			writer.write("\r\n");
//			/*write ess0 control result*/
//			writer.write("Ess0 Close: ");
//			for(int i=0;i<controlList0.size();i++){
//				String str=controlList0.get(i);
//
//				writer.write(str+" ");
//				
//			}
//			writer.write("\r\n");
//			/*write ess1 control result*/
//			writer.write("Ess1 Close: ");
//			for(int i=0;i<ess1List.size();i++){
//				String str=ess1List.get(i);
//
//				writer.write(str+" ");
//				
//			}
//			writer.write("\r\n");
			
			/*write on appliance*/
	
			String [] appListName=(String[])EnvStructure.appList.keySet().toArray(new String[0]);
			ArrayList<String> appOnList=new ArrayList<String>();
			for(int i=0;i<sensorName.length;i++){
				String str=sensorName[i];
				Boolean isAppliance=false;
				for(String str2:appListName){
					if(str.equals(str2)){
						isAppliance=true;
						break;
					}
				}
				if(isAppliance && sensorContext[i].equals("on")){
					appOnList.add(str);
				}
			}
			writer.write("App On List: ");
			for(String str:appOnList){
				writer.write(str+" ");
			}
			writer.write("\r\n");
			
//			/*write noise*/
//			writer.write("Noise: ");
//			for(String str:noiseList){
//				writer.write(str+" ");
//			}
//			writer.write("\r\n");
//		
//			/*writecontrolList1*/
//			writer.write("controlList1: ");
//			for(String str:controlList1){
//				writer.write(str+" ");
//			}
//			writer.write("\r\n");
//			/*write ess1List*/
//			writer.write("ess1List: ");
//			for(String str:ess1List){
//				writer.write(str+" ");
//			}
//			writer.write("\r\n");
//			
//
//			exp.wrong+=overCloseList.size();
//			
//			/*write control*/
//			writer.write("our control:");
//			for(String str:allControlList){
//				writer.write(str+" ");
//			}
//			writer.write("\r\n");
//			writer.write("room control:");
//			for(String str:exp2.roomControlList){
//				writer.write(str+" ");
//			}
//			writer.write("\r\n");
//			
//			/*get room control not in noise not in standby*/
//			writer.write("N noise N standby:");
//			for(String str:exp2.roomControlList){
//				boolean inNoise=false;
//				for(String str2:noiseList){
//					if(str.equals(str2)){
//						inNoise=true;
//					}
//				}
//				if(!inNoise){
//					String context=getSensorContext(str,sensorContext);
//					if(!context.equals("standby")){
//						writer.write(str+" ");
//					}
//				}
//			}
//			writer.write("\r\n");
//			/*write wrong*/
//			writer.write("our wrong:");
//			for(String str:overCloseList){
//				writer.write(str+" ");
//			}
//			writer.write("\r\n");
//			writer.write("room wrong:");
//			for(String str:exp2.roomWrongList){
//				writer.write(str+" ");
//			}
//			writer.write("\r\n");
//			
//			
//			writer.write("noise saving:"+exp.noiseSaving+" "+exp2.noiseSaving+" \r\n");
//			writer.write("consume:"+exp.afterEssComsuption+" "+exp2.afterEssComsuption+" \r\n");
			
			/*write 12 rule*/
			Set<Integer> ruleNum = prModel.rule12.keySet();
			for(Integer i:ruleNum){
				writer.write("rule"+i+" :");
				ArrayList <AppNode> appList=prModel.rule12.get(i);
				for(AppNode app:appList){
					writer.write(app.appName+" ");
				}
				writer.write("\r\n");
			}
					
			//writer.write("feedback  :"+prModel.feedback);
			writer.write("\r\n\r\n");
			writer.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void userFeedback(PRmodel prModel,ArrayList<String> ess1List,ArrayList<String> notCloseList,ArrayList<String> overCloseList){
		/*ess1List*/
		for(String str:ess1List){
			Boolean find=false;
			int index1=-1;int index2=-1;
			
			for(int i=0;i<prModel.esServiceList.size() && !find;i++){
				ESService ess=prModel.esServiceList.get(i);
				for(int j=0;j<ess.appList.size() && !find;j++){
					AppNode app=ess.appList.get(j);
					if(app.appName.equals(str)){
						prModel.esServiceList.get(i).appList.get(j).state="off";
						find=true;
						index1=i;
						index2=j;
					}
				}
			}
			if(index1!=-1 && index2!=-1){
				AppNode app=prModel.esServiceList.get(index1).appList.get(index2);
				app.state="off";
				prModel.esServiceList.get(index1).appList.remove(index2);
				prModel.esServiceList.get(0).appList.add(app);
			}
		}
		/*notCloseList*/
		for(String str:notCloseList){
			Boolean find=false;
			int index1=-1;int index2=-1;
			
			for(int i=0;i<prModel.esServiceList.size() && !find;i++){
				ESService ess=prModel.esServiceList.get(i);
				for(int j=0;j<ess.appList.size() && !find;j++){
					AppNode app=ess.appList.get(j);
					if(app.appName.equals(str)){
						prModel.esServiceList.get(i).appList.get(j).state="off";
						find=true;
						index1=i;
						index2=j;
					}
				}
			}
			if(index1!=-1 && index2!=-1){
				AppNode app=prModel.esServiceList.get(index1).appList.get(index2);
				app.state="off";
				prModel.esServiceList.get(index1).appList.remove(index2);
				prModel.esServiceList.get(0).appList.add(app);
			}
		}
		/*overCloseList*/
		for(String str:overCloseList){
			Boolean find=false;
			int index1=-1;int index2=-1;
			
			for(int i=0;i<prModel.esServiceList.size() && !find;i++){
				ESService ess=prModel.esServiceList.get(i);
				for(int j=0;j<ess.appList.size() && !find;j++){
					AppNode app=ess.appList.get(j);
					if(app.appName.equals(str)){
						prModel.esServiceList.get(i).appList.get(j).state="off";
						find=true;
						index1=i;
						index2=j;
					}
				}
			}
			if(index1!=-1 && index2!=-1){
				AppNode app=prModel.esServiceList.get(index1).appList.get(index2);
				app.state="on";
				prModel.esServiceList.get(index1).appList.remove(index2);
				prModel.esServiceList.get(2).appList.add(app);
			}
		}
		prModel.feedback+=1;


	}
	
	public ArrayList<String> getNonAdjustableAppList(String read,ArrayList<GaEscGenerator> GaEscList,PRClassifier prClassifier,ArrayList<GaGenerator> GaGeneratorList){
		ArrayList<String> actTruth= new ArrayList<String>();
		String []split1=read.split("#");
		String []split2=split1[1].split(" ");
		for(String str:split2){
			/*change act name to ga name*/
			String GID=GaGeneratorList.get(0).getGID(str).get(0);
			actTruth.add(GID);
		}
		ArrayList<AppNode> controlTruth=prClassifier.getEscList(actTruth, GaEscList);
		ArrayList<String> nonAdjustableAppList = new ArrayList<String>();
		for(AppNode app:controlTruth){
			if(app.escType.equals("explicit") && app.state.equals("on")){
				nonAdjustableAppList.add(app.appName);
			}
		}
		return nonAdjustableAppList;
	}
	public void writeFinalResult(FileWriter writer,ESExpResult systemResult,ESExpResult roomBasedResult){
		try {
			writer.write("Total Consumption:"+ systemResult.totalComsumption+"\r\n");
			writer.write("Noise Consumption:"+ systemResult.noiseConsumption+"\r\n");
			writer.write("Our \r\n");
			writer.write(systemResult.noiseSaving+" "+systemResult.standbySaving+"\r\n");
			writer.write("Our Consumption:"+systemResult.afterEssComsuption+"\r\n");
			writer.write("wrong"+systemResult.wrong+"\r\n");
			writer.write("RoomBased \r\n");
			writer.write(roomBasedResult.noiseSaving+" "+roomBasedResult.standbySaving+"\r\n");
			writer.write("wrong"+roomBasedResult.wrong+"\r\n");
			writer.write("rule Consumption:"+roomBasedResult.afterEssComsuption+"\r\n");
			/*write rule*/
			Set<Integer> ruleKey=systemResult.ruleSaving.keySet();
			writer.write("our rule: \r\n");
			for(Integer i:ruleKey){
				writer.write("rule"+i+" :"+systemResult.ruleSaving.get(i)+"\r\n");
			}
			/*write room rule*/
			ruleKey=roomBasedResult.ruleSaving.keySet();
			writer.write("room based: \r\n");
			for(Integer i:ruleKey){
				writer.write("rule"+i+" :"+roomBasedResult.ruleSaving.get(i)+"\r\n");
			}
			writer.write("room no:"+roomBasedResult.roomNo+"\r\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String generateWastedData(String testingDataPath,String modifiedPath,String resultPath){
	
		try{
			int []controlMember= {0,1,2,3,13,14};
			
			BufferedReader reader = new BufferedReader(new FileReader(testingDataPath));
			FileWriter writer1 = new FileWriter(new File(modifiedPath),false);
			FileWriter writer2 = new FileWriter(new File(resultPath),false);
			String read=null;
			Random r=new Random();
			Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
			String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
			while((read = reader.readLine()) != null){
				String []split1= read.split("#");
				String []split2=split1[0].split(" ");
				ArrayList<String>groundTruth= new ArrayList<String>();
				for(int i=0;i<controlMember.length;i++){
					if(r.nextDouble()>0.4 ) {
						if(!split2[controlMember[i]].equals("on")){
							split2[controlMember[i]]="on";
							String name= sensorName[ controlMember[i] ];
							groundTruth.add(name);
						}
					}
				}
				/*write modify result*/
				for(String str:split2){
					writer1.write(str+" ");
				}
				writer1.write("#"+split1[1]+"\r\n");
				writer1.flush();
				/*write modify ground truth*/
				for(String str:groundTruth){
					writer2.write(str+" ");
				}
				writer2.write("\r\n");
				writer2.flush();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return modifiedPath;
	}
	 public void irosTesting(ArrayList<GaGenerator> GaGeneratorList,ArrayList<GaDbnClassifier> GaDbnList,ArrayList<GaEscGenerator> GaEscList,String testingDataPath){
		   	try {
	
		   			/*generate energy waste pattern-1*/
		   			//String modifiedPath="./_input_data/iros_test_data.txt";
 	   			    //String resultPath="./_input_data/iros_test_data_truth.txt";
		   			//generateWastedData(testingDataPath,modifiedPath,resultPath);
		   			
		   			/*simulator*/
	   				//String modifiedPath="./_input_data/simulator/iros_test_data.txt";
	   				//String resultPath="./_input_data/simulator/iros_test_data_truth.txt";
		   			String modifiedPath="./_input_data/simulator/simple_test_data.txt";
		   			String resultPath="./_input_data/simulator/simple_test_data_truth.txt";

		   			testingDataPath=modifiedPath;

					
		   			BufferedReader reader = new BufferedReader(new FileReader(testingDataPath));
					BufferedReader reader2 = new BufferedReader(new FileReader(resultPath));
					String read=null;
					
					
					/*k層有k個gaList,k個expResult*/
					ArrayList<ArrayList<String>> kGaList=new ArrayList<ArrayList<String>>();
					ArrayList<Map <String,ExpResult>> kExpResult=new ArrayList<Map <String,ExpResult> >();
					
					for(int k=0;k<GaGeneratorList.size();k++){
						/*build gaList*/
						Set<String> gSet=GaGeneratorList.get(k).gaList.keySet();
				  		ArrayList<String> gaList= new ArrayList<String>();
				   		for(String str:gSet){
				   			gaList.add(str);
				   		}
				   		/*exp result for GA*/
						Map <String,ExpResult> expResult = new  LinkedHashMap<String,ExpResult>();
						for(int i=0;i<gaList.size();i++){
							ExpResult r= new ExpResult();
							expResult.put(gaList.get(i), r);
						}
				   		kGaList.add(gaList);
				   		kExpResult.add(expResult);
					}
					FileWriter writer = new FileWriter(new File("./_output_results/ess.txt"),false);
					FileWriter writer2 = new FileWriter(new File("./_output_results/roomBased.txt"),false);
					PRClassifier prClassifier= new PRClassifier();
					ESExpResult systemResult= new ESExpResult();
					for(int i=1;i<=12;i++){
						systemResult.ruleSaving.put(i, 0.0);
					}
					ESExpResult roomBasedResult= new ESExpResult();
					for(int i=1;i<=13;i++){
						roomBasedResult.ruleSaving.put(i, 0.0);
					}
					String lastRead="";
					while((read = reader.readLine()) != null)
					{
						String appNoiseGroundTruth=reader2.readLine();
						/**/
						if(read.equals(lastRead)){
							continue;
						}
						lastRead=read;
						
						/*Get GA*/
						ArrayList<Map <String,Boolean>> GAinferResultList= getInferResult(GaGeneratorList,GaDbnList, kGaList, read);
						/*Get PR */
						ArrayList<String> prFeature= prClassifier.getPrInferFeature(GAinferResultList);
						PRmodel prModel=prClassifier.inferPR(prFeature);
						
						if(prModel==null){
							prClassifier.buildPrModel(GAinferResultList, GaEscList,GaGeneratorList,read);
						}
						//else{
							/*Get Service*/
						    prModel=prClassifier.inferPR(prFeature);
							/*given ES service*/
							writer = new FileWriter(new File("./_output_results/ess.txt"),true);
							writer2 = new FileWriter(new File("./_output_results/roomBased.txt"),true);
							ArrayList<String> nonAdjustableAppList=getNonAdjustableAppList(read,GaEscList,prClassifier,GaGeneratorList);
							writeRoomBasedResult(writer2,read,appNoiseGroundTruth,nonAdjustableAppList,roomBasedResult);
							writeESSResult(writer,prModel,read,appNoiseGroundTruth,nonAdjustableAppList,systemResult,roomBasedResult);
							
			
							
						//}
					}
					writeFinalResult(writer,systemResult,roomBasedResult);
				
					System.out.println("Testing Finish");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
		   	
		   	
		   	
		   }
}
