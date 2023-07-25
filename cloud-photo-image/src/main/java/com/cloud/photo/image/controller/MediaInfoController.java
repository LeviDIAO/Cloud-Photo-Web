package com.cloud.photo.image.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.photo.common.bo.FileAnalyzeBo;
import com.cloud.photo.common.bo.UserFileBo;
import com.cloud.photo.common.common.ResultBody;
import com.cloud.photo.common.fegin.Cloud2TransService;
import com.cloud.photo.common.utils.RequestUtil;
import com.cloud.photo.image.entity.MediaInfo;
import com.cloud.photo.image.service.IMediaInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 文件媒体信息 前端控制器
 * </p>
 *
 * @author kkoneone11
 * @since 2023-07-15
 */
@RestController
@Slf4j
@RequestMapping("/image")
public class MediaInfoController {

    @Autowired
    private Cloud2TransService cloud2TransService;
    @Autowired
    private IMediaInfoService iMediaInfoService;

    /**
     * 获取图片信息
     * @param request
     * @param response
     * @param fileAnalyzeBo
     * @return
     */
    @RequestMapping("/getMediaInfo")
    public ResultBody getMeddiaInfo(HttpServletRequest request , HttpServletResponse response,
                                    @RequestBody FileAnalyzeBo fileAnalyzeBo){
        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request,fileAnalyzeBo);
        UserFileBo userFileBo = cloud2TransService.getUserFileById(fileAnalyzeBo.getFileId());
        MediaInfo mediaInfo = iMediaInfoService.getOne(new QueryWrapper<MediaInfo>().eq("Storage_Object_Id", userFileBo.getStorageObjectId()),false);
        log.info("getMediaInfo() mediaInfo = "+ mediaInfo );
        return ResultBody.success(mediaInfo);
    }
}
