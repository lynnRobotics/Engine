
package IntelM2M.epcie.erc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import weka.classifiers.bayes.net.EditableBayesNet;
import weka.core.Instances;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.RelationTable;
import IntelM2M.epcie.classifier.DbnClassifier;

public class EtcGenerator
{
	
 	public Map<String, RelationTable> actAppList; //< roomName_activity , relationtable> 儲存該room對應的電器與電器資訊
	public Instances insts;
	
	public EtcGenerator()
	{
		try{
			File dir = new File("./_weka_training_data");
			insts = new Instances(new FileReader("./_weka_training_data/" + dir.list()[0]));

		}catch(Exception ex){}
		actAppList = EnvStructure.actAppList;	

	}
	
	public void buildAllETC(EditableBayesNet[] classifier,String etcPath){
		try{
			FileWriter writer = new FileWriter(new File(etcPath), false);
			writer.flush();
			writer.close();
		}catch(Exception ex){}
		
		String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]); 
		String  output_file_path="./_output_results/";
		
		for(int index = 0;  index < activityList.length;  index ++){
    	    	
    		buildETCList(etcPath, activityList[index],classifier[index]); //build etc	    		
    		System.out.println("build etc: " + activityList[index].substring(0, activityList[index].length() ) + ", done!");
		}
	}
	
	private double calDistanceForClassifier(EditableBayesNet classifier,EditableBayesNet classifier2){
		 HashSet<String> nodeList = new HashSet<String>();
		
		/*取兩個classifier node的聯集*/
		for(int i=0;i<classifier.getNrOfNodes()-1;i++){
			String node=classifier.getNodeName(i);
			nodeList.add(node);
		}
		for(int i=0;i<classifier2.getNrOfNodes()-1;i++){
			String node=classifier2.getNodeName(i);
			nodeList.add(node);
		}
		/*計算每個node的distance*/
	
		double totalDist=0;
		for(String node:nodeList){
	
			int stateNum = insts.attribute(node).numValues();
			for(int k=0;k<stateNum;k++){
				int iNode1=classifier.getNode2(node);
				/*get probability if node exist*/
				double pe1;
				if(iNode1==-1){
					pe1=0;
				}else{
					pe1=classifier.getProbability(iNode1, 1, k);
				}
				int iNode2=classifier2.getNode2(node);
				double pe2;
				if(iNode2==-1){
					pe2=0;
				}else{
					pe2=classifier2.getProbability(iNode2, 1, k);
				}
				totalDist+=Math.pow(pe1-pe2,2);
			}

		}
		Math.pow(totalDist,0.5);
		return totalDist;
	}

	public void buildSMatrix(EditableBayesNet[] classifiers,String smPath){
		
		String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);
		double sMatrix [][]=new double[activityList.length][activityList.length];
		for(int i=0; i<activityList.length;i++){
			for(int i2=0;i2<activityList.length;i2++){
				EditableBayesNet classifier=classifiers[i];
				String activity=activityList[i];
				EditableBayesNet classifier2=classifiers[i2];
				String activity2=activityList[i2];
				
				double dis=calDistanceForClassifier(classifier,classifier2);
				sMatrix[i][i2]=dis;
			}
		}
		/*print result*/
		try {
			FileWriter  writer = new FileWriter(new File(smPath), false);
			for(int i=0; i<activityList.length;i++){
				for(int j=0; j<activityList.length;j++){
					writer.write(sMatrix[i][j]+" ");
				}
				writer.write("\r\n");
				writer.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public void buildSMatrix(String smPath){
		EditableBayesNet[] classifiers=DbnClassifier.buildARModelwithAllFeatureTmp( );
		String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);
		double sMatrix [][]=new double[activityList.length][activityList.length];
		for(int i=0; i<activityList.length;i++){
			for(int i2=0;i2<activityList.length;i2++){
				EditableBayesNet classifier=classifiers[i];
				String activity=activityList[i];
				EditableBayesNet classifier2=classifiers[i2];
				String activity2=activityList[i2];
				
				double dis=calDistanceForClassifier(classifier,classifier2);
				sMatrix[i][i2]=dis;
			}
		}
		/*print result*/
		try {
			FileWriter  writer = new FileWriter(new File(smPath), false);
			for(int i=0; i<activityList.length;i++){
				for(int j=0; j<activityList.length;j++){
					writer.write(sMatrix[i][j]+" ");
				}
				writer.write("\r\n");
				writer.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
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
				ArrayList<AppNode> appList=actAppList.get(etcListKey).appList;
				
				
				for(int j = 0; j < appList.size(); j ++){
					if(!node.equals(appList.get(j).appName)) //explicit
					{

						
						
					}else if(j < appList.size())
					{
						//ArrayList<AppNode> appList= actAppList.get(etcListKey).appList;
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
			int aa=0;
			aa++;
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
			
			//calculate the mutual relationship
			int[] mutualLength = new int[actInsts.numAttributes()-1]; //initial value: 0
			for(int i = 0; i < actInsts.numInstances(); i ++)
			{
				for(int j = 0; j < actInsts.numAttributes() - 1; j ++)
				{
					// value "on" or heater "standby"
					if(actInsts.instance(i).stringValue(j).equals("on") || (actInsts.attribute(j).name().contains("waterHeater") && actInsts.instance(i).stringValue(j).equals("standby")))
						mutualLength[j] ++;
				}
			}
			for(int i = 0; i < actInsts.numAttributes()-1; i ++)
			{
				if(((double)mutualLength[i]/(double)allLength) > 0.4)
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
						actAppList.get(etcListKey).appList.get(j).state = "on";
						actAppList.get(etcListKey).appList.get(j).confidence = roundTwoDecimals((double)mutualLength[i]/(double)allLength);

						//actAppList.get(etcListKey).setAppPower(j,"on");
						
					}
					

				}
			}
		}catch(IOException e){e.printStackTrace();}
	}
	
	public void buildETCList(String file_path, String activity, EditableBayesNet classifier) {
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
			writer.write("Apps (ex and im): ");
			for(int i = 0; i < actAppList.get(etcListKey).appList.size(); i ++)
			{
				if(actAppList.get(etcListKey).appList.get(i).escType.equals("explicit") || actAppList.get(etcListKey).appList.get(i).escType.equals("implicit"))
				{
					writer.write(actAppList.get(etcListKey).appList.get(i).appName + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).escType + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).state + "|");
					writer.write(actAppList.get(etcListKey).appList.get(i).confidence + "|");
					
					//writer.write(actAppList.get(etcListKey).appList.get(i).powerLevel + " ");
				}
			}
			
			writer.write("\n\n");
			writer.flush();
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
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
