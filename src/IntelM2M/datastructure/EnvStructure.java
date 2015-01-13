package IntelM2M.datastructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import IntelM2M.agent.visual.VisualComfortTable;

/**
 * 
 * Revised by Shu-Fan 2013/11/19
 *
 */

public class EnvStructure {
	
	/* These values are shared among all EnvStructure object */
	public static ArrayList<String> activityList;                          // e.g., <WatchingTV>
	public static Map<String, ArrayList<String>> sensorStatus;             // e.g., <light_livingroom, on/standby/off> 
	public static Map<String, SensorNode> sensorList;                      // e.g., <current_20, SensorNode(name, status, threshold, switch, switchLux)>
	public static Map<String, RelationTable> actAppList;                   // e.g., <livingroom_WatchingTV, relationTable(appList, intensity)>
	public static HashSet<String> roomList;                                // e.g., <livingroom>
	public static Map<String, String> sensorState;                         // Seems no longer use
	public static Map<String, AppNode> appList;                            // e.g., <light_livingroom, appNode(appName, comfortType)>
    public static boolean[] actPreState;                                   // Seems no longer use
    public static Map<String, VisualComfortTable> visualComfortTableList;  // e.g., <livingroom, visualComforTable (Relation between light level and lux)>
    public static Map<String, String> actRoomList;                         // e.g., <WatchingTV, livingroom>
    public static Map<String, SensorNode> applianceList;                   // e.g., <socketmeter_03, SensorNode> Only for appliances.
    public static ArrayList<String> applianceNameList;                     // e.g., <current_AC_livingroom>

    static{
    	read();
    }
	
	public static void read() {
		XMLHandler xml = new XMLHandler();
		activityList = xml.getActList();
		sensorList = xml.getSensorList();
		sensorStatus = xml.getSensorStatus();
		appList = xml.getAppList();
		actAppList = xml.getActAppList();
		//roomList = buildRoomList();
		roomList = xml.getRoomList();
		actPreState = buildactPreState();
		sensorState = buildSensorState();
		visualComfortTableList = xml.getVisualComfortTableList();
		actRoomList = buildActRoomList();
		applianceList = xml.getApplianceList();
		applianceNameList = xml.getApplianceNameList();
	}
	
	public EnvStructure(){
	}
	
	/* Extract room list from key value of actAppList */
	static HashSet<String> buildRoomList(){
    	// Build room list
	    Set<String> acts = actAppList.keySet(); // e.g., livingroom_WatchingTV
	    HashSet<String> roomList = new HashSet<String>();
	    for(String act : acts)
	    {
	    	String[] split = act.split("_");
	    	roomList.add(split[0]);
	    }
	    return roomList;
	}
	
	static boolean[] buildactPreState(){
		boolean[] actPreState = new boolean[activityList.size()];
		Arrays.fill(actPreState, false);
		return actPreState;
	}
	
	static Map<String, String> buildSensorState(){
		Map<String, String> sensorState = new HashMap<String, String>();
		return sensorState;
	}
	
	static Map<String, String> buildActRoomList(){
		Set<String> acts = actAppList.keySet(); // key ex: hallway_GoOut
		Map<String, String> actRoomList = new HashMap<String, String>();
		for (String locationActivity : acts) {
			String[] split = locationActivity.split("_");
			actRoomList.put(split[1], split[0]);
		}
		return actRoomList;
	}

}
