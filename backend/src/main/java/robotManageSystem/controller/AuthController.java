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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.UserDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserUpdateDTO;
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
            // 1. 先根据用户名查找用户
            QueryRequestVo queryRequest = new QueryRequestVo();
            queryRequest.addCondition("name", ConditionType.EQUAL, loginRequest.username);

            List<UserViewDTO> users = userDelegator.find(queryRequest, new RDMPageVO(1, 1));

            if (users != null && !users.isEmpty()) {
                UserViewDTO user = users.get(0);

                // 2. 比较密码的MD5值
                if (!user.getPassword().equals(loginRequest.password)) {
                    return ResponseEntity.status(401)
                        .body(BaseResponse.error("用户名或密码错误"));
                }

                // 3. 生成token并返回用户信息
                String token = jwtUtil.generateToken(user);
                Map<String, Object> result = new HashMap<>();
                result.put("id", user.getId());
                result.put("name", user.getName());
                result.put("token", token);

                return ResponseEntity.ok(BaseResponse.ok(result));
            } else {
                return ResponseEntity.status(401)
                    .body(BaseResponse.error("用户名或密码错误"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("登录失败：" + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(BaseResponse.ok("注销成功"));
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(
        @RequestHeader(value="Access-Token", required=false) String token,
        @RequestBody Map<String, String> passwordRequest
    ) {
        String newPassword = passwordRequest.get("newPassword");
        if (newPassword == null) {
            return ResponseEntity.badRequest().body(BaseResponse.error("新密码不能为空"));
        }

        String userId = jwtUtil.validateToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("token无效"));
        }

        QueryRequestVo queryRequest = new QueryRequestVo();
        queryRequest.addCondition("id", ConditionType.EQUAL, userId);
        UserViewDTO user = userDelegator.find(queryRequest, new RDMPageVO(1, 1)).get(0);
        if (user == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("用户不存在"));
        }

        if (newPassword.equals(user.getPassword())) {
            return ResponseEntity.status(401).body(BaseResponse.error("新密码与旧密码相同"));
        }

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setId(user.getId());
        userUpdateDTO.setPassword(newPassword);
        userDelegator.update(userUpdateDTO);

        return ResponseEntity.ok(BaseResponse.ok("密码修改成功"));
    }
}