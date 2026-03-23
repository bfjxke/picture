package com.yupi.yupicturebackend.api.imagesearch.model;


import lombok.Data;

/**
 * 图片搜索结果
 */
@Data
public class ImageSearchResult {
    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 图片来源
     */
    private String formUrl;

}
