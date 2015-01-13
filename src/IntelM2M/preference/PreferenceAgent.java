package IntelM2M.preference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import IntelM2M.datastructure.AppNode;
import IntelM2M.epcie.GAinference;
import IntelM2M.epcie.GaGenerator;


public class PreferenceAgent {
	
	
	public class PreferenceModel{
		 ArrayList<String> prFeature= new ArrayList<String>();
		 ArrayList<AppNode> decisionList= new ArrayList<AppNode>();
		 Set<String> actInferResultSet = new HashSet<String>();
		 ArrayList<String> gaInferResultList=new ArrayList<String>();
		
		public Boolean inferUpdate=false;
		public Boolean controlUpdate=false;
		/*raw decision */
		public ArrayList<AppNode> rawDecisionList= new ArrayList<AppNode>();
		/*raw infer result*/
		public Set<String> rawSingleInferResult =null;
	}
	
	private int controlWrongCounter=0;
	private ArrayList<PreferenceModel> prModelList= new ArrayList<PreferenceModel>();

//	 public void userFeedback(GAinference gaInference,String read,String read2){
//		 /*update 的目標model是最後插入的那一個model*/
//		 PreferenceModel model= inferPRmodel(gaInference);
//		 		 		 
//		 /*check 是否有inference錯誤*/
//		 Boolean inferWrong=false;
//		 if(read!=null){
//			 String split[]=read.split("#");
//			 String split2[]=split[1].split(" ");
//			 /*check infer的結果是否正確*/
//			 for(String str:model.singleInferResult){
//				 Boolean contain=false;
//				 for(String str2:split2){
//					if(str.equals(str2)){
//						contain=true;
//					}
//				 }
//				 /*infer的結果不再 ground trugh 中*/
//				 if(contain==false){
//					 inferWrong=true;
//					 break;
//				 }
//			 }
//			 /*模擬 infer wrong 時的情形*/
//			 if(inferWrong==true){
//				 model.inferUpdate=true;
//				 model.rawSingleInferResult= new HashSet<String>();
//				 /*先複製原先錯的infer結果到 rawSingleInferResult*/
//				 for(String str:model.singleInferResult){
//					 model.rawSingleInferResult.add(str);
//				 }
//				 
//				 /*將infer 的結果更改為使用者更新的內容*/
//				 model.singleInferResult.clear();
//				 for(String str:split2){
//					 model.singleInferResult.add(str);
//				 }
//			 }
//			 
//		 }
//		 
//		 /*check ap agent是否關錯 並且 update decision list*/
//		
//		 if(!read2.equals("")){
//			 	String split[]=read2.split(" ");
//				/*先複製原先的decisionList到 rawDecisino中*/
//			 	for(AppNode app:model.decisionList){
//			 		AppNode app2=app.copyAppNode(app);
//			 		model.rawDecisionList.add(app2);
//			 	}
//			 	
//			 	/*判斷是否有無關錯*/
//				for(AppNode app:model.decisionList){
//					if(app.haveAPControlFromOn){
//						for(String str:split){
//							if(app.appName.equals(str)){
//								/*關對了*/
//							}else{
//								model.controlUpdate=true;
//								/*把關錯的打開*/
//								app.haveAPControlFromOn=false;
//								app.envContext="on";
//								controlWrongCounter++;
//							}
//						}
//					}else{
//						/*把沒關的關掉*/
//						for(String str:split){
//							if(app.appName.equals(str) && app.envContext.equals("on")){
//								app.haveAPControlFromOn=true;
//								app.envContext="off";
//							}
//						}
//					}
//			   }
//		}
//		 
//		 
//	 }
	 
	 
	 public void userFeedback_new(GAinference gaInference,String read,String read2){
		 /*update 的目標model是最後插入的那一個model*/
		 PreferenceModel model= inferPRmodel(gaInference);
		 		 		 
		 /*check 是否有inference錯誤*/
		 Boolean inferWrong=false;
		 if(read!=null && model!=null){
			 String split[]=read.split("#");
			 String split2[]=split[1].split(" ");
			 /*check infer的結果是否正確*/
			 if(model.actInferResultSet==null){
				 /*for debug*/
			 }else{
				 for(String str:model.actInferResultSet){
					 Boolean contain=false;
					 for(String str2:split2){
						if(str.equals(str2)){
							contain=true;
						}
					 }
					 /*infer的結果不再 ground trugh 中*/
					 if(contain==false){
						 inferWrong=true;
						 break;
					 }
				 }
			 }

			 /*模擬 infer wrong 時的情形*/
			 if(inferWrong==true){
				 model.inferUpdate=true;
				 model.rawSingleInferResult= new HashSet<String>();
				 /*先複製原先錯的infer結果到 rawSingleInferResult*/
				 for(String str:model.actInferResultSet){
					 model.rawSingleInferResult.add(str);
				 }
				 
				 /*將infer 的結果更改為使用者更新的內容*/
				 model.actInferResultSet.clear();
				 for(String str:split2){
					 model.actInferResultSet.add(str);
				 }
				 
				 model.gaInferResultList.clear();
				 /*根據single act result 來取得ga set*/
				 GaGenerator gaG=gaInference.GaGeneratorList.get(0);
				 for(String str:split2){
					 ArrayList<String> gaName=gaG.getGID(str);
					 model.gaInferResultList.add(gaName.get(0));
				 }
			 }
			 
		 }
	 		 
	 }
	 
	 public void updateInferResult(GAinference gaInference, PreferenceModel prM){
		 gaInference.actInferResultSet= prM.actInferResultSet;
		 gaInference.gaInferResultList=prM.gaInferResultList;
	 }

	 public   PreferenceModel inferPRmodel(GAinference gaInference){
			int index=-1;
			
			
			for( int i=0;i<prModelList.size();i++ ){
				PreferenceModel model=prModelList.get(i);
				Boolean featureSame=true;
				if(model.prFeature.size()!= gaInference.rawGAinferResultList.size()){
					featureSame=false;
				}else{
					
					for(String str: model.prFeature){
						Boolean contain=false;
						for(String str2: gaInference.rawGAinferResultList){
							if(str.equals(str2)){
								contain=true;
								break;
							}
						}
						if(contain==false){
							featureSame=false;
							break;
						}
					}
				}

				
				if(featureSame==true){
					/*這邊要debug 可能會有錯*/
					index=i;
					break;
				}
			}
			
			if(index<0){
				return null;
			}
			
			else{
				/*infer preference model*/
				PreferenceModel model=prModelList.get(index);
				/*return */
				return model;
			}
	
	 }
	 

	 
	 public void buildPRModel(ArrayList<AppNode> decisionList,GAinference gaInference){
		
		 
		 PreferenceModel model= new PreferenceModel();
		for(String str: gaInference.rawGAinferResultList){
			model.prFeature.add(str);
		}
		
		for(AppNode app:decisionList){
			AppNode app2=app.copyAppNode(app);
			model.decisionList.add(app2);
		}
		
		 for(String str:gaInference.actInferResultSet){
				model.actInferResultSet.add(str);
		}
		 
		 for(String str:gaInference.gaInferResultList){
			 model.gaInferResultList.add(str);
		 }
		
		prModelList.add(model);
		
	}
}
