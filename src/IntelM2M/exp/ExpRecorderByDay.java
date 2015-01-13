package IntelM2M.exp;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import IntelM2M.test.SimulatorTest;

public class ExpRecorderByDay {

		static public ExpRecorderByDay expByDay= new ExpRecorderByDay();
		int dayIndex=0;
		

		ArrayList<Integer>   dayFlag=new ArrayList<Integer>();
		
	
		/*For 耗電量比較 by day*/
		ArrayList <Double> rawConsumption= new ArrayList<Double>(); 
		ArrayList <Double> mchessConsumption = new ArrayList<Double>();
		
		/*type save*/
		ArrayList <Double> thermalTypeSave = new ArrayList<Double>();
		ArrayList <Double> visualTypeSave = new ArrayList<Double>();
		ArrayList <Double> apTypeSave = new ArrayList<Double>();
		/*noise save*/
		ArrayList<Double> noiseConsumption= new ArrayList<Double>();
		ArrayList<Double> apAgentRightSaveFromNoise= new ArrayList<Double>();
		
		/*control wrong time*/
		ArrayList<Double> mchessWrongCount = new ArrayList<Double>();
		
		/*comfort*/
		//int comfort_count=0;
		ArrayList<Double> rawVCList= new ArrayList<Double>();
		ArrayList<Double> VCList = new ArrayList<Double>();
		ArrayList<Double> rawTCList= new ArrayList<Double>();
		ArrayList<Double> TCList= new ArrayList<Double>();
		
		ArrayList<Double> rawVCCountList= new ArrayList<Double>();
		ArrayList<Double> VCCountList = new ArrayList<Double>();
		ArrayList<Double> rawTCCountList= new ArrayList<Double>();
		ArrayList<Double> TCCountList= new ArrayList<Double>();
		
		/*service time*/
		ArrayList<Double> serviceTimeWithOutGA= new ArrayList<Double>();
		ArrayList<Double> serviceTimeWithGA= new ArrayList<Double>();
	

		/*location based*/
		ArrayList <Double> locationBasedConsumption = new ArrayList<Double>();
		ArrayList<Double> locationBasedRightSaveFromNoise = new ArrayList<Double>();  /*to be finished*/
		ArrayList<Double> locationBasedWrongCount = new ArrayList<Double>();
		public void initial(){
			/*設定每一天在側資中到第幾行*/
			dayFlag.add(288);
			dayFlag.add(576);
			dayFlag.add(864);
			dayFlag.add(1152);
			dayFlag.add(1440);//dayFlag.add(1452);
			dayFlag.add(1728);//dayFlag.add(1745);
			dayFlag.add(2016);//dayFlag.add(2036);
	
			/**/
			for(int i=0;i<7;i++){
				/*For 耗電量比較 by day*/
				rawConsumption.add(Double.valueOf(0));
				mchessConsumption.add(Double.valueOf(0));
				
				/*type save*/
				thermalTypeSave.add(Double.valueOf(0));
				visualTypeSave.add(Double.valueOf(0));
				apTypeSave.add(Double.valueOf(0));
				/*noise save*/
				noiseConsumption.add(Double.valueOf(0));
				apAgentRightSaveFromNoise.add(Double.valueOf(0));
				
				
				/*control wrong time*/
				mchessWrongCount.add(Double.valueOf(0));
				
				/*comfort*/
				rawVCList.add(Double.valueOf(0));
				VCList.add(Double.valueOf(0));
				rawTCList.add(Double.valueOf(0));
				TCList.add(Double.valueOf(0));
				
				rawVCCountList.add(Double.valueOf(0));
				VCCountList.add(Double.valueOf(0));
				rawTCCountList.add(Double.valueOf(0));
				TCCountList.add(Double.valueOf(0));

				
				
				/*service time*/
				serviceTimeWithOutGA.add(Double.valueOf(0));
				serviceTimeWithGA.add(Double.valueOf(0));
				
				/*location based*/
				locationBasedRightSaveFromNoise.add(Double.valueOf(0));
				locationBasedConsumption.add(Double.valueOf(0));
				locationBasedWrongCount.add(Double.valueOf(0));
				
			}
		}
		
		public void setDay(int row){

						
			int rowNum=row;
			int day=0;
			for(int i=0;i<dayFlag.size();i++){
				int dayRow=dayFlag.get(i);
				if(rowNum<=dayRow){
					day=i;
					break;
				}
			}
			dayIndex=day;
		
		}
		
		public void setConsumption(double rawAmp, double newAmp){
			Double raw=rawConsumption.get(dayIndex);
			raw+=rawAmp;
			Double mchess=mchessConsumption.get(dayIndex);
		
			mchess+=newAmp;
			
			rawConsumption.set(dayIndex, raw);
			mchessConsumption.set(dayIndex, mchess);
			
		}
		
		public void setConsumptionForLocationBased(double newAmp){
			Double raw=locationBasedConsumption.get(dayIndex);
			raw+=newAmp;
			
			locationBasedConsumption.set(dayIndex, raw);
		}
		
		public void setTypeSave(double save,String type){
			if(type.equals("thermal")){
				Double raw= thermalTypeSave.get(dayIndex);
				raw+=save;
				thermalTypeSave.set(dayIndex, raw);
				
			}else if(type.equals("visual")){
				Double raw=visualTypeSave.get(dayIndex);
				raw+=save;
				visualTypeSave.set(dayIndex, raw);
				
			}else if(type.equals("ap")){
				Double raw=apTypeSave.get(dayIndex);
				raw+=save;
				apTypeSave.set(dayIndex, raw);
			}	
		}
		
		public void setNoiseSave(double save, String type){
			if(type.equals("noise")){
				Double raw = noiseConsumption.get(dayIndex);
				raw+=save;
				noiseConsumption.set(dayIndex, raw);
			}else if(type.equals("mchess")){
				Double raw= apAgentRightSaveFromNoise.get(dayIndex);
				raw+=save;
				apAgentRightSaveFromNoise.set(dayIndex, raw);
			}else if(type.equals("locationBased")){
				Double raw= locationBasedRightSaveFromNoise.get(dayIndex);
				raw+=save;
				locationBasedRightSaveFromNoise.set(dayIndex,raw);
			}
			
		}
		
		
		
		public void setWrongControl(int count, String type){
			if(type.equals("mchess")){
				Double raw= mchessWrongCount.get(dayIndex);
				raw+=count;
				mchessWrongCount.set(dayIndex, raw);
			}else if(type.equals("locationBased")){
				Double raw= locationBasedWrongCount.get(dayIndex);
				raw+=count;
				locationBasedWrongCount.set(dayIndex, raw);
			}
			
		}
		
		public void setComfort(ArrayList<Double> comfortList, String type){
			final int maxLevel=3;
			if(type.equals("raw_visual")){
				Double raw = rawVCList.get(dayIndex);
				for(Double comfort:comfortList){
					double diff=Math.abs(comfort);
					if(diff>maxLevel){
						diff=maxLevel;
					}
					//raw+= Math.pow(diff, 2);
					raw+=diff;
				}
				rawVCList.set(dayIndex, raw);
				/*count*/
				Double count= rawVCCountList.get(dayIndex);
				count+=comfortList.size();
				rawVCCountList.set(dayIndex, count);
			}else if(type.equals("visual")){
				Double raw=VCList.get(dayIndex);
				for(Double comfort:comfortList){
					double diff=Math.abs(comfort);
					if(diff>maxLevel){
						diff=maxLevel;
					}
					//raw+= Math.pow(diff, 2);
					raw+=diff;
				}
				
				
				VCList.set(dayIndex, raw);
				/*count*/
				Double count= VCCountList.get(dayIndex);
				count+=comfortList.size();
				VCCountList.set(dayIndex, count);
				
			}else if(type.equals("raw_thermal")){
				Double raw= rawTCList.get(dayIndex);
				for(Double comfort:comfortList){
					double diff=Math.abs(comfort);
					if(diff>maxLevel){
						diff=maxLevel;
					}
					//raw+= Math.pow(diff, 2);
					raw+=diff;
				}
				rawTCList.set(dayIndex, raw);
				
				/*count*/
				Double count= rawTCCountList.get(dayIndex);
				count+=comfortList.size();
				rawTCCountList.set(dayIndex, count);
				
			}else if(type.equals("thermal")){
				Double raw= TCList.get(dayIndex);
				for(Double comfort:comfortList){
					double diff=Math.abs(comfort);
					if(diff>maxLevel){
						diff=maxLevel;
					}
					//raw+= Math.pow(diff, 2);
					raw+=diff;
					
				}
				TCList.set(dayIndex, raw);
				/*count*/
				Double count= TCCountList.get(dayIndex);
				count+=comfortList.size();
				TCCountList.set(dayIndex, count);

			}
		}
		
		private void averageComfortList(){
			for(int i=0;i<rawVCList.size();i++){
				Double raw = rawVCList.get(i);
				Double count1=rawVCCountList.get(i);
				//raw=Math.sqrt(raw/count1);
				raw=raw/count1;
				rawVCList.set(i, raw);
				
				Double count2=VCCountList.get(i);
				raw=VCList.get(i);
				//raw=Math.sqrt(raw/count2);
				raw=raw/count2;
				VCList.set(i, raw);
				
				Double count3=rawTCCountList.get(i);
				raw= rawTCList.get(i);
				//raw=Math.sqrt(raw/count3);
				raw=raw/count3;
				rawTCList.set(i, raw);
				
				Double count4=TCCountList.get(i);
				raw=TCList.get(i);
				//raw=Math.sqrt(raw/count4);
				raw=raw/count4;
				TCList.set(i, raw);
			}
		}
		
		
		public void setServingTime(int time,String type){
			if(type.equals("withGA")){
				Double raw= serviceTimeWithGA.get(dayIndex);
				raw+=time;
				serviceTimeWithGA.set(dayIndex, raw);
			}else if(type.equals("withoutGA")){
				Double raw= serviceTimeWithOutGA.get(dayIndex);
				raw+=time;
				serviceTimeWithOutGA.set(dayIndex, raw);
			}
		}
		
		public void writeOutESResult(){
			try{
				
				/*write approch save*/
				FileWriter writer = new FileWriter(new File("./_output_results/day_data/ESresult.txt"),false);
				double t1=0,t2=0,t3=0;
				for(int i=0;i<rawConsumption.size();i++){
					double raw= rawConsumption.get(i);
					double mchess=mchessConsumption.get(i);
					double locationBased=locationBasedConsumption.get(i);
					t1+=raw;
					t2+=mchess;
					t3+=locationBased;
					writer.write(raw+" , "+locationBased+" , "+mchess+"\r\n");
					writer.flush();
				}
				writer.write(t1+" , "+t3+" , "+t2+"\r\n");
				writer.close();
				/*write type save*/
				writer = new FileWriter(new File("./_output_results/day_data/TypeSaveResult.txt"),false);
				for(int i=0;i<thermalTypeSave.size();i++){
					double thermalSave= thermalTypeSave.get(i);
					double visualSave= visualTypeSave.get(i);
					double apSave= apTypeSave.get(i);
					writer.write(thermalSave+" , "+visualSave+" , "+apSave+"\r\n");
					writer.flush();
				}
				writer.close();
				
				/*write noise save*/
				writer = new FileWriter(new File("./_output_results/day_data/NoiseSaveResult.txt"),false);
				for(int i=0;i<noiseConsumption.size();i++){
					double noise=noiseConsumption.get(i);
					double mchessSave= apAgentRightSaveFromNoise.get(i);
					double locationSave=locationBasedRightSaveFromNoise.get(i);
					writer.write(noise+" , "+locationSave+" , "+mchessSave+"\r\n");
					writer.flush();
					
				}
				writer.close();
				
				/*writer wrong count*/
				writer = new FileWriter(new File("./_output_results/day_data/WrongCount.txt"),false);
				for(int i=0;i<mchessWrongCount.size();i++){
					double count = mchessWrongCount.get(i);
					double count2=locationBasedWrongCount.get(i);
					writer.write(count2+" , "+count+"\r\n");
					writer.flush();
				}
				writer.close();
				
				/*write average comfort*/
				averageComfortList();
				writer = new FileWriter(new File("./_output_results/day_data/ComfortResult.txt"),false);
				for(int i=0;i<rawVCList.size();i++){
					double rawVC=rawVCList.get(i);
					double VC= VCList.get(i);
					double rawTC=rawTCList.get(i);
					double TC=TCList.get(i);
					
					writer.write(rawVC+" , "+VC+" , "+rawTC+" , "+TC+"\r\n");
					writer.flush();
				}
				writer.close();
				/*write service time*/
				averageComfortList();
				writer = new FileWriter(new File("./_output_results/day_data/ServingTime.txt"),false);
				for(int i=0;i<serviceTimeWithGA.size();i++){
					double time1=serviceTimeWithGA.get(i);
					double time2= serviceTimeWithOutGA.get(i);
	
					
					writer.write(time2+" , "+time1+"\r\n");
					writer.flush();
				}
				writer.close();
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
}
