package IntelM2M.datastructure;

import java.util.LinkedHashMap;
import java.util.Map;

public class AppNode {
	public String appName = "";        // e.g., light_livingroom 
	
	/* eus */
	public String state = "";          // eus: on | off | standby
	public double confidence = 0;      // eus
	public String escType = "";        // eus: explicit or implicit
	
	/* For esdse*/
	public String envContext = "";     // real environment context
	public String agentName = "";      // eus dispatch
	public String comfortType = "";    // thermal, visual, ap
	public String location = "";       // getActAppList
	public Boolean global = false;
	public double priority = 0;        // appliance preference
	
	/* For experiment, no longer use */
	public Boolean haveAPControlFromOn = false;  // 有沒有被ap agent 控制
	public Map<String, Double> ampere =  new LinkedHashMap<String, Double>(); // getAppList
		
		public AppNode(){
		}
		
		public AppNode(String name){
			appName = name;
		}
		
		public AppNode copyAppNode(AppNode tmp){
			AppNode app = new AppNode();

			/* Notice : This is not clone */
			app.appName = tmp.appName;
			app.state = tmp.state;
			app.confidence = tmp.confidence;
			app.escType = tmp.escType;			
			app.location = tmp.location;
			app.comfortType = tmp.comfortType;
			app.ampere = tmp.ampere;
			app.global = tmp.global;
			app.agentName = tmp.agentName;
			app.envContext = tmp.envContext;
			app.haveAPControlFromOn = tmp.haveAPControlFromOn;
			
			return app;
		}
		
}
