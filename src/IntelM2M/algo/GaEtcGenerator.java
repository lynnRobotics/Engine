package IntelM2M.algo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import weka.classifiers.bayes.net.EditableBayesNet;
import weka.core.Instances;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.RelationTable;
import IntelM2M.epcie.GaGenerator;

public class GaEtcGenerator {
 	public Map<String, RelationTable> actAppList; //< roomname_activity , relationtable> 儲存該room對應的電器與電器資訊
	public Instances insts;
	
	public GaEtcGenerator(GaGenerator GA)
	{
		try{
			File dir = new File("./_weka_training_data");
			insts = new Instances(new FileReader("./_weka_training_data/" + dir.list()[0]));

		}catch(Exception ex){}
		actAppList=buildGaActAppList(GA,EnvStructure.actAppList);	

	}
	
	public Map<String, RelationTable> buildGaActAppList(GaGenerator GA,Map<String, RelationTable> actAppList){
		Map<String, RelationTable> GaActAppList= new LinkedHashMap<String, RelationTable>();
  		Set<String> gSet=GA.gaList.keySet();
   		String [] gaList=(String[])gSet.toArray(new String[0]); 
   		
   		//RelationTable rtRaw= actAppList.get("WatchingTV");
   		
   		for(String str:gaList){	
   			RelationTable rt=new RelationTable();
//   			for(AppNode singleAPP:rtRaw.appList){
//   				rt.appList.add(singleAPP);
//   			}
   			/*build ga appList form member activity*/
   			ArrayList <String>gaMember=GA.getGroupMember(str);
   			for(String str2:gaMember){
   				/*if keyset of actAppList contraion gaMember*/
   				Set<String> actRoomSet=actAppList.keySet();
   				for(String str3:actRoomSet){
   					if(str3.contains(str2)){
   						/*add all appliance to the rt*/
   						for(AppNode singleApp:actAppList.get(str3).appList){
   							Boolean same=false;
   							for(AppNode gaApp:rt.appList){
   								if(gaApp.appName.equals(singleApp.appName)){
   									same=true;
   								}
   							}
   							if(!same){
   								AppNode newNode= new AppNode(singleApp.appName);
   								//newNode.avgAmpere=singleApp.avgAmpere;
   								rt.appList.add(newNode);
   							}
 						
   						}
   					}
   				}
   			}
   			GaActAppList.put(str, rt);
   		}	
		return GaActAppList;
	}
	


	public void buildAllETC(EditableBayesNet[] classifier,String etcPath,GaGenerator GA){
		try{
			FileWriter writer = new FileWriter(new File(etcPath), false);
			writer.flush();
			writer.close();
		}catch(Exception ex){}
		
		Set<String> gSet=GA.gaList.keySet();
		String [] gaList=(String[])gSet.toArray(new String[0]); 
		
		
		for(int index = 0;  index < gaList.length;  index ++){
		    	
			buildETCList(etcPath, gaList[index],classifier[index],GA); //build etc	    		
			System.out.println("build etc: " + gaList[index].substring(0, gaList[index].length() ) + ", done!");
		}
	}

	public void buildETCList(String file_path, String activity, EditableBayesNet classifier,GaGenerator GA) {
		//format: sensorName|type(explicit, implicit, or other)|state|confidence|powerLevel
	
		try {
			
			String activityName = activity; //activity: xxx.arff
			FileWriter  writer = new FileWriter(new File(file_path), true);
			/*activity name*/
			writer.write("Activity: " + activityName + "\n");
	
			/*find location of acts, set explicit and implicit apps*/
			String etcListKey = "";
			Set<String> acts = actAppList.keySet();
	
			
			for(String act:acts)
			{
				if(act.contains(activityName))
				{
					etcListKey = act;
					String[] split = act.split("_");
					writer.write("Location: " + split[0] + "\n");				
					setExplicit(act, activityName, classifier); //set etc
					setImplicit(act, activityName, classifier); //set etc
					break;
				}
			}
			
			/* Get selected sensors form classifier */
			writer.write("All_selected_sensors: ");
			for (int j = 0; j < classifier.getNrOfNodes() - 1; j++) {
				String node = classifier.getNodeName(j);
				//
				int stateNum = insts.attribute(node).numValues();
				
				int index = -1;
				double max_pe = 0.0, pe = 0.0;
				for(int k= 0; k < stateNum; k ++)
				{
					pe = classifier.getProbability(j, 1, k);
					if((k == 0) || (pe > max_pe))
					{
						max_pe = pe;
						index = k;
					}	
				}
				String nodeValue = classifier.getNodeValue(j, index);
				//
			
				writer.write(node+"|"+nodeValue+"|"+max_pe + " ");
			}
			writer.write("\n");
			
			/* explicit and implicit appliances */
			writer.write("Explicit :");
			for(int i = 0; i < actAppList.get(etcListKey).appList.size(); i ++)
			{
				if(actAppList.get(etcListKey).appList.get(i).escType.equals("explicit"))
				{
					writer.write(actAppList.get(etcListKey).appList.get(i).appName + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).escType + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).state + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).confidence + ",  ");
					//writer.write(actAppList.get(etcListKey).appList.get(i).power + "|");
					//writer.write(actAppList.get(etcListKey).appList.get(i).powerLevel + " ");
				}
			}
			writer.write("\r\n");
			writer.write("Implicit :");
			for(int i = 0; i < actAppList.get(etcListKey).appList.size(); i ++)
			{
				if( actAppList.get(etcListKey).appList.get(i).escType.equals("implicit"))
				{
					writer.write(actAppList.get(etcListKey).appList.get(i).appName + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).escType + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).state + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).confidence + ",  ");
					
	
				}
			}
			writer.write("\r\n");
			writer.write("\n\n");
			writer.flush();
			writer.close();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void setExplicit(String etcListKey, String activity, EditableBayesNet classifier){
		int aaa=classifier.getNrOfNodes();
		for (int i = 0; i < classifier.getNrOfNodes()-1 ; i ++) 
		{
		
			String node = classifier.getNodeName(i);
			if(node.contains("light") || node.contains("current"))
			{
			
				int stateNum = insts.attribute(node).numValues();
			
				int index = -1;
				double max_pe = 0.0, pe = 0.0;
				for(int j = 0; j < stateNum; j ++)
				{
					pe = classifier.getProbability(i, 1, j);
					if((j == 0) || (pe > max_pe))
					{
						max_pe = pe;
						index = j;
					}	
				}

				String nodeValue = classifier.getNodeValue(i, index);				
				
				for(int j = 0; j < actAppList.get(etcListKey).appList.size(); j ++){
					if(!node.equals(actAppList.get(etcListKey).appList.get(j).appName)) //explicit
					{
						
					}else if(j < actAppList.get(etcListKey).appList.size())
					{
						ArrayList<AppNode> appList= actAppList.get(etcListKey).appList;
						//set appList
						if(max_pe > 0.4)
							 appList.get(j).escType = "explicit";
						else
							 appList.get(j).escType = "other";
							
						 appList.get(j).state = nodeValue;
						 appList.get(j).confidence = roundTwoDecimals(max_pe);
						//actAppList.get(etcListKey).appList.get(j).power = Integer.parseInt(allAppActMap.findAppWatt(node, nodeValue));
						//actAppList.get(etcListKey).setAppPower(j,nodeValue);
						break;
					}
						
				}

				

			}
			else
				continue;
		}
	}
	

	
	public void setImplicit(String etcListKey, String activity, EditableBayesNet classifier){
		try{
			Instances actInsts = new Instances(new FileReader("./_weka_training_data/" + activity + ".arff"));
			int allLength = actInsts.numInstances();
			
			//extract on_activity
			for(int i = actInsts.numInstances() - 1; i >= 0 ; i --)
			{
				if(!activity.equals(actInsts.instance(i).stringValue(actInsts.numAttributes()-1)))
				{
					String tmp=actInsts.instance(i).stringValue(actInsts.numAttributes()-1);
					actInsts.delete(i);
					allLength --;
				}
			}

			//remove non-appliance
			for(int i = actInsts.numAttributes() - 2; i >= 0; i --)
				if(!(actInsts.attribute(i).name().contains("light") || actInsts.attribute(i).name().contains("current")))
					actInsts.deleteAttributeAt(i);
			
			//remove explicit
			for(int i = 0; i < actAppList.get(etcListKey).appList.size(); i ++)
			{
				if(actAppList.get(etcListKey).appList.get(i).escType.equals("explicit"))
				{
					String nodeName = actAppList.get(etcListKey).appList.get(i).appName;
					for(int j = actInsts.numAttributes() - 2; j >= 0; j --)
						if(actInsts.attribute(j).name().contains(nodeName))
							actInsts.deleteAttributeAt(j);
				}
			}
			
			if(activity.equals("g1-10")){
				int aaa=0;
				aaa++;
			}
			ArrayList <AppNode> appList=actAppList.get(etcListKey).appList;
			setImplictFeature( actInsts,"on",allLength,etcListKey);
			setImplictFeature( actInsts,"off",allLength,etcListKey);
		//	setImplictFeature( actInsts,"standby",allLength,etcListKey);

		//			/*ON appliance*/


		}catch(IOException e){e.printStackTrace();}
	}
	
	public void setImplictFeature(Instances actInsts,String status,int allLength,String etcListKey){
		int[] mutualLengthForON = new int[actInsts.numAttributes()-1]; //initial value: 0
		for(int i = 0; i < actInsts.numInstances(); i ++)
		{
			for(int j = 0; j < actInsts.numAttributes() - 1; j ++)
			{
				if( actInsts.instance(i).stringValue(j).equals(status)  )
					mutualLengthForON[j] ++;
			}
		}

		for(int i = 0; i < actInsts.numAttributes()-1; i ++)
		{
			if(((double)mutualLengthForON[i]/(double)allLength) > 0.7)
			{
				String node = actInsts.attribute(i).name();
				int j;
				for(j = 0; j < actAppList.get(etcListKey).appList.size(); j ++)
					if(node.equals(actAppList.get(etcListKey).appList.get(j).appName) && actAppList.get(etcListKey).appList.get(j).escType.equals(""))
						break;
				
				if(j < actAppList.get(etcListKey).appList.size())
				{
					//set appList
					actAppList.get(etcListKey).appList.get(j).escType = "implicit";
					actAppList.get(etcListKey).appList.get(j).state = status;
					actAppList.get(etcListKey).appList.get(j).confidence = roundTwoDecimals((double)mutualLengthForON[i]/(double)allLength);

					//actAppList.get(etcListKey).setAppPower(j,status);
					
				}
			}
		}
	}
	
	private double roundTwoDecimals(double d)
    {
    	DecimalFormat twoDForm = new DecimalFormat("#.##");
    	Double returnValue = 0.0;
    	
    	if(Double.isNaN(d))
    		returnValue = 1.0;
    	else 
    		returnValue = Double.valueOf(twoDForm.format(d));
    	
    	return returnValue;
    }
}
