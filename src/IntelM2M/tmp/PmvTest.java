package IntelM2M.tmp;

import IntelM2M.agent.thermal.PMVCalculate;



public class PmvTest {
    public static void main(String[] args) throws Exception {

    	double intensity=80;
    	double temp=32;
    	double vel=3;
    	PMVCalculate pc= new PMVCalculate(intensity,temp,vel);
    	double pmv=pc.getPMVandPPD()[0];
    	double ppd=pc.getPMVandPPD()[1];
    	double good=pc.getMostFitTemp(1.0);
    	System.out.println(pmv+" "+ppd+" "+good);
    }
}
