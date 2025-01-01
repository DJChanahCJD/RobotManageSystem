package robotManageSystem.dto.auth;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "用户名不能为空")
    public String username;

    @NotBlank(message = "密码不能为空")
    public String password;
}