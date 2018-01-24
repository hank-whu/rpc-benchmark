package benchmark.rpc.thrift;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserServiceServerImpl;

public class UserServiceThriftClientImpl implements benchmark.service.UserService, Closeable {

	private final String host = "benchmark-server";
	private final int port = 8080;

	// not thread safe
	private final TTransport transport = new TFramedTransport(new TSocket(host, port));
	private final TProtocol protocol = new TBinaryProtocol(transport);
	private final UserService.Client client = new UserService.Client(protocol);

	public UserServiceThriftClientImpl() {
		try {
			transport.open();
		} catch (TTransportException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			transport.close();
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean existUser(String email) {
		try {
			return client.userExist(email);
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean createUser(User user) {
		benchmark.rpc.thrift.User thriftUser = Converter.toThrift(user);

		try {
			return client.createUser(thriftUser);
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public User getUser(long id) {

		try {
			benchmark.rpc.thrift.User thriftUser = client.getUser(id);
			User user = Converter.toRaw(thriftUser);

			return user;
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Page<User> listUser(int pageNo) {

		try {
			UserPage userPage = client.listUser(pageNo);

			Page<User> page = new Page<>();

			page.setPageNo(userPage.getPageNo());
			page.setTotal(userPage.getTotal());

			List<User> userList = new ArrayList<>(userPage.getResult().size());

			for (benchmark.rpc.thrift.User thriftUser : userPage.getResult()) {
				User user = Converter.toRaw(thriftUser);

				userList.add(user);
			}

			page.setResult(userList);

			return page;
		} catch (TException e) {
			throw new RuntimeException(e);
		}

	}

	public static void main(String[] args) throws IOException {
		try (UserServiceThriftClientImpl userService = new UserServiceThriftClientImpl()) {
			System.out.println(userService.existUser("12345"));
			System.out.println(userService.createUser(new UserServiceServerImpl().getUser(1)));
			System.out.println(userService.getUser(5));
			System.out.println(userService.listUser(100));
		}
	}

}
