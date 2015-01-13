package IntelM2M.func;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import IntelM2M.algo.Prior;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.epcie.GaGenerator;


public class text2Arff
{
	
	//public String ga_modify_train_path="";

	
	public text2Arff()
	{
	
	
	}
	
//	public void temp(){
//		try {
//			BufferedReader reader = new BufferedReader(new FileReader("./"+ dirName + "/" + fileName + ".txt"));
//			FileWriter writer = new FileWriter(new File("./"+ dirName + "/" + fileName + "2.txt"));
//			String read;
//			while((read = reader.readLine()) != null)
//			{
//				String[] split = read.split("#");
//				String [] split2=split[0].split(" ");
//				writer.write(split2[8]+" "+split2[1]+" "+split2[0]+" "+split2[7]+" "+split2[2]+" "+split2[6]+" "+split2[4]+" "+split2[3]+" "+split2[5]+" "+split2[9]+" "+split2[10]+" #"+split[1]+"\r\n");
//				writer.flush();
//			}	
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
//	}
		
	public void convertRawToArff(String rawTrainingDataPath)
	{

		
		try
		{
			String read ;
		
			
			ArrayList<String> actList =EnvStructure.activityList;
			Map<String, ArrayList<String>> sensorList=EnvStructure.sensorStatus;
					
			//output arff file
			
			for(String activity : actList)
			{
				FileWriter writer = new FileWriter(new File("./_weka_training_data/" + activity + ".arff"));
				
				
				//---------------------- write description --------------------------------------
				writer.write("@relation TrainingData_" + activity + "\r\n\n");
				writer.flush();
				Set<String>  keys = sensorList.keySet();
				for(String sensor : keys)
				{
					writer.write("@attribute " + sensor + " {"); 
					Object[] sensorStates = sensorList.get(sensor).toArray(); 
					for(int i = 0; i < sensorStates.length; i ++)
					{
						if(i == sensorStates.length - 1)
							writer.write((String)sensorStates[i]);
						else
							writer.write((String)sensorStates[i] + " ");
						writer.flush();
					}
					
					writer.write("}\r\n");
					writer.flush();
				}
				writer.write("@attribute class {OtherActivity " + activity + "}\r\n\n");
				writer.flush();
				writer.write("@data\r\n");
				writer.flush();
				//-------------------------------------------------------------------------------
				
				BufferedReader reader = new BufferedReader(new FileReader(rawTrainingDataPath));
				while((read = reader.readLine()) != null)
				{
					Prior.priorForTrainingData(read,activity,writer);

				
					/* 2.prior training data*/
//					if(split[1].contains(activity)){
//						if(split[1].contains("AllSleeping") &&activity.equals("Sleeping")){
//
//							writer.write(split[0]+"OtherActivity\r\n");
//						}
//						else {
//						writer.write(split[0]+activity + "\r\n");
//						}
//					}
//					else if(activity.equals("WatchingTV") && split[1].contains("PlayingKinect")){
//						
//					}
//					else if(activity.equals("Chatting")){
//						
//						String []split3=split[0].split(" ");
//						split3[5]="off";
//						String tmp="";
//						for(String str:split3){
//							tmp+=str+" ";
//						}
//						writer.write(tmp+"OtherActivity\r\n");
//					}
//					else if(activity.equals("Reading")){
//						
//						String []split3=split[0].split(" ");
//						split3[10]="off";
//						String tmp="";
//						for(String str:split3){
//							tmp+=str+" ";
//						}
//						writer.write(tmp+"OtherActivity\r\n");
//					}
//					else if(activity.equals("GoOut")){
//						String []split3=split[0].split(" ");
//						split3[16]="on";
//						split3[17]="on";
//						split3[18]="on";
//						String tmp="";
//						for(String str:split3){
//							tmp+=str+" ";
//						}
//						writer.write(tmp+"OtherActivity\r\n");
//					}
//
//					else{
//						writer.write(split[0]+"OtherActivity\r\n");
//					}
					
				}	
				writer.close();	
				reader.close();
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	static public void convertGaRawToArff(GaGenerator GA,String rawTrainingDataPath)
	{

		String gaTrainingDataPath="./_input_data/inputTmp/ga_training_data.txt";
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					rawTrainingDataPath));
			FileWriter writer = new FileWriter(new File(gaTrainingDataPath));
			String read="";
			while((read = reader.readLine()) != null){
				String []split=read.split("#");
				String []split2=split[1].split(" ");
					
				writer.write(split[0]+"#");
				for(String str:split2){
					//String GID=GA.getGID(str);
					ArrayList<String> gidArr=GA.getGID(str);
					for(String GID:gidArr){
						writer.write(GID+" ");
					}
				}
				writer.write("\r\n");
				writer.flush();
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try
		{
			String read ;
		
			
	   		/*build gaList*/
	  		Set<String> gSet=GA.gaList.keySet();
	  		ArrayList<String> gaList= new ArrayList<String>();
	   		for(String str:gSet){
	   			gaList.add(str);
	   		}
			Map<String, ArrayList<String>> sensorList=EnvStructure.sensorStatus;
					
			//output arff file
			
			for(String activity : gaList)
			{
				FileWriter writer = new FileWriter(new File("./_weka_training_data/" + activity + ".arff"));
				
				//---------------------- write description --------------------------------------
				writer.write("@relation TrainingData_" + activity + "\r\n\n");
				writer.flush();
				Set<String>  keys = sensorList.keySet();
				for(String sensor : keys)
				{
					writer.write("@attribute " + sensor + " {"); 
					Object[] sensorStates = sensorList.get(sensor).toArray(); 
					for(int i = 0; i < sensorStates.length; i ++)
					{
						if(i == sensorStates.length - 1)
							writer.write((String)sensorStates[i]);
						else
							writer.write((String)sensorStates[i] + " ");
						writer.flush();
					}
					
					writer.write("}\r\n");
					writer.flush();
				}
				writer.write("@attribute class {OtherActivity " + activity + "}\r\n\n");
				writer.flush();
				writer.write("@data\r\n");
				writer.flush();
				//-------------------------------------------------------------------------------
				
				BufferedReader reader = new BufferedReader(new FileReader(gaTrainingDataPath));
				while((read = reader.readLine()) != null)
				{

					Prior.priorForTrainingData(read,activity,writer,GA);
					
					/* 1. raw training data*/
//					String[] split = read.split("#");
//					String[] split2=split[1].split(" ");
//					Boolean contains=false;
//					for(String str:split2){
//						if(str.equals(activity)){
//							contains=true;
//						}
//					}
//					if(contains){
//						writer.write(split[0]+activity + "\r\n");
//					}else{
//						writer.write(split[0]+"OtherActivity\r\n");
//					}
//					
					/* 1. raw training data*/
//					String[] split = read.split("#");
//					String[] split2=split[1].split(" ");
//					Boolean thisAct=false;
//					for(String str:split2){
//						if(str.equals(activity))
//							thisAct=true;
//					}
//					if(thisAct==true){
//						writer.write(split[0]+activity + "\r\n");
//					}else{
//						writer.write(split[0]+"OtherActivity\r\n");
//					}
					/* 2.prior training data*/
//					if(split[1].contains(activity)){
//						writer.write(split[0]+activity + "\r\n");
//					}
					
//					else if(GA.gaList.get(activity).actMemberList.contains("Chatting")){
//						
//						String []split3=split[0].split(" ");
//						split3[6]="off";
//						String tmp="";
//						for(String str:split3){
//							tmp+=str+" ";
//						}
//						writer.write(tmp+"OtherActivity\r\n");
//					}
//					else if(GA.gaList.get(activity).actMemberList.contains("Reading")){
//						
//						String []split3=split[0].split(" ");
//						split3[12]="off";
//						String tmp="";
//						for(String str:split3){
//							tmp+=str+" ";
//						}
//						writer.write(tmp+"OtherActivity\r\n");
//					}
//					else if(GA.gaList.get(activity).actMemberList.contains("GoOut")){
//						String []split3=split[0].split(" ");
//						split3[18]="on";
//						split3[19]="on";
//						split3[20]="on";
//						String tmp="";
//						for(String str:split3){
//							tmp+=str+" ";
//						}
//						writer.write(tmp+"OtherActivity\r\n");
//					}


					
					writer.flush();
					
				}	
				writer.close();	
				reader.close();
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
//    public void convertGaRawToArff2(GaGenerator GA,String rawTrainingDataPath)
//	{
//
//		String gaTrainingDataPath="./_input_data/ga_training_data.txt";
//		
//		try {
//			BufferedReader reader = new BufferedReader(new FileReader(
//					rawTrainingDataPath));
//			FileWriter writer = new FileWriter(new File(gaTrainingDataPath));
//			String read="";
//			while((read = reader.readLine()) != null){
//				String []split=read.split("#");
//				String []split2=split[1].split(" ");
//					
//				writer.write(split[0]+"#");
//				for(String str:split2){
//					String GID=GA.getGID(str);
//					writer.write(GID+" ");
//				}
//				writer.write("\r\n");
//				writer.flush();
//				
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
//		try
//		{
//			String read ;
//		
//			
//	   		/*build gaList*/
//	  		Set<String> gSet=GA.gaList.keySet();
//	  		ArrayList<String> gaList= new ArrayList<String>();
//	   		for(String str:gSet){
//	   			gaList.add(str);
//	   		}
//			Map<String, ArrayList<String>> sensorList=EnvStructure.sensorStatus;
//					
//			//output arff file
//			
//			for(String activity : gaList)
//			{
//				FileWriter writer = new FileWriter(new File("./_weka_training_data/" + activity + ".arff"));
//				
//				//---------------------- write description --------------------------------------
//				writer.write("@relation TrainingData_" + activity + "\r\n\n");
//				writer.flush();
//				Set<String>  keys = sensorList.keySet();
//				for(String sensor : keys)
//				{
//					writer.write("@attribute " + sensor + " {"); 
//					Object[] sensorStates = sensorList.get(sensor).toArray(); 
//					for(int i = 0; i < sensorStates.length; i ++)
//					{
//						if(i == sensorStates.length - 1)
//							writer.write((String)sensorStates[i]);
//						else
//							writer.write((String)sensorStates[i] + " ");
//						writer.flush();
//					}
//					
//					writer.write("}\r\n");
//					writer.flush();
//				}
//				writer.write("@attribute class {OtherActivity " + activity + "}\r\n\n");
//				writer.flush();
//				writer.write("@data\r\n");
//				writer.flush();
//				//-------------------------------------------------------------------------------
//				
//				BufferedReader reader = new BufferedReader(new FileReader(gaTrainingDataPath));
//				while((read = reader.readLine()) != null)
//				{
//					String[] split = read.split("#");
//					writer.write(split[0]);
//					if(split[1].contains(activity))
//						writer.write(activity + "\r\n");
//					else
//						writer.write("OtherActivity\r\n");
//				}	
//				writer.close();	
//				reader.close();
//			}
//			
//		}
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
	
	public void writeClusterArff( String rawTrainingDataPath){
		Map<String, ArrayList<String>> sensorList=EnvStructure.sensorStatus;
		ArrayList<String> actList =EnvStructure.activityList;
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter("./_weka_training_data/cluster.arff"));
			writer.write("@relation cluster  \n\n");
			Set<String>  keys = sensorList.keySet();
			for(String sensor : keys)
			{
				writer.write("@attribute " + sensor + " {"); 
				Object[] sensorStates = sensorList.get(sensor).toArray(); 
				for(int i = 0; i < sensorStates.length; i ++)
				{
					if(i == sensorStates.length - 1)
						writer.write((String)sensorStates[i]);
					else
						writer.write((String)sensorStates[i] + " ");
					writer.flush();
				}
				
				writer.write("}\r\n");
				writer.flush();
			}
			writer.write("@attribute class {");
			for(int i = 0; i < actList.size(); i ++)
			{
				writer.write(actList.get(i));
				if(i < actList.size() - 1)
					writer.write(",");
				else
					writer.write("}\r\n\n");
			}
			writer.write("@data\r\n");
			writer.flush();
			//-------------------------------------------------------------------------------
			String read=null;
			BufferedReader reader = new BufferedReader(new FileReader(rawTrainingDataPath));
			while((read = reader.readLine()) != null)
			{
				String[] split = read.split("#");
				writer.write(split[0]+" "+split[1]+ "\r\n");
		
			}	
			writer.close();	
			reader.close();
			
		}catch(IOException e){}
	}
	
	

}
