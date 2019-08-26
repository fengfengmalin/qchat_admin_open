package com.qunar.qtalk.ss.sift.service;

import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.Shop;

import java.util.List;

public interface ISiftByAssignStrategy {
    CSR sift(List<CSR> csrList, Shop shop);
}
