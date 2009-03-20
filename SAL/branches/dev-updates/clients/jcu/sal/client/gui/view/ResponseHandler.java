package jcu.sal.client.gui.view;

import java.io.IOException;

import jcu.sal.common.Response;
import jcu.sal.common.cml.StreamCallback;

public interface ResponseHandler extends StreamCallback{
	public void close();
	public void collect(Response r)  throws IOException ;
}
