
package jcu.sal.comms.transport.tcp.codec.xml;

import jcu.sal.comms.TransportMessage;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;

public class MessageEncoder extends ProtocolEncoderAdapter {

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		TransportMessage command = (TransportMessage) message;

		String s = command.toXmlString();
		int size = 4 + s.length();

		ByteBuffer buffer = ByteBuffer.allocate(size, false);

		buffer.putInt(size);
		buffer.putInt(command.getId());

		for (int i = 0; i < s.length(); ++i) {
			buffer.putChar(s.charAt(i));
		}

		buffer.flip();
		out.write(buffer);
	}
}
