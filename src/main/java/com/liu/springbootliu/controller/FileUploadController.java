package com.liu.springbootliu.controller;

import com.liu.springbootliu.bean.UserInfo;
import com.liu.springbootliu.bean.UserInfoRepository;
import com.liu.springbootliu.utils.ResultMsg;
import com.liu.springbootliu.utils.ResultStatusCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 上传
 */

@RestController
public class FileUploadController  {
    @Autowired
    private UserInfoRepository userRepositoy;

    @Value("${cbs.imagesPath}")
    private String mImagesPath;

    @RequestMapping(value = "/portraitUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultMsg fileUpload(@RequestParam("file")MultipartFile file,@RequestParam("userId") String userId) {
        ResultMsg resultMsg = null;
        if (!file.isEmpty()) {
            if (file.getContentType().contains("image")) {
                try {
                    //temp="upload/"
                    String temp =  "upload" + File.separator;
                    // 获取图片的文件名
                    String fileName = file.getOriginalFilename();
                    // 获取图片的扩展名
                    String extensionName = StringUtils.substringAfter(fileName, ".");
                    // 新的图片文件名 = 获取时间戳+"."图片扩展名
                    //String newFileName = String.valueOf(System.currentTimeMillis()) + "." + extensionName;
                    String newFileName = fileName ;
                    // 数据库保存的目录 datdDirectory="upload/useridxxxxxx/"
                    String datdDirectory = temp.concat(userId).concat(File.separator);
                    // 文件路径 图片根路径/数据库目录保存的相对路径
                    String filePath = mImagesPath.substring(6).concat(datdDirectory);
                    File dest = new File(filePath, newFileName);
                    if (!dest.getParentFile().exists()) {
                        dest.getParentFile().mkdirs();
                    }

                    // 判断是否有旧头像，如果有就先删除旧头像，再上传
                        UserInfo userInfo = userRepositoy.findUserInfoByUid(userId);
                    if (StringUtils.isNotBlank(userInfo.getUserHead())) {
                        String oldFilePath = mImagesPath.substring(6).concat(userInfo.getUserHead());
                        File oldFile = new File(oldFilePath);
                        if (oldFile.exists()) {
                            oldFile.delete();
                        }
                    }
                    // 上传到指定目录
                    //file.transferTo(dest);
                    BufferedOutputStream out = new BufferedOutputStream(
                            new FileOutputStream(dest));//保存图片到目录下

                    out.write(file.getBytes());
                    out.flush();
                    out.close();
                    userRepositoy.upDateHead(datdDirectory.concat(newFileName),userId);//增加用户
                    //将图片流转换进行BASE64加码
                    //BASE64Encoder encoder = new BASE64Encoder();
                    //String data = encoder.encode(file.getBytes());

                    // 将反斜杠转换为正斜杠
                    String data = datdDirectory.replaceAll("\\\\", "/") + newFileName;
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("file", data);
                    //封装上传成功消息
                    resultMsg = new ResultMsg(ResultStatusCode.UPFILE_SCUESS.getErrcode(),
                            ResultStatusCode.UPFILE_SCUESS.getErrmsg(),
                            resultMap);
                } catch (IOException e) {
                    //封装上传失败消息
                    resultMsg= new ResultMsg(ResultStatusCode.UPFILE_ERR.getErrcode(),
                            ResultStatusCode.UPFILE_ERR.getErrmsg(),
                            null);
                }
            }else {
                //封装上传的文件不是图片类型消息
                resultMsg= new ResultMsg(ResultStatusCode.UPFILE_TYPEERR.getErrcode(),
                        ResultStatusCode.UPFILE_TYPEERR.getErrmsg(),null);
            }
            return resultMsg;
        }else {
            //封装上传失败，请选择要上传的图片消息
            resultMsg= new ResultMsg(ResultStatusCode.UPFILE_NOFILE.getErrcode(),
                    ResultStatusCode.UPFILE_NOFILE.getErrmsg(),null);
            return resultMsg;
        }
    }
}
