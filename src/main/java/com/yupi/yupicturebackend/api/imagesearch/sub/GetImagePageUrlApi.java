package com.yupi.yupicturebackend.api.imagesearch.sub;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取以图搜图页面地址(step 1)
 */
@Slf4j
public class GetImagePageUrlApi {
    public static String getImagePageUrl(String imageUrl){
        //1.准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn","pc");
        formData.put("from","pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳（毫秒级）
        long uptime = System.currentTimeMillis();
        //请求地址
        String url = "https://graph.baidu.com/upload?uptime="+ uptime;//这是百度以图搜图地址

        try {
            //2.发送请求
            HttpResponse httpResponse = HttpRequest.post(url).form(formData)
                    .timeout(5000).execute();
            if(httpResponse.getStatus()!= HttpStatus.HTTP_OK){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
            }
            //解析响应
            String body = httpResponse.body();
            Map<String,Object> result = JSONUtil.toBean(body, Map.class);//把body转成map，因为body里面数据很多，转成map可以随机访问直接得到想要的数据，弄一个接收类还麻烦

            //处理响应结果
            Map<String,Object> data =(Map<String,Object>) result.get("data");
            //对url进行解码
            String rawUrl = (String)data.get("url");
            String searchResultUrl= URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            if(StrUtil.isBlank(searchResultUrl)){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"未返回有效的结果地址");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.info("调用百度以图搜图接口失败",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"搜索失败");
        }
    }

        //测试
//    public static void main(String[] args) {
//        //测试以图搜图功能
//        String imageUrl = "https://www.codefather.cn/logo.png";
//        String result = getImagePageUrl(imageUrl);
//        System.out.println("搜索成功,结果 URL:" + result);
//    }


}
