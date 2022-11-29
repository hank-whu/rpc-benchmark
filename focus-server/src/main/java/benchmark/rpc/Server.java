package benchmark.rpc;

import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import com.dinstone.focus.server.FocusServer;
import com.dinstone.focus.server.ServerOptions;


public class Server {

    public static void main(String[] args) {

        ServerOptions serverOptions = new ServerOptions().listen(3333)
                .setEndpoint("focus.server");
        FocusServer server = new FocusServer(serverOptions);

        // exporting service
        server.exporting(UserService.class, new UserServiceServerImpl());

        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.destroy();

    }

}
