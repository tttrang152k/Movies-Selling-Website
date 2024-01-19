import java.util.HashMap;
import java.util.Map;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    private int userId;
    private Map<String, Integer> cart;

    public User(String username) {
        this.username = username;
    }
    public void setUserId(int id) { this.userId = id; }

    public int getUserId() { return userId; }

    public String getUsername() { return username;}

    public void addToCart(String item) {
        if (cart.isEmpty()) {
            cart = new HashMap<>();
            cart.put(item, 1);
        } else if (cart.get(item) != null)
            cart.put(item, cart.get(item) + 1);
        else
            cart.put(item, 1);
    }

    public int getItemAmount(String item) {
        return cart.get(item) == null ? 0 : cart.get(item);
    }

    public void deleteItem(String item){
        if (cart.get(item) != null)
            cart.remove(item);
    }

}
