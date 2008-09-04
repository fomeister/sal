package jcu.sal.common.cml;

public interface CMLConstants {
	public static String CMD_DESCRIPTIONS_TAG = 		"commandDescriptions";
	public static String CMD_DESCRIPTION_TAG = 			"CommandDescription";
	public static String CMD_INSTANCE_TAG = 			"CommandInstance";
	public static String CID_ATTRIBUTE = 				"cid";
	public static String NAME_TAG = 					"Name";
	public static String SHORT_DESCRIPTION_TAG = 		"ShortDescription";
	public static String ARGUMENTS_TAG = 				"arguments";
	public static String ARGUMENT_TAG = 				"Argument";
	public static String NAME_ATTRIBUTE = 				"name";
	public static String TYPE_ATTRIBUTE = 				"type";
	public static String RETURN_TYPE_TAG = 				"ReturnType";
	
	public static String XPATH_CMD_DESC = 				"/"+CMD_DESCRIPTIONS_TAG+"/"+CMD_DESCRIPTION_TAG;
	public static String XPATH_CMD_DESC_NAME = 			XPATH_CMD_DESC+"/"+NAME_TAG;
	public static String XPATH_CMD_DESC_SHORT_DESC = 	XPATH_CMD_DESC+"/"+SHORT_DESCRIPTION_TAG;
	public static String XPATH_CMD_DESC_ARGUMENTS = 	XPATH_CMD_DESC+"/"+ARGUMENTS_TAG;
	public static String XPATH_CMD_DESC_ARGUMENT = 		XPATH_CMD_DESC_ARGUMENTS+"/"+ARGUMENT_TAG;
	public static String XPATH_CMD_DESC_RETURN_TYPE= 	XPATH_CMD_DESC+"/"+RETURN_TYPE_TAG;
	
	public static String XPATH_CMD_INST = 				"/"+CMD_INSTANCE_TAG;
	public static String XPATH_CMD_INST_ARGUMENTS = 	XPATH_CMD_INST+"/"+ARGUMENTS_TAG;
	public static String XPATH_CMD_INST_ARGUMENT = 		XPATH_CMD_INST_ARGUMENTS+"/"+ARGUMENT_TAG;
	
	public static String ARG_TYPE_STRING="string";
	public static String ARG_TYPE_INT="int";
	public static String ARG_TYPE_FLOAT="float";
	public static String ARG_TYPE_CALLBACK="callback";
	
	public static String RET_TYPE_INT="int";
	public static String RET_TYPE_FLOAT="float";
	public static String RET_TYPE_STRING="string";
	public static String RET_TYPE_BYTE_ARRAY="byte array";
	public static String RET_TYPE_VOID="void";
}
