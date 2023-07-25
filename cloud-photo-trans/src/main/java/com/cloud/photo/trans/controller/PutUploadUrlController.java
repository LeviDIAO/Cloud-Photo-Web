package com.cloud.photo.trans.controller;

import com.cloud.photo.common.bo.FileUploadBo;
import com.cloud.photo.common.common.CommonEnum;
import com.cloud.photo.common.common.ResultBody;
import com.cloud.photo.common.utils.RequestUtil;
import com.cloud.photo.trans.service.IPutUploadUrlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author：kkoneone11
 * @name：PutuploadController
 * @Date：2023/7/13 16:47
 */

/**
 * 获取上传地址的一个操作，是根据逻辑业务而得
 */
@RestController
@RequestMapping("/trans")
@Slf4j
public class PutUploadUrlController {
    @Autowired
    private IPutUploadUrlService iPutUploadUrlService;



    /**
     * 用户要上传一个文件，文件上传操作需的参数对应着文件的信息(md5、fileName等)、上传文件的用户信息、上传信息和修改信息等
     * @param request
     * @param response
     * @param userId
     * @param fileName
     * @param fileMd5
     * @param fileSize
     * @return
     */
    @RequestMapping("/getPutUploadUrl")
    public ResultBody getPutUploadUrl(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(value = "userId",required = false) String userId,
                                      @RequestParam(value = "fileName") String fileName,
                                      @RequestParam(value = "fileMd5",required = false) String fileMd5,
                                      @RequestParam(value = "fileSize",required = false) Long fileSize){

        //获得当前时间
        Long startTime = System.currentTimeMillis();
        //根据请求获取一个专属当前请求的id
        String requestId = RequestUtil.getRequestId(request);

        //根据fileName、fileMd5、fileSize查看当前文件是否有被上传过。是判断逻辑，调用service接口里的实现方法
        String result = iPutUploadUrlService.getPutUploadUrl(fileName,fileSize,fileMd5);

        //输出当前用户、结果、和这次操作的时间
        Long endTime = System.currentTimeMillis();
        log.info("userId = " + userId + ", resutlt " + result + ", cost" + (endTime-startTime));

        //返回上传地址或者文件和当前请求的id
        return ResultBody.success(result,requestId);
    }

    @PostMapping("/commit")
    public ResultBody commit(HttpServletRequest request, HttpServletResponse response,
                             @RequestBody FileUploadBo bo){

        //生成一次请求，并打印出来
        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request);

        //返回值
        CommonEnum result;

        //根据StoreObjectId这个字符串是否为空，即是否存储在store来判断是否需要秒传
        if(StringUtils.isBlank(bo.getStorageObjectId())){
            //非秒传，即要传入一个文件
            result = iPutUploadUrlService.commit(bo);
        }else{
            //是秒传，即返回数据库存储的信息

            result = iPutUploadUrlService.commitTransSecond(bo);
        }

        //打印相关信息
        log.info("getPutUploadUrl() userId = " + bo.getUserId() + " , result = " + result);
        if (StringUtils.equals(result.getResultMsg(), CommonEnum.SUCCESS.getResultMsg())) {
            return ResultBody.success(CommonEnum.SUCCESS.getResultMsg(), requestId);
        } else {
            return ResultBody.error(result.getResultCode(), result.getResultMsg(), requestId);
        }
    }


}
