package IntelM2M.tmp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import IntelM2M.datastructure.EnvStructure;

import s2h.platform.annotation.MessageFrom;
import s2h.platform.annotation.UPnP;
import s2h.platform.node.LogicNode;
import s2h.platform.node.NodeRunner;
import s2h.platform.node.PlatformMessage;
import s2h.platform.node.PlatformTopic;

@MessageFrom(PlatformTopic.CONTEXT)
@UPnP

public class test extends LogicNode {

	Map<String, String> sensorData = new LinkedHashMap<String, String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Map<String, ArrayList<String>> sensorStatus = EnvStructure.sensorStatus;
		String[] sensorName = (String[]) sensorStatus.keySet().toArray(new String[0]);
		for (int i = 0; i < sensorName.length; i++) {
			System.out.println(sensorName[i]);
		}
	}
	
	protected void processMessage(PlatformMessage message) {
		
		
	}

}
