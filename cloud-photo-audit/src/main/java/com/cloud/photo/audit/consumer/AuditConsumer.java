package com.cloud.photo.audit.consumer;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.photo.audit.entity.FileAudit;
import com.cloud.photo.audit.service.IFileAuditService;
import com.cloud.photo.common.bo.UserFileBo;
import com.cloud.photo.common.constant.CommonConstant;
import com.cloud.photo.common.fegin.Cloud2TransService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuditConsumer {

    @Autowired
    IFileAuditService iFileAuditService;
    @Autowired
    private Cloud2TransService cloud2TransService;

    // 消费监听
    @KafkaListener(topics = {"file_audit_topic"})
    public void onMessage(ConsumerRecord<String, Object> record){
        // 消费的哪个topic、partition的消息,打印出消息内容
        System.out.println("消费："+record.topic()+"-"+record.partition()+"-"+record.value());
        //分别取出对应的值
        Object value = record.value();
        JSONObject jsonObject = JSONObject.parseObject(value.toString());
        String userFileId = jsonObject.getString("userFileId");
        String fileMd5 = jsonObject.getString("fileMd5");
        String fileName = jsonObject.getString("fileName");
        Integer fileSize = jsonObject.getInteger("fileSize");
        String storageObjectId = jsonObject.getString("storageObjectId");

        //根据文件MD5读取审核状态  相同文件未审核 加入到审核列表。一次只需要审核一次
        FileAudit fileAudit = iFileAuditService.getOne(new QueryWrapper<FileAudit>().eq("md5", fileMd5),false);
        if(fileAudit == null){
            //未审核  插入审核列表
            FileAudit newFileAudit = new FileAudit();
            newFileAudit.setAuditStatus(CommonConstant.FILE_AUDIT);
            newFileAudit.setFileName(fileName);
            newFileAudit.setMd5(fileMd5);
            newFileAudit.setFileSize(fileSize);
            newFileAudit.setCreateTime(LocalDateTime.now());
            newFileAudit.setUserFileId(userFileId);
            newFileAudit.setStorageObjectId(storageObjectId);
            iFileAuditService.save(newFileAudit);


        }else if(fileAudit.getAuditStatus().equals(CommonConstant.FILE_AUDIT_ACCESS)){
            //文件审核状态默认为已通过审核  无需修改文件状态

        }else if(fileAudit.getAuditStatus().equals(CommonConstant.FILE_AUDIT_FAIL)){
            //审核失败  - 更新文件审核状态  - 根据StorageObjectId更新user_file表
            //1.先构造一个bo类 根据userFileId即可把对应用户所有的图片都找到
            UserFileBo userFileBo = new UserFileBo();
            userFileBo.setUserFileId(userFileId);
            userFileBo.setAuditStatus(CommonConstant.FILE_AUDIT_FAIL);

            List<UserFileBo> userFileBoList = new ArrayList<>();
            userFileBoList.add(userFileBo);
            cloud2TransService.updateUserFile(userFileBoList);
        }
    }
}
