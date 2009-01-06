
package jcu.sal.comms.transport.tcp.xml;

import jcu.sal.comms.transport.TransportCommand;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;

public class CommandDecoder extends CumulativeProtocolDecoder {

    public boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.prefixedDataAvailable(4)) {
			int size = in.getInt();
			int id = in.getInt();

			StringBuffer s = new StringBuffer();

			for (int i = 0; i < size - 8; ++i) {
				s.append(in.getChar());
			}

			TransportCommand command = new TransportCommand(s.toString());
			command.setId(id);
            out.write(command);
            return true;
        } else {
            return false;
        }
    }
}
