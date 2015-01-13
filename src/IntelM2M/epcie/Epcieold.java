package IntelM2M.epcie;

import java.util.ArrayList;
import java.util.Map;

import s2h.platform.annotation.MessageFrom;
import s2h.platform.annotation.UPnP;
import s2h.platform.node.LogicNode;
import s2h.platform.node.NodeRunner;
import s2h.platform.node.PlatformMessage;
import s2h.platform.node.PlatformTopic;
import IntelM2M.algo.GaEtcGenerator;
import IntelM2M.datastructure.ExpResult;
import IntelM2M.epcie.classifier.DbnClassifier;
import IntelM2M.epcie.classifier.GaDbnClassifier;
import IntelM2M.epcie.erc.EtcGenerator;
import IntelM2M.epcie.erc.GaEscGenerator;
import IntelM2M.func.CrossValidate;
import IntelM2M.func.text2Arff;
import IntelM2M.test.HgaTest;
import IntelM2M.test.IrosTest;
import IntelM2M.test.SimulatorTest;

/*
 * 
 * 
 * no use old version
 * 
 * 
 * 
 * */


@MessageFrom(PlatformTopic.RAW_DATA)
@UPnP
public class Epcieold extends LogicNode{

	  DbnClassifier DBN = new DbnClassifier();
	  GaDbnClassifier GaDBN = new GaDbnClassifier();
	  EtcGenerator ETC = new EtcGenerator();
	

	  GaGenerator GA= new GaGenerator();
	  text2Arff CVT = new text2Arff();

	  String resultPath="./_output_results/_testing_result.txt";
	  String gaResultPath="./_output_results/ga_testing_result.txt";


	  /*
	   * String rawTrainingDataPath= "./_input_data/CrossValidate/cv_all_data3.txt";
	  	 String testingDataPath=	"./_input_data/iros_test_data.txt";
	  */
	  
	  /*simulator*/
	   String rawTrainingDataPath= "./_input_data/simulator/simulator_trainingdata2.txt";
	   String testingDataPath=	"./_input_data/simulator/simple_test_data.txt";


	
    public Epcieold()
    {
      super();
     // sysProc(testingDataPath,rawTrainingDataPath,resultPath,gaResultPath);

      //sysProc2(testingDataPath,rawTrainingDataPath,gaResultPath);
      sysProcForSimulator(testingDataPath,rawTrainingDataPath,gaResultPath);
    }
    

    

    
    public void sysProc(String testPath, String trainPath,String resultPath,String gaResultPath){
		/*build GA*/
		GA.buildGaList();
    	
    	CVT.convertRawToArff(trainPath);
    	CVT.writeClusterArff(trainPath);
    	CVT.convertGaRawToArff(GA, trainPath);
		/*build model*/
		DBN.buildARModel(true); //build AR model	
		ETC.buildAllETC(DBN.classifier,"./_output_results/etc.txt");//build ETC
		/*build sMatrix*/
		ETC.buildSMatrix("./_output_results/sMatrix.txt");
		//ETC.buildSMatrix(DBN.classifier,"./_output_results/sMatrix.txt");
		//initial model 
		DBN.allSetDefaultValue(true);   


		/*build GA Model*/
		GaDBN.buildGaModel(GA,true);
		GaEtcGenerator GAETC = new GaEtcGenerator(GA);
		GAETC.buildAllETC(GaDBN.classifier, "./_output_results/ga_etc.txt",GA);
		GaDBN.allSetDefaultValue(GA);
		/*Testing*/
		GaDBN.testing(GA, testPath, gaResultPath);
		
		/*Testing*/
		DBN.testing(GA,testPath,resultPath);
    }
    
  static  public ArrayList<Map <String,ExpResult>> sysProc2(String testPath, String trainPath,String gaResultPath){
  	
		ArrayList<GaGenerator> GaGeneratorList=new ArrayList<GaGenerator>();
		ArrayList<GaDbnClassifier> GaDbnList = new ArrayList<GaDbnClassifier>();
		ArrayList<GaEtcGenerator> GaEtcList = new ArrayList<GaEtcGenerator>();
		ArrayList<GaEscGenerator> GaEscList = new ArrayList<GaEscGenerator>();
		text2Arff CVT = new text2Arff();
		int round=CrossValidate.cvRound;
	    for(int i=1;i<=2;i++){
		    GaGenerator GA= new GaGenerator(i);
		    GaDbnClassifier GaDBN = new GaDbnClassifier();
		    if(i==1){
		    	GA.buildFirstGaList();
		    }
		    else 
	    	{
		    	Boolean flag=GA.buildHGA(GaDbnList.get(i-2),GaGeneratorList.get(i-2),GaEtcList.get(i-2),true);
		    	if(!flag){
		    		break;
		    	}
		    	         	
	    	}

	    	CVT.convertGaRawToArff(GA, trainPath);
	    	/*build GA Model*/
			GaDBN.buildGaModel(GA,true);
			GaEtcGenerator GAETC = new GaEtcGenerator(GA);
			GAETC.buildAllETC(GaDBN.classifier, "./_output_results/ETC/round"+round+"_ga_etc_"+i+".txt",GA);
			
			GaEscGenerator GAESC=new GaEscGenerator(GA,true);
			if(i==1)
				GAESC.buildAllESC(GaDBN.classifier,  "./_output_results/ESC/round"+round+"_ga_esc_"+i+".txt", GA, null, null);
			else
				GAESC.buildAllESC(GaDBN.classifier,  "./_output_results/ESC/round"+round+"_ga_esc_"+i+".txt", GA, GaGeneratorList.get(0), GaEscList.get(0));
			GaDBN.allSetDefaultValue(GA);
			/*record hga*/
			GaGeneratorList.add(GA);
			GaDbnList.add(GaDBN);
			GaEtcList.add(GAETC);
			GaEscList.add(GAESC);
			
			
	    }
	    /*寫出 hga結構*/
		GaGenerator.writeHGA ("./_output_results/hga.txt", GaGeneratorList);
		/*Testing*/
		ArrayList<Map <String,ExpResult>> kExpResult=HgaTest.hgaTesting2(GaGeneratorList, GaDbnList, testPath);
		return kExpResult;

    }
 
  public void sysProcForIros(String testPath, String trainPath,String gaResultPath){
	  	
		ArrayList<GaGenerator> GaGeneratorList=new ArrayList<GaGenerator>();
		ArrayList<GaDbnClassifier> GaDbnList = new ArrayList<GaDbnClassifier>();
	
		ArrayList<GaEscGenerator> GaEscList = new ArrayList<GaEscGenerator>();
		text2Arff CVT = new text2Arff();
		int round=CrossValidate.cvRound;
	    for(int i=1;i<=7;i++){
		    GaGenerator GA= new GaGenerator(i);
		    GaDbnClassifier GaDBN = new GaDbnClassifier();
		    if(i==1){
		    	GA.buildFirstGaList();
		    }
		    else 
	    	{
		    	Boolean flag=GA.buildHGA(GaDbnList.get(i-2),GaGeneratorList.get(i-2),GaEscList.get(i-2),true);
		    	if(!flag){
		    		break;
		    	}
		    	         	
	    	}
	    	CVT.convertGaRawToArff(GA, trainPath);
	    	/*build GA Model*/
			GaDBN.buildGaModel(GA,true);		
			GaEscGenerator GAESC=new GaEscGenerator(GA,true);
			if(i==1)
				GAESC.buildAllESC(GaDBN.classifier,  "./_output_results/ESC/round"+round+"_ga_esc_"+i+".txt", GA, null, null);
			else
				GAESC.buildAllESC(GaDBN.classifier,  "./_output_results/ESC/round"+round+"_ga_esc_"+i+".txt", GA, GaGeneratorList.get(0), GaEscList.get(0));
			GaDBN.allSetDefaultValue(GA);
			/*record hga*/
			GaGeneratorList.add(GA);
			GaDbnList.add(GaDBN);
			GaEscList.add(GAESC);

	    }
	    /*寫出 hga結構*/
		GaGenerator.writeHGA ("./_output_results/hga.txt", GaGeneratorList);
		/*Testing*/
		IrosTest test=new IrosTest();
		test.irosTesting(GaGeneratorList, GaDbnList,GaEscList, testPath);		
  }
  
  public void sysProcForSimulator(String testPath, String trainPath,String gaResultPath){
		ArrayList<GaGenerator> GaGeneratorList=new ArrayList<GaGenerator>();
		ArrayList<GaDbnClassifier> GaDbnList = new ArrayList<GaDbnClassifier>();
	
		ArrayList<GaEscGenerator> GaEscList = new ArrayList<GaEscGenerator>();
		text2Arff CVT = new text2Arff();
		int round=CrossValidate.cvRound;
	    for(int i=1;i<=2;i++){
		    GaGenerator GA= new GaGenerator(i);
		    GaDbnClassifier GaDBN = new GaDbnClassifier();
		    if(i==1){
		    	GA.buildFirstGaList();
		    }
		    else 
	    	{
		    	Boolean flag=GA.buildHGA(GaDbnList.get(i-2),GaGeneratorList.get(i-2),GaEscList.get(i-2),true);
		    	if(!flag){
		    		break;
		    	}
		    	         	
	    	}
	    	CVT.convertGaRawToArff(GA, trainPath);
	    	/*build GA Model*/
			GaDBN.buildGaModel(GA,true);		
			GaEscGenerator GAESC=new GaEscGenerator(GA,true);
			if(i==1)
				GAESC.buildAllESC(GaDBN.classifier,  "./_output_results/ESC/round"+round+"_ga_esc_"+i+".txt", GA, null, null);
			else
				GAESC.buildAllESC(GaDBN.classifier,  "./_output_results/ESC/round"+round+"_ga_esc_"+i+".txt", GA, GaGeneratorList.get(0), GaEscList.get(0));
			GaDBN.allSetDefaultValue(GA);
			/*record hga*/
			GaGeneratorList.add(GA);
			GaDbnList.add(GaDBN);
			GaEscList.add(GAESC);

	    }
	    /*寫出 hga結構*/
		GaGenerator.writeHGA ("./_output_results/hga.txt", GaGeneratorList);
		/*Testing*/
		

		SimulatorTest test= new SimulatorTest();
		//test.simulatorTesting(GaGeneratorList, GaDbnList, GaEscList); 
		
  }

    
    public void realTimeSysProc( String trainPath,String resultPath,String gaResultPath){
		/*build GA*/
		GA.buildGaList();
    	
    	CVT.convertRawToArff(trainPath);
    	CVT.writeClusterArff(trainPath);
    	CVT.convertGaRawToArff(GA, trainPath);
		/*build model*/
		DBN.buildARModel(true); //build AR model	
		ETC.buildAllETC(DBN.classifier,"./_output_results/etc.txt");//build ETC
		ETC.buildSMatrix( "./_output_results/sMatrix.txt");
	    DBN.allSetDefaultValue(true); //initial model    
	    /*clustering For each room's activity*/
		//KM.runClustering(3);	

		/*build GA Model*/
		GaDBN.buildGaModel(GA,true);
		GaEtcGenerator GAETC = new GaEtcGenerator(GA);
		GAETC.buildAllETC(GaDBN.classifier, "./_output_results/ga_etc.txt",GA);
		GaDBN.allSetDefaultValue(GA);

    }

	@SuppressWarnings("unchecked")
	protected void processMessage(PlatformMessage message)
	{
		DBN.inference(message,getSender());
	
	}

	public static void main(String[] args)
    {
		new NodeRunner(Epcieold.class).execute();
    }
}
