package com.yupi.yupicturebackend.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 创建阔图任务相应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutPaintingTaskResponse {
private Output output;

//表示任务的输出信息
@Data
public static class Output {
    //任务ID
    private String taskId;
    //任务状态
//<ul>
//<li>PENDING:排队中</1i>
//<li>RUNNING:处理中</1i>
//<li>SUSPENDED:挂起</1i>
//<1i>SUCCEEDED:执行成功</1i>
//<li>FAILED:执行失败</1i>
//<1i>UNKNOWN:任务不存在或状态未知</1i>
//</ul>
    private String taskStatus;
}

    //接口错误码。
           // <p>接口成功请求不会返回该参数。</p>
    private String code;
    //*接口错误信息。
            //<p>接口成功请求不会返回该参数。</p>
    private String message;
    /**
     *请求唯一标识。
     *<p>可用于请求明细溯源和问题排查。〈/p>
     */
    private String requestId;


}