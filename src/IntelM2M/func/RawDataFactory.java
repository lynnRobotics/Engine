package IntelM2M.func;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class RawDataFactory {
	
	String rawTrainingDataPath = "./_input_data/BL313/trainingData_test9.txt";
	String rawTrainingDataPathModified = "./_input_data/BL313/trainingData_test10.txt";
	
	public static void main(String[] args) {
		RawDataFactory rdf = new RawDataFactory();
		//rdf.insertOneColumn(18, "###");
		//rdf.deleteOneColumn(16);
		//rdf.modifyOneColumn(18, "off");
		//rdf.modifyOneColumn(10, "on", "AllSleeping");
		//rdf.countColumn();
		//rdf.modifyOneColumn(10, "off");
		rdf.modifyOneColumn(10, "on", "PlayingKinect");
		//rdf.modifyOneColumn(10, "on", "WatchingTV");
	}

	public void insertOneColumn(int index, String value) {
		try {
			String read = null;
			String buffer = "";
			BufferedReader reader = new BufferedReader(new FileReader(rawTrainingDataPath));
			FileWriter writer = new FileWriter(new File(rawTrainingDataPathModified));
			while ((read = reader.readLine()) != null) {
				String[] split = read.split(" ");
				buffer = "";
				for (int i = 0; i < split.length; i++) {
					if (i == index) {
						buffer = buffer + value + " ";
					}
					buffer = buffer + split[i] + " ";
				}
				writer.write(buffer.trim() + "\r\n");
				writer.flush();
			}
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteOneColumn(int index) {
		try {
			String read = null;
			String buffer = "";
			BufferedReader reader = new BufferedReader(new FileReader(rawTrainingDataPath));
			FileWriter writer = new FileWriter(new File(rawTrainingDataPathModified));
			while ((read = reader.readLine()) != null) {
				String[] split = read.split(" ");
				buffer = "";
				for (int i = 0; i < split.length; i++) {
					if (i != index) {
						buffer = buffer + split[i] + " ";
					}
				}
				writer.write(buffer.trim() + "\r\n");
				writer.flush();
			}
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void modifyOneColumn(int index, String value) {
		try {
			String read = null;
			String buffer = null;
			BufferedReader reader = new BufferedReader(new FileReader(rawTrainingDataPath));
			FileWriter writer = new FileWriter(new File(rawTrainingDataPathModified));
			while ((read = reader.readLine()) != null) {
				String[] split = read.split(" ");
				split[index] = value;
				buffer = Arrays.toString(split).replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "");
				writer.write(buffer.trim() + "\r\n");
				writer.flush();
			}
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void modifyOneColumn(int index, String value, String activity) {
		try {
			String read = null;
			String buffer = null;
			BufferedReader reader = new BufferedReader(new FileReader(rawTrainingDataPath));
			FileWriter writer = new FileWriter(new File(rawTrainingDataPathModified));
			while ((read = reader.readLine()) != null) {
				ArrayList<String> activityList = toArrayList(read.split("#")[1].split(" "));
				String[] split = read.split(" ");
				if (activityList.contains(activity)) split[index] = value;
				buffer = Arrays.toString(split).replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "");
				writer.write(buffer.trim() + "\r\n");
				writer.flush();
			}
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void countColumn() {
		try {
			String read = null;
			BufferedReader reader = new BufferedReader(new FileReader(rawTrainingDataPathModified));
			if ((read = reader.readLine()) != null) {
				String[] split = read.split(" ");
				System.out.println("Number of Column is " + split.length);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//String[] to ArrayList
	public ArrayList<String> toArrayList(String[] aString) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < aString.length; i++) {
			if (!((null == aString[i]) || "".equals(aString[i]))) {
				list.add(aString[i]);
			}
		}
		return list;
	}
}
