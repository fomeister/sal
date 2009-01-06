
package jcu.sal.comms.grow;

public class GrowCommandFactory {

	public static GrowCommand createGrowCommand(String inputString, int reps) {
		return new GrowCommand(inputString, reps);
	}

	public static GrowSequenceCommand createGrowSequenceCommand(String inputString, int reps) {
		return new GrowSequenceCommand(inputString, reps);
	}
}
