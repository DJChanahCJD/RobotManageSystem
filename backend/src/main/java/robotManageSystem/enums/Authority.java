package robotManageSystem.enums;

public enum Authority {
    Admin("Admin"),
    Normal("Normal");

    private final String value;

    Authority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}