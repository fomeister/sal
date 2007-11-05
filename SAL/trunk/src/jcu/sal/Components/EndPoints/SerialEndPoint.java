/**
 * 
 */
package jcu.sal.Components.EndPoints;

import java.util.Enumeration;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import javax.management.BadAttributeValueExpException;

import jcu.sal.utils.Slog;
import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class SerialEndPoint extends EndPoint {

	public static final String PORTDEVICEATTRIBUTE_TAG="PortDeviceFile";
	public static final String PORTSPEEDATTRIBUTE_TAG="PortSpeed";
	public static final String DATABITSATTRIBUTE_TAG="DataBits";
	public static final String PARITYATTRIBUTE_TAG="Parity";
	public static final String STOPBITATTRIBUTE_TAG="StopBit";
	
	private Logger logger = Logger.getLogger(SerialEndPoint.class);
	
	/**
	 * 
	 */
	public SerialEndPoint() {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor SerialEndPoint");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws RuntimeException {
		// Check if we have this serial port on this platform
		CommPortIdentifier id;
		this.logger.debug("check if we can setup the serial port");
		try {
			id = CommPortIdentifier.getPortIdentifier(getConfig(PORTDEVICEATTRIBUTE_TAG));
			if(id.getPortType()!=CommPortIdentifier.PORT_SERIAL) {
				this.logger.error("The supplied device file is NOT a serial port");
				throw new RuntimeException("Could not setup the serial port");
			}

			this.logger.debug("The serial port name is " + id.getName());
			SerialPort p = (SerialPort) id.open("SALv1", 20);
			p.setSerialPortParams(Integer.valueOf(getConfig(PORTSPEEDATTRIBUTE_TAG)),
					Integer.valueOf(getConfig(DATABITSATTRIBUTE_TAG)),
					Integer.valueOf(getConfig(STOPBITATTRIBUTE_TAG)),
					Integer.valueOf(getConfig(PARITYATTRIBUTE_TAG)));
			p.close();
			this.configured = true;
			this.logger.debug("The serial port was configured successfully");
		} catch (PortInUseException e) {
			this.logger.warn("The serial port cannot be opened and is currently in use ...");
			e.printStackTrace();
			throw new RuntimeException("Could not setup the serial port");
		} catch (NoSuchPortException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not setup the serial port");
		} catch (BadAttributeValueExpException e) {
			e.printStackTrace();
			this.logger.debug("Bad serial EndPoint XML config");
			throw new RuntimeException("Could not setup the serial port");
		} catch (UnsupportedCommOperationException e) {
			this.logger.warn("The serial port cannot be setup");
			e.printStackTrace();
			throw new RuntimeException("Could not setup the serial port");
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove() {
		//Not much to do here...
		this.logger.debug("Removing serial Endpoint.");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#start()
	 */
	@Override
	public void start() {
		if(configured && !started) {
			this.logger.debug("Starting serial Endpoint.");
			started=true;
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		if(started) {
			this.logger.debug("Stopping serial Endpoint.");
			started=false;
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