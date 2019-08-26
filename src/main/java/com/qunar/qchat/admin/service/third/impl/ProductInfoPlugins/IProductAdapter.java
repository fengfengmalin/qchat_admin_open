package com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins;

import com.qunar.qchat.admin.vo.third.ProductVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IProductAdapter {
    public String  getBusiEnName();
    public ProductVO getProduct(int bType,String source, String pdtId, Map<String,String> subparams);
    public ProductVO getProduct(HttpServletRequest request,int bType,String source, String pdtId, Map<String,String> subparams);
}
