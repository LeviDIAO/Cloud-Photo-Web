package com.cloud.photo.trans.service.impl;

import com.cloud.photo.common.utils.S3Util;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.photo.trans.entity.StorageObject;
import com.cloud.photo.trans.entity.UserFile;
import com.cloud.photo.trans.mapper.UserFileMapper;
import com.cloud.photo.trans.service.IDownloadService;
import com.cloud.photo.trans.service.IStorageObjectService;
import com.cloud.photo.trans.service.IUserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author：kkoneone11
 * @name：DownloadServiceImpl
 * @Date：2023/7/14 16:55
 */
@Service
public class DownloadServiceImpl extends ServiceImpl<UserFileMapper, UserFile> implements IDownloadService {

    @Autowired
    IUserFileService iUserFileService;
    @Autowired
    IStorageObjectService iStorageObjectService;
    /**
     * 通过s3工具的getDownloadUrl()获得下载地址
     * @param userId
     * @param fileId
     * @return
     */
    public String getDownloadUrlByFileid(String userId , String fileId){
        //查询文件信息,为了拿到FileName
        UserFile userFile = iUserFileService.getById(fileId);
        if(userFile == null){
            log.error("getDownloadUrlByFileid() userFile is null,fileId"+ fileId );
            return null;
        }

        //查询入库中文件存储的信息,为了拿到ContainerId和getObjectId
        StorageObject storageObject = iStorageObjectService.getById(userFile.getStorageObjectId());
        if(storageObject == null){
            log.error("getDownloadUrlByFileid() storageObject is null,fileId"+ fileId );
            return null;
        }

        return S3Util.getDownloadUrl(storageObject.getContainerId(),storageObject.getObjectId(),userFile.getFileName());
    }

    /**
     * 用s3工具的getDownloadUrl()和objectId获得下载地址，但参数不同
     * @param containerId
     * @param objectId
     * @return
     */
    public String getDownloadUrl(String containerId, String objectId){
        return S3Util.getDownloadUrl(containerId,objectId);
    }


}
