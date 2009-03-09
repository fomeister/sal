package jcu.sal.client.gui.view;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;

import jcu.sal.client.gui.RMIClientController;
import jcu.sal.client.gui.RMIClientControllerImpl;
import jcu.sal.common.Constants;
import jcu.sal.common.RMICommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.RMIStreamCallback;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.RMIEventHandler;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.events.ProtocolListEvent;
import jcu.sal.events.SensorNodeEvent;
import jcu.sal.events.SensorStateEvent;
import jcu.sal.utils.Slog;

public class RMIClientView extends AbstractClientView implements RMIEventHandler, RMIStreamCallback{
	private static Logger logger = Logger.getLogger(RMIClientView.class);
	static {Slog.setupLogger(logger);}
	
	
	private String RMI_CLIENT_NAME = "Client View RMI Handler";
	private String name;
	private RMIClientController rmiController;
	private Hashtable<String, VideoViewer> viewers;

	
	public RMIClientView(RMIClientController c, String n){
		super(c);
		rmiController = c;
		name = n;
		viewers = new Hashtable<String, VideoViewer>();
	}
	
	public void connect(String agentIP, String ourIP) throws NotFoundException, RemoteException, ConfigurationException{		
		/*
		 * RMI stuff		
		 */
		rmiController.connect(name, agentIP, ourIP);
		rmiController.bind(RMI_CLIENT_NAME, this);
		rmiController.registerEventHandler(RMI_CLIENT_NAME, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
		rmiController.registerEventHandler(RMI_CLIENT_NAME, Constants.SENSOR_MANAGER_PRODUCER_ID);
		rmiController.registerEventHandler(RMI_CLIENT_NAME, Constants.SENSOR_STATE_PRODUCER_ID);
	}
	
	@Override
	public void release(){
		try {
			rmiController.unregisterEventHandler(RMI_CLIENT_NAME, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			rmiController.unregisterEventHandler(RMI_CLIENT_NAME, Constants.SENSOR_MANAGER_PRODUCER_ID);
			rmiController.unregisterEventHandler(RMI_CLIENT_NAME, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (NotFoundException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		try {
			rmiController.unbind(RMI_CLIENT_NAME);
		} catch (AccessException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		try {
			rmiController.disconnect(name);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}
	

	@Override
	public void collect(Response r) throws RemoteException {
		try {
			viewers.get(r.getSID()).setImage(r.getBytes());
		} catch (SensorControlException e) {
			addLog("Stream from sensor "+r.getSID()+" terminated");
			if(!e.getClass().equals(ClosedStreamException.class))
				addLog("Error: "+e.getMessage());
			viewers.get(r.getSID()).close();
			viewers.remove(r.getSID());
		}		
	}
	
	@Override
	public void sendCommand(Hashtable<String, String> values,
			final CMLDescription cml, final Context c) {

		final RMICommandFactory cf = new RMICommandFactory(cml);
		
		//put all arg-values in 
		for(String name: values.keySet())
			try {
				cf.addArgumentValue(name, values.get(name));
			} catch (ConfigurationException e) {
				addLog("The value '"+name+"' is incorrect.");
				return;
			}
		
		//check for callbacks
		if(cml.getArgTypes().contains(ArgumentType.CallbackArgument)){
			SMLDescription sml = tree.getSelectedLabel().getSMLDescription();
			List<String> names = cml.getArgNames();
			cf.addArgumentCallback(names.get(cml.getArgTypes().indexOf(ArgumentType.CallbackArgument)), name, RMI_CLIENT_NAME);
			viewers.put(c.getSMLDescription().getID(), new JPEGViewer(sml.getProtocolName()+" - "+sml.getID()));
		}
		//Must be sent in a separate thread. Otherwise, race condition occur
		//with v4l sensors which stream video handled by the AWT event dispatcher thread
		//it also makes the GUI more responsive for slow sensor, aka 1-wire
		new Thread(new Runnable(){
			public void run(){
				Response r;
				try {
					r = rmiController.execute(cf.getCommand(), c.getSMLDescription().getID());
					if(cml.getReturnType().equals(ReturnType.ByteArray)){
						new JPEGViewer(tree.getSelectedLabel().toString()).setImage(r.getBytes());
					} else if (r.getLength()>0)
						addLog("Command returned: "+r.getString());
					
				} catch (SensorControlException e) {
					addLog("Error executing command:\n"+e.getMessage()+"("+e.getCause().getMessage()+")");
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	
	}	

	@Override
	public void handle(Event e) throws RemoteException {
		addLog("Received event:\n"+e.toString());
		if(e instanceof ProtocolListEvent){
			ProtocolListEvent ple = (ProtocolListEvent) e;
			if(ple.getType()==ProtocolListEvent.PROTOCOL_ADDED){
				//add new protocol to tree
				try {
					ProtocolConfigurations ps = new ProtocolConfigurations(rmiController.listProtocols());
					tree.addProtocol(ps.getDescription(ple.getSourceID()));
				} catch (SALDocumentException e1) {
					e1.printStackTrace();
				} catch (NotFoundException e2) {
					e2.printStackTrace();
				}
			} else if(ple.getType()==ProtocolListEvent.PROTOCOL_REMOVED)
				tree.removeProtocol(ple.getSourceID());
			
		} else if (e instanceof SensorNodeEvent){
			SensorNodeEvent sne = (SensorNodeEvent) e;
			if(sne.getType()==SensorNodeEvent.SENSOR_NODE_ADDED){
				//add new sensor to tree
				try {
					SMLDescription sml = new SMLDescription(rmiController.listSensor(sne.getSourceID()));
					tree.addSensor(sml);
				} catch (SALDocumentException e1) {
					e1.printStackTrace();
				} catch (NotFoundException e2) {
					e2.printStackTrace();
				}
			} else if(sne.getType()==SensorNodeEvent.SENSOR_NODE_REMOVED){
				actionPane.displaySensor(null);
				tree.removeSensor(sne.getSourceID());
			}

		} else if(e instanceof SensorStateEvent){
			SensorStateEvent sse = (SensorStateEvent) e;
			if(sse.getType()==SensorStateEvent.SENSOR_STATE_CONNECTED)
				tree.toggleSensor(sse.getSourceID());
			else if(sse.getType()==SensorStateEvent.SENSOR_STATE_DISCONNECTED)
				tree.toggleSensor(sse.getSourceID());

		} else {
			addLog("Inknown event type");
		}
		
	}

	public static void main(final String[] args) throws InterruptedException, ConfigurationException, RemoteException, NotFoundException {
		
		RMIClientController c = new RMIClientControllerImpl();
		final RMIClientView cl = new RMIClientView(c, args[0]);
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				cl.initGUI();
			}
		});

	}
}
