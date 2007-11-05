/**
 * @author gilles
 */
package jcu.sal.Components.EndPoints;

import jcu.sal.Components.AbstractComponent;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public abstract class EndPoint extends AbstractComponent {

	private Logger logger = Logger.getLogger(EndPoint.class);
	public static final String ENDPOINTPARAMNAME_TAG = "name";
	public static final String ENDPOINTPARAM_TAG = "Param";
	public static final String ENDPOINTTYPE_TAG = "type";
	public static final String ENDPOINTNAME_TAG = "name";
	public static final String ENPOINT_TAG="EndPoint";
	/**
	 * 
	 */
	public EndPoint() {
		super();
		Slog.setupLogger(this.logger);
		this.logger.debug("ctor Endpoint");
	}	
}
