package IntelM2M.tmp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddCluster;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.RelationTable;


public class KmCluster {
	
	public Map<String, ArrayList<clusterNode>> clusterArray = new LinkedHashMap<String, ArrayList<clusterNode>>();; // cluster result
	
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
	   
	   
    public void runClustering(int clusterNumber)
    {
    	//PrintFunction out=new PrintFunction();
    	HashSet<String> roomList=EnvStructure.roomList;
    	Map<String, RelationTable> etcList=EnvStructure.actAppList;
    	for(String room:roomList){
    		try
	    	{
    			BufferedWriter writer = new BufferedWriter(new FileWriter("./_weka_output_data/" + room + ".arff"));
    			writeArff(writer, room, etcList);
    			
	    		File f = new File("./_weka_output_data/" + room + ".arff");
	        	Instances train = new Instances(new BufferedReader(new FileReader(f)));
	        	AddCluster ac = new AddCluster();  
	        	SimpleKMeans skm = new SimpleKMeans(); 
	        	skm.setNumClusters(clusterNumber); // 設定用 K-Means 要分成 n 群 
	        	ac.setClusterer(skm);  
	        	ac.setInputFormat(train);  // 指定輸入資料 
	        	Instances CI = Filter.useFilter(train, ac); // 執行分群演算法 
	    		Instances clusterCenter = skm.getClusterCentroids();
	    		
	    		//output
	    		 writer = new BufferedWriter(new FileWriter("./_weka_output_data/" + room + "_clustered.arff"));
	    		writer.write(CI.toString());
	    		writer.flush();
	    		writer = new BufferedWriter(new FileWriter("./_weka_output_data/" + room + "_centroids.arff"));
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
    
	public void writeArff(BufferedWriter writer, String room, Map<String, RelationTable> actMap){
		ArrayList<String> actCandidate = new ArrayList<String>();
		ArrayList<String> content = new ArrayList<String>();
		boolean initial = true;
		
		try{
			writer.write("@relation " + room + "\n\n");
			writer.write("@attribute intensity NUMERIC\n");
		
			Set<String> acts = actMap.keySet();
			for(String act:acts)
			{
				if(act.split("_")[0].equals(room))
				{
					actCandidate.add(act.split("_")[1]);
					String str = Double.toString(actMap.get(act).intensity);
//					for(int i = 0; i < actMap.get(act).appList.size(); i ++)
//						str += "," + actMap.get(act).appList.get(i).powerLevel;
					
					str += "," + act.split("_")[1];
					content.add(str);
					
					if(initial)
					{
						initial = false;
						for(int i = 0; i < actMap.get(act).appList.size(); i ++)
							writer.write("@attribute " + actMap.get(act).appList.get(i).appName + " NUMERIC\n");
					}
				}
			}
			writer.write("@attribute class {");
			for(int i = 0; i < actCandidate.size(); i ++)
			{
				writer.write(actCandidate.get(i));
				if(i < actCandidate.size() - 1)
					writer.write(",");
				else
					writer.write("}");
			}
			writer.write("\n\n");
			writer.write("@data\n");
			for(int i = 0; i < content.size(); i ++)
				writer.write(content.get(i) + "\n");
			writer.flush();
			
		}catch(IOException e){}
		
		
	}
}
