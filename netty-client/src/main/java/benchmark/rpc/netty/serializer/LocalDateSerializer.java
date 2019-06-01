package benchmark.rpc.netty.serializer;

import java.time.LocalDate;

import io.netty.buffer.ByteBuf;

public class LocalDateSerializer implements Serializer<LocalDate> {

	@Override
	public void write(ByteBuf byteBuf, LocalDate localDate) {
		byteBuf.writeShort(localDate.getYear());
		byteBuf.writeByte(localDate.getMonthValue());
		byteBuf.writeByte(localDate.getDayOfMonth());
	}

	@Override
	public LocalDate read(ByteBuf byteBuf) {
		int year = byteBuf.readShort();
		int month = byteBuf.readByte();
		int dayOfMonth = byteBuf.readByte();

		return LocalDate.of(year, month, dayOfMonth);
	}

	@Override
	public Class<LocalDate> typeClass() {
		return LocalDate.class;
	}

}
