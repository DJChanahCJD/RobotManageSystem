package robotManageSystem.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.OrderDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.OrderCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.OrderUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.OrderViewDTO;

import robotManageSystem.dto.BaseResponse;
import robotManageSystem.dto.PageResultDTO;

/*
 * order实体
 * Name: 订单名称
 * ID: 订单ID
 * Type: 订单类型
 * Quantity: 订单数量
 * OrderDate: 订单日期
 */
@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired
    private OrderDelegator orderDelegator;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateDTO createDTO) {
        try {
            System.out.println("开始创建订单: " + createDTO);
            Object result = orderDelegator.create(createDTO);
            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            System.out.println("创建订单失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("创建订单失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            PersistObjectIdModifierDTO idDTO = new PersistObjectIdModifierDTO();
            idDTO.setId(id);
            orderDelegator.delete(idDTO);
            return ResponseEntity.ok(BaseResponse.ok(Collections.singletonMap("message", "订单删除成功")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("删除订单失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
            idDTO.setId(id);
            OrderViewDTO order = orderDelegator.get(idDTO);
            return ResponseEntity.ok(BaseResponse.ok(order));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("获取订单失败：" + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> findOrders(
            @RequestParam(required = false) String Name,
            @RequestParam(required = false) String ID,
            @RequestParam(required = false) String Type,
            @RequestParam(required = false) String StartDate,
            @RequestParam(required = false) String EndDate,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = QueryRequestVo.build();
            QueryCondition condition = queryRequestVo.and();

            // 名称模糊搜索
            if (Name != null && !Name.trim().isEmpty()) {
                condition.addCondition("name", ConditionType.LIKE, Name);
            }

            // ID精确查询
            if (ID != null && !ID.trim().isEmpty()) {
                try {
                    Long idValue = Long.parseLong(ID.trim());
                    condition.addCondition("id", ConditionType.EQUAL, idValue);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(BaseResponse.error("订单ID必须是数字"));
                }
            }

            // 类型精确查询
            if (Type != null && !Type.trim().isEmpty()) {
                condition.addCondition("type", ConditionType.EQUAL, Type);
            }

            // 日期范围查询
            if (StartDate != null && !StartDate.trim().isEmpty()) {
                condition.addCondition("orderDate", ConditionType.GREATER_EQUAL, StartDate);
            }
            if (EndDate != null && !EndDate.trim().isEmpty()) {
                condition.addCondition("orderDate", ConditionType.LESS_EQUAL, EndDate);
            }

            // 设置排序（按创建时间倒序）
            queryRequestVo.setOrderBy("createTime")
                         .setSort("DESC");

            // 执行查询
            List<OrderViewDTO> orders = orderDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = orderDelegator.count(queryRequestVo);

            return ResponseEntity.ok(BaseResponse.ok(PageResultDTO.of(
                pageNo,
                pageSize,
                totalCount,
                orders
            )));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("获取订单列表失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateDTO updateDTO) {
        try {
            OrderViewDTO result = orderDelegator.update(updateDTO);
            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("更新订单失败：" + e.getMessage()));
        }
    }
}