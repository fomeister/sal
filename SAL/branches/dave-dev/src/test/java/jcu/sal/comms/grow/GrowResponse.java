
package jcu.sal.comms.grow;

import jcu.sal.comms.Response;

public class GrowResponse extends Response {

	public GrowResponse() {
		super();
		setName("GrowResponse");
		setFinal(true);
	}

	public GrowResponse(String[] outputStrings) {
		this();
		setOutputStrings(outputStrings);
	}

	public GrowResponse(Response response) {
		super(response);
		setName("GrowResponse");
		setFinal(true);
	}

	public void setOutputStrings(String[] outputStrings) {
		for (int i = 0; i < outputStrings.length; ++i) {
			setValue(0, i, outputStrings[i], "");
		}
	}

	public String[] getOutputStrings() {
		int size = getValueSize(0);
		String[] response = new String[size];

		for (int i = 0; i < size; ++i) {
			response[i] = getValue(0, i);
		}

		return response;
	}
}

