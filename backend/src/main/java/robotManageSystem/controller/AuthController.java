package robotManageSystem.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.UserDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserViewDTO;

import robotManageSystem.dto.BaseResponse;
import robotManageSystem.dto.auth.LoginRequestDTO;
import robotManageSystem.utils.JwtUtil;
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserDelegator userDelegator;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            String username = loginRequest.username;
            String password = loginRequest.password;

            // 构造查询条件
            QueryRequestVo queryRequest = new QueryRequestVo();
            queryRequest.addCondition("name", ConditionType.EQUAL, username);
            queryRequest.addCondition("password", ConditionType.EQUAL, password);

            // 设置分页参数
            RDMPageVO pageVO = new RDMPageVO(1, 1);

            // 查询用户
            List<UserViewDTO> users = userDelegator.find(queryRequest, pageVO);

            if (users != null && !users.isEmpty()) {
                UserViewDTO user = users.get(0);
                String token = jwtUtil.generateToken(user);

                // 构造与 mock 数据相同的返回格式
                Map<String, Object> result = new HashMap<>();
                result.put("id", user.getId());
                result.put("name", user.getName());
                result.put("username", user.getName());  // 用 name 作为 username
                result.put("password", "");  // 不返回密码
                result.put("avatar", "https://gw.alipayobjects.com/zos/rmsportal/jZUIxmJycoymBprLOUbT.png");
                result.put("status", 1);
                result.put("telephone", "");
                result.put("lastLoginIp", "127.0.0.1");
                result.put("lastLoginTime", System.currentTimeMillis());
                result.put("creatorId", "admin");
                result.put("createTime", System.currentTimeMillis());
                result.put("deleted", 0);
                result.put("roleId", "admin");
                result.put("lang", "zh-CN");
                result.put("token", token);

                return ResponseEntity.ok(BaseResponse.ok(result));
            } else {
                return ResponseEntity.status(401)
                    .body(BaseResponse.error("用户名或密码错误"));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("message", "登录失败：" + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, String> result = new HashMap<>();

        result.put("message", "注销成功");
        return ResponseEntity.ok(result);
    }
}