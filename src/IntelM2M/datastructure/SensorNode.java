package IntelM2M.datastructure;

public class SensorNode {
  
    	public String name;
    	public String type;
    	public double[] threshold;
    	public String[] status;
    	public String id;
    	public String discreteValue;
    	public double rawValue;
    	public int[] switchLevel; // ex:1, 2, 3
    	public double[] switchLux;
    	public int currentSwitchLevel;
    	//int count;
    	//double accumulated_watt;
    	
    	public SensorNode(String name, String type, double[] threshold, String[] status, int[] switchLevel, double[] switchLux){
    		this.name = name;
    		this.type = type;
    		this.threshold = threshold.clone();
    		this.status = status.clone();
    		this.switchLevel = switchLevel.clone();
    		this.switchLux = switchLux.clone();
    	}
    	
    	public SensorNode(String name, String type, double[] threshold, String[] status){
    		this.name = name;
    		this.type = type;
    		this.threshold = threshold.clone();
    		this.status = status.clone();
    		//this.count = 0;
    		//this.accumulated_watt = 0.0;
    	}

    	public SensorNode(String name, String discreteValue){
    		this.name = name;
    		this.discreteValue = discreteValue;
    	}
    	
    	public SensorNode(String name, String discreteValue, int currentSwitchLevel){
    		this.name = name;
    		this.discreteValue = discreteValue;
    		this.currentSwitchLevel = currentSwitchLevel;
    	}
    	
    	public SensorNode(){		
    	}
    
}
