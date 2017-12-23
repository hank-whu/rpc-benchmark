package benchmark.rpc.protocol;

import java.io.Serializable;

public class Response implements Serializable {
	private static final long serialVersionUID = -2827803061483152127L;

	private long requestId;
	private byte statusCode;
	private Object result;

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public byte getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(byte statusCode) {
		this.statusCode = statusCode;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Response [requestId=" + requestId + ", statusCode=" + statusCode + ", result=" + result + "]";
	}

}
