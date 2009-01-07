
package jcu.sal.comms.transport.tcp;

import jcu.sal.comms.TransportMessage;
import jcu.sal.comms.transport.ClientTransport;
import jcu.sal.comms.listeners.TransportResponseListener;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;

public class TcpClientTransport extends IoHandlerAdapter implements ClientTransport {

	public static final int CONNECT_TIMEOUT = 3000;

	private String host;
	private int port;
	private ProtocolCodecFactory codecFactory = null;

	private TransportResponseListener rl = null;
	private IoSession session = null;

	public TcpClientTransport() {
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
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

	public void setup() throws RuntimeIOException {
		if (session == null) {
			SocketConnector connector = new SocketConnector();
	        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));

			ConnectFuture connectFuture = connector.connect(new InetSocketAddress(host,port), this);
			connectFuture.join(CONNECT_TIMEOUT);
			session = connectFuture.getSession();
		}
	}

	public void shutdown() {
		if (session != null) {
			session.close().join(CONNECT_TIMEOUT);
			session = null;
		}
	}

	public void send(TransportMessage tm) {
		if (session != null) {
			session.write(tm);
		}
	}

	public void setResponseListener(TransportResponseListener rl) {
		this.rl = rl;
	}

	public void messageRecieved(IoSession session, Object message) {
		TransportMessage tm = (TransportMessage) message;
		rl.receivedResponse(tm);
	}
}
