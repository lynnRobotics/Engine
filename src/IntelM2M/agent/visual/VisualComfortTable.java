package IntelM2M.agent.visual;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.RelationTable;

public class VisualComfortTable {
	ArrayList<String> appNameList; // All of light_location
	Map<String,Double> luxTable;   // e.g., <1-1, 50> 
	
	/* Set visual comfort table */
	public VisualComfortTable(String[] appliances, String[] relation){
		appNameList = new ArrayList<String>();
		
		for(String str : appliances){
			appNameList.add(str);
		}
		luxTable = new LinkedHashMap<String, Double>();
		
		for(String str : relation){
			str = str.trim();
			if(!str.equals("")){
				String[] split = str.split("=");
				String key = split[0].trim();
				String tmp = split[1].trim();
				Double lux = Double.parseDouble(tmp.split(" ")[0]);
				luxTable.put(key, lux);
			}
		}
	}
	
	public double getLuxFromTable(ArrayList<AppNode>  appList){
		/* Get key for lux table from appList */
		try {
			String key = "";
		
			for(int i = 0; i < appList.size(); i++){
				AppNode app = appList.get(i);
				if(app.envContext.equals("off") || app.envContext.equals("standby")){	
				}
				else{
					if(!key.equals("")){
						key += "+";
					}
					String switchNum = app.envContext.split("_")[1];
					key = key + Integer.toString(appNameList.indexOf(app.appName) + 1) + "_" + switchNum;
				} 
			}
			double lux = 0;
			if(key.equals("")){
				lux = 0;
			}else{
				if(!luxTable.containsKey(key)){
					/*for debug*/
					int aa = 0;
					aa++;
				}else{
					lux = luxTable.get(key);
				}
			}
			return lux;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	}
