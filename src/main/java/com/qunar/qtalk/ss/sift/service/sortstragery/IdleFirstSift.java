package com.qunar.qtalk.ss.sift.service.sortstragery;

import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qtalk.ss.consult.QtQueueManager;
import com.qunar.qtalk.ss.consult.entity.QtSessionKey;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.service.ISiftByAssignStrategy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Service
public class IdleFirstSift implements ISiftByAssignStrategy {

    @Override
    public CSR sift(List<CSR> csrList, Shop shop) {
        Integer idleIndex = -1;
        Double idlest = Double.NEGATIVE_INFINITY;
        csrList.sort(new Comparator<CSR>() {
            @Override
            public int compare(CSR o1, CSR o2) {
                if (o1.getMaxServiceCount() > o2.getMaxServiceCount()) return -1;
                if (o1.getMaxServiceCount() < o2.getMaxServiceCount()) return 1;
                return 0;
            }
        });

        for (int idx = 0; idx < csrList.size(); idx++) {
            CSR csr = csrList.get(idx);
            int maxServices = csr.getMaxServiceCount();
            if (maxServices < 1)
                return null;
            LinkedList<QtSessionKey>  sessionKeys =
                    QtQueueManager.getInstance().workingQueueForSeats(csr.getQunarName());
            //说明没取到正在服务的人，即为最闲的
            if (CollectionUtil.isEmpty(sessionKeys)) {
                return csr;
            }


            Double idleCoff = new Double(maxServices - sessionKeys.size()).doubleValue()/maxServices;
            if (idleCoff.compareTo(idlest) > 0 ){
                idleIndex = idx;
                idlest = idleCoff;
            }
        }

        return csrList.get(idleIndex);
    }
}
