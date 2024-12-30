package robotManageSystem.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import java.util.ArrayList;
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
import com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionViewDTO;
import com.huawei.innovation.rdm.xdm.dto.relation.EXADefinitionLinkViewDTO;

@RestController
@RequestMapping("/api/parts")
@CrossOrigin(origins = "*")
public class PartController {
    @Autowired
    private PartDelegator partDelegator;

    @Autowired
    private ClassificationNodeDelegator classificationNodeDelegator;

    @Autowired
    private EXADefinitionLinkDelegator exaDefinitionLinkDelegator;

    @PostMapping("/create")
    public ResponseEntity<?> createPart(@RequestBody PartCreateDTO createDTO) {
        try {
            // 创建主对象
            PartMasterCreateDTO pmcd = new PartMasterCreateDTO();

            // 创建分支对象
            PartBranchCreateDTO pbcd = new PartBranchCreateDTO();
            // 设置Part的主对象和分支对象
            createDTO.setMaster(pmcd);
            createDTO.setBranch(pbcd);

            Object result = partDelegator.create(createDTO);
            return ResponseEntity.ok(Collections.singletonMap("data", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "创建部件失败：" + e.getMessage()));
        }
    }

    // 删除部件
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePart(@PathVariable Long id) {
        try {
            MasterIdModifierDTO dto = new MasterIdModifierDTO();
            dto.setMasterId(id);
            int result = partDelegator.delete(dto);
            return ResponseEntity.ok(Collections.singletonMap("deleted", result > 0));
        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "删除部件失败：" + e.getMessage()));
        }
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

    // 更新部件
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePart(@PathVariable Long id, @Valid @RequestBody PartUpdateDTO updateDTO) {
        try {
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