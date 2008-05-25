package haldbus;

import haldbus.match.HalMatchInterface;
import haldbus.match.HalNextMatch;
import haldbus.match.HalStringAlwaysMatch;
import haldbus.match.HalStringMatch;
import haldbus.match.HalVectorStringMatch;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

public class V4LHalClient implements HalClientInterface {
	private HalMatchInterface caps;
	private HalMatchInterface cat;
	private HalMatchInterface usbVendor;
	private HalMatchInterface usbProduct;
	
	public V4LHalClient() throws ConfigurationException{
		caps = new HalVectorStringMatch("info.capabilities", "video4linux", "caps");
		cat = new HalStringMatch("info.category", "video4linux", "cat");
		usbVendor = new HalNextMatch("@info.parent", new HalStringAlwaysMatch("usb_device.vendor_id", "usbVendor"), "usbParent");
		usbProduct = new HalNextMatch("@info.parent", new HalStringAlwaysMatch("usb_device.product_id", "usbProduct"), "usbParent2");		
	}

	public void doAction(Map<String,String> l) {
		System.out.println("Doing action");
	}

	public List<HalMatchInterface> getMatchList() {
		List<HalMatchInterface> l = new LinkedList<HalMatchInterface>();
		l.add(cat);
		l.add(caps);
		l.add(usbVendor);
		l.add(usbProduct);
		return l;
	}

	public String getName() {
		return "V4L HAL DBus client";
	}

	public int getMaxMatches() {
		return getMatchList().size();
	}

	public int getMinMatches() {
		return getMatchList().size();
	}

}
