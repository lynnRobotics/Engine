package IntelM2M.mq;
import javax.jms.*;

@URL("tcp://140.112.49.154:61616")
// "tcp://140.112.49.154:61616";	/* IP of lab313: 140.112.49.154:61616 */
@TOPIC("ssh.RAW_DATA")
public class Producer extends BaseConnector {
	String msg;
	@Override
	public void sendOut(String s) {
		msg = s;
        TextMessage message;
		try {
			if(!this.isStarted()) {
				this.start();
				this.getSendor();
			}
			message = session.createTextMessage(s);
			// Here we are sending the message!
			this.sendor.send(message);
	        //System.out.println("Sent message: " + message.getText() + "(count " + count + ")");	
		} catch (JMSException e) {
			e.printStackTrace();
		}	
	}

	// !! please use this method carefully, and not frequently
	public void sendOut(String s, String topic) {
		msg = s;
        TextMessage message;
        Destination dest;
		try {
			if(!this.isStarted()) {
				this.start();
				this.getSendor();
			}
			dest = session.createTopic(topic);
			message = session.createTextMessage(s);
			// Here we are sending the message!
			session.createProducer(dest).send(message);
	        //System.out.println("Sent message: " + message.getText() + "(count " + count + ")");	
		} catch (JMSException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void processMsg(String msg) {
		
	}
	
	public String getMsg(){
		return msg;
	}
	
}
