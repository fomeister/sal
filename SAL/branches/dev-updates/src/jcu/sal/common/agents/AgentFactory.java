package jcu.sal.common.agents;

import jcu.sal.agent.SALAgentImpl;
import jcu.sal.common.exceptions.ConfigurationException;

public class AgentFactory {
	
	private static AgentFactory factory = new AgentFactory();
	
	private AgentFactory(){}
	
	public static AgentFactory getFactory(){
		return factory;
	}
	
	/**
	 * This method creates a new local SAL agent and returns a reference to it.
	 * @param pc the file to be used as platform configuration file
	 * @param sc the file to be used as sensor configuration file
	 * @return a reference to a fully initialised local SAL agent
	 * @throws ConfigurationException if the given files cannot be written to, parsed,
	 * or if their content is not valid.
	 */
	public SALAgent createLocalAgent(String pc, String sc) throws ConfigurationException{
		SALAgentImpl s = new SALAgentImpl();
		s.start(pc, sc);
		return s;
	}
	
	/**
	 * This method releases a local SAL agent previously instantiated with {@link #createLocalAgent(String, String)}.
	 * @param s the local SAL agent to be released
	 * @throws ClassCastException if the given SAL agent is not a local SAL agent.
	 */
	public void releaseLocalAgent(SALAgent s){
		SALAgentImpl agent = (SALAgentImpl) s;
		agent.stop();
	}

}
