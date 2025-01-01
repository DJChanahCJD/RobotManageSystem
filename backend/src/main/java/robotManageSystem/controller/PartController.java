package robotManageSystem.controller;

import java.util.Collections;
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

import com.huawei.innovation.rdm.coresdk.basic.dto.GenericLinkQueryDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.MasterIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.VersionCheckOutDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.VersionUndoCheckOutDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.VersionUpdateAndCheckinDTO;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.exception.RDMCoreSDKException;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.PartDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.PartBranchCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.PartCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.PartMasterCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.PartUpdateByAdminDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.PartViewDTO;
import com.huawei.innovation.rdm.xdm.delegator.ClassificationNodeDelegator;
import com.huawei.innovation.rdm.xdm.delegator.EXADefinitionLinkDelegator;
import com.huawei.innovation.rdm.xdm.dto.entity.ClassificationNodeViewDTO;
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
    @PutMapping("/{masterId}")
    public ResponseEntity<?> updatePart(@PathVariable Long masterId,
                                      @RequestBody Map<String, Object> updateData) {
        try {
            // 1. 先进行签出操作
            VersionCheckOutDTO checkOutDTO = new VersionCheckOutDTO();
            checkOutDTO.setMasterId(masterId);
            PartViewDTO checkedOutPart = partDelegator.checkout(checkOutDTO);

            if (checkedOutPart == null) {
                return ResponseEntity.badRequest()
                    .body(BaseResponse.error("签出失败"));
            }

            // 2. 准备更新数据
            VersionUpdateAndCheckinDTO<PartUpdateByAdminDTO> updateDTO = new VersionUpdateAndCheckinDTO<>();
            PartUpdateByAdminDTO partData = new PartUpdateByAdminDTO();

            // 设置基本信息
            partData.setId(masterId);
            partData.setName((String) updateData.get("name"));
            partData.setDescription((String) updateData.get("description"));

            // 设置版本控制相关信息
            updateDTO.setMasterId(masterId);
            updateDTO.setData(partData);
            updateDTO.setModifier(checkedOutPart.getModifier());

            // 3. 执行更新并签入操作
            PartViewDTO result = partDelegator.updateAndCheckin(updateDTO);

            return ResponseEntity.ok(BaseResponse.ok(result));

        } catch (Exception e) {
            // 发生错误时尝试撤销签出
            try {
                VersionUndoCheckOutDTO undoDTO = new VersionUndoCheckOutDTO();
                undoDTO.setMasterId(masterId);
                partDelegator.undoCheckout(undoDTO);
            } catch (Exception ex) {
                System.out.println("撤销签出失败: " + ex.getMessage());
            }

            System.out.println("更新部件失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("更新失败：" + e.getMessage()));
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