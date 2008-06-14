package jcu.sal.common.cml;

import javax.naming.ConfigurationException;

import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

/**
 * This class encapsulates the different supported return types in CML descriptors
 * @author gilles
 *
 */
public class ReturnType {
	private String t;
	private static Logger logger = Logger.getLogger(ReturnType.class);
	static {
		Slog.setupLogger(logger);
	}
	/**
	 * Construct a new return type. Supported types are listed in CMLConstants.RET_TYPE_*
	 * @param s the type (CMLConstants.RET_TYPE_*)
	 * @throws ConfigurationException if the type is invalid
	 */
	public ReturnType(String s) throws ConfigurationException{
		if(!s.equals(CMLConstants.RET_TYPE_INT) && !s.equals(CMLConstants.RET_TYPE_FLOAT) && !s.equals(CMLConstants.RET_TYPE_STRING) && !s.equals(CMLConstants.RET_TYPE_BYTE_ARRAY) && !s.equals(CMLConstants.RET_TYPE_VOID)) {
			logger.error("Unknown return type: "+s);
			throw new ConfigurationException();
		}
		t = s;
	}
	/**
	 * Returns the return type (CMLConstants.RET_TYPE_*)
	 * @return the return type (CMLConstants.RET_TYPE_*)
	 */
	String getReturnType() {
		return t; 
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