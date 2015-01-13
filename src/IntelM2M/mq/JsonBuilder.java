package IntelM2M.mq;

public class JsonBuilder {
	StringBuilder sb;
	
	public JsonBuilder() {
		reset();
	}
	
	public JsonBuilder add(String a, String b) {
		sb.append("\""+a+"\":\""+b+"\",");
		return this;
	}
	
	public String toJson(){
		sb.deleteCharAt(sb.length()-1); //delete the last ','
		sb.append("}");
		String out = sb.toString();
		reset();
		return out;
	}
	
	public void reset() {
		sb = new StringBuilder();
		sb.append("{");
	}
	
	// return the value of the name in the json-String
	public static String getValue(String jsonMsg, String name) {
		String jsonString = String.copyValueOf(jsonMsg.toCharArray(), 1, jsonMsg.length()-2); // remove { }
		String[] membersString = jsonString.split(",");
		for(int i = 0; i < membersString.length; i++) {
			String[] splitStrings = membersString[i].split(":");
			if(splitStrings[0].equals("\""+name+"\"")){
				return String.copyValueOf(splitStrings[1].toCharArray(),1,splitStrings[1].length()-2);
			}
		}
		return "";
	}
	
}
