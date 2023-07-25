package com.cloud.photo.image.consumer;

import com.alibaba.fastjson.JSONObject;
import com.cloud.photo.image.service.IFileResizeIconService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @Author：kkoneone11
 * @name：ImageConsumer
 * @Date：2023/7/15 16:11
 */

@Component
public class ImageConsumer {
    @Autowired
    IFileResizeIconService iFileResizeIconService;

    //消费监听file_image_topic
    @KafkaListener(topics = {"file_image_topic"})
    public void onMessage(ConsumerRecord<String , Object> record){
        //打印出消费的是哪个topic、partition，打印消息内容
        System.out.println("消费"+record.topic()+"-"+record.partition()+"-"+record.value());
        //拿出消费内容，并转化出来
        Object value = record.value();
        JSONObject jsonObject = JSONObject.parseObject(value.toString());
        String userFileId = jsonObject.getString("userFileId");
        String storageObjectId = jsonObject.getString("storageObjectId");
        String fileName = jsonObject.getString("fileName");

        //最后调用imageThumbnailAndMediaInfo分别生成200尺寸和600尺寸的缩略图并把他们的信息和他们入库
        iFileResizeIconService.imageThumbnailAndMediaInfo(storageObjectId,fileName);

    }
}
