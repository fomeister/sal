package jcu.sal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.naming.ConfigurationException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.w3c.dom.Document;

import jcu.sal.agent.SALAgent;
import jcu.sal.common.CMLConstants;
import jcu.sal.common.Command;
import jcu.sal.common.CommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.ResponseParser;
import jcu.sal.common.StreamCallback;
import jcu.sal.components.protocols.CMLDescription.ArgTypes;
import jcu.sal.components.sensors.SensorState;
import jcu.sal.events.Event;
import jcu.sal.events.EventHandler;
import jcu.sal.managers.ProtocolManager;
import jcu.sal.managers.SensorManager;
import jcu.sal.utils.XMLhelper;

public class SALuserFile implements EventHandler, StreamCallback{
	static JpgMini jpg;
	static long start, now;
	static int n;
	
	public static class JpgMini {
		JLabel l;
		JFrame f;
	    public JpgMini(){
	        f = new JFrame();
	        l = new JLabel();
	        f.getContentPane().add(l);
	        f.setSize(640,480);
	        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    }
	    
	    public void setImage(byte[] b) {
	    	l.setIcon(new ImageIcon(b));
	    }
	    
	    public void setVisible(){
	    	f.setVisible(true);
	    }
	}
	
	
	public static void main(String [] args) throws ConfigurationException, InterruptedException {
		int i=0,j=0;
//		int fn=0;
		String str, str2;
		Response res;
		Document d;
		Command c=null;
		StringBuilder sb = new StringBuilder();
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		SALuserFile user = new SALuserFile(); 
		SALAgent s = new SALAgent();
		s.registerEventHandler(user, SensorManager.PRODUCER_ID);
		s.registerEventHandler(user, ProtocolManager.PRODUCER_ID);
		s.registerEventHandler(user, SensorState.PRODUCER_ID);
		try {
			s.start(args[0], args[1]);
			jpg = new JpgMini();
			
			while(i!=-1) {
				System.out.println("Enter either :\n\ta sensor id to send a command\n\t-1 to quit\n\t-2 to see a list of active sensors");
				System.out.println("\t-3 to add a new protocol\n\t-4 to remove a protocol\n\t-5 to add a new sensor\n\t-6 to remove a sensor");
				System.out.println("\t-7 to list all sensors");
				try {
					i=Integer.parseInt(b.readLine());
					if(i>=0) {
						System.out.println("\n\nHere is the CML document for this sensor:");
						d = s.getCML(String.valueOf(i));
						System.out.println(XMLhelper.toString(d));
						System.out.println("Enter a command id:");
						j=Integer.parseInt(b.readLine());
						
						CommandFactory cf = new CommandFactory(d, j);
						ArgTypes t;
						boolean ok1=false, ok2=false;
						while(!ok2) {
							Enumeration<String> e = cf.listMissingArgNames();
							while(e.hasMoreElements()){
								str = e.nextElement();
								t = cf.getArgType(str);
								if(!t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
									while(!ok1) {
										System.out.println("Enter value of type '"+t.getArgType()+"' for argument '"+str+"'");
										str2 = b.readLine();
										try {cf.addArgumentValue(str, str2); ok1 = true;}
										catch (ConfigurationException e1) {System.out.println("Wrong value"); ok1=false;}
									}
								} else {
									cf.addArgumentCallback(str, user);
									now=start=System.currentTimeMillis();
									n=0;
									jpg.setVisible();
								}
							}
							try {c = cf.getCommand(); ok2=true;}
							catch (ConfigurationException e1) {System.out.println("Values missing"); ok2=false;}
						}
						
						res = s.execute(c, String.valueOf(i));
						//new FileOutputStream("file"+(fn++),false).write(ResponseParser.toByteArray(res));
						String xpath=CMLConstants.XPATH_CMD_DESC+"[@"+CMLConstants.CID_ATTRIBUTE+"=\""+j+"\"]/"+CMLConstants.RETURN_TYPE_TAG;
						try {
							String type = XMLhelper.getAttributeFromName(xpath, CMLConstants.TYPE_ATTRIBUTE, d);
							if(type.equals(CMLConstants.RET_TYPE_BYTE_ARRAY)) {
								jpg.setImage(ResponseParser.toByteArray(res));
								jpg.setVisible();
							} else {
								System.out.println("Command returned: " + ResponseParser.toString(res));
							}
						} catch (Exception e){
							System.out.println("Cant find the return type");
							System.out.println("XPATH: "+xpath);
							e.printStackTrace();
						}
						
											
					} else if(i==-2)
						System.out.println(s.listActiveSensors());
					else if(i==-3) {
						System.out.println("Enter the XML doc for the new procotol:");
						sb.delete(0, sb.length());
						while(!(str=b.readLine()).equals(""))
							sb.append(str);
						System.out.println("Load associated sensors from config file ? (yes-no)");
						str2=b.readLine();
						s.addProtocol(sb.toString(), (str2.equals("yes"))?true:false);
						sb.delete(0, sb.length());
					}else if(i==-4) {
						System.out.println("Enter the ID of the protocol to be removed:");
						str=b.readLine();
						System.out.println("Remove associated sensors from config file ? (yes-no)");
						str2=b.readLine();
						s.removeProtocol(str, (str2.equals("yes"))?true:false);
					} else if(i==-5) {
						System.out.println("Enter the XML doc for the new sensor:");
						sb.delete(0, sb.length());
						while(!(str=b.readLine()).equals(""))
							sb.append(str);
						s.addSensor(sb.toString());
						sb.delete(0, sb.length());
					} else if(i==-6) {
						System.out.println("Enter the ID of the Sensor to be removed:");
						str=b.readLine();
						s.removeSensor(str);
					} else if(i==-7)
						System.out.println(s.listSensors());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
		}
		finally {
			s.unregisterEventHandler(user, SensorManager.PRODUCER_ID);
			s.unregisterEventHandler(user, ProtocolManager.PRODUCER_ID);
			s.unregisterEventHandler(user, SensorState.PRODUCER_ID);
			s.stop();
		}
		System.exit(0);
	}

	public String getName() {
		return "SAL user";
	}

	public void handle(Event e) {
		System.out.println("Received "+e.toString());
	}

	public void collect(Response r) {
		now=System.currentTimeMillis();
		if(now>start+(10*1000)) {
			System.out.println("\tFPS: "+((float) n/(now/1000-start/1000)));
			start = now;
			n=0;
		}
		n++;
			
		jpg.setImage(ResponseParser.toByteArray(r));		
	}
}
