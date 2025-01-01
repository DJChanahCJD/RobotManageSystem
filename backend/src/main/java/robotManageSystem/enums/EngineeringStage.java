package robotManageSystem.enums;

public enum EngineeringStage {
    InitialStage("InitialStage", "初始阶段"),
    DesignStage("DesignStage", "概念化和设计阶段"),
    DevelopmentStage("DevelopmentStage", "原型开发阶段"),
    PartStage("PartStage", "部件采购和制造阶段"),
    SaleStage("SaleStage", "产品销售阶段");

    private final String value;
    private final String description;

    EngineeringStage(String value, String description) {
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