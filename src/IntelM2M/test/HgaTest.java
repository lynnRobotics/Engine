package IntelM2M.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import IntelM2M.algo.Prior;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.ExpResult;
import IntelM2M.datastructure.GroupActivity;
import IntelM2M.datastructure.SensorNode;
import IntelM2M.epcie.GaGenerator;
import IntelM2M.epcie.classifier.GaDbnClassifier;
import IntelM2M.func.CrossValidate;

public class HgaTest {

	  static public ArrayList<Map <String,ExpResult>> hgaTesting(ArrayList<GaGenerator> GaGeneratorList,ArrayList<GaDbnClassifier> GaDbnList,String testingDataPath){
		   	try {
		   			int round=CrossValidate.cvRound;
			   		for(int k=0;k<GaGeneratorList.size();k++){
			   			
			   			FileWriter writer = new FileWriter(new File("./_output_results/"+round+"_hga_testing_result"+k+".txt"),false);
			   		}
					BufferedReader reader = new BufferedReader(new FileReader(testingDataPath));
					//FileWriter writer = new FileWriter(new File(resultPath));
					String read=null;
					Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;

					/*k層有k個gaList,k個expResult*/
					ArrayList<ArrayList<String>> kGaList=new ArrayList<ArrayList<String>>();
					ArrayList<Map <String,ExpResult>> kExpResult=new ArrayList<Map <String,ExpResult> >();
					
					for(int k=0;k<GaGeneratorList.size();k++){
						/*build gaList*/
						Set<String> gSet=GaGeneratorList.get(k).gaList.keySet();
				  		ArrayList<String> gaList= new ArrayList<String>();
				   		for(String str:gSet){
				   			gaList.add(str);
				   		}
				   		/*exp result for GA*/
						Map <String,ExpResult> expResult = new  LinkedHashMap<String,ExpResult>();
						for(int i=0;i<gaList.size();i++){
							ExpResult r= new ExpResult();
							expResult.put(gaList.get(i), r);
						}
				   		kGaList.add(gaList);
				   		kExpResult.add(expResult);
					}
					
					
					int highestLevel=0;
					while((read = reader.readLine()) != null)
					{
						/*根據Infer狀況來決定infer到第幾層*/
						Boolean continueFlag=true;
						/*每一筆data,k層最多做k次*/
						for(int k=0;k<GaGeneratorList.size()&&continueFlag;k++){
							if(k>highestLevel){
								highestLevel=k;
							}
							FileWriter writer = new FileWriter(new File("./_output_results/"+round+"_hga_testing_result"+k+".txt"),true);
							ArrayList<String> gaList=kGaList.get(k);
							GaGenerator GA=GaGeneratorList.get(k);
							GaDbnClassifier GaDBN=GaDbnList.get(k);
							
							String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
							/*initial */
							Map <String,Boolean> GAinferResult=new LinkedHashMap<String,Boolean>();
							Map <String,Boolean> groundTruth=new LinkedHashMap<String,Boolean>();
							Map <String,Boolean> GAgroundTruth=new LinkedHashMap<String,Boolean>();
							ArrayList<String> activityList=EnvStructure.activityList;
							for(int i=0;i<activityList.size();i++){
		
								groundTruth.put(activityList.get(i), false);
							}
							for(int i=0;i<gaList.size();i++){
								GAinferResult.put(gaList.get(i), false);
								GAgroundTruth.put(gaList.get(i), false);
							}
		
							
							String[] split = read.split("#");
							String []sensorContext=split[0].split(" ");
							ArrayList<String> rawFromDBN = new ArrayList<String>();
							ArrayList<String> inferDBN = new ArrayList<String>();
							Map<String,String> probDBN = new LinkedHashMap<String,String>();
							for(int i=0 ;i<sensorContext.length;i++){
								SensorNode s= new SensorNode(sensorName[i],sensorContext[i]);
								rawFromDBN = GaDBN.GaDBNInference(GA,s.name, s.discreteValue);
							}
		
							int humanNumber= split[1].split(" ").length;
							if(rawFromDBN.size()!=0){
								/*prior Knowledge 處理*/
								rawFromDBN= Prior.priorForInferenceGA(rawFromDBN,humanNumber,GA);
								/*這邊決定是否每一筆data都會跑testing，或是沒有Infer出來的結果才跑testing*/
								continueFlag=false;
							}
							for(String str:rawFromDBN){
								String []splitActPb=str.split(" ");
								inferDBN.add(splitActPb[0]);
								probDBN.put(splitActPb[0], splitActPb[1]);
							}
						
							/*record result*/
							for(String str:inferDBN){
								GAinferResult.put(str, true);
							}
		
							
							if(inferDBN.size()!=0){
								writer.write("DBN infer:");
								for(int i = 0; i < inferDBN.size(); i ++)
								{
		
									writer.write(" " + inferDBN.get(i)+" "+probDBN.get(inferDBN.get(i)));
									
		
								}
								writer.write(" |");
							}
							/*record ground truth*/
							String []truth=split[1].split(" ");
							/*去掉NO*/
							ArrayList<String>tmpArr= new ArrayList<String>();
							for(String str:truth){
								if(!str.equals("NO")){
									tmpArr.add(str);
								}
							}
							truth=(String[])tmpArr.toArray(new String[0]);
		
							for(String str:truth){
								groundTruth.put(str, true);
							}
							
							/*選擇要不要印truth*/
							writer.write(" truth:");
							//writer.write("  truth: "+ split[1]);
							//writer.write(" | ");
							writer.flush();
							
		
							
							/*record ground truth for GA*/			
							for(String str:truth){
								//String gid=GA.getGID(str);
								ArrayList<String> gidArr=GA.getGID(str);
								for(String GID:gidArr){
									GAgroundTruth.put(GID, true);
								}
								
							}
		
							
							Set<String> keys=GAgroundTruth.keySet();
							for(String str:keys){
								if(GAgroundTruth.get(str)==true){
									String truthProb=GaDBN.allGaProb.get(str);
									writer.write(" "+str+" "+truthProb);
								}
							}
										
		
							

							Map <String,ExpResult> expResult=kExpResult.get(k);	
							/*record tp tn fp fn for GA*/
							for(String str:gaList){
								if(GAgroundTruth.get(str)==true && GAinferResult.get(str)==true){
									ExpResult r=expResult.get(str);
									r.tp+=1;
									expResult.put(str, r);
								}else if(GAgroundTruth.get(str)==false && GAinferResult.get(str)==true){
									
									ExpResult r=expResult.get(str);
									r.fp+=1;
									expResult.put(str,r);
								}else if(GAgroundTruth.get(str)==true && GAinferResult.get(str)==false){
									ExpResult r=expResult.get(str);
									r.fn+=1;
									expResult.put(str,r);
								}else if(GAgroundTruth.get(str)==false && GAinferResult.get(str)==false){
									ExpResult r=expResult.get(str);
									r.tn+=1;
									expResult.put(str,r);
								}
							}
		
						
							writer.write("\r\n");
							writer.flush();
						}
					}

					/*write final result*/
					for(int k=0;k<=highestLevel;k++){
						
						FileWriter writer = new FileWriter(new File("./_output_results/"+round+"_hga_testing_result"+k+".txt"),true);
						ArrayList<String> gaList=kGaList.get(k);
						Map <String,ExpResult> expResult=kExpResult.get(k);
						GaGenerator GA=GaGeneratorList.get(k);
						
						writer.write("####################\r\n\r\n");
						for(String str:gaList){
							double precision= expResult.get(str).tp /(expResult.get(str).tp+expResult.get(str).fp);
							double recall= expResult.get(str).tp /(expResult.get(str).tp+expResult.get(str).fn);
							
							writer.write(str+": Precision="+precision+" Recall="+recall+"\r\n");
							writer.flush();
						}
						/*write GA member*/
						for(String str:gaList){
							ArrayList<String> actList= GA.getGroupMember(str);
							writer.write(str+" :");
							for(String str2: actList){
								writer.write(str2+"  ");
							}
							writer.write("\r\n");
							writer.flush();
						}
					}

					System.out.println("Testing Finish");
								
					/*return test result*/

					return kExpResult;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
		   	
		   	
		   	
		   }
	  
	  static private Boolean checkMemberRepeat(ArrayList<String> memberList,ArrayList<String> higherMemberList){
			/*檢查member是否有重複*/
			for(String str3:higherMemberList){
				for(String str4:memberList){
					if(str3.equals(str4)){
						return true;
					}
				}
			}
			return false;
	  }
	  
	  static public ArrayList<Map <String,ExpResult>> hgaTesting2(ArrayList<GaGenerator> GaGeneratorList,ArrayList<GaDbnClassifier> GaDbnList,String testingDataPath){
		   	try {
		   			int round=CrossValidate.cvRound;
			   		for(int k=0;k<GaGeneratorList.size();k++){
			   			
			   			FileWriter writer = new FileWriter(new File("./_output_results/"+round+"_hga_testing_result"+k+".txt"),false);
			   		}
					BufferedReader reader = new BufferedReader(new FileReader(testingDataPath));
					//FileWriter writer = new FileWriter(new File(resultPath));
					String read=null;
					Map<String, ArrayList<String>> sensorStatus=EnvStructure.sensorStatus;

					/*k層有k個gaList,k個expResult*/
					ArrayList<ArrayList<String>> kGaList=new ArrayList<ArrayList<String>>();
					ArrayList<Map <String,ExpResult>> kExpResult=new ArrayList<Map <String,ExpResult> >();
					
					for(int k=0;k<GaGeneratorList.size();k++){
						/*build gaList*/
						Set<String> gSet=GaGeneratorList.get(k).gaList.keySet();
				  		ArrayList<String> gaList= new ArrayList<String>();
				   		for(String str:gSet){
				   			gaList.add(str);
				   		}
				   		/*exp result for GA*/
						Map <String,ExpResult> expResult = new  LinkedHashMap<String,ExpResult>();
						for(int i=0;i<gaList.size();i++){
							ExpResult r= new ExpResult();
							expResult.put(gaList.get(i), r);
						}
				   		kGaList.add(gaList);
				   		kExpResult.add(expResult);
					}
					
					
					int highestLevel=0;

					while((read = reader.readLine()) != null)
					{
						/*根據Infer狀況來決定infer到第幾層*/
						Boolean continueFlag=true;
						/*每一筆data,k層最多做k次*/
						ArrayList<Map <String,Boolean>> GAinferResultList= new ArrayList<Map <String,Boolean> >();
						ArrayList<Map <String,Boolean>> GAgroundTruthList= new ArrayList<Map <String,Boolean> >();
						for(int k=0;k<GaGeneratorList.size()&&continueFlag;k++){
							if(k>highestLevel){
								highestLevel=k;
							}
							FileWriter writer = new FileWriter(new File("./_output_results/"+round+"_hga_testing_result"+k+".txt"),true);
							ArrayList<String> gaList=kGaList.get(k);
							GaGenerator GA=GaGeneratorList.get(k);
							GaDbnClassifier GaDBN=GaDbnList.get(k);
							
							String [] sensorName=(String[])sensorStatus.keySet().toArray(new String[0]);
							/*initial */
							Map <String,Boolean> GAinferResult=new LinkedHashMap<String,Boolean>();
							Map <String,Boolean> groundTruth=new LinkedHashMap<String,Boolean>();
							Map <String,Boolean> GAgroundTruth=new LinkedHashMap<String,Boolean>();
							ArrayList<String> activityList=EnvStructure.activityList;
							for(int i=0;i<activityList.size();i++){
		
								groundTruth.put(activityList.get(i), false);
							}
							for(int i=0;i<gaList.size();i++){
								GAinferResult.put(gaList.get(i), false);
								GAgroundTruth.put(gaList.get(i), false);
							}
		
							
							String[] split = read.split("#");
							String []sensorContext=split[0].split(" ");
							ArrayList<String> rawFromDBN = new ArrayList<String>();
							ArrayList<String> inferDBN = new ArrayList<String>();
							Map<String,String> probDBN = new LinkedHashMap<String,String>();
							for(int i=0 ;i<sensorContext.length;i++){
								SensorNode s= new SensorNode(sensorName[i],sensorContext[i]);
								rawFromDBN = GaDBN.GaDBNInference(GA,s.name, s.discreteValue);
							}
		
							int humanNumber= split[1].split(" ").length;
							if(rawFromDBN.size()!=0){
								/*prior Knowledge 處理*/
								rawFromDBN= Prior.priorForInferenceGA(rawFromDBN,humanNumber,GA);
										
							}
							for(String str:rawFromDBN){
								String []splitActPb=str.split(" ");
								inferDBN.add(splitActPb[0]);
								probDBN.put(splitActPb[0], splitActPb[1]);
							}
						
							/*record result*/
							for(String str:inferDBN){
								GAinferResult.put(str, true);
							}
		
							
							if(inferDBN.size()!=0){
								writer.write("DBN infer:");
								for(int i = 0; i < inferDBN.size(); i ++)
								{
		
									writer.write(" " + inferDBN.get(i)+" "+probDBN.get(inferDBN.get(i)));
									
		
								}
								writer.write(" |");
							}
							/*record ground truth*/
							String []truth=split[1].split(" ");
							/*去掉NO*/
							ArrayList<String>tmpArr= new ArrayList<String>();
							for(String str:truth){
								if(!str.equals("NO")){
									tmpArr.add(str);
								}
							}
							truth=(String[])tmpArr.toArray(new String[0]);
														
		
							for(String str:truth){
								groundTruth.put(str, true);
							}
							
							/*選擇要不要印truth*/
							writer.write(" truth:");
							//writer.write("  truth: "+ split[1]);
							//writer.write(" | ");
							writer.flush();
							
		
							
							/*record ground truth for GA*/			
							for(String str:truth){
								//String gid=GA.getGID(str);
								ArrayList<String> gidArr=GA.getGID(str);
								for(String GID:gidArr){
									GAgroundTruth.put(GID, true);
								}
								
							}
		
							
							Set<String> keys=GAgroundTruth.keySet();
							for(String str:keys){
								if(GAgroundTruth.get(str)==true){
									String truthProb=GaDBN.allGaProb.get(str);
									writer.write(" "+str+" "+truthProb);
								}
							}
										
							GAgroundTruthList.add(GAgroundTruth);
							GAinferResultList.add(GAinferResult);
						
							writer.write("\r\n");
							writer.flush();
						}//end for
						
						/*delete GA infer result*/

						for(int k=0;k<GaGeneratorList.size();k++){
							Map <String,Boolean> GAinferResult=GAinferResultList.get(k);
							if(GAinferResult.size()!=0){
								Set <String> gaSet=GAinferResult.keySet();
								for(String str:gaSet){
									if(GAinferResult.get(str)){
										/*砍掉有包含這個GA成員 且階層比k大的其他GA*/
										GaGenerator GA=GaGeneratorList.get(k);
										ArrayList<String> memberList=GA.getGroupMember(str);
										/*往後面階層檢查，有包含成員的GA infer result就刪除*/
										for(int j=k+1;j<GaGeneratorList.size();j++){
											Map <String,Boolean> GAinferResultHigherLevel=GAinferResultList.get(j);
											Set <String> highLevelGaSet=GAinferResultHigherLevel.keySet();
											for(String str2:highLevelGaSet){
												GaGenerator higherGA=GaGeneratorList.get(j);
												ArrayList<String> higherMemberList=higherGA.getGroupMember(str2);
												Boolean repeat=checkMemberRepeat(memberList,higherMemberList);
												if(repeat){
													GAinferResultHigherLevel.remove(str2);
													break;
												}
											}
										}
									}
								}
							}
						}
						/*save final result*/
						ArrayList<String> SAfinalResult=new ArrayList<String>();
						ArrayList<GroupActivity> SAfinalmember=new ArrayList<GroupActivity>();
						for(int k=0;k<GaGeneratorList.size();k++){
							Map <String,Boolean> GAinferResult=GAinferResultList.get(k);
							if(GAinferResult.size()!=0){
								Set <String> gaSet=GAinferResult.keySet();
								for(String str:gaSet){
									if(GAinferResult.get(str)){
										SAfinalResult.add(str);
										GaGenerator GA=GaGeneratorList.get(k);
										SAfinalmember.add(GA.gaList.get(str));
									}
								}
							}
						}
						
						
						
						
						
						/*count ground truth*/
						int humanNumber=0;
						for(String str:GAinferResultList.get(0).keySet()){
							if(GAinferResultList.get(0).get(str)){
								humanNumber++;
							}
						}
								
						/*cal final result for single activity*/
						for(String str:kGaList.get(0)){
							Map <String,Boolean> GAinferResult=GAinferResultList.get(0);
							Map <String,Boolean> GAgroundTruth=GAgroundTruthList.get(0);
							int detectNumber=0;	
							for(String str2:GAinferResult.keySet()){
								if(GAinferResult.get(str2)){
									detectNumber++;
								}
							}
							
							if(GAinferResult.get(str)){
								continue;
							}
							
							String actName=GaGeneratorList.get(0).getGroupMember(str).get(0);
							int containerIndex=0;
	
							
							
							/*先判斷這個活動有沒有在某個GA裡*/
							Boolean exist=false;
							for(int i=0;i<SAfinalmember.size() &&!exist;i++){
								GroupActivity ga=SAfinalmember.get(i);
								for(String str2:ga.actMemberList){
									if(actName.equals(str2)){
										exist=true;
										containerIndex=i;
										break;
									}
								}
							}
							
							if(GAgroundTruth.get(str)==true){

								if(exist){
									GAinferResult.put(str, true);
									String gName=SAfinalResult.get(containerIndex);
									FileWriter writer = new FileWriter(new File("./_output_results/"+round+"_hga_testing_result0.txt"),true);
									writer.write("~"+ gName+"\r\n");
									writer.flush();
								}else{
									GAinferResult.put(str, false);

								}
							/*Ground truth沒有, GA結果有包含 就要另外判斷*/
							}else if(GAgroundTruth.get(str)==false){
								if(exist){
									Boolean containTruth=false;
									/*檢查包含此ground truth的GA成員是否有包含真的有發生的活動*/
									ArrayList<String> gaMember= SAfinalmember.get(containerIndex).actMemberList;
									
									for(String str2:gaMember){
										GaGenerator GA=GaGeneratorList.get(0);
										String gid=GA.getGID(str2).get(0);
										if(GAgroundTruth.get(gid)){
											containTruth=true;
										}
									}
									if(containTruth)
										GAinferResult.put(str, false);
									else{
										if(detectNumber<humanNumber){
											GAinferResult.put(str, true);
											String gName=SAfinalResult.get(containerIndex);
											FileWriter writer = new FileWriter(new File("./_output_results/"+round+"_hga_testing_result0.txt"),true);
											writer.write("~"+ gName+"\r\n");
											writer.flush();
										}
									}
								}else{
									GAinferResult.put(str, false);
								}
								
							}
							
						}
	
						for(String str:kGaList.get(0)){
							Map <String,ExpResult> expResult=kExpResult.get(0);	
							Map <String,Boolean> GAinferResult=GAinferResultList.get(0);
							Map <String,Boolean> GAgroundTruth=GAgroundTruthList.get(0);
							if(GAgroundTruth.get(str)==true && GAinferResult.get(str)==true){
							ExpResult r=expResult.get(str);
							r.tp+=1;
							expResult.put(str, r);
							}else if(GAgroundTruth.get(str)==false && GAinferResult.get(str)==true){
								
								ExpResult r=expResult.get(str);
								r.fp+=1;
								expResult.put(str,r);
							}else if(GAgroundTruth.get(str)==true && GAinferResult.get(str)==false){
								ExpResult r=expResult.get(str);
								r.fn+=1;
								expResult.put(str,r);
							}else if(GAgroundTruth.get(str)==false && GAinferResult.get(str)==false){
								ExpResult r=expResult.get(str);
								r.tn+=1;
								expResult.put(str,r);
							}
						}
						
						
						
					}
					


					/*write final result*/
					for(int k=0;k<=0;k++){
						
						FileWriter writer = new FileWriter(new File("./_output_results/"+round+"_hga_testing_result"+k+".txt"),true);
						ArrayList<String> gaList=kGaList.get(k);
						Map <String,ExpResult> expResult=kExpResult.get(k);
						GaGenerator GA=GaGeneratorList.get(k);
						
						writer.write("####################\r\n\r\n");
						for(String str:gaList){
							double precision= expResult.get(str).tp /(expResult.get(str).tp+expResult.get(str).fp);
							double recall= expResult.get(str).tp /(expResult.get(str).tp+expResult.get(str).fn);
							
							writer.write(str+": Precision="+precision+" Recall="+recall+"\r\n");
							writer.flush();
						}
						/*write GA member*/
						for(String str:gaList){
							ArrayList<String> actList= GA.getGroupMember(str);
							writer.write(str+" :");
							for(String str2: actList){
								writer.write(str2+"  ");
							}
							writer.write("\r\n");
							writer.flush();
						}
					}

					System.out.println("Testing Finish");
								
					/*return test result*/

					return kExpResult;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
		   	
		   	
		   	
		   }
}
