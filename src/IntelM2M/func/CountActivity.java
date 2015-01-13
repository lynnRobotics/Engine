package IntelM2M.func;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import IntelM2M.datastructure.EnvStructure;

public class CountActivity {
	/*old not use*/
	private void count(){
		ArrayList<String> actList= EnvStructure.activityList;
		for(String str:actList){
			double count=0;
			double allCount=0;
			try{
				BufferedReader reader = new BufferedReader(new FileReader("./_input_data/CrossValidate/cv_all_data.txt"));
				String read=null;

				while((read = reader.readLine()) != null){
					allCount++;
					String []split=read.split("#");

					if(split[1].contains(str)){
						if(str.equals("Sleeping") && split[1].contains("AllSleeping") ){
							
						}
						else{
							count++;
						}
					}
				}
				System.out.println(str+" :"+count+" "+allCount+" "+count/allCount);
				
			}catch(Exception ex){
			ex.printStackTrace();
			}
			
		}
	}
	/*計算training data活動頻率*/
	private void count2(){
		ArrayList<String> actList= EnvStructure.activityList;
		Map <String,Integer> countList1=new LinkedHashMap<String,Integer>();
		Map <String,Integer> countList2=new LinkedHashMap<String,Integer>();
		actList.add("NO");
		for(String str:actList){
			countList1.put(str, 0);
			countList2.put(str,0);
		}
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./_input_data/CrossValidate/cv_all_data3.txt"));
			String read=null;
			int allCount=0;

			while((read = reader.readLine()) != null){
				allCount++;
				String []split2=read.split("#");
				String []split=split2[1].split(" ");
				if(split[0].equals("AllSleeping")){
					int c1=countList1.get(split[0])+1;
					int c2=countList2.get(split[0])+1;
					countList1.put(split[0], c1);
					countList2.put(split[0], c2);
				}else {
					int c1=countList1.get(split[0])+1;
					int c2=countList2.get(split[1])+1;
					countList1.put(split[0], c1);
					countList2.put(split[1], c2);
				}
				
			}
			double total1=0,total2=0;
			for(String str:actList){
				int c1=countList1.get(str);
				double ans=(double)c1/(double)allCount;
				total1+=ans;
				System.out.println(str+":"+ans+" ");
			}
			System.out.println(total1);
			for(String str:actList){
				int c2=countList2.get(str);
				double ans=(double)c2/(double)allCount;
				total2+=ans;
				System.out.println(str+":"+ans+" ");
			}
			System.out.println(total2);
		}catch(Exception ex){
		ex.printStackTrace();
		}
	}
	
	private void countConfidence(){
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./_weka_output_data/selected_Reading.arff"));
			String read=null;

			double c1=0;
			double c2=0;
			double c3=0;
			double c4=0;
			double all=0;

			boolean countFlag=false;
			while((read = reader.readLine()) != null){
				String []split=read.split(" ");

				String[]split2=read.split(",");
				if(countFlag){
					if(split2[4].equals("Reading")){
						if(split2[0].equals("off")){
							c1++;
						}
						if(split2[1].equals("on")){
							c2++;
						}
						if(split2[2].equals("standby")){
							c3++;
						}
						if(split2[3].equals("on")){
							c4++;
						}
						all++;
					}
				}
				if(split[0].equals("@data")){
					countFlag=true;
				}
			}
			System.out.println("c1:"+c1+" c2:"+c2+" c3:"+c3+" c4:"+c4+" all:"+all);
			System.out.println("c1:"+c1/all+" c2:"+c2/all+" c3:"+c3/all+" c4:"+c4/all);
			
			
		}catch(Exception ex){
		ex.printStackTrace();
		}
		
	}

	private void delStudy_Refri(){
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./_input_data/CrossValidate/cv_all_data.txt"));
			FileWriter writer = new FileWriter(new File("./_input_data/CrossValidate/cv_all_data_new.txt"));
			String read=null;

			while((read = reader.readLine()) != null){
				String []split=read.split("#");
				String[]split2=split[0].split(" ");
				for(int i=0;i<split2.length;i++){
					if(i==0 ||i==9 ){
						
					}else{
						writer.write(split2[i]+" ");
					}
				}
				writer.write("#"+split[1]+"\r\n");
				writer.flush();
			}
			
			
		}catch(Exception ex){
		ex.printStackTrace();
		}
	}
	
	private void addNoise(String inPath, String outPath){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(inPath));
			FileWriter writer = new FileWriter(new File(outPath));
			String read=null;
			ArrayList <String> dataArr=new ArrayList<String>();

			while((read = reader.readLine()) != null){
				String []split=read.split("#");
				String[]split2=split[1].split(" ");
				
				/*已經有data*/
				if(dataArr.size()!=0){
					String []split3=dataArr.get(0).split("#");
					if(split[1].equals(split3[1])){
						dataArr.add(read);
					}
					else{
						int size=dataArr.size();
						for(int i=0;i<size;i++){
							if(i%5==0){
								writer.write(dataArr.get(i)+"\r\n");
								writer.flush();
							}else if(i%5==1 || i %5==2){
								String []split4=dataArr.get(i).split("#");
								String []split5=split4[1].split(" ");
								split5[0]="NO";
								writer.write(split4[0]+"#"+split5[0]+" "+split5[1]+"\r\n");
								writer.flush();
							}else if(i%5==3 || i%5==4){
								String []split4=dataArr.get(i).split("#");
								String []split5=split4[1].split(" ");
								split5[1]="NO";
								writer.write(split4[0]+"#"+split5[0]+" "+split5[1]+"\r\n");
								writer.flush();
							}
						}
						dataArr.clear();
					}
				}
				
				if(split2.length>1 &&!(split2[0].equals(split2[1])) && !(split2[0].equals("NO") || split2[1].equals("NO") )){
					dataArr.add(read);
				}else {
					writer.write(read+"\r\n");
					writer.flush();
				}

			}
			
			
		}catch(Exception ex){
		ex.printStackTrace();
		}
	}
	
	private void sortSMattrix(){
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./_input_data/sMatrix.txt"));
			FileWriter writer = new FileWriter(new File("./_input_data/sMatrix2.txt"));
			String read=null;

			while((read = reader.readLine()) != null){
				String []split=read.split(" ");
				
				double min=999;
				int minIndex=0;
				double []minArr= new double[split.length];
				for(int i=0;i<minArr.length;i++){
					
					for(int j=0;j<split.length;j++){
						double d =Double.parseDouble(split[j]);
						if(d<=min && d!=0){
							min=d;
							
							minIndex=j;
						}
					}
					min=999;
					split[minIndex]="0";
					minArr[i]=minIndex;
				}
				for(int i=0;i<minArr.length;i++){
					String str=null;
					if(minArr[i]==0)
						str="watch_TV";
					else if(minArr[i]==1)
						str="play_Kinect";
					else if(minArr[i]==2)
						str="Chat";
					else if(minArr[i]==3)
						str="Read";
					else if(minArr[i]==5)
						str="ComeBack";
					else if(minArr[i]==6)
						str="GoOut";
					else if(minArr[i]==7)
						str="Prepare_Food";
					else if(minArr[i]==8)
						str="Sleep";
					else if(minArr[i]==4)
						str="UsingPC";
					else if(minArr[i]==9)
						str="AllSleep";
					
					writer.write( str+",");
					writer.flush();
				}
				writer.write("\r\n");
			}
			
			
		}catch(Exception ex){
		ex.printStackTrace();
		}
	}
	
	private void changeGoOutFeature(){
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./_input_data/simulator/simulator_trainingdata2.txt"));
			FileWriter writer = new FileWriter(new File("./_input_data/simulator/simulator_trainingdata3.txt"),false);
			String read=null;

			String goOutmod="off off off off off off off off off off off off off off off off off off standby off standby off off off off standby standby off off off standby standby standby off off standby off off off off off low low low low low low low low low low medium #GoOut";
			int i=0;
			while((read = reader.readLine()) != null){
				String split[]=read.split("#");
				String split2[]=split[0].split(" ");
				String modify="";
				Boolean flag=false;
				if(split[1].contains("GoOut")){

					modify=goOutmod;
					flag=true;
				}
				
//				if(split[1].contains("Chatting") || split[1].contains("WatchingTV")){
//					
//					split2[1]="on";
//					flag=true;
//				}
//				
//
//				
//				if(split[1].contains("UsingPC") || split[1].contains("Studying")  || split[1].contains("Sleeping") || split[1].contains("ListeningMusic")){
//					
//					split2[2]="on";
//					flag=true;
//				}
//				
//				for(String str:split2){
//					modify=modify+str+" ";
//				}
//				modify=modify+"#"+split[1];
				
				if(flag==false){
					writer.write(read+"\r\n");
					writer.flush();
				}else{
					writer.write(modify+"\r\n");
					writer.flush();
				}
				
				

				i++;
			}
			writer.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void watchContext(){
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./_input_data/test/test.txt"));
			FileWriter writer = new FileWriter(new File("./_input_data/test/result.txt"),false);
			String read=null;
			Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;
			String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
			while((read = reader.readLine()) != null){
				String split[]=read.split("#");
				String split2[]=split[0].split(" ");
				for(int i=0;i<sensorName.length;i++){
					writer.write(sensorName[i]+" :"+split2[i]+"\r\n");
				}
				writer.flush();
			}
			writer.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("finish");
	}
	
	public static void main(String[] args)
    {
		CountActivity ca= new CountActivity();
		ca.watchContext();
		//ca.changeGoOutFeature();
		//ca.changeGoOutFeature();
		//ca.sortSMattrix();
		//ca.addNoise("./_input_data/CrossValidate/cv_all_data2.txt","./_input_data/CrossValidate/cv_all_data_tmp.txt");
		//ca.countConfidence();
		//ca.delStudy_Refri();
		//ca.count();
    }
}
