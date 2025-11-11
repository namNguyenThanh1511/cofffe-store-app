package namnt.vn.coffestore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import namnt.vn.coffestore.data.model.CartItem;

public class CartManager {
    private static final String TAG = "CartManager";
    private static final String PREFS_NAME = "coffee_cart_prefs";
    private static final String KEY_CART_ITEMS = "cart_items";
    
    private static CartManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    
    private CartManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }
    
    /**
     * Add item to cart
     */
    public void addItem(CartItem item) {
        List<CartItem> cartItems = getCartItems();
        
        // Check if item already exists (same variantId and customizations)
        boolean found = false;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem existingItem = cartItems.get(i);
            if (isSameItem(existingItem, item)) {
                // Update quantity
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                cartItems.set(i, existingItem);
                found = true;
                break;
            }
        }
        
        if (!found) {
            cartItems.add(item);
        }
        
        saveCartItems(cartItems);
        Log.d(TAG, "Item added to cart. Total items: " + cartItems.size());
    }
    
    /**
     * Check if two items are the same (same product, variant, and customizations)
     */
    private boolean isSameItem(CartItem item1, CartItem item2) {
        if (item1.getVariantId() == null || item2.getVariantId() == null) {
            return false;
        }
        
        boolean sameVariant = item1.getVariantId().equals(item2.getVariantId());
        boolean sameTemp = (item1.getTemperature() == null ? "" : item1.getTemperature())
                .equals(item2.getTemperature() == null ? "" : item2.getTemperature());
        boolean sameSweetness = (item1.getSweetness() == null ? "" : item1.getSweetness())
                .equals(item2.getSweetness() == null ? "" : item2.getSweetness());
        boolean sameMilk = (item1.getMilkType() == null ? "" : item1.getMilkType())
                .equals(item2.getMilkType() == null ? "" : item2.getMilkType());
        
        // Compare addon lists
        List<String> addons1 = item1.getSelectedAddonIds();
        List<String> addons2 = item2.getSelectedAddonIds();
        boolean sameAddons = addons1.size() == addons2.size() && addons1.containsAll(addons2);
        
        return sameVariant && sameTemp && sameSweetness && sameMilk && sameAddons;
    }
    
    /**
     * Get all cart items
     */
    public List<CartItem> getCartItems() {
        String json = prefs.getString(KEY_CART_ITEMS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Type listType = new TypeToken<ArrayList<CartItem>>(){}.getType();
            List<CartItem> items = gson.fromJson(json, listType);
            return items != null ? items : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing cart items: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Save cart items to SharedPreferences
     */
    private void saveCartItems(List<CartItem> items) {
        String json = gson.toJson(items);
        prefs.edit().putString(KEY_CART_ITEMS, json).apply();
        Log.d(TAG, "Cart items saved: " + items.size() + " items");
    }
    
    /**
     * Update item quantity
     */
    public void updateItemQuantity(int position, int newQuantity) {
        List<CartItem> cartItems = getCartItems();
        if (position >= 0 && position < cartItems.size()) {
            if (newQuantity > 0) {
                cartItems.get(position).setQuantity(newQuantity);
            } else {
                cartItems.remove(position);
            }
            saveCartItems(cartItems);
        }
    }
    
    /**
     * Remove item from cart
     */
    public void removeItem(int position) {
        List<CartItem> cartItems = getCartItems();
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            saveCartItems(cartItems);
            Log.d(TAG, "Item removed. Remaining items: " + cartItems.size());
        }
    }
    
    /**
     * Clear all items from cart
     */
    public void clearCart() {
        prefs.edit().remove(KEY_CART_ITEMS).apply();
        Log.d(TAG, "Cart cleared");
    }
    
    /**
     * Get total number of items in cart
     */
    public int getCartItemCount() {
        List<CartItem> items = getCartItems();
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }
    
    /**
     * Get total price of all items in cart
     */
    public double getCartTotalPrice() {
        List<CartItem> items = getCartItems();
        double total = 0;
        for (CartItem item : items) {
            total += item.getTotal();
        }
        return total;
    }
    
    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return getCartItems().isEmpty();
    }
}
