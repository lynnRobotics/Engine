package IntelM2M.datastructure;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import IntelM2M.agent.visual.VisualComfortTable;

/**
 * 
 * Revised by Shu-Fan 2013/11/19
 * 
 */

public class XMLHandler {
	
	/* Assign path of xml file 
	 * BL313env_test7 is the newest version
	 * */
	File xml= new File("./_input_data/BL313/BL313env_test7.xml");
	
	public void XMLHandler(){
	}
	
	/* Get list of activity's name */
	public ArrayList<String> getActList(){
		ArrayList<String> actList = new ArrayList<String>();
		try{
  			SAXReader saxReader = new SAXReader();
  			Document document = saxReader.read(xml);
  			List list = document.selectNodes("/metaData/activityList/type");
  			Iterator iter = list.iterator();
  			while(iter.hasNext()){
  				Element actElement = (Element)iter.next();
  				String activity = actElement.attributeValue("id");
  				actList.add(activity);
  			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return actList;
	}
	
	/* Get <sensorName, all status> */
	public Map<String, ArrayList<String>> getSensorStatus(){
		Map<String, ArrayList<String>> sensorList = new LinkedHashMap<String, ArrayList<String>>();
		
		try{
  			SAXReader saxReader = new SAXReader();
  			Document document = saxReader.read(xml);
  			List list = document.selectNodes("/metaData/sensorList/type");
  			Iterator iter = list.iterator();
  			while(iter.hasNext()){
  				Element typeElement = (Element)iter.next();
  				List list2 = typeElement.selectNodes("sensor");
  				Iterator iter2 = list2.iterator();
  				while(iter2.hasNext()){
  					Element sensorElement = (Element)iter2.next();  					
  					// Sensor name
  					String sensorName = sensorElement.element("name").getText();
  					// All the corresponding status
  					String[] sensorStatus = sensorElement.element("status").getText().split(" ");

  					ArrayList<String> tmp = new ArrayList<String>();
 					for(int i = 0; i < sensorStatus.length; i ++)
 						tmp.add(sensorStatus[i]);
 					sensorList.put(sensorName, tmp);
  				}	
  			}
		} catch(Exception e){
			e.printStackTrace();
		}		
		return sensorList;
	}
	
	/* Get <sensorType_Id, sensor node object(name, status, threshold, switch, switchLux)> */
    public Map<String, SensorNode> getSensorList(){  		 
    	Map<String, SensorNode> sensorList = new LinkedHashMap<String, SensorNode>();
  		try {
  			SAXReader saxReader = new SAXReader();
  			Document document = saxReader.read(xml);
  			List list = document.selectNodes("/metaData/sensorList/type");
  			Iterator iter = list.iterator();
  			while(iter.hasNext()){
  				Element typeElement = (Element)iter.next();
  				// Get sensor type
  				String type = typeElement.attributeValue("id");
  				// Iterator of sensor
  				List list2 = typeElement.selectNodes("sensor");
  				Iterator iter2 = list2.iterator();
  				while(iter2.hasNext()){
  					Element sensorElement = (Element)iter2.next();
  					// Get sensor ID
  					String sensorID = sensorElement.attributeValue("id");
  					// Get name, status, threshold
  					String sensorName = sensorElement.element("name").getText();
  					String[] sensorStatus = sensorElement.element("status").getText().split(" ");
  		           	String[] sensorThreshold = sensorElement.element("threshold").getText().split(" ");
  		           	double[] sensorThres = new double[sensorThreshold.length];
  		           	for(int i = 0; i < sensorThreshold.length; i ++) {
  		           		sensorThres[i] = Double.parseDouble(sensorThreshold[i]);
  		           	}
  		           	
  		           	SensorNode tmp = null;
  		           	// Light sensor, get threshold and corresponding level
  		           	if (sensorElement.element("switch") != null && sensorElement.element("switch_lux") != null) {
  		           		String[] switchLevel = sensorElement.element("switch").getText().split(" ");
  		           		int[] switchLevelInteger = new int[switchLevel.length];
  		           		String[] switchLux = sensorElement.element("switch_lux").getText().split(" ");
  		           		double[] switchLuxDouble = new double[switchLux.length];
  		           		for (int i = 0; i < switchLevel.length; i ++) {
  		           			switchLevelInteger[i] = Integer.parseInt(switchLevel[i]);
  		           			switchLuxDouble[i] = Double.parseDouble(switchLux[i]);
  		           		}
  		           		tmp = new SensorNode(sensorName, type, sensorThres, sensorStatus, switchLevelInteger, switchLuxDouble);
		            }
  		           	else{
  		           		tmp = new SensorNode(sensorName, type, sensorThres, sensorStatus);
  		           	}
  		           	sensorList.put(type + "_" + sensorID, tmp);
  				}
  			} 		 
  		} catch (Exception e){
  			 e.printStackTrace(); 
  		}
  		return sensorList;
  	}
	
    /* Get <sensorType_location(e.g., light_livingroom), appNode(appName, location, comforType)> */
    public Map<String, AppNode> getAppList(){
    	Map<String,AppNode> appList = new LinkedHashMap<String, AppNode>();
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xml);
 			List list = document.selectNodes("/metaData/sensorList/type");
  			Iterator iter = list.iterator();
  			while(iter.hasNext()){
  				Element typeElement = (Element)iter.next();
  				// Get sensor type, e.g. light, switch, current
  				String type = typeElement.attributeValue("id");
 
  				if(type.equals("current") || type.equals("light")){
  	  				List list2 = typeElement.selectNodes("sensor");
  	  				Iterator iter2 = list2.iterator();
  	  				while(iter2.hasNext()){
  	  					Element sensorElement = (Element)iter2.next();
  	  					// Get name, ampere, comfort_type
  	  					String sensorName = sensorElement.element("name").getText();
  	  					String[] ampere = sensorElement.element("ampere").getText().split(" ");
  	  				    String comfortType = sensorElement.element("comfort_type").getText();
  	  				
  	  					AppNode app = new AppNode();
  	  					app.appName = sensorName;
  	  					app.comfortType = comfortType;
  	  					
  	  					// Put sensor status(on, off, standby) and corresponding ampere)
  	  					// If there is a switch tag, set realation between switch and switch_ampere
  	  					if(sensorElement.element("switch") != null){ 
  	  						String [] switchNum = sensorElement.element("switch").getText().split(" ");
  	  						String [] switchAmpere = sensorElement.element("switch_ampere").getText().split(" ");
  	  						// Put (off, ampere_0), (standby, ampere_1)
  	  						if(ampere.length == 2){
  	  							app.ampere.put("off", Double.parseDouble(ampere[0]));
  	  						}
  	  						else if(ampere.length == 3){
  	  	  						app.ampere.put("off", Double.parseDouble(ampere[0]));
  	  	  						app.ampere.put("standby", Double.parseDouble(ampere[1]));
  	  						}
  	  						// Put (on_1, switch_ampere_1), (on_2, switch_ampere_2), ...
  	  						for(int i = 0; i < switchNum.length;i++){
  	  							app.ampere.put("on_" + switchNum[i], Double.parseDouble(switchAmpere[i]));
  	  						}	
  	  					} 
  	  					else{
  	  						if(ampere.length == 2){
  	  							app.ampere.put("off", Double.parseDouble(ampere[0]));
  	  							app.ampere.put("on", Double.parseDouble(ampere[1]));
  	  						}
  	  						else if(ampere.length == 3){
  	  							app.ampere.put("off", Double.parseDouble(ampere[0]));
  	  							app.ampere.put("standby", Double.parseDouble(ampere[1]));
  	  							app.ampere.put("on", Double.parseDouble(ampere[2]));
  	  						}
  	  					}
  	  					appList.put(sensorName, app);
  	  				}
  				}
  			}
  			
  			// Set location information in app for above appList
  			list = document.selectNodes("/metaData/relation/location");
			iter = list.iterator();
		    while(iter.hasNext()){
		    	Element roomElement = (Element)iter.next();
		    	// Get room name, e.g., livingroom, hallway, ...
		    	String roomName = roomElement.attributeValue("id");
		    	   
		    	// If there's no appliance in this location
		    	if (roomElement.element("appliance") == null) continue;
		    	   
		    	// There's at least one appliance
		    	Element appElement = roomElement.element("appliance");
		    	String[] appliances = appElement.getText().split(" ");
		    	   
		    	// If the appliance is in global, location = "global", global = true
		    	if(roomName.equals("global")){
		    		for(String appliance : appliances){
		    			appList.get(appliance).location = roomName;
			    		appList.get(appliance).global = true;
			    	}  
		    	}
		    	else{
		    		for(String appliance : appliances){
		    			AppNode app = appList.get(appliance);
			    		app.location = roomName;
			    		app.global = false;
			    	}  
		    	}
		    }
		} catch(DocumentException e){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		return appList;
    }

    /* Get <location_activity, relationTable(All the AppNode and its intensity)> */
	public Map<String, RelationTable>  getActAppList(){
		 Map<String, RelationTable> tableList = new LinkedHashMap<String, RelationTable>();
		 try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xml);
			List list = document.selectNodes("/metaData/relation/location");
			Iterator iter = list.iterator();
		    while(iter.hasNext()){
		    	Element roomElement = (Element)iter.next();
		        // Get location
		        String roomName = roomElement.attributeValue("id");
		        // Get activity 
		        Element actElement = roomElement.element("activity");
		              
		        // roomName == global
		        if(actElement == null){ 
		        }
		        else{
		        	// Get all activities in activity tag
			        String[] activities = actElement.getText().split(" ");
			        for(String activity : activities){		            	   	            	 	            	   
			        	RelationTable table = new RelationTable();	
			        	// Copy all appNodes in appList to RelationTable
			        	Map<String,AppNode> appList = EnvStructure.appList;
			        	Set<String> sensorSet = appList.keySet();
			        	for(String sensors : sensorSet){
			        		AppNode app = appList.get(sensors);
			        		// Add app into table
			        		AppNode newApp = app.copyAppNode(app);
			        		table.appList.add(newApp);
			        	}
			        	// Get Intensity of activity and store in table
			        	Node node = document.selectSingleNode("/metaData/activityList/type[@id='" + activity + "']/intensity[1]");		            	 
			        	table.intensity = Double.parseDouble(node.getText());
			        	// Add table to tableList
			        	tableList.put(roomName + "_" + activity, table);      		            	   
			        }
		        } 
		    }   
		} catch (Exception e){
			e.printStackTrace();
		}
		return tableList;
	}
	
	/* Get <roomName, visualComforTable(Relation between light level and lux)> */
	public Map<String, VisualComfortTable> getVisualComfortTableList(){
		Map<String, VisualComfortTable> tableList = new LinkedHashMap<String, VisualComfortTable>();
		try{
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xml);
			List list = document.selectNodes("/metaData/comfort_table/visual/location");
			Iterator iter = list.iterator();
			while(iter.hasNext()){
				Element roomElement = (Element)iter.next();
				// e.g., livingroom, hallway
				String roomName = roomElement.attributeValue("id");
				// e.g., light_livingroom, light_hallway
				Element appElement = roomElement.element("appliance");
				String[] appliances = appElement.getText().split(" ");
				Element relationElement = roomElement.element("relation");
				String[] relation = relationElement.getText().split(";");
				VisualComfortTable vct = new VisualComfortTable(appliances, relation);
				tableList.put(roomName, vct);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return tableList;
	}
	
	/* Empty */
	public void getThermalComfortTable(){
	}
	
	/* Get <roomName> 
	 * Implemented by Shu-Fan, 2013/11/18
	 * */
	public HashSet<String> getRoomList(){
		HashSet<String> tmpRoomList = new HashSet<String>();
		try{	
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xml);
			List list = document.selectNodes("/metaData/roomList/type");
			Iterator iter = list.iterator();
			while(iter.hasNext()){
				Element roomElement = (Element)iter.next();
				String roomName = roomElement.attributeValue("id");
				tmpRoomList.add(roomName);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return tmpRoomList;
	}
	
	/* Get <Socketmeter_id, sensorNode(Name, Status, Threshold, Switch)> */
	public Map<String, SensorNode> getApplianceList() {
		Map<String, SensorNode> sensorList = new LinkedHashMap<String, SensorNode>();
		
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xml);
			List list = document.selectNodes("/metaData/applianceList/type");
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				Element typeElement = (Element) iter.next();
				String type = typeElement.attributeValue("id"); // id = socketMeter
				List list2 = typeElement.selectNodes("sensor");
				Iterator iter2 = list2.iterator();
				while (iter2.hasNext()) {
					Element sensorElement = (Element) iter2.next();
					String sensorID = sensorElement.attributeValue("id");
					/* Get name, status, threshold */
					String sensorName = sensorElement.element("name").getText();
					String[] sensorStatus = sensorElement.element("status").getText().split(" ");
					String[] sensorThreshold = sensorElement.element("threshold").getText().split(" ");
					double[] sensorThres = new double[sensorThreshold.length];
					for (int i = 0; i < sensorThreshold.length; i++) {
						sensorThres[i] = Double.parseDouble(sensorThreshold[i]);
					}
					SensorNode tmp = null;
					/* If the corresponding sensor is level related */
					if (sensorElement.element("switch") != null && sensorElement.element("switch_lux") != null) {
						String[] switchLevel = sensorElement.element("switch").getText().split(" ");
						int[] switchLevelInteger = new int[switchLevel.length];
						String[] switchLux = sensorElement.element("switch_lux").getText().split(" ");
						double[] switchLuxDouble = new double[switchLux.length];
						for (int i = 0; i < switchLevel.length; i++) {
							switchLevelInteger[i] = Integer.parseInt(switchLevel[i]);
							switchLuxDouble[i] = Double.parseDouble(switchLux[i]);
						}
						tmp = new SensorNode(sensorName, type, sensorThres, sensorStatus, switchLevelInteger, switchLuxDouble);
					} else {
						tmp = new SensorNode(sensorName, type, sensorThres, sensorStatus);
					}
					sensorList.put(type + "_" + sensorID, tmp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sensorList;
	}
	
	/* Get <current_appliance_location> */
	public ArrayList<String> getApplianceNameList() {
		ArrayList<String> applianceNameList = new ArrayList<String>();
		
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xml);
			List list = document.selectNodes("/metaData/applianceList/type");
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				Element typeElement = (Element) iter.next();
				String type = typeElement.attributeValue("id");
				List list2 = typeElement.selectNodes("sensor");
				Iterator iter2 = list2.iterator();
				while (iter2.hasNext()) {
					Element sensorElement = (Element) iter2.next();
					String sensorName = sensorElement.element("name").getText();
					applianceNameList.add(sensorName);
				}
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return applianceNameList;
	}
}
