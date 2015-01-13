package IntelM2M.tmp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import s2h.platform.annotation.MessageFrom;
import s2h.platform.annotation.UPnP;
import s2h.platform.node.LogicNode;
import s2h.platform.node.NodeRunner;
import s2h.platform.node.PlatformMessage;
import s2h.platform.node.PlatformTopic;
import s2h.platform.support.JsonBuilder;
import s2h.platform.support.MessageUtils;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.bayes.net.EditableBayesNet;
import weka.classifiers.bayes.net.MarginCalculator;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddCluster;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.RelationTable;
import IntelM2M.datastructure.SensorNode;
import IntelM2M.epcie.erc.EtcGenerator;
/**
 * InferenceEngine
 * @author Mao  (2011.05)
 */
@MessageFrom(PlatformTopic.RAW_DATA)
@UPnP

public class EPC_InferenceEngine extends LogicNode
{
    private Log log = LogFactory.getLog(EPC_InferenceEngine.class.getName());
    private final static JsonBuilder json = MessageUtils.jsonBuilder();
    private String  output_file_path="./_output_results/";
   
    /*For Weka*/
    /*這邊用static class variable包起*/
    private EditableBayesNet[] classifier;
    private Instances insts, attSelected;
    private AttributeSelection attSelector;
    private String options = "-Q weka.classifiers.bayes.net.search.global.TAN";
    /******************/
    /*DataStructure*/
   
    EnvStructure d=new EnvStructure();
 
    private Map<String, ArrayList<clusterNode>> clusterArray = new LinkedHashMap<String, ArrayList<clusterNode>>();; // cluster result
    private ArrayList<String> preInferDBN= new ArrayList<String>();
  

    /*KMcluster*/
    class clusterNode
    {
		clusterNode()
		{
			centroid = new ArrayList<String>();
			clusterMember = new ArrayList<String>();
		}

		ArrayList<String> centroid;
		ArrayList<String> clusterMember;
		double diff;
	}
    public EPC_InferenceEngine (int n){}
    public EPC_InferenceEngine()
    {
    	super();
    	/*
    	text2Arff cvt = new text2Arff();
    	cvt.convert();
 	    */
    	
		/*build model*/
    	
		buildARModel(true); //build AR model	and etc
	    allSetDefaultValue(true); //initial model    
	    /*clustering For each room's activity*/
		runClustering();

		    
    }
    
    public static void main(String[] args)
    {
        new NodeRunner(EPC_InferenceEngine.class).execute();
    }
    
    /*DBNclaasifier*/
    private void buildARModel( boolean rebuild)
    {
    	try
    	{
    		String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);   
    		classifier = new EditableBayesNet[activityList.length];
    		EtcGenerator etc= new EtcGenerator();
    		for(int index = 0;  index < activityList.length;  index ++){
	    		if(rebuild)
	    		{
	    				    			
	    			insts = new Instances(new FileReader("./_weka_training_data/" + activityList[index]+".arff"));
	    			int aa=insts.numAttributes();
	    			
	    			insts.setClassIndex(insts.numAttributes() - 1); //the position of class in attribute
	    			classifier[index] = new EditableBayesNet(insts);
	    			
	    			////attribute selection
	    			attSelector = new AttributeSelection();
	    			attSelector.setEvaluator(new CfsSubsetEval());
	    			attSelector.setSearch(new BestFirst());
	    			attSelector.SelectAttributes(insts);
	    			attSelected = attSelector.reduceDimensionality(insts);    			
	    			////build
	    			classifier[index].buildClassifier(attSelected);
	    			SerializationHelper.write("./_weka_output_data/" + activityList[index].substring(0, activityList[index].length() - 5) + ".model", classifier);
	    			classifier[index].setOptions(Utils.splitOptions(options));    			
	    			//output
	    			BufferedWriter writer = new BufferedWriter(new FileWriter("./_weka_output_data/selected_" + activityList[index]));
	    			writer.write(attSelected.toString());
	    			writer.flush();
	    			writer = new BufferedWriter(new FileWriter("./_weka_output_data/" + activityList[index].substring(0, activityList[index].length() - 5) + ".xml"));
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
	    		etc.buildETCList(output_file_path + "ETC.txt", activityList[index],classifier[index]); //build etc	    		
	    		System.out.println("train model: " + activityList[index].substring(0, activityList[index].length() - 5) + ", done!");
    		}
    	}
    	catch(Exception e)
	    {e.printStackTrace();}
    }
    /*KMcluster*/
    private void runClustering()
    {
    	PrintFunction out=new PrintFunction();
    	HashSet<String> roomList=EnvStructure.roomList;
    	Map<String, RelationTable> etcList=EnvStructure.actAppList;
    	for(String room:roomList){
    		try
	    	{
    			BufferedWriter writer = new BufferedWriter(new FileWriter("./_cluster_data/" + room + ".arff"));
    			out.writeArff(writer, room, etcList);
    			
	    		File f = new File("./_cluster_data/" + room + ".arff");
	        	Instances train = new Instances(new BufferedReader(new FileReader(f)));
	        	AddCluster ac = new AddCluster();  
	        	SimpleKMeans skm = new SimpleKMeans(); 
	        	skm.setNumClusters(3); // 設定用 K-Means 要分成 3 群 
	        	ac.setClusterer(skm);  
	        	ac.setInputFormat(train);  // 指定輸入資料 
	        	Instances CI = Filter.useFilter(train, ac); // 執行分群演算法 
	    		Instances clusterCenter = skm.getClusterCentroids();
	    		
	    		//output
	    		 writer = new BufferedWriter(new FileWriter("./_cluster_data/" + room + "_clustered.arff"));
	    		writer.write(CI.toString());
	    		writer.flush();
	    		writer = new BufferedWriter(new FileWriter("./_cluster_data/" + room + "_centroids.arff"));
	    		writer.write(clusterCenter.toString());
	    		writer.flush();
	    		writer.close();
	    		
	    		ArrayList<clusterNode> arr = new ArrayList<clusterNode>();
	    		for(int i = 0; i < clusterCenter.numInstances(); i++) //no. of centers
	    		{
	    			clusterNode cNode = new clusterNode();
	    			arr.add(cNode);
	    			
	    			int len = clusterCenter.instance(i).numAttributes(); /*number of attribute(dimension+ans)*/
	    			/*record centroid for cluster i*/
	       			for(int j = 0; j < len-1; j++) //len-1: the class
	       			{
	       				String tmp=clusterCenter.instance(i).toString(j);
	       				arr.get(i).centroid.add(tmp);
	       			}
	    		}
	    		/*record member for cluster i*/
	    		for(int i=0; i<CI.numInstances(); i++)
	    		{ 
	    			int index = (int)CI.instance(i).value(CI.instance(i).numAttributes()-1);
	    			String actName= CI.instance(i).toString( (CI.instance(i).attribute(CI.instance(i).numAttributes()-2)));
	    			arr.get(index).clusterMember.add(actName);
	    		}
	    		clusterArray.put(room, arr);
	    	}
	    	catch(Exception e)
		    {e.printStackTrace();}
    	}
    }
    
    /*DBNclaasifier*/
    public void allSetDefaultValue(boolean print)
    {
    	try
    	{
		    String sensorName = "", sensorValue = "";
			for(int i = 0; i < classifier.length; i ++)
			{
				for(int j = 0; j < classifier[i].getNrOfNodes() - 1; j ++)
				{
					sensorName = classifier[i].getNodeName(j);
					sensorValue = setDefaultValue(i, j, sensorName);
					System.out.println(sensorName+"     "+sensorValue);
					
				}
			}
			DBNPrint();
		}
		catch(Exception e)
		{e.printStackTrace();}
    	
    }
    /*DBNclaasifier*/
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
   
	public  void sendMQ(JsonBuilder j,String topicString){
		if(topicString.equals("COMMAND")){	
			getSender().send(j.toJson(), PlatformTopic.COMMAND);
		}
		if(topicString.equals("CONTEXT")){
			getSender().send(j.toJson(), PlatformTopic.CONTEXT);
		}

	}

    

    
    @SuppressWarnings("unchecked")
	protected void processMessage(PlatformMessage message)
    {
    	 Map<String, SensorNode> sensorList=EnvStructure.sensorList;
    	 Map<String, RelationTable> etcList=EnvStructure.actAppList;
    	 Map<String, String> sensorState=EnvStructure.sensorState;
    	
    	String sensorType = extractValue(message, "subject");
    	String sensorID = extractValue(message, "id");
    	String sensorValueString = "", sensorName = "", sensorContext = "";
    	
    	if(sensorType.equals("socketmeter"))
    		sensorValueString = extractValue(message, "ampere");
   
    	else
    		sensorValueString = extractValue(message, "value");
    	
   
    	
    	double sensorValue = 0.0;
    	if(!sensorValueString.equals(""))
    		sensorValue = Double.parseDouble(sensorValueString);
    	

    	
		//TODO
		//modify, send "camera_livingroom on"
		//set("camera_livingroom", "on");
		//set("PIR_livingroom", "on");
    	
    	if(sensorName.equals("reset"))
    		allSetDefaultValue(false);   	

    	else //real time inference
    	{
    		if(sensorList.containsKey(sensorType + "_" + sensorID))
    		{
    			//preprocessing
        		sensorName = sensorList.get(sensorType + "_" + sensorID).name;
        		double[] thre = sensorList.get(sensorType + "_" + sensorID).threshold;
        		String[] status = sensorList.get(sensorType + "_" + sensorID).status;
        		
        		//find context
        		int index;
        		for(index = 0; index < thre.length; index ++)
        		{
        			if(sensorValue < thre[index])
        			{
        				sensorContext = status[index];
        				break;
        			}
        			else if(index + 1 == thre.length )
        			{
        				sensorContext = status[index + 1];
        				break;
        			}
        		}
        		

        		
        		//infernce
        		Map<String, ArrayList<String>> inferGA, groundTruthGA;
        		inferGA = new LinkedHashMap<String, ArrayList<String>>();
            	groundTruthGA = new LinkedHashMap<String, ArrayList<String>>();
        	    
            	ArrayList<String> inferDBN, groundTruthDBN, tmp;
                inferDBN = new ArrayList<String>();
                groundTruthDBN = new ArrayList<String>();
                tmp = new ArrayList<String>();
        	    
        		
        		System.out.println("receive: {\"" + sensorName + "\":\"" + sensorContext + "\"}");
        		try
        		{
        			BufferedWriter ETC_GA_writer_realtime = new BufferedWriter(new FileWriter(output_file_path + "EC_GA_realtime.txt"));
        			if(    (sensorState.containsKey(sensorName) && !sensorState.get(sensorName).equals(sensorValue) )   ||    (!sensorState.containsKey(sensorName))           )
            		{ //sensor heartbeat
            			inferDBN = DBNInference(sensorName, sensorContext);///////////////////////////
            			
        				boolean the_same = false;
        				if((inferDBN.size() == 0 && preInferDBN.size() == 0)||(inferDBN.size() == 0 && preInferDBN.size() > 0))
        					the_same = true;
        				else if(inferDBN.size() > 0 && preInferDBN.size() > 0)
        				{
        					int equal_to_previous = 0, equal_to_this = 0;
                			for(int i = 0; i < preInferDBN.size(); i ++)
                				if(inferDBN.contains(preInferDBN.get(i)))
                					equal_to_this ++;
                			
                			for(int i = 0; i < inferDBN.size(); i ++)
                				if(preInferDBN.contains(inferDBN.get(i)))
                					equal_to_previous ++;
                			
                			if((inferDBN.size() == equal_to_this) && (preInferDBN.size() == equal_to_previous))
                				the_same = true;
        				}
            			
            			//activity heartbeat, 5 second check once
                		if(true)
        				//if(!the_same)
                		{
                    		Set<String> actRooms = etcList.keySet(); //actRoom: room_act

                    		if(inferDBN.size()!=0)
                    			System.out.print("DBN infer:");
                			for(int i = 0; i < inferDBN.size(); i ++)
                			{

                				System.out.print(" " + inferDBN.get(i));
                				json.reset();
                				// ???
                				
                				//getSender().send(json.add("subject","activity").add("value",inferDBN.get(i) ).toJson(), PlatformTopic.CONTEXT);
                				
  

                			}		
                			System.out.println("\n");
                			
                			////////=====================================================================
                			/////------------------ convert into Group Activity -------------------------
                	    	//record the inferred GA
                	    	inferGA.clear();
                	    	String room_now = "";
                			for(int i = 0; i < inferDBN.size(); i ++)
                	    	{
                	    		String act = inferDBN.get(i), room = "";
                	    		ETC_GA_writer_realtime.write(act + "\n");
                	    		for(String rm:actRooms)
                	    		{
                	    			if(rm.contains(act))
                	    			{
                	    				room = rm.split("_")[0];
                	    				room_now = room;
                	    				break;
                	    			}
                	    		}
                	    		
                	    		ArrayList<clusterNode> cluster_oneRoom = clusterArray.get(room);
                	    		for(int j = 0; j < cluster_oneRoom.size(); j++)
                        	    {
                        	    	for(String str:cluster_oneRoom.get(j).clusterMember)
                        	    	{
                        	    		if(act.equals(str))
                        	    		{
                        	    			if(!inferGA.containsKey(room))
                        	    			{
                        	    				tmp = new ArrayList<String>();
                        	    				tmp.add("cluster" + j);
                        	    				inferGA.put(room, tmp);
                        	    			}
                        	    			else
                        	    				inferGA.get(room).add("cluster" + j);
                        	    		}
                        	    	}
                        	    }
                	    	}
                			
                			//combine ETC for GA
                			Map<String, RelationTable> ETC_of_GroupedActs = new LinkedHashMap<String, RelationTable>();
                			if(!inferGA.isEmpty())
                			{
                				for(int i = 0; i < inferGA.get(room_now).size(); i ++) //i: which group id
                    			{
                    				String mapID = room_now + "_" + inferGA.get(room_now).get(i); //ex: livingroom_cluster1
                    				RelationTable table = new RelationTable();
                    				table.intensity = 0.0;
                    				ETC_of_GroupedActs.put(mapID, table);
                    				
                    				//find the acts in the # group in room_now
                    				int groupNo = Integer.parseInt(inferGA.get(room_now).get(i).substring(7, 8));
                    				ArrayList<String> groupedActs = clusterArray.get(room_now).get(groupNo).clusterMember;
                    				
                    				//find ETC of each acts
                    				for(int j = 0; j < groupedActs.size(); j ++) //j: in this group, which activity
                    				{
                    					String room_act = room_now + "_" + groupedActs.get(j);
                    					//intensity
                    					double intensity_original = ETC_of_GroupedActs.get(mapID).intensity, 
                    					       inensity_toBeAdded = etcList.get(room_act).intensity;
                    					ETC_of_GroupedActs.get(mapID).intensity = intensity_original + inensity_toBeAdded;
                    					
                    					//apps
                    					//AppNode: appName
                    					//         powerType: explicit >> implicit
                    					//         state: on >> standby
                    					//         power & powerLevel: higher one
                    					//         appType, confidence
                    					for(int k = 0; k < etcList.get(room_act).appList.size(); k ++)
                    					{
                    						String now_app = etcList.get(room_act).appList.get(k).appName, powerType = etcList.get(room_act).appList.get(k).escType;
                    						
                    						if(!etcList.get(room_act).appList.get(k).state.equals("off") && (powerType.equals("explicit")||powerType.equals("implicit")))
                    						{
                    							boolean notFound = false;
                    							if(ETC_of_GroupedActs.get(mapID).appList.size() != 0) //find the index of now_app in "ETC_of_GroupedActs"
                    							{
                    								for(int l = 0; l < ETC_of_GroupedActs.get(mapID).appList.size(); l ++)
                        							{
                        								if(now_app.equals(ETC_of_GroupedActs.get(mapID).appList.get(l).appName))//now_app exist
                        								{
                        									if(powerType.equals("explicit") && ETC_of_GroupedActs.get(mapID).appList.get(l).escType.equals("implicit"))
                        									{
                        										ETC_of_GroupedActs.get(mapID).appList.get(l).escType = etcList.get(room_act).appList.get(k).escType;
                        										//ETC_of_GroupedActs.get(mapID).appList.get(l).power = etcList.get(room_act).appList.get(k).power;
                                    							//ETC_of_GroupedActs.get(mapID).appList.get(l).powerLevel = etcList.get(room_act).appList.get(k).powerLevel;
                                    							ETC_of_GroupedActs.get(mapID).appList.get(l).state = etcList.get(room_act).appList.get(k).state;
                        									}
                        									else if(powerType.equals(ETC_of_GroupedActs.get(mapID).appList.get(l).escType)) //both explicit or implicit
                        									{
                        										if(etcList.get(room_act).appList.get(k).state.equals("on") && ETC_of_GroupedActs.get(mapID).appList.get(l).state.equals("standby"))	
                        										{
                        											//ETC_of_GroupedActs.get(mapID).appList.get(l).power = etcList.get(room_act).appList.get(k).power;
                                        							//ETC_of_GroupedActs.get(mapID).appList.get(l).powerLevel = etcList.get(room_act).appList.get(k).powerLevel;
                                        							ETC_of_GroupedActs.get(mapID).appList.get(l).state = etcList.get(room_act).appList.get(k).state;
                        										}
                        									}
                        									break;
                        								}
                        								
                        								if(l == ETC_of_GroupedActs.get(mapID).appList.size() - 1)
                        								{
                        									notFound = true;
                        									break;
                        								}
                        							}
                    							}

                    							if((ETC_of_GroupedActs.get(mapID).appList.size() == 0) || notFound)
                        						{
                    								AppNode app= new AppNode();
                    								app.appName=now_app;
                    								ETC_of_GroupedActs.get(mapID).appList.add(app);
                    								
                        							int arr_size = ETC_of_GroupedActs.get(mapID).appList.size();
                        							
                        							//ETC_of_GroupedActs.get(mapID).appList.get(arr_size - 1).power = etcList.get(room_act).appList.get(k).power;
                        							//ETC_of_GroupedActs.get(mapID).appList.get(arr_size - 1).powerLevel = etcList.get(room_act).appList.get(k).powerLevel;
                        							ETC_of_GroupedActs.get(mapID).appList.get(arr_size - 1).escType = etcList.get(room_act).appList.get(k).escType;
                        							ETC_of_GroupedActs.get(mapID).appList.get(arr_size - 1).state = etcList.get(room_act).appList.get(k).state;
                        						}
                    						}
                    					}
                    				}
                    				ETC_of_GroupedActs.get(mapID).intensity /= groupedActs.size();
                    			}
                    			//print ETC of GA
                    			Set<String> keys = ETC_of_GroupedActs.keySet();
                    			for(String key:keys)
                    			{
                    				RelationTable now_aa_table = ETC_of_GroupedActs.get(key);
                    				ETC_GA_writer_realtime.write(key + "\n");
                    				ETC_GA_writer_realtime.write("intensity: " + now_aa_table.intensity + "\n");
                    				ETC_GA_writer_realtime.write("appliances: \n");
                					for(int i = 0; i < now_aa_table.appList.size(); i ++)
                					{
                						ETC_GA_writer_realtime.write("\t" + now_aa_table.appList.get(i).appName + ": ");
                						//ETC_GA_writer_realtime.write(now_aa_table.appList.get(i).powerType + ", " + now_aa_table.appList.get(i).state
                							//	            + ", " + now_aa_table.appList.get(i).power + ", " + now_aa_table.appList.get(i).powerLevel + "\n");
                					}
                					ETC_GA_writer_realtime.write("\n");
                					ETC_GA_writer_realtime.flush();
                    			}
                			}
                			else
                			{
                				ETC_GA_writer_realtime.write("NA\n\n");
                				ETC_GA_writer_realtime.flush();
                			}
                			
                			//print into file and onto screen
//                			System.out.print("GA infer:");
//                			Set<String>GA_key = inferGA.keySet();
//                			
//                			for(String room : GA_key)
//                			{
//                    			System.out.print("\n\t" + room + ":");
//                				for(int i = 0; i < inferGA.get(room).size(); i ++)
//                				{
//                        			System.out.print(" " + inferGA.get(room).get(i));
//                				}
//                			}
//                			System.out.println("\n");

                			//sleep
                			Thread.sleep(100);
                		}
            		}
        		}
        		catch(Exception e)
        		{
        			e.printStackTrace();
        		}
        		
        		preInferDBN = (ArrayList<String>) inferDBN.clone();
        		sensorState.put(sensorName, sensorContext);
    		}
    	}
    }
    /*DBNclaasifier*/
    public void DBNPrint(){
       	double[] result;
       	String []activityList=(String[])EnvStructure.activityList.toArray(new String[0]);
       	
      	try
    	{
    		for(int actIndex = 0; actIndex < activityList.length; actIndex ++)
        	{
    			String activity = activityList[actIndex].substring(0, activityList[actIndex].length() - 5);
        		result = classifier[actIndex].getMargin(classifier[actIndex].getNode("class"));
        		
        		System.out.println(activity + ": " + result[1]);
    			
        	}
    	}
    	catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    /*DBNclaasifier*/
    public ArrayList<String> DBNInference(String sensorName, String sensorValue){
    	double[] result;
    	ArrayList<String> strArray = new ArrayList<String>();
    	String []activityList=(String[])EnvStructure.activityList.toArray();
    	boolean[] actPreState=EnvStructure.actPreState;
    	
    	set(sensorName, sensorValue);// update model 
    	try
    	{
    		for(int actIndex = 0; actIndex < activityList.length; actIndex ++)
        	{
    			String activity = activityList[actIndex].substring(0, activityList[actIndex].length() - 5);
        		result = classifier[actIndex].getMargin(classifier[actIndex].getNode("class"));
   		
    			//json.reset();
    			if(result[1] >= 0.4) //activity exist 			
    			{

    				actPreState[actIndex] = true;
    				strArray.add(activity);
    				String prob=Double.toString(result[1]);
    				
    				getSender().send(json.add("subject","activity").add("value",activity ).add("prob",prob).toJson(), PlatformTopic.CONTEXT);
    					
    				
    			}
    			else if(result[1] < 0.4)
    			{
    				actPreState[actIndex] = false;
    			}
        	}
    	}
    	catch(Exception e)
		{
			e.printStackTrace();
		}

    	return strArray;
	}
    /*DBNclaasifier*/
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
		else {log.debug("type error");}
    	
    	return iValue;
    }
    /*DBNclaasifier*/
  
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
    
    /*DBNclaasifier*/
    private String extractValue(PlatformMessage message, String key)
    {
    	String value = MessageUtils.get(message.getContent(), key);
        
        return value;
    }

}
