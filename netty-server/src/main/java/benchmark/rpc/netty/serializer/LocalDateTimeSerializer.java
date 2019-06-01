package benchmark.rpc.netty.serializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import io.netty.buffer.ByteBuf;

public class LocalDateTimeSerializer implements Serializer<LocalDateTime> {

	private final LocalDateSerializer localDateSerializer = new LocalDateSerializer();
	private final LocalTimeSerializer localTimeSerializer = new LocalTimeSerializer();

	@Override
	public void write(ByteBuf byteBuf, LocalDateTime localDateTime) {
		localDateSerializer.write(byteBuf, localDateTime.toLocalDate());
		localTimeSerializer.write(byteBuf, localDateTime.toLocalTime());
	}

	@Override
	public LocalDateTime read(ByteBuf byteBuf) {
		LocalDate localDate = localDateSerializer.read(byteBuf);
		LocalTime localTime = localTimeSerializer.read(byteBuf);

		return LocalDateTime.of(localDate, localTime);
	}

	@Override
	public Class<LocalDateTime> typeClass() {
		return LocalDateTime.class;
	}

}
