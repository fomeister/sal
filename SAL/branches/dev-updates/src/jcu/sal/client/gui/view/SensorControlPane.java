package jcu.sal.client.gui.view;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SensorControlPane{

	private static final long serialVersionUID = -3678056141437194190L;
	private ClientView view;
	private CommandListPane cmdPane;
	private CommandDataPane cmdDataPane;
	private Context current;
	private JPanel pane;
	
	public SensorControlPane(ClientView v){
		super();
		pane = new JPanel();
		view = v;
		cmdDataPane = new CommandDataPane(view);
		cmdPane = new CommandListPane(view, cmdDataPane);

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
	 * @param c the {@link Context} object of the sensor
	 * whose information is to be displayed. If <code>null</code> the panel data is reset.
	 */
	public void displaySensor(Context c){
		if(c==null) 
			cmdPane.displayCommand(null);
		else if(c!=current){
			current = c;
			cmdPane.displayCommand(current);
		}
	}



}
