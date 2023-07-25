package com.cloud.photo.image.controller;

import com.cloud.photo.common.bo.FileResizeIconBo;
import com.cloud.photo.common.common.ResultBody;
import com.cloud.photo.common.utils.RequestUtil;
import com.cloud.photo.image.service.IFileResizeIconService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.requests.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 图片缩略图 前端控制器
 * </p>
 *
 * @author kkoneone11
 * @since 2023-07-15
 */
@RestController
@Slf4j
@RequestMapping("/image")
public class FileResizeIconController {

    @Autowired
    IFileResizeIconService iFileResizeIconService;

    /**
     * 获取缩略图的地址
     * @param request
     * @param response
     * @param fileResizeIconBo
     * @return
     */
    @RequestMapping("/getIconUrl")
    public ResultBody getIconUrl(HttpServletRequest request , HttpServletResponse response,
                                 @RequestBody FileResizeIconBo fileResizeIconBo){
        //userId, fileId iconCode
        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request , fileResizeIconBo);

        String url = iFileResizeIconService.getIconUrl(fileResizeIconBo.getUserId(),
                                                       fileResizeIconBo.getFileId(),
                                                        fileResizeIconBo.getIconCode());

        log.info("getIconUrl() url = " + url);
        return ResultBody.success(url);
    }

}
