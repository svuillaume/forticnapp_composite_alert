package demo;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

/**
 * Educational demo — Log4Shell callback catcher.
 *
 * A minimal LDAP listener whose sole purpose is to prove that the JNDI lookup
 * in CVE-2021-44228 fired: it accepts the inbound connection from the
 * vulnerable server and prints what arrived.  It does NOT serve any LDAP
 * response or deliver a payload — watching the connection appear is enough to
 * demonstrate the vulnerability.
 *
 * Run first, then start VulnerableServer and send the crafted request:
 *   mvn exec:java -Dexec.mainClass=demo.CallbackMonitor
 */
public class CallbackMonitor {

    private static final int LDAP_PORT = 1389;

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(LDAP_PORT)) {
            System.out.println("[CallbackMonitor] LDAP listener on port " + LDAP_PORT);
            System.out.println("[CallbackMonitor] Waiting for JNDI callbacks...\n");

            //noinspection InfiniteLoopStatement
            while (true) {
                Socket client = server.accept();
                // Handle each callback in its own thread so the monitor stays responsive.
                Thread t = new Thread(() -> handleCallback(client));
                t.setDaemon(true);
                t.start();
            }
        }
    }

    private static void handleCallback(Socket client) {
        String remote = client.getRemoteSocketAddress().toString();
        System.out.printf("[%s]  *** CALLBACK from %s ***%n", Instant.now(), remote);

        try (InputStream in = client.getInputStream()) {
            byte[] buf = new byte[512];
            int n = in.read(buf);
            if (n > 0) {
                System.out.println("  Bytes received : " + n);
                System.out.println("  Hex dump       : " + toHex(buf, n));
                String text = extractPrintable(buf, n);
                if (!text.isEmpty()) {
                    System.out.println("  Printable      : " + text);
                }
            }
        } catch (IOException e) {
            System.out.println("  [read error: " + e.getMessage() + "]");
        } finally {
            try { client.close(); } catch (IOException ignored) {}
            System.out.println();
        }
    }

    // -------------------------------------------------------------------------

    private static String toHex(byte[] data, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02x ", data[i]));
        }
        return sb.toString().trim();
    }

    /** Extracts ASCII-printable characters so the LDAP DN / path is readable. */
    private static String extractPrintable(byte[] data, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = (char) (data[i] & 0xFF);
            if (c >= 32 && c < 127) sb.append(c);
        }
        return sb.toString();
    }
}
