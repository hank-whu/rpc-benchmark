package benchmark.rpc;

import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;

public class Server {

    public static void main(String[] args) {
        ServerConfig serverConfig = new ServerConfig()
                .setProtocol("bolt")
                .setPort(12200)
                .setDaemon(false);

        ProviderConfig<UserService> providerConfig = new ProviderConfig<UserService>()
                .setInterfaceId(UserService.class.getName())
                .setRef(new UserServiceServerImpl())
                .setServer(serverConfig);

        providerConfig.export();
    }

}
