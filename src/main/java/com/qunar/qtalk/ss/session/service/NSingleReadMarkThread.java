package com.qunar.qtalk.ss.session.service;

import com.qunar.qtalk.ss.session.dao.IReadMarkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class NSingleReadMarkThread implements Runnable {
    private int hash_index;
    private IReadMarkDao iReadMarkDao;
    private Map<Integer, ArrayBlockingQueue<String>> receivedspool;
    private Map<Integer, ArrayBlockingQueue<String>> readedspool;
    private static final Logger LOGGER = LoggerFactory.getLogger(NSingleReadMarkThread.class);

    public NSingleReadMarkThread(Map<Integer, ArrayBlockingQueue<String>> receivedspool, Map<Integer, ArrayBlockingQueue<String>> readedspool, int i, IReadMarkDao iReadMarkDao){
        this.receivedspool = receivedspool;
        this.readedspool = readedspool;

        this.iReadMarkDao = iReadMarkDao;
        hash_index = i;
        LOGGER.info("NewSingleReadMarkThread start :{}",hash_index);
    }

    @Override
    public void run() {
        try {
            String Ids1 = null;
            int s;


            ArrayBlockingQueue<String> arrayBlockingQueue1;
            arrayBlockingQueue1 = receivedspool.get(hash_index);
            s = arrayBlockingQueue1.size();

            ArrayList<String> arrayList1 = new ArrayList<String>(s);
            arrayBlockingQueue1.drainTo(arrayList1,s);
            if (arrayList1.size()> 0){
                Ids1 = concatIds(arrayList1);
            }

            if (Ids1 == null || Ids1.isEmpty()){
                LOGGER.info("NSingleReadMarkThread1 update  is null");
            }else {
                LOGGER.info("NSingleReadMarkThread1 update  :{}, read_flag is : {}",Ids1, 1);
                iReadMarkDao.updateNewSingleReadmark(1, Ids1);
            }

            String Ids2 = null;
            ArrayBlockingQueue<String> arrayBlockingQueue2;
            arrayBlockingQueue2 = readedspool.get(hash_index);
            s = arrayBlockingQueue2.size();

            ArrayList<String> arrayList2 = new ArrayList<String>(s);
            arrayBlockingQueue2.drainTo(arrayList2,s);
            if (arrayList2.size()> 0){
                Ids2 = concatIds(arrayList2);
            }

            if (Ids2 == null || Ids2.isEmpty()){
                LOGGER.info("NSingleReadMarkThread2 update  is null");
            }else {
                LOGGER.info("NSingleReadMarkThread2 update  :{}, read_flag is : {}",Ids2, 3);
                iReadMarkDao.updateNewSingleReadmark(3, Ids2);
            }
        } catch (Exception e) {
            LOGGER.error("NSingleReadMarkThread error", e);
        }
    }

    private String concatIds(ArrayList<String> arrayList){
        StringBuilder stringBuilder = new StringBuilder();

        for (String id : arrayList){
            stringBuilder.append(" msg_id = '").append(id).append("' or ");
            // ids = ids + " msg_id = '" + id + "' or ";
        }
        String ids = stringBuilder.toString();
        if (ids.length() > 3) {
            return ids.substring(0, ids.length() - 3);
        }
        return ids;
    }
}