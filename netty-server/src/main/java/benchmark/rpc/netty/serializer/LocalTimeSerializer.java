package benchmark.rpc.netty.serializer;

import java.time.LocalTime;

import io.netty.buffer.ByteBuf;

public class LocalTimeSerializer implements Serializer<LocalTime> {

	@Override
	public void write(ByteBuf byteBuf, LocalTime localTime) {
		byteBuf.writeByte(localTime.getHour());
		byteBuf.writeByte(localTime.getMinute());
		byteBuf.writeByte(localTime.getSecond());
		byteBuf.writeInt(localTime.getNano());
	}

	@Override
	public LocalTime read(ByteBuf byteBuf) {
		int hour = byteBuf.readByte();
		int minute = byteBuf.readByte();
		int second = byteBuf.readByte();
		int nanoOfSecond = byteBuf.readInt();

		return LocalTime.of(hour, minute, second, nanoOfSecond);
	}

	@Override
	public Class<LocalTime> typeClass() {
		return LocalTime.class;
	}

}
