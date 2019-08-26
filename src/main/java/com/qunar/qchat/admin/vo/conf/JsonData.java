package com.qunar.qchat.admin.vo.conf;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @since 1.0.0
 * @author kris.zhang
 */
@ToString
public class JsonData {

    /** 状态返回值 */
    private final boolean ret;

    /** 前端弹出消息 */
    private String msg;

    /** 后端具体传递给前端的消息 */
    private Object data;

    /** 错误码 */
    private Integer errcode;

    /** 为了保证符合规范，我们闭合构造权限 */
    private JsonData(boolean ret) {
        this.ret = ret;
    }

    /**
     * eg.
     *
     * <pre>
     *      {
     *          "ret" : false,
     *          "msg" : "错误的id，修改失败",
     *          "errcode" : 1
     *      }
     * </pre>
     *
     * @param message
     * @param errcode
     * @return JsonData
     */
    public static JsonData error(String message, Integer errcode) {
        JsonData result =  new JsonData(false);
        result.msg = message ;
        result.errcode = errcode;
        return result;
    }

    /**
     * eg.
     *
     * <pre>
     *      {
     *          "ret" : false,
     *          "msg" : "错误的id，修改失败"
     *      }
     * </pre>
     *
     * @param message
     * @return JsonData
     */
    public static JsonData error(String message) {
        return error(message,null);
    }

    /**
     * eg.
     *
     * <pre>
     *      {
     *          "ret" : false
     *      }
     * </pre>
     * @return JsonData
     */
    public static JsonData error() {
        return error(null,null);
    }

    /**
     * eg.
     *
     * <pre>
     *      {
     *          "ret" : false
     *          "errcode" : 1
     *      }
     * </pre>
     *
     * @param errcode 错误码
     * @return JsonData
     */
    public static JsonData error(Integer errcode) {
        return error(null,errcode);
    }

    /**
     * eg.
     *
     * <pre>
     *      {
     *          "ret" : true,
     *          "data": {
     *                      "encodedRID": "2916181129",
     *                      "operator": "gambol2",
     *                      "createTime": 1411363837776,
     *                      "rescueStatus": 1,
     *                      "pFunction": "free",
     *                      "departure": "北京",
     *                      "arrive": "大连"
     *          },
     *          "msg";"修改成功"
     *      }
     * </pre>
     *
     * @param object 对象
     * @param msg
     * @return JsonData
     */
    public static JsonData success(Object object, String msg) {
        JsonData result =  new JsonData(true);
        result.data = object;
        result.msg = msg;
        return result;
    }

    /**
     * eg.
     *
     * <pre>
     *      {
     *          "ret" : true,
     *          "data": {
     *                      "encodedRID": "2916181129",
     *                      "operator": "gambol2",
     *                      "createTime": 1411363837776,
     *                      "rescueStatus": 1,
     *                      "pFunction": "free",
     *                      "departure": "北京",
     *                      "arrive": "大连"
     *          }
     *      }
     * </pre>
     *
     * @param object 对象
     * @return JsonData
     */
    public static JsonData success(Object object) {
        return success(object,null);
    }

    /**
     * eg.
     *
     * <pre>
     *      {
     *          "ret" : true,
     *          "msg";"修改成功"
     *      }
     * </pre>
     *
     * @param msg
     * @return JsonData
     */
    public static JsonData success(String msg) {
        return success(null,msg);
    }

    /**
     * eg.
     *
     * <pre>
     *      {
     *          "ret" : true
     *      }
     * </pre>
     *
     * @return JsonData
     */
    public static JsonData success() {
        return success(null,null);
    }

    /**
     * eg.
     * <pre>
     *
     *
     *  {
     *      "ret": true,
     *      "data": {
     *          "content": [
     *                          {
     *                              "operationTime": 1411389538087,
     *                              "username": "haifeng111222",
     *                              "encodedSID": "3279243797",
     *                              "operation": "关闭订单",
     *                              "totalQuotas": 30,
     *                              "operator": "kris.zhang"
     *                          },
     *                          {
     *                              "operationTime": 1411389517575,
     *                              "username": "haifeng111222",
     *                              "encodedSID": "3279243797",
     *                              "operation": "关闭订单",
     *                              "totalQuotas": 30,
     *                              "operator": "kris.zhang"
     *                          }
     *                      ],
     *          "totalRows": 4
     *      },
     *      "msg": "查询到结果"
     *  }
     * </pre>
     *
     * @param content
     * @param totalRows
     * @param msg
     * @return JsonData
     */
    public static <T> JsonData list(Collection<T> content, Integer totalRows, String msg) {
        Preconditions.checkNotNull(content);  // fast fail
        JsonData result =  new JsonData(true);
        Map<String,Object> map = Maps.newHashMap();
        map.put("content",content);
        if (totalRows == null) {
            map.put("totalRows",content.size());
        } else {
            map.put("totalRows",totalRows);
        }
        result.msg = msg;
        result.data = map;
        return result;
    }

    /**
     * eg.
     * <pre>
     *
     *
     *  {
     *      "ret": true,
     *      "data": {
     *          "content": [
     *                          {
     *                              "operationTime": 1411389538087,
     *                              "username": "haifeng111222",
     *                              "encodedSID": "3279243797",
     *                              "operation": "关闭订单",
     *                              "totalQuotas": 30,
     *                              "operator": "kris.zhang"
     *                          },
     *                          {
     *                              "operationTime": 1411389517575,
     *                              "username": "haifeng111222",
     *                              "encodedSID": "3279243797",
     *                              "operation": "关闭订单",
     *                              "totalQuotas": 30,
     *                              "operator": "kris.zhang"
     *                          }
     *                      ],
     *          "totalRows": 2
     *      },
     *      "msg": "查询到结果"
     *  }
     * </pre>
     *
     * @param content
     * @param msg
     * @return JsonData
     */
    public static <T> JsonData list(Collection<T> content, String msg) {
        return list(content,null,msg);
    }

    /**
     * eg.
     * <pre>
     *
     *
     *  {
     *      "ret": true,
     *      "data": {
     *          "content": [
     *                          {
     *                              "operationTime": 1411389538087,
     *                              "username": "haifeng111222",
     *                              "encodedSID": "3279243797",
     *                              "operation": "关闭订单",
     *                              "totalQuotas": 30,
     *                              "operator": "kris.zhang"
     *                          },
     *                          {
     *                              "operationTime": 1411389517575,
     *                              "username": "haifeng111222",
     *                              "encodedSID": "3279243797",
     *                              "operation": "关闭订单",
     *                              "totalQuotas": 30,
     *                              "operator": "kris.zhang"
     *                          }
     *                      ],
     *          "totalRows": 2
     *      }
     *  }
     * </pre>
     *
     * @param content
     * @return JsonData
     */
    public static <T> JsonData list(Collection<T> content) {
        return list(content,null,null);
    }

    /**
     * eg.
     * <pre>
     *
     *  {
     *      "ret": true,
     *      "data": {
     *          "content": [
     *                          {
     *                              "operationTime": 1411389538087,
     *                              "username": "haifeng111222",
     *                              "encodedSID": "3279243797",
     *                              "operation": "关闭订单",
     *                              "totalQuotas": 30,
     *                              "operator": "kris.zhang"
     *                          },
     *                          {
     *                              "operationTime": 1411389517575,
     *                              "username": "haifeng111222",
     *                              "encodedSID": "3279243797",
     *                              "operation": "关闭订单",
     *                              "totalQuotas": 30,
     *                              "operator": "kris.zhang"
     *                          }
     *                      ],
     *          "totalRows": 40
     *      }
     *  }
     * </pre>
     *
     * @param content
     * @return JsonData
     */
    public static <T> JsonData list(Collection<T> content, Integer totalRows) {
        return list(content, totalRows, null);
    }

    /**
     * eg.
     * <pre>
     *
     *  {
     *      "ret": true,
     *      "data": {
     *          "content": [],
     *          "totalRows": 0
     *      }
     *  }
     * </pre>
     *
     * @param <T>
     * @return JsonData
     */
    public static <T> JsonData emptyList() {
        return list(Collections.emptySet());
    }

    /**
     * 获得ret值
     *
     * @return boolean
     */
    public boolean getRet() {
        return ret;
    }

    /**
     * 获得message
     *
     * @return String
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 获得数据
     *
     * @return Object
     */
    public Object getData() {
        return data;
    }

    /**
     * 获得error code
     *
     * @return Integer
     */
    public Integer getErrcode() {
        return errcode;
    }

}
