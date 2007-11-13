/**
 * 
 */
package jcu.sal.Components.Identifiers;

/**
 * this class represents Endpoint names
 * @author gilles
 *
 */
public class ProtocolID extends AbstractIdentifier{

	/**
	 * Creates a new ProtocolID with an initial name
	 * @param id
	 */
	public ProtocolID(String id, String type) {
		super(id, type);
	}
	
	public static void main(String[] args) {
		ProtocolID p1 = new ProtocolID("myname1", Identifier.ANY_TYPE);
		ProtocolID p2 = new ProtocolID(Identifier.ANY_NAME, "usb");
		ProtocolID p3 = new ProtocolID("myname2", "usb");
		ProtocolID p4 = new ProtocolID("myname1", "usb1");
		
		System.out.println("p1==p2:" + (p1.equals(p2)));
		System.out.println("p2==p3:" + (p2.equals(p3)));
		System.out.println("p3==p1:" + (p3.equals(p1)));
		System.out.println("p4==p1:" + (p4.equals(p1)));
	}
}
