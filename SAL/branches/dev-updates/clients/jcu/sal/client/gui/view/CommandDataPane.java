package jcu.sal.client.gui.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLDescription;

public class CommandDataPane implements ActionListener{
	
	private static final long serialVersionUID = -3929136251479602868L;
	private ClientView view;
	private JPanel mainPane, dataPane;
	private JButton send;
	private JLabel title;
	private Hashtable<String, JTextField> argValues;
	private CMLDescription cml;
	private Context current;
	
	public CommandDataPane(ClientView v){
		view = v;
		dataPane = new JPanel(new SpringLayout());
		mainPane = new JPanel();
		send = new JButton("Send command");
		title = new JLabel("Command detail");
		argValues = new Hashtable<String, JTextField>();
	}
	
	public void init(){
		send.addActionListener(this);
		
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(title);
		dataPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(dataPane);
		send.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(send);
	}
	
	public JPanel getPanel(){
		return mainPane;
	}
	
	/**
	 * This method is called whenever this panel should display
	 * command data for the new command, given in argument 
	 * @param c the {@link CMLDescription} object of the new command.
	 * @param s the sensor ID where the command belongs
	 * If <code>null</code> the panel is simply cleared.
	 */
	public void displayCommandData(CMLDescription c, Context ctx){
		dataPane.removeAll();
		dataPane.repaint();

		if(c!=null){
			int i;
			
			addDescription(c.getShortDesc());
			addRetType(c.getReturnType().toString());
			i = addArg(c.getArgNames(), c.getArgTypes());
			SpringLayoutHelper.makeCompactGrid(dataPane,
					(i+2), 2, //rows, cols
	                6, 6,        //initX, initY
	                6, 6);       //xPad, yPad
			send.setEnabled(true);
			mainPane.validate();
			mainPane.repaint();
		} else
			send.setEnabled(false);
		cml = c;
		current = ctx;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(send)){
			final Hashtable<String,String> values = new Hashtable<String,String>();
			//put entered values in a table
			for(String name: argValues.keySet())
				values.put(name, argValues.get(name).getText());
						
			//send table to view
			view.sendCommand(values, cml, current);
			
		}
	}
	
	private void addDescription(String d){
		dataPane.add(new JLabel("Description:"));
		JTextArea t = new JTextArea(d);
		t.setEditable(false);
		JScrollPane s = new JScrollPane(t);
		s.setPreferredSize(new Dimension(200,75));
		s.setMaximumSize(new Dimension(800,75));
		dataPane.add(s);
	}
	
	private void addRetType(String r){
		dataPane.add(new JLabel("Return Type:"));
		dataPane.add(new JLabel(r));
	}
	
	private int addArg(List<String> names, List<ArgumentType> types){
		int i, nb=0;
		argValues.clear();
		for(i=0; i<names.size(); i++){

			
			JLabel l = new JLabel(names.get(i));
			dataPane.add(l);
			JTextField t = new JTextField(25);
			argValues.put(names.get(i), t);
			t.setPreferredSize(new Dimension(200,30));
			t.setMaximumSize(new Dimension(800,30));
			l.setLabelFor(t);
			dataPane.add(t);
			nb++;
		}
		return nb;
	}


}
