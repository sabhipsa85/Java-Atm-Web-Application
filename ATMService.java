import java.util.HashMap;
import java.util.Map;

public class ATMService {
    private Map<String, User> users;
    private Map<String, Double> accounts;

    public ATMService() {
        // Initialize predefined users
        users = new HashMap<>();
        users.put("Abhipsa", new User("Abhipsa", "Admin", "1234567890"));
        users.put("Namrata", new User("Namrata", "Admin", "0987654321"));

        // Initialize predefined accounts with account numbers and balances
        accounts = new HashMap<>();
        accounts.put("1234567890", 5000.00);
        accounts.put("0987654321", 10000.00);
    }

    public boolean authenticate(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    public String getAccountNumber(String username) {
        User user = users.get(username);
        return user != null ? user.getAccountNumber() : null;
    }

    public double checkBalance(String accountNumber) {
        return accounts.getOrDefault(accountNumber, 0.00);
    }

    public void deposit(String accountNumber, double amount) {
        accounts.put(accountNumber, accounts.getOrDefault(accountNumber, 0.00) + amount);
    }

    public void withdraw(String accountNumber, double amount) throws Exception {
        double balance = accounts.getOrDefault(accountNumber, 0.00);
        if (balance >= amount) {
            accounts.put(accountNumber, balance - amount);
        } else {
            throw new Exception("Insufficient funds");
        }
    }
}
