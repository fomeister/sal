package au.edu.jcu.haldbus.examples;


import java.io.IOException;
import java.util.Map;

import au.edu.jcu.haldbus.HardwareWatcher;
import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.DBusException;
import au.edu.jcu.haldbus.filter.AbstractHalFilter;
import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.GenericMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.VectorMatch;


public class V4LHalFilter extends AbstractHalFilter {
	public V4LHalFilter(String n) throws AddRemoveElemException{
		super(n);
		addMatch("1-capability", new VectorMatch<String>("info.capabilities", "video4linux"));
		addMatch("2-category", new GenericMatch<String>("info.category", "video4linux"));
		addMatch("3-category.capture", new VectorMatch<String>("info.capabilities", "video4linux.video_capture"));
		addMatch("4-video4linux.device", new GenericMatch<String>("video4linux.device", "video", true,true));
		addMatch("5-deviceFile", new AlwaysMatch("linux.device_file"));
		addMatch("6-info.product", new NextMatch("@info.parent", new AlwaysMatch("info.product")));
		addMatch("7-info.vendor", new NextMatch("@info.parent", new AlwaysMatch("info.vendor")));
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		System.out.println(l.get("6-info.product")+" - "+l.get("7-info.vendor")+ " on "+l.get("5-deviceFile")+" Connected");
	}

	@Override
	public void deviceRemoved(Map<String, String> l) {
		System.out.println(l.get("6-info.product")+" - "+l.get("7-info.vendor")+ " on "+l.get("5-deviceFile")+" Disconnected");
	}
	
	public static void main(String[] args) throws DBusException{
		HardwareWatcher w = new HardwareWatcher();
		V4LHalFilter v1 = new V4LHalFilter("Filter1");
		w.addClient(v1);
		w.start();
		System.out.println("Try connect / disconnect a v4l device");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		w.stop();
		w.removeClient(v1);
	}
}
