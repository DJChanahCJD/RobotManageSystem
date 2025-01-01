package robotManageSystem.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.huawei.innovation.rdm.coresdk.basic.dto.*;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.exception.RDMCoreSDKException;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.PartDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.*;
import com.huawei.innovation.rdm.xdm.delegator.ClassificationNodeDelegator;
import com.huawei.innovation.rdm.xdm.delegator.EXADefinitionLinkDelegator;
import com.huawei.innovation.rdm.xdm.dto.entity.ClassificationNodeViewDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionViewDTO;
import com.huawei.innovation.rdm.xdm.dto.relation.EXADefinitionLinkViewDTO;

import robotManageSystem.dto.BaseResponse;
import robotManageSystem.dto.PageResultDTO;

@RestController
@RequestMapping("/api/part")
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
            MasterIdModifierDTO modifierDTO = new MasterIdModifierDTO();
            modifierDTO.setMasterId(id);
            int result = partDelegator.delete(modifierDTO);
            if (result > 0) {
                return ResponseEntity.ok(BaseResponse.ok(result));
            } else {
                return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("删除部件失败: " + result));
            }
        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("删除部件失败：" + e.getMessage()));
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
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String version,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = QueryRequestVo.build();
            QueryCondition condition = queryRequestVo.and();

            if (name != null && !name.trim().isEmpty()) {
                condition.addCondition("name", ConditionType.LIKE, name);
            }
            if (description != null && !description.trim().isEmpty()) {
                condition.addCondition("description", ConditionType.LIKE, description);
            }
            if (version != null && !version.trim().isEmpty()) {
                condition.addCondition("version", ConditionType.LIKE, version);
            }
            if (id != null && !id.trim().isEmpty()) {
                condition.addCondition("id", ConditionType.EQUAL, id);
            }

            queryRequestVo.setOrderBy("createTime").setSort("DESC");

            // 执行查询
            List<PartViewDTO> parts = partDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = partDelegator.count(queryRequestVo);

            return ResponseEntity.ok(BaseResponse.ok(PageResultDTO.of(
                pageNo,
                pageSize,
                totalCount,
                parts
            )));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("获取部件列表失败：" + e.getMessage()));
        }
    }

    // 更新部件
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePart(@PathVariable Long id, @RequestBody Map<String, Object> updateData) {
        try {
            // 参数校验
            if (updateData == null || updateData.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(BaseResponse.error("更新数据不能为空"));
            }

            // 创建更新DTO
            PartUpdateDTO updateDTO = new PartUpdateDTO();
            updateDTO.setId(id);
            updateDTO.setNeedSetNullAttrs(Arrays.asList("name", "description"));
            updateDTO.setRdmExtensionType("PART");
            updateDTO.setSecurityLevel("0");
            // 设置基本属性
            if (updateData.containsKey("name")) {
                updateDTO.setName((String) updateData.get("name"));
            }
            if (updateData.containsKey("description")) {
                updateDTO.setDescription((String) updateData.get("description"));
            }

            System.out.println("[before set branch] updateDTO: " + updateDTO);
            // 创建主数据对象
            PartMasterUpdateDTO masterDTO = new PartMasterUpdateDTO();
            // 创建分支数据对象
            PartBranchUpdateDTO branchDTO = new PartBranchUpdateDTO();

            // 设置主数据和分支数据
            updateDTO.setMaster(masterDTO);
            updateDTO.setBranch(branchDTO);

            // 执行更新操作
            PartViewDTO result = partDelegator.update(updateDTO);

            System.out.println("[after update] result: " + result);
            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (ClassCastException e) {
            return ResponseEntity.badRequest()
                .body(BaseResponse.error("数据格式错误：" + e.getMessage()));
        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("更新部件失败：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("系统错误：" + e.getMessage()));
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