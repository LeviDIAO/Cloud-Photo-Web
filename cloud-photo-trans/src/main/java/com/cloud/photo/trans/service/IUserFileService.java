package com.cloud.photo.trans.service;

import com.cloud.photo.common.bo.FileUploadBo;
import com.cloud.photo.trans.entity.UserFile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kkoneone11
 * @since 2023-07-13
 */
public interface IUserFileService extends IService<UserFile> {

    /**
     * 根据FileUploadBo来设置的save方法
     * @param bo
     */

    boolean saveAndFileDeal(FileUploadBo bo);

//    String getDownloadUrlByFileid(String userId , String fileId);
}
