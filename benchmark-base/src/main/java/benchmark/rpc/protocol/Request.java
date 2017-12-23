package benchmark.rpc.protocol;

import java.io.Serializable;
import java.util.Arrays;

public class Request implements Serializable {
	private static final long serialVersionUID = 7798556948864269597L;

	private long requestId;
	private int serviceId;
	private Object[] params;

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "Request [requestId=" + requestId + ", serviceId=" + serviceId + ", params=" + Arrays.toString(params)
				+ "]";
	}

}
