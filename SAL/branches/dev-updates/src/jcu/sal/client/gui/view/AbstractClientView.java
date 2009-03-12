package jcu.sal.client.gui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import jcu.sal.client.gui.ClientController;

public abstract class AbstractClientView implements ClientView, ActionListener{
	
	private static String ActionAddRMIAgent = "addRMI";
	private static String ActionRemoveRMIAgent = "removeRMI";
	private static String ActionQuit = "quit";
	
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
		
		frame.setJMenuBar(createMenu());
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
	
	protected JMenuBar createMenu(){
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("SAL Client");
		bar.add(menu);
		
		JMenuItem item = new JMenuItem("Add RMI Agent");
		item.setActionCommand(ActionAddRMIAgent);
		item.setMnemonic(KeyEvent.VK_A);
		item.addActionListener(this);
		menu.add(item);
		
		item = new JMenuItem("Disconnect RMI Agent");
		item.setActionCommand(ActionRemoveRMIAgent);
		item.setMnemonic(KeyEvent.VK_D);
		item.addActionListener(this);
		menu.add(item);
		
		menu.addSeparator();
		
		item = new JMenuItem("Quit");
		item.setActionCommand(ActionQuit);
		item.setMnemonic(KeyEvent.VK_Q);
		item.addActionListener(this);
		menu.add(item);		
		
		return bar;
	}
	
	public void addRmiAgent(){
//		Vector<String> v;
//		try {
//			v = new Vector<String>();
//			Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces();
//			Enumeration<InetAddress> add;
//			while(i.hasMoreElements()){
//				add = i.nextElement().getInetAddresses();
//				while(add.hasMoreElements())
//					v.add(add.nextElement().getHostAddress());
//			}
//				
//		} catch (SocketException e1) {
//			JOptionPane.showMessageDialog(frame,
//				    "Unable to list the network interfaces.",
//				    "Network error",
//				    JOptionPane.ERROR_MESSAGE);
//			return;
//		}
//		String[] ifs = new String[v.size()];		
//		v.copyInto(ifs);
		
		String agentIP = (String)JOptionPane.showInputDialog(
                frame,
                "Enter the IP address of the RMI SAL agent:\n",
                "SAL Agent detail",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "127.0.0.1");
		if(agentIP==null)
			return;
		
		String ourIP = (String)JOptionPane.showInputDialog(
                frame,
                "Enter the IP address of the RMI SAL agent:\n",
                "SAL Agent detail",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "127.0.0.1");
		
		if(ourIP!=null){
			
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if(ActionAddRMIAgent.equals(e.getActionCommand())){
			addRmiAgent();
		} else if(ActionRemoveRMIAgent.equals(e.getActionCommand())){
			//remove rmi agent
		} else if(ActionQuit.equals(e.getActionCommand())){
			//quit
			frame.dispose();
		} 
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
