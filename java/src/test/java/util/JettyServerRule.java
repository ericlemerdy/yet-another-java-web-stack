package util;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.rules.ExternalResource;

public class JettyServerRule extends ExternalResource {

    private Server server;

    @Override
    protected void before() throws Throwable {
        server = new Server(8080);
        server.setHandler(new WebAppContext(server, "src/main/webapp/", "/"));
        server.start();
    }

    @Override
    protected void after() {
        try {
            server.stop();
        } catch (Exception e) {
            System.err.println("Unable to stop test server");
        }
    }
}
