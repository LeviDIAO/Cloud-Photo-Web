package com.cloud.photo.trans.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.photo.trans.entity.UserFile;

/**
 * @Author：kkoneone11
 * @name：IDownloadService
 * @Date：2023/7/14 16:53
 */
public interface IDownloadService extends IService<UserFile> {
    String getDownloadUrlByFileid(String userId , String fileId);
    String getDownloadUrl(String containerId, String objectId);
}
