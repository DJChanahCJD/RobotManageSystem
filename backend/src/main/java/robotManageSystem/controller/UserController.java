package robotManageSystem.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdsModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.UserDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserViewDTO;

import robotManageSystem.dto.BaseResponse;
import robotManageSystem.utils.JwtUtil;
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserDelegator userDelegator;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO createDTO) {
        try {
            System.out.println("开始创建用户: " + createDTO);
            Object result = userDelegator.create(createDTO);
            System.out.println("创建结果: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("创建用户失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            // 1. 验证用户是否存在
            PersistObjectIdDecryptDTO queryDto = new PersistObjectIdDecryptDTO();
            queryDto.setId(id);
            UserViewDTO user = userDelegator.get(queryDto);
            if (user == null) {
                System.out.println("用户不存在");
                return ResponseEntity.notFound().build();
            }

            // 2. 删除用户
            PersistObjectIdModifierDTO idDTO = new PersistObjectIdModifierDTO();
            idDTO.setId(id);
            userDelegator.delete(idDTO);
            return ResponseEntity.ok(BaseResponse.ok("删除用户成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("删除用户失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/batch")
    public ResponseEntity<?> batchDeleteUser(@RequestBody List<Long> ids) {
        try {
            PersistObjectIdsModifierDTO idsDTO = new PersistObjectIdsModifierDTO();
            idsDTO.setIds(ids);
            userDelegator.batchDelete(idsDTO);
            return ResponseEntity.ok("批量删除用户成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("批量删除用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
            idDTO.setId(id);
            UserViewDTO user = userDelegator.get(idDTO);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("获取用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> findUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 根据具体需求设置搜索条件
                // queryRequestVo.setCondition(...);
            }

            List<UserViewDTO> users = userDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = userDelegator.count(queryRequestVo);

            Map<String, Object> result = new HashMap<>();
            result.put("pageSize", pageSize);
            result.put("pageNo", pageNo);
            result.put("totalCount", totalCount);
            result.put("data", users);

            return ResponseEntity.ok(Collections.singletonMap("result", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("获取用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public long countUsers() {
        return userDelegator.count(new QueryRequestVo());
    }

    @PutMapping("/{id}")
    public UserViewDTO updateUser(@PathVariable String id, @RequestBody UserUpdateDTO updateDTO) {
        return userDelegator.update(updateDTO);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestHeader(value = "Access-Token", required = false) String token) {
        try {
            // 验证 token
            String userId = jwtUtil.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Collections.singletonMap("message", "token无效"));
            }

            // 构造与 mock 数据相同的返回格式
            Map<String, Object> role = new HashMap<>();
            role.put("id", "admin");
            role.put("name", "管理员");
            role.put("describe", "拥有所有权限");
            role.put("permissions", getSimplePermissions());

            Map<String, Object> result = new HashMap<>();
            result.put("role", role);
            result.put("name", "Admin");
            result.put("avatar", "https://gw.alipayobjects.com/zos/rmsportal/jZUIxmJycoymBprLOUbT.png");

            return ResponseEntity.ok(Collections.singletonMap("result", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("message", "获取用户信息失败：" + e.getMessage()));
        }
    }

    private List<Map<String, Object>> getSimplePermissions() {
        List<Map<String, Object>> permissions = new ArrayList<>();

        Map<String, Object> permission = new HashMap<>();
        permission.put("permissionId", "admin");
        permission.put("permissionName", "仪表盘");
        permission.put("actions", Arrays.asList("add", "query", "update", "delete"));

        permissions.add(permission);
        return permissions;
    }
}