
package jcu.sal.comms.transport.tcp.codec.binary;

import jcu.sal.comms.TransportMessage;
import jcu.sal.xml.Argument;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;

public class MessageDecoder extends CumulativeProtocolDecoder {

	private String charsetName = "UTF-8";

	public MessageDecoder() {
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

    public boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.prefixedDataAvailable(4)) {

			int size = in.getInt();

			TransportMessage m = new TransportMessage();

			// id
			m.setId(in.getInt());

			// name string size
			byte[] name = new byte[in.getInt()];
			// name string
			in.get(name);
			m.setName(new String(name, charsetName));

			// arguments size
			int size1 = in.getInt();
			//arguments
			for (int i = 0; i < size1; ++i) {
				Argument arg = new Argument();
				//value size
				int size2 = in.getInt();
				for (int j = 0; j < size2; ++j) {
					// value string size
					byte[] value = new byte[in.getInt()];
					// value string
					in.get(value);
					arg.getValue().add(new String(value, charsetName));
				}
				m.getArgument().add(arg);
			}

            out.write(m);

            return true;
        } else {
            return false;
        }
    }
}
