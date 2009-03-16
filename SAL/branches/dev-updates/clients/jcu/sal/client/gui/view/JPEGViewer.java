package jcu.sal.client.gui.view;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class JPEGViewer implements VideoViewer {
	
	/**
	 * How often do we refresh teh frame rate label (in milliseconds)
	 */
	private static int FPS_REFRESH = 1 * 1000;
	
	private JFrame frame;
	private JLabel image;
	private String title;
	private long start = 0;
	private int n;
	
	/**
	 * This method builds a JPEG viewer 
	 * @param n the title of the JFrame
	 */
	public JPEGViewer(String n){
		title = n;
		frame = new JFrame(n);
		image = new JLabel();
		frame.getContentPane().add(image);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void setImage(byte[] b) {
		image.setIcon(new ImageIcon(b));
		if(start==0){
    		start = System.currentTimeMillis();
    		frame.setSize(image.getIcon().getIconWidth(), image.getIcon().getIconHeight());
		}else if(System.currentTimeMillis()>start+FPS_REFRESH) {
			frame.setTitle(title + " - FPS: "+ (((float) 1000*n/(System.currentTimeMillis()-start))  ));
			start = System.currentTimeMillis();
			n = 0;
		} else
			n++;
	}
	
    public void close() {
    	SwingUtilities.invokeLater(new Runnable(){
    		public void run(){
    			frame.dispose();    			
    		}
    	});
    }
}
