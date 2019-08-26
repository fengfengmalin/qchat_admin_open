package com.qunar.qtalk.ss.consult;

import com.qunar.qtalk.ss.session.service.ConsultMessageService;
import com.qunar.qtalk.ss.sift.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SpringComponents {

    public static SpringComponents components;

    @PostConstruct
    public void init() {
        components = this;
    }

    @Autowired
    public SiftStrategyService siftStrategyService;

    @Autowired
    public ShopService shopService;

    @Autowired
    public CsrService csrService;

    @Autowired
    public BusiShopMapService busiShopMapService;

    @Autowired
    public ConsultMessageService consultMessageService;


    @Autowired
    public HotlineSupplierService hotlineSupplierService;

    @Autowired
    public QueueMappingService queueMappingService;
}
