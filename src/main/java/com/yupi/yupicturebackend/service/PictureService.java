package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.dto.picture.PictureQueryRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureUploadRequest;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ertstyuqk
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-03-05 21:09:00
 */
public interface PictureService extends IService<Picture> {

 void validPicture(Picture picture);

 /**
 *上传图片
 *@param multipartFile
 *@param pictureUploadRequest
 *@param loginUser
 @return
 */
 PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

 Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

 //获取查询对象
 QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

 PictureVO getPicturevo(Picture picture, HttpServletRequest request);
}
