package benchmark.rpc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Server {

	public static void main(String[] args) throws Exception {
		new File("/tmp/data").mkdirs();
		new File("/tmp/log").mkdirs();

		File tarsConf = copyToTemp("/tars.conf");

		System.setProperty("config", tarsConf.getAbsolutePath());
		System.out.println(System.getProperty("config"));

		new com.qq.tars.server.core.Server().startUp(args);

		Thread.sleep(Long.MAX_VALUE);
	}

	private static File copyToTemp(String name) throws Exception {
		File file = new File("/tmp/" + name);

		try (FileOutputStream fos = new FileOutputStream(file);
				InputStream is = Server.class.getResourceAsStream(name);
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));) {

			String line = "";

			while ((line = br.readLine()) != null) {
				fos.write(line.getBytes("UTF-8"));
				fos.write("\r\n".getBytes("UTF-8"));
			}

		}

		return file;
	}

}
