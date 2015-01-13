package IntelM2M.datastructure;

public class ExpResult {
	public double tp,tn,fp,fn;
	/*add for 慧文的算法*/
	public double precision,recall,accuracy;
	public double precisionCount;
	public double recallCount;
	public double accuracyCount;

	public ExpResult(){
		tp=0;
		tn=0;
		fp=0;
		fn=0;
		/*add for 慧文的算法*/
		precision=0;
		recall=0;
		accuracy=0;
		precisionCount=0;
		recallCount=0;
		accuracyCount=0;
	}
	static public void add(ExpResult r1,ExpResult r2){

		r1.tp=r1.tp+r2.tp;
		r1.tn=r1.tn+r2.tn;
		r1.fp=r1.fp+r2.fp;
		r1.fn=r1.fn+r2.fn;
		//
		r1.precision=r1.precision+r2.precision;
		r1.recall=r1.recall+r2.recall;
		r1.accuracy=r1.accuracy+r2.accuracy;
		r1.precisionCount=r1.precisionCount+r2.precisionCount;
		r1.accuracyCount=r1.accuracyCount+r2.accuracyCount;
		r1.recallCount=r1.recallCount+r2.recallCount;
	}
	
	public void recordForKerropi(double tp,double tn,double fp,double fn){
		
		this.tp+=tp;
		this.tn+=tn;
		this.fp+=fp;
		this.fn+=fn;
		
		if(tp==0 && fp==0){
			
		}
		else{
			precisionCount+=1;
			double p=tp/(tp+fp);
			precision=precision+(tp/(tp+fp));
		}
		if(tp==0 && fn==0){
			
		}
		else{
			recallCount+=1;
			double r=tp/(tp+fn);
			recall= recall +(tp/(tp+fn));
		}
		accuracyCount+=1;
		double a=tp/(tp+fn+tn+fp);
		accuracy =accuracy +( (tp+tn)/(tp+tn+fp+fn) );
	}
}
