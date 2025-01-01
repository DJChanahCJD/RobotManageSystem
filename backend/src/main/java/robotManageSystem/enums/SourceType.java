package robotManageSystem.enums;

public enum SourceType {
    Purchase("Purchase", "采购"),
    Manufacture("Manufacture", "制造"),
    Sale("Sale", "销售");

    private final String value;
    private final String description;

    SourceType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}