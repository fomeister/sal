package jcu.sal.client.gui.view;

import java.util.Hashtable;

import jcu.sal.client.gui.ClientController;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;

public interface ClientView {
	
	/**
	 * This method returns the {@link ClientController} used by this view
	 * @return the {@link ClientController} used by this view
	 */
	public ClientController getController();
	
	/**
	 * This method sends a command, given the arguments' values, its {@link CMLDescription}
	 * and the sensor ID
	 * @param values the values for each argument of the command, except {@link CMLConstants#ARG_TYPE_CALLBACK} 
	 * callback arguments.
	 * @param cml the {@link CMLDescription} of the command to be sent
	 * @param sid the sensor ID where the command should be sent
	 */
	public void sendCommand(Hashtable<String,String> values, CMLDescription cml, String sid);
	
	/**
	 * This method is called by {@link SensorTree} when a component (sensor, protocol, agent)
	 * to notify the view that it should update other parts.
	 * @param label the {@link SensorTreeLabel} of the selected object
	 */
	public void componentSelected(SensorTreeLabel label);
	
	/**
	 * This method adds an entry to the log area
	 * @param l the log entry
	 */
	public void addLog(String l);
}
