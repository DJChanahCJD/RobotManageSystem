package robotManageSystem.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
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
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.ProductDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.ProductCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.ProductUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.ProductViewDTO;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    @Autowired
    private ProductDelegator productDelegator;

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody ProductCreateDTO createDTO) {
        try {
            System.out.println("开始创建产品: " + createDTO);
            Object result = productDelegator.create(createDTO);
            System.out.println("创建结果: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("创建产品失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "创建产品失败：" + e.getMessage()));
        }
    }

    //删除产品
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            PersistObjectIdModifierDTO idDTO = new PersistObjectIdModifierDTO();
            idDTO.setId(id);
            productDelegator.delete(idDTO);
            return ResponseEntity.ok(Collections.singletonMap("message", "产品删除成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "删除产品失败：" + e.getMessage()));
        }
    }


    //获取产品
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
            idDTO.setId(id);
            ProductViewDTO product = productDelegator.get(idDTO);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "获取产品失败：" + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> findProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            if (keyword != null && !keyword.trim().isEmpty()) {

                //根据产品名称进行模糊搜索
                queryRequestVo.addCondition("name", ConditionType.LIKE,"%" + keyword + "%");
            }

            List<ProductViewDTO> products = productDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = productDelegator.count(queryRequestVo);

            Map<String, Object> result = new HashMap<>();
            result.put("pageSize", pageSize);
            result.put("pageNo", pageNo);
            result.put("totalCount", totalCount);
            result.put("data", products);

            return ResponseEntity.ok(Collections.singletonMap("result", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "获取产品列表失败：" + e.getMessage()));
        }
    }

    //更新产品
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateDTO updateDTO) {
        try {
            ProductViewDTO result = productDelegator.update(updateDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "更新产品失败：" + e.getMessage()));
        }
    }

    //获取产品数量
    @GetMapping("/count")
    public ResponseEntity<?> countProducts() {
        try {
            long count = productDelegator.count(new QueryRequestVo());
            return ResponseEntity.ok(Collections.singletonMap("count", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "获取产品数量失败：" + e.getMessage()));
        }
    }

    @GetMapping("/by-stage/{stage}")
    public ResponseEntity<?> findProductsByStage(
            @PathVariable String stage,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            // 设置产品阶段查询条件
            // queryRequestVo.setCondition("ProductStage", stage);

            //根据产品阶段进行查询
            queryRequestVo.addCondition("stage", ConditionType.EQUAL, stage);

            List<ProductViewDTO> products = productDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = productDelegator.count(queryRequestVo);

            Map<String, Object> result = new HashMap<>();
            result.put("pageSize", pageSize);
            result.put("pageNo", pageNo);
            result.put("totalCount", totalCount);
            result.put("data", products);

            return ResponseEntity.ok(Collections.singletonMap("result", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "按阶段获取产品列表失败：" + e.getMessage()));
        }
    }
}