import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ATMServlet {
    private static ATMService atmService;
    private static Map<String, String> sessionTokens = new HashMap<>(); // Maps session tokens to usernames

    public static void startServer(ATMService service) {
        atmService = service;
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(5000), 0);
            server.createContext("/login", new CORSHandler(new LoginHandler()));
            server.createContext("/checkBalance", new CORSHandler(new CheckBalanceHandler()));
            server.createContext("/deposit", new CORSHandler(new DepositHandler()));
            server.createContext("/withdraw", new CORSHandler(new WithdrawHandler()));
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Server started on port 5000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class CORSHandler implements HttpHandler {
        private HttpHandler next;

        public CORSHandler(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                addCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1); // No Content for OPTIONS request
            } else {
                addCorsHeaders(exchange);
                next.handle(exchange);
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String[] params = exchange.getRequestURI().getQuery().split("&");
                String username = params[0].split("=")[1];
                String password = params[1].split("=")[1];
                boolean isAuthenticated = atmService.authenticate(username, password);
                String response;
                if (isAuthenticated) {
                    String sessionToken = generateSessionToken(username);
                    sessionTokens.put(sessionToken, username);
                    response = "Login successful, token: " + sessionToken;
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    response = "Invalid credentials";
                    exchange.sendResponseHeaders(401, response.length());
                }
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed for non-GET requests
            }
        }
    }

    static class CheckBalanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String token = getTokenFromHeaders(exchange);
                String username = sessionTokens.get(token);
                if (username != null) {
                    String accountNumber = atmService.getAccountNumber(username);
                    double balance = atmService.checkBalance(accountNumber);
                    String response = "Balance: $" + balance;
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(403, -1); // Forbidden
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed for non-GET requests
            }
        }
    }

    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String token = getTokenFromHeaders(exchange);
                String username = sessionTokens.get(token);
                if (username != null) {
                    String[] params = exchange.getRequestURI().getQuery().split("&");
                    double amount = Double.parseDouble(params[0].split("=")[1]);
                    String accountNumber = atmService.getAccountNumber(username);
                    atmService.deposit(accountNumber, amount);
                    String response = "Deposited $" + amount;
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(403, -1); // Forbidden
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed for non-GET requests
            }
        }
    }

    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String token = getTokenFromHeaders(exchange);
                String username = sessionTokens.get(token);
                if (username != null) {
                    String[] params = exchange.getRequestURI().getQuery().split("&");
                    double amount = Double.parseDouble(params[0].split("=")[1]);
                    String accountNumber = atmService.getAccountNumber(username);
                    try {
                        atmService.withdraw(accountNumber, amount);
                        String response = "Withdrew $" + amount;
                        exchange.sendResponseHeaders(200, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } catch (Exception e) {
                        String response = "Error: " + e.getMessage();
                        exchange.sendResponseHeaders(400, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                } else {
                    exchange.sendResponseHeaders(403, -1); // Forbidden
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed for non-GET requests
            }
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static String generateSessionToken(String username) {
        return username + "_token"; // Simplified session token generation
    }

    private static String getTokenFromHeaders(HttpExchange exchange) {
        return exchange.getRequestHeaders().getFirst("Authorization");
    }
}
