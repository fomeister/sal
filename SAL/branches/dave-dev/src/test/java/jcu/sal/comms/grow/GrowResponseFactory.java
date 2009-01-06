
package jcu.sal.comms.grow;

public class GrowResponseFactory {

	public static GrowResponse createGrowResponse(String[] outputStrings) {
		return new GrowResponse(outputStrings);
	}

	public static GrowSequenceResponse createGrowSequenceResponse(String outputString, boolean isFinal) {
		return new GrowSequenceResponse(outputString, isFinal);
	}
}
