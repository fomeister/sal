
package jcu.sal.comms.grow;

import jcu.sal.comms.Response;

public class GrowSequenceResponse extends Response {

	public GrowSequenceResponse() {
		super();
		setName("GrowSequenceResponse");
	}

	public GrowSequenceResponse(String outputString, boolean isFinal) {
		this();
		setOutputString(outputString);
		setFinal(isFinal);
	}

	public GrowSequenceResponse(Response response) {
		super(response);
		setName("GrowSequenceResponse");
	}

	public void setOutputString(String outputString) {
		setValue(0, outputString);
	}

	public String getOutputString() {
		return getValue(0);
	}
}

