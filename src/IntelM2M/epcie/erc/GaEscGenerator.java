package IntelM2M.epcie.erc;

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

public class GaEscGenerator {
	
 	public Map<String, RelationTable> actAppList; //< activity , relationtable> 儲存該活動對應的電器與電器資訊
	public Instances insts;
	private Boolean writeOut=true;
	
	public GaEscGenerator(GaGenerator GA,Boolean writeOut)
	{
		try{
			File dir = new File("./_weka_training_data");
			insts = new Instances(new FileReader("./_weka_training_data/" + dir.list()[0]));
			this.writeOut=writeOut;
			

		}catch(Exception ex){}
		actAppList=buildGaActAppList(GA,EnvStructure.actAppList);	

	}
	public void buildAllESC(EditableBayesNet[] classifier,String escPath,GaGenerator GA,GaGenerator firstGA,GaEscGenerator firstESC){
		try{
			FileWriter writer = new FileWriter(new File(escPath), false);
			writer.flush();
			writer.close();
		}catch(Exception ex){}
		
		Set<String> gSet=GA.gaList.keySet();
		String [] gaList=(String[])gSet.toArray(new String[0]); 
		
		
		for(int index = 0;  index < gaList.length;  index ++){
		    	
			buildESCList(escPath, gaList[index],classifier[index],GA,firstGA, firstESC); //build etc	    		
			System.out.println("build esc: " + gaList[index].substring(0, gaList[index].length() ) + ", done!");
		}
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
   				/*if keyset of actAppList contaion gaMember*/
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
   								
   								AppNode newNode=singleApp.copyAppNode(singleApp);
   								
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
	
	public void buildESCList(String file_path, String activity, EditableBayesNet classifier,GaGenerator GA,GaGenerator firstGA,GaEscGenerator firstESC) {
		
		try {
			
			String activityName = activity; //ex: g1-1
	
	
			/*find location of acts, set explicit and implicit apps*/
			String etcListKey = "";
			Set<String> acts = actAppList.keySet();
	
			
			for(String act:acts)
			{
				if(act.contains(activityName))
				{
					etcListKey = act;
			
					setESCExplicit(act, classifier,GA,firstGA,firstESC); //set explicit
					setESCImplicit(act, activityName, classifier); //set implicit
					break;
				}
			}
			if(writeOut){
				/*write out result*/
				/* explicit and implicit appliances */
				FileWriter  writer = new FileWriter(new File(file_path), true);
				writer.write("Activity: " + activityName + "\n");
				writer.write("Explicit :");
				for(int i = 0; i < actAppList.get(etcListKey).appList.size(); i ++)
				{
					if(actAppList.get(etcListKey).appList.get(i).escType.equals("explicit"))
					{
						writer.write(actAppList.get(etcListKey).appList.get(i).appName + "|");
						writer.write(actAppList.get(etcListKey).appList.get(i).escType + "|");
						writer.write(actAppList.get(etcListKey).appList.get(i).state + "|");
						writer.write(actAppList.get(etcListKey).appList.get(i).confidence + ",  ");
		
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
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setESCExplicit(String etcListKey, EditableBayesNet classifier,GaGenerator GA,GaGenerator firstGA,GaEscGenerator firstESC){
		if(GA.level==1){
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
							break;
						}
							
					}
				}
				else
					continue;
			}
		}else{
			mergeESCExplicit(etcListKey, classifier,GA, firstGA,firstESC);
		}
	}
	
	public void mergeESCExplicit(String etcListKey, EditableBayesNet classifier,GaGenerator GA,GaGenerator firstGA,GaEscGenerator firstESC){
		ArrayList<String> memberActivity=GA.gaList.get(etcListKey).actMemberList;
		
		try {
			for(String str:memberActivity){
				ArrayList<String> firstGidList=firstGA.getGID(str);
				for(String str2:firstGidList){
					ArrayList<AppNode> firstAppList=firstESC.actAppList.get(str2).appList;
					ArrayList<AppNode> theseAppList=actAppList.get(etcListKey).appList;
					for(AppNode firstApp:firstAppList){
						for(AppNode theseApp:theseAppList){
							if(firstApp.appName.equals( theseApp.appName) && firstApp.escType.equals("explicit")){
								theseApp.escType="explicit";
								theseApp.confidence=firstApp.confidence;
								theseApp.state=firstApp.state;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void setESCImplicit(String etcListKey, String activity, EditableBayesNet classifier){
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
				
				AppNode tmpNode= actAppList.get(etcListKey).appList.get(i);
					if(actAppList.get(etcListKey).appList.get(i).escType.equals("explicit"))
					{
						String nodeName = actAppList.get(etcListKey).appList.get(i).appName;
						for(int j = actInsts.numAttributes() - 2; j >= 0; j --)
							if(actInsts.attribute(j).name().contains(nodeName))
								actInsts.deleteAttributeAt(j);
					}
			
			}

			setImplictFeature( actInsts,"on",allLength,etcListKey);
			setImplictFeature( actInsts,"off",allLength,etcListKey);
			setImplictFeature( actInsts,"standby",allLength,etcListKey);




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
