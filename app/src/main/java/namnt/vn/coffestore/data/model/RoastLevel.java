package namnt.vn.coffestore.data.model;

public enum RoastLevel {
    LIGHT("Light Roast", "Nhẹ"),
    MEDIUM("Medium Roast", "Vừa"),
    MEDIUM_DARK("Medium Dark Roast", "Vừa Đậm"),
    DARK("Dark Roast", "Đậm"),
    EXTRA_DARK("Extra Dark Roast", "Rất Đậm");

    private final String englishName;
    private final String vietnameseName;

    RoastLevel(String englishName, String vietnameseName) {
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

    public static RoastLevel fromString(String value) {
        if (value == null) return null;
        
        for (RoastLevel level : RoastLevel.values()) {
            if (level.name().equalsIgnoreCase(value) || 
                level.englishName.equalsIgnoreCase(value) ||
                level.vietnameseName.equalsIgnoreCase(value)) {
                return level;
            }
        }
        return null;
    }
}
