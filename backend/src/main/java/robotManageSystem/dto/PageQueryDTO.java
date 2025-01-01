package robotManageSystem.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    public Integer pageNo = 1;

    public Integer pageSize = 10;
}