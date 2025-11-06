package namnt.vn.coffestore.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Product {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("alias")
    private String alias;
    
    @SerializedName("origin")
    private String origin;
    
    @SerializedName("roastLevel")
    private String roastLevel;
    
    @SerializedName("brewMethod")
    private String brewMethod;
    
    @SerializedName("variants")
    private List<ProductVariant> variants;

    // Constructor
    public Product() {
    }

    public Product(String id, String name, String description, String imageUrl, String alias, 
                   String origin, String roastLevel, String brewMethod, List<ProductVariant> variants) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.alias = alias;
        this.origin = origin;
        this.roastLevel = roastLevel;
        this.brewMethod = brewMethod;
        this.variants = variants;
    }

    // Get cheapest price from variants (for display in menu)
    public double getMinPrice() {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        double minPrice = Double.MAX_VALUE;
        for (ProductVariant variant : variants) {
            if (variant.getBasePrice() < minPrice) {
                minPrice = variant.getBasePrice();
            }
        }
        return minPrice;
    }
    
    // Get most expensive price from variants
    public double getMaxPrice() {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        double maxPrice = 0;
        for (ProductVariant variant : variants) {
            if (variant.getBasePrice() > maxPrice) {
                maxPrice = variant.getBasePrice();
            }
        }
        return maxPrice;
    }

    // Convert to CoffeeItem for UI (using minimum price)
    public CoffeeItem toCoffeeItem(String category) {
        return new CoffeeItem(id, name, description, getMinPrice(), null, imageUrl, category);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getRoastLevel() {
        return roastLevel;
    }

    public void setRoastLevel(String roastLevel) {
        this.roastLevel = roastLevel;
    }
    
    public RoastLevel getRoastLevelEnum() {
        return RoastLevel.fromString(roastLevel);
    }

    public String getBrewMethod() {
        return brewMethod;
    }

    public void setBrewMethod(String brewMethod) {
        this.brewMethod = brewMethod;
    }
    
    public BrewMethod getBrewMethodEnum() {
        return BrewMethod.fromString(brewMethod);
    }
}
