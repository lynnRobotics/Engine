package IntelM2M.epcie.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import s2h.platform.node.PlatformMessage;
import s2h.platform.node.PlatformTopic;
import s2h.platform.node.Sendable;
import s2h.platform.support.JsonBuilder;
import s2h.platform.support.MessageUtils;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.bayes.net.EditableBayesNet;
import weka.classifiers.bayes.net.MarginCalculator;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import IntelM2M.algo.Classifier;
import IntelM2M.algo.Prior;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.ExpResult;
import IntelM2M.datastructure.GroupActivity;
import IntelM2M.datastructure.SensorNode;
import IntelM2M.epcie.GaGenerator;


/*No USE*/
/*No USE*/
/*No USE*/
/*No USE*/
public class DbnClassifier implements Classifier  {
     public EditableBayesNet[] classifier;
     /*Threshold for Single TraingData*/
    // static final double inferThreshold=0.7;
     /*noise confidence*/
     static final double inferThreshold=0.9;


  
     /*For Debug*/
     Map <String,String> allActProb = new LinkedHashMap<String,String>();
 
    private ArrayList<String> preInferDBN= new ArrayList<String>();
   

    
    public void buildARModel( boolean rebuild)
    {
    	 Instances insts, attSelected;
    	 AttributeSelection attSelector;
    	 String options = "-Q weka.classifiers.bayes.net.search.global.TAN";
    	 String  output_file_path="./_output_results/";
    	try
    	{
    		String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);   		
    		classifier = new EditableBayesNet[activityList.length];
    		//ETCGenerator etc= new ETCGenerator();
    		for(int index = 0;  index < activityList.length;  index ++){
	    		if(rebuild)
	    		{
	    				    			
	    			insts = new Instances(new FileReader("./_weka_training_data/" + activityList[index]+".arff"));
	    			int i=insts.numAttributes() ;
	    			insts.setClassIndex(insts.numAttributes() - 1); //the position of class in attribute
	    			classifier[index] = new EditableBayesNet(insts);
	    			
	    			////attribute selection
	    			attSelector = new AttributeSelection();
	    			attSelector.setEvaluator(new CfsSubsetEval());
	    			attSelector.setSearch(new BestFirst());
	    			attSelector.SelectAttributes(insts);
	    			attSelected = attSelector.reduceDimensionality(insts);    			
	    			////build
	    			
	    			/*feature selection or not*/
	    			classifier[index].buildClassifier(attSelected);
	    			//classifier[index].buildClassifier(insts);
	    			
	    			
	    			SerializationHelper.write("./_weka_output_data/" + activityList[index] + ".model", classifier);
	    			classifier[index].setOptions(Utils.splitOptions(options));    			
	    			//output
	    			BufferedWriter writer = new BufferedWriter(new FileWriter("./_weka_output_data/selected_" + activityList[index]+".arff"));
	    			writer.write(attSelected.toString());
	    			writer.flush();
	    			writer = new BufferedWriter(new FileWriter("./_weka_output_data/" + activityList[index]+ ".xml"));
	    			writer.write(classifier[index].toXMLBIF03());
	    			writer.flush();
	    			writer.close();
	    		}
	    		else //read models
	    		{
	    			attSelected = new Instances(new FileReader("./_weka_output_data/selected_" + activityList[index]));
	    			attSelected.setClassIndex(attSelected.numAttributes() - 1);
	    			classifier[index] = new EditableBayesNet(attSelected);
	    			classifier[index].buildClassifier(attSelected);
	    		}	    	
	    		//etc.buildETCList(output_file_path + "ETC.txt", activityList[index],classifier[index]); //build etc	    		
	    		System.out.println("train model: " + activityList[index] + ", done!");
    		}
    	}
    	catch(Exception e)
	    {e.printStackTrace();}
    }
    public  EditableBayesNet[] buildARModelwithAllFeature( )
    {
    	 Instances insts;
    	 EditableBayesNet[] classifier=null;
    	try
    	{
    		String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);   		
    		classifier = new EditableBayesNet[activityList.length];
    		//ETCGenerator etc= new ETCGenerator();
    		for(int index = 0;  index < activityList.length;  index ++){
	    				    			
	    			insts = new Instances(new FileReader("./_weka_training_data/" + activityList[index]+".arff"));
	    			int i=insts.numAttributes() ;
	    			insts.setClassIndex(insts.numAttributes() - 1); //the position of class in attribute
	    			classifier[index] = new EditableBayesNet(insts);    			
	    			classifier[index].buildClassifier(insts);
	    				    		   		
	    		System.out.println("train All Feature model: " + activityList[index] + ", done!");
    		}
    	}
    	catch(Exception e)
	    {e.printStackTrace();}
    	return classifier;
    }
    
    public  EditableBayesNet[] buildARModelwithAllFeature(String []activityList,Boolean retrain )
    {
    	 Instances insts;
    	 EditableBayesNet[] classifier=null;
    	try
    	{  		
    		classifier = new EditableBayesNet[activityList.length];
    		//ETCGenerator etc= new ETCGenerator();
    		for(int index = 0;  index < activityList.length;  index ++){
	    				    			
	    			insts = new Instances(new FileReader("./_weka_training_data/" + activityList[index]+".arff"));
	    			int i=insts.numAttributes() ;
	    			insts.setClassIndex(insts.numAttributes() - 1); //the position of class in attribute
	    			classifier[index] = new EditableBayesNet(insts);    			
	    			classifier[index].buildClassifier(insts);
	    				    		   		
	    		System.out.println("train All Feature model: " + activityList[index] + ", done!");
    		}
    	}
    	catch(Exception e)
	    {e.printStackTrace();}
    	return classifier;
    }
    
    static public  EditableBayesNet[] buildARModelwithAllFeatureTmp( )
    {
    	 Instances insts;
    	 EditableBayesNet[] classifier=null;
    	try
    	{
    		String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);   		
    		classifier = new EditableBayesNet[activityList.length];
    		//ETCGenerator etc= new ETCGenerator();
    		for(int index = 0;  index < activityList.length;  index ++){
	    				    			
	    			insts = new Instances(new FileReader("./_weka_training_data/" + activityList[index]+".arff"));
	    			int i=insts.numAttributes() ;
	    			insts.setClassIndex(insts.numAttributes() - 1); //the position of class in attribute
	    			classifier[index] = new EditableBayesNet(insts);    			
	    			classifier[index].buildClassifier(insts);
	    				    		   		
	    		System.out.println("train All Feature model: " + activityList[index] + ", done!");
    		}
    	}
    	catch(Exception e)
	    {e.printStackTrace();}
    	return classifier;
    }
    public void allSetDefaultValue(boolean print)
    {
    	try
    	{
			for(int i = 0; i < classifier.length; i ++)
			{
				for(int j = 0; j < classifier[i].getNrOfNodes() - 1; j ++)
				{
					 String sensorName = classifier[i].getNodeName(j);
					 String sensorValue = setDefaultValue(i, j, sensorName);
					//System.out.println(sensorName+"     "+sensorValue);
					
				}
			}
			/*Print result*/
	       	double[] result;
	       	String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);
	      	try
	    	{
	    		for(int actIndex = 0; actIndex < activityList.length; actIndex ++)
	        	{
	    			String activity = activityList[actIndex].substring(0, activityList[actIndex].length() - 5);
	        		result = classifier[actIndex].getMargin(classifier[actIndex].getNode("class"));
	        		
	        		//System.out.println(activity + ": " + result[1]);
	    			
	        	}
	    	}
	    	catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{e.printStackTrace();}
    	
    }
    
    public String setDefaultValue(int index, int iNode, String nodeName)
    {
    	Map<String, String> sensorState=EnvStructure.sensorState;
    	String sensorValue = "";
    	if(nodeName.contains("TV") || nodeName.contains("kinect") ||nodeName.contains("fan"))
		{
    		classifier[index].setEvidence(iNode, 1);
			if(nodeName.contains("accelerometer"))
			{
				sensorValue = "medium";
				sensorState.put(nodeName, sensorValue);
			}
			else
			{
				sensorValue = "standby";
				sensorState.put(nodeName, sensorValue);
			}
		}
		else if(nodeName.contains("AC"))
		{
			classifier[index].setEvidence(iNode, 2);
			sensorValue = "on";
			sensorState.put(nodeName, sensorValue);
		}
		else
		{
			classifier[index].setEvidence(iNode, 0);
			sensorValue = "off";
			sensorState.put(nodeName, sensorValue);
		}
    	update(classifier[index]);
    	
    	return sensorValue;
    }
    
    public void inference(PlatformMessage message, Sendable sender){
		
		JsonBuilder json = MessageUtils.jsonBuilder();
		SensorNode s= getSensorAttribute(message);
		Map<String, SensorNode> sensorList=EnvStructure.sensorList;   	
	
    	if(s.type.equals("reset")){
    		allSetDefaultValue(false); 
    		return;
    	}

	
		if(sensorList.containsKey(s.type + "_" + s.id))
		{
	    

        	ArrayList<String> inferDBN = new ArrayList<String>();
    		
    		System.out.println("receive: {\"" + s.name + "\":\"" + s.discreteValue+ "\"}");
    		try
    		{

				/*Inference */
    			inferDBN = DBNInference(s.name, s.discreteValue);
    			
            			
        		if(inferDBN.size()!=0){
        			System.out.print("DBN infer:");
        			for(int i = 0; i < inferDBN.size(); i ++)
        			{
        				System.out.print(" " + inferDBN.get(i));
        				//json.reset();  
        				//sender.send(json.add("subject","activity").add("value",inferDBN.get(i) ).toJson(),PlatformTopic.CONTEXT);
        			}	
        		}

        		
        		
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    		
    		preInferDBN = (ArrayList<String>) inferDBN.clone();
    		
		}
		
	}


    

    public Map <String,ExpResult> testing(GaGenerator GA,String testingDataPath,String resultPath){
    	try {
			BufferedReader reader = new BufferedReader(new FileReader(testingDataPath));
			FileWriter writer = new FileWriter(new File(resultPath));
			String read=null;
			Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
			ArrayList<String> activityList=EnvStructure.activityList;
			
			/*precision and recall for one activity*/
			Map <String,ExpResult> expResult = new  LinkedHashMap<String,ExpResult>();
			for(int i=0;i<activityList.size();i++){
				ExpResult r= new ExpResult();
				expResult.put(activityList.get(i), r);
			}
			/*precision and recall for GA*/
			Map <String,ExpResult> GAexpResult = new  LinkedHashMap<String,ExpResult>();
			Set<String> GIDS=GA.gaList.keySet();
			for(String str:GIDS){
				ExpResult r=new ExpResult();
				GAexpResult.put(str, r);
			}
			
			/*慧文 GA 算法*/
			Map<String,ExpResult> GAexpResultForKerropi = new LinkedHashMap<String,ExpResult>();
			for(String str:GIDS){
				ExpResult r=new ExpResult();
				GAexpResultForKerropi.put(str, r);
			}
			int count=0;
			
			String preRead="";
			while((read = reader.readLine()) != null)
			{
				
				/*兩種testing 方式*/
				/*第一種*/
				//System.out.println();
//				String []tmpStr=read.split("#");
//				if(tmpStr[1].equals(preRead)){
//					continue;
//				}
//				preRead=tmpStr[1];
				/*第二種*/
//				if(read.equals(preRead)){
//					continue;
//				}
//				preRead=read;
//				
				
				//allSetDefaultValue(false);
				count++;
				String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
				/*initial */
				Map <String,Boolean> inferResult=new LinkedHashMap<String,Boolean>();
				Map <String,Boolean> groundTruth=new LinkedHashMap<String,Boolean>();
				for(int i=0;i<activityList.size();i++){
					inferResult.put(activityList.get(i), false);
					groundTruth.put(activityList.get(i), false);
				}
				/*initila GA*/
				Map <String,Boolean> GAinferResult=new LinkedHashMap<String,Boolean>();
				Map <String,Boolean> GAgroundTruth=new LinkedHashMap<String,Boolean>();
				for(String str:GIDS){
					GAinferResult.put(str, false);
					GAgroundTruth.put(str, false);
				}
				String[] split = read.split("#");
				String []sensorContext=split[0].split(" ");
				ArrayList<String> rawFromDBN = new ArrayList<String>();
				ArrayList<String> inferDBN = new ArrayList<String>();
				Map<String,String> probDBN = new LinkedHashMap<String,String>();
				/*inference*/
				for(int i=0 ;i<sensorContext.length;i++){
					SensorNode s= new SensorNode(sensorName[i],sensorContext[i]);
					rawFromDBN = DBNInference(s.name, s.discreteValue);
				}
				int humanNumber= split[1].split(" ").length;
				if(rawFromDBN.size()!=0){
					/*prior Knowledge 處理*/
					
					rawFromDBN= Prior.priorForInference(rawFromDBN,humanNumber);
				
					for(String str:rawFromDBN){
						String []splitActPb=str.split(" ");
						inferDBN.add(splitActPb[0]);
						probDBN.put(splitActPb[0], splitActPb[1]);
					}
				
					/*record result*/
					
					for(String str:inferDBN){
						inferResult.put(str, true);
					}
				
				}

				
				if(inferDBN.size()!=0){
					writer.write("DBN infer:");
        			for(int i = 0; i < inferDBN.size(); i ++)
        			{

        				writer.write(" " + inferDBN.get(i)+" "+probDBN.get(inferDBN.get(i)));
        				

        			}
        			/*GA infer result*/
        			writer.write(" | ");
        			Set<String> keys=GAinferResult.keySet();
        			for(String str:keys){
        				if(GAinferResult.get(str)==true){
        					writer.write(" "+str);
        				}
        			}
					
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
				
				writer.write(" truth:");
				for(String str:truth){
					String truthProb=allActProb.get(str);
					writer.write(" "+str+" "+truthProb);
				}
				
				//writer.write("  truth: "+ split[1]);
				writer.flush();
				
				/*record tp tn fp fn*/
				for(String str:activityList){
					if(groundTruth.get(str)==true && inferResult.get(str)==true){
						ExpResult r=expResult.get(str);
						r.tp+=1;
						expResult.put(str,r);
					}else if(groundTruth.get(str)==false && inferResult.get(str)==true){
						
						ExpResult r=expResult.get(str);
						r.fp+=1;
						expResult.put(str,r);
					}else if(groundTruth.get(str)==true && inferResult.get(str)==false){
						ExpResult r=expResult.get(str);
						r.fn+=1;
						expResult.put(str,r);
					}else if(groundTruth.get(str)==false && inferResult.get(str)==false){
						ExpResult r=expResult.get(str);
						r.tn+=1;
						expResult.put(str,r);
					}
				}

				
				writer.write("\r\n");
				writer.flush();

			}
			/*write result*/
			writer.write("####################\r\n\r\n");
			for(String str:activityList){
				double precision= expResult.get(str).tp /(expResult.get(str).tp+expResult.get(str).fp);
				double recall= expResult.get(str).tp /(expResult.get(str).tp+expResult.get(str).fn);
				
				writer.write(str+": Precision="+precision+" Recall="+recall+"\r\n");
				writer.flush();
			}
		
			/*return test result*/

			return expResult;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	
    	
    	
    }
    double lastConfidence=0;
	public ArrayList<String> DBNInference(String sensorName, String sensorValue){
		double[] result;
		ArrayList<String> strArray = new ArrayList<String>();
		String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);
		boolean[] actPreState=EnvStructure.actPreState;
		
		set(sensorName, sensorValue);// update model 
		try
		{
			for(int actIndex = 0; actIndex < activityList.length; actIndex ++)
	    	{
				String activity = activityList[actIndex].substring(0, activityList[actIndex].length() );
	    		result = classifier[actIndex].getMargin(classifier[actIndex].getNode("class"));
		
				/*加上 Prior Knowledge*/

	    		if(result[1] >= inferThreshold) //activity exist 			
				{
	
					String prob=Double.toString(result[1]);
					actPreState[actIndex] = true;
					strArray.add(activity+" "+prob);
					
					
						//getSender().send(json.add("subject","activity").add("value",activity ).add("prob",prob).toJson(), PlatformTopic.CONTEXT);
						
					
				}
				else if(result[1] < inferThreshold)
				{
					actPreState[actIndex] = false;
				}
	    		allActProb.put(activity, Double.toString(result[1]));
//	    	if(activity.equals("WatchingTV")){
//    			System.out.println("WatchingTV->Sensor:"+sensorName+" value:"+sensorValue+" prob:"+Double.toString(result[1]));
//    			System.out.println("diff:"+(result[1]-lastConfidence));
//    			lastConfidence=result[1];
//    		}
//	    		if(activity.equals("ComeBack")){
//	    			System.out.println("ComeBack->Sensor:"+sensorName+" value:"+sensorValue+" prob:"+Double.toString(result[1]));
//	    		}
//	    		if(activity.equals("GoOut")){
//	    			System.out.println("GoOut->Sensor:"+sensorName+" value:"+sensorValue+" prob:"+Double.toString(result[1]));
//	    		}
//	    		if(activity.equals("Chatting")){
//	    			//System.out.println("Sensor:"+sensorName+" value"+sensorValue+" prob:"+Double.toString(result[1]));
//	    			
//	    		//	System.out.println("Chatting!!!");
//	    			for (int i = 0; i <classifier[actIndex].getNrOfNodes()-1 ; i ++) 
//	    			{
//	    				String node=classifier[actIndex].getNodeName(i);
//	    				//System.out.println(node);
//	    				File dir = new File("./_weka_training_data");
//	    				Instances insts = new Instances(new FileReader("./_weka_training_data/" + dir.list()[0]));
//	    				int stateNum = insts.attribute(node).numValues();
//	    				
//	    				int index = -1;
//	    				double max_pe = 0.0, pe = 0.0;
//	    				for(int j = 0; j < stateNum; j ++)
//	    				{
//	    					pe = classifier[actIndex].getProbability(i, 1, j);
//	    					if((j == 0) || (pe > max_pe))
//	    					{
//	    						max_pe = pe;
//	    						index = j;
//	    					}	
//	    				}
//	    				String nodeValue = classifier[actIndex].getNodeValue(i, index);
//	    				//System.out.println(nodeValue+" "+max_pe);
//	    			}
//	    		}
	    		
	    	}
			//System.out.println();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
		return strArray;
	}

	private void set(String sensorName, String sensorValue){
		try
		{
		    int attIndex = 0, iValue = getValue(sensorName, sensorValue);
			for(int i = 0; i < classifier.length; i ++)
			{
				for(int j = 0; j < classifier[i].getNrOfNodes(); j ++)
				{
					if(sensorName.equals(classifier[i].getNodeName(j)))
					{
						attIndex = classifier[i].getNode(sensorName);
						classifier[i].setEvidence(attIndex, iValue);
						
						update(classifier[i]);


						
						break;
					}
				}
			}
		}
		catch(Exception e)
		{e.printStackTrace();}
	}

	public void update(EditableBayesNet bayesNet)
    {
        try
        {
        	MarginCalculator mc = new MarginCalculator();
        	mc.calcMargins(bayesNet);
        	//SerializedObject so = new SerializedObject(mc);
        	//MarginCalculator mcWithEvidence = (MarginCalculator) so.getObject();

           	
        	for (int iNode = 0; iNode < bayesNet.getNrOfNodes(); iNode++)
        	{
        		if (bayesNet.getEvidence(iNode) >= 0)
        		{

           			mc.setEvidence(iNode, bayesNet.getEvidence(iNode));

        			//mcWithEvidence.setEvidence(iNode, bayesNet.getEvidence(iNode));
        		}
        	}
            
        	
        	for (int iNode = 0; iNode < bayesNet.getNrOfNodes(); iNode++)
        	{
        		
        		bayesNet.setMargin(iNode, mc.getMargin(iNode));

        	}
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }
    

    
    public SensorNode getSensorAttribute(PlatformMessage message){
    	Map<String, SensorNode> sensorList=EnvStructure.sensorList;
    	SensorNode s= new SensorNode();	
    	s.type = extractValue(message, "subject");
    	s.id = extractValue(message, "id");
    	s.name=sensorList.get(s.type + "_" + s.id).name;
    	if(s.type.equals("socketmeter")){
    		s.rawValue=Double.parseDouble(extractValue(message, "ampere"));
    	}
    	else{
    		s.rawValue=Double.parseDouble(extractValue(message, "value"));
    	}
      	System.out.println("----------------------------------------");
		System.out.println("receive: {\"" + s.type + "_" + s.id + "\":\"" + s.rawValue + "\"}");
	

		/**/
		if(sensorList.containsKey(s.type + "_" + s.id))
		{
			double[] thre = sensorList.get(s.name).threshold;
			String[] status = sensorList.get(s.name).status;
	
			for(int index = 0; index < thre.length; index ++)
			{
				if(s.rawValue < thre[index])
				{
					s.discreteValue = status[index];
					break;
				}
				else if(index + 1 == thre.length )
				{
					s.discreteValue = status[index + 1];
					break;
				}
			}
		}
		
		return s;
    	
    }
    private String extractValue(PlatformMessage message, String key)
    {
    	String value = MessageUtils.get(message.getContent(), key);
        
        return value;
    }

    /*DBNclaasifier*/
    public int getValue(String sensorName, String sensorValue)
    {
    	int iValue = -1;
    	
    	if(sensorName.contains("current"))
		{
			if(sensorValue.equals("off") || sensorValue.equals("Off"))
				iValue = 0;
			else if(sensorValue.equals("standby") || sensorValue.equals("Standby"))
				iValue = 1;
			else if(sensorValue.equals("on") || sensorValue.equals("On"))
				iValue = 2;
		}
		else if(sensorName.contains("temperature") || sensorName.contains("humidity"))
		{
			if(sensorValue.equals("low") || sensorValue.equals("Low")) //higher temperature
				iValue = 0;
			else if(sensorValue.equals("high") || sensorValue.equals("High"))
				iValue = 1;
		}
		else if(sensorName.equals("accelerometer"))
		{
			if(sensorValue.equals("low") || sensorValue.equals("Low"))
				iValue = 0;
			else if(sensorValue.equals("medium") || sensorValue.equals("Medium")) //higher temperature
				iValue = 1;
			else if(sensorValue.equals("high") || sensorValue.equals("High"))
				iValue = 2;
		}
		else if(sensorName.equals("audio_bathroom") || sensorName.equals("audio_kitchen"))
		{
			if(sensorValue.equals("off") || sensorValue.equals("Off"))
				iValue = 0;
			else if(sensorValue.equals("little") || sensorValue.equals("Little")) //higher temperature
				iValue = 1;
			else if(sensorValue.equals("loud") || sensorValue.equals("Loud"))
				iValue = 2;
		}
		else if(sensorName.contains("PIR") || sensorName.contains("camera") || sensorName.contains("light") || sensorName.contains("audio"))
		{
			if(sensorValue.equals("off") || sensorValue.equals("Off"))
				iValue = 0;
			else if(sensorValue.equals("on") || sensorValue.equals("On")) //higher temperature
				iValue = 1;
		}
		else if(sensorName.contains("switch")){
			if(sensorValue.equals("off") || sensorValue.equals("Off"))
				iValue = 1;
			else if(sensorValue.equals("on") || sensorValue.equals("On")) //higher temperature
				iValue = 0;
			
		}
		
    	
    	return iValue;
    }
}

