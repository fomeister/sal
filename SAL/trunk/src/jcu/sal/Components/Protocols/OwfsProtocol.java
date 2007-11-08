/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;

import jcu.sal.Components.Command;
import jcu.sal.Components.Identifiers.ProtocolID;
import jcu.sal.Components.Sensors.Sensor;
import jcu.sal.utils.ProcessHelper;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class OwfsProtocol extends Protocol {

	private Logger logger = Logger.getLogger(OwfsProtocol.class);
	public final static String OWFSLOCATION_TAG = "Location";
	
	/**
	 * 
	 */
	public OwfsProtocol(ProtocolID i, String t, Hashtable<String,String> c) {
		super(i,t,c);
		Slog.setupLogger(logger);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.Protocols.Protocol#execute(jcu.sal.Components.Command)
	 */
	@Override
	public String execute(Command c, Sensor s) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void parseConfig() throws RuntimeException {
		//concurrent instances of owfs can coexist as long as their mount point is different
		//Check whether an instance is is using the same mount point as ours
		

		//Check that OWFS is installed
		logger.debug("Checking OWFS version");
		try {
			Process p = ProcessHelper.createProcess(getConfig(OWFSLOCATION_TAG) + " --version");
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while(b.ready()) logger.debug(b.readLine());
			b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while(b.ready()) logger.debug(b.readLine());

		} catch (IOException e) {
			this.logger.debug("Could NOT run/read from owfs");
			e.printStackTrace();
			throw new RuntimeException("Did not detect OWFS");
		} catch (BadAttributeValueExpException e) {
			this.logger.debug("unable to find OWFS configuration directives...");
			e.printStackTrace();
			throw new RuntimeException("Did not detect OWFS");
		}
		

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#remove()
	 */
	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#start()
	 */
	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see jcu.sal.Components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}


	@Override
	public void probeSensors() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Hashtable<String, String> c = new Hashtable<String, String>();
		c.put(OwfsProtocol.OWFSLOCATION_TAG, "/opt/owfs/bin/owfs");
		OwfsProtocol o = new OwfsProtocol(new ProtocolID("owfs"), "owfs", c);
		o.dumpConfig();
	}
	
}
