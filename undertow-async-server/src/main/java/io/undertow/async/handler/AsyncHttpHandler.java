/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.undertow.async.handler;

import static io.undertow.async.util.UnsafeUtils.unsafe;
import static org.xnio.Bits.intBitMask;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.xnio.IoUtils;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

import io.undertow.UndertowLogger;
import io.undertow.async.io.PooledByteBufferInputStream;
import io.undertow.async.io.PooledByteBufferOutputStream;
import io.undertow.connector.ByteBufferPool;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.io.AsyncSenderImpl;
import io.undertow.io.Receiver.RequestToLargeException;
import io.undertow.io.Sender;
import io.undertow.server.Connectors;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

/**
 * unsafe and only support few versions
 * 
 * @author hank.whu@gmail.com
 *
 */
public abstract class AsyncHttpHandler implements HttpHandler {

	private static final PooledByteBuffer[] EMPTY_BUFFERS = PooledByteBufferInputStream.EMPTY_BUFFERS;

	private static final int MASK_RESPONSE_CODE = intBitMask(0, 9);

	private static final long exchangeResponseChannelFieldOffset;
	private static final long exchangeStatusCodeFieldOffset;
	private static final long asyncSenderImplChannelFieldOffset;
	private static final long asyncSenderImplPooledBuffersFieldOffset;

	@Override
	final public void handleRequest(HttpServerExchange exchange) throws Exception {
		HttpString httpMethod = exchange.getRequestMethod();

		if (httpMethod == Methods.GET) {
			internalAsyncRequest(exchange, null);
			return;
		}

		final String contentLengthString = exchange.getRequestHeaders().getFirst(Headers.CONTENT_LENGTH);
		final long contentLength;

		if (contentLengthString != null) {
			contentLength = Long.parseLong(contentLengthString);

			if (contentLength > Integer.MAX_VALUE) {
				throw new RequestToLargeException();
			}
		} else {
			contentLength = -1;
		}

		PooledByteBuffer buffer = exchange.getConnection().getByteBufferPool().allocate();
		final ArrayList<PooledByteBuffer> bufferList;
		if (contentLength > 0) {
			int bufferLength = buffer.getBuffer().capacity();
			long count = (contentLength + bufferLength - 1) / bufferLength;// ceil div
			bufferList = new ArrayList<>((int) count);
		} else {
			bufferList = new ArrayList<>(8);// usually enough
		}

		final StreamSourceChannel channel = exchange.getRequestChannel();

		// copy from io.undertow.server.handlers.RequestBufferingHandler
		try {
			do {
				ByteBuffer b = buffer.getBuffer();
				int r = channel.read(b);

				if (r == -1) { // TODO: listener read
					if (b.position() == 0) {
						buffer.close();
					} else {
						b.flip();
						bufferList.add(buffer);
					}

					break;
				} else if (r == 0) {
					final PooledByteBuffer finalBuffer = buffer;

					channel.getReadSetter().set(requestChannel -> {
						PooledByteBuffer pooledByteBuffer = finalBuffer;
						try {
							do {
								ByteBuffer byteBuffer = pooledByteBuffer.getBuffer();
								int readLength = requestChannel.read(byteBuffer);

								if (readLength == -1) { // TODO: listener read
									if (byteBuffer.position() == 0) {
										pooledByteBuffer.close();
									} else {
										byteBuffer.flip();
										bufferList.add(pooledByteBuffer);
									}

									Connectors.resetRequestChannel(exchange);
									requestChannel.getReadSetter().set(null);

									return;
								} else if (readLength == 0) {
									return;
								} else if (!byteBuffer.hasRemaining()) {
									byteBuffer.flip();
									bufferList.add(pooledByteBuffer);

									pooledByteBuffer = exchange.getConnection().getByteBufferPool().allocate();
								}
							} while (true);
						} catch (Throwable t) {
							if (t instanceof IOException) {
								UndertowLogger.REQUEST_IO_LOGGER.ioException((IOException) t);
							} else {
								UndertowLogger.REQUEST_IO_LOGGER.handleUnexpectedFailure(t);
							}

							release(bufferList);

							if (pooledByteBuffer != null && pooledByteBuffer.isOpen()) {
								IoUtils.safeClose(pooledByteBuffer);
							}

							exchange.endExchange();
							return;
						}
					});

					channel.resumeReads();
					internalAsyncRequest(exchange, bufferList);

					return;
				} else if (!b.hasRemaining()) {
					b.flip();
					bufferList.add(buffer);
					buffer = exchange.getConnection().getByteBufferPool().allocate();
				}
			} while (true);

			Connectors.resetRequestChannel(exchange);
			internalAsyncRequest(exchange, bufferList);
		} catch (Throwable e) {
			release(bufferList);

			if (buffer != null && buffer.isOpen()) {
				IoUtils.safeClose(buffer);
			}

			throw e;
		}
	}

	private void internalAsyncRequest(final HttpServerExchange exchange, ArrayList<PooledByteBuffer> bufferList)
			throws Exception {
		// ensure exchange not end
		getResponseChannel(exchange).resumeWrites();

		PooledByteBuffer[] buffers = EMPTY_BUFFERS;

		if (bufferList != null && bufferList.size() > 0) {
			buffers = new PooledByteBuffer[bufferList.size()];
			buffers = bufferList.toArray(buffers);
		}

		handleAsyncRequest(exchange, new PooledByteBufferInputStream(buffers));
	}

	/**
	 * will be called after get all request data.</br>
	 * 
	 * 
	 * @param exchange
	 * 
	 * @param content
	 *            must close it
	 * 
	 * @throws Exception
	 */
	protected abstract void handleAsyncRequest(HttpServerExchange exchange, PooledByteBufferInputStream content)
			throws Exception;

	protected byte[] readBytesAndClose(PooledByteBufferInputStream content) {
		try {
			byte[] bytes = new byte[content.available()];
			content.read(bytes);
			return bytes;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {// must close it
				content.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * response
	 *
	 * @param exchange
	 * @param statusCode
	 * @param msg
	 */
	protected final void send(HttpServerExchange exchange, int statusCode, String msg) {
		ByteBufferPool pool = exchange.getConnection().getByteBufferPool();
		PooledByteBufferOutputStream output = new PooledByteBufferOutputStream(pool);

		try {
			output.write(msg);
		} catch (IOException e) {
			UndertowLogger.REQUEST_IO_LOGGER.ioException(e);
		}

		send(exchange, statusCode, output);
	}

	/**
	 * response
	 * 
	 * @param exchange
	 * @param statusCode
	 * @param output
	 *            auto release
	 */
	protected final void send(HttpServerExchange exchange, int statusCode, PooledByteBufferOutputStream output) {
		try {
			output.flip();

			StreamSinkChannel channel = getResponseChannel(exchange);
			Sender sender = exchange.getResponseSender();

			setStatusCode(exchange, statusCode);
			setResponseChannel(sender, channel);
			setPooledBuffers(sender, output.getPooledByteBuffers());

			sender.send(output.getByteBuffers());
		} catch (Throwable t) {
			UndertowLogger.REQUEST_IO_LOGGER.handleUnexpectedFailure(t);
		}
	}

	/**
	 * return the buffer to the buffer pool
	 *
	 * @param bufferList
	 */
	private void release(ArrayList<PooledByteBuffer> bufferList) {
		if (bufferList == null || bufferList.size() == 0) {
			return;
		}

		for (int i = 0; i < bufferList.size(); ++i) {
			IoUtils.safeClose(bufferList.get(i));
		}
	}

	/**
	 * force get response channel
	 * 
	 * @param exchange
	 * @return
	 */
	private StreamSinkChannel getResponseChannel(HttpServerExchange exchange) {
		StreamSinkChannel channel = (StreamSinkChannel) unsafe().getObject(exchange,
				exchangeResponseChannelFieldOffset);

		if (channel == null) {
			channel = exchange.getResponseChannel();
		}

		return channel;
	}

	/**
	 * force set status code
	 * 
	 * @param exchange
	 * @param statusCode
	 */
	private void setStatusCode(HttpServerExchange exchange, int statusCode) {
		int oldVal = unsafe().getInt(exchange, exchangeStatusCodeFieldOffset);
		int newVal = oldVal & ~MASK_RESPONSE_CODE | statusCode & MASK_RESPONSE_CODE;

		unsafe().getAndSetInt(exchange, exchangeStatusCodeFieldOffset, newVal);
	}

	/**
	 * force set response channel
	 * 
	 * @param sender
	 * @param channel
	 */
	private void setResponseChannel(Sender sender, StreamSinkChannel channel) {
		if (!(sender instanceof AsyncSenderImpl)) {
			throw new RuntimeException("only support AsyncSenderImpl");
		}

		unsafe().getAndSetObject(sender, asyncSenderImplChannelFieldOffset, channel);
	}

	/**
	 * just for async release
	 * 
	 * @param sender
	 * @param pooledBuffers
	 *            will be released
	 */
	private void setPooledBuffers(Sender sender, PooledByteBuffer[] pooledBuffers) {
		if (!(sender instanceof AsyncSenderImpl)) {
			throw new RuntimeException("only support AsyncSenderImpl");
		}

		unsafe().getAndSetObject(sender, asyncSenderImplPooledBuffersFieldOffset, pooledBuffers);
	}

	static {
		try {
			Field field = HttpServerExchange.class.getDeclaredField("responseChannel");
			exchangeResponseChannelFieldOffset = unsafe().objectFieldOffset(field);
		} catch (Throwable e) {
			throw new RuntimeException("cannot find HttpServerExchange.responseChannel", e);
		}

		try {
			Field field = HttpServerExchange.class.getDeclaredField("state");
			exchangeStatusCodeFieldOffset = unsafe().objectFieldOffset(field);
		} catch (Throwable e) {
			throw new RuntimeException("cannot find HttpServerExchange.state", e);
		}

		try {
			Field field = AsyncSenderImpl.class.getDeclaredField("channel");
			asyncSenderImplChannelFieldOffset = unsafe().objectFieldOffset(field);
		} catch (Throwable e) {
			throw new RuntimeException("cannot find AsyncSenderImpl.channel", e);
		}

		try {
			Field field = AsyncSenderImpl.class.getDeclaredField("pooledBuffers");
			asyncSenderImplPooledBuffersFieldOffset = unsafe().objectFieldOffset(field);
		} catch (Throwable e) {
			throw new RuntimeException("cannot find AsyncSenderImpl.pooledBuffers", e);
		}
	}

}
