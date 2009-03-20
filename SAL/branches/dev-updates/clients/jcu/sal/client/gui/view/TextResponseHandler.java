package jcu.sal.client.gui.view;

import java.io.IOException;

import jcu.sal.common.Response;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.SensorControlException;


public class TextResponseHandler implements ResponseHandler{
	private ClientView view;
	
	public TextResponseHandler(Context c, ClientView v){
		view = v;
	}

	@Override
	public void close() {}

	@Override
	public void collect(Response r) throws IOException {
		try {
			view.addLog("Command returned "+r.getString());			
		} catch (SensorControlException e) {
			view.addLog("Stream from sensor "+r.getSID()+" terminated");
			if(!e.getClass().equals(ClosedStreamException.class))
				view.addLog("Error: "+e.getMessage());
		}	
	}

}
