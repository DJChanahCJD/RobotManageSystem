package robotManageSystem.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.huawei.innovation.rdm.coresdk.basic.dto.*;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.exception.RDMCoreSDKException;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.PartDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.*;
import com.huawei.innovation.rdm.xdm.delegator.ClassificationNodeDelegator;
import com.huawei.innovation.rdm.xdm.delegator.EXADefinitionLinkDelegator;
import com.huawei.innovation.rdm.xdm.dto.entity.ClassificationNodeViewDTO;
import com.huawei.innovation.rdm.xdm.dto.relation.EXADefinitionLinkViewDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;


import robotManageSystem.dto.Part.PartCreateDTO;
import robotManageSystem.dto.Part.PartUpdateDTO;


@RestController
@RequestMapping("/api/parts")
@CrossOrigin(origins = "*")
public class PartController {
    private static final Logger logger = LoggerFactory.getLogger(PartController.class);

    @Autowired
    private PartDelegator partDelegator;

    @Autowired
    private ClassificationNodeDelegator classificationNodeDelegator;

    @Autowired
    private EXADefinitionLinkDelegator exaDefinitionLinkDelegator;

    @ExceptionHandler(RDMCoreSDKException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleRDMCoreSDKException(RDMCoreSDKException e) {
        logger.error("RDMCoreSDKException occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "RDMCoreSDKException: " + e.getMessage()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Unexpected error: " + e.getMessage()));
    }

    private boolean isClassificationIdValid(Long classificationId) {
        if (classificationId == null) {
            return false;
        }
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            queryRequestVo.addCondition("id", ConditionType.EQUAL, classificationId);
            RDMPageVO rdmPageVO = new RDMPageVO(1, 1);
            List<ClassificationNodeViewDTO> classifications = classificationNodeDelegator.find(queryRequestVo, rdmPageVO);
            return !classifications.isEmpty();
        } catch (Exception e) {
            logger.error("Failed to check classification existence", e);
            return false;
        }
    }

    private boolean areAttributesValidForClassification(Long classificationId, List<EXADefinitionLinkViewDTO> attributes) {
        if (classificationId == null || attributes == null) {
            return false;
        }
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            queryRequestVo.addCondition("targetId", ConditionType.EQUAL, classificationId);
            RDMPageVO rdmPageVO = new RDMPageVO(1, Integer.MAX_VALUE);
            List<EXADefinitionLinkViewDTO> validAttributes = exaDefinitionLinkDelegator.find(queryRequestVo, rdmPageVO);
            return attributes.stream().allMatch(attr -> validAttributes.stream().anyMatch(va -> va.getId().equals(attr.getId())));
        } catch (Exception e) {
            logger.error("Failed to check attributes validity", e);
            return false;
        }
    }


    @PostMapping("/create")
    public ResponseEntity<?> createPart(@RequestBody PartCreateDTO createDTO) {
        try {
            Long classificationId = createDTO.getClassificationId();
            if (!isClassificationIdValid(classificationId)) {
                return ResponseEntity.badRequest().body("Classification ID is invalid");
            }
            List<EXADefinitionLinkViewDTO> attributes = createDTO.getAttributes();
            if (attributes != null && !areAttributesValidForClassification(classificationId, attributes)) {
                return ResponseEntity.badRequest().body("Attributes are not valid for the given classification");
            }
            Object result = partDelegator.create(createDTO);
            return ResponseEntity.ok(Collections.singletonMap("data", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "创建部件失败：" + e.getMessage()));
        }
    }


    // 删除部件前检查是否被产品调用
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePart(@PathVariable Long id) {
        try {
            // 检查部件是否被任何产品调用
            boolean isUsed = checkIfPartIsUsed(id);
            if (isUsed) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "部件正在被使用，无法删除"));
            }
            MasterIdModifierDTO dto = new MasterIdModifierDTO();
            dto.setMasterId(id);
            int result = partDelegator.delete(dto);
            return ResponseEntity.ok(Collections.singletonMap("deleted", result > 0));
        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "删除部件失败：" + e.getMessage()));
        }
    }

    // 检查部件是否被任何产品调用
    private boolean checkIfPartIsUsed(Long partId) {
        // 这里需要实现检查逻辑，例如查询产品部件关系表
        // 返回true如果部件被使用，否则返回false
        // 此处省略具体实现细节
        return false;
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getPart(@PathVariable Long id) {
        try {
            PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
            idDTO.setId(id);
            PartViewDTO part = partDelegator.get(idDTO);
            return ResponseEntity.ok(part);
        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "获取部件失败：" + e.getMessage()));
        }
    }


    @GetMapping("/list")
    public ResponseEntity<?> findParts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryRequestVo.addCondition("name", ConditionType.LIKE, "%" + keyword + "%");
            }

            List<PartViewDTO> parts = partDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = partDelegator.count(queryRequestVo);

            Map<String, Object> result = new HashMap<>();
            result.put("pageSize", pageSize);
            result.put("pageNo", pageNo);
            result.put("totalCount", totalCount);
            result.put("data", parts);

            return ResponseEntity.ok(Collections.singletonMap("result", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "获取部件列表失败：" + e.getMessage()));
        }
    }

    // 获取部件详情
    @GetMapping("/{id}/_detail")
    public ResponseEntity<?> getPartDetail(@PathVariable Long id) {
        try {
            PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
            idDTO.setId(id);
            PartViewDTO part = partDelegator.get(idDTO);
            if (part == null) {
                return ResponseEntity.notFound().build();
            }
            // 这里可以添加逻辑来获取部件的分类和属性信息
            // 例如，通过部件的分类ID去查询分类和属性信息
            Boolean classificationId = part.getLatestIteration();
            List<EXADefinitionLinkViewDTO> attributes = getAttributesByClassificationId(classificationId);
            part.setLastUpdateTime((Timestamp) attributes);
            return ResponseEntity.ok(Collections.singletonMap("data", part));
        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "获取部件详情失败：" + e.getMessage()));
        }
    }

    private List<EXADefinitionLinkViewDTO> getAttributesByClassificationId(Boolean classificationId) {
        return null;
    }

    private List<EXADefinitionLinkViewDTO> getAttributesByClassificationId(Long classificationId) {
        return null;
    }



    // 更新部件

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePart(@PathVariable Long id, @Valid @RequestBody PartUpdateDTO updateDTO) {
        try {
            Long classificationId = updateDTO.getClassificationId();
            if (!isClassificationIdValid(classificationId)) {
                return ResponseEntity.badRequest().body("Classification ID is invalid");
            }
            List<EXADefinitionLinkViewDTO> attributes = updateDTO.getAttributes();
            if (attributes != null && !areAttributesValidForClassification(classificationId, attributes)) {
                return ResponseEntity.badRequest().body("Attributes are not valid for the given classification");
            }
            PartViewDTO result = partDelegator.update(updateDTO);
            return ResponseEntity.ok(result);
        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "更新部件失败：" + e.getMessage()));
        }
    }


    @GetMapping("/classifications")
    public ResponseEntity<?> getClassificationList() {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            queryRequestVo.addCondition("businessCode", ConditionType.LIKE, "");
            RDMPageVO rdmPageVO = new RDMPageVO(1, Integer.MAX_VALUE);

            List<ClassificationNodeViewDTO> classifications =
                    classificationNodeDelegator.find(queryRequestVo, rdmPageVO);

            return ResponseEntity.ok(Collections.singletonMap("data", classifications));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "获取分类列表失败：" + e.getMessage()));
        }
    }

    @GetMapping("/attributes/{classificationId}")
    public ResponseEntity<?> getClassificationAttribute(@PathVariable Long classificationId) {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            queryRequestVo.addCondition("targetId", ConditionType.EQUAL, classificationId);
            RDMPageVO rdmPageVO = new RDMPageVO(1, Integer.MAX_VALUE);
            List<EXADefinitionLinkViewDTO> attributes = exaDefinitionLinkDelegator.find(queryRequestVo, rdmPageVO);
            return ResponseEntity.ok(Collections.singletonMap("data", attributes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "获取分类属性失败：" + e.getMessage()));
        }
    }


    @GetMapping("/{id}/attributes")
    public ResponseEntity<?> getPartAttributes(@PathVariable Long id) {
        try {
            QueryRequestVo queryDTO = new QueryRequestVo();
            queryDTO.addCondition("targetId", ConditionType.EQUAL, id);
            RDMPageVO pageVO = new RDMPageVO(1, 10);

            List<EXADefinitionLinkViewDTO> attributes =
                    exaDefinitionLinkDelegator.queryRelationship(
                            new GenericLinkQueryDTO(),
                            pageVO
                    );
            return ResponseEntity.ok(attributes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "获取部件属性失败：" + e.getMessage()));
        }
    }

}





