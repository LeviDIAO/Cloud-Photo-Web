package com.cloud.photo.trans.controller;

import com.cloud.photo.common.common.ResultBody;
import com.cloud.photo.common.utils.RequestUtil;
import com.cloud.photo.trans.service.IDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author：kkoneone11
 * @name：DownloadControlle
 * @Date：2023/7/14 15:45
 */
@RestController
@Slf4j
@RequestMapping("/trans")
public class DownloadController {
    @Autowired
    private IDownloadService iDownloadService;

    /**
     * 根据fileid获得文件信息
     * @param request
     * @param response
     * @param userId
     * @param fileId
     * @return
     */
    @RequestMapping("/getDownloadUrlByFileid")
    public ResultBody getDownloadUrlByFileid(HttpServletRequest request, HttpServletResponse response,
                                          @RequestParam(value = "userId") String userId,
                                          @RequestParam(value = "fileId") String fileId){

        //获取当前请求id
        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request);
        String url = iDownloadService.getDownloadUrlByFileid(userId,fileId);

        log.info("getDownloadUrlByFileid()" + userId + ", url = " + url);
        return ResultBody.success(url);
    }

    /**
     * 通过存储信息获得下载地址（缩略图专用）
     * @param request
     * @param response
     * @param containerId
     * @param objectId
     * @return
     */
    @GetMapping("/getDownloadUrl")
    public ResultBody getDownloadUrl(HttpServletRequest request , HttpServletResponse response,
                                     @RequestParam(value = "containerId") String containerId,
                                     @RequestParam(value = "objectId") String objectId){
        //获取当前请求id
        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request);
        String url = iDownloadService.getDownloadUrl(containerId, objectId);
        log.info("url = " + url);
        if(url == null){
            return ResultBody.error("uri is null");
        }

        return ResultBody.success(url);
    }
}
