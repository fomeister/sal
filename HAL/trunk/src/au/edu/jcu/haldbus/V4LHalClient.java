package au.edu.jcu.haldbus;


import java.util.Map;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.InvalidConstructorArgs;
import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.TypeMatch;
import au.edu.jcu.haldbus.match.VectorMatch;


public class V4LHalClient extends AbstractDeviceDetection {
	
	
	public V4LHalClient() throws InvalidConstructorArgs, AddRemoveElemException{
		super(SUBSEQUENT_RUN_FLAG);
		addMatch("capability", new VectorMatch<String>("info.capabilities", "video4linux"));
		addMatch("category", new TypeMatch<String>("info.category", "video4linux"));
		addMatch("category.capture", new VectorMatch<String>("info.capabilities", "video4linux.video_capture"));
		addMatch("deviceFile", new AlwaysMatch("linux.device_file"));
		addMatch("info.product", new NextMatch("@info.parent", new AlwaysMatch("info.product")));
		addMatch("info.vendor", new NextMatch("@info.parent", new AlwaysMatch("info.vendor")));
		//addMatch("usbVendor", new NextMatch("@info.parent", new AlwaysMatch("usb_device.vendor_id"));
		//addMatch("usbProduct", new NextMatch("@info.parent", new AlwaysMatch("usb_device.product_id"));		
	}

	@Override
	public void doAction(Map<String,String> l) {
		System.out.println("Found "+l.get("info.product")+" - "+l.get("info.vendor")+ " on "+l.get("deviceFile"));
	}

	@Override
	public String getName() {
		return "V4L HAL DBus client";
	}
}
