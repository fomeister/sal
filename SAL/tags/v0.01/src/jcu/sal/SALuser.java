package jcu.sal;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.naming.ConfigurationException;

import jcu.sal.Agent.SALAgent;
import jcu.sal.Components.Command;

public class SALuser {
	static SALAgent s;
	
	public static void main(String [] args) throws ConfigurationException {
		int i=0,j=0;
		String str;
		StringBuilder sb = new StringBuilder();
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		s = new SALAgent();
		s.init(args[0], args[1]);
		
		while(i!=-1) {
			System.out.println("Enter either :\n\ta sensor id to send a command\n\t-1 to quit\n\t-2 to see a list of sensors");
			System.out.println("\t-3 to add a new protocol\n\t-4 to remove a protocol\n\t-5 to add a new sensor\n\t-6 to remove a sensor");
			try {
				i=Integer.parseInt(b.readLine());
				if(i>=0) {
					System.out.println("\n\nHere is the CML document for this sensor:");
					System.out.println(s.getCML(String.valueOf(i)));
					System.out.println("Enter a command id:");
					j=Integer.parseInt(b.readLine());
					System.out.println("command "+j+" returned : "+s.execute(new Command(j, "", ""), String.valueOf(i)));
				} else if(i==-2)
					System.out.println(s.listSensors());
				else if(i==-3) {
					System.out.println("Enter the XML doc for the new procotol:");
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					s.addProtocol(sb.toString());
					sb.delete(0, sb.length());
					//str = "<Protocol name=\"osData\" type=\"PlatformData\"><EndPoint name=\"filesystem\" type=\"fs\" /><parameters><Param name=\"CPUTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp2_input\" /><Param name=\"NBTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp1_input\" /><Param name=\"SBTempFile\" value=\"/sys/class/hwmon/hwmon0/device/temp3_input\" /></parameters>	</Protocol";
					//s.addProtocol(str);
				}else if(i==-4) {
					System.out.println("Enter the ID of the protocol to be removed:");
					str=b.readLine();
					s.removeProtocol(str);
					sb.delete(0, sb.length());
				} else if(i==-5) {
					System.out.println("Enter the XML doc for the new sensor:");
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					s.addSensor(sb.toString());
					sb.delete(0, sb.length());
				} else if(i==-6) {
					System.out.println("Enter the ID of the Sensor to be removed:");
					str=b.readLine();
					s.removeSensor(str);
					sb.delete(0, sb.length());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		s.stop();
	}
}
