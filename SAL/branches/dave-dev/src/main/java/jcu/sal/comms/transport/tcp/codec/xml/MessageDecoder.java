
package jcu.sal.comms.transport.tcp.codec.xml;

import jcu.sal.comms.TransportMessage;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;

public class MessageDecoder extends CumulativeProtocolDecoder {

    public boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.prefixedDataAvailable(4)) {
			int size = in.getInt();
			int id = in.getInt();

			StringBuffer s = new StringBuffer();

			for (int i = 0; i < size - 4; ++i) {
				s.append(in.getChar());
			}

            out.write(new TransportMessage(s.toString(), id));

            return true;
        } else {
            return false;
        }
    }
}
