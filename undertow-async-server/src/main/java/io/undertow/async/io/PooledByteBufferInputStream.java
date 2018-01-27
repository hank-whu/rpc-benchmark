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
package io.undertow.async.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import io.undertow.connector.PooledByteBuffer;

/**
 * 
 * @author hank.whu@gmail.com
 *
 */
public final class PooledByteBufferInputStream extends InputStream {
	public static final PooledByteBuffer[] EMPTY_BUFFERS = new PooledByteBuffer[0];

	private PooledByteBuffer[] buffers;

	private int index = -1;
	private ByteBuffer current;

	public PooledByteBufferInputStream(PooledByteBuffer[] buffers) {
		Objects.requireNonNull(buffers);

		for (int i = 0; i < buffers.length; i++) {
			if (!buffers[i].isOpen()) {
				throw new IllegalAccessError("buffers must all open");
			}
		}

		this.buffers = buffers;
		next();
	}

	private void next() {
		if (buffers.length > ++index) {
			current = buffers[index].getBuffer();
			return;
		}

		current = null;
	}

	public void flip() {
		for (int i = 0; i < buffers.length; i++) {
			buffers[i].getBuffer().flip();
		}
	}

	@Override
	public int read() throws IOException {
		while (current != null && !current.hasRemaining()) {
			next();
		}

		if (current == null) {
			return -1;
		}

		return current.get() & 0xFF;
	}

	@Override
	public int read(byte[] bytes) throws IOException {
		return read(bytes, 0, bytes.length);
	}

	@Override
	public int read(byte[] bytes, int off, int len) throws IOException {
		if (bytes == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > bytes.length - off) {
			throw new IndexOutOfBoundsException();
		}

		int readLength = 0;

		while (current != null && len > 0) {
			while (current != null && !current.hasRemaining()) {
				next();
			}

			if (current == null) {
				break;
			}

			int currentRead = Math.min(current.remaining(), len);
			current.get(bytes, off, currentRead);

			readLength += currentRead;
			off += currentRead;
			len -= currentRead;
		}

		return readLength == 0 ? -1 : readLength;
	}

	@Override
	public long skip(long n) throws IOException {
		long remaining = n;

		while (current != null && remaining > 0) {
			while (current != null && !current.hasRemaining()) {
				next();
			}

			if (current == null) {
				break;
			}

			int currentSkip = (int) Math.min(current.remaining(), remaining);
			current.position(current.position() + currentSkip);

			remaining -= currentSkip;
		}

		return n - remaining;
	}

	@Override
	public int available() throws IOException {
		int remaining = 0;

		for (int i = 0; i < buffers.length; i++) {
			remaining += buffers[i].getBuffer().remaining();
		}

		return remaining;
	}

	@Override
	public void close() throws IOException {
		if (buffers == null || buffers == EMPTY_BUFFERS) {
			return;
		}

		for (int i = 0; i < buffers.length; i++) {
			buffers[i].close();
		}

		buffers = EMPTY_BUFFERS;
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}
