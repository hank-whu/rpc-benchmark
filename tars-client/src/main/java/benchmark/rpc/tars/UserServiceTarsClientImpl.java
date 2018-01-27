package benchmark.rpc.tars;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.CommunicatorFactory;
import com.qq.tars.net.client.ticket.TicketManager;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;

public class UserServiceTarsClientImpl implements UserService, Closeable {

	private CommunicatorConfig cfg;
	// 构建通信器
	private Communicator communicator;
	// 通过通信器，生成代理对象
	private TarsUserServicePrx proxy;

	public UserServiceTarsClientImpl() {
		new File("/tmp/data").mkdirs();
		new File("/tmp/log").mkdirs();

		try {
			File tarsConf = copyToTemp("/tars.conf");

			System.setProperty("config", tarsConf.getAbsolutePath());
			System.out.println(System.getProperty("config"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		cfg = new CommunicatorConfig();
		cfg.setLogPath("/tmp/log");
		cfg.setDataPath("/tmp/data");

		communicator = CommunicatorFactory.getInstance().getCommunicator(cfg);

		proxy = communicator.stringToProxy(TarsUserServicePrx.class,
				"TestApp.HelloServer.UserService@tcp -h benchmark-server -p 18600 -t 60000");
	}

	private static File copyToTemp(String name) throws Exception {
		File file = new File("/tmp/" + name);

		try (FileOutputStream fos = new FileOutputStream(file);
				InputStream is = UserServiceTarsClientImpl.class.getResourceAsStream(name);
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));) {

			String line = "";

			while ((line = br.readLine()) != null) {
				fos.write(line.getBytes("UTF-8"));
				fos.write("\r\n".getBytes("UTF-8"));
			}

		}

		return file;
	}

	@Override
	public void close() throws IOException {

		try {
			Field field = Communicator.class.getDeclaredField("threadPoolExecutor");
			field.setAccessible(true);
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) field.get(communicator);
			threadPoolExecutor.shutdownNow();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			TicketManager.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean existUser(String email) {
		return proxy.existUser(email);
	}

	@Override
	public boolean createUser(User user) {
		TarsUser tarsUser = Converter.toTars(user);
		return proxy.createUser(tarsUser);
	}

	@Override
	public User getUser(long id) {
		TarsUser tarsUser = proxy.getUser(id);
		return Converter.toRaw(tarsUser);
	}

	@Override
	public Page<User> listUser(int pageNo) {
		TarsPage tarsPage = proxy.listUser(pageNo);

		List<User> list = new ArrayList<>(tarsPage.getResult().size());
		for (TarsUser tarsUser : tarsPage.getResult()) {
			User user = Converter.toRaw(tarsUser);
			list.add(user);
		}

		Page<User> page = new Page<>();
		page.setPageNo(tarsPage.pageNo);
		page.setTotal(tarsPage.total);
		page.setResult(list);

		return page;
	}

}
