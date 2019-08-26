package com.qunar.qchat.admin.controller;

import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.HotlineSupplierMapping;
import com.qunar.qtalk.ss.sift.model.DistributedInfo;
import com.qunar.qtalk.ss.sift.service.HotlineSupplierService;
import com.qunar.qtalk.ss.sift.service.SiftStrategyService;
import com.qunar.qtalk.ss.utils.JID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/test2")
public class HelenTestController {
    private static final Logger logger = LoggerFactory.getLogger(HelenTestController.class);

    @Resource
    private SiftStrategyService siftStrategyService;

    @RequestMapping(value = "/testsift.json", method = RequestMethod.GET)
    @ResponseBody
    public CSR testSift(
            @RequestParam(value = "productID", required = false, defaultValue = "") String productID,
            @RequestParam(value = "shopID", required = true) String shopID,
            @RequestParam(value = "csrQunarName", required = false, defaultValue = "") String csrQunarName,
            @RequestParam(value = "isTranAs", required = true, defaultValue = "") String isTranAs) {

        JID fromJid = null;
        if (StringUtils.isNotEmpty(csrQunarName)) {
            if (!csrQunarName.contains("@")) {
                csrQunarName = String.format("%s@%s", csrQunarName, "ejabhost2");
            }
            fromJid = JID.parseAsJID(csrQunarName);
        }

        DistributedInfo distributedInfo =
                siftStrategyService.siftCsr(productID, new Long(shopID), fromJid,"ejabhost2", new Boolean(isTranAs));

        CSR csr = distributedInfo == null ? null : distributedInfo.getCsr();
        return csr;
    }



    @Autowired
    HotlineSupplierService hotlineSupplierService;

    @RequestMapping(value = "/testHotline.json", method = RequestMethod.GET)
    @ResponseBody
    public JsonData testHotline(HotlineSupplierMapping hotlineSupplierMapping, String shopJid) {
        if (StringUtils.isNotEmpty(hotlineSupplierMapping.getHotline())
                && hotlineSupplierMapping.getSupplierId() != null
                && hotlineSupplierMapping.getId() == null) {
            JsonData jsonData2 = hotlineSupplierService.insertHotlineSeat(hotlineSupplierMapping);
            logger.info("jsonData2:{}", JacksonUtil.obj2String(jsonData2));
        }
        if (StringUtils.isNotEmpty(hotlineSupplierMapping.getHotline()) && hotlineSupplierMapping.getId() != null) {
            JsonData jsonData = hotlineSupplierService.updateHotlineSeat(hotlineSupplierMapping.getHotline(), hotlineSupplierMapping.getId());
            logger.info("jsonData:{}", JacksonUtil.obj2String(jsonData));
        }
        if (StringUtils.isNotEmpty(shopJid)) {
            String s = hotlineSupplierService.selectHotlineBySupplierId(JID.parseAsJID(shopJid));
            logger.info("s:{}", s);
        }
        // JsonData jsonData1 = hotlineSeatService.deleteHotlineSeat(2);

        return JsonData.success("success");
    }

}
