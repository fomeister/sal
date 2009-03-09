package jcu.sal.client.gui.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import jcu.sal.client.gui.ClientController;

public abstract class AbstractClientView implements ClientView{
	
	protected JFrame frame;
	protected SensorTree tree;
	protected JTextArea log;
	protected ActionPanel actionPane;
	protected ClientController controller;
	
	protected AbstractClientView(ClientController c){
		controller = c;
		actionPane = new ActionPanel(this);
		tree = new SensorTree(this);
		log = new JTextArea(8,80);
		frame = new JFrame("SAL Client");
	}
		
	protected void initGUI(){
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.PAGE_AXIS));

		actionPane.init();
		
		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		leftRight.setLeftComponent(tree.getPanel());
		leftRight.setRightComponent(actionPane.getPanel());
		
		log.setEditable(false);
		JScrollPane scroll = new JScrollPane(log);
		
		JSplitPane upDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		upDown.setOneTouchExpandable(true);
		upDown.setTopComponent(leftRight);
		upDown.setBottomComponent(scroll);		
		
		frame.getContentPane().add(upDown);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			   /**
		     * Catch window closing event so we can free up resources before exiting
		     * @param e
		     */
			public void windowClosing(WindowEvent e) {
		    	frame.dispose();
		    	release();
				System.exit(0);
			}
		});
		
		frame.pack();
		frame.setVisible(true);
	}
	
	@Override
	public final ClientController getController(){
		return controller;
	}
	
	/**
	 * This method is called by {@link SensorTree} when a component (sensor, protocol, agent)
	 * has been selected
	 * @param c the {@link Context} of the selected object
	 */
	@Override
	public void componentSelected(Context c){
		if(c.getType()==Context.ROOT_NODE)
			addLog("Root Node '"+c.toString()+"' selected");
		else if(c.getType()==Context.AGENT_TYPE)
			addLog("Agent Node '"+c.toString()+"' selected");
		else if(c.getType()==Context.PROTOCOL_TYPE)
			addLog("Protocol '"+c.toString()+"' selected");
		else if(c.getType()==Context.SENSOR_TYPE)
			actionPane.displaySensor(c);
		else
			addLog("Unknown element type");
		
		actionPane.getPanel().revalidate();
		actionPane.getPanel().repaint();
		
	}
	
	
	@Override
	public void addLog(String l){
		if(!l.endsWith("\n"))
			l += "\n";
		log.append(l);
		log.setCaretPosition(log.getDocument().getLength());

	}
		
	/**
	 * This method is called after the frame has been disposed, when the application exits.
	 * Can be overriden be subclasses
	 */
	public void release(){}

}
