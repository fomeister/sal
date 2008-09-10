package jcu.sal.client.stressTest.actions;

import java.rmi.RemoteException;
import java.util.Set;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.components.EndPoints.FSEndPoint;
import jcu.sal.components.protocols.dummy.DummyProtocol;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

public class AddProtocolAction implements Action {
	private static Logger logger = Logger.getLogger(AddProtocolAction.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String name="Dummy_";
	public static int MAX_PROTOCOLS=5;
	
	private static String type = DummyProtocol.PROTOCOL_TYPE;
	
	private RMISALAgent agent;
	
	public AddProtocolAction(RMISALAgent a){
		agent = a;
	}

	public void execute() {
		ProtocolConfigurations p;
		int i;
		try {
			p = new ProtocolConfigurations(agent.listProtocols());
		} catch (Exception e1) {
			logger.info("we shouldnt be here");
			e1.printStackTrace();
			return;
		}

		String n1=null, n2;
		Set<String> s = p.getPIDs();
		for(i=0; i<MAX_PROTOCOLS;i++){
			n2 = name+i; 
			if(!s.contains(n2)){
				n1 = n2;
				break;
			}			
		}
		if(n1==null){
			//logger.info("cant create more protocols, MAX reached");
			return;
		}
		
		try {
			//logger.info("creating protocol "+n1);
			agent.addProtocol(new ProtocolConfiguration(n1, type, new EndPointConfiguration("fs_"+i, FSEndPoint.ENDPOINT_TYPE)).getXMLString(),false);
			//logger.info("protocol "+n1+" created");
		} catch (ConfigurationException e) {
			logger.info("protocol "+n1+" already exists");
		} catch (RemoteException e) {
			logger.info("Cant create protocol "+n1);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			logger.info("Cant create protocol "+n1);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
