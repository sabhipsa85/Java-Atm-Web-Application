public class Main {
    public static void main(String[] args) {
        // Initialize ATM Service with predefined data
        ATMService atmService = new ATMService();

        // Start the ATM Server (HTTP server)
        ATMServlet.startServer(atmService);
    }
}
