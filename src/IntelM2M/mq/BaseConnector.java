package IntelM2M.mq;


import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface URL {
	//String value() default ActiveMQConnection.DEFAULT_BROKER_URL;
	String value() default "failover://(tcp://localhost:61616)";
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface TOPIC {
	String value() default "ssh.CONTEXT";
}

@URL()
@TOPIC()
public abstract class BaseConnector implements MessageListener {
	String url;
	String topic;
	Session session;
	Destination destination;
	ActiveMQConnection connection;
	MessageProducer sendor;
	String tcp_transport_options = "?timeout=5000&maxReconnectAttempts=0"; // a string appending to url
	
	BaseConnector() {
		Class<? extends BaseConnector> c = this.getClass();
		boolean flag = c.isAnnotationPresent(URL.class);
		if (flag) {
			URL url_des = (URL) c.getAnnotation(URL.class);
			url = url_des.value();
			//System.out.println(this.getClass().getName()+": URL = " + url);
		}
		flag = c.isAnnotationPresent(TOPIC.class);
		if (flag) {
			TOPIC topic_des = (TOPIC) c.getAnnotation(TOPIC.class);
			topic = topic_des.value();
			//System.out.println(this.getClass().getName()+": TOPIC = " + topic);
		}
	}
	
	// To format the String:`url` into String:"failover://(`url`)"
	private String toFailoverURL(String url) {
		return "failover://("+url+")";
	}
	
	public boolean connect() {
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(toFailoverURL(url)+tcp_transport_options);
			
			connection = (ActiveMQConnection)connectionFactory.createConnection();
			connection.start();
			// JMS messages are sent and received using a Session. We will
	        // create here a non-transactional session object. If you want
	        // to use transactions you should set the first parameter to 'true'
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			return true;
		} catch (JMSException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// different from close(), it doesn't close connection
	public void stop() {
		try {
			connection.stop();
		} catch (JMSException e) {
			e.printStackTrace();
			disconnect();
		}
	}
	
	// different from connect(), it awake the stopped connection
	public void start() {
		try {
			connection.start();
		} catch (JMSException e) {
			e.printStackTrace();
			disconnect();
			connect();
		}
	}
	
	public boolean isStarted() {
		return connection.isStarted();
	} 
	
	// listen to the default topic in the Class
	public void listen() {
        try {
        	// Destination represents here our topic 'ssh.CONTEXT' on the
            // JMS server. You don't have to do anything special on the
            // server to create it, it will be created automatically.
			destination = session.createTopic(this.topic);
			 // MessageConsumer is used for receiving (consuming) messages
	        MessageConsumer consumer = session.createConsumer(destination);
	        consumer.setMessageListener(this);
	        // Here we receive the message.
	        // By default this call is blocking, which means it will wait
	        // for a message to arrive on the queue/topic.
		} catch (JMSException e) {
			e.printStackTrace();
		} 
	}
	
	// listen to the specific topic rather than default one declared in the Class
	public void listen(String other_topic) {
	        try {
	        	// Destination represents here our topic 'ssh.CONTEXT' on the
	            // JMS server. You don't have to do anything special on the
	            // server to create it, it will be created automatically.
				destination = session.createTopic(other_topic);
				 // MessageConsumer is used for receiving (consuming) messages
		        MessageConsumer consumer = session.createConsumer(destination);
		        consumer.setMessageListener(this);
		        // Here we receive the message.
		        // By default this call is blocking, which means it will wait
		        // for a message to arrive on the queue/topic.
			} catch (JMSException e) {
				e.printStackTrace();
			} 
		}
	
	public void disconnect() {
		try {
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
		
	// send to the default topic declared in the Class
	public void getSendor() {
		try {
			destination = session.createTopic(topic);
			// MessageProducer is used for sending messages (as opposed
	        // to MessageConsumer which is used for receiving them)
	        sendor = session.createProducer(destination);
		} catch (JMSException jms_error) {
			
		}
	}
	
	//send to the specific topic rather than default one declared in the Class
	public void getSendor(String target_topic) {
		try {
			destination = session.createTopic(target_topic);
			// MessageProducer is used for sending messages (as opposed
	        // to MessageConsumer which is used for receiving them)
			sendor = session.createProducer(destination);
		} catch (JMSException jms_error) {
			
		}
	}
	
	public void onMessage(Message message) {   
		 // invoke processMsg() to process message in a String type
		if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
            	processMsg(textMessage.getText());
			} catch (JMSException e) {
				e.printStackTrace();
				if(!this.isStarted()) {
					this.start();
					this.listen();
				}
			}
        }
	}   
	
	public void setURL(String u) {
		this.url = u;
	}
	
	public void setTopic(String t) {
		this.topic = t;
	}
	
	public abstract void sendOut(String s);
	
	public abstract void processMsg(String msg);
	
}
