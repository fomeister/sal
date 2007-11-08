/**
 * 
 */
package jcu.sal.Components.Protocols;

import java.io.BufferedReader;
import java.io.IOException;
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
	public final static String OWFSMOUNTPOINT_TAG = "MountPoint";
	
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
		String mtpt, temp;
		logger.debug("Checking OWFS software");

		try {
			mtpt = getConfig(OWFSMOUNTPOINT_TAG);
			if(mtpt.length()==0) throw new BadAttributeValueExpException("Empty mount point directive...");
			
			//concurrent instances of owfs can coexist as long as their mount points are different
			//Check whether instances of owfs are using the same mount point as ours
			if(ProcessHelper.getRunningProcessArgs("owfs").containsValue(mtpt)) {
				logger.error("An instance of owfs seems to be using the same mountpoint as ours: " + mtpt);
				throw new BadAttributeValueExpException("Wrong OWFS mount point configuration");
			}

			//Next, we check that OWFS is installed in the given directory
			this.logger.debug("Detecting OWFS version");
			BufferedReader[] b = ProcessHelper.captureOutputs(getConfig(OWFSLOCATION_TAG) + " --version");
			while((temp = b[0].readLine()) != null) logger.debug(temp);
			while((temp = b[1].readLine()) != null) logger.debug(temp);
			
			configured = true;
			logger.debug("OWFS protocol configured");
			
		} catch (IOException e) {
			this.logger.error("Could NOT run/read owfs");
			e.printStackTrace();
			throw new RuntimeException("Could NOT run/read owfs");
		} catch (BadAttributeValueExpException e) {
			this.logger.error("incorrect OWFS configuration directives...");
			e.printStackTrace();
			throw new RuntimeException("Could not setup OWFS protocol");
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
		c.put(OwfsProtocol.OWFSMOUNTPOINT_TAG, "/mnt/w1");
		OwfsProtocol o = new OwfsProtocol(new ProtocolID("owfs"), "owfs", c);
		o.dumpConfig();
	}
	
}
