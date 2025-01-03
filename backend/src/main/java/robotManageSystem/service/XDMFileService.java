package robotManageSystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.huawei.iit.sdk.common.vo.file.UploadFileModelVO;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMResultVO;
import com.huawei.innovation.rdm.delegate.service.FileDelegatorService;

import robotManageSystem.model.CustomFile;
@Service
public class XDMFileService {

    @Autowired
    private FileDelegatorService fileDelegatorService;

    public RDMResultVO uploadFile(CustomFile customFile) {
        MultipartFile file = customFile.getFile();
        UploadFileModelVO uploadFileModelVO = new UploadFileModelVO();
        uploadFileModelVO.setFile(file);
        uploadFileModelVO.setModelNumber("DM02907836"); // 替换成你的模型编号
        uploadFileModelVO.setModelName("DesignBlueprint"); // 替换成你的模型名称
        uploadFileModelVO.setAttributeName("BluePrint"); // 替换成你的属性名称
        uploadFileModelVO.setApplicationId("1dd2dce363cc4e5fbe951a171a91b825"); // 替换成你的应用ID
        uploadFileModelVO.setUsername("hid_c7i7kks2hw1e4le"); // 替换成你的用户名
        uploadFileModelVO.setStorageType(0);
        uploadFileModelVO.setExaAttr("0");
        // uploadFileModelVO.setInstanceId(customFile.getId());
        uploadFileModelVO.setEncrypted(false);
        uploadFileModelVO.setAutoResolution(true);
        // 设置文件数组
        MultipartFile[] multipartFiles = new MultipartFile[1];
        multipartFiles[0] = file;
        uploadFileModelVO.setFiles(multipartFiles);
        // 打印日志
        System.out.println(uploadFileModelVO);

        // 调用上传
        RDMResultVO result = fileDelegatorService.uploadFile(uploadFileModelVO);
        System.out.println("result from uploadFile: " + result);
        return result;
    }
}