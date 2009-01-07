
package jcu.sal.comms.transport.tcp.codec.binary;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolDecoder;

public class BinaryCodecFactory implements ProtocolCodecFactory {
    private final MessageEncoder encoder;
    private final MessageDecoder decoder;

	public BinaryCodecFactory() {
		encoder = new MessageEncoder();
		decoder = new MessageDecoder();
	}

	public void setCharsetName(String charsetName) {
		encoder.setCharsetName(charsetName);
		decoder.setCharsetName(charsetName);
	}

	public ProtocolEncoder getEncoder() throws Exception {
		return encoder;
	}

	public ProtocolDecoder getDecoder() throws Exception {
		 return decoder;
	}
}
