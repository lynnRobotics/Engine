package IntelM2M.datastructure;

import java.util.ArrayList;

public class AllComfort {
	ArrayList<Double> pmvList;
	ArrayList<Double> illList;
	double cost;
	
	public AllComfort(ArrayList<Double> in1, ArrayList<Double> in2, double in3){
		pmvList=in1;
		illList=in2;
		cost=in3;
	}
}
