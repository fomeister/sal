package jcu.sal.common.cml;

import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * This class encapsulates the different supported return types in CML descriptors
 * @author gilles
 *
 */
public class ReturnType {
	private static Logger logger = Logger.getLogger(ReturnType.class);
	static {
		Slog.setupLogger(logger);
	}
	
	public static ReturnType Integer = new ReturnType(CMLConstants.RET_TYPE_INT);
	public static ReturnType ByteArray = new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY);
	public static ReturnType String = new ReturnType(CMLConstants.RET_TYPE_STRING);
	public static ReturnType Float = new ReturnType(CMLConstants.RET_TYPE_FLOAT);
	public static ReturnType Void = new ReturnType(CMLConstants.RET_TYPE_VOID);
	
	private String t;
	
	/**
	 * Construct a new return type. Supported types are listed in CMLConstants.RET_TYPE_*
	 * @param s the type (CMLConstants.RET_TYPE_*)
	 * @throws SALRunTimeExceptionif the type is invalid
	 */
	public ReturnType(String s) {
		if(!s.equals(CMLConstants.RET_TYPE_INT) && !s.equals(CMLConstants.RET_TYPE_FLOAT) && !s.equals(CMLConstants.RET_TYPE_STRING) && !s.equals(CMLConstants.RET_TYPE_BYTE_ARRAY) && !s.equals(CMLConstants.RET_TYPE_VOID)) {
			logger.error("Unknown return type: "+s);
			throw new SALRunTimeException("Invalid return type '"+s+"'");
		}
		t = s;
	}
	/**
	 * Returns the return type (CMLConstants.RET_TYPE_*)
	 * @return the return type (CMLConstants.RET_TYPE_*)
	 */
	public String getReturnType() {
		return t; 
	}
	
	public String toString(){
		if(t.equals(CMLConstants.RET_TYPE_INT))
			return "integer";
		else if(t.equals(CMLConstants.RET_TYPE_BYTE_ARRAY))
			return "byte array";
		else if(t.equals(CMLConstants.RET_TYPE_FLOAT))
			return "float";
		else if(t.equals(CMLConstants.RET_TYPE_STRING))
			return "string";
		else if(t.equals(CMLConstants.RET_TYPE_VOID))
			return "none";
		else
			return "UNKNOWN RETURN TYPE";
				
	}
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((t == null) ? 0 : t.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ReturnType other = (ReturnType) obj;
		if (t == null) {
			if (other.t != null)
				return false;
		} else if (!t.equals(other.t))
			return false;
		return true;
	}	
}