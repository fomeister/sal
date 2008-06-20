package jcu.sal.components.protocols;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.ReturnType;
import jcu.sal.utils.XMLhelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CMLDescriptorTest {
	
	private CMLDescription c1, c2, c3;
	private List<ArgumentType> t1;
	private List<String> n1;
	private ReturnType r1;
//	private String noArgDoc = "<CommandDescription name=\"noArgDoc\">\n"
//								+"<CID>100</CID> <!-- Command ID -->\n"
//								+"<ShortDescription>it works without args too</ShortDescription>\n"
//								+"<ReturnType type=\"void\" />\n"
//								+"</CommandDescription>\n";

	@Before
	public void setUp() throws Exception {

		
		t1 = new Vector<ArgumentType>();
		t1.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));
		
		n1 = new Vector<String>();
		n1.add("integer");
		
		r1 = new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY);
		
		c1 = new CMLDescription("MethodName1", new Integer(1), "Command1", "Description for command 1", t1, n1, r1);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCMLDescriptorStringIntegerStringStringArgTypesArrayStringArrayReturnType() throws ConfigurationException {
		List<ArgumentType> args;
		List<String> argNames;
		assertTrue(c1.getMethodName().equals("MethodName1"));
		
		assertTrue(c1.getCID().equals(new Integer(1)));
		
		assertTrue(c1.getName().equals("Command1"));
		
		assertTrue(c1.getDesc().equals("Description for command 1"));

		args = new Vector<ArgumentType>();
		args.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));		
		assertTrue(c1.getArgTypes().equals(args));
		
		argNames = new Vector<String>();
		argNames.add("integer");
		assertTrue(c1.getArgNames().equals(argNames));
		
		assertFalse(c1.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_VOID)));
		assertTrue(c1.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY)));
	}

	@Test
	public void testCMLDescriptorIntegerStringCMLDescriptor() throws ConfigurationException {
		c2 = new CMLDescription(new Integer(2), "Command2",c1);
		List<String> argNames;
		List<ArgumentType> args;
	
		assertTrue(c2.getMethodName().equals(c1.getMethodName()));
		assertTrue(c2.getMethodName().equals("MethodName1"));
		
		assertTrue(c2.getCID().equals(new Integer(2)));
		
		assertTrue(c2.getName().equals("Command2"));
		assertFalse(c2.getName().equals(c1.getName()));

		assertTrue(c2.getDesc().equals("Description for command 1"));
		assertTrue(c2.getDesc().equals(c1.getDesc()));
		
		assertTrue(c2.getArgTypes().equals(c1.getArgTypes()));
		
		args = new Vector<ArgumentType>();
		args.add(new ArgumentType(CMLConstants.ARG_TYPE_FLOAT));	
		assertFalse(c2.getArgTypes().equals(args));


		assertTrue(c2.getArgNames().equals(c1.getArgNames()));
		argNames = new Vector<String>();
		argNames.add("Int");
		assertFalse(c2.getArgNames().equals(argNames));
		argNames = new Vector<String>();
		argNames.add("integer");
		assertTrue(c2.getArgNames().equals(argNames));
		
		assertTrue(c2.getReturnType().equals(c1.getReturnType()));		
		assertFalse(c2.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_VOID)));
		assertTrue(c2.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY)));
		
		assertFalse(c2.getCMLString().equals(c1.getCMLString()));
	}

	@Test
	public void testCMLDescriptorDocument() throws ConfigurationException, ParserConfigurationException {
		c3 = new CMLDescription(XMLhelper.createDocument(c1.getCMLString()));
		List<String> argNames;
		
		assertFalse(c3.getMethodName().equals(c1.getMethodName()));
		assertTrue(c3.getMethodName().equals(""));
		
		assertFalse(c3.getCID().equals(new Integer(2)));
		assertTrue(c3.getCID().equals(c1.getCID()));
		
		assertFalse(c3.getName().equals("commandX"));
		assertTrue(c3.getName().equals(c1.getName()));
		
		assertTrue(c3.getDesc().equals(c1.getDesc()));
		assertTrue(c3.getDesc().equals("Description for command 1"));
		
		assertTrue(c3.getArgTypes().equals(c1.getArgTypes()));
		
		assertTrue(c3.getArgNames().equals(c1.getArgNames()));
		argNames = new Vector<String>();
		argNames.add("integer");
		assertTrue(c3.getArgNames().equals(argNames));
		argNames = new Vector<String>();
		argNames.add("Integer");
		assertFalse(c3.getArgNames().equals(argNames));
		
		assertTrue(c3.getReturnType().equals(c1.getReturnType()));
		assertTrue(c3.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY)));
		assertFalse(c3.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_INT)));
		

		c2 = new CMLDescription(new Integer(2), "Command2",c1);
		
		assertFalse(c2.getMethodName().equals(c3.getMethodName()));
		assertTrue(c2.getMethodName().equals(c1.getMethodName()));
		
		assertFalse(c2.getCID().equals(c3.getCID()));
		assertFalse(c2.getCID().equals(c1.getCID()));
		assertTrue(c2.getCID().equals(new Integer(2)));
		
		assertFalse(c2.getName().equals(c3.getName()));
		assertFalse(c2.getName().equals(c1.getName()));

		assertTrue(c2.getDesc().equals(c3.getDesc()));
		assertTrue(c2.getDesc().equals(c1.getDesc()));

		assertTrue(c2.getArgTypes().equals(c3.getArgTypes()));
		assertTrue(c2.getArgTypes().equals(c1.getArgTypes()));
		
		argNames = new Vector<String>();
		argNames.add("Int");
		assertTrue(c2.getArgNames().equals(c3.getArgNames()));
		assertTrue(c2.getArgNames().equals(c1.getArgNames()));
		
		assertTrue(c2.getReturnType().equals(c3.getReturnType()));
		assertTrue(c2.getReturnType().equals(c1.getReturnType()));
		
//		c3 = new CMLDescription(XMLhelper.createDocument(noArgDoc));
//		assertFalse(c3.getMethodName().equals("noArgDoc"));
//		assertTrue(c3.getMethodName().equals(""));
//		
//		assertFalse(c3.getCID().equals(new Integer(100)));
//		assertTrue(c3.getCID().equals(c1.getCID()));
//		
//		assertTrue(c3.getName().equals("noArgDoc"));
//				
//		assertTrue(c3.getDesc().equals("it works without args too"));
//		
//		assertTrue(c3.getArgCount()==0);
//		
//		args = new Vector<ArgTypes>();
//		assertTrue(c3.getArgTypes().equals(args));
//		
//		argNames = new Vector<String>();
//		assertTrue(c3.getArgNames().equals(argNames));
//				
//		assertTrue(c3.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_VOID)));
	}

}
