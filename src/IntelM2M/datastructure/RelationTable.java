package IntelM2M.datastructure;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RelationTable{

	
	
	public ArrayList<AppNode> appList;
	public double intensity;
	
	public RelationTable(){
		appList = new ArrayList<AppNode>();
		intensity = 0.0;
	}
	

//	 public void addAppNode(String name, String range){
//		appList.add( new AppNode(name,range));
//	}
	
//	 public void buildAppPower(String appName,String status, String ampere){
//		for(AppNode n:appList){
//			if(n.appName.equals(appName)){
//				 String []tmp1=status.split(" ");
//				 String []tmp2=ampere.split(" ");
//				 for(int i=0; i<tmp1.length;i++){
//					 n.avgAmpere.put(tmp1[i], 110*Double.parseDouble(tmp2[i]));
//				 }
//			}
//		}
//	 }
	 
//	 public void updataInfromation(String appName,String type,String info){
//		 for(AppNode n:appList){
//			 if(n.appName.equals(appName)){
//				 if(type.equals("comfort_type")){
//					 n.comfortType=info;
//				 }else if(type.equals("location")){
//					 n.location=info;
//				 }
//			 }
//		 }
//		 
//	 }

//	public void setAppPower(int j, String state){
//			double power= appList.get(j).avgAmpere.get(state);
//			int i=0;
//			if(power<=0)
//				i=0;
//			else if(power<=15)
//				i=1;
//			else if(power<=200)
//				i=2;
//			else if(power<=300)
//				i=3;
//			else if(power<=1000)
//				i=4;
//			else if(power<=1000000)
//				i=5;
//			//appList.get(j).power= power;
//			appList.get(j).powerLevel=i;
//	}
	 
//	public String ETCForKmeans(){
//		String strReturn = Double.toString(this.intensity);
//		for(int i = 0; i < this.appList.size(); i ++)
//		{
//			if(this.appList.get(i).appType.equals("local") || this.appList.get(i).appName.contains("AC") || this.appList.get(i).appName.contains("waterHeater"))
//				strReturn = strReturn + "," + this.appList.get(i).power;
//		}
//		
//		return strReturn;
//	}
	

//	public String ETCForHamming(){
//		String strReturn = Double.toString(this.intensity);
//		for(int i = 0; i < this.appList.size(); i ++)
//		{
//			if(this.appList.get(i).appType.equals("local") || this.appList.get(i).appName.contains("AC") || this.appList.get(i).appName.contains("waterHeater"))
//				strReturn = strReturn + "," + this.appList.get(i).powerLevel;
//		}
//		
//		return strReturn;
//	}
	

}