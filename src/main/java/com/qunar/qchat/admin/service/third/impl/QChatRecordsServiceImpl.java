package com.qunar.qchat.admin.service.third.impl;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.third.IQChatRecordsService;
import com.qunar.qchat.admin.vo.SeatVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by yinmengwang on 17-5-9.
 */
@Slf4j
@Service("qChatRecordsService")
public class QChatRecordsServiceImpl implements IQChatRecordsService {

    @Resource
    private ISeatService seatService;

    private static final int CONN_TIME = 3000;

    private static final Function<SeatVO, String> getQName = new Function<SeatVO, String>() {
        @Override
        public String apply(SeatVO seatVO) {
            return seatVO != null ? seatVO.getQunarName() : null;
        }
    };

//    @Override
//    public Object getQChatRecords(QChatForTransferParam param, String qunarName, String k) {
//        if (param == null || Strings.isNullOrEmpty(qunarName)) {
//            return null;
//        }
//        if (Strings.isNullOrEmpty(param.getTo()) || !param.getTo().startsWith(Supplier.SHOPID_PREFIX)) {
//            return "to并非店铺id";
//        }
//        String supplierIdStr = param.getTo();
//        long supplierId = Long.valueOf(supplierIdStr.replace(Supplier.SHOPID_PREFIX, ""));
//        // 判断客服是否可以跨供应商 or 客服是否属于该供应商
//        if ( belongsSupplier(qunarName, supplierId)) {
//            return getQChatRecordsFromWeb(param, qunarName, k);
//        }
//        return null;
//    }

    @Override
    public boolean belongsSupplier(String qunarName, long supplierId) {
        if (supplierId <= 0 || Strings.isNullOrEmpty(qunarName)) {
            return false;
        }
        List<SeatVO> seatVOs = seatService.getSeatListBySupplierId(supplierId);
        List<String> qunarNames = Lists.transform(seatVOs, getQName);
        if (qunarNames.contains(qunarName)) {
            return true;
        }
        return false;
    }

//    private Object getQChatRecordsFromWeb(QChatForTransferParam param, String qunarName, String k) {
//        String url = Config.QCHAT_RECORDS_FOR_TRANSFER_URL + "?u=" + qunarName + "&k=" + k;
//        String queryString = JacksonUtils.obj2String(param);
//        try {
//
//            log.info("调用接口:{},begin,param:{},u:{},k:{}", url, queryString, qunarName, k);
//            ResponseWrapper responseWrapper = HttpClients.syncClient(CONN_TIME, CONN_TIME).post(url, queryString);
//            log.info("调用接口:{},end,result:{}", url, responseWrapper.getContent());
//            if (responseWrapper.getStatus() == HttpStatus.SC_OK) {
//                QChatResult qChatResult = JacksonUtils.string2Obj(responseWrapper.getContent(), QChatResult.class);
//                if (qChatResult.isRet()) {
//                    return qChatResult.getData();
//                } else {
//                    return qChatResult.getErrmsg();
//                }
//            }
//        } catch (Exception e) {
//            log.error("调用接口:{}出错,param:{},u:{},k:{}", url, queryString, qunarName, k, e);
//        }
//        return "调用接口出错";
//    }
}

@Data
class QChatResult {
    private boolean ret;
    private int errcode;
    private String errmsg;
    private Object data;
}