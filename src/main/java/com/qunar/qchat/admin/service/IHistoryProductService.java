package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.product.HistoryProduct;

import java.util.List;

/**
 * Created by yinmengwang on 17-4-26.
 */
public interface IHistoryProductService {

    HistoryProduct getLastProduct(String seatQName, String userQName);

    List<HistoryProduct> getProductHistory(String userQName);
}
