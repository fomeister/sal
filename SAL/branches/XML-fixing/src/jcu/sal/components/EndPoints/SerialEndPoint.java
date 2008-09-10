/**
 * 
 */
package jcu.sal.components.EndPoints;

import java.util.Enumeration;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class SerialEndPoint extends EndPoint {

	public static final String PORTDEVICEATTRIBUTE_TAG = "PortDeviceFile";
	public static final String PORTSPEEDATTRIBUTE_TAG = "PortSpeed";
	public static final String DATABITSATTRIBUTE_TAG = "DataBits";
	public static final String PARITYATTRIBUTE_TAG = "Parity";
	public static final String STOPBITATTRIBUTE_TAG = "StopBit";
	public static final String ENDPOINT_TYPE = "serial";
	
	private static Logger logger = Logger.getLogger(SerialEndPoint.class);
	static {Slog.setupLogger(logger);}
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public SerialEndPoint(EndPointID i,  EndPointConfiguration c) throws ConfigurationException {
		super(i,ENDPOINT_TYPE,c);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Check if we have this serial port on this platform
		CommPortIdentifier id;
		//logger.debug("check if we can setup the serial port");
		try {
			id = CommPortIdentifier.getPortIdentifier(getParameter(PORTDEVICEATTRIBUTE_TAG));
			if(id.getPortType()!=CommPortIdentifier.PORT_SERIAL) {
				logger.error("The supplied device file is NOT a serial port");
				throw new ConfigurationException("Could not setup the serial port");
			}

			//logger.debug("The serial port name is " + id.getName());
			SerialPort p = (SerialPort) id.open("SALv1", 20);
			p.setSerialPortParams(Integer.valueOf(getParameter(PORTSPEEDATTRIBUTE_TAG)),
					Integer.valueOf(getParameter(DATABITSATTRIBUTE_TAG)),
					Integer.valueOf(getParameter(STOPBITATTRIBUTE_TAG)),
					Integer.valueOf(getParameter(PARITYATTRIBUTE_TAG)));
			p.close();
			configured = true;
			//logger.debug("The serial port was configured successfully");
		} catch (PortInUseException e) {
			logger.error("The serial port cannot be opened and is currently in use ...");
			e.printStackTrace();
			throw new ConfigurationException("Could not setup the serial port");
		} catch (NoSuchPortException e) {
			e.printStackTrace();
			throw new ConfigurationException("Could not setup the serial port");
		} catch (BadAttributeValueExpException e) {
			e.printStackTrace();
			logger.error("Bad serial EndPoint XML config");
			throw new ConfigurationException("Could not setup the serial port");
		} catch (UnsupportedCommOperationException e) {
			logger.error("The serial port cannot be setup");
			e.printStackTrace();
			throw new ConfigurationException("Could not setup the serial port");
		}
	}

	public static void main(String[] args) {
		/* Lists javax.comm recognised-serial ports */
		CommPortIdentifier portId;
		Enumeration e = CommPortIdentifier.getPortIdentifiers();
		System.out.println("List of serial ports reported by javax.comm:");
		while(e.hasMoreElements()) {
			portId= (CommPortIdentifier) e.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Port: " +portId.getName() + " - Currently used? " + portId.isCurrentlyOwned());
			} 
		}
	}
}
