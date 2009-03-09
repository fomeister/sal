package jcu.sal.client.gui.view;

import java.awt.Component;
import java.awt.Container;
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
import javax.swing.Spring;
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
			
			addDescription(c.getDesc());
			addRetType(c.getReturnType().toString());
			i = addArg(c.getArgNames(), c.getArgTypes());
			makeCompactGrid(dataPane,
					(i+2), 2, //rows, cols
	                6, 6,        //initX, initY
	                6, 6);       //xPad, yPad
			mainPane.validate();
			mainPane.repaint();
		}
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
			
			//skip callbacks
			if(types.get(i).equals(ArgumentType.CallbackArgument))
				continue;
			
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


	/*
	 *
	 * P R I V A T E   M E T H O D S
	 * 
	 */
	
	
	/*
	 * Taken from the Java Tutorials, Swing trail
	 * at http://java.sun.com/docs/books/tutorial/uiswing/examples/layout/SpringGridProject/src/layout/SpringUtilities.java
	 */
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    private void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                                   getConstraintsForCell(r, c, parent, cols).
                                       getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                                    getConstraintsForCell(r, c, parent, cols).
                                        getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
    
    /* Used by makeCompactGrid. */
    private SpringLayout.Constraints getConstraintsForCell(
                                                int row, int col,
                                                Container parent,
                                                int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }
}
