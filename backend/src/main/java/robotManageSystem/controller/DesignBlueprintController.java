package robotManageSystem.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.huawei.innovation.rdm.bean.entity.XDMFileModel;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.exception.RDMCoreSDKException;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.dto.entity.XDMFileModelViewDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.DesignBlueprintDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.DesignBlueprintCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.DesignBlueprintSaveDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.DesignBlueprintUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.DesignBlueprintViewDTO;

import robotManageSystem.dto.BaseResponse;

@RestController
@RequestMapping("/api/blueprint")
@CrossOrigin(origins = "*")
public class DesignBlueprintController {
    @Autowired
    private DesignBlueprintDelegator designBlueprintDelegator;

    @PostMapping("/create")
    public ResponseEntity<?> createBlueprint(@RequestBody DesignBlueprintCreateDTO createDTO) {
        try {
            System.out.println("开始创建设计蓝图: " + createDTO);
            Object result = designBlueprintDelegator.create(createDTO);
            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("创建设计蓝图失败：" + e.getMessage()));
        }
    }

    // 上传蓝图文件
    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadBlueprintFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1. 验证蓝图是否存在
            PersistObjectIdDecryptDTO queryDto = new PersistObjectIdDecryptDTO();
            queryDto.setId(id);
            DesignBlueprintViewDTO blueprint = designBlueprintDelegator.get(queryDto);

            if (blueprint == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. 构造文件模型
            XDMFileModel fileModel = new XDMFileModel();
            fileModel.setName(file.getOriginalFilename());

            // 3. 构造保存参数
            DesignBlueprintSaveDTO saveDto = new DesignBlueprintSaveDTO();
            saveDto.setBluePrint(Collections.singletonList(fileModel));

            // 4. 调用保存方法
            List<DesignBlueprintSaveDTO> dtoList = Collections.singletonList(saveDto);
            int result = designBlueprintDelegator.save(dtoList);

            if (result > 0) {
                return ResponseEntity.ok(Collections.singletonMap("data", "文件上传成功"));
            } else {
                return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "文件上传失败"));
            }

        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "保存蓝图失败：" + e.getMessage()));
        }
    }

    // 下载蓝图文件
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadBlueprintFile(@PathVariable Long id) {
        try {
            // 1. 获取蓝图信息
            PersistObjectIdDecryptDTO dto = new PersistObjectIdDecryptDTO();
            dto.setId(id);
            DesignBlueprintViewDTO blueprint = designBlueprintDelegator.get(dto);

            if (blueprint == null || blueprint.getBluePrint() == null || blueprint.getBluePrint().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 2. 获取文件信息
            XDMFileModelViewDTO fileModel = blueprint.getBluePrint().get(0);

            // 3. 返回文件
            return ResponseEntity.ok()
                .header("Content-Disposition",
                    "attachment; filename=" + fileModel.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(BaseResponse.ok(fileModel));

        } catch (RDMCoreSDKException e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("获取蓝图失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlueprint(@PathVariable Long id) {
        try {
            PersistObjectIdModifierDTO idDTO = new PersistObjectIdModifierDTO();
            idDTO.setId(id);
            designBlueprintDelegator.delete(idDTO);
            return ResponseEntity.ok(BaseResponse.ok(Collections.singletonMap("message", "设计蓝图删除成功")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("删除设计蓝图失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBlueprint(@PathVariable Long id) {
        try {
            PersistObjectIdDecryptDTO idDTO = new PersistObjectIdDecryptDTO();
            idDTO.setId(id);
            DesignBlueprintViewDTO blueprint = designBlueprintDelegator.get(idDTO);
            return ResponseEntity.ok(BaseResponse.ok(blueprint));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("获取设计蓝图失败：" + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> findBlueprints(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryRequestVo.setConditions(Collections.singletonList(new QueryCondition("description", keyword)));
            }

            List<DesignBlueprintViewDTO> blueprints = designBlueprintDelegator.find(queryRequestVo, new RDMPageVO(pageNo, pageSize));
            long totalCount = designBlueprintDelegator.count(queryRequestVo);

            Map<String, Object> result = new HashMap<>();
            result.put("pageSize", pageSize);
            result.put("pageNo", pageNo);
            result.put("totalCount", totalCount);
            result.put("data", blueprints);

            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("获取设计蓝图列表失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlueprint(@PathVariable Long id, @RequestBody DesignBlueprintUpdateDTO updateDTO) {
        try {
            DesignBlueprintViewDTO result = designBlueprintDelegator.update(updateDTO);
            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("更新设计蓝图失败：" + e.getMessage()));
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadBlueprint(@PathVariable Long id) {
        try {
            // TODO: 实现文件下载逻辑
            // 返回文件流
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=blueprint.pdf")
                .body(null/* 文件流对象 */);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("下载蓝图文件失败：" + e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> countBlueprints() {
        try {
            long count = designBlueprintDelegator.count(new QueryRequestVo());
            return ResponseEntity.ok(BaseResponse.ok(Collections.singletonMap("count", count)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("获取设计蓝图数量失败：" + e.getMessage()));
        }
    }
}