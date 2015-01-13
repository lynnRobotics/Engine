package IntelM2M.mq;

@URL("tcp://140.112.49.154:61616")
@TOPIC("ssh.COMMAND")
public class Consumer extends BaseConnector{

    String msg;
    Listener listener;
    
	@Override
	public void sendOut(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processMsg(String m) {
		//System.out.println("recv a message from MQ.");
		this.msg = m;
		//trigger();
	}
	
	public String getMsg(){
		return msg;
	}
	
	public void addListener(Listener l) {
		listener = l;
	}
	
	public void trigger() {
		listener.onEvent(msg);
	}
	
}