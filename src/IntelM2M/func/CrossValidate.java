package IntelM2M.func;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import IntelM2M.algo.GaEtcGenerator;
import IntelM2M.datastructure.ExpResult;
import IntelM2M.epcie.Epcieold;
import IntelM2M.epcie.GaGenerator;
import IntelM2M.epcie.classifier.DbnClassifier;
import IntelM2M.epcie.classifier.GaDbnClassifier;
import IntelM2M.epcie.erc.EtcGenerator;
import IntelM2M.tmp.KmCluster;

public class CrossValidate {
	
	/*input*/
	String cvAllDataPath="./_input_data/CrossValidate/cv_all_data3.txt";
	/*output*/
	String cvTestDataPath="./_input_data/CrossValidate/cvTmp/cv_test_data.txt";
	String cvTrainingDataPath="./_input_data/CrossValidate/cvTmp/cv_training_data.txt";
	String cvResultPath="./_output_results/cv_result.txt";
	static int crossParameter=4;
	public static int cvRound=0;
	

	public Map<String,ArrayList<String>> testDataTransform(){
		

		
		Map<String,ArrayList<String>> actDataMap= new LinkedHashMap<String,ArrayList<String>>();
//		for(String str: EnvStructure.activityList){
//			ArrayList<String> data= new ArrayList<String>();
//			actDataMap.put(str,data);
//		}

		try{
				BufferedReader reader = new BufferedReader(new FileReader(cvAllDataPath));
				String read=null;

				while((read = reader.readLine()) != null){
					String[] split = read.split("#");
					String [] split2=split[1].split(" ");
					String actName="";
					/*activity 去掉NO*/
					for(String str:split2){

						if(!str.equals("NO")){
							actName+=str+" ";
						}
					}
					/*如果 Map 的 key 沒有 actName，put這個key */
					if(!actDataMap.containsKey(actName)){
						ArrayList<String> data= new ArrayList<String>();
						actDataMap.put(actName, data);
					}
					
					actDataMap.get(actName).add(split[0]+" #"+actName);
					
//					for(String str:split2){
//						if(!str.equals("NO")){
//							actDataMap.get(str).add(split[0]+" #"+actName);
//						}
//					}
					

				}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		/*check each key in actDataMap的筆數有筆cv的分述還多*/
		Set<String>  keys = actDataMap.keySet();
		Random r=new Random();
		for(String str:keys){
			ArrayList <String>actData=actDataMap.get(str);
			while(actData.size()<crossParameter){
				int selectIndex = r.nextInt(actData.size());
				String tmp=actData.get(selectIndex);
				actData.add(tmp);
			}
			actDataMap.put(str, actData);
		}
		

		return actDataMap;
		
	}
	
	public String dataProcess(String rawIn){
		/*針對training Data 做 process*/
		String [] split=rawIn.split("#");
		String [] rawArr=split[0].split(" ");
		String [] defaultArr={"off","off","off","off","off","off","off","standby","off","off","standby","standby","off","off","standby","standby","standby","standby","off","off","off"};
		/*根據區域將和活動無關的電器設為預設值*/
		
		/*LivingRoom 留1 2 3 6 7 8 12 13 14 17 19*/
		if(split[1].equals("WatchingTV") ||split[1].equals("PlayingKinect")||split[1].equals("Chatting")||split[1].equals("Reading")||split[1].equals("ComeBack")||split[1].equals("GoOut")){
			for(int k=0;k<rawArr.length;k++){
				if(k==0|| k==1 || k==2|| k==5|| k==6|| k==7|| k==11|| k==12|| k==13|| k==16|| k==18 ){
					
				}else{
					rawArr[k]=defaultArr[k];
				}
			}
		}
		/*Kitchen 留1  5 10 15 21*/
		else if(split[1].equals("PreparingFood")){
			for(int k=0;k<rawArr.length;k++){
				if( k==0||k==4 || k==9|| k==14|| k==20 ){
					
				}else{
					rawArr[k]=defaultArr[k];
				}
			}
		}
		/*BedRoom :1 4 9 11 16 18 20*/
		else if(split[1].equals("Sleeping")||split[1].equals("UsingPC")){
			for(int k=0;k<rawArr.length;k++){
				if( k==0||k==3 || k==8|| k==10|| k==15|| k==17|| k==19 ){
					
				}else{
					rawArr[k]=defaultArr[k];
				}
			}
		}
		String traingStr="";
		for(int k=0;k<rawArr.length;k++){
			traingStr=traingStr+rawArr[k]+" ";
		}
		traingStr=traingStr+"#"+split[1];
		return traingStr;
	}
	
	public  void  copyData(String from, String to){
		  try{
		       FileChannel srcChannel = new FileInputStream(from).getChannel();
		       FileChannel dstChannel = new FileOutputStream(to).getChannel();
		       dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
		       srcChannel.close();
		       dstChannel.close();
		  } catch (IOException e) {
		      e.printStackTrace();    
		  }
	}
	
	public void randomWrite(Map<String,ArrayList<String>> dataMap){
		try {			
			Set<String>  keys = dataMap.keySet();
			FileWriter trainingDataWriter = new FileWriter(new File(
					cvTrainingDataPath));
			FileWriter testingDataWriter = new FileWriter(new File(
					cvTestDataPath));
			
			for(String str:keys){
				ArrayList <String> actData= dataMap.get(str);
				Boolean []trainArr = new Boolean[actData.size()];
				for(int i=0;i<trainArr.length;i++){
					trainArr[i]=false;
				}
				double count=0;
				int trainNum=trainArr.length*3/4;
				Random r=new Random();
				while(count<  trainNum){
					int selectIndex = r.nextInt(trainArr.length);
					if(trainArr[selectIndex]==false){
						trainArr[selectIndex]=true;
						count++;
					}
				}

				for(int j=0;j<actData.size();j++){
					/*Data Process*/
					String processStr=actData.get(j);
					//String processStr=dataProcess(actData.get(j));
					
					if(trainArr[j]){
						trainingDataWriter.write(processStr+"\r\n");				
						trainingDataWriter.flush();	
					}else{

						testingDataWriter.write(processStr+"\r\n");
						testingDataWriter.flush();			
					}
				}
			}
			trainingDataWriter.close();
			testingDataWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public Map<String, int[]> randomSelect(Map<String,ArrayList<String>> dataMap){
					
			Set<String>  keys = dataMap.keySet();
			Map<String, int[]> cvDataMap= new LinkedHashMap<String, int[]>();

			
			for(String str:keys){
				ArrayList <String> actData= dataMap.get(str);
				int[]trainArr = new int[actData.size()];
				
				for(int i=0;i<trainArr.length;i++){
					trainArr[i]=crossParameter;
				}
				for(int i=1;i<=crossParameter-1;i++){
					double count=0;
					int chooseNum=trainArr.length/crossParameter;

					Random r=new Random();
					while(count<  chooseNum){
						int selectIndex = r.nextInt(trainArr.length);
						if(trainArr[selectIndex]==crossParameter){
							trainArr[selectIndex]=i;
							count++;
						}
					}
				}
				/*debug count*/
				int c1=0,c2=0,c3=0,c4=0;
				for(int i=0;i<trainArr.length;i++){
					if(trainArr[i]==1)c1++;
					if(trainArr[i]==1)c2++;
					if(trainArr[i]==1)c3++;
					if(trainArr[i]==1)c4++;
				}
				cvDataMap.put(str, trainArr);

				
			}
			
			return cvDataMap;

	
	}
	public void wbCVData(Map<String,ArrayList<String>> dataMap,Map<String,int[]> cvDataMap,int testNumIndex){
		try {			
			Set<String>  keys = dataMap.keySet();
			FileWriter trainingDataWriter = new FileWriter(new File(
					cvTrainingDataPath));
			FileWriter testingDataWriter = new FileWriter(new File(
					cvTestDataPath));
			
			for(String str:keys){
				ArrayList <String> actData= dataMap.get(str);
				int [] dataIndex=cvDataMap.get(str);

/*拿多份train的方式*/
				for(int j=0;j<actData.size();j++){
					/*Data Process*/
					String processStr=actData.get(j);
					
					if(! (dataIndex[j]==testNumIndex) ){
						trainingDataWriter.write(processStr+"\r\n");				
						trainingDataWriter.flush();	
					}else{

						testingDataWriter.write(processStr+"\r\n");
						testingDataWriter.flush();			
					}
/*只拿一份train的方式*/	
//					if(dataIndex[j]==testNumIndex ){
//						trainingDataWriter.write(processStr+"\r\n");				
//						trainingDataWriter.flush();	
//					}else{
//
//						testingDataWriter.write(processStr+"\r\n");
//						testingDataWriter.flush();			
//					}
					
				}
			}
			trainingDataWriter.close();
			testingDataWriter.close();
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	

	
	public void wbResult(String path, Map <String,ExpResult> expResult,Boolean append){
		try {
			FileWriter writer = new FileWriter(new File(path),append);
			//write out testing result for single activity
			Set<String> resultKey = expResult.keySet();
			for (String str : resultKey) {
				double tp = expResult.get(str).tp;
				double tn = expResult.get(str).tn;
				double fp = expResult.get(str).fp;
				double fn = expResult.get(str).fn;
				double precision = expResult.get(str).tp
						/ (expResult.get(str).tp + expResult.get(str).fp);
				double recall = expResult.get(str).tp
						/ (expResult.get(str).tp + expResult.get(str).fn);
				
				writer.write(str + ": Precision=" + precision + " Recall="
						+ recall +  "\r\n");
				//writer.write(" tp:"+tp+" tn:"+tn+" fp:"+fp+" fn:"+fn+"\r\n");
				writer.flush();
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void wbResult(String path, ArrayList<Map <String,ExpResult>>  allExpResult,Boolean append){
		try {
			FileWriter writer = new FileWriter(new File(path),append);
			//write out testing result for single activity
			for(Map <String,ExpResult> expResult: allExpResult){
				Set<String> resultKey = expResult.keySet();
				for (String str : resultKey) {

					double tp = expResult.get(str).tp;
					double tn = expResult.get(str).tn;
					double fp = expResult.get(str).fp;
					double fn = expResult.get(str).fn;
					double precision = expResult.get(str).tp
							/ (expResult.get(str).tp + expResult.get(str).fp);
					double recall = expResult.get(str).tp
							/ (expResult.get(str).tp + expResult.get(str).fn);
					
					writer.write(str + ": Precision=" + precision + " Recall="
							+ recall +  "\r\n");
					//writer.write(" tp:"+tp+" tn:"+tn+" fp:"+fp+" fn:"+fn+"\r\n");
					writer.flush();
				}
				writer.write("#######\r\n");
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public Map <String,ExpResult> addResult(Map <String,ExpResult> finalResult,Map <String,ExpResult> tmpResult)
	{
					
					//add tmpResult to finalResult
					Set<String>  resultKey = tmpResult.keySet();
					for(String str:resultKey){
						ExpResult r1=finalResult.get(str);
						ExpResult r2=tmpResult.get(str);
						ExpResult.add(r1,r2);
						finalResult.put(str, r1);
					}
						
				
				return finalResult;
	}
	public ArrayList<Map <String,ExpResult>> addResult(ArrayList<Map <String,ExpResult>> finalResult,ArrayList<Map <String,ExpResult>> tmpResult)
	{
			int minSize=0;
			if(tmpResult.size()<=finalResult.size()){
				minSize=tmpResult.size();
			}else{
				minSize=finalResult.size();
			}
					
			for(int i=0;i<minSize;i++){
				Map <String,ExpResult> tmpResultMember=tmpResult.get(i);
				Map <String,ExpResult> finalResultMember=finalResult.get(i);
				Set<String>  resultKey = tmpResultMember.keySet();
				for(String str:resultKey){
					ExpResult r1=finalResultMember.get(str);
					ExpResult r2=tmpResultMember.get(str);
					ExpResult.add(r1,r2);
					finalResultMember.put(str, r1);
				}
				finalResult.set(i, finalResultMember);
			}
					//add tmpResult to finalResult

						
				
				return finalResult;
	}
	
	public CrossValidate(){}
	
	public void addNoise(String inPath, String outPath){
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
				
				if( split2.length>1 &&(!split2[0].equals("Sleeping")) &&(!split2[1].equals("Sleeping"))&&!(split2[0].equals(split2[1])) && !(split2[0].equals("NO") || split2[1].equals("NO") )){
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
	
	public ArrayList<Map <String,ExpResult>> cvProcess(){
		//Build test and training data Map
		Map<String,ArrayList<String>> dataMap=testDataTransform();
	
		Set<String>  keys = dataMap.keySet();
		Map <String,ExpResult> finalResult=null;
		Map <String,ExpResult> GAfinalResult=null;
		//Loop for all data
		try {
			Map<String,int []> cvDataMap=randomSelect(dataMap);
			for(int i=1;i<=crossParameter;i++){
	
				wbCVData(dataMap,cvDataMap,i);
				
				copyData(cvTrainingDataPath,"./_input_data/CrossValidate/cvTmp/cv_training_data_"+i+".txt");
				copyData(cvTestDataPath,"./_input_data/CrossValidate/cvTmp/cv_test_data_"+i+".txt");
				
				/*製造noise給trainingData*/
				addNoise("./_input_data/CrossValidate/cvTmp/cv_training_data_"+i+".txt",cvTrainingDataPath);
				
				//DBN testing
				  DbnClassifier DBN = new DbnClassifier();
				  EtcGenerator ETC = new EtcGenerator();
				  GaGenerator GA= new GaGenerator();
				  text2Arff CVT = new text2Arff();
				  GaDbnClassifier GaDBN = new GaDbnClassifier();
				  
					/*build GA*/
					GA.buildGaList();
			    	
			    	CVT.convertRawToArff(cvTrainingDataPath);
			    	CVT.writeClusterArff(cvTrainingDataPath);
			    	CVT.convertGaRawToArff(GA, cvTrainingDataPath);
					/*build model*/
					DBN.buildARModel(true); //build AR model	
					ETC.buildAllETC(DBN.classifier,"./_output_results/"+i+"etc.txt");//build ETC
				    DBN.allSetDefaultValue(true); //initial model    
					/*build GA Model*/
					GaDBN.buildGaModel(GA,true);
					GaEtcGenerator GAETC = new GaEtcGenerator(GA);
					GAETC.buildAllETC(GaDBN.classifier, "./_output_results/"+i+"ga_etc.txt",GA);
					/*initialGaModel*/
					GaDBN.allSetDefaultValue(GA);
					
					/*Testing*/
					Map <String,ExpResult> GAtmpResult=GaDBN.testing(GA, cvTestDataPath, "./_output_results/ga_testing_result_cv_"+i+".txt");
					Map <String,ExpResult> tmpResult=DBN.testing(GA,cvTestDataPath,"./_output_results/_testing_result_cv_"+i+".txt");
				  		
					if(i==1){
						finalResult=tmpResult;
						GAfinalResult=GAtmpResult;
					}else{
						finalResult= addResult(finalResult,tmpResult);
						GAfinalResult= addResult(GAfinalResult,GAtmpResult);
					}
						
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		wbResult(cvResultPath,finalResult,false);
		wbResult(cvResultPath,GAfinalResult,true);
		ArrayList<Map <String,ExpResult>> oneRoundResult = new ArrayList<Map <String,ExpResult>> ();
		oneRoundResult.add(finalResult);
		oneRoundResult.add(GAfinalResult);
		return oneRoundResult;
	}
	
	public  ArrayList<Map <String,ExpResult>> cvProcessForHga(){
		//Build test and training data Map
		Map<String,ArrayList<String>> dataMap=testDataTransform();
		String cvResultPath="./_output_results/HGA_cv_result.txt";


		ArrayList<Map <String,ExpResult>> HGAfinalResult=null;
		//Loop for all data
		try {
			Map<String,int []> cvDataMap=randomSelect(dataMap);
			for(int i=1;i<=crossParameter;i++){
				cvRound=i;
				wbCVData(dataMap,cvDataMap,i);
				
				copyData(cvTrainingDataPath,"./_input_data/CrossValidate/cvTmp/cv_training_data_"+i+".txt");
				copyData(cvTestDataPath,"./_input_data/CrossValidate/cvTmp/cv_test_data_"+i+".txt");
				
				/*製造noise給trainingData*/
				//addNoise("./_input_data/CrossValidate/cvTmp/cv_training_data_"+i+".txt",cvTrainingDataPath);
				
				ArrayList<Map <String,ExpResult>> kExpResultTmp=Epcieold.sysProc2(cvTestDataPath, cvTrainingDataPath,  "./_output_results/hga_testing_result_cv_"+i+".txt");
				
				if(i==1){
					HGAfinalResult=kExpResultTmp;
				}else{
					HGAfinalResult= addResult(HGAfinalResult,kExpResultTmp);
				}
			
				System.out.println("cv "+i+"finish");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//wbResult(cvResultPath,HGAfinalResult,false);
		
		return HGAfinalResult;

	}
	
	public static void main(String[] args)
    {
		ArrayList<Map <String,ExpResult>> finalResult=null ;
		for(int i=0;i<1;i++){
			CrossValidate cv= new CrossValidate();
			ArrayList<Map <String,ExpResult>> oneRoundResult=cv.cvProcessForHga();
			if(i==0){
				finalResult=oneRoundResult;
			}else{
				Map <String,ExpResult> singleTmp=cv.addResult(finalResult.get(0),oneRoundResult.get(0));
				finalResult.set(0, singleTmp);
				//finalResult.add(singleTmp);
			}
		}
		CrossValidate cv= new CrossValidate();
		cv.wbResult("./_output_results/HGA_cv_result.txt",finalResult,false);

//		CrossValidate cv= new CrossValidate();
//
//		cv.cvProcessForHga();
    }
}
