package IntelM2M.tmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import s2h.platform.annotation.MessageFrom;
import s2h.platform.annotation.UPnP;
import s2h.platform.node.LogicNode;
import s2h.platform.node.NodeRunner;
import s2h.platform.node.PlatformMessage;
import s2h.platform.node.PlatformTopic;
import s2h.platform.support.JsonBuilder;
import s2h.platform.support.MessagePropertiesBuilder;
import s2h.platform.support.MessageUtils;


@MessageFrom(PlatformTopic.CONTEXT)
@UPnP

public class EpcieContextProcessor extends LogicNode {


	private Log log = LogFactory.getLog( EpcieContextProcessor.class.getName());
	//private final static JsonBuilder json = MessageUtils.jsonBuilder();

	private final static MessagePropertiesBuilder props = MessageUtils.propertiesBuilder();
   

   	
    
	protected void processMessage(PlatformMessage message)
	{
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
