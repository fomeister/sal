package jcu.sal.client.gui.view;

import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.sml.SMLDescription;

public class SensorTreeLabel {
	public static int SML_TYPE = 0;
	public static int PROTOCOL_TYPE = 1;
	public static int STRING_TYPE = 1;
	
	/**
	 * the type of label (SML or protocol config), see constants above
	 */
	private int type;
	
	/**
	 * The {@link SMLDescription} or the {@link ProtocolConfiguration} or String object
	 */
	private Object obj;
	
	/**
	 * The string representation shown in the Tree
	 */
	private String stringVal;
	
	
	/**
	 * This method builds a SensorTreeLabel object given either a
	 * {@link ProtocolConfiguration} or {@link SMLDescription} object.
	 * @param o a {@link ProtocolConfiguration} or {@link SMLDescription} object used
	 * to build this SensorTreeLabel object
	 * @throws SALRunTimeException if the given object is neither of type
	 * ProcotolConfiguration nor SMLDescription 
	 */
	public SensorTreeLabel(Object o){
		if(o instanceof ProtocolConfiguration){
			type = PROTOCOL_TYPE;
			stringVal = new String(((ProtocolConfiguration) o).getID());
		} else if (o instanceof SMLDescription){
			type = SML_TYPE;
			stringVal = new String(((SMLDescription) o).getSensorAddress()+
					" - "+((SMLDescription) o).getID());
		} else if (o instanceof String){
			//for the root node
			stringVal = (String) o;
			type = STRING_TYPE;
		} else
			throw new SALRunTimeException("The given object is neither of type ProcotolConfiguration nor SMLDescription");
		obj = o;
	}
	
	/**
	 * This method returns the type of the label:
	 * {@link #SML_TYPE} or {@link #PROTOCOL_TYPE}.
	 * @return the type of the label
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * This method returns the
	 * {@link SMLDescription} object associated with this label.
	 * @return the {@link SMLDescription} object associated with this label.
	 */
	public SMLDescription getSMLDescription(){
		return (SMLDescription) obj;
	}

	/**
	 * This method returns the
	 * {@link ProtocolConfiguration} object associated with this label.
	 * @return the {@link ProtocolConfiguration} object associated with this label.
	 */
	public ProtocolConfiguration getProtocolConfiguration(){
		return (ProtocolConfiguration) obj;
	}
		
	
	@Override
	public String toString(){
		return stringVal;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		result = prime * result
				+ ((stringVal == null) ? 0 : stringVal.hashCode());
		result = prime * result + type;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorTreeLabel other = (SensorTreeLabel) obj;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		if (stringVal == null) {
			if (other.stringVal != null)
				return false;
		} else if (!stringVal.equals(other.stringVal))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
