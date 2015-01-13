package IntelM2M.algo;

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

/*Mao Yuan Weng 2012/01*/
public class NewKmCluster {

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
	   
	   
    public ArrayList<clusterNode> runClustering(int clusterNumber)
    {
    	//PrintFunction out=new PrintFunction();

    		try
	    	{  			
	    		File f = new File("./_weka_training_data/cluster.arff");
	        	Instances train = new Instances(new BufferedReader(new FileReader(f)));
	        	AddCluster ac = new AddCluster();  
	        	SimpleKMeans skm = new SimpleKMeans(); 
	        	skm.setNumClusters(clusterNumber); // 設定用 K-Means 要分成 n 群 
	        	ac.setClusterer(skm);  
	        	ac.setInputFormat(train);  // 指定輸入資料 
	        	Instances CI = Filter.useFilter(train, ac); // 執行分群演算法 
	    		Instances clusterCenter = skm.getClusterCentroids();
	    		

	    		
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
	    		return arr;
	    	}
	    	catch(Exception e)
		    {e.printStackTrace();}
	    	return null;
	    	
    	
    }
    

}
