package com.cloud.photo.trans.controller;

import com.cloud.photo.common.bo.AlbumPageBo;
import com.cloud.photo.common.common.ResultBody;
import com.cloud.photo.common.utils.RequestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.photo.trans.entity.UserFile;
import com.cloud.photo.trans.service.IUserFileService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kkoneone11
 * @since 2023-07-13
 */
@RestController
@RequestMapping("/trans")
public class UserFileController {

    @Autowired
    private IUserFileService iUserFileService;



    /**
     * 文件列表查询
     * @param request
     * @param response
     * @param pageBo
     * @return
     */
    @RequestMapping("/userFilelist")
    public ResultBody userFilelist(HttpServletRequest request, HttpServletResponse response,
                                   @RequestBody AlbumPageBo pageBo){
        //打印请求信息
        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request,pageBo);

        //1.设置查询Warpper
        QueryWrapper<UserFile> qw = new QueryWrapper<>();
        //2.设置查询条件
        HashMap<String,Object> param = new HashMap<>();
        if(pageBo.getCategory() != null){
            param.put("category",pageBo.getCategory());
        }
        param.put("user_id",pageBo.getUserId());
        qw.allEq(param);
        Integer current = pageBo.getCurrent();
        Integer pageSize = pageBo.getPageSize();
        if(current == null){
            current = 1;
        }
        if(pageSize == null){
            pageSize = 20;
        }
        IPage<UserFile> page = new Page<>(current,pageSize);
        IPage<UserFile> userFileIPage = iUserFileService.page(page,qw.orderByDesc("user_id","create_time"));

        return ResultBody.success(userFileIPage);
    }

    /**
     * 更新文件审核状态
     * @param request
     * @param response
     * @param userFileList
     * @return
     */
    @RequestMapping("/updateUserFile")
    public Boolean updateUserFile(HttpServletRequest request , HttpServletResponse response,
                                  @RequestBody List<UserFile> userFileList){
        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request);
        //for循环每个对象处理一遍对应的status
        for(UserFile userFile : userFileList){
            //用更新Wrapper去更新
            UpdateWrapper<UserFile> updateWrapper = new UpdateWrapper<>();
            //1.添加StorageObjectId条件更新审核
            if(StringUtils.isNotBlank(userFile.getStorageObjectId())){
                updateWrapper.eq("storage_object_id" , userFile.getStorageObjectId());
            };
            //2.添加userfileid条件更新审核
            if(StringUtils.isNotBlank(userFile.getUserFileId())){
                updateWrapper.eq("user_file_id", userFile.getUserFileId());
            }
            //执行更新更新Wrapper
            updateWrapper.set("audit_status",userFile.getAuditStatus());
            iUserFileService.update(updateWrapper);
        }
        return true;
    }


    /**
     * 根据fileid获取文件信息
     * @param request
     * @param response
     * @param fileId
     * @return
     */
    @RequestMapping("/getUserFileById")
    public UserFile getUserFileById(HttpServletRequest request , HttpServletResponse response,
                                 @RequestParam(value = "fileId") String fileId){
        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request);

        return iUserFileService.getById(fileId);
    }
}
