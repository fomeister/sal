
package jcu.sal.comms.transport.tcp.codec;

import jcu.sal.comms.transport.TransportCommand;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class CommandEncoder implements ProtocolEncoder {

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		TransportCommand command = (TransportCommand) message;

		String s = command.toXmlString();
		int size = 8 + s.length();

		ByteBuffer buffer = ByteBuffer.allocate(size, false);

		buffer.putInt(size);
		buffer.putInt(command.getId());

		for (int i = 0; i < s.length(); ++i) {
			buffer.putChar(s.charAt(i));
		}

		buffer.flip();
		out.write(buffer);
	}

	public void dispose(IoSession session) throws Exception {
	}
}
