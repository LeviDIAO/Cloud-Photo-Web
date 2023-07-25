package com.cloud.photo.image.service;

import com.cloud.photo.image.entity.FileResizeIcon;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.File;

/**
 * <p>
 * 图片缩略图 服务类
 * </p>
 *
 * @author kkoneone11
 * @since 2023-07-15
 */
public interface IFileResizeIconService extends IService<FileResizeIcon> {

    FileResizeIcon getFileResizeIcon(String storageObjectId , String iconCode);

    String downloadImage(String containerId , String objectId , String suffixName);

    FileResizeIcon imageThumbnailSave(String iconCode , String suffixName , String srcFileName,
                                      String storageObjectId , String fileName);

    FileResizeIcon uploadIcon(String userId, String storageObjectId, String s, File iconFile, String fileName);

    void imageThumbnailAndMediaInfo(String storageObjectId, String fileName);

    String getIconUrl(String userId,String fileId,String iconCode);
}
