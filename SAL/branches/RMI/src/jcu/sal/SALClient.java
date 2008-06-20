package jcu.sal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.ClosedChannelException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jcu.sal.agent.SALAgentImpl;
import jcu.sal.common.CommandFactory;
import jcu.sal.common.Constants;
import jcu.sal.common.Response;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.EventHandler;
import jcu.sal.utils.XMLhelper;

import org.w3c.dom.Document;

public class SALClient implements EventHandler, StreamCallback{
	private static final long serialVersionUID = -8376295971546676596L;
	
	public static class JpgMini {
		private JLabel l;
		private JFrame f;
		private long start = 0;
		private int n;
		private String sid;
		
	    public JpgMini(String sid){
	        f = new JFrame();
	        this.sid = sid;
	        l = new JLabel();
	        f.getContentPane().add(l);
	        f.setSize(640,480);
	        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    }
	    
	    public void setImage(byte[] b) {
	    	//l.setIcon(new ImageIcon(b));
	    	if(start==0)
	    		start = System.currentTimeMillis();
	    	else {
	    		if(System.currentTimeMillis()<start+10000) 
	    			n++;
	    		else {
	    			System.out.println("SID: "+sid+" - FPS: "+ ( (float) (1000*n/(System.currentTimeMillis()-start))  ));
	    			start = System.currentTimeMillis();
	    			n = 0;
	    		}
	    			
	    	}
	    }
	    
	    public void close() {
	    	f.dispose();
	    }
	    
	    public void setVisible(){
	    	f.setVisible(true);
	    }
	}
	
	private Map<String, JpgMini> viewers;
	private SALAgent agent;
	
	public SALClient() {
		viewers = new Hashtable<String, JpgMini>();
	}
	
	public void start(String pc, String sc) throws ConfigurationException{
		agent = new SALAgentImpl();
		try {
			agent.registerEventHandler(this, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(this, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(this, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (ConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		agent.start(pc, sc);
	}
	
	public void run(){
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		int sid=0, j;
		String str, str2;
		Document d;
		Command c = null;
		Response res;
		CommandFactory cf;
		ArgumentType t;
		StringBuilder sb = new StringBuilder();	

		while(sid!=-1) {
			System.out.println("Enter either :\n\ta sensor id to send a command\n\t-1 to quit\n\t-2 to see a list of active sensors");
			System.out.println("\t-3 to add a new protocol\n\t-4 to remove a protocol\n\t-5 to add a new sensor\n\t-6 to remove a sensor");
			System.out.println("\t-7 to list all sensors");
			try {
				sid=Integer.parseInt(b.readLine());
				if(sid>=0) {
					System.out.println("\n\nHere is the CML document for this sensor:");
					d = XMLhelper.createDocument(agent.getCML(String.valueOf(sid)));
					System.out.println(XMLhelper.toString(d));
					System.out.println("Enter a command id:");
					j=Integer.parseInt(b.readLine());
					
					cf = new CommandFactory(d, j);
					boolean argOK=false, argsDone=false;
					while(!argsDone) {
						Iterator<String> e = cf.listMissingArgNames().iterator();
						while(e.hasNext()){
							str = e.next();
							t = cf.getArgType(str);
							if(!t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
								while(!argOK) {
									System.out.println("Enter value of type '"+t.getArgType()+"' for argument '"+str+"'");
									str2 = b.readLine();
									try {cf.addArgumentValue(str, str2); argOK = true;}
									catch (ConfigurationException e1) {System.out.println("Wrong value"); argOK=false;}
								}
							} else {
								cf.addArgumentCallback(str, this);
								JpgMini jpg = new JpgMini(String.valueOf(sid));
								jpg.setVisible();
								viewers.put(String.valueOf(sid), jpg);	
							}
						}
						try {c = cf.getCommand(); argsDone=true;}
						catch (ConfigurationException e1) {System.out.println("Values missing"); argsDone=false;}
					}
					
					res = agent.execute(c, String.valueOf(sid));
					//new FileOutputStream("file"+(fn++),false).write(ResponseParser.toByteArray(res));
					String xpath=CMLConstants.XPATH_CMD_DESC+"[@"+CMLConstants.CID_ATTRIBUTE+"=\""+j+"\"]/"+CMLConstants.RETURN_TYPE_TAG;
					try {
						String type = XMLhelper.getAttributeFromName(xpath, CMLConstants.TYPE_ATTRIBUTE, d);
						if(type.equals(CMLConstants.RET_TYPE_BYTE_ARRAY)) {
							JpgMini v = new JpgMini(String.valueOf(sid));
							v.setImage(res.getBytes());
							v.setVisible();
						} else {
							System.out.println("Command returned: " + res.getString());
						}
					} catch (Exception e){
						System.out.println("Cant find the return type");
						System.out.println("XPATH: "+xpath);
						e.printStackTrace();
					}
					
										
				} else if(sid==-2)
					System.out.println(agent.listActiveSensors());
				else if(sid==-3) {
					System.out.println("Enter the XML doc for the new procotol:");
					sb.delete(0, sb.length());
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					System.out.println("Load associated sensors from config file ? (yes-no)");
					str2=b.readLine();
					agent.addProtocol(sb.toString(), (str2.equals("yes"))?true:false);
					sb.delete(0, sb.length());
				}else if(sid==-4) {
					System.out.println("Enter the ID of the protocol to be removed:");
					str=b.readLine();
					System.out.println("Remove associated sensors from config file ? (yes-no)");
					str2=b.readLine();
					agent.removeProtocol(str, (str2.equals("yes"))?true:false);
				} else if(sid==-5) {
					System.out.println("Enter the XML doc for the new sensor:");
					sb.delete(0, sb.length());
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					agent.addSensor(sb.toString());
					sb.delete(0, sb.length());
				} else if(sid==-6) {
					System.out.println("Enter the ID of the Sensor to be removed:");
					str=b.readLine();
					agent.removeSensor(str);
				} else if(sid==-7)
					System.out.println(agent.listSensors());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void stop(){
		agent.stop();
		try {
			agent.unregisterEventHandler(this, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(this, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(this, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Main exiting");
	}
	
	
	public static void main(String [] args){
		SALClient c = new SALClient();
		try {
			c.start(args[0], args[1]);
			c.run();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			c.stop();
		}
	}

	public String getName() {
		return "SAL user";
	}

	public void handle(Event e) {
		System.out.println("Received "+e.toString());
	}

	public void collect(Response r) {
		final Response rr = r;

			new Thread ( new Runnable() {
				public void run() {
					try {
						viewers.get(rr.getSID()).setImage(rr.getBytes());
					} catch (ConfigurationException e) {
						System.out.println("Stream from sensor "+rr.getSID()+" returned an error");
						viewers.remove(rr.getSID()).close();
					} catch (ClosedChannelException e) {
						System.out.println("Stream from sensor "+rr.getSID()+" completed");
						viewers.remove(rr.getSID()).close();
					}
				}
			}
			).start();
		
	}
}
