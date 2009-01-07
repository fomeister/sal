
package jcu.sal.comms.grow;

import jcu.sal.xml.XMLHelper;
import jcu.sal.xml.MessageDescription;
import jcu.sal.xml.ArgumentDescription;

import javax.xml.bind.JAXBException;

public class GrowMessageFactory {

	public static final String GROW_COMMAND_NAME = "GrowCommand";
	public static final String GROW_SEQUENCE_COMMAND_NAME = "GrowSequenceCommand";
	public static final String GROW_RESPONSE_NAME = "GrowResponse";
	public static final String GROW_SEQUENCE_RESPONSE_NAME = "GrowSequenceResponse";

	private static MessageDescription growCommandDescription = null;
	private static MessageDescription growSequenceCommandDescription = null;

	private static MessageDescription getGrowCommandDescription() {
		if (growCommandDescription == null) {
			String d = "Takes a string and a number of repetitions and returns an array of strings, " +
				"where the ith element of the array is the initial string repeated (i + 1) times.";

			String xml = "<?xml version='1.0' encoding='UTF-8'?>\n";
			xml += "<messageDescription name='" + GROW_COMMAND_NAME + "'>\n";
			xml += "	<description>" + d + "</description>\n";
			xml += "	<argument name='inputString' type='string'>The string to repeat</argument>\n";
			xml += "	<argument name='reps' type='int'>The number of repetitions</argument>\n";
			xml += "</messageDescription>";

			try {
				growCommandDescription = (MessageDescription) XMLHelper.fromXmlString(xml);
			} catch (JAXBException je) {
			}
		}
		return growCommandDescription;
	}

	private static MessageDescription getGrowSequenceCommandDescription() {
		if (growSequenceCommandDescription == null) {

			String d = "Takes a string and a number of repetitions and returns an stream of strings, " +
				"where the ith element recieved is the initial string repeated (i + 1) times.";

			String xml = "<?xml version='1.0' encoding='UTF-8'?>\n";
			xml += "<messageDescription name='" + GROW_SEQUENCE_COMMAND_NAME + "'>\n";
			xml += "	<description>" + d + "</description>\n";
			xml += "	<argument name='inputString' type='string'>The string to repeat</argument>\n";
			xml += "	<argument name='reps' type='int'>The number of repetitions</argument>\n";
			xml += "</messageDescription>";

			try {
				growSequenceCommandDescription = (MessageDescription) XMLHelper.fromXmlString(xml);
			} catch (JAXBException je) {
			}
		}
		return growSequenceCommandDescription;
	}

	public static String[] getCommandNames() {
		String[] s = new String[2];
		s[0] = GROW_COMMAND_NAME;
		s[1] = GROW_SEQUENCE_COMMAND_NAME;
		return s;
	}

	public static MessageDescription getDescription(String commandName) {
		if (commandName.equals(GROW_COMMAND_NAME)) {
			return getGrowCommandDescription();
		} else if (commandName.equals(GROW_SEQUENCE_COMMAND_NAME)) {
			return getGrowSequenceCommandDescription();
		}

		return null;
	}

	public static GrowCommand createGrowCommand(String inputString, int reps) {
		GrowCommand growCommand = new GrowCommand(inputString, reps);
		growCommand.setDescription(getGrowCommandDescription());
		return growCommand;
	}

	public static GrowSequenceCommand createGrowSequenceCommand(String inputString, int reps) {
		GrowSequenceCommand growSequenceCommand = new GrowSequenceCommand(inputString, reps);
		growSequenceCommand.setDescription(getGrowSequenceCommandDescription());
		return growSequenceCommand;
	}

	public static GrowResponse createGrowResponse(String[] outputStrings) {
		return new GrowResponse(outputStrings);
	}

	public static GrowSequenceResponse createGrowSequenceResponse(String outputString, boolean isFinal) {
		return new GrowSequenceResponse(outputString, isFinal);
	}
}