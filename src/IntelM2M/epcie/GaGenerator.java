package IntelM2M.epcie;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import weka.classifiers.bayes.net.EditableBayesNet;
import weka.core.Instances;
import IntelM2M.algo.Classifier;
import IntelM2M.algo.GaEtcGenerator;
import IntelM2M.datastructure.AppNode;
import IntelM2M.datastructure.EnvStructure;
import IntelM2M.datastructure.GroupActivity;
import IntelM2M.datastructure.RelationTable;
import IntelM2M.epcie.erc.GaEscGenerator;
import IntelM2M.func.CrossValidate;

/*Mao Yuan Weng 2012/01*/

public class GaGenerator {

	public Map<String, GroupActivity> gaList = new LinkedHashMap<String, GroupActivity>();
	public Instances insts;
	public int level;

	public GaGenerator() {
		try {
			File dir = new File("./_weka_training_data");
			insts = new Instances(new FileReader("./_weka_training_data/"
					+ dir.list()[0]));
		} catch (Exception ex) {
		}
	}

	public GaGenerator(int level) {
		try {
			File dir = new File("./_weka_training_data");
			insts = new Instances(new FileReader("./_weka_training_data/"
		    			+ dir.list()[0]));
		} catch (Exception ex) {
		}
		this.level = level;
	}

	public void buildFirstGaList() {
		String[] activityList = (String[]) EnvStructure.activityList
				.toArray(new String[0]);
		int i = 0;
		for (String str : activityList) {
			GroupActivity ga = new GroupActivity("g1-"
					+ Integer.toString(i + 1));
			ga.actMemberList.add(activityList[i]);
			gaList.put(ga.GID, ga);
			i++;
		}
	}

	public void buildGaList() {

		// GroupActivity ga1=new GroupActivity("1");
		// GroupActivity ga2=new GroupActivity("2");
		// GroupActivity ga3=new GroupActivity("3");
		// GroupActivity ga4=new GroupActivity("4");
		// GroupActivity ga5=new GroupActivity("5");
		// GroupActivity ga6=new GroupActivity("6");
		// GroupActivity ga7=new GroupActivity("7");
		// GroupActivity ga8=new GroupActivity("8");
		//
		// ga1.actMemberList.add("WatchingTV");
		// ga1.actMemberList.add("PlayingKinect");
		//
		// ga2.actMemberList.add("PreparingFood");
		//
		// ga3.actMemberList.add("GoOut");
		// ga3.actMemberList.add("AllSleeping");
		//
		// ga4.actMemberList.add("UsingPC");
		// ga5.actMemberList.add("ComeBack");
		// ga6.actMemberList.add("Reading");
		// ga7.actMemberList.add("Sleeping");
		// ga8.actMemberList.add("Chatting");
		//
		// gaList.put(ga1.GID, ga1);
		// gaList.put(ga2.GID, ga2);
		// gaList.put(ga3.GID, ga3);
		// gaList.put(ga4.GID, ga4);
		// gaList.put(ga5.GID, ga5);
		// gaList.put(ga6.GID, ga6);
		// gaList.put(ga7.GID, ga7);
		// gaList.put(ga8.GID, ga8);

		GroupActivity ga1 = new GroupActivity("1");
		GroupActivity ga2 = new GroupActivity("2");
		GroupActivity ga3 = new GroupActivity("3");
		GroupActivity ga4 = new GroupActivity("4");
		GroupActivity ga5 = new GroupActivity("5");
		GroupActivity ga6 = new GroupActivity("6");
		GroupActivity ga7 = new GroupActivity("7");
		GroupActivity ga8 = new GroupActivity("8");
		GroupActivity ga9 = new GroupActivity("9");
		GroupActivity ga10 = new GroupActivity("10");
		ga1.actMemberList.add("WatchingTV");
		ga2.actMemberList.add("PlayingKinect");
		ga3.actMemberList.add("Chatting");
		ga4.actMemberList.add("Reading");
		ga5.actMemberList.add("UsingPC");
		ga6.actMemberList.add("ComeBack");
		ga7.actMemberList.add("GoOut");
		ga8.actMemberList.add("PreparingFood");
		ga9.actMemberList.add("Sleeping");
		ga10.actMemberList.add("AllSleeping");
		gaList.put(ga1.GID, ga1);
		gaList.put(ga2.GID, ga2);
		gaList.put(ga3.GID, ga3);
		gaList.put(ga4.GID, ga4);
		gaList.put(ga5.GID, ga5);
		gaList.put(ga6.GID, ga6);
		gaList.put(ga7.GID, ga7);
		gaList.put(ga8.GID, ga8);
		gaList.put(ga9.GID, ga9);
		gaList.put(ga10.GID, ga10);

	}

	// public void buildGaList(int n){
	// gaList.clear();
	// NewKmCluster KM = new NewKmCluster();
	// ArrayList<clusterNode> cArr=KM.runClustering(n);
	// for(int i=0;i<cArr.size();i++){
	// GroupActivity ga=new GroupActivity(Integer.toString(i));
	// for(String actStr : cArr.get(i).clusterMember){
	// Boolean same=false;
	// for(String str:ga.actMemberList){
	// if(str.equals(actStr)){
	// same=true;
	// }
	// }
	// if(!same){
	// ga.actMemberList.add(actStr);
	// }
	// }
	// gaList.put(ga.GID, ga);
	// }
	// }

	public double[] findsMatrixMin(String[] lastActivityList, double[][] sMatrix) {
		double min = 9999;
		double[] minArr = new double[3];
		Boolean allZeroFlag = true;
		for (int i = 0; i < lastActivityList.length; i++) {
			for (int j = 0; j < lastActivityList.length; j++) {
				if (sMatrix[i][j] < min && sMatrix[i][j] > 0) {
					min = sMatrix[i][j];
					minArr[1] = i;
					minArr[2] = j;
				}
				if (sMatrix[i][j] != 0) {
					allZeroFlag = false;
				}
			}
		}
		if (allZeroFlag)
			minArr[0] = 0;
		else
			minArr[0] = min;
		return minArr;

	}

	public double findsMatrixMax(String[] lastActivityList, double[][] sMatrix) {
		double max = 0;

		for (int i = 0; i < lastActivityList.length; i++) {
			for (int j = 0; j < lastActivityList.length; j++) {
				if (sMatrix[i][j] > max && sMatrix[i][j] > 0)
					max = sMatrix[i][j];
			}
		}
		return max;

	}

	// public Boolean buildHGA2(Classifier DBN,GaGenerator lastGA){
	// /*找出上一個gaList*/
	// Set<String> keys = lastGA.gaList.keySet();
	// String []lastGAList= (String[])keys.toArray(new String[0]);
	// /*根據 classifiers來算smatrix*/
	// int round=CrossValidate.cvRound;
	// double sMatrix
	// [][]=buildSMatrix("./_output_results/sMatrix/round"+round+"_sMatrix_"+(level-1)+".txt",DBN,lastGAList);
	//
	//
	// /*根據smatrix來build gaList*/
	// int gaIndex=0;/*第n個ga*/
	// double lastMin=999;
	// double parameter1=1.5;
	// double minThreshold=4;
	//
	// while(true){
	// double []minArr=findsMatrixMin(lastGAList,sMatrix);
	// double min=minArr[0];
	// double max=findsMatrixMax(lastGAList,sMatrix);
	// if( ( gaIndex==0 && min<minThreshold ) || ( min< (max/2) &&
	// min<minThreshold) ){
	// for(int i=0;i<lastGAList.length;i++){
	// for(int j=0;j<lastGAList.length;j++)
	// {
	// if(sMatrix[i][j]==min ){
	// GroupActivity ga=new
	// GroupActivity("g"+Integer.toString(level)+"-"+Integer.toString(gaIndex+1));
	//
	// /*第二層才開始合併*/
	// if(level==1){
	//
	// }else {
	// ArrayList<String>memberList1=lastGA.getGroupMember(lastGAList[i]);
	// ArrayList<String>memberList2=lastGA.getGroupMember(lastGAList[j]);
	// for(String str:memberList1){
	// /*加入合併前該ga所有的 activity*/
	//
	// if(!ga.actMemberList.contains(str)){
	// ga.actMemberList.add(str);
	// }
	// }
	// for(String str:memberList2){
	// if(!ga.actMemberList.contains(str)){
	// ga.actMemberList.add(str);
	// }
	// }
	// gaList.put(ga.GID, ga);
	// }
	// // /*方法一 同一個活動只會屬於一個cluster*/
	// // for(int k=0;k< lastGAList.length;k++){
	// // sMatrix[i][k]=0;
	// // sMatrix[j][k]=0;
	// // sMatrix[k][i]=0;
	// // sMatrix[k][j]=0;
	// // }
	// /*登記加過的activity*/
	// sMatrix[i][i]=-1;
	// sMatrix[j][j]=-1;
	// //
	// sMatrix[i][j]=0;
	// sMatrix[j][i]=0;
	// gaIndex+=1;
	// }
	//
	// }
	// }
	// lastMin=min;
	// }else {
	// break;
	// }
	// }
	// /*沒有任何相似的activity*/
	// if(gaList.size()==0){
	// return false;
	// }else{
	// /*沒被group到的也加入ga_list*/
	// for(int i=0;i<lastGAList.length;i++){
	// Boolean existInGroup=false;
	// for(int j=0;j<lastGAList.length;j++)
	// {
	// if(i==j && sMatrix[i][j]<0){
	// existInGroup=true;
	// }
	// }
	// if(!existInGroup){
	// GroupActivity ga=new
	// GroupActivity("g"+Integer.toString(level)+"-"+Integer.toString(gaIndex+1));
	// ArrayList<String>memberList1=lastGA.getGroupMember(lastGAList[i]);
	// for(String str:memberList1){
	// if(!ga.actMemberList.contains(str)){
	// ga.actMemberList.add(str);
	// }
	// }
	// gaList.put(ga.GID, ga);
	// gaIndex+=1;
	// }
	// }
	// return true;
	// }
	// }

	public Boolean checkSameService(String gaName1, String gaName2,
			Map<String, RelationTable> actAppList) {
		Boolean same = false;
		ArrayList<AppNode> gaAppList1 = actAppList.get(gaName1).appList;
		ArrayList<AppNode> gaAppList2 = actAppList.get(gaName2).appList;
		for (AppNode aNode1 : gaAppList1) {
			for (AppNode aNode2 : gaAppList2) {
				if (aNode1.appName.equals(aNode2.appName)
						&& aNode1.state.equals(aNode2.state)
						&& aNode1.escType.equals(aNode2.escType)) {
					if (aNode1.confidence > 0.9 && aNode2.confidence > 0.9) {
						same = true;
					}
				}
			}
		}

		return same;
	}

	/* No use */
	public Boolean buildHGA(Classifier DBN, GaGenerator lastGA,
			GaEtcGenerator lastGAETC, Boolean retrain) {
		/* 找出上一個gaList */
		Set<String> keys = lastGA.gaList.keySet();
		String[] lastGAList = (String[]) keys.toArray(new String[0]);
		/* 根據 classifiers來算smatrix */
		int round = CrossValidate.cvRound;
		double sMatrix[][] = buildSMatrix("./_output_results/sMatrix/round"
				+ round + "_sMatrix_" + (level - 1) + ".txt", DBN, lastGAList,
				retrain);
		/* 根據smatrix來build gaList */
		int gaIndex = 0;/* 第n個ga */

		Boolean continueFlag = true;
		while (continueFlag) {
			double minArr[] = findsMatrixMin(lastGAList, sMatrix);
			double min = minArr[0];
			int i = (int) minArr[1];
			int j = (int) minArr[2];
			/* 檢查 i j是否有共同的 explicit/implict */
			Map<String, RelationTable> actAppList = lastGAETC.actAppList;
			String gaName1 = lastGAList[i];
			String gaName2 = lastGAList[j];
			Boolean sameService = checkSameService(gaName1, gaName2, actAppList);
			/* 第二層才開始合併 */
			if (level != 1 && min != 0) {
				if (sameService) {
					GroupActivity ga = new GroupActivity("g"
							+ Integer.toString(level) + "-"
							+ Integer.toString(gaIndex + 1));
					ArrayList<String> memberList1 = lastGA
							.getGroupMember(lastGAList[i]);
					ArrayList<String> memberList2 = lastGA
							.getGroupMember(lastGAList[j]);
					for (String str : memberList1) {
						/* 加入合併前該ga所有的 activity */
						if (!ga.actMemberList.contains(str)) {
							ga.actMemberList.add(str);
						}
					}
					for (String str : memberList2) {
						if (!ga.actMemberList.contains(str)) {
							ga.actMemberList.add(str);
						}
					}
					gaList.put(ga.GID, ga);
					/* 登記加過的activity */
					sMatrix[i][i] = -1;
					sMatrix[j][j] = -1;
					gaIndex += 1;
					continueFlag = false;
				}
				sMatrix[i][j] = 0;
				sMatrix[j][i] = 0;

			} else if (level != 1 && min == 0) {
				continueFlag = false;
			}

		}
		/* 沒有任何相似的activity */
		if (gaList.size() == 0) {
			return false;
		} else {
			/* 沒被group到的也加入ga_list */
			for (int i = 0; i < lastGAList.length; i++) {
				Boolean existInGroup = false;
				for (int j = 0; j < lastGAList.length; j++) {
					if (i == j && sMatrix[i][j] < 0) {
						existInGroup = true;
					}
				}
				if (!existInGroup) {
					GroupActivity ga = new GroupActivity("g"
							+ Integer.toString(level) + "-"
							+ Integer.toString(gaIndex + 1));
					ArrayList<String> memberList1 = lastGA
							.getGroupMember(lastGAList[i]);
					for (String str : memberList1) {
						if (!ga.actMemberList.contains(str)) {
							ga.actMemberList.add(str);
						}
					}
					gaList.put(ga.GID, ga);
					gaIndex += 1;
				}
			}
			return true;
		}
	}

	public Boolean buildHGA(Classifier DBN, GaGenerator lastGA,
			GaEscGenerator lastGAESC, Boolean retrain) {
		/* 找出上一個gaList */
		Set<String> keys = lastGA.gaList.keySet();
		String[] lastGAList = (String[]) keys.toArray(new String[0]);
		/* 根據 classifiers來算smatrix */
		int round = CrossValidate.cvRound;
		double sMatrix[][] = buildSMatrix("./_output_results/sMatrix/round"
				+ round + "_sMatrix_" + (level - 1) + ".txt", DBN, lastGAList,
				retrain);
		/* 根據smatrix來build gaList */
		int gaIndex = 0;/* 第n個ga */

		Boolean continueFlag = true;
		while (continueFlag) {
			double minArr[] = findsMatrixMin(lastGAList, sMatrix);
			double min = minArr[0];
			int i = (int) minArr[1];
			int j = (int) minArr[2];
			/* 檢查 i j是否有共同的 explicit/implict */
			Map<String, RelationTable> actAppList = lastGAESC.actAppList;
			String gaName1 = lastGAList[i];
			String gaName2 = lastGAList[j];
			Boolean sameService = checkSameService(gaName1, gaName2, actAppList);
			/* 第二層才開始合併 */
			if (level != 1 && min != 0) {
				if (sameService) {
					GroupActivity ga = new GroupActivity("g"
							+ Integer.toString(level) + "-"
							+ Integer.toString(gaIndex + 1));
					ArrayList<String> memberList1 = lastGA
							.getGroupMember(lastGAList[i]);
					ArrayList<String> memberList2 = lastGA
							.getGroupMember(lastGAList[j]);
					for (String str : memberList1) {
						/* 加入合併前該ga所有的 activity */
						if (!ga.actMemberList.contains(str)) {
							ga.actMemberList.add(str);
						}
					}
					for (String str : memberList2) {
						if (!ga.actMemberList.contains(str)) {
							ga.actMemberList.add(str);
						}
					}
					gaList.put(ga.GID, ga);
					/* 登記加過的activity */
					sMatrix[i][i] = -1;
					sMatrix[j][j] = -1;
					gaIndex += 1;
					continueFlag = false;
				}
				sMatrix[i][j] = 0;
				sMatrix[j][i] = 0;

			} else if (level != 1 && min == 0) {
				continueFlag = false;
			}

		}
		/* 沒有任何相似的activity */
		if (gaList.size() == 0) {
			return false;
		} else {
			/* 沒被group到的也加入ga_list */
			for (int i = 0; i < lastGAList.length; i++) {
				Boolean existInGroup = false;
				for (int j = 0; j < lastGAList.length; j++) {
					if (i == j && sMatrix[i][j] < 0) {
						existInGroup = true;
					}
				}
				if (!existInGroup) {
					GroupActivity ga = new GroupActivity("g"
							+ Integer.toString(level) + "-"
							+ Integer.toString(gaIndex + 1));
					ArrayList<String> memberList1 = lastGA
							.getGroupMember(lastGAList[i]);
					for (String str : memberList1) {
						if (!ga.actMemberList.contains(str)) {
							ga.actMemberList.add(str);
						}
					}
					gaList.put(ga.GID, ga);
					gaIndex += 1;
				}
			}
			return true;
		}
	}

	static public void writeHGA(String hgaPath,
			ArrayList<GaGenerator> GaGeneratorList) {
		try {
			FileWriter writer = new FileWriter(new File(hgaPath), false);
			for (int i = 0; i < GaGeneratorList.size(); i++) {
				Map<String, GroupActivity> gaList = GaGeneratorList.get(i).gaList;
				Set<String> keys = gaList.keySet();
				for (String str : keys) {
					writer.write(str + " :");
					ArrayList<String> memberList = gaList.get(str).actMemberList;
					for (String str2 : memberList) {
						writer.write(str2 + ", ");
					}
					writer.write("\r\n");
					writer.flush();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public double[][] buildSMatrix(String smPath, Classifier DBN,
			String[] activityList, Boolean retrain) {
		EditableBayesNet[] classifiers = DBN.buildARModelwithAllFeature(
				activityList, retrain);
		// EditableBayesNet[]
		// classifiers=DBN.buildARModelwithAllFeature((String[])EnvStructure.activityList.toArray(new
		// String[0]));
		double sMatrix[][] = new double[activityList.length][activityList.length];
		for (int i = 0; i < activityList.length; i++) {
			for (int i2 = 0; i2 < activityList.length; i2++) {
				EditableBayesNet classifier = classifiers[i];

				EditableBayesNet classifier2 = classifiers[i2];

				double dis = calDistanceForClassifier(classifier, classifier2);
				sMatrix[i][i2] = dis;
			}
		}
		/* print result */
		try {
			FileWriter writer = new FileWriter(new File(smPath), false);
			for (int i = 0; i < activityList.length; i++) {
				for (int j = 0; j < activityList.length; j++) {
					writer.write(sMatrix[i][j] + " ");
				}
				writer.write("\r\n");
				writer.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return sMatrix;

	}

	private double calDistanceForClassifier(EditableBayesNet classifier,
			EditableBayesNet classifier2) {
		HashSet<String> nodeList = new HashSet<String>();

		/* 取兩個classifier node的聯集 */
		for (int i = 0; i < classifier.getNrOfNodes() - 1; i++) {
			String node = classifier.getNodeName(i);
			nodeList.add(node);
		}
		for (int i = 0; i < classifier2.getNrOfNodes() - 1; i++) {
			String node = classifier2.getNodeName(i);
			nodeList.add(node);
		}
		/* 計算每個node的distance */

		double totalDist = 0;
		for (String node : nodeList) {

			int stateNum = insts.attribute(node).numValues();
			for (int k = 0; k < stateNum; k++) {
				int iNode1 = classifier.getNode2(node);
				/* get probability if node exist */
				double pe1;
				if (iNode1 == -1) {
					pe1 = 0;
				} else {
					pe1 = classifier.getProbability(iNode1, 1, k);
				}
				int iNode2 = classifier2.getNode2(node);
				double pe2;
				if (iNode2 == -1) {
					pe2 = 0;
				} else {
					pe2 = classifier2.getProbability(iNode2, 1, k);
				}
				totalDist += Math.pow(pe1 - pe2, 2);
			}

		}
		Math.pow(totalDist, 0.5);
		return totalDist;
	}

	public ArrayList<String> getGID(String activity) {
		Collection<GroupActivity> collection = gaList.values();
		String GID = null;
		ArrayList<String> gidArr = new ArrayList<String>();
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			GroupActivity ga = (GroupActivity) iterator.next();
			for (String str : ga.actMemberList) {
				if (str.equals(activity)) {
					GID = ga.GID;
					gidArr.add(GID);
				}
			}
		}
		return gidArr;
	}

	public ArrayList<String> getGroupMember(String GID) {
		Collection<GroupActivity> collection = gaList.values();
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			GroupActivity ga = (GroupActivity) iterator.next();
			if (ga.GID.equals(GID)) {
				return ga.actMemberList;
			}
		}
		return null;
	}

	// public void buildGaModel(String rawDataPath,String gaTrainingDataPath){
	// try {
	// BufferedReader reader = new BufferedReader(new FileReader(
	// rawDataPath));
	// FileWriter writer = new FileWriter(new File(gaTrainingDataPath));
	// String read="";
	// while((read = reader.readLine()) != null){
	// String []split=read.split("#");
	// String GID=getGID(split[1]);
	// writer.write(split[0]+" #"+GID+"\r\n");
	// writer.flush();
	//
	// }
	// } catch (Exception e) {
	// // TODO: handle exception
	// }
	// }
}
