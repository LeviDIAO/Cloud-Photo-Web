package com.cloud.photo.trans.service;

import com.cloud.photo.common.bo.FileUploadBo;
import com.cloud.photo.common.common.CommonEnum;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.photo.trans.entity.UserFile;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author：kkoneone11
 * @name：IPutUploadUrlService
 * @Date：2023/7/13 17:16
 */

/**
 * 针对UserFile数据库的操作
 */
public interface IPutUploadUrlService extends IService<UserFile> {

    /**
     * 获得上传地址
     * @param fileName
     * @param fileSize
     * @param fileMd5
     * @return
     */
    String getPutUploadUrl(@RequestParam(value = "fileName") String fileName,
                                  @RequestParam(value = "fileSize") Long fileSize,
                                  @RequestParam(value = "fileMd5") String fileMd5);

    /**
     * 非秒传处理
     *
     */
    CommonEnum commit(FileUploadBo bo);

    /**
     * 秒传处理
     */
    CommonEnum commitTransSecond(FileUploadBo bo);
}
