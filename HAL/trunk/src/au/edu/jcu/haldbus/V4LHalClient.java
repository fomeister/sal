package au.edu.jcu.haldbus;


import java.util.Map;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.GenericMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.VectorMatch;


public class V4LHalClient extends AbstractDeviceDetection {
	
	
	public V4LHalClient() throws AddRemoveElemException{
		//super(INITIAL_RUN_FLAG);
		addMatch("1-capability", new VectorMatch<String>("info.capabilities", "video4linux"));
		addMatch("2-category", new GenericMatch<String>("info.category", "video4linux"));
		addMatch("3-category.capture", new VectorMatch<String>("info.capabilities", "video4linux.video_capture"));
		addMatch("4-video4linux.device", new GenericMatch<String>("video4linux.device", "video", true,true));
		addMatch("5-deviceFile", new AlwaysMatch("linux.device_file"));
		addMatch("6-info.product", new NextMatch("@info.parent", new AlwaysMatch("info.product")));
		addMatch("7-info.vendor", new NextMatch("@info.parent", new AlwaysMatch("info.vendor")));
		//addMatch("usbVendor", new NextMatch("@info.parent", new AlwaysMatch("usb_device.vendor_id"));
		//addMatch("usbProduct", new NextMatch("@info.parent", new AlwaysMatch("usb_device.product_id"));
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		System.out.println("Found "+l.get("6-info.product")+" - "+l.get("7-info.vendor")+ " on "+l.get("5-deviceFile"));
	}

	@Override
	public String getName() {
		return "V4L HAL DBus client";
	}

	public void deviceRemoved(Map<String, String> l) {
		// TODO Auto-generated method stub
		
	}
}
