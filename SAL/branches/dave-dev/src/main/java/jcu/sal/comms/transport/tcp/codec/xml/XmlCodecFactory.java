
package jcu.sal.comms.transport.tcp.codec.xml;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolDecoder;

public class XmlCodecFactory implements ProtocolCodecFactory {
    private final ProtocolEncoder encoder;
    private final ProtocolDecoder decoder;

	public XmlCodecFactory() {
		encoder = new MessageEncoder();
		decoder = new MessageDecoder();
	}

	public ProtocolEncoder getEncoder() throws Exception {
		return encoder;
	}

	public ProtocolDecoder getDecoder() throws Exception {
		 return decoder;
	}
}
