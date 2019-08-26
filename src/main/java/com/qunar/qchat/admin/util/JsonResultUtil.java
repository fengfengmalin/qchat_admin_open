package com.qunar.qchat.admin.util;

import com.qunar.qchat.admin.vo.JsonResultVO;

/**
 * Author : mingxing.shao
 * Date : 15-10-20
 *
 */
public class JsonResultUtil {
    /**
     * 构建一个成功动作的json字符串
     *
     * @param data 需要放入data中的对象
     * @param <T>  放入data中的对象的类型
     * @return JsonResultVO对象
     */
    public static <T> JsonResultVO<T> buildSucceedJsonResult(T data) {
        return buildSucceedJsonResult(null, data);
    }

    /**
     * 构建一个成功动作的json字符串
     *
     * @param data 需要放入data中的对象
     * @param <T>  放入data中的对象的类型
     * @param total 总记录数，分页使用
     * @return
     */
    public static <T> JsonResultVO<T> buildSucceedJsonResultWithTotal(T data, Integer total) {
        JsonResultVO result = buildSucceedJsonResult("success", data);
        result.setTotal(total);
        return result;
    }


    /**
     * 构建一个成功动作的json字符串
     *
     * @param msg  成功的提示信息
     * @param data 需要放入data中的对象
     * @param <T>  放入data中的对象的类型
     * @return JsonResultVO对象
     */
    public static <T> JsonResultVO<T> buildSucceedJsonResult(String msg, T data) {
        JsonResultVO<T> jsonResultVO = new JsonResultVO<>();
        jsonResultVO.setRet(JsonResultVO.ResultStatus.SUCCESS);
        jsonResultVO.setMsg(msg);
        jsonResultVO.setData(data);
        return jsonResultVO;
    }

    /**
     * 构建一个成功动作的json字符串
     * @param code 响应码
     * @param msg 成功的提示信息
     * @param data 需要放入data中的对象
     * @param <T> 放入data中的对象的类型
     * @return JsonResultVO对象
     */
    public static <T> JsonResultVO<T> buildSucceedJsonResult(String code, String msg, T data) {
        JsonResultVO<T> jsonResultVO = new JsonResultVO<>();
        jsonResultVO.setRet(JsonResultVO.ResultStatus.SUCCESS);
        jsonResultVO.setCode(code);
        jsonResultVO.setMsg(msg);
        jsonResultVO.setData(data);
        return jsonResultVO;
    }

    public static <T> JsonResultVO<T> buildJsonResult(boolean ret,String code, String msg, T data) {
        JsonResultVO<T> jsonResultVO = new JsonResultVO<>();
        jsonResultVO.setRet(ret ? JsonResultVO.ResultStatus.SUCCESS : JsonResultVO.ResultStatus.FAILED);
        jsonResultVO.setCode(code);
        jsonResultVO.setMsg(msg);
        jsonResultVO.setData(data);
        return jsonResultVO;
    }

    /**
     * 构建一个失败动作的json字符串
     *
     * @param errorMsg 放入json字符串的错误信息
     * @return JsonResultVO对象
     */
    public static JsonResultVO<?> buildFailedJsonResult(String errorMsg) {
        JsonResultVO jsonResultVO = new JsonResultVO();
        jsonResultVO.setRet(JsonResultVO.ResultStatus.FAILED);
        jsonResultVO.setMsg(errorMsg);
        return jsonResultVO;
    }

    /**
     * 构建一个失败动作的json字符串
     *
     * @param errorMsg 放入json字符串的错误信息
     * @return JsonResultVO对象
     */
    public static JsonResultVO<?> buildFailedJsonResult(String code,String errorMsg) {
        JsonResultVO jsonResultVO = new JsonResultVO();
        jsonResultVO.setRet(JsonResultVO.ResultStatus.FAILED);
        jsonResultVO.setMsg(errorMsg);
        jsonResultVO.setCode(code);
        return jsonResultVO;
    }
}
