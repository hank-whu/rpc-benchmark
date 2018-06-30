package benchmark.rpc.netty.serializer;

import java.util.ArrayList;
import java.util.List;

import benchmark.bean.Page;
import benchmark.bean.User;
import io.netty.buffer.ByteBuf;

public class UserPageSerializer implements Serializer<Page<User>> {

	private final UserSerializer userSerializer = new UserSerializer();

	@Override
	public void write(ByteBuf byteBuf, Page<User> userPage) {
		List<User> userList = userPage.getResult();

		byteBuf.writeInt(userPage.getPageNo());
		byteBuf.writeInt(userPage.getTotal());
		byteBuf.writeInt(userList.size());

		for (User user : userList) {
			userSerializer.write(byteBuf, user);
		}
	}

	@Override
	public Page<User> read(ByteBuf byteBuf) {
		int pageNo = byteBuf.readInt();
		int total = byteBuf.readInt();
		int size = byteBuf.readInt();

		List<User> userList = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			userList.add(userSerializer.read(byteBuf));
		}

		Page<User> userPage = new Page<>();
		userPage.setPageNo(pageNo);
		userPage.setTotal(total);
		userPage.setResult(userList);

		return userPage;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class<Page> typeClass() {
		return Page.class;
	}

}
