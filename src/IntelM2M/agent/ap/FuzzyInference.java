package IntelM2M.agent.ap;

import net.sourceforge.jFuzzyLogic.FIS;
import IntelM2M.datastructure.AppNode;

public class FuzzyInference {

	/*old-wrong version*/
	static public double getPriority_old(AppNode app){
		String fileName= "./fcl/eus.fcl";
        FIS fis = FIS.load(fileName,true);
        // Error while loading?
        if( fis == null ) { 
            System.err.println("Can't load file: '" + fileName + "'");
            return 0;
        }
        // Show 
        fis.chart();
        
        /*get node type*/
        if(app.state.equals("off") || app.state.equals("standby")){
        	fis.setVariable("confidence1", app.confidence);
        	fis.setVariable("confidence2", 0);
        	fis.setVariable("confidence3", 0);
        }else if(app.escType.equals("implicit") && app.state.equals("on")){
        	fis.setVariable("confidence1", 0);
        	fis.setVariable("confidence2", app.confidence);
        	fis.setVariable("confidence3", 0);
        	
        }else if(app.escType.equals("explicit") && app.state.equals("on")){
        	//fis.setVariable("confidence1", 0);
        	//fis.setVariable("confidence2", 0);
        	fis.setVariable("confidence3", app.confidence);
        }
        
        /*evaluate*/
        fis.evaluate();

        /*get output*/
        fis.getVariable("priority").chartDefuzzifier(true);
        double output=fis.getVariable("priority").defuzzify();
        return output;
	}
	
	/*test version*/
	static public double getPriority_test(AppNode app){
		String fileName="./fcl/eus_v2.fcl";
        

        FIS fis = FIS.load(fileName,true);
        // Error while loading?
        if( fis == null ) { 
            System.err.println("Can't load file: '" + fileName + "'");
            return 0;
        }
        
        // Show 
       // fis.chart();
        
        //set input
        fis.setVariable("confidence", app.confidence);
        /*get node type*/
        if(app.escType.equals("implicit")){
        	fis.setVariable("eusType", 2);
        }else{
        	fis.setVariable("eusType", 1);
        }
        if( app.state.equals("standby")){
        	fis.setVariable("eusState", 2);
        	
        }else if( app.state.equals("on")){
        	fis.setVariable("eusState", 3);
        	
        }else if( app.state.equals("off")){
        	fis.setVariable("eusState", 1);
        }
        /*evaluate*/
        fis.evaluate();

        /*get output*/
        //fis.getVariable("priority").chartDefuzzifier(true);
        double output=fis.getVariable("priority").defuzzify();
        return output;
	}
	
	static public double getPriority(AppNode app){
		String fileName=null;
        
        /*get node type*/
        if(app.state.equals("off") || app.state.equals("standby")){
    		 fileName= "./fcl/eus_off_standby.fcl";;
        	
        }else if(app.escType.equals("implicit") && app.state.equals("on")){
        	 fileName= "./fcl/eus_on_implicit.fcl";;
        	
        }else if(app.escType.equals("explicit") && app.state.equals("on")){
        	fileName= "./fcl/eus_on_explicit.fcl";;
        }
        FIS fis = FIS.load(fileName,true);
        // Error while loading?
        if( fis == null ) { 
            System.err.println("Can't load file: '" + fileName + "'");
            return 0;
        }
        
        // Show 
       // fis.chart();
        
        //set input
        fis.setVariable("confidence", app.confidence);
        
        /*evaluate*/
        fis.evaluate();

        /*get output*/
       // fis.getVariable("priority").chartDefuzzifier(true);
        double output=fis.getVariable("priority").defuzzify();
        return output;
	}
	
	
}
