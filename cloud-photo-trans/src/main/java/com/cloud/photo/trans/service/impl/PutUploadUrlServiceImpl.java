package com.cloud.photo.trans.service.impl;

import com.cloud.photo.common.bo.FileUploadBo;
import com.cloud.photo.common.common.CommonEnum;
import com.cloud.photo.common.utils.S3Util;
import cn.hutool.json.JSONObject;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.photo.trans.entity.FileMd5;
import com.cloud.photo.trans.entity.StorageObject;
import com.cloud.photo.trans.entity.UserFile;
import com.cloud.photo.trans.mapper.UserFileMapper;
import com.cloud.photo.trans.service.IFileMd5Service;
import com.cloud.photo.trans.service.IPutUploadUrlService;
import com.cloud.photo.trans.service.IStorageObjectService;
import com.cloud.photo.trans.service.IUserFileService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author：kkoneone11
 * @name：PutUploadUrlServiceImpl
 * @Date：2023/7/13 17:19
 */
@Service
public class PutUploadUrlServiceImpl extends ServiceImpl<UserFileMapper, UserFile> implements IPutUploadUrlService {

    @Autowired
    private IStorageObjectService iStorageObjectService;
    @Autowired
    private IFileMd5Service iFileMd5Service;
    @Autowired
    private IUserFileService iUserFileService;
    /**
     * 根据s3工具类
     * @param fileName
     * @param fileSize
     * @param fileMd5
     * @return json格式的字符串形式
     */
    @Override
    public String getPutUploadUrl(String fileName, Long fileSize, String fileMd5) {
        //根据Md5的唯一性查看是否有该对应文件，因此调用FileMd5服务的接口查询md5。而后续还需要这个实体类里其他消息所以用getOne方法
        FileMd5 fileMd5Entity = iFileMd5Service.getOne(new QueryWrapper<FileMd5>().eq("md5" , fileMd5));

        //表示已经存在，需秒传，则拿出他的秒传字段StorageObjectId然后返回一个json对象
        if(fileMd5Entity != null){
            //生成一个JSON对象
            JSONObject jsonObject = new JSONObject();
            //json对象里存入Storage里存在的id
            jsonObject.set("StorageObjectId",fileMd5Entity.getStorageObjectId());
            return jsonObject.toString();
        }

        //文件不存在，根据S3的getUpload方法返回上传地址
        //用StringUtils.isNotBlank方法，如果不为空则通过照片的名字用来生成一个suffixName
        String suffixName = "";
        if(StringUtils.isNotBlank(fileName)){
            suffixName = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        }

        return S3Util.getPutUploadUrl(suffixName,fileMd5);
    }

    /**
     * 非秒传处理
     * @param bo
     * @return
     */
    @Override
    public CommonEnum commit(FileUploadBo bo) {
        //用S3工具根据存储池中文件Id即objectId来看要查询的文件是否上传了。因为没上传过所以不会有StorageObjectId也不会有objectId
        S3ObjectSummary s3ObjectSummary = S3Util.getObjectInfo(bo.getObjectId());
        //1.报错
        //1.1已经生成上传地址但还未进行文件未上传
        if(s3ObjectSummary == null){
            return CommonEnum.FILE_NOT_UPLOADED;
        }
        //1.2上传文件大小和当前上传文件大小对应不上、Md5的字符串形式和eTag(桶里对应照片的标识符)对不上
        if(!bo.getFileSize().equals(s3ObjectSummary.getSize())
                ||!StringUtils.equalsIgnoreCase(s3ObjectSummary.getETag(),bo.getFileMd5())){
            return CommonEnum.FILE_UPLOADED_ERROR;
        }
        //2.文件上传成功 -文件存储信息入库（StorageObject表）
        StorageObject storageObject = new StorageObject("minio",bo.getContainerId(),bo.getObjectId(),
                bo.getFileMd5(),bo.getFileSize());
        iStorageObjectService.save(storageObject);

        //3.将MD5入库(文件MD5表)
        FileMd5 fileMd5 = new FileMd5(bo.getFileMd5(),bo.getFileSize(),bo.getStorageObjectId());
        iFileMd5Service.save(fileMd5);

        //- 用户文件列表 - 分别发送到审核、图片kafka列表
        bo.setStorageObjectId(storageObject.getStorageObjectId());
        iUserFileService.saveAndFileDeal(bo);



        return CommonEnum.SUCCESS;
    }

    /**
     * 秒传处理
     * @param bo
     * @return
     */
    @Override
    public CommonEnum commitTransSecond(FileUploadBo bo) {
        //1通过StorageObjectServiceImpl实现类查询秒传信息是否正确
        StorageObject storageObject =  iStorageObjectService.getById(bo.getStorageObjectId());
        //1.1为空则表明已传过的文件和当前文件是不同的，表示找不到该文件
        if(storageObject == null){
            return CommonEnum.FILE_UPLOADED_ERROR;
        }
        //1.2同样检查当前文件大小和已经上传过文件大小，如果对不上则表明文件是不匹配的
        if(!storageObject.getObjectSize().equals(bo.getFileSize())
            || !StringUtils.equalsIgnoreCase(bo.getFileMd5(),storageObject.getMd5())){
            return CommonEnum.FILE_UPLOADED_ERROR;
        }

        //2.将文件入库（保存文件） - 用户文件列表 - 分别发送到审核、图片kafka列表
        bo.setStorageObjectId(storageObject.getStorageObjectId());
        iUserFileService.saveAndFileDeal(bo);
        return CommonEnum.SUCCESS;
    }



}
