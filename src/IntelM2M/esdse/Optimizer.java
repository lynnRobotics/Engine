package IntelM2M.esdse;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import s2h.platform.node.Sendable;

import IntelM2M.agent.ap.APAgent;
import IntelM2M.agent.thermal.ThermalAgent;
import IntelM2M.agent.visual.VisualAgent;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.epcie.GAinference;
import IntelM2M.exp.ExpRecorder;
import IntelM2M.mq.Producer;

public class Optimizer {
	Producer producer;
	
	/* Default constructor */
	public Optimizer() {
	}
	
	/* Constructor with setting producer */
	public Optimizer(Producer producer) {
			this.producer = producer;
	}
	
	/* Update state of appliance according to candidate list */ 
	static public void updateState(ArrayList<AppNode> appList, String candidate) {
		String[] stateArr = candidate.split(",");

		for (int i = 0; i < stateArr.length; i++) {
			String state = stateArr[i];
			appList.get(i).envContext = state;
		}
	}

	/* Calculate energy consumption for real-time */
	static public double calEnergyConsumption(ArrayList<AppNode> decionList) {
		double amp = 0;
		try {
			Map<String, AppNode> appList = EnvStructure.appList;
			for (AppNode app : decionList) {
				AppNode rawApp = appList.get(app.appName);
				if(!rawApp.ampere.containsKey(app.envContext)){
					//int aa = 0;
					//aa++;
				} 
				else{
					amp += rawApp.ampere.get(app.envContext);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return amp;
	}

	/* Calculate energy consumption for simulation */
	static public double calEnergyConsumptionForSimulator(ArrayList<AppNode> decionList) {
		double amp = 0;
		/* for debug */
		int duration = ExpRecorder.exp.getDuration();
		/**/
		try {
			Map<String, AppNode> appList = EnvStructure.appList;

			for (AppNode app : decionList) {

				AppNode rawApp = appList.get(app.appName);

				if (!rawApp.ampere.containsKey(app.envContext)) {
					int aa = 0;
					aa++;
				} else {
					amp += (rawApp.ampere.get(app.envContext) * 110 * duration * 5 / (60 * 1000));

				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}

		return amp;

	}

	/* not finish, Build up a candidate list */
	public ArrayList<String> buildCandidateList(ArrayList<AppNode> candidateAppList) {
		// 判斷每個appliance有幾個state
		ArrayList<ArrayList<String>> applianceState = new ArrayList<ArrayList<String>>();
		for (AppNode app : candidateAppList) {
			Set<String> stateSet = app.ampere.keySet();
			ArrayList<String> stateList = new ArrayList<String>();
			for (String str : stateSet) {
				stateList.add(str);
			}
			applianceState.add(stateList);
		}
		// 然後找出所有的排列組合
		ArrayList<String> candidateList = new ArrayList<String>();
		// The number of appliances needed to be consider
		String[] strArr = new String[applianceState.size()];
		iterative(0, applianceState, candidateList, strArr);

		return candidateList;

	}

	/* Find all kinds of combinations */
	private void iterative(int i, ArrayList<ArrayList<String>> applianceState, ArrayList<String> candidateList, String[] strArr) {
		// agent 有可能沒有負責到任何電器
		if (applianceState.size() == 0) {
			return;
		}
		else {
			for(int j = 0; j < applianceState.get(i).size(); j++){
				strArr[i] = applianceState.get(i).get(j);
				if((i + 1) < applianceState.size()){
					iterative(i + 1, applianceState, candidateList, strArr);
				} 
				else{
					String ans = "";
					for(String str : strArr){
						ans += str + ",";
					}
					candidateList.add(ans);
				}
			}
		}
	}

	/* Get optimal decision list */
	public ArrayList<AppNode> getOptDecisionList(ArrayList<AppNode> eusList, GAinference gaInference) {
		/* for debug
		int t = 0, v = 0, ap = 0;
		for (AppNode app : eusList) {
			if (app.agentName.equals("thermal")) {
				t += 1;
			} else if (app.agentName.equals("visual")) {
				v += 1;
			} else if (app.agentName.equals("ap")) {
				ap += 1;
			}
		}*/

		// Thermal evaluation
		ThermalAgent ta = new ThermalAgent();
		ArrayList<AppNode> thermalList = ta.getOptThermalList(eusList, gaInference, producer);
		System.out.println("getOptThermalList done! thermalList = " + thermalList.size());
		// Visual evaluation
		VisualAgent va = new VisualAgent();
		ArrayList<AppNode> visualList = va.getOptVisualList(eusList, gaInference, producer);
		System.out.println("getOptVisualList done! visualList = " + visualList.size());
		// AP evaluation
		APAgent apA = new APAgent();
		ArrayList<AppNode> apList = apA.getOptApList(eusList, gaInference);
		
		// Combine
		ArrayList<AppNode> decisionList = new ArrayList<AppNode>();
		decisionList.addAll(thermalList);
		decisionList.addAll(visualList);
		decisionList.addAll(apList);

		return decisionList;
	}
	
	/* Get optimal decision list for M-CHESS online */
	public ArrayList<AppNode> getOptDecisionListForOnline(ArrayList<AppNode> eusList, GAinference gaInference) {
		/* for debug */
		int t = 0, v = 0, ap = 0;
		for (AppNode app : eusList) {
			if (app.agentName.equals("thermal")) {
				t += 1;
			} else if (app.agentName.equals("visual")) {
				v += 1;
			} else if (app.agentName.equals("ap")) {
				ap += 1;
			}
		}

		/* thermal evaluation */
		ThermalAgent ta = new ThermalAgent();
		ArrayList<AppNode> thermalList = ta.getOptThermalListForOnline(eusList, gaInference);
		System.out.println("getOptThermalList done! thermalList = " + thermalList.size());
		/* visual evaluation */
		VisualAgent va = new VisualAgent();
		ArrayList<AppNode> visualList = va.getOptVisualListForOnline(eusList, gaInference);
		System.out.println("getOptVisualList done! visualList = " + visualList.size());
		/* ap evaluation */
		APAgent apA = new APAgent();
		ArrayList<AppNode> apList = apA.getOptApList(eusList, gaInference);
		/* combine */
		ArrayList<AppNode> decisionList = new ArrayList<AppNode>();
		decisionList.addAll(thermalList);
		decisionList.addAll(visualList);
		decisionList.addAll(apList);

		return decisionList;
	}
}
