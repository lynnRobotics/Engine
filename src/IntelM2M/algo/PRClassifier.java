package IntelM2M.algo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.ESService;
import IntelM2M.datastructure.PRmodel;
import IntelM2M.epcie.GaGenerator;
import IntelM2M.epcie.erc.GaEscGenerator;

public class PRClassifier {
	ArrayList<PRmodel> prModelList=new ArrayList<PRmodel>();
	double threshold=0.9;
		
	public int getModelIndex(ArrayList<String> prFeature){
		for( int i=0;i<prModelList.size();i++ ){
			PRmodel model=prModelList.get(i);
			if(model.prFeature.equals(prFeature)){
				return i;
			}
		}
			
		return -1;
	}
	
	public PRmodel inferPR(ArrayList<String> prFeature){
		int index=getModelIndex(prFeature);
		if(index<0){
			return null;
		}
		/*如果有這個user context*/
		else{
			/*infer preference model*/
			PRmodel model=prModelList.get(index);
			/*return model*/
			return model;
		}
	}
	
	public void updatePrModel( PRmodel prModel){
		int index=getModelIndex(prModel.prFeature);
		if(index<0){

		}
		/*如果有這個user context*/
		else{
			/*infer preference model*/
			prModelList.set(index, prModel);
			prModelList.get(index);

		}
	}
	
	public void buildPrModel(ArrayList<Map <String,Boolean>> rawInferData,ArrayList<GaEscGenerator> GaEscList,ArrayList<GaGenerator> GaGeneratorList,String read){
		PRmodel model=new PRmodel();
		model.prFeature=getPrInferFeature(rawInferData);
		model.selectedPrFeature=prFeatureSelection(rawInferData,GaEscList,GaGeneratorList,read);
		model.esServiceList=buildESService(rawInferData,GaEscList,GaGeneratorList,model);
		model.escList=getEscList(model.selectedPrFeature,GaEscList);
		model.rule12=build12rule(model);
		prModelList.add(model);
	}
	public Map<Integer,ArrayList<AppNode>> build12rule(PRmodel model){
		Map<Integer,ArrayList<AppNode>> rule12= new LinkedHashMap<Integer,ArrayList<AppNode>>();
	
		/*init*/
		for(int i=1;i<=12;i++){
			ArrayList <AppNode> appList = new ArrayList<AppNode>();
			rule12.put(i, appList);
		}

		/*Find ESC*/
		ArrayList<AppNode> escList=model.escList;
	
		for(AppNode app:escList){
			if(app.escType.equals("explicit")&& app.state.equals("on")){
				if(app.confidence>=threshold){
					/*1 explict+on+yes*/
					rule12.get(1).add(app);
				}else{
					/*7 explict+on+no*/
					rule12.get(7).add(app);
				}
			}
			else if(app.escType.equals("explicit")&& app.state.equals("off")){
				if(app.confidence>=threshold){
					/*2 explict+off+yes*/
					rule12.get(2).add(app);
				}else{
					/*8 explict+off+no*/
					rule12.get(8).add(app);
				}
			}else if(app.escType.equals("explicit")&& app.state.equals("standby")){
				if(app.confidence>=threshold){
					/*3 explict+standby+yes*/
					rule12.get(3).add(app);
				}else{
					/*9 explict+standby+no*/
					rule12.get(9).add(app);
				}
			}else if(app.escType.equals("implicit")&& app.state.equals("on")){
				if(app.confidence>=threshold){
					/*4 implicit+on+yes*/
					rule12.get(4).add(app);
				}else{
					/*10 implicit+on+no*/
					rule12.get(10).add(app);
				}
			}else if(app.escType.equals("implicit")&& app.state.equals("off")){
				if(app.confidence>=threshold){
					/*5 implicit+off+yes*/
					rule12.get(5).add(app);
				}else{
					/*11 implicit+off+no*/
					rule12.get(11).add(app);
				}
			}else if(app.escType.equals("implicit")&& app.state.equals("standby")){
				if(app.confidence>=threshold){
					/*6 implicit+standby+yes*/
					rule12.get(6).add(app);
				}else{
					/*12 implicit+standby+no*/
					rule12.get(12).add(app);
				}
			}
		}
		return rule12;
	}
	
	public ArrayList<ESService> buildESService2(PRmodel model){
			ArrayList<ESService> esServiceList= new ArrayList<ESService> ();
			ESService ess0=new ESService(0);
			ESService ess1=new ESService(1);
			ESService ess2=new ESService(2);
			Map<Integer,ArrayList<AppNode>> rule12= model.rule12;
			/*build ES policy 0 */
			for(AppNode app: rule12.get(2)){
				AppNode tmp=app.copyAppNode(app);
				ess0.appList.add(tmp);
			}
			for(AppNode app: rule12.get(5)){
				AppNode tmp=app.copyAppNode(app);
				ess0.appList.add(tmp);
			}
			/*build ES policy 1*/
			
			esServiceList.add(ess0);
			esServiceList.add(ess1);
			esServiceList.add(ess2);
			return esServiceList;
			
	}
	
	public  ArrayList<ESService> buildESService(ArrayList<Map <String,Boolean>> rawInferData,ArrayList<GaEscGenerator> GaEscList,ArrayList<GaGenerator> GaGeneratorList,PRmodel model){
		 ArrayList<ESService> esServiceList= new ArrayList<ESService> ();
		ESService ess0=new ESService(0);
		ESService ess1=new ESService(1);
		ESService ess2=new ESService(2);
		/*prFeature selection*/
		ArrayList<String> selectedPrFeature=model.selectedPrFeature;
		/*Find ESC*/
		ArrayList<AppNode> escList=getEscList(selectedPrFeature,GaEscList);
		/*build ES policy 0 */
		for(AppNode app:escList){
			if( (app.escType.equals("explicit") ||app.escType.equals("implicit") ) && app.confidence>=threshold){
				if(app.state.equals("standby") || app.state.equals("off")){
					AppNode tmp=app.copyAppNode(app);
					ess0.appList.add(tmp);
				}
			}
		}
		/*build ES policy 1 */
		for(AppNode app:escList){
			if(app.escType.equals("implicit") && app.confidence>=threshold){
				if(app.state.equals("on")){
					AppNode tmp=app.copyAppNode(app);
					ess1.appList.add(tmp);
				}
			}
		}
		/*build ES policy 2 */
		for(AppNode app:escList){
			if(app.escType.equals("explicit") && app.confidence>=threshold){
				if(app.state.equals("on")){
					AppNode tmp=app.copyAppNode(app);
					ess2.appList.add(tmp);
				}
			}
		}
		esServiceList.add(ess0);
		esServiceList.add(ess1);
		esServiceList.add(ess2);
		return esServiceList;
	}

	public ArrayList<String> prFeatureSelection(ArrayList<Map <String,Boolean>> rawInferData,ArrayList<GaEscGenerator> GaEscList,ArrayList<GaGenerator> GaGeneratorList,String read){	
		for(int k=0;k<GaGeneratorList.size();k++){
			Map <String,Boolean> GAinferResult=rawInferData.get(k);
			if(GAinferResult.size()!=0){
				Set <String> gaSet=GAinferResult.keySet();
				for(String str:gaSet){
					if(GAinferResult.get(str)){
						/*砍掉有包含這個GA成員 且階層比k大的其他GA*/
						GaGenerator GA=GaGeneratorList.get(k);
						ArrayList<String> memberList=GA.getGroupMember(str);
						/*往後面階層檢查，有包含成員的GA infer result就刪除*/
						for(int j=k+1;j<GaGeneratorList.size();j++){
							Map <String,Boolean> GAinferResultHigherLevel=rawInferData.get(j);
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
		
		ArrayList<String> selectedPrFeature=getPrInferFeature(rawInferData);
		String []split1=read.split("#");
		int humanNum =split1[1].split(" ").length;
		while(selectedPrFeature.size()>humanNum){
			selectedPrFeature.remove(selectedPrFeature.size()-1);
		}

		return selectedPrFeature;
	}
	
	  public Boolean checkMemberRepeat(ArrayList<String> memberList,ArrayList<String> higherMemberList){
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

	public ArrayList<AppNode>getEscList(ArrayList<String> prFeature,ArrayList<GaEscGenerator> GaEscList){
		ArrayList<AppNode> escList= new ArrayList<AppNode>();
		for(String str:prFeature){
			for(GaEscGenerator gaEsc:GaEscList){
				boolean containKey=gaEsc.actAppList.containsKey(str);
				if(containKey){
					ArrayList<AppNode> tmpList=gaEsc.actAppList.get(str).appList;
					for(AppNode tmp:tmpList){
						AppNode app= tmp.copyAppNode(tmp);
						/*check escList before add*/
						int same=-1;
						for(int i=0;i<escList.size();i++){
							if(escList.get(i).appName.equals(app.appName)){
								same=i;
							}
						}
						if(same<0){
							escList.add(app);
						}else{
							/*set  appliance to highest state on->standby->off*/
							/*set appliance to highest state explicit->implicit*/
							if(escList.get(same).state.equals("on") && app.state.equals("on")){
								if(escList.get(same).escType.equals("explicit") || app.escType.equals("explicit")){
									escList.get(same).escType="explicit";
								}
							}else if( app.state.equals("on")){
								escList.get(same).state=app.state;
								escList.get(same).escType=app.escType;
								escList.get(same).confidence=app.confidence;
							}else if(escList.get(same).state.equals("on")){
								
							}
						}
					}
				}
			}
		
		}
		return escList;
	}
	
	public ArrayList<String> getPrInferFeature(ArrayList<Map <String,Boolean>> rawInferData){
		ArrayList<String> prFeature=new ArrayList<String>();
		for(Map<String,Boolean> GAresult: rawInferData ){
			for(String str:GAresult.keySet()){
				if(GAresult.get(str)==true){
					prFeature.add(str);
				}
			}
		}
		
		return prFeature;
		
	}
}
