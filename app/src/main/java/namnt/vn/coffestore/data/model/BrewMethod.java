package namnt.vn.coffestore.data.model;

public enum BrewMethod {
    Espresso("Espresso", "Espresso"),
    Phin("Phin", "Phin"),
    PourOver("Pour Over", "Pour Over"),
    ColdBrew("Cold Brew", "Pha lạnh");

    private final String englishName;
    private final String vietnameseName;

    BrewMethod(String englishName, String vietnameseName) {
        this.englishName = englishName;
        this.vietnameseName = vietnameseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    public String getDisplayName() {
        return vietnameseName;
    }

    public static BrewMethod fromString(String value) {
        if (value == null) return null;
        
        for (BrewMethod method : BrewMethod.values()) {
            if (method.name().equalsIgnoreCase(value) || 
                method.englishName.equalsIgnoreCase(value) ||
                method.vietnameseName.equalsIgnoreCase(value)) {
                return method;
            }
        }
        return null;
    }
}
