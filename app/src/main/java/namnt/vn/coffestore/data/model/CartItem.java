package namnt.vn.coffestore.data.model;

public class CartItem {
    private String id;
    private String name;
    private double price;
    private String imageUrl;
    private String size;
    private int quantity;

    public CartItem(String id, String name, double price, String imageUrl, String size, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.size = size;
        this.quantity = quantity;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Calculate total for this item
    public double getTotal() {
        return price * quantity;
    }
}
