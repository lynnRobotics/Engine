package IntelM2M.tmp;

import IntelM2M.agent.ap.FuzzyInference;
import IntelM2M.datastructure.AppNode;

public class fuzzyTest {
    public static void main(String[] args) throws Exception {
//        // Load from 'FCL' file
//        String fileName = "./fcl/tipper.fcl";
//        FIS fis = FIS.load(fileName,true);
//        // Error while loading?
//        if( fis == null ) { 
//            System.err.println("Can't load file: '" 
//                                   + fileName + "'");
//            return;
//        }
//
//        // Show 
//        fis.chart();
//
//        // Set inputs
//        fis.setVariable("service", 3);
//        fis.setVariable("food", 7);
//
//        // Evaluate
//        fis.evaluate();
//
//        // Show output variable's chart 
//        double output=fis.getVariable("tip").defuzzify();
//        fis.getVariable("tip").chartDefuzzifier(true);
//
//        // Print ruleSet
//        System.out.println(fis);
    	
    	
    	AppNode app= new AppNode();
  
    	
    	

    	/*test type 2*/
    	app.state="on";
    	app.escType="explicit";
    	app.confidence=0.74;
    	FuzzyInference.getPriority_test(app);
    	
    	
//    	app.state="on";
//    	app.escType="implicit";
//    	app.confidence=0.74;
//    	FuzzyInference.getPriority(app);
//    	
//    	app.state="on";
//    	app.escType="explicit";
//    	app.confidence=0.93;
//    	FuzzyInference.getPriority(app);
//    	
//    	app.state="on";
//    	app.escType="implicit";
//    	app.confidence=0.93;
//    	FuzzyInference.getPriority(app);
//    	/*test type 3*/
//    	
//    	app.state="on";
//    	app.escType="explicit";
//    	app.confidence=0.74;
//    	FuzzyInference.getPriority_test(app);
//    	
//    	app.state="on";
//    	app.escType="implicit";
//    	app.confidence=0.74;
//    	// FuzzyInference.getPriority(app);
//    	FuzzyInference.getPriority_test(app);
//
//    	
//    	app.state="on";
//    	app.escType="explicit";
//    	app.confidence=0.93;
//    	FuzzyInference.getPriority_test(app);
//    	
//    	app.state="on";
//    	app.escType="implicit";
//    	app.confidence=0.93;
//    	FuzzyInference.getPriority_test(app);
    	
    }
}
