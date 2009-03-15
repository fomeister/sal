package jcu.sal.client.stressTest.actions;

import java.rmi.RemoteException;
import java.util.Random;

import jcu.sal.common.Slog;
import jcu.sal.common.agents.rmi.RMIAgent;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.pcml.ProtocolConfigurations;

import org.apache.log4j.Logger;

public class RemoveProtocolAction implements Action {
	private static Logger logger = Logger.getLogger(RemoveProtocolAction.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String name="Dummy_";
	
	private RMIAgent agent;
	private Random r;
	
	public RemoveProtocolAction(RMIAgent a){
		agent = a;
		r = new Random();
	}

	public void execute() {
		ProtocolConfigurations p;
		try {
			p = new ProtocolConfigurations(agent.listProtocols());
		} catch (Exception e1) {
			logger.info("we shouldnt be here");
			e1.printStackTrace();
			return;
		}
		
		if(p.getSize()==0){
			//logger.info("no protocols yet");
			return;
		}

		String name=null;
		int i = r.nextInt(p.getSize());
		int j=0;
		for(String n: p.getPIDs()){
			if(j++==i) {
				name = n;
				break;
			}
		}

		try {
			//logger.info("removing protocol "+name);
			agent.removeProtocol(name,false);
			//logger.info("protocol "+name+" removed");
		} catch (NotFoundException e) {
			logger.info("protocol "+name+" doesnt exist");
		} catch (RemoteException e) {
			logger.info("Cant remove protocol "+name);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
