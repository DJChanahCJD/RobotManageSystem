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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.OrderDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.OrderCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.OrderUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.OrderViewDTO;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired
    private OrderDelegator orderDelegator;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateDTO createDTO) {
        try {
            System.out.println("开始创建订单: " + createDTO);
            Object result = orderDelegator.create(createDTO);
            System.out.println("创建结果: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("创建订单失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "创建订单失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            PersistObjectIdModifierDTO idDTO = new PersistObjectIdModifierDTO();
            idDTO.setId(id);
            orderDelegator.delete(idDTO);
            return ResponseEntity.ok(Collections.singletonMap("message", "订单删除成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "删除订单失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
            idDTO.setId(id);
            OrderViewDTO order = orderDelegator.get(idDTO);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "获取订单失败：" + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> findOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 可以根据订单名称、类型等进行搜索
                // queryRequestVo.setCondition(...);
            }

            List<OrderViewDTO> orders = orderDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = orderDelegator.count(queryRequestVo);

            Map<String, Object> result = new HashMap<>();
            result.put("pageSize", pageSize);
            result.put("pageNo", pageNo);
            result.put("totalCount", totalCount);
            result.put("data", orders);

            return ResponseEntity.ok(Collections.singletonMap("result", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "获取订单列表失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateDTO updateDTO) {
        try {
            OrderViewDTO result = orderDelegator.update(updateDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "更新订单失败：" + e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> countOrders() {
        try {
            long count = orderDelegator.count(new QueryRequestVo());
            return ResponseEntity.ok(Collections.singletonMap("count", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "获取订单数量失败：" + e.getMessage()));
        }
    }
}