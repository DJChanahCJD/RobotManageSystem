package robotManageSystem.dto.Part;

import com.huawei.innovation.rdm.xdm.dto.relation.EXADefinitionLinkViewDTO;

import javax.validation.Valid;
import java.util.List;

// PartUpdateDTO.java
public class PartUpdateDTO extends com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.@Valid PartUpdateDTO {
    private Long classificationId;
    private List<EXADefinitionLinkViewDTO> attributes;
    // 其他字段和方法

    public Long getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(Long classificationId) {
        this.classificationId = classificationId;
    }

    public List<EXADefinitionLinkViewDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<EXADefinitionLinkViewDTO> attributes) {
        this.attributes = attributes;
    }
}