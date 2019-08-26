package com.qunar.qchat.admin.service.third;

import com.qunar.qchat.admin.vo.third.ProductVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 封装各个业务先产品相关的业务逻辑
 * Created by qyhw on 12/17/15.
 */
public interface IProductService {


    /**
     * 获取产品详情  区分pc和app
     */
    ProductVO getProduct(HttpServletRequest request, String pdtId, String tEnId, int bType, String tuId, String t3id, String source);
}
