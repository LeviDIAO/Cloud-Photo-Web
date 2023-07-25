package com.cloud.photo.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.cloud.photo.api.utils.MyUtils;
import com.cloud.photo.common.bo.FileUploadBo;
import com.cloud.photo.common.bo.UserBo;
import com.cloud.photo.common.common.CommonEnum;
import com.cloud.photo.common.common.ResultBody;
import com.cloud.photo.common.constant.CommonConstant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 传输管理相关接口
 * @author linzsh
 */
@RestController
//@RequestMapping("/api")
public class TransController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/getTransList")
    public ResultBody getTransList(){

        //拿到用户信息
        UserBo userBo = MyUtils.getUserInfo();
        if (userBo == null){
            return ResultBody.error(CommonEnum.USER_IS_NULL);
        }
        //用户ID
        String userId = userBo.getUserId();
        //匹配前缀
        String keyPatten = userId + ":" + "*";
        Set<String> keys = stringRedisTemplate.keys(keyPatten);
        if (keys != null && CollUtil.isNotEmpty(keys)){
            //遍历所有的key
            List<FileUploadBo> uploadBos = keys.stream().map(key -> {
                String value = stringRedisTemplate.opsForValue().get(key);
                return JSONUtil.toBean(value, FileUploadBo.class);
            }).collect(Collectors.toList());
            //根据上传时间降序
            uploadBos = uploadBos.stream().sorted(Comparator.comparing(FileUploadBo::getUploadTime, Comparator.reverseOrder())).collect(Collectors.toList());
            return ResultBody.success(uploadBos);
        }else {
            return ResultBody.error(CommonEnum.TRANS_IS_NULL);
        }
    }
}
