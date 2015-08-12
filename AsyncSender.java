package asyncSender;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import deliveryDelay.JMSDelayExample;

//SOAP Message
//<m:placeOrder xmlns:m="http://services.samples">
//<m:order>
//    <m:price>82.9788684380384</m:price>
//    <m:quantity>17215</m:quantity>
//    <m:symbol>IBM</m:symbol>
//</m:order>
//</m:placeOrder>

public class AsyncSender {
	private static final Logger log = Logger.getLogger(JMSDelayExample.class.getName());

    // Set up all the default values
	private static final String param="IBM";
	//with header for inbounds
	private static final String MESSAGE_WITH_HEADER =  "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "   <soapenv:Header/>\n" +
            "<soapenv:Body>\n" +
            "<m:placeOrder xmlns:m=\"http://services.samples\">\n" +
			"    <m:order>\n" +
			"        <m:price>" + getRandom(100, 0.9, true) + "</m:price>\n" +
			"        <m:quantity>" + (int) getRandom(10000, 1.0, true) + "</m:quantity>\n" +
			"        <m:symbol>" + param + "</m:symbol>\n" +
			"    </m:order>\n" +
			"</m:placeOrder>" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
	//for ESB transports
	private static final String DEFAULT_MESSAGE =	"<m:placeOrder xmlns:m=\"http://services.samples\">\n" +
													"    <m:order>\n" +
													"        <m:price>" + getRandom(100, 0.9, true) + "</m:price>\n" +
													"        <m:quantity>" + (int) getRandom(10000, 1.0, true) + "</m:quantity>\n" +
													"        <m:symbol>" + param + "</m:symbol>\n" +
													"    </m:order>\n" +
													"</m:placeOrder>";
    private static final String DEFAULT_CONNECTION_FACTORY = "QueueConnectionFactory";
    private static final String DEFAULT_DESTINATION = "queue/mySampleQueue";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jnp.interfaces.NamingContextFactory";
    private static final String PROVIDER_URL = "jnp://localhost:1099";

    public static void main(String[] args) {

        Context namingContext = null;

        try {

            // Set up the namingContext for the JNDI lookup
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
            namingContext = new InitialContext(env);

            // Perform the JNDI lookups
            String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
            log.info("Attempting to acquire connection factory \"" + connectionFactoryString + "\"");
            ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(connectionFactoryString);
            log.info("Found connection factory \"" + connectionFactoryString + "\" in JNDI");

            String destinationString = System.getProperty("destination", DEFAULT_DESTINATION);
            log.info("Attempting to acquire destination \"" + destinationString + "\"");
            Destination destination = (Destination) namingContext.lookup(destinationString);
            log.info("Found destination \"" + destinationString + "\" in JNDI");

           // String content = System.getProperty("message.content", DEFAULT_MESSAGE);
           String content = System.getProperty("message.content", MESSAGE_WITH_HEADER);

           for (int i=0; i<10;i++)asyncSendClassic(connectionFactory,(Queue)destination,content);
            
        } catch (NamingException e) {
            log.severe(e.getMessage());
        } catch (Exception e) {
        	log.severe(e.getMessage());
        } finally {
            if (namingContext != null) {
                try {
                    namingContext.close();
                } catch (NamingException e) {
                    log.severe(e.getMessage());
                }
            }
        }
    }
    
    private static void asyncSendClassic(ConnectionFactory connectionFactory,Queue queue,String content) throws Exception
    		    {

    		   // send a message asynchronously
    		   try (Connection connection = connectionFactory.createConnection();){
    		      Session session = connection.createSession();
    		      MessageProducer messageProducer = session.createProducer(queue);
    		      TextMessage textMessage = session.createTextMessage(content);
    		      messageProducer.send(textMessage,new MyCompletionListener());
    		      System.out.println("Message sent, now waiting for reply");

    		   }
    		}
    
    private static double getRandom(double base, double varience, boolean onlypositive) {
		double rand = Math.random();
		return (base + ((rand > 0.5 ? 1 : -1) * varience * base * rand))
		       * (onlypositive ? 1 : (rand > 0.5 ? 1 : -1));
	}
}
