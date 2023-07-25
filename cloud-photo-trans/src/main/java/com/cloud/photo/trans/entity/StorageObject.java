package com.cloud.photo.trans.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 资源池文件存储信息
 * </p>
 *
 * @author kkoneone11
 * @since 2023-07-13
 */
@TableName("tb_storage_object")
public class StorageObject implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     *
     */
    //getById方法要加入这个注解字段，即自动生成id，不然mybatis-plus识别不出哪个是id，就使用不了getById方法。
    @TableId(type = IdType.ASSIGN_UUID)
    private String storageObjectId;

    /**
     * 存储池供应商
     */
    private String storageProvider;

    /**
     * 桶ID
     */
    private String containerId;

    /**
     * 存储池文件id
     */
    private String objectId;

    /**
     * 文件md5
     */
    private String md5;

    /**
     * 文件大小
     */
    private Long objectSize;

    //要有一个空的构造方法，不然会报错
    public StorageObject() {
    }

    public StorageObject(String storageProvider, String containerId, String objectId, String md5, Long objectSize){
        this.storageProvider = storageProvider;
        this.containerId = containerId;
        this.objectId = objectId;
        this.md5 = md5;
        this.objectSize = objectSize;
    }


    public String getStorageObjectId() {
        return storageObjectId;
    }

    public void setStorageObjectId(String storageObjectId) {
        this.storageObjectId = storageObjectId;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider) {
        this.storageProvider = storageProvider;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getObjectSize() {
        return objectSize;
    }

    public void setObjectSize(Long objectSize) {
        this.objectSize = objectSize;
    }

    @Override
    public String toString() {
        return "StorageObject{" +
            "storageObjectId = " + storageObjectId +
            ", storageProvider = " + storageProvider +
            ", containerId = " + containerId +
            ", objectId = " + objectId +
            ", md5 = " + md5 +
            ", objectSize = " + objectSize +
        "}";
    }
}
