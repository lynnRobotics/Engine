package IntelM2M.tmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import IntelM2M.epcie.classifier.DbnClassifier;

import s2h.platform.annotation.MessageFrom;
import s2h.platform.annotation.UPnP;
import s2h.platform.node.LogicNode;
import s2h.platform.node.NodeRunner;
import s2h.platform.node.PlatformMessage;
import s2h.platform.node.PlatformTopic;
import s2h.platform.support.JsonBuilder;
import s2h.platform.support.MessagePropertiesBuilder;
import s2h.platform.support.MessageUtils;


@MessageFrom(PlatformTopic.RAW_DATA)
@UPnP
public class EpcieRawProcessor extends LogicNode {
	
    
	public static DbnClassifier DBN ;
	public static KmCluster KM;

    public EpcieRawProcessor()
    {
    	super();
    

		    
    }
    
	
	
	public static void main(String[] args)
	{

		new NodeRunner(EpcieRawProcessor.class).execute();
	}

	private final static JsonBuilder json = MessageUtils.jsonBuilder();

	private final static MessagePropertiesBuilder props = MessageUtils.propertiesBuilder();
	
    
	protected void processMessage(PlatformMessage message)
	{
		System.out.println(extractValue(message));
		getSender().send("aa", PlatformTopic.CONTEXT);
	
	}



	/**
	* This function is to extract sensor value
	*/
	private String extractValue(PlatformMessage message)
	{
		String value = MessageUtils.get(message.getContent(), "value");

		return value;
	}

	/**
	* This function is to extract sensor name
	*/
	private String extractSubject(PlatformMessage message)
	{
		String subject = MessageUtils.get(message.getContent(), "subject");
        
		return subject;
	}




}