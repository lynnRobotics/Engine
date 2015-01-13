package IntelM2M.tmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class regenerateTestData
{
//	protected String fileName1, fileName2;
//	
//	public regenerateTestData()
//	{
//		fileName1 = "testing_data";
//		fileName2 = "testing_data_numHuman_PAL";
//	}
//	
//	protected void combine()
//	{
//		Map<Integer, String> sensorList = new LinkedHashMap<Integer, String>();
//		
//		try
//		{
//			BufferedReader rdr = new BufferedReader(new FileReader("./_input_data/sensor_list.txt"));
//			String readStr = "", data = "", pal = "";
//			int i = 0;
//			while((readStr = rdr.readLine()) != null)
//			{
//				String[] split = readStr.split(" ");
//				sensorList.put(i, split[0]);
//				i ++;
//			}
//			rdr.close();
//			
//			FileWriter writer = new FileWriter(new File("./_simulated_test_data/testing_data_with_PAL.txt"));
//			BufferedReader rdrData = new BufferedReader(new FileReader("./_simulated_test_data/" +  fileName1 + ".txt"));
//			BufferedReader rdrPAL = new BufferedReader(new FileReader("./_simulated_test_data/" +  fileName2 + ".txt"));
//			
//			while(((data = rdrData.readLine()) != null) && ((pal = rdrPAL.readLine()) != null))
//			{
//				writer.write("Activity:");
//				String strPAL = "";
//				String[] split = pal.split(" ");
//				for(int k = 1; k < split.length; k++)
//				{
//					String[] actInfo = split[k].split("_");
//					writer.write(" " + actInfo[0]);
//					strPAL = strPAL.concat(" ").concat(actInfo[1]);
//				}
//				writer.write("\r\n");
//				writer.write("Sensor:" + strPAL);
//				
//				split = data.split(" ");
//				for(int k = 0; k < split.length - 1; k ++)
//				{
//					if((!split[k].equals("off")) && (!split[k].equals("low")))
//						writer.write(" " + sensorList.get(k) + "_" + split[k]);
//				}
//				writer.write("\r\n");
//			}
//			
//			rdrData.close();
//			rdrPAL.close();
//			writer.close();
//		}
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
}
