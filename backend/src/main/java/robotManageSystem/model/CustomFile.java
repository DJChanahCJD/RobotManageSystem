package robotManageSystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.web.multipart.MultipartFile;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomFile {
    private String id;
    private MultipartFile file;

    public String getId() {
        return id;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}