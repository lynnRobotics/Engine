package IntelM2M.func;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PassingMessages {
	ArrayList<String> inputDataList = new ArrayList<String>();
	Map<String, Integer> numOccurence = new LinkedHashMap<String, Integer>();
	ArrayList<String> validDataList = new ArrayList<String>();
	ArrayList<Integer> allDataSimilarityList = new ArrayList<Integer>();
	ArrayList<Integer> validDataSimilarityList = new ArrayList<Integer>();
	ArrayList<Double> finalDataSimilarityList = new ArrayList<Double>();
	int initialMedian;
	/*input*/
	String inputData = "./_input_data/PassingMessages/trainingData_tv_kinect.txt";
	/*output*/
	String outputSimilarityMatrix = "./_output_results/PassingMessages/similarityMatrix.txt";
	String outputValidData = "./_output_results/PassingMessages/ValidData.txt";
	
	private void readInputData() {
		String readString = null;
		String modifiedReadString = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputData));
			while ((readString = reader.readLine()) != null) {
				modifiedReadString = readString.replaceAll(" #.*", "");
				inputDataList.add(modifiedReadString);
				if (!numOccurence.keySet().contains(modifiedReadString)) {
					validDataList.add(readString);
					numOccurence.put(modifiedReadString, 1);
				} else {
					numOccurence.put(modifiedReadString, numOccurence.get(modifiedReadString) + 1);
				}
			}
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void computeAllDataSimilarity() {
		for (int i = 0; i < inputDataList.size(); i++) {
			String currentData = inputDataList.get(i);
			String[] currentDataState = currentData.split(" ");
			for (int j = 0; j < inputDataList.size(); j++) {
				String otherData = inputDataList.get(j);
				String[] otherDataState = otherData.split(" ");
				int similarity = 0;
				if (currentData.equals(otherData)) {
					allDataSimilarityList.add(0);
					continue;
				}
				for (int k = 0; k < currentDataState.length; k++) {
					if (!currentDataState[k].equals(otherDataState[k])) {
						similarity++;
					}
				}
				allDataSimilarityList.add(similarity);
			}
		}
		
		return;
	}
	
	private void computeValidSimilarity() {
		Object[] data = numOccurence.keySet().toArray();
		for (int i = 0; i < data.length; i++) {
			String currentData = (String) data[i];
			String[] currentDataState = currentData.split(" ");
			int numCurrentData = numOccurence.get(currentData);
			for (int j = 0; j < data.length; j++) {
				String otherData = (String) data[j];
				String[] otherDataState = otherData.split(" ");
				int numOtherData = numOccurence.get(otherData);
				int similarity = 0;
				if (currentData.equals(otherData)) {
					finalDataSimilarityList.add(0.0);
					continue;
				}
				for (int k = 0; k < currentDataState.length; k++) {
					if (!currentDataState[k].equals(otherDataState[k])) {
						similarity++;
					}
				}
				finalDataSimilarityList.add((double) -similarity);
				for (int n = 0; n < numCurrentData * numOtherData; n++) {
					validDataSimilarityList.add(-similarity);
				}
			}
		}
		
		return;
	}
	
	private int getInitialMedian() {
		ArrayList<Integer> copyOfValidDataSimilarityList = new ArrayList<Integer>(validDataSimilarityList);
		Collections.sort(copyOfValidDataSimilarityList);
		int median = copyOfValidDataSimilarityList.get(copyOfValidDataSimilarityList.size() / 2);
		return median;
	}
	
	private void adjustInitialMedian() {
		Object[] data = numOccurence.keySet().toArray();
		for (int i = 0; i < data.length; i++) {
			int index = i * data.length + i;
			finalDataSimilarityList.set(index, 1.0 * this.initialMedian * (inputDataList.size() - numOccurence.get((String) data[i])) / inputDataList.size());
		}
		return;
	}
	
	private void ouputSimilarityMatrix() {
		try {
			FileWriter fw = new FileWriter(new File(outputSimilarityMatrix));
			for (int i = 0; i < finalDataSimilarityList.size(); i++) {
				String s = String.format("%.2f", finalDataSimilarityList.get(i));
				fw.write(s);
				if ((i + 1) % numOccurence.size() == 0) {
					fw.write("\r\n");
				} else {
					fw.write(" ");
				}
				fw.flush();
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void outputValidData() {
		try {
			FileWriter fw = new FileWriter(new File(outputValidData));
			for (String s : validDataList) {
				fw.write(s + "\r\n");
				fw.flush();
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PassingMessages pm = new PassingMessages();
		pm.readInputData();
		pm.computeValidSimilarity();
		pm.initialMedian = pm.getInitialMedian();
		pm.adjustInitialMedian();
		pm.ouputSimilarityMatrix();
		pm.outputValidData();
	}

}
