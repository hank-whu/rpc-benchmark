//========================================================================
//Copyright 2007-2010 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package io.protostuff;

import static io.protostuff.WireFormat.WIRETYPE_END_GROUP;
import static io.protostuff.WireFormat.WIRETYPE_FIXED32;
import static io.protostuff.WireFormat.WIRETYPE_FIXED64;
import static io.protostuff.WireFormat.WIRETYPE_LENGTH_DELIMITED;
import static io.protostuff.WireFormat.WIRETYPE_START_GROUP;
import static io.protostuff.WireFormat.WIRETYPE_TAIL_DELIMITER;
import static io.protostuff.WireFormat.WIRETYPE_VARINT;
import static io.protostuff.WireFormat.getTagFieldNumber;
import static io.protostuff.WireFormat.getTagWireType;
import static io.protostuff.WireFormat.makeTag;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;
import io.protostuff.ByteString;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.ProtobufException;
import io.protostuff.Schema;
import io.protostuff.StringSerializer.STRING;
import io.protostuff.UninitializedMessageException;

/**
 * Reads and decodes protocol buffer message fields from an internal byte array
 * buffer. This object is re-usable via doing a reset on the byte array position
 * and length. This is used internally by {@link IOUtil} where it catches
 * {@link ArrayIndexOutOfBoundsException} when a message is truncated.
 * 
 * @author David Yu
 * @created Jun 22, 2010
 */
public final class ByteBufInput implements Input {

	static final int TAG_TYPE_BITS = 3;
	static final int TAG_TYPE_MASK = (1 << TAG_TYPE_BITS) - 1;

	private final ByteBuf buffer;
	// private final byte[] buffer;
	private int lastTag = 0;
	// private int offset, limit, lastTag = 0;
	private int packedLimit = 0;

	/**
	 * If true, the nested messages are group-encoded
	 */
	public final boolean decodeNestedMessageAsGroup;

	/**
	 * An input for a ByteBuffer
	 * 
	 * @param buffer
	 *            the buffer to read from, it will be sliced
	 * @param protostuffMessage
	 *            if we are parsing a protostuff (true) or protobuf (false) message
	 */
	public ByteBufInput(ByteBuf buffer, boolean protostuffMessage) {
		this.buffer = buffer;
		this.decodeNestedMessageAsGroup = protostuffMessage;
	}

	/**
	 * Resets the offset and the limit of the internal buffer.
	 */
	public ByteBufInput reset(int offset, int len) {
		return this;
	}

	/**
	 * Returns the current offset (the position).
	 */
	public int currentOffset() {
		return buffer.readerIndex();
	}

	/**
	 * Returns the current limit (the end index).
	 */
	public int currentLimit() {
		return buffer.writerIndex();
	}

	/**
	 * Return true if currently reading packed field
	 */
	public boolean isCurrentFieldPacked() {
		return packedLimit != 0 && packedLimit != buffer.readerIndex();
	}

	/**
	 * Returns the last tag.
	 */
	public int getLastTag() {
		return lastTag;
	}

	/**
	 * Attempt to read a field tag, returning zero if we have reached EOF. Protocol
	 * message parsers use this to read tags, since a protocol message may legally
	 * end wherever a tag occurs, and zero is not a valid tag number.
	 */
	public int readTag() throws IOException {
		if (buffer.readableBytes() == 0) {
			lastTag = 0;
			return 0;
		}

		final int tag = readRawVarint32();
		if (tag >>> TAG_TYPE_BITS == 0) {
			// If we actually read zero, that's not a valid tag.
			throw invalidTag();
		}
		lastTag = tag;
		return tag;
	}

	/**
	 * Verifies that the last call to readTag() returned the given tag value. This
	 * is used to verify that a nested group ended with the correct end tag.
	 * 
	 * @throws ProtobufException
	 *             {@code value} does not match the last tag.
	 */
	public void checkLastTagWas(final int value) throws ProtobufException {
		if (lastTag != value) {
			throw invalidEndTag();
		}
	}

	/**
	 * Reads and discards a single field, given its tag value.
	 * 
	 * @return {@code false} if the tag is an endgroup tag, in which case nothing is
	 *         skipped. Otherwise, returns {@code true}.
	 */
	public boolean skipField(final int tag) throws IOException {
		switch (getTagWireType(tag)) {
		case WIRETYPE_VARINT:
			readInt32();
			return true;
		case WIRETYPE_FIXED64:
			readRawLittleEndian64();
			return true;
		case WIRETYPE_LENGTH_DELIMITED:
			final int size = readRawVarint32();
			if (size < 0)
				throw negativeSize();
			buffer.readerIndex(buffer.readerIndex() + size);
			// offset += size;
			return true;
		case WIRETYPE_START_GROUP:
			skipMessage();
			checkLastTagWas(makeTag(getTagFieldNumber(tag), WIRETYPE_END_GROUP));
			return true;
		case WIRETYPE_END_GROUP:
			return false;
		case WIRETYPE_FIXED32:
			readRawLittleEndian32();
			return true;
		default:
			throw invalidWireType();
		}
	}

	/**
	 * Reads and discards an entire message. This will read either until EOF or
	 * until an endgroup tag, whichever comes first.
	 */
	public void skipMessage() throws IOException {
		while (true) {
			final int tag = readTag();
			if (tag == 0 || !skipField(tag)) {
				return;
			}
		}
	}

	@Override
	public <T> void handleUnknownField(int fieldNumber, Schema<T> schema) throws IOException {
		skipField(lastTag);
	}

	@Override
	public <T> int readFieldNumber(Schema<T> schema) throws IOException {
		if (buffer.readableBytes() == 0) {
			lastTag = 0;
			return 0;
		}

		// are we reading packed field?
		if (isCurrentFieldPacked()) {
			if (packedLimit < buffer.readerIndex())
				throw misreportedSize();

			// Return field number while reading packed field
			return lastTag >>> TAG_TYPE_BITS;
		}

		packedLimit = 0;
		final int tag = readRawVarint32();
		final int fieldNumber = tag >>> TAG_TYPE_BITS;
		if (fieldNumber == 0) {
			if (decodeNestedMessageAsGroup && WIRETYPE_TAIL_DELIMITER == (tag & TAG_TYPE_MASK)) {
				// protostuff's tail delimiter for streaming
				// 2 options: length-delimited or tail-delimited.
				lastTag = 0;
				return 0;
			}
			// If we actually read zero, that's not a valid tag.
			throw invalidTag();
		}
		if (decodeNestedMessageAsGroup && WIRETYPE_END_GROUP == (tag & TAG_TYPE_MASK)) {
			lastTag = 0;
			return 0;
		}

		lastTag = tag;
		return fieldNumber;
	}

	/**
	 * Check if this field have been packed into a length-delimited field. If so,
	 * update internal state to reflect that packed fields are being read.
	 * 
	 * @throws IOException
	 */
	private void checkIfPackedField() throws IOException {
		// Do we have the start of a packed field?
		if (packedLimit == 0 && getTagWireType(lastTag) == WIRETYPE_LENGTH_DELIMITED) {
			final int length = readRawVarint32();
			if (length < 0)
				throw negativeSize();

			if (length > buffer.readableBytes())
				throw misreportedSize();

			this.packedLimit = buffer.readerIndex() + length;
		}
	}

	/**
	 * Read a {@code double} field value from the internal buffer.
	 */
	@Override
	public double readDouble() throws IOException {
		checkIfPackedField();
		return Double.longBitsToDouble(readRawLittleEndian64());
	}

	/**
	 * Read a {@code float} field value from the internal buffer.
	 */
	@Override
	public float readFloat() throws IOException {
		checkIfPackedField();
		return Float.intBitsToFloat(readRawLittleEndian32());
	}

	/**
	 * Read a {@code uint64} field value from the internal buffer.
	 */
	@Override
	public long readUInt64() throws IOException {
		checkIfPackedField();
		return readRawVarint64();
	}

	/**
	 * Read an {@code int64} field value from the internal buffer.
	 */
	@Override
	public long readInt64() throws IOException {
		checkIfPackedField();
		return readRawVarint64();
	}

	/**
	 * Read an {@code int32} field value from the internal buffer.
	 */
	@Override
	public int readInt32() throws IOException {
		checkIfPackedField();
		return readRawVarint32();
	}

	/**
	 * Read a {@code fixed64} field value from the internal buffer.
	 */
	@Override
	public long readFixed64() throws IOException {
		checkIfPackedField();
		return readRawLittleEndian64();
	}

	/**
	 * Read a {@code fixed32} field value from the internal buffer.
	 */
	@Override
	public int readFixed32() throws IOException {
		checkIfPackedField();
		return readRawLittleEndian32();
	}

	/**
	 * Read a {@code bool} field value from the internal buffer.
	 */
	@Override
	public boolean readBool() throws IOException {
		checkIfPackedField();
		return buffer.readByte() != 0;
	}

	/**
	 * Read a {@code uint32} field value from the internal buffer.
	 */
	@Override
	public int readUInt32() throws IOException {
		checkIfPackedField();
		return readRawVarint32();
	}

	/**
	 * Read an enum field value from the internal buffer. Caller is responsible for
	 * converting the numeric value to an actual enum.
	 */
	@Override
	public int readEnum() throws IOException {
		checkIfPackedField();
		return readRawVarint32();
	}

	/**
	 * Read an {@code sfixed32} field value from the internal buffer.
	 */
	@Override
	public int readSFixed32() throws IOException {
		checkIfPackedField();
		return readRawLittleEndian32();
	}

	/**
	 * Read an {@code sfixed64} field value from the internal buffer.
	 */
	@Override
	public long readSFixed64() throws IOException {
		checkIfPackedField();
		return readRawLittleEndian64();
	}

	/**
	 * Read an {@code sint32} field value from the internal buffer.
	 */
	@Override
	public int readSInt32() throws IOException {
		checkIfPackedField();
		final int n = readRawVarint32();
		return (n >>> 1) ^ -(n & 1);
	}

	/**
	 * Read an {@code sint64} field value from the internal buffer.
	 */
	@Override
	public long readSInt64() throws IOException {
		checkIfPackedField();
		final long n = readRawVarint64();
		return (n >>> 1) ^ -(n & 1);
	}

	@Override
	public String readString() throws IOException {
		final int length = readRawVarint32();
		if (length < 0)
			throw negativeSize();

		if (buffer.readableBytes() < length)
			throw misreportedSize();

		// if(offset + length > limit)

		if (buffer.hasArray()) {
			final int currPosition = buffer.readerIndex();
			buffer.readerIndex(buffer.readerIndex() + length);
			return STRING.deser(buffer.array(), buffer.arrayOffset() + currPosition, length);
		} else {
			byte[] tmp = new byte[length];
			buffer.readBytes(tmp);
			return STRING.deser(tmp);
		}

		// final int offset = this.offset;

		// this.offset += length;

		// return STRING.deser(buffer, offset, length);
	}

	@Override
	public ByteString readBytes() throws IOException {
		return ByteString.wrap(readByteArray());
	}

	@Override
	public void readBytes(final ByteBuffer bb) throws IOException {
		final int length = readRawVarint32();
		if (length < 0)
			throw negativeSize();

		if (buffer.readableBytes() < length)
			// if(offset + length > limit)
			throw misreportedSize();

		buffer.readBytes(bb);
	}

	@Override
	public byte[] readByteArray() throws IOException {
		final int length = readRawVarint32();
		if (length < 0)
			throw negativeSize();

		if (buffer.readableBytes() < length)
			// if(offset + length > limit)
			throw misreportedSize();

		final byte[] copy = new byte[length];
		buffer.readBytes(copy);
		return copy;
	}

	@Override
	public <T> T mergeObject(T value, final Schema<T> schema) throws IOException {
		if (decodeNestedMessageAsGroup)
			return mergeObjectEncodedAsGroup(value, schema);

		final int length = readRawVarint32();
		if (length < 0)
			throw negativeSize();

		if (buffer.readableBytes() < length)
			throw misreportedSize();

		ByteBuf dup = buffer.slice();
		dup.readerIndex(length);

		// save old limit
		// final int oldLimit = this.limit;

		// this.limit = offset + length;

		if (value == null)
			value = schema.newMessage();
		ByteBufInput nestedInput = new ByteBufInput(dup, decodeNestedMessageAsGroup);
		schema.mergeFrom(nestedInput, value);
		if (!schema.isInitialized(value))
			throw new UninitializedMessageException(value, schema);
		nestedInput.checkLastTagWas(0);
		// checkLastTagWas(0);

		// restore old limit
		// this.limit = oldLimit;

		buffer.readerIndex(buffer.readerIndex() + length);
		return value;
	}

	private <T> T mergeObjectEncodedAsGroup(T value, final Schema<T> schema) throws IOException {
		if (value == null)
			value = schema.newMessage();
		schema.mergeFrom(this, value);
		if (!schema.isInitialized(value))
			throw new UninitializedMessageException(value, schema);
		// handling is in #readFieldNumber
		checkLastTagWas(0);
		return value;
	}

	/**
	 * Reads a var int 32 from the internal byte buffer.
	 */
	public int readRawVarint32() throws IOException {
		byte tmp = buffer.readByte();
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = buffer.readByte()) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = buffer.readByte()) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = buffer.readByte()) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = buffer.readByte()) << 28;
					if (tmp < 0) {
						// Discard upper 32 bits.
						for (int i = 0; i < 5; i++) {
							if (buffer.readByte() >= 0) {
								return result;
							}
						}
						throw malformedVarint();
					}
				}
			}
		}
		return result;
	}

	/**
	 * Reads a var int 64 from the internal byte buffer.
	 */
	public long readRawVarint64() throws IOException {
		// final byte[] buffer = this.buffer;
		// int offset = this.offset;

		int shift = 0;
		long result = 0;
		while (shift < 64) {
			final byte b = buffer.readByte();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				// this.offset = offset;
				return result;
			}
			shift += 7;
		}
		throw malformedVarint();
	}

	/**
	 * Read a 32-bit little-endian integer from the internal buffer.
	 */
	public int readRawLittleEndian32() throws IOException {
		return buffer.readIntLE();
	}

	/**
	 * Read a 64-bit little-endian integer from the internal byte buffer.
	 */
	public long readRawLittleEndian64() throws IOException {
		return buffer.readLongLE();
	}

	@Override
	public void transferByteRangeTo(Output output, boolean utf8String, int fieldNumber, boolean repeated)
			throws IOException {
		final int length = readRawVarint32();
		if (length < 0)
			throw negativeSize();

		if (utf8String) {
			// if it is a UTF string, we have to call the writeByteRange.

			if (buffer.hasArray()) {
				output.writeByteRange(true, fieldNumber, buffer.array(), buffer.arrayOffset() + buffer.readerIndex(),
						length, repeated);
				buffer.readerIndex(buffer.readerIndex() + length);
			} else {
				byte[] bytes = new byte[length];
				buffer.readBytes(bytes);
				output.writeByteRange(true, fieldNumber, bytes, 0, bytes.length, repeated);
			}
		} else {
			// Do the potentially vastly more efficient potential splice call.
			if (buffer.readableBytes() < length)
				throw misreportedSize();

			ByteBuf dup = buffer.slice();
			dup.readerIndex(length);

			if (output instanceof ByteBufOutput) {
				((ByteBufOutput) output).writeBytes(fieldNumber, dup, repeated);
			} else {
				ByteBuffer dst = ByteBuffer.allocate(buffer.readableBytes());
				dup.readBytes(dst);
				output.writeBytes(fieldNumber, dst, repeated);
			}

			buffer.readerIndex(buffer.readerIndex() + length);
		}

		// output.writeByteRange(utf8String, fieldNumber, buffer, offset, length,
		// repeated);
		//
		// offset += length;
	}

	private static final String ERR_TRUNCATED_MESSAGE = "While parsing a protocol message, the input ended unexpectedly "
			+ "in the middle of a field.  This could mean either than the "
			+ "input has been truncated or that an embedded message " + "misreported its own length.";

	/**
	 * Reads a byte array/ByteBuffer value.
	 */
	@Override
	public ByteBuffer readByteBuffer() throws IOException {
		return ByteBuffer.wrap(readByteArray());
	}

	static ProtobufException truncatedMessage(final Throwable cause) {
		return new ProtobufException(ERR_TRUNCATED_MESSAGE, cause);
	}

	static ProtobufException truncatedMessage() {
		return new ProtobufException(ERR_TRUNCATED_MESSAGE);
	}

	static ProtobufException misreportedSize() {
		return new ProtobufException(
				"CodedInput encountered an embedded string or bytes " + "that misreported its size.");
	}

	static ProtobufException negativeSize() {
		return new ProtobufException(
				"CodedInput encountered an embedded string or message " + "which claimed to have negative size.");
	}

	static ProtobufException malformedVarint() {
		return new ProtobufException("CodedInput encountered a malformed varint.");
	}

	static ProtobufException invalidTag() {
		return new ProtobufException("Protocol message contained an invalid tag (zero).");
	}

	static ProtobufException invalidEndTag() {
		return new ProtobufException("Protocol message end-group tag did not match expected tag.");
	}

	static ProtobufException invalidWireType() {
		return new ProtobufException("Protocol message tag had invalid wire type.");
	}

	static ProtobufException recursionLimitExceeded() {
		return new ProtobufException("Protocol message had too many levels of nesting.  May be malicious.  "
				+ "Use CodedInput.setRecursionLimit() to increase the depth limit.");
	}

	static ProtobufException sizeLimitExceeded() {
		return new ProtobufException("Protocol message was too large.  May be malicious.  "
				+ "Use CodedInput.setSizeLimit() to increase the size limit.");
	}
}
