package robotManageSystem.controller;

import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserViewDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.UserDelegator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserDelegator userDelegator;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            // 构造查询条件
            QueryRequestVo queryRequest = new QueryRequestVo();
            queryRequest.setConditions(Collections.singletonList(new QueryCondition("name", username)));
            queryRequest.setConditions(Collections.singletonList(new QueryCondition("password", password)));

            // 查询用户
            List<UserViewDTO> users = userDelegator.find(queryRequest, null);

            if (users != null && !users.isEmpty()) {
                UserViewDTO user = users.get(0);
                return ResponseEntity.ok(Collections.singletonMap("data", user));
            } else {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "用户名或密码错误"));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "登录失败：" + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Collections.singletonMap("data", "logout success"));
    }
}