package benchmark.rpc.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientUtils {

	public static final int SOCKET_TIMEOUT = 15000;
	public static final int CONNECTION_REQUEST_TIMEOUT = 30000;
	public static final int CONNECT_TIMEOUT = 30000;

	public static CloseableHttpClient createHttpClient(int concurrency) {
		HttpClientBuilder builder = HttpClientBuilder.create();

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultMaxPerRoute(concurrency);
		connManager.setMaxTotal(concurrency);

		RequestConfig requestConfig = RequestConfig.custom()//
				.setAuthenticationEnabled(true)//
				.setSocketTimeout(SOCKET_TIMEOUT)//
				.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)//
				.setConnectTimeout(CONNECT_TIMEOUT)//
				.setRedirectsEnabled(true)//
				.setRelativeRedirectsAllowed(true)//
				.setMaxRedirects(15)//
				.build();

		SocketConfig socketConfig = SocketConfig.custom()//
				.setSoKeepAlive(true)//
				.setSoReuseAddress(true)//
				.build();

		builder.setConnectionManager(connManager);
		builder.setDefaultSocketConfig(socketConfig);
		builder.setDefaultRequestConfig(requestConfig);

		return builder.build();
	}
}
