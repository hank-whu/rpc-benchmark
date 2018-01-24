package benchmark.rpc.aeron.server;

public class Config {
	/**
	 * The channel (an endpoint identifier) to send the message to
	 */
	public static final String PUBLISH_CHANNEL = "aeron:udp?endpoint=benchmark-client:40123";

	/**
	 * The channel (an endpoint identifier) to recive the message
	 */
	public static final String SUBSCRIBE_CHANNEL = "aeron:udp?endpoint=benchmark-server:40124";

	/**
	 * A unique identifier for a stream within a channel. Stream ID 0 is reserved
	 * for internal use and should not be used by applications.
	 */
	public static final int STREAM_ID = 10;

	/**
	 * 消息大小
	 */
	public static final int MAX_MESSAGE_LENGTH = 2 * 1024 * 1024;
}
