package jcu.sal.client.stressTest.actions;

import java.io.NotActiveException;
import java.rmi.RemoteException;
import java.util.Random;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.RMICommandFactory;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.sml.SMLDescriptions;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;

public class ExecuteSensorAction implements Action {
	private static Logger logger = Logger.getLogger(ExecuteSensorAction.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String name="Dummy_";
	
	private RMISALAgent agent;
	private Random r;
	private RMICommand cmd;
	
	public ExecuteSensorAction(RMISALAgent a){
		agent = a;
		r = new Random();
		cmd = null;
	}

	public void execute() {
		SMLDescriptions p;
		try {
			p = new SMLDescriptions(agent.listActiveSensors());
		} catch (Exception e1) {
			logger.info("we shouldnt be here");
			e1.printStackTrace();
			return;
		}
		
		if(p.getSize()==0){
			//logger.info("no sensors yet");
			return;
		}

		Integer name=null;
		String protocol=null;
		int i = r.nextInt(p.getSize());
		int j=0;
		for(Integer n: p.getSIDs()){
			if(j++==i) {
				name = n;
				try {
					protocol = p.getDescription(name.intValue()).getProtocolName();
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		
		if(cmd==null){			
			try {
				RMICommandFactory f = new RMICommandFactory(XMLhelper.createDocument(agent.getCML(name.toString())), 100);
				cmd = f.getCommand();
			} catch (Exception e) {
				logger.info("Shouldnt be here");
				e.printStackTrace();
				return;
			}			
		}

		try {
			//logger.info("executing command on sensor "+name+" ("+protocol+")");
			agent.execute(cmd, name.toString());
			//logger.info("command executed by sensor "+name);
		} catch (ConfigurationException e) {
			logger.info("command NOT executed by sensor "+name+" "+protocol);
			e.printStackTrace();
		} catch (RemoteException e) {
			logger.info("sensor "+name+" cant be removed");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotActiveException e) {
			logger.info("sensor "+name+" cant be removed");
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (BadAttributeValueExpException e) {
			logger.info("sensor "+name+" cant be removed");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
