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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

import org.xnio.IoUtils;

import io.undertow.connector.ByteBufferPool;
import io.undertow.connector.PooledByteBuffer;

/**
 * 
 * @author hank.whu@gmail.com
 *
 */
public final class PooledByteBufferOutputStream extends OutputStream {
	private final ByteBufferPool byteBufferPool;
	private ArrayList<PooledByteBuffer> pooledList = new ArrayList<>(4);
	private int index = -1;
	private ByteBuffer current;

	public PooledByteBufferOutputStream(ByteBufferPool byteBufferPool) {
		this.byteBufferPool = byteBufferPool;
	}

	public PooledByteBuffer[] getPooledByteBuffers() {
		PooledByteBuffer[] buffers = new PooledByteBuffer[pooledList.size()];
		return pooledList.toArray(buffers);
	}

	public ByteBuffer[] getByteBuffers() {
		ByteBuffer[] buffers = new ByteBuffer[pooledList.size()];

		for (int i = 0; i < pooledList.size(); i++) {
			buffers[i] = pooledList.get(i).getBuffer();
		}

		return buffers;
	}

	public void flip() {
		for (int i = 0; i < pooledList.size(); i++) {
			PooledByteBuffer pooled = pooledList.get(i);
			pooled.getBuffer().flip();
		}
	}

	public void clear() {
		for (int i = 0; i < pooledList.size(); i++) {
			PooledByteBuffer pooled = pooledList.get(i);
			pooled.getBuffer().clear();
		}

		current = null;
		index = -1;
	}

	public void release() {
		for (int i = 0; i < pooledList.size(); i++) {
			PooledByteBuffer pooled = pooledList.get(i);
			IoUtils.safeClose(pooled);
		}

		pooledList.clear();
	}

	@Override
	public void write(final byte[] bytes, int offset, int length) throws IOException {
		Objects.requireNonNull(bytes, "bytes is null");

		if (length <= 0) {
			return;
		}

		if (current == null) {
			alloc();
		}

		while (length > 0) {
			if (!current.hasRemaining()) {
				alloc();
			}

			int writeLength = Math.min(current.remaining(), length);
			current.put(bytes, offset, writeLength);

			length -= writeLength;
			offset += writeLength;
		}
	}

	public void write(String str) throws IOException {
		write(str.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		write(bytes, 0, bytes.length);
	}

	@Override
	public void write(int b) throws IOException {
		if (current == null || !current.hasRemaining()) {
			alloc();
		}

		current.put((byte) b);
	}

	private void alloc() {
		index++;

		if (pooledList.size() - 1 > index) {
			current = pooledList.get(index).getBuffer();
			return;
		}

		PooledByteBuffer pooled = byteBufferPool.allocate();
		current = pooled.getBuffer();
		pooledList.add(pooled);
	}

}
