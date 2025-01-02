package robotManageSystem.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Query;

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

import com.alibaba.fastjson.JSON;
import com.huawei.innovation.rdm.coresdk.basic.dto.ObjectReferenceParamDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.bean.enumerate.EngineeringStage;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.DesignBlueprintDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.PartDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.ProductBlueprintLinkDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.ProductDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.ProductPartLinkDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.DesignBlueprintViewDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.PartViewDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.ProductCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.ProductUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.ProductViewDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.relation.ProductBlueprintLinkCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.relation.ProductBlueprintLinkViewDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.relation.ProductPartLinkCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.relation.ProductPartLinkUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.relation.ProductPartLinkViewDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.relation.ProductBlueprintLinkUpdateDTO;
import robotManageSystem.dto.BaseResponse;
import robotManageSystem.dto.PageResultDTO;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class ProductController {
    @Autowired
    private ProductDelegator productDelegator;
    @Autowired
    private ProductBlueprintLinkDelegator productBlueprintLinkDelegator;
    @Autowired
    private ProductPartLinkDelegator productPartLinkDelegator;
    @Autowired
    private PartDelegator partDelegator;
    @Autowired
    private DesignBlueprintDelegator designBlueprintDelegator;


    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> createData) {
        try {
            System.out.println("开始创建产品: " + createData);

            // 1. 创建产品
            ProductCreateDTO createDTO = new ProductCreateDTO();
            createDTO.setProductName((String) createData.get("productName"));
            createDTO.setProductOwner((String) createData.get("productOwner"));
            createDTO.setProductInformation((String) createData.get("productInformation"));
            createDTO.setProductStage(EngineeringStage.valueOf((String) createData.get("productStage")));

            // 创建产品
            ProductViewDTO result = productDelegator.create(createDTO);

            // 2. 如果有蓝图ID，创建关联关系
            Long blueprintId = Long.parseLong(createData.get("blueprintId").toString());
            if (blueprintId != null && blueprintId > 0) {
                ProductBlueprintLinkCreateDTO linkDTO = new ProductBlueprintLinkCreateDTO();

                // 设置源（产品）和目标（蓝图）
                linkDTO.setSource(new ObjectReferenceParamDTO(
                    result.getId(),      // 新创建的产品ID
                    "Product",           // 实体类名
                    "Product"            // 类名
                ));

                linkDTO.setTarget(new ObjectReferenceParamDTO(
                    blueprintId,
                    "DesignBlueprint",
                    "DesignBlueprint"
                ));

                // 创建关联
                productBlueprintLinkDelegator.create(linkDTO);
            }

            // 3. 如果有部件ID，创建关联关系
            Long partId = Long.parseLong(createData.get("partId").toString());
            if (partId != null && partId > 0) {
                ProductPartLinkCreateDTO linkDTO = new ProductPartLinkCreateDTO();
                linkDTO.setSource(new ObjectReferenceParamDTO(
                    result.getId(),
                    "Product",
                    "Product"
                ));
                linkDTO.setTarget(new ObjectReferenceParamDTO(
                    partId,
                    "Part",
                    "Part"
                ));
                productPartLinkDelegator.create(linkDTO);
            }
            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            System.out.println("创建产品失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("创建产品失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            PersistObjectIdModifierDTO idDTO = new PersistObjectIdModifierDTO();
            idDTO.setId(id);
            productDelegator.delete(idDTO);
            return ResponseEntity.ok(BaseResponse.ok(Collections.singletonMap("message", "产品删除成功")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("删除产品失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            // 1. 创建ID解密DTO
            PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
            idDTO.setId(id);
            Map<String, Object> result = new HashMap<>();

            // 2. 获取产品基本信息
            ProductViewDTO product = productDelegator.get(idDTO);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            result.put("product", product);

            // 3. 获取零件关联，然后获取零件详情
            QueryRequestVo queryRequestVo = QueryRequestVo.build();
            QueryCondition condition = queryRequestVo.or();
            condition.addCondition("source.id", ConditionType.EQUAL, id);
            RDMPageVO pageVO = new RDMPageVO(1, 1);
            try {
                List<ProductPartLinkViewDTO> partLinks = productPartLinkDelegator.find(queryRequestVo, pageVO);
                if (!partLinks.isEmpty()) {
                    ProductPartLinkViewDTO partLink = partLinks.get(0);
                    if (partLink != null && partLink.getTarget() != null) {
                        System.out.println("获取零件关联成功: " + JSON.toJSONString(partLink));
                        // 使用关联中的目标ID获取零件详情
                        PersistObjectIdDecryptDTO partIdDTO = new PersistObjectIdDecryptDTO();
                        partIdDTO.setId(partLink.getTarget().getId());
                        PartViewDTO part = partDelegator.get(partIdDTO);
                        result.put("part", part);
                    }
                }
            } catch (Exception e) {
                System.out.println("获取零件信息失败: " + e.getMessage());
            }

            // 4. 获取蓝图关联，然后获取蓝图详情
            try {
                List<ProductBlueprintLinkViewDTO> blueprintLinks = productBlueprintLinkDelegator.find(queryRequestVo, pageVO);
                if (!blueprintLinks.isEmpty()) {
                    ProductBlueprintLinkViewDTO blueprintLink = blueprintLinks.get(0);
                    if (blueprintLink != null && blueprintLink.getTarget() != null) {
                        System.out.println("获取蓝图关联成功: " + JSON.toJSONString(blueprintLink));
                        // 使用关联中的目标ID获取蓝图详情
                    PersistObjectIdDecryptDTO blueprintIdDTO = new PersistObjectIdDecryptDTO();
                    blueprintIdDTO.setId(blueprintLink.getTarget().getId());
                    DesignBlueprintViewDTO blueprint = designBlueprintDelegator.get(blueprintIdDTO);
                    result.put("blueprint", blueprint);
                    }
                }
            } catch (Exception e) {
                System.out.println("获取蓝图信息失败: " + e.getMessage());
            }

            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            System.out.println("获取产品详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("获取产品失败：" + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> findProducts(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productOwner,
            @RequestParam(required = false) String productInformation,
            @RequestParam(required = false) EngineeringStage productStage,
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String id,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            System.out.println("开始获取产品列表: " + productName + " " + productOwner + " " + productInformation + " " + productStage + " " + partName + " " + id + " " + pageNo + " " + pageSize);
            QueryRequestVo queryRequestVo = QueryRequestVo.build();
            QueryCondition condition = queryRequestVo.and();

            // 产品名称模糊搜索
            if (productName != null && !productName.trim().isEmpty()) {
                condition.addCondition("productName", ConditionType.LIKE, productName.trim());
            }

            // 产品负责人模糊搜索
            if (productOwner != null && !productOwner.trim().isEmpty()) {
                condition.addCondition("productOwner", ConditionType.LIKE, productOwner.trim());
            }

            // 产品基本信息模糊搜索
            if (productInformation != null && !productInformation.trim().isEmpty()) {
                condition.addCondition("productInformation", ConditionType.LIKE, productInformation.trim());
            }

            // 产品工程阶段精确查询
            if (productStage != null) {
                condition.addCondition("productStage", ConditionType.EQUAL, productStage);
            }

            // 部件名称模糊搜索
            if (partName != null && !partName.trim().isEmpty()) {
                condition.addCondition("partName", ConditionType.LIKE, partName.trim());
            }

            // ID精确查询
            if (id != null && !id.trim().isEmpty()) {
                Long idValue = Long.parseLong(id.trim());
                condition.addCondition("id", ConditionType.EQUAL, idValue);
            }

            // 设置排序（按创建时间倒序）
            queryRequestVo.setOrderBy("createTime")
                         .setSort("DESC");

            // 执行查询
            List<ProductViewDTO> products = productDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = productDelegator.count(queryRequestVo);

            // 使用 PageResultDTO 封装返回结果
            return ResponseEntity.ok(BaseResponse.ok(PageResultDTO.of(
                pageNo,
                pageSize,
                totalCount,
                products
            )));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("获取产品列表失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateDTO updateDTO, @RequestParam(required = false) String blueprintId, @RequestParam(required = false) String partId) {
        try {
            ProductViewDTO result = productDelegator.update(updateDTO);
            if (blueprintId != null && blueprintId.trim().isEmpty()) {
                ProductBlueprintLinkUpdateDTO linkDTO = new ProductBlueprintLinkUpdateDTO();
                linkDTO.setSource(new ObjectReferenceParamDTO(id, "Product", "Product"));
                linkDTO.setTarget(new ObjectReferenceParamDTO(Long.parseLong(blueprintId), "DesignBlueprint", "DesignBlueprint"));
                productBlueprintLinkDelegator.update(linkDTO);
            }
            if (partId != null && partId.trim().isEmpty()) {
                ProductPartLinkUpdateDTO linkDTO = new ProductPartLinkUpdateDTO();
                linkDTO.setSource(new ObjectReferenceParamDTO(id, "Product", "Product"));
                linkDTO.setTarget(new ObjectReferenceParamDTO(Long.parseLong(partId), "Part", "Part"));
                productPartLinkDelegator.update(linkDTO);
            }
            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("更新产品失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<?> getLink(@PathVariable Long id) {
        PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
        idDTO.setId(id);
        ProductViewDTO product = productDelegator.get(idDTO);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        QueryRequestVo queryRequestVo = QueryRequestVo.build();
        QueryCondition condition = queryRequestVo.or();
        condition.addCondition("source.id", ConditionType.EQUAL, id);
        List<ProductBlueprintLinkViewDTO> blueprintLinks = productBlueprintLinkDelegator.find(queryRequestVo, new RDMPageVO(1, 1));
        List<ProductPartLinkViewDTO> partLinks = productPartLinkDelegator.find(queryRequestVo, new RDMPageVO(1, 1));
        Map<String, Object> result = new HashMap<>();
        result.put("blueprintId", blueprintLinks.get(0).getTarget().getId());
        result.put("partId", partLinks.get(0).getTarget().getId());
        return ResponseEntity.ok(BaseResponse.ok(result));
    }
}