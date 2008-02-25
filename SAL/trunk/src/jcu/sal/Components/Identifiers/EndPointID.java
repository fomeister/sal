/**
 * 
 */
package jcu.sal.Components.Identifiers;

/**
 * this class represents Endpoint IDs. Two EndPointIDs are equal if their name is equal 
 * @author gilles
 *
 */
public class EndPointID extends AbstractIdentifier{
	
	/** The Protocol ID associated with this EndPoint **/
	private ProtocolID pid;
	
	/**
	 * Creates a new EndPointID with an initial name, type and ProtocolID
	 * @param id the name
	 * @param type the type
	 * @param pid the protocolID
	 */
	public EndPointID(String id, ProtocolID pid) {
		super(id);
		this.pid = pid; 
	}
	
	/**
	 * Creates a new EndPointID with an initial name and type. THe protocolID member is left empty 
	 * @param id the name
	 * @param type the type
	 */
	public EndPointID(String id) {	this(id, null); } 
	
	/**
	 * Return the name of the ProtocolID associated with this EndPoint 
	 * @return the protocolID
	 */
	public String getPIDName() {
		return getPid().getName();
	}
	
	/**
	 * Return the type of the ProtocolID associated with this EndPoint 
	 * @return the protocolID
	 */
	public String getPIDtype() {
		return getPid().getName();
	}

	/**
	 * Return the ProtocolID associated with this EndPoint 
	 * @return the protocolID
	 */
	public ProtocolID getPid() {
		if(pid == null) {
			System.out.println("************ TRYING TO ACCESS AN EMPTY PROTOCOL ID ***********");
			return new ProtocolID("");
		}
		else
			return pid;
	}

	/**
	 * Sets the ProtocolID associated with this EndPoint 
	 * @param pid the protocolID
	 */
	public void setPid(ProtocolID pid) {
		if(pid == null || (pid.getName().length()== 0)) {
			System.out.println("************ TRYING TO SET AN EMPTY PROTOCOL ID ON AN ENDPOINT***********");
			pid = null;
		}
		else
			this.pid = pid;
	}
	
	public static void main(String[] args) {
/*		EndPointID e1 = new EndPointID("usb1", "usb");
		ProtocolID p1 = new ProtocolID("myname1", Identifier.ANY_TYPE);
		e1.setPid(p1);
		
		EndPointID e2 = new EndPointID("usb2", "usb");
		
		EndPointID e3 = new EndPointID(ANY_NAME, "usb");
		
		EndPointID e4 = new EndPointID("serial", ANY_TYPE);
		ProtocolID p2 = new ProtocolID(Identifier.ANY_NAME, "fs");
		e4.setPid(p2);

		
		System.out.println("e1==e2:" + (e1.equals(e2)));
		System.out.println("e2==e3:" + (e2.equals(e3)));
		System.out.println("e3==e1:" + (e3.equals(e1)));
		System.out.println("e4==e2:" + (e4.equals(e2)));
		System.out.println("e1p1==e4p2:" + (e1.getPid().equals(e4.getPid())));
		*/
	}
}
