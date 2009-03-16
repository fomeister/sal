package jcu.sal.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jcu.sal.agent.LocalAgentImpl;
import jcu.sal.common.CommandFactory;
import jcu.sal.common.Constants;
import jcu.sal.common.Response;
import jcu.sal.common.XMLhelper;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.events.ClientEventHandler;
import jcu.sal.common.events.Event;
import jcu.sal.common.exceptions.ArgumentNotFoundException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;

public class SALClient implements ClientEventHandler, StreamCallback{
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
	        setVisible();
	    }
	    
	    public void setImage(byte[] b) {
	    	l.setIcon(new ImageIcon(b));
	    	if(System.currentTimeMillis()>start+10000) {
    			System.out.println("SID: "+sid+" - FPS: "+ (((float) 1000*n/(System.currentTimeMillis()-start))  ));
    			start = System.currentTimeMillis();
    			n = 0;
    		} else
    			n++;
	    }
	    
	    public void close() {
	    	f.dispose();
	    }
	    
	    public void setVisible(){
	    	f.setVisible(true);
	    }
	}
	
	private Map<String, JpgMini> viewers;
	private LocalAgentImpl agent;
	private BufferedReader b; 
	
	public SALClient() {
		viewers = new Hashtable<String, JpgMini>();
	}
	
	public void start(String pc, String sc) throws ConfigurationException{
		agent = new LocalAgentImpl();
		agent.start(pc, sc);
		try {
			agent.registerEventHandler(this, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(this, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(this, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (NotFoundException e2) {
			e2.printStackTrace();
		}
		
		b = new BufferedReader(new InputStreamReader(System.in));
	}
	

	public int getAction(){
		int sid=-1;;
		boolean ok=false;
		System.out.println("Enter either :\n\ta sensor id to send a command\n\t-1 to quit\n\t-2 to see a list of active sensors (XML)");
		System.out.println("\t-3 to see a list of active sensors (shorter, human readable listing)");
		System.out.println("\t-4 to add a new protocol\n\t-5 to remove a protocol\n\t-6 to list all protocols");
		System.out.println("\t-7 to add a new sensor\n\t-8 to remove a sensor");
		System.out.println("\t-9 to list all sensors (XML)\n\t-10 to list all sensors(shorter, human readable listing)");
		while(!ok)
			try {
				sid = Integer.parseInt(b.readLine());
				ok = true;
			} catch (Exception e) {}

		return sid;
	}
	
	public void doActionSensor(int sid) throws Exception{
		String str2;
		CommandFactory cf;
		Command c = null;
		Response res;
		ArgumentType t;
		CMLDescriptions cmls;
		int j;
		
		cmls = new CMLDescriptions(agent.getCML(String.valueOf(sid)));
		System.out.println("\n\nHere is the CML document for this sensor:");
		System.out.println(cmls.getXMLString());
		System.out.println("Print human-readable form ?(Y/n)");
		if(!b.readLine().equals("n")){
			for(CMLDescription tmp: cmls.getDescriptions()){
				System.out.print("CID: "+tmp.getCID());
				System.out.println(" - "+tmp.getDesc());
			}
		}
		System.out.println("Enter a command id:");
		j=Integer.parseInt(b.readLine());
		
		cf = new CommandFactory(cmls.getDescription(j));
		boolean argOK=false, argsDone=false;
		int i = 0;
		while(!argsDone) {
			for(String str: cf.listMissingArgNames()){
				t = cf.getArgType(str);
				if(!t.equals(ArgumentType.CallbackArgument)) {
					argOK=false;
					while(!argOK) {
						System.out.println("Enter value of type '"+t.getArgType()+"' for argument '"+str+"'");
						str2 = b.readLine();
						try {cf.addArgumentValue(str, str2); argOK = true;}
						catch (ArgumentNotFoundException e1) {System.out.println("Wrong value"); argOK=false;}
					}
				} else {
					cf.addArgumentCallback(str, this);
					JpgMini jpg = new JpgMini(String.valueOf(sid));
					viewers.put(String.valueOf(sid), jpg);	
				}
			}
			try {c = cf.getCommand(); argsDone=true;}
			catch (ConfigurationException e1) {System.out.println("Values missing"); argsDone=false;}
			if(i++>5){
				System.out.println("Exiting");
				return;
			}
		}
		
		res = agent.execute(c, String.valueOf(sid));

		if(cmls.getDescription(j).getReturnType().equals(CMLConstants.RET_TYPE_BYTE_ARRAY))
			new JpgMini(String.valueOf(sid)).setImage(res.getBytes());
		else
			System.out.println("Command returned: " + res.getString());					
	}
	
	public void printSensorList(SMLDescriptions smls){
		for(SMLDescription tmp: smls.getDescriptions()){
			System.out.print("SID: "+tmp.getSID());
			System.out.print(" - "+tmp.getSensorAddress());
			System.out.print(" - "+tmp.getProtocolType());
			System.out.println(" - "+tmp.getProtocolName());
		}
	}
	
	public void run(){
		int sid=0;
		String str, str2;
		StringBuilder sb = new StringBuilder();	

		while((sid=getAction())!=-1) {
			try {
				if(sid>=0)
					doActionSensor(sid);
				else if(sid==-2)
					System.out.println(agent.listActiveSensors());
				else if(sid==-3)
					printSensorList(new SMLDescriptions(agent.listActiveSensors()));
				else if (sid==-4)	{
					System.out.println("Enter the XML doc for the new procotol:");
					sb.delete(0, sb.length());
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					System.out.println("Load associated sensors from config file ? (yes-no)");
					str2=b.readLine();
					agent.addProtocol(sb.toString(), (str2.equals("yes"))?true:false);
					sb.delete(0, sb.length());
				}else if(sid==-5) {
					System.out.println("Enter the ID of the protocol to be removed:");
					str=b.readLine();
					System.out.println("Remove associated sensors from config file ? (yes-no)");
					str2=b.readLine();
					agent.removeProtocol(str, (str2.equals("yes"))?true:false);
				} else if(sid==-6)
					System.out.println(XMLhelper.toString(agent.listProtocols())); 
				else if(sid==-7) {
					System.out.println("Enter the XML doc for the new sensor:");
					sb.delete(0, sb.length());
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					agent.addSensor(sb.toString());
					sb.delete(0, sb.length());
				} else if(sid==-8) {
					System.out.println("Enter the ID of the Sensor to be removed:");
					str=b.readLine();
					agent.removeSensor(str);
				} else if(sid==-9)
					System.out.println(agent.listSensors());
				else if(sid==-10) 
					printSensorList(new SMLDescriptions(agent.listSensors()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stop(){
		try {
			agent.unregisterEventHandler(this, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(this, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(this, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		agent.stop();
		System.out.println("Main exiting");
	}
	
	
	public static void main(String [] args){
		String pc, sc;
		if(args.length!=2) {
			pc="conf/platformConfig-empty.xml";
			sc="conf/sensorsConfig-empty.xml";
		} else {
			pc = args[0];
			sc = args[1];
		}
		SALClient c = new SALClient();
		try {
			c.start(pc, sc);
			c.run();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} finally {
			c.stop();
		}
	}

	public String getName() {
		return "SAL user";
	}

	public void handle(Event e, SALAgent a) {
		System.out.println("Received "+e.toString());
	}


	private int n;
	private long ts;
	
	public void collect(Response r) {
		if(ts==0)
			ts = System.currentTimeMillis();
		else if((ts+10000)<System.currentTimeMillis()){
			System.out.println("FPS: "+( (float) ((float) 1000*n/((float)(System.currentTimeMillis()-ts))) ) );
			ts = System.currentTimeMillis();
			n=0;
		} else
			n++;
		try {
			viewers.get(r.getSID()).setImage(r.getBytes());
		} catch (SensorControlException e) {
			System.out.println("Stream from sensor "+r.getSID()+" returned an error");
			viewers.remove(r.getSID()).close();
		}
	}

}
