package IntelM2M.tmp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import IntelM2M.datastructure.RelationTable;

import weka.classifiers.bayes.net.EditableBayesNet;

public class PrintFunction {
	
	public void writeAATable(BufferedWriter writer, String room, Map<String, RelationTable> actMap){
		try{
			Set<String> acts = actMap.keySet();
			for(String act:acts)
			{
				if(act.split("_")[0].equals(room))
				{
					String str = act.split("_")[1] + " " + actMap.get(act).intensity + ", ";
					for(int i = 0; i < actMap.get(act).appList.size(); i ++)
					{
						//str += actMap.get(act).appList.get(i).powerLevel;
						if(i < actMap.get(act).appList.size() - 1)
							str += ", ";
					}
					writer.write(str + "\n");
				}
			}
			writer.flush();
		}catch(IOException e){}
	}
	public void writeTrugh(){
		
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
