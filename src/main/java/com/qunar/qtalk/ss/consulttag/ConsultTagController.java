package com.qunar.qtalk.ss.consulttag;

import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qtalk.ss.consulttag.enums.ConsultTagType;
import com.qunar.qtalk.ss.consulttag.service.ConsultTagService;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.service.ShopService;
import com.qunar.qchat.admin.vo.conf.JsonData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/consultTag")
public class ConsultTagController {

//    private static final Logger logger = LoggerFactory.getLogger(ConsultTagController.class);

    @Autowired
    ConsultTagService consultTagService;

    @Autowired
    ShopService shopService;

    @ResponseBody
    @RequestMapping(value = "/tagList.qunar")
    public JsonData queryTagList(String supplierId, @RequestParam(value = "pid", required = false) String pid,
                                 @RequestParam(value = "busiSupplierId", required = false) String busiSupplierId,
                                 @RequestParam(value = "line", required = false) String line) {
        BusinessEnum anEnum = BusinessEnum.ofByEnName(line);

        if (supplierId.contains("@")) {
            supplierId = supplierId.split("@")[0];
        }
        if (supplierId.startsWith("shop_")) {
            supplierId = supplierId.replace("shop_", "");
        }
        if (!StringUtils.isNumeric(supplierId)) {
            return JsonData.error("参数错误");
        }
        long shopId = Long.parseLong(supplierId);
        if (anEnum != null && StringUtils.isNotEmpty(busiSupplierId)) {
            Shop shop = shopService.selectShopByBsiIdAndBusiSupplierId(anEnum.getId(), busiSupplierId);
            if (shop == null || shop.getId() != shopId)
                return JsonData.error("参数错误");
        }

        return consultTagService.findTagBySupplierId(shopId);
    }

    @ResponseBody
    @RequestMapping(value = "/insertTag.qunar")
    public JsonData insertTag(long supplierId, String busiSupplierId, String pid,
                              int busiId, String title, String content, int type) {
        BusinessEnum anEnum = BusinessEnum.of(busiId);
        ConsultTagType tagType = ConsultTagType.of(type);
        if(anEnum == null || tagType == null) {
            return JsonData.error("参数错误");
        }

        return consultTagService.insertTag(supplierId, busiSupplierId, pid, busiId, title, content, type);
    }
}
