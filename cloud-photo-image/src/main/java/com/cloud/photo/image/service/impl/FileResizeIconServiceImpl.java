package com.cloud.photo.image.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.packagescan.util.ResourceUtils;
import com.cloud.photo.common.bo.StorageObjectBo;
import com.cloud.photo.common.bo.UserFileBo;
import com.cloud.photo.common.common.ResultBody;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.photo.common.constant.CommonConstant;
import com.cloud.photo.image.entity.FileResizeIcon;
import com.cloud.photo.common.fegin.Cloud2TransService;
import com.cloud.photo.image.entity.MediaInfo;
import com.cloud.photo.image.mapper.FileResizeIconMapper;
import com.cloud.photo.image.service.IFileResizeIconService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.photo.image.service.IMediaInfoService;
import com.cloud.photo.image.utils.DownloadFileUtil;
import com.cloud.photo.image.utils.PicUtils;
import com.cloud.photo.image.utils.UploadFileUtil;
import com.cloud.photo.image.utils.VipsUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.UUID;

/**
 * <p>
 * 图片缩略图 服务实现类
 * </p>
 *
 * @author kkoneone11
 * @since 2023-07-15
 */
@Service
public class FileResizeIconServiceImpl extends ServiceImpl<FileResizeIconMapper, FileResizeIcon> implements IFileResizeIconService {

    @Autowired
    private IFileResizeIconService iFileResizeIconService;

    @Autowired
    private Cloud2TransService cloud2TransService;

    @Autowired
    private IMediaInfoService iMediaInfoService;


    /**
     * 判断原图的缩略图是否存在，如果已经存在则会存储在storageObjectId库中，iconCode大小是判断是200还是600
     * @param storageObjectId
     * @param iconCode
     * @return
     */
    public FileResizeIcon getFileResizeIcon(String storageObjectId , String iconCode){
        //1.设置查询Wrapper
        QueryWrapper<FileResizeIcon> qw = new QueryWrapper<>();
        //2.根据hashmap 组装一个查询
        HashMap<String , Object> hm = new HashMap<>();
        //3.要求字段对应字段值，所以用hashmap
        hm.put("storage_object_id" , storageObjectId);
        hm.put("icon_code" , iconCode);
        //4.全部查询，即多个查询语句
        qw.allEq(hm);
        //5.执行查询 , 没有的话直接返回null即可
        FileResizeIcon fileResizeIcon = iFileResizeIconService.getOne(qw,false);
        return fileResizeIcon;
    }


    /**
     * 在minio中获得原图，并下载到本地目录中，调用trans服务下的getDownloadUrl()接口
     * @param containerId
     * @param objectId
     * @return
     */
    public String downloadImage(String containerId , String objectId , String suffixName){
        //获取要下载到本地的下载地址，
        String srcFileDirName = "D:/cloudImage/";
        //调用服务获得下载地址
        ResultBody baseRespone = cloud2TransService.getDownloadUrl(containerId , objectId);
        //获取下载地址、根据源文件路径生成缩略图的名字使其有意义且uuid不冲突
        String url = baseRespone.getData().toString();
        String srcFileName = srcFileDirName + UUID.randomUUID().toString() +"." +suffixName;
        File dir = new File(srcFileDirName);
        //如果目标源目录不存在则生成一个
        if(!dir.exists()){
            dir.mkdir();
        }
        //把原图下载到文件夹里
        Boolean downloadResult = DownloadFileUtil.downloadFile(url , srcFileName);
        if(!downloadResult) return null;
        return srcFileName;
    }

    /**
     * 根据原图生成缩略图  封装成一个方法，方便后面同时生成200尺寸和600尺寸的缩略图
     * @param iconCode
     * @param suffixName
     * @param srcFileName
     * @param storageObjectId
     * @param fileName
     * @return
     */

    public FileResizeIcon imageThumbnailSave(String iconCode , String suffixName , String srcFileName,
                                             String storageObjectId , String fileName){

        //获取照片在本地的地址，
        String srcFileDirName = "D:/cloudImage/";

        //通过vips工具生成缩略图
        //1.根据源文件路径生成缩略图的名字使其有意义且uuid不冲突
        String iconFileName = srcFileDirName + UUID.randomUUID().toString() +"." +suffixName;
        //2.生成缩略图大小
        int width = Integer.parseInt(iconCode.split("_")[0]);
        int height = Integer.parseInt(iconCode.split("_")[1]);
        //根据源文件名生成缩略图
        VipsUtil.thumbnail(srcFileName , iconFileName , width , height , "70");

        //截图失败或者文件为空
        if(StringUtils.isBlank(iconFileName) || !new File(iconFileName).exists()){
            return null;
        }

        //调用uploadIcon()缩略图上传到存储池并且入库file_resize_icon表
        FileResizeIcon fileResizeIcon = iFileResizeIconService.uploadIcon(null, storageObjectId, "200_200", new File(iconFileName), fileName);
        return fileResizeIcon;
    }

    /**
     * 上传缩略图 通过fegin调用trans服务的getPutUploadUrl接口。
     * @param userId
     * @param storageObjectId
     * @param iconCode
     * @param iconFile
     * @param fileName
     * @return FileResizeIcon
     */
    public FileResizeIcon uploadIcon(String userId, String storageObjectId, String iconCode, File iconFile, String fileName){
        //调用trans服务的getPutUploadUrl接口。基本不会有重复的缩略图名字所以不会有重复的md5因此不需要传入md5
        ResultBody uploadrUrlResponse = cloud2TransService.getPutUploadUrl(userId,fileName,null,null);
        //将ResultBody里的数据转化为JSON格式并分别拿出属性
        JSONObject jsonObject = JSONObject.parseObject(uploadrUrlResponse.getData().toString());
        String objectId = jsonObject.getString("objectId");
        String uploadUrl = jsonObject.getString("url");
        String containerId = jsonObject.getString("containerId");

        //上传文件到存储池
        UploadFileUtil.uploadSinglePart(iconFile , uploadUrl);

        //保存信息入库到file_resize_icon表
        FileResizeIcon newfileResizeIcon = new FileResizeIcon(storageObjectId, iconCode, iconFile,containerId, objectId);
        iFileResizeIconService.save(newfileResizeIcon);

        return newfileResizeIcon;
    }

    /**
     * 图片处理  1、生成 200_200、600_600尺寸缩略图  2、分析图片格式 宽高等信息
     * @param storageObjectId
     * @param fileName
     */
    @Override
    public void imageThumbnailAndMediaInfo(String storageObjectId, String fileName) {
        String iconCode200 = "200_200";
        String iconCode600 = "600_600";

        //查询尺寸200和尺寸600缩略图 是否存在  - 同一张缩略图无需重复生成
        FileResizeIcon fileResizeIcon200 = getFileResizeIcon(storageObjectId,iconCode200);
        FileResizeIcon fileResizeIcon600 = getFileResizeIcon(storageObjectId,iconCode600);

        //查询图片是否分析属性
        MediaInfo mediaInfo = iMediaInfoService.getOne(new QueryWrapper<MediaInfo>().eq("storage_Object_Id", storageObjectId) ,false);

        //缩略图已存在&图片已分析
        if(fileResizeIcon200!=null && fileResizeIcon600 !=null && mediaInfo!=null){
            return ;
        }

        //缩略图不存在-下载原图
        String suffixName = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
        StorageObjectBo storageObject = cloud2TransService.getStorageObjectById(storageObjectId);
        String srcFileName = downloadImage(storageObject.getContainerId(), storageObject.getObjectId(), suffixName);

        //原图下载失败
        if(StringUtils.isBlank(srcFileName)){
            log.error("downloadResult error!");
            return;
        }

        FileResizeIcon fileResizeIcon2 = null;
        FileResizeIcon fileResizeIcon6 = null;
        //生成缩略图 保存入库
        if(fileResizeIcon200 == null){
            //生成缩略图并入库
            fileResizeIcon2 = iFileResizeIconService.imageThumbnailSave(iconCode200, suffixName, srcFileName, storageObjectId, fileName);
        }

       if(fileResizeIcon600 == null){
           //生成缩略图并入库
           fileResizeIcon6 = iFileResizeIconService.imageThumbnailSave(iconCode600, suffixName, srcFileName, storageObjectId, fileName);
       }

        //使用PicUtils工具将图片部分格式属性分析&入库
        MediaInfo newMediaInfo = PicUtils.analyzePicture(new File(srcFileName));
        //额外设置还没有存入的属性
        newMediaInfo.setStorageObjectId(storageObjectId);
        if(StringUtils.isBlank(newMediaInfo.getShootingTime())){
            newMediaInfo.setShootingTime(DateUtil.now());
        }
        //存入MediaInfoSercie数据库
        iMediaInfoService.save(newMediaInfo);

//        JSONObject jsonObject200 = JSONObject.parseObject(fileResizeIcon2.toString());
//        String width200 = jsonObject200.getString("width");
//        String height200 = jsonObject200.getString("height");
//
//
//
//        JSONObject jsonObject600 = JSONObject.parseObject(fileResizeIcon6.toString());
//        String width600 = jsonObject600.getString("width");
//        String height600 = jsonObject600.getString("height");


    }

    /**
     * 因为这个接口是每个用户自己的缩略图，所以需要一个userId，且不知道对应的file文件有没有存入、有没有缩略图、有多少文件，因此采用的是fileId。因为有存入的话则可以采用storageObjectId
     * @param userId
     * @param fileId
     * @param iconCode
     * @return
     */
    @Override
    public String getIconUrl(String userId,String fileId,String iconCode) {
        //1.根据fileId查询文件相关信息

        //调用trans项目的getUserFileById()查询文件信息
        UserFileBo userFile = cloud2TransService.getUserFileById(fileId);
        String storageObjectId = userFile.getStorageObjectId();
        String fileName = userFile.getFileName();
        String suffixName = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());

        //2.查看通过审核的缩略图
        //审核失败  返回审核未通过的缩略图
        if(userFile.getAuditStatus().equals(CommonConstant.FILE_AUDIT_FAIL)){
            return getAuditFailIconUrl();
        }

        //存储文件存储信息 调用trans项目的getStorageObjectById()将所有存储文件的信息查询出来
        StorageObjectBo storageObject = cloud2TransService.getStorageObjectById(userFile.getStorageObjectId());

        //3.查看已经存在的缩略图，没有则生成然后返回
        //查询缩略图信息
        FileResizeIcon fileResizeIcon = getFileResizeIcon(userFile.getStorageObjectId(),iconCode);

        String objectId;
        String containerId;
        //缩略图不存在则生成缩略图
        if(fileResizeIcon == null){


            //调用downloadImage()生成缩略图
            System.out.println("serviceImpl-getIconUrl() = ContainerId()"+storageObject.getContainerId());
            String srcFilename = iFileResizeIconService.downloadImage(storageObject.getContainerId(),storageObject.getObjectId(), suffixName);
            //看缩略图是否生成成功
            if(StringUtils.isBlank(srcFilename)){
                log.error("缩略图生成失败");
                return null;
            }
            //保存一下生成的缩略图
            FileResizeIcon newFileResizeIcon = iFileResizeIconService.imageThumbnailSave(iconCode,suffixName,srcFilename,storageObjectId,fileName);

            //文件为空或者截图出错
            if(newFileResizeIcon == null){
                log.error("文件为空或者截图出错！");
                return null;
            }
            //提取出拿出objectId和containerId用来生成缩略图地址
            objectId = newFileResizeIcon.getObjectId();
            containerId = newFileResizeIcon.getContainerId();

        }else{
            //缩略图存在，则拿出objectId和containerId用来生成缩略图地址
            objectId = fileResizeIcon.getObjectId();
            containerId = fileResizeIcon.getContainerId();
        }

        //生成缩略图下载地址，调用trans的需要的参数getDownloadUrl()方法，因此需要对应的桶和对应的是哪一个文件
        ResultBody iconUrlResponse = cloud2TransService.getDownloadUrl(containerId,objectId);
        return iconUrlResponse.getData().toString();
    }

    /**
     * 获取审核失败的缩略图地址
     * @return
     */
    public String getAuditFailIconUrl() {

        //查询默认图是否存在存储池  不存在 上传到存储池
        String iconStorageObjectId = CommonConstant.ICON_STORAGE_OBJECT_ID;
        StorageObjectBo iconStorageObject = cloud2TransService.getStorageObjectById(iconStorageObjectId);
        String containerId = "";
        String objectId = "";
        String srcFileName = "";
        if(iconStorageObject == null){
            File file = null;
            try {
                file = ResourceUtils.getFile("classpath:static/auditFail.jpg");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            FileResizeIcon newFileResizeIcon =  iFileResizeIconService.uploadIcon(null,iconStorageObjectId ,"200_200", file,"auditFail.jpg");
            containerId = newFileResizeIcon.getContainerId();
            objectId = newFileResizeIcon.getObjectId();
        }else{
            containerId = iconStorageObject.getContainerId();
            objectId = iconStorageObject.getObjectId();
        }
        //生成缩略图下载地址
        ResultBody iconUrlResponse = cloud2TransService.getDownloadUrl(containerId,objectId);
        return iconUrlResponse.getData().toString();
    }

}
