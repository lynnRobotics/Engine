package IntelM2M.datastructure;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import IntelM2M.epcie.GaGenerator;
import IntelM2M.epcie.erc.GaEscGenerator;
import IntelM2M.preference.PreferenceAgent;
/*old not use*/
public class PRmodel {
	public ArrayList<String> prFeature;
	public ArrayList<ESService> esServiceList ;
	public ArrayList <String> selectedPrFeature;
	public ArrayList<AppNode> escList;
	public Map<Integer,ArrayList<AppNode>> rule12;
	public int feedback=0;
	

	
}
