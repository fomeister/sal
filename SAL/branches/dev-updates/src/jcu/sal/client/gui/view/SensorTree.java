package jcu.sal.client.gui.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import jcu.sal.client.gui.ClientController;
import jcu.sal.client.gui.RMIClientController;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;

public class SensorTree implements TreeSelectionListener{

	private static final long serialVersionUID = -1407856342401386714L;
	private ClientView view;
	private ClientController controller;
	
	private JTree tree;
	private JPanel mainPane;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode rootNode;
	private Hashtable<String,ProtocolConfiguration> protocols;
	private Hashtable<String,SMLDescription> smlDescriptions;
	
	
	public SensorTree(ClientView v){

		view = v;
		controller = v.getController();
		mainPane = new JPanel(new GridLayout(1,0));
		rootNode = new DefaultMutableTreeNode(new SensorTreeLabel("SAL Client"));
        treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		protocols = new Hashtable<String,ProtocolConfiguration>();
		smlDescriptions = new Hashtable<String,SMLDescription>();
		
		
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(this);
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setCellRenderer(new MyRenderer());
		mainPane.add(new JScrollPane(tree));
		mainPane.setMinimumSize(new Dimension(200,400));
	}
	
	public JPanel getPanel(){
		return mainPane;
	}
	
	/**
	 * This method clears the tree, calls {@link RMIClientController#listProtocols()} and
	 * {@link RMIClientController#listSensors()} to re-populate the tree. this method is
	 * computationally expensive. 
	 */
	public void updateTree(){
		SMLDescriptions smls = null;
		ProtocolConfigurations pcml = null;
		MutableTreeNode parent;
		
		try {
			smls = new SMLDescriptions(controller.listSensors());
			pcml = new ProtocolConfigurations(controller.listProtocols());
		} catch (SALDocumentException e) {
			view.addLog("Error retrieving the XML sensor /protocol list\n"+e.getMessage());
			e.printStackTrace();
			return;
		}
		
		//Empty tree
		rootNode.removeAllChildren();
		
		//add protocols & sensors
		for(ProtocolConfiguration p:pcml.getConfigurations()){
			//add protocol node
			parent = addNode(p, rootNode);
			//for all sensor under this protocol ...
			for(SMLDescription s : smls.getDescriptions(p.getID()))
				//add them
				addNode(s,parent);
		}
	}

	/**
	 * This method must be called when a new protocol must be added to this tree
	 * @param p the {@link ProtocolConfiguration} of the protocol to be added
	 */
	public void addProtocol(ProtocolConfiguration p){
		synchronized(rootNode){
			addNode(p, rootNode);
			protocols.put(p.getID(),p);
		}
	}
	
	/**
	 * This method must be called when a new protocol must be removed from this tree
	 * @param p the protocol ID of the protocol to be removed
	 */
	public void removeProtocol(String pid){
		synchronized(rootNode){
			DefaultMutableTreeNode n = findProtocolNode(pid);
			if(n!=null ){
				removeNode(n);
				protocols.remove(pid);
			}else
				System.out.println("Cant find protocol "+pid);
		}
	}
	
	/**
	 * This method must be called when a new sensor must be added to this tree
	 * @param s the {@link SMLDescription} of the sensor to be added
	 */
	public void addSensor(SMLDescription s){
		synchronized(rootNode){
			DefaultMutableTreeNode n = findProtocolNode(s.getProtocolName());
			if(n!=null ){
				addNode(s, n);
				smlDescriptions.put(s.getID(),s);
			}else
				System.out.println("Cant find parent protocol "+s.getProtocolName());
		}
	}
	
	/**
	 * This method must be called when a new sensor must be removed from this tree
	 * @param s the sensor ID of the sensor to be removed
	 */
	public void removeSensor(String sid){
		synchronized(rootNode){
			DefaultMutableTreeNode n = findSensorNode(sid);
			if(n!=null ){
				removeNode(n);
				smlDescriptions.remove(sid);
			}else
				view.addLog("Cant find sensor node "+sid);
		}
	}
	
	public void toggleSensor(String sid){
		synchronized(rootNode){
			DefaultMutableTreeNode n = findSensorNode(sid);
			if(n!=null )
				toggleSensor(n);
			else
				view.addLog("Cant find sensor node "+sid);
		}
	}
	
	/**
	 * This method returns the {@link SensorTreeLabel} of the currently selected element.
	 * @return the {@link SensorTreeLabel} of the currently selected element.
	 */
	public SensorTreeLabel getSelectedLabel(){
		if(tree.getLastSelectedPathComponent()==null)
			return null;
		else
			return (SensorTreeLabel) ((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()).getUserObject();
	}
	
	public SMLDescription getSMLDescription(String sid){
		synchronized(rootNode){
			return smlDescriptions.get(sid);
		}
	}
	
	public ProtocolConfiguration getProtocolConfiguration(String pid){
		synchronized(rootNode){
			return protocols.get(pid);
		}
	}
	
	/**
	 * This method is called whenever a node in the tree is selected
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// Get the selected element and tell the RMIClientView to update the display accordingly
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if(node==null)
			return;
		
		view.componentSelected((SensorTreeLabel) node.getUserObject());		
	}

	static class MyRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = -4758932006597272509L;
		private static Font enabledFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
		private static Font disabledFont = new Font(Font.SANS_SERIF, Font.ITALIC, 11);

		public MyRenderer() {
        	super();
        }

        public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
            SensorTreeLabel l = (SensorTreeLabel) n.getUserObject();
            if(l.isEnabled()) {
                setToolTipText("Enabled");
                setFont(enabledFont);
            } else {
            	setToolTipText("Disabled");
            	setFont(disabledFont);
            }
            
            
            return this;
        }
    }
	
	/*
	 * 
	 * P R I V A T E   M E T H O D S
	 * 
	 */
	
	/**
	 * This method returns the node in this tree representing a protocol matching
	 * the given protocol ID. 
	 * @param pid the protocol ID of the protocol node to be looked up in this tree.
	 * @return the node representing the protocol, or <code>null</code>
	 * if no protocol matches the given ID.
	 */
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode findProtocolNode(String pid){
		ProtocolConfiguration p;
		DefaultMutableTreeNode n;
		Enumeration<DefaultMutableTreeNode> e = rootNode.children();
		while(e.hasMoreElements()){
			n = e.nextElement();
			p = ((SensorTreeLabel) n.getUserObject()).getProtocolConfiguration();
			if(p.getID().equals(pid)){
				return n;
			}
		}
		
		return null;
	}
	
	/**
	 * This method returns the node in this tree representing a sensor matching
	 * the given sensor ID. 
	 * @param sid the sensor ID of the sensor node to be looked up in this tree.
	 * @return the node representing the sensor, or <code>null</code>
	 * if no sensor matches the given ID.
	 */
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode findSensorNode(String sid){
		DefaultMutableTreeNode pn, sn;
		SMLDescription s;
		Enumeration<DefaultMutableTreeNode> sensors, proto = rootNode.children();
		while(proto.hasMoreElements()){
			pn = proto.nextElement();
			sensors = pn.children();
			while(sensors.hasMoreElements()){
				sn = sensors.nextElement();
				s = ((SensorTreeLabel) sn.getUserObject()).getSMLDescription();
				if(s.getID().equals(sid))
					return sn;				
			}
		}
		return null;
	}	

	/**
	 * This method adds a node represented by <code>o</code> to the parent
	 * node <code>parent</code>.
	 * @param o the node to be added {@link SMLDescription} or {@link ProtocolConfiguration} 
	 * @param parent the parent node
	 * @return the newly added node
	 */
	private MutableTreeNode addNode(Object o, final MutableTreeNode parent){
		final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new SensorTreeLabel(o));
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				treeModel.insertNodeInto(n, parent, parent.getChildCount());				
			}
		});

		return n;
	}
	
	/**
	 * This method adds a node represented by <code>o</code> to the parent
	 * node <code>parent</code>.
	 * @param o the node to be added {@link SMLDescription} or {@link ProtocolConfiguration} 
	 * @param parent the parent node
	 * @return the newly added node
	 */
	private void removeNode(final MutableTreeNode n){
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				treeModel.removeNodeFromParent(n);
			}
		});
	}
	
	/**
	 * This method toggles the state of a sensor node in this tree
	 * @param n the sensor node
	 */
	private void toggleSensor(final DefaultMutableTreeNode n){
		SensorTreeLabel stl = (SensorTreeLabel) n.getUserObject();
		stl.toggleState();
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				treeModel.reload(n);
			}
		});
	}
//
//
//	
//	public static void main(String[] args) throws InterruptedException, ConfigurationException, RemoteException, NotFoundException {
//		JFrame f = new JFrame("test");
//		RMIClientView cl = new RMIClientView(new RMIClientControllerImpl());
//		cl.getController().connect(args[0], "127.0.0.1", "127.0.0.1");
//		SensorTree t = new SensorTree(cl);
//		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(),BoxLayout.PAGE_AXIS));
//
//		f.add(t);
//		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		f.pack();
//		f.setVisible(true);
//		t.updateTree();
//	}
}
