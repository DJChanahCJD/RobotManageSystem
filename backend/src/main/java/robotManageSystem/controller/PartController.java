package robotManageSystem.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
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
import com.huawei.innovation.rdm.coresdk.extattrmgmt.dto.EXAValueParamDTO;
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
    public ResponseEntity<?> createPart(@RequestBody Map<String, Object> data) {
        System.out.println("data: " + data);
        try {
            PartCreateDTO createDTO = new PartCreateDTO();
            createDTO.setName((String) data.get("name"));
            createDTO.setDescription((String) data.get("description"));
            createDTO.setPartName((String) data.get("partName"));
            createDTO.setExtAttrs((List<EXAValueParamDTO>) data.get("extAttrs"));
            // 处理 clsAttrs
            if (data.get("clsAttrs") != null) {
                List<Map<String, Object>> clsAttrsList = (List<Map<String, Object>>) data.get("clsAttrs");
                if (!clsAttrsList.isEmpty()) {
                    Map<String, Object> firstElement = clsAttrsList.get(0);

                    // 创建新的 JSONArray
                    JSONArray clsAttrs = new JSONArray();

                    // 将 Map 转换为 JSONObject
                    JSONObject jsonObject = new JSONObject(firstElement);

                    clsAttrs.add(jsonObject);
                    createDTO.setClsAttrs(clsAttrs);
                }
            }
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
            @RequestParam(required = false) String classification,
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

            // 如果需要按分类筛选
            if (classification != null && !classification.trim().isEmpty()) {
                parts = parts.stream()
                    .filter(part -> {
                        if (part.getExtAttrs() != null) {
                            return part.getExtAttrs().stream()
                                .anyMatch(attr ->
                                    "Classification".equals(attr.getName()) &&
                                    attr.getValue() != null &&
                                    classification.equals(((Map<?, ?>)attr.getValue()).get("id"))
                                );
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            }

            // 重新计算总数
            long totalCount = classification != null && !classification.trim().isEmpty()
                ? parts.size()
                : partDelegator.count(queryRequestVo);

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
            partData.setPartName((String) updateData.get("partName"));
            partData.setExtAttrs((List<EXAValueParamDTO>) updateData.get("extAttrs"));
            // 处理 clsAttrs
            if (updateData.get("clsAttrs") != null) {
                List<Map<String, Object>> clsAttrsList = (List<Map<String, Object>>) updateData.get("clsAttrs");
                if (!clsAttrsList.isEmpty()) {
                    Map<String, Object> firstElement = clsAttrsList.get(0);

                    // 创建新的 JSONArray
                    JSONArray clsAttrs = new JSONArray();

                    // 将 Map 转换为 JSONObject
                    JSONObject jsonObject = new JSONObject(firstElement);

                    clsAttrs.add(jsonObject);
                    partData.setClsAttrs(clsAttrs);
                }
            }

            // 设置版本控制相关信息
            updateDTO.setMasterId(masterId);
            updateDTO.setData(partData);
            updateDTO.setModifier(checkedOutPart.getModifier());

            System.out.println("updateDTO: " + updateDTO);

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

    @GetMapping("/classification/list")
    public ResponseEntity<?> getClassificationList() {
        try {
            // 1. 构建查询条件 无条件
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            // 添加状态条件，确保只获取有效的分类
            // queryRequestVo.addCondition("status", ConditionType.EQUAL, "ACTIVE");

            // 2. 设置分页
            RDMPageVO rdmPageVO = new RDMPageVO(1, 1000);  // 限制返回数量

            // 3. 执行查询
            List<ClassificationNodeViewDTO> classifications =
                classificationNodeDelegator.find(queryRequestVo, rdmPageVO);

            // 4. 处理空结果
            if (classifications == null) {
                return ResponseEntity.ok(BaseResponse.ok(Collections.emptyList()));
            }

            return ResponseEntity.ok(BaseResponse.ok(classifications));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("获取分类列表失败：" + e.getMessage()));
        }
    }

    // @PostMapping("/classification/create")
    // public ResponseEntity<?> addClassification(
    //     @RequestParam @NotBlank(message = "分类名称不能为空") String name,
    //     @RequestParam @NotBlank(message = "英文名称不能为空") String nameEn,
    //     @RequestBody(required = true) List<String> columnNames
    // ) {
    //     try {
    //         System.out.println("columnNames: " + columnNames);
    //         // 1. 生成业务编码
    //         BusinessCodeVo businessCode = classificationNodeDelegator.generateBusinessCode();
    //         ClassificationNodeCreateDTO createDTO = new ClassificationNodeCreateDTO();
    //         createDTO.setBusinessCode(businessCode.getCode());

    //         // 2. 设置默认值
    //         createDTO.setEnable(true);  // 默认启用
    //         createDTO.setInstantiable(true);  // 默认可实例化
    //         createDTO.setDisableFlag(false);  // 默认不禁用
    //         createDTO.setEnabledTime(new Timestamp(System.currentTimeMillis()));

    //         // 3. 设置分类名称
    //         createDTO.setName(name);
    //         createDTO.setNameEn(nameEn);

    //         // 4. 设置属性
    //         List<EXAValueParamDTO> attributes = new ArrayList<>();
    //         Map<Object, Object> attrMap = new HashMap<>();
    //         for (String columnName : columnNames) {
    //             EXAValueParamDTO attr = new EXAValueParamDTO();
    //             attr.setName(columnName);
    //             Map<String, Object> value = new HashMap<>();
    //             value.put("type", "String");
    //             value.put("defaultValue", "");
    //             attr.setValue(value);
    //             attributes.add(attr);
    //             attrMap.put(columnName, value);
    //         }
    //         createDTO.setExtAttrs(attributes);
    //         createDTO.setExtAttrMap(attrMap);

    //         System.out.println("attributes: " + attributes);
    //         System.out.println("createDTO: " + createDTO);

    //         // 5. 创建分类
    //         ClassificationNodeViewDTO result = classificationNodeDelegator.create(createDTO);
    //         System.out.println("result: " + result);
    //         return ResponseEntity.ok(BaseResponse.ok(result));
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError()
    //             .body(BaseResponse.error("创建分类失败：" + e.getMessage()));
    //     }
    // }

    // @DeleteMapping("/classification/{id}")
    // public ResponseEntity<?> deleteClassification(@PathVariable Long id) {
    //     try {
    //         PersistObjectIdModifierDTO modifier = new PersistObjectIdModifierDTO();
    //         modifier.setId(id);
    //         return ResponseEntity.ok(BaseResponse.ok(classificationNodeDelegator.delete(modifier)));
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError()
    //             .body(BaseResponse.error("删除分类失败：" + e.getMessage()));
    //     }
    // }


@GetMapping("/classification/{classificationId}/attributes")
public ResponseEntity<?> getAttributeByClassificationId(@PathVariable Long classificationId) {
    try {
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        queryRequestVo.addCondition("target.id", ConditionType.EQUAL, classificationId);
        RDMPageVO rdmPageVO = new RDMPageVO(1, 1000);

        List<EXADefinitionLinkViewDTO> attributes = exaDefinitionLinkDelegator.find(queryRequestVo, rdmPageVO);

        // 转换为简化的属性列表
        List<Map<String, Object>> simplifiedAttributes = attributes.stream()
            .map(attr -> {
                Map<String, Object> simple = new HashMap<>();
                simple.put("id", attr.getId());
                simple.put("name", attr.getSource().getName());
                simple.put("nameEn", attr.getSource().getNameEn());
                simple.put("type", attr.getSource().getType());
                simple.put("businessCode", attr.getSource().getBusinessCode());
                return simple;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(BaseResponse.ok(simplifiedAttributes));
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
            .body(BaseResponse.error("获取分类属性失败：" + e.getMessage()));
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