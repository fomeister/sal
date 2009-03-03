package jcu.sal.client.gui.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jcu.sal.client.gui.RMIClientController;
import jcu.sal.client.gui.RMIClientControllerImpl;
import jcu.sal.common.Constants;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.RMIEventHandler;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.events.ProtocolListEvent;
import jcu.sal.events.SensorNodeEvent;
import jcu.sal.events.SensorStateEvent;

public class RMIClientView extends WindowAdapter implements RMIEventHandler{
	
	private RMIClientController controller;
	private JFrame frame;
	private SensorTree tree;
	
	
	//private JFrame frame;
	
	public RMIClientView(RMIClientController c) throws NotFoundException, RemoteException{
		controller = c;
		
		initGUI();
		
		c.registerEventHandler("Client View RMI Handler", Constants.PROTOCOL_MANAGER_PRODUCER_ID, this);
		c.registerEventHandler("Client View RMI Handler", Constants.SENSOR_MANAGER_PRODUCER_ID, this);
		c.registerEventHandler("Client View RMI Handler", Constants.SENSOR_STATE_PRODUCER_ID, this);
	}
	
	public void initGUI(){
		frame = new JFrame("SAL RMI Client");
		tree = new SensorTree(this);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.PAGE_AXIS));
		frame.add(tree);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);				
	}
	
	public RMIClientController getController(){
		return controller;
	}
	
	public void componentSelected(SensorTreeLabel sensor){
		addLog("Sensor "+sensor.toString()+" selected");
	}
	
	public void addLog(String l){
		System.out.println("Adding to the log: "+l);
	}
	
    /**
     * Catch window closing event so we can free up resources before exiting
     * @param e
     */
	public void windowClosing(WindowEvent e) {
		try {
			controller.unregisterEventHandler("Client View RMI Handler", Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			controller.unregisterEventHandler("Client View RMI Handler", Constants.SENSOR_MANAGER_PRODUCER_ID);
			controller.unregisterEventHandler("Client View RMI Handler", Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (NotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	frame.dispose();		
	}
	

	@Override
	public void handle(Event e) throws RemoteException {
		addLog("Received event:\n"+e.toString());
		if(e instanceof ProtocolListEvent){
			ProtocolListEvent ple = (ProtocolListEvent) e;
			if(ple.getType()==ProtocolListEvent.PROTOCOL_ADDED){
				ProtocolConfigurations ps;
				try {
					ps = new ProtocolConfigurations(controller.listProtocols());
					tree.addProtocol(ps.getDescription(ple.getSourceID()));
				} catch (SALDocumentException e1) {
					e1.printStackTrace();
				} catch (NotFoundException e2) {
					e2.printStackTrace();
				}
				
			} 
		} else if (e instanceof SensorNodeEvent){
			
		} else if(e instanceof SensorStateEvent){
			
		} else {
			addLog("Inknown event type");
		}
		
	}

	public static void main(String[] args) throws InterruptedException, ConfigurationException, RemoteException, NotFoundException {
		String agentIP="127.0.0.1";
		
		RMIClientController c = new RMIClientControllerImpl();
		c.connect(args[0], agentIP, "127.0.0.1");
		RMIClientView cl = new RMIClientView(c);
		
		

	}	
}
