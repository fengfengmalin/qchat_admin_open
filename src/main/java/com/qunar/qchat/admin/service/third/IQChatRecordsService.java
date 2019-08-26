package com.qunar.qchat.admin.service.third;

/**
 * Created by yinmengwang on 17-5-9.
 */
public interface IQChatRecordsService {

//    Object getQChatRecords(QChatForTransferParam param,String qunarName,String k);

    boolean belongsSupplier(String qunarName,long supplierId);

}
