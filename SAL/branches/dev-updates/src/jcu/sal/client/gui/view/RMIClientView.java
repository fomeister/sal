package jcu.sal.client.gui.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
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
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.events.ProtocolListEvent;
import jcu.sal.events.SensorNodeEvent;
import jcu.sal.events.SensorStateEvent;

public class RMIClientView extends WindowAdapter implements RMIEventHandler{
	
	private String RMI_CLIENT_NAME = "Client View RMI Handler";
	
	private RMIClientController controller;
	private JFrame frame;
	private SensorTree tree;
	private JTextArea log;
	private ActionPanel actionPane;	
	
	//private JFrame frame;
	
	public RMIClientView(RMIClientController c){
		controller = c;
		actionPane = new ActionPanel(this, c);
		tree = new SensorTree(this);
		log = new JTextArea(8,80);
		frame = new JFrame("SAL RMI Client");
	}
	
	public void init() throws NotFoundException, RemoteException{
		initGUI();
		
		/*
		 * RMI stuff		
		 */
		controller.bind(RMI_CLIENT_NAME, this);
		controller.registerEventHandler(RMI_CLIENT_NAME, Constants.PROTOCOL_MANAGER_PRODUCER_ID, this);
		controller.registerEventHandler(RMI_CLIENT_NAME, Constants.SENSOR_MANAGER_PRODUCER_ID, this);
		controller.registerEventHandler(RMI_CLIENT_NAME, Constants.SENSOR_STATE_PRODUCER_ID, this);
	}
	
	private void initGUI(){
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.PAGE_AXIS));

		actionPane.init();
		
		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		leftRight.setLeftComponent(tree);
		leftRight.setRightComponent(actionPane.getPanel());
		
		log.setAutoscrolls(true);
		JScrollPane scroll = new JScrollPane(log);
		
		JSplitPane upDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		upDown.setOneTouchExpandable(true);
		upDown.setTopComponent(leftRight);
		upDown.setBottomComponent(scroll);		
		
		frame.add(upDown);
		
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(this);
		frame.pack();
		frame.setVisible(true);
	}
	
	public RMIClientController getController(){
		return controller;
	}
	
	public void componentSelected(SensorTreeLabel label){
		if(label.getType()==SensorTreeLabel.STRING_TYPE)
			addLog("Root Node '"+label.toString()+"' selected");
		else if(label.getType()==SensorTreeLabel.PROTOCOL_TYPE)
			addLog("Protocol '"+label.toString()+"' selected");
		else if(label.getType()==SensorTreeLabel.SML_TYPE) {
			//addLog("Sensor '"+label.toString()+"' selected");
			actionPane.displaySensor(label.getSMLDescription());
		}
		
	}
	
	public void addLog(String l){
		if(!l.endsWith("\n"))
			l += "\n";
		log.append(l);
	}
	
    /**
     * Catch window closing event so we can free up resources before exiting
     * @param e
     */
	public void windowClosing(WindowEvent e) {
		try {
			controller.unregisterEventHandler(RMI_CLIENT_NAME, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			controller.unregisterEventHandler(RMI_CLIENT_NAME, Constants.SENSOR_MANAGER_PRODUCER_ID);
			controller.unregisterEventHandler(RMI_CLIENT_NAME, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (NotFoundException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		try {
			controller.unbind(RMI_CLIENT_NAME);
		} catch (AccessException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
    	frame.dispose();
    	synchronized (this) {
        	notify();	
		}
	}
	

	@Override
	public void handle(Event e) throws RemoteException {
		addLog("Received event:\n"+e.toString());
		if(e instanceof ProtocolListEvent){
			ProtocolListEvent ple = (ProtocolListEvent) e;
			if(ple.getType()==ProtocolListEvent.PROTOCOL_ADDED){
				//add new protocol to tree
				try {
					ProtocolConfigurations ps = new ProtocolConfigurations(controller.listProtocols());
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
					SMLDescription sml = new SMLDescription(controller.listSensor(sne.getSourceID()));
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

	public static void main(String[] args) throws InterruptedException, ConfigurationException, RemoteException, NotFoundException {
		String agentIP="127.0.0.1";
		
		RMIClientController c = new RMIClientControllerImpl();
		c.connect(args[0], agentIP, "127.0.0.1");
		final RMIClientView cl = new RMIClientView(c);
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				try {
					cl.init();
				} catch (NotFoundException e) {
					e.printStackTrace();			
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		synchronized (cl) {
			cl.wait();
		}
		c.disconnect(args[0]);
		System.exit(0);
	}	
}
