package robotManageSystem.dto;

import java.util.List;
import lombok.Data;

@Data
public class PageResultDTO<T> {
    private int pageNo;
    private int pageSize;
    private long totalCount;
    private List<T> data;

    public PageResultDTO(int pageNo, int pageSize, long totalCount, List<T> data) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.data = data;
    }

    public static <T> PageResultDTO<T> of(int pageNo, int pageSize, long totalCount, List<T> data) {
        return new PageResultDTO<>(pageNo, pageSize, totalCount, data);
    }
}
