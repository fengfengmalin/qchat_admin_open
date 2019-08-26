package com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.constants.ConfigConstants;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.vo.third.ProductVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public abstract class ProductBaseAdapter  implements IProductAdapter{
    public static String defaultBusiEnName = "default";

    protected void checkParam(ProductVO pVO) {
        if (pVO.getTag() == null) {
            pVO.setTag("");
        }
        if (pVO.getImageUrl() == null) {
            pVO.setImageUrl("");
        }
        if (pVO.getTitle() == null) {
            pVO.setTitle("");
        }
    }

    protected String getUrl(int bType,String source){
        String url = Config.getProperty(
                ConfigConstants.PRODUCT_DETAIL_URL
                        + "." + BusinessEnum.of(bType).getEnName()
                        + (Strings.isNullOrEmpty(source) ? "" : ("." + source)));

        return url;

    }
    protected String getHost(int bType,String source){
        String url = Config.getProperty(
                ConfigConstants.PRODUCT_DETAIL_URL_HOST
                        + "." + BusinessEnum.of(bType).getEnName()
                        + (Strings.isNullOrEmpty(source) ? "" : ("." + source)));

        return url;
    }

    @Override
    public ProductVO getProduct(HttpServletRequest request, int bType, String source, String pdtId, Map<String, String> subparams) {
        return  getProduct(bType, source, pdtId, subparams);
    }
}
