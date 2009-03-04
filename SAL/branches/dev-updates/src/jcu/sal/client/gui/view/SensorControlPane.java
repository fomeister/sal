package jcu.sal.client.gui.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jcu.sal.client.gui.RMIClientController;
import jcu.sal.common.sml.SMLDescription;

public class SensorControlPane{

	private static final long serialVersionUID = -3678056141437194190L;
	private RMIClientView view;
	private RMIClientController controller;
	private CommandListPane cmdPane;
	private CommandDataPane cmdDataPane;
	private SMLDescription current;
	private JPanel pane;
	
	public SensorControlPane(RMIClientView v, RMIClientController c){
		super();
		pane = new JPanel();
		view = v;
		controller = c;
		cmdDataPane = new CommandDataPane(view, controller);
		cmdPane = new CommandListPane(view, controller, cmdDataPane);

	}
	
	public void init(){
		pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
		
		cmdPane.init();
//		cmdPane.getPanel().setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Color.red),
//                cmdPane.getPanel().getBorder()));

		cmdDataPane.init();
//		cmdDataPane.getPanel().setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Color.red),
//                cmdDataPane.getPanel().getBorder()));

		cmdPane.getPanel().setAlignmentX(Component.RIGHT_ALIGNMENT);
		pane.add(cmdPane.getPanel());
		pane.add(Box.createHorizontalStrut(20));
		JScrollPane listScroller = new JScrollPane(cmdDataPane.getPanel());
		//listScroller.setPreferredSize(new Dimension(250, 80));
		pane.add(listScroller);
	}
	
	public JPanel getPanel(){
		return pane;
	}
	
	/**
	 * This method updates this panel with the information relative
	 * to the given sensor
	 * @param sml the {@link SMLDescription} object of the sensor
	 * whose information is to be displayed.
	 */
	public void displaySensor(SMLDescription sml){
		if(sml!=current){
			if(sml==null) 
				cmdPane.displayCommand(null);
			else
				cmdPane.displayCommand(sml.getID());
			current = sml;
		}
	}



}
