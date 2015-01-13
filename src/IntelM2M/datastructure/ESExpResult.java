package IntelM2M.datastructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ESExpResult {
	public double noiseSaving=0;
	public double standbySaving=0;
	public double noiseConsumption=0;
	public double afterEssComsuption=0;
	public double totalComsumption=0;
	public double wrong=0;
	public Map <Integer,Double> ruleSaving = new  LinkedHashMap<Integer,Double>();

	public double roomNo=0;
	public ArrayList<String> roomWrongList;
	public ArrayList<String> roomControlList;
}
