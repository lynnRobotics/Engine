package IntelM2M.mchess;

import org.apache.log4j.Logger;
import s2h.platform.support.MessageUtils;

/* Import necessary package */
import IntelM2M.epcie.Epcie;
import IntelM2M.esdse.Esdse;
import IntelM2M.mq.Consumer;
import IntelM2M.test.SimulatorTest;

/**
 * MCHESS
 * 
 * @author Mao (2012.06)
 */

public class Mchess extends Consumer implements Runnable {

	/* Two main engine */
	static Epcie epcie = null;
	static Esdse esdse;
	
	s2h.platform.support.JsonBuilder json = MessageUtils.jsonBuilder();
	//private Logger log = Logger.getLogger(Mchess.class.getName());

	public Mchess(String mode) {
		super();               // Consumer's constructor
		esdse = new Esdse();   // Initialization process
		/* Set up MQ information */
		this.setURL("tcp://140.112.49.154:61616");
		this.setTopic("ssh.RAW_DATA");
		this.connect();
		this.listen();
		
		//sysProcForSimulator();
		
		if(mode.equals("-run")) {
			realTimeSysProc();     // Call build model function in epcie
		}
	}

	/* For simulated usage */
	public void sysProcForSimulator() {
		epcie.buildModel();
		// Simulate sensor data
		SimulatorTest test = new SimulatorTest();
		test.simulatorTesting(epcie, esdse);
	}

	/* For real-time usage */
	public void realTimeSysProc() {
		epcie.buildModel();
	}
	
	/* MQ message process function */
	@Override
	public void processMsg(String m) {
		esdse.processMQMessage(epcie, m);
	}
	
	/* Define what M-CHESS thread should do */ 
	@Override
	public void run() {
		while(true){
			/* If thread doesn't start then try to start again */
			if(!this.isStarted()) {
				this.start();
				this.listen();
				esdse.backToLive = true;
				esdse.signal = false;
			} 
			/* Thread starts, send out the start signal */
			else if (esdse.backToLive) {
				json.reset();
				esdse.producer.sendOut(json.add("subject", "signal").add("current_resend", "start").toJson(), "ssh.RAW_DATA");
			}
			/* Try to sleep one second */
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("please add the argument!\ne.g. [mchess] -run 3 or [mchess] -learn");
			return; // Test necessary argument from input
		}
		
		if(args[0].equals("-run")) {
			System.out.println("===Running Mode===");
			if (args.length < 2) {
				System.out.println("...using default training threshold: 3");
				epcie = new Epcie("3");  // New a epcie object and set threshold (args[0])
				new Mchess(args[0]).run();          // New a M-CHESS object and start the thread
			}
			else {
				epcie = new Epcie(args[1]);  // New a epcie object and set threshold (args[0])
				new Mchess(args[0]).run();          // New a M-CHESS object and start the thread
			}
		}
		else if(args[0].equals("-learn")) {
			System.err.println("===Learning Mode===");
			new Mchess(args[0]).run();          // New a M-CHESS object and start the thread
		}
	}
}
