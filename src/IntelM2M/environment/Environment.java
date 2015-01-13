package IntelM2M.environment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Environment {
	private Map<String, String> sensorReading = new LinkedHashMap<String, String>();
	private ArrayList<String> updatedSensorList = new ArrayList<String>();
	public static Map<String, Double> temperatureReading = new LinkedHashMap<String, Double>();
	public static Map<String, Double> humidityReading = new LinkedHashMap<String, Double>();
	public static Map<String, Double> illuminationReading = new LinkedHashMap<String, Double>();
	
	public int openDoorCount = 0;
	public boolean ComeBack = false;
	public boolean GoOut = true;
	public boolean doorOpen = false;
	
	
}
