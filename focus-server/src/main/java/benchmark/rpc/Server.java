package benchmark.rpc;

import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import com.dinstone.focus.server.FocusServer;
import com.dinstone.focus.server.ServerOptions;


public class Server {

    public static void main(String[] args) {

        ServerOptions serverOptions = new ServerOptions("focus.server").listen(3333);
        FocusServer server = new FocusServer(serverOptions);

        // exporting service
        server.exporting(UserService.class, new UserServiceServerImpl());
        try {
            server.start();
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.close();

    }

}
