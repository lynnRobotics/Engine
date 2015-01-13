package IntelM2M.agent.visual;

public class IdealLuxLevel{
	   public static double getIdealLuxLevel(String activity) {
	       double level = 0;
	       /* Simulator */
//		   if(activity.equals("ComeBack"))
//	           level = 7;
//	       else if(activity.equals("GoOut"))
//	           level = 7;
//	       else if(activity.equals("Sleeping"))
//	           level = 1;
//	       else if(activity.equals("WatchingTV"))
//	           level = 11;
//	       else if(activity.equals("TakingBath"))
//	           level = 12;
//	       else if(activity.equals("UsingPC"))
//	           level = 12.5;
//	       else if(activity.equals("Laundering"))
//	           level = 11;
//	       else if(activity.equals("ReadingBook"))
//	           level = 14.5;
//	       else if(activity.equals("Cleaning"))
//	           level = 12;
//	       else if(activity.equals("Cooking"))
//	           level = 12;
//	       else if(activity.equals("PlayingKinect"))
//	           level = 11;
//	       else if(activity.equals("Chatting"))
//	           level = 11;
//	       else if(activity.equals("Studying"))
//	           level = 14.5;
//	       else if(activity.equals("UsingRestroom"))
//	           level = 12;
//	       else if(activity.equals("ListeningMusic"))
//	           level = 12;
//	       else if(activity.equals("BrushingTooth"))
//	           level = 12;
//	       else if(activity.equals("WashingDishes"))
//	           level = 12;
	       
		   /* BL313 */
		   if(activity.equals("ComeBack"))
	           level = 7;
	       else if(activity.equals("GoOut"))
	           level = 7;
	       else if(activity.equals("Sleeping"))
	           level = 1;
	       else if(activity.equals("AllSleeping"))
	           level = 1;
	       else if(activity.equals("WatchingTV"))
	           level = 12;
	       else if(activity.equals("PlayingKinect"))
	           level = 7;
	       else if(activity.equals("Chatting"))
	           level = 7;
	       else if(activity.equals("Reading"))
	           level = 12;
	       else if(activity.equals("PreparingFood"))
	           level = 8;
	       else if(activity.equals("UsingPC"))
	           level = 12;
	       else if(activity.equals("UsingNoteBook"))
	           level = 12;
	       else if(activity.equals("Studying"))
	           level = 12;
		   
		   /* BL313 for DEMO*/
//		   if(activity.equals("ComeBack"))
//	           level = 7;
//	       else if(activity.equals("GoOut"))
//	           level = 7;
//	       else if(activity.equals("Sleeping"))
//	           level = 1;
//	       else if(activity.equals("AllSleeping"))
//	           level = 1;
//	       else if(activity.equals("WatchingTV"))
//	           level = 12;
//	       else if(activity.equals("PlayingKinect"))
//	           level = 11;
//	       else if(activity.equals("Chatting"))
//	           level = 7;
//	       else if(activity.equals("Reading"))
//	           level = 12;
//	       else if(activity.equals("PreparingFood"))
//	           level = 8;
//	       else if(activity.equals("UsingPC"))
//	           level = 10;
//	       else if(activity.equals("UsingNoteBook"))
//	           level = 9;
//	       else if(activity.equals("Studying"))
//	           level = 9;
		   
	       return level;
	   }
	   
	   public static double getMarginIdealLux(String activity){
		   double idealLux = 0;
		   
		   /* BL313 */
		   if(activity.equals("ComeBack"))
			   idealLux = 22;
	       else if(activity.equals("GoOut"))
	    	   idealLux = 22;
	       else if(activity.equals("Sleeping"))
	    	   idealLux = 0;
	       else if(activity.equals("AllSleeping"))
	    	   idealLux = 0;
	       else if(activity.equals("WatchingTV"))
	    	   //idealLux = 156;
		       idealLux = 200;
	       else if(activity.equals("PlayingKinect"))
	    	   idealLux = 80;
	    	   //idealLux = 106;
	       else if(activity.equals("Chatting"))
	    	   idealLux = 22;
	       else if(activity.equals("Reading"))
	    	   //idealLux = 156;
	    	   idealLux = 200;
	       else if(activity.equals("PreparingFood"))
	    	   idealLux = 106;
	       else if(activity.equals("UsingPC"))
	    	   idealLux = 156;
	       else if(activity.equals("UsingNoteBook"))
	    	   idealLux = 156;
	       else if(activity.equals("Studying"))
	    	   idealLux = 156;
		   
		   return idealLux;
	   }
}
