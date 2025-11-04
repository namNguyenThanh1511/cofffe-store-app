package namnt.vn.coffestore.data.model;

public enum BrewMethod {
    ESPRESSO("Espresso", "Ép"),
    DRIP("Drip", "Nhỏ giọt"),
    FRENCH_PRESS("French Press", "Phin Pháp"),
    POUR_OVER("Pour Over", "Rót"),
    COLD_BREW("Cold Brew", "Pha lạnh"),
    AEROPRESS("AeroPress", "AeroPress"),
    VIETNAMESE("Vietnamese", "Phin Việt Nam"),
    MOKA_POT("Moka Pot", "Ấm Moka");

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
        return vietnameseName + " (" + englishName + ")";
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
