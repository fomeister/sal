package jcu.sal.Agent;

import java.io.NotActiveException;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.Components.Command;

public interface SALAgentInterface {
	/**
	 * Initialises the SAL agent. Parses the platform & sensor configuration files, and creates the objects 
	 * @param pc the platform config file
	 * @param sc the sensor config file
	 * @throws ConfigurationException if the files can not be parsed, or the configuration is incorrect
	 */
	public void init(String pc, String sc) throws ConfigurationException;
	
	/**
	 * TODO change this into a method that returns the sensor XML docs instead
	 * This method displays on the console the current state of all sensors.
	 */
	public void dumpSensors();
	
	/**
	 * Execute the command c on sensor sid
	 * @param c the command
	 * @param sid the sensor
	 * @return the result
	 */
	public String execute(Command c, int sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException;
	
	/**
	 * Returns the CML document for a given sensor
	 * @param sid the sensor id
	 * @return the CML doc
	 * @throws ConfigurationException if the CML doc cant be found
	 */
	public String getCML(int sid) throws ConfigurationException, NotActiveException;
	
	/**
	 * Stops the SAL agent
	 *
	 */
	public void stop();
}

