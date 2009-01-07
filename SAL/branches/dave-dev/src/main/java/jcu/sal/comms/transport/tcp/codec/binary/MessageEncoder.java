
package jcu.sal.comms.transport.tcp.codec.binary;

import jcu.sal.comms.TransportMessage;
import jcu.sal.xml.Argument;

import java.io.UnsupportedEncodingException;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;

public class MessageEncoder extends ProtocolEncoderAdapter {

	private String charsetName = "UTF-8";

	public MessageEncoder() {
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	private int getTransportMessageSize(TransportMessage tm) throws UnsupportedEncodingException {
		int size = 0;

		// id
		size += 4;

		byte[] name = tm.getName().getBytes(charsetName);
		// name string size
		size += 4;
		// name string
		size += name.length;
		// arguments size
		int size1 = tm.getArgument().size();
		size += 4;
		// arguments
		for (int i = 0; i < size1; ++i) {
			Argument arg = tm.getArgument().get(i);
			// values size
			int size2 = arg.getValue().size();
			size += 4;
			// values
			for (int j = 0; j < size2; ++j) {
				byte[] value = arg.getValue().get(j).getBytes(charsetName);
				// value string size
				size += 4;
				// value string
				size += value.length;
			}
		}

		return size;
	}

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		TransportMessage tm = (TransportMessage) message;

		int size = getTransportMessageSize(tm);

		ByteBuffer buffer = ByteBuffer.allocate(size, false);

		buffer.putInt(size);

		// id
		buffer.putInt(tm.getId());

		byte[] name = tm.getName().getBytes(charsetName);
		// name string size
		buffer.putInt(name.length);
		// name string
		buffer.put(name);

		// arguments size
		int size1 = tm.getArgument().size();
		buffer.putInt(size1);
		// arguments
		for (int i = 0; i < size1; ++i) {
			Argument arg = tm.getArgument().get(i);
			// value size
			int size2 = arg.getValue().size();
			buffer.putInt(size2);
			// values
			for (int j = 0; j < size2; ++j) {
				byte[] value = arg.getValue().get(j).getBytes(charsetName);
				// value string size
				buffer.putInt(value.length);
				// value string
				buffer.put(value);
			}
		}

		buffer.flip();
		out.write(buffer);
	}
}
