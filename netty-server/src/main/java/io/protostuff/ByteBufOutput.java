package io.protostuff;

import static io.protostuff.ProtobufOutput.encodeZigZag32;
import static io.protostuff.ProtobufOutput.encodeZigZag64;
import static io.protostuff.WireFormat.WIRETYPE_END_GROUP;
import static io.protostuff.WireFormat.WIRETYPE_FIXED32;
import static io.protostuff.WireFormat.WIRETYPE_FIXED64;
import static io.protostuff.WireFormat.WIRETYPE_LENGTH_DELIMITED;
import static io.protostuff.WireFormat.WIRETYPE_START_GROUP;
import static io.protostuff.WireFormat.WIRETYPE_VARINT;
import static io.protostuff.WireFormat.makeTag;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.KryoException;

import io.netty.buffer.ByteBuf;

public class ByteBufOutput implements Output {

	public ByteBuf byteBuf;

	public ByteBufOutput(final ByteBuf buffer) {
		this.byteBuf = buffer;
	}

	private void writeVarInt(int value) throws KryoException {

		if (value >>> 7 == 0) {
			byteBuf.writeByte((byte) value);
			return;
		}

		if (value >>> 14 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7));
			return;
		}

		if (value >>> 21 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7 | 0x80));
			byteBuf.writeByte((byte) (value >>> 14));
			return;
		}

		if (value >>> 28 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7 | 0x80));
			byteBuf.writeByte((byte) (value >>> 14 | 0x80));
			byteBuf.writeByte((byte) (value >>> 21));
			return;
		}

		byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
		byteBuf.writeByte((byte) (value >>> 7 | 0x80));
		byteBuf.writeByte((byte) (value >>> 14 | 0x80));
		byteBuf.writeByte((byte) (value >>> 21 | 0x80));
		byteBuf.writeByte((byte) (value >>> 28));
		return;
	}

	private void writeVarLong(long value) throws KryoException {

		if (value >>> 7 == 0) {
			byteBuf.writeByte((byte) value);
			return;
		}

		if (value >>> 14 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7));
			return;
		}

		if (value >>> 21 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7 | 0x80));
			byteBuf.writeByte((byte) (value >>> 14));
			return;
		}

		if (value >>> 28 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7 | 0x80));
			byteBuf.writeByte((byte) (value >>> 14 | 0x80));
			byteBuf.writeByte((byte) (value >>> 21));
			return;
		}

		if (value >>> 35 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7 | 0x80));
			byteBuf.writeByte((byte) (value >>> 14 | 0x80));
			byteBuf.writeByte((byte) (value >>> 21 | 0x80));
			byteBuf.writeByte((byte) (value >>> 28));
			return;
		}

		if (value >>> 42 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7 | 0x80));
			byteBuf.writeByte((byte) (value >>> 14 | 0x80));
			byteBuf.writeByte((byte) (value >>> 21 | 0x80));
			byteBuf.writeByte((byte) (value >>> 28 | 0x80));
			byteBuf.writeByte((byte) (value >>> 35));
			return;
		}

		if (value >>> 49 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7 | 0x80));
			byteBuf.writeByte((byte) (value >>> 14 | 0x80));
			byteBuf.writeByte((byte) (value >>> 21 | 0x80));
			byteBuf.writeByte((byte) (value >>> 28 | 0x80));
			byteBuf.writeByte((byte) (value >>> 35 | 0x80));
			byteBuf.writeByte((byte) (value >>> 42));
			return;
		}

		if (value >>> 56 == 0) {
			byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
			byteBuf.writeByte((byte) (value >>> 7 | 0x80));
			byteBuf.writeByte((byte) (value >>> 14 | 0x80));
			byteBuf.writeByte((byte) (value >>> 21 | 0x80));
			byteBuf.writeByte((byte) (value >>> 28 | 0x80));
			byteBuf.writeByte((byte) (value >>> 35 | 0x80));
			byteBuf.writeByte((byte) (value >>> 42 | 0x80));
			byteBuf.writeByte((byte) (value >>> 49));
			return;
		}

		byteBuf.writeByte((byte) ((value & 0x7F) | 0x80));
		byteBuf.writeByte((byte) (value >>> 7 | 0x80));
		byteBuf.writeByte((byte) (value >>> 14 | 0x80));
		byteBuf.writeByte((byte) (value >>> 21 | 0x80));
		byteBuf.writeByte((byte) (value >>> 28 | 0x80));
		byteBuf.writeByte((byte) (value >>> 35 | 0x80));
		byteBuf.writeByte((byte) (value >>> 42 | 0x80));
		byteBuf.writeByte((byte) (value >>> 49 | 0x80));
		byteBuf.writeByte((byte) (value >>> 56));
		return;
	}

	@Override
	public void writeInt32(int fieldNumber, int value, boolean repeated) throws IOException {
		if (value < 0) {
			writeVarInt(makeTag(fieldNumber, WIRETYPE_VARINT));
			writeVarLong(value);
		} else {
			writeVarInt(makeTag(fieldNumber, WIRETYPE_VARINT));
			writeVarInt(value);
		}
	}

	@Override
	public void writeUInt32(int fieldNumber, int value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_VARINT));
		writeVarInt(value);
	}

	@Override
	public void writeSInt32(int fieldNumber, int value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_VARINT));
		writeVarInt(encodeZigZag32(value));
	}

	@Override
	public void writeFixed32(int fieldNumber, int value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_FIXED32));
		byteBuf.writeIntLE(value);
	}

	@Override
	public void writeSFixed32(int fieldNumber, int value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_FIXED32));
		byteBuf.writeIntLE(value);
	}

	@Override
	public void writeInt64(int fieldNumber, long value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_VARINT));
		writeVarLong(value);
	}

	@Override
	public void writeUInt64(int fieldNumber, long value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_VARINT));
		writeVarLong(value);
	}

	@Override
	public void writeSInt64(int fieldNumber, long value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_VARINT));
		writeVarLong(encodeZigZag64(value));
	}

	@Override
	public void writeFixed64(int fieldNumber, long value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_FIXED64));
		byteBuf.writeLongLE(value);
	}

	@Override
	public void writeSFixed64(int fieldNumber, long value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_FIXED64));
		byteBuf.writeLongLE(value);
	}

	@Override
	public void writeFloat(int fieldNumber, float value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_FIXED32));
		byteBuf.writeLongLE(Float.floatToRawIntBits(value));
	}

	@Override
	public void writeDouble(int fieldNumber, double value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_FIXED64));
		byteBuf.writeLongLE(Double.doubleToRawLongBits(value));
	}

	@Override
	public void writeBool(int fieldNumber, boolean value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_VARINT));
		byteBuf.writeByte(value ? (byte) 0x01 : 0x00);
	}

	@Override
	public void writeEnum(int fieldNumber, int number, boolean repeated) throws IOException {
		writeInt32(fieldNumber, number, repeated);
	}

	@Override
	public void writeString(int fieldNumber, CharSequence value, boolean repeated) throws IOException {
		// TODO the original implementation is a lot more complex, is this compatible?
		String str = value.toString();
		byte[] strbytes = str.getBytes("UTF-8");
		writeByteArray(fieldNumber, strbytes, repeated);
	}

	@Override
	public void writeBytes(int fieldNumber, ByteString value, boolean repeated) throws IOException {
		writeByteArray(fieldNumber, value.getBytes(), repeated);
	}

	@Override
	public void writeByteArray(int fieldNumber, byte[] bytes, boolean repeated) throws IOException {
		writeByteRange(false, fieldNumber, bytes, 0, bytes.length, repeated);
	}

	@Override
	public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length,
			boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED));
		writeVarInt(length);
		byteBuf.writeBytes(value, offset, length);
	}

	@Override
	public <T> void writeObject(final int fieldNumber, final T value, final Schema<T> schema, final boolean repeated)
			throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_START_GROUP));
		schema.writeTo(this, value);
		writeVarInt(makeTag(fieldNumber, WIRETYPE_END_GROUP));
	}

	@Override
	public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated) throws IOException {
		writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(), value.remaining(),
				repeated);
	}

	public void writeBytes(int fieldNumber, ByteBuf value, boolean repeated) throws IOException {
		writeVarInt(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED));
		writeVarInt(value.readableBytes());

		byteBuf.writeBytes(value);
	}

}
