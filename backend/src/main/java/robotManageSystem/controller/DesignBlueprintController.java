package robotManageSystem.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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

import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.exception.RDMCoreSDKException;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMResultVO;
import com.huawei.innovation.rdm.delegate.service.FileDelegatorService;
import com.huawei.innovation.rdm.dto.entity.XDMFileModelViewDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.delegator.DesignBlueprintDelegator;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.DesignBlueprintCreateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.DesignBlueprintUpdateDTO;
import com.huawei.innovation.rdm.intelligentrobotengineering.dto.entity.DesignBlueprintViewDTO;

import robotManageSystem.dto.BaseResponse;
import robotManageSystem.model.CustomFile;
import robotManageSystem.service.XDMFileService;
@RestController
@RequestMapping("/api/blueprint")
@CrossOrigin(origins = "*")
public class DesignBlueprintController {
    @Autowired
    private DesignBlueprintDelegator designBlueprintDelegator;

    @Autowired
    private XDMFileService xdmFileService;

    @Autowired
    private FileDelegatorService fileDelegatorService;

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
            @RequestParam("bluePrint") MultipartFile file
    ) {
        System.out.println("开始上传蓝图文件: " + id + " " + file.getOriginalFilename());
        try {
            // 1. 验证蓝图是否存在
            PersistObjectIdDecryptDTO queryDto = new PersistObjectIdDecryptDTO();
            queryDto.setId(id);
            DesignBlueprintViewDTO blueprint = designBlueprintDelegator.get(queryDto);

            if (blueprint == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. 构造自定义文件对象
            CustomFile customFile = new CustomFile();
            customFile.setId(String.valueOf(id));
            customFile.setFile(file);

            // 3. 上传文件
            RDMResultVO result = xdmFileService.uploadFile(customFile);
            System.out.println("result from uploadFile: " + result);

            if (result == null || result.getData() == null) {
                return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("文件上传失败"));
            }

            return ResponseEntity.ok(BaseResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(BaseResponse.error("上传蓝图失败：" + e.getMessage()));
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
            @RequestParam(required = false) String BlueprintDescription,
            @RequestParam(required = false) String ID,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            QueryRequestVo queryRequestVo = new QueryRequestVo();

            // 处理描述的模糊查询
            if (BlueprintDescription != null && !BlueprintDescription.trim().isEmpty()) {
                queryRequestVo.addCondition("blueprintDescription", ConditionType.LIKE, BlueprintDescription);
            }

            // 处理ID的精确查询
            if (ID != null && !ID.trim().isEmpty()) {
                try {
                    Long idValue = Long.parseLong(ID.trim());
                    queryRequestVo.addCondition("id", ConditionType.EQUAL, idValue);  // 使用 EQUAL 而不是 LIKE
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                        .body(BaseResponse.error("ID必须是数字"));
                }
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
        System.out.println("开始更新设计蓝图: " + updateDTO);
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

    @GetMapping("/image/{fileId}")
    public void getImage(
            @PathVariable String fileId,
            @RequestParam String instanceId,
            HttpServletResponse response
    ) {
        try {
            // 设置响应类型
            response.setContentType("image/png");  // 根据实际图片类型设置

            // 调用下载
            fileDelegatorService.downloadFile(
                fileId,
                "DesignBlueprint",
                "BluePrint",
                instanceId,
                "1dd2dce363cc4e5fbe951a171a91b825",
                "0",
                response
            );
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}