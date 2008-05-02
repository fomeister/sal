package jcu.sal;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.naming.ConfigurationException;

import jcu.sal.agent.SALAgent;
import jcu.sal.common.Command;
import jcu.sal.components.sensors.SensorState;
import jcu.sal.events.Event;
import jcu.sal.events.EventHandler;
import jcu.sal.managers.ProtocolManager;
import jcu.sal.managers.SensorManager;

public class SALuser implements EventHandler{
	static SALAgent s;
	
	public static void main(String [] args) throws ConfigurationException, InterruptedException {
		int i=0,j=0;
		String str, str2;
		StringBuilder sb = new StringBuilder();
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		SALuser user = new SALuser(); 
		s = new SALAgent();
		s.registerEventHandler(user, SensorManager.PRODUCER_ID);
		s.registerEventHandler(user, ProtocolManager.PRODUCER_ID);
		s.registerEventHandler(user, SensorState.PRODUCER_ID);
		try {
			s.start(args[0], args[1]);
	
			
			while(i!=-1) {
				System.out.println("Enter either :\n\ta sensor id to send a command\n\t-1 to quit\n\t-2 to see a list of active sensors");
				System.out.println("\t-3 to add a new protocol\n\t-4 to remove a protocol\n\t-5 to add a new sensor\n\t-6 to remove a sensor");
				System.out.println("\t-7 to list all sensors");
				try {
					i=Integer.parseInt(b.readLine());
					if(i>=0) {
						System.out.println("\n\nHere is the CML document for this sensor:");
						System.out.println(s.getCML(String.valueOf(i)));
						System.out.println("Enter a command id:");
						j=Integer.parseInt(b.readLine());
						System.out.println("command "+j+" returned : "+s.execute(new Command(j, "", ""), String.valueOf(i)));
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
	}

	public String getName() {
		return "SAL user";
	}

	public void handle(Event e) {
		System.out.println("Received "+e.toString());
	}
}
