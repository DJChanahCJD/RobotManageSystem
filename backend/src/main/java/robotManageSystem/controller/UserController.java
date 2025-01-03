package robotManageSystem.controller;

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
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.UserDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.UserViewDTO;

import robotManageSystem.dto.BaseResponse;
import robotManageSystem.dto.PageResultDTO;
import robotManageSystem.enums.Authority;
import robotManageSystem.utils.JwtUtil;

/*
 * User实体
 * Name: 用户名
 * Password: 密码
 * Authority: 权限（必填）
 * Phone: 电话
 */

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
            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            System.out.println("创建用户失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("创建用户失败: " + e.getMessage()));
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
            return ResponseEntity.ok(BaseResponse.ok(user));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("获取用户失败: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> findUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Authority authority,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = QueryRequestVo.build();

            // 使用 and() 方法开始构建条件
            QueryCondition condition = queryRequestVo.and();

            // 添加查询条件
            if (name != null && !name.trim().isEmpty()) {
                condition.addCondition("name", ConditionType.LIKE, name);
            }

            if (authority != null) {
                condition.addCondition("authority", ConditionType.EQUAL, authority.toString());
            }

            if (phone != null && !phone.trim().isEmpty()) {
                condition.addCondition("phone", ConditionType.LIKE, phone);
            }

            // 设置排序
            queryRequestVo.setOrderBy("createTime")
                         .setSort("DESC");

            List<UserViewDTO> users = userDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = userDelegator.count(queryRequestVo);

            return ResponseEntity.ok(BaseResponse.ok(PageResultDTO.of(
                pageNo,
                pageSize,
                totalCount,
                users
            )));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("获取用户列表失败: " + e.getMessage()));
        }
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

            QueryRequestVo queryRequestVo = new QueryRequestVo();
            queryRequestVo.addCondition("id", ConditionType.EQUAL, userId);
            UserViewDTO user = userDelegator.find(queryRequestVo, new RDMPageVO(1, 1)).get(0);

            // 构造与 mock 数据相同的返回格式
            Map<String, Object> role = new HashMap<>();
            role.put("id", userId);
            role.put("name", user.getName());
            role.put("permissions", user.getAuthority());

            Map<String, Object> result = new HashMap<>();
            result.put("role", role);
            result.put("name", user.getName());
            result.put("avatar", "https://gw.alipayobjects.com/zos/rmsportal/jZUIxmJycoymBprLOUbT.png");
            result.put("password", user.getPassword());
            return ResponseEntity.ok(Collections.singletonMap("result", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("message", "获取用户信息失败：" + e.getMessage()));
        }
    }
}