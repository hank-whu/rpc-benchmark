package benchmark.rpc;

import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.springboot.starter.provider.EnableServiceComb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableServiceComb
public class Server {

	public static void main(String[] args) throws Exception {
		Log4jUtils.init();
		SpringApplication.run(Server.class, args);
	}

}
