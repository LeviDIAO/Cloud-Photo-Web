package com.cloud.photo.trans.controller;

import com.cloud.photo.common.utils.RequestUtil;
import com.cloud.photo.trans.entity.StorageObject;
import com.cloud.photo.trans.service.IStorageObjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 资源池文件存储信息 前端控制器
 * </p>
 *
 * @author kkoneone11
 * @since 2023-07-13
 */
@RestController
@Slf4j
@RequestMapping("/trans")
public class StorageObjectController {

    @Autowired
    IStorageObjectService iStorageObjectService;

    @RequestMapping("/getStorageObjectById")
    public StorageObject getStorageObjectById(HttpServletRequest request, HttpServletResponse response,
                                              @RequestParam(value = "storageObjectId") String storageObjectId){

        String requestId = RequestUtil.getRequestId(request);
        RequestUtil.printQequestInfo(request);
        StorageObject storageObject = iStorageObjectService.getById(storageObjectId);
        log.info("getStorageObjectById() storageObject = " + storageObject);
        if(storageObject == null){
            return null;
        }

        return storageObject;
    }

}
