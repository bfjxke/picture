package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.dto.picture.*;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ertstyuqk
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-03-05 21:09:00
 */
public interface PictureService extends IService<Picture> {

 //校验图片
 void validPicture(Picture picture);

 /**
 *上传图片
  *@param inputSource 文件输入源
 *@param pictureUploadRequest
 *@param loginUser
 @return
 */
 PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

 /**
  * 获取图片包装类（分页）
  *
  * @param picturePage
  * @param request
  * @return
  */
 Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);


 //获取查询对象
 QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

 /**
  * 获取图片包装类（单条）
  *
  * @param picture
  * @param request
  * @return
  */
 PictureVO getPictureVO(Picture picture, HttpServletRequest request);

 /**
  * 图片审核
  */
 void doPictureReview(PictureReviewRequest pictureReviewRequest, User user);

 void fillReviewParams(Picture picture, User loginUser);


 /**
  * 批量抓取和创建图片
  *
  * @param pictureUploadByBatchRequest
  * @param loginUser
  * @return 成功创建的图片数
  */
 Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);


 //清理图片
 void clearPictureFile(Picture oldPicture);

 void deletePicture(long pictureId, User loginUser);

 void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

 /**
  *校验空间图片权限(用户能不能看到这张图片)
  */
 void checkPictureAuth(User loginUser,Picture picture);


 List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

}
