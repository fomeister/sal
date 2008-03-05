package jcu.sal.Components.Protocols;

import java.io.IOException;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Protocols.CMLStore.SimpleSNMPCML;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import uk.co.westhawk.snmp.pdu.BlockPdu;
import uk.co.westhawk.snmp.stack.AgentException;
import uk.co.westhawk.snmp.stack.PduException;
import uk.co.westhawk.snmp.stack.SnmpContext;
import uk.co.westhawk.snmp.stack.varbind;

public class SimpleSNMPProtocol extends Protocol{
	/**
	 * The string used in PCML docs to represent this protocol 
	 */
	public static final String SIMPLESNMPPROTOCOL_TYPE = "SSNMP";
	private static Logger logger = Logger.getLogger(SimpleSNMPProtocol.class);

	private String agent;
	private String comm_string;
	private int timeout;

	static { 
		Slog.setupLogger(logger);
		//Add to the list of supported EndPoint IDs
		SUPPORTED_ENDPOINT_TYPES.add("ethernet");
		//Add to the list of supported commands
		commands.put(new Integer(100), "getReading");

	}
	
	
	/**
	 * Construct the SimpleSNMPProtocol object. The Endpoint is instanciated in super(), 
	 * and parseConfig is called in super()
	 * @throws ConfigurationException if there is a problem with the component's config
	 * 
	 */
	public SimpleSNMPProtocol(ProtocolID i, Hashtable<String,String> c, Node d) throws ConfigurationException {
		super(i,SIMPLESNMPPROTOCOL_TYPE,c,d);
		
		cmls = new SimpleSNMPCML();
	}

	
	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_parseConfig()
	 */
	protected void internal_parseConfig() throws ConfigurationException {
		try {
			agent = getConfig("AgentIP");
			comm_string = getConfig("CommunityString");
		} catch (BadAttributeValueExpException e) {
			logger.error("Cant find 'AgentIP' / 'CommunityString' in Protocol config.");
			throw new ConfigurationException("SSNMP 'AgentIP' / 'CommunityString' config directives missing");
		}

		try { timeout = Integer.parseInt(getConfig("Timeout")); } catch (BadAttributeValueExpException e) { timeout=1500;}
		logger.debug("SimpleSNMP protocol configured");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_stop()
	 */
	protected void internal_stop() {}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_start()
	 */
	protected void internal_start() {}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocol#internal_remove()
	 */
	protected void internal_remove() {
	}
	
	@Override
	protected boolean internal_isSensorSupported(Sensor sensor){
		//TODO improve me... maybe check that the OID exist and is valid
		//TODO with a GET PDU... only problem: it requires the Agent to
		//TODO be accessible...
		return true;	
	}


	@Override
	protected boolean internal_probeSensor(Sensor s) {
		try {
			get(s.getNativeAddress());
			s.enable();
			return true;
		} catch (Exception e) {
			logger.error("couldnt probe sensor "+s.toString()+". Raised exception: "+e.getMessage());
		}
		s.disconnect();
		return false;
	}
	

	// TODO create an exception class for this instead of Exception
	public String getReading(Hashtable<String,String> c, Sensor s) throws IOException{
		String ret=null;
		logger.debug("getReading method called on sensor " +s.toString());
		ret = get(s.getNativeAddress());
		return ret;
	}


	@Override
	protected String internal_getCMLStoreKey(Sensor s){
		return "ALL";
	}
	

	public String get(String oid) throws IOException{
		SnmpContext s ;
		varbind v=null;
		String ret=null;
		s = new SnmpContext(agent, 161);
		s.setCommunity(comm_string);
		BlockPdu p = new BlockPdu(s);
		p.setPduType(BlockPdu.GET);
		p.addOid(oid);
		p.setRetryIntervals(new int[]{timeout});
		try {
			v = p.getResponseVariableBinding();
			if(v!=null) 
				ret = v.getValue().toString();
		} catch (AgentException e) {
			logger.error("SNMP response timeout while getting OID "+oid);
			throw new IOException("SNMP response timeout");
		} catch (PduException e) {
			logger.error("PDU exception while getting OID "+oid);
			throw new IOException("PDU exception");			
		}
		s.destroy();
		return ret;

	}
}
