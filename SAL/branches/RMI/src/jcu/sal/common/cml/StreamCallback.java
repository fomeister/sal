package jcu.sal.common.cml;

import java.io.Serializable;

import jcu.sal.common.Response;

public interface StreamCallback extends Serializable {
	public void collect(Response r);
}
