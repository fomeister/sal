
package jcu.sal.comms.transport.tcp;

import jcu.sal.comms.TransportMessage;
import jcu.sal.comms.MessageProcessor;
import jcu.sal.comms.transport.ServerTransport;
import jcu.sal.comms.listeners.TransportResponseListener;
import jcu.sal.comms.listeners.TransportResponseListenerAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

public class TcpServerTransport extends IoHandlerAdapter implements ServerTransport {

	private int port;
	private ProtocolCodecFactory codecFactory = null;

	private SocketAcceptor acceptor = null;
	private MessageProcessor processor = null;

	public TcpServerTransport() {
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
		this.codecFactory = codecFactory;
	}

	public ProtocolCodecFactory getCodecFactory() {
		return codecFactory;
	}

	public void setup() throws IOException {
		if (acceptor == null) {
			acceptor = new SocketAcceptor();

			SocketAcceptorConfig cfg = new SocketAcceptorConfig();
			cfg.setReuseAddress(true);
			cfg.getFilterChain().addLast("protocol", new ProtocolCodecFilter(codecFactory));

			acceptor.bind(new InetSocketAddress(port), this, cfg);
		}
	}

	public void shutdown() {
		if (acceptor != null) {
			acceptor.unbindAll();
			acceptor = null;
		}
	}

	public void setProcessor(MessageProcessor processor) {
		this.processor = processor;
	}

	public void messageRecieved(IoSession session, Object message) {
		TransportMessage tm = (TransportMessage) message;
		processor.process(tm, new TransportResponseListenerAdapter(new IoSessionResponseListener(session), tm.getId()));
	}

	public class IoSessionResponseListener implements TransportResponseListener {

		private IoSession session;

		public IoSessionResponseListener(IoSession session) {
			this.session = session;
		}

		public void receivedResponse(TransportMessage message) {
			session.write(message);
		}
	}
}
