package com.qunar.qtalk.ss.session.service;

import com.qunar.qtalk.ss.session.dao.IReadMarkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by MSI on 2017/9/28.
 */

@Service
public  class  ReadMarkManager {

    @Autowired
    private IReadMarkDao iReadMarkDao;

//    @Autowired
//    private ReadedMarkManager readeMarkManager;

    private  Map<Integer,ArrayBlockingQueue<String>> spool;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadMarkManager.class);


    @PostConstruct
    void init(){
        startThreads();
    }


    ScheduledExecutorService service;
    public ReadMarkManager() {
        service = Executors.newScheduledThreadPool(5);
        spool = new HashMap<Integer, ArrayBlockingQueue<String>>();
    }

//    public  void  setReadMarkId(String id)
//    {
//        int index = id.hashCode();
//        index = index % 5;
//        if (index <0){
//            index = index * -1;
//        }
//
//        ArrayBlockingQueue<String>  arrayBlockingQueue = spool.get(index);
//        if (!arrayBlockingQueue.contains(id)){
//            LOGGER.info("bodys id :{}",id);
//            arrayBlockingQueue.add(id);
//        }
//    }

    public void startThreads(){
        for (int i=0 ;i<7;i++) {
            spool.put(i,new ArrayBlockingQueue<String>(500));
            service.scheduleWithFixedDelay(new SingleReadMarkThread(i,iReadMarkDao),30,30, TimeUnit.MINUTES);
        }
    }

    public class SingleReadMarkThread implements Runnable {
        private int hash_index;
        private IReadMarkDao iReadMarkDao;

        public SingleReadMarkThread(int i,IReadMarkDao iReadMarkDao){
            this.iReadMarkDao = iReadMarkDao;
            hash_index = i;
            LOGGER.info("SingleReadMarkThread start :{}",hash_index);
        }
        @Override
        public void run() {
            try {
                ArrayBlockingQueue<String> arrayBlockingQueue;
                String Ids = null;

                arrayBlockingQueue = spool.get(hash_index);
                int s = arrayBlockingQueue.size();
                int size = 100;
                if (s < size) {
                    size = s;
                }

                ArrayList<String> arrayList = new ArrayList<String>(size);
                arrayBlockingQueue.drainTo(arrayList,size);
                if (arrayList.size()> 0){
                    Ids = concatIds(arrayList);
                }

                if (Ids == null || Ids.isEmpty()){
                    LOGGER.info("SingleReadMarkThread update  is null");
                }else {
                    LOGGER.info("SingleReadMarkThread update  :{}",Ids);
                    iReadMarkDao.updateSingleReadmark(Ids);
                }

            }catch(Exception e){
                LOGGER.error("SingleReadMarkThread error", e);
            }
        }

        private String concatIds(ArrayList<String> arrayList){

            StringBuilder stringBuilder = new StringBuilder();
            for (String id : arrayList){
                // ids = ids + " msg_id = '" + id + "' or ";
                stringBuilder.append(" msg_id = '").append(id).append("' or ");
            }
            String ids = stringBuilder.toString();
            if (ids.length() > 3) {
                return ids.substring(0, ids.length() - 3);
            }
            return ids;
        }
    }


    public void processReadmarkMessage(Map<String, Object> readmarkMessage) {

//        Map<String, Object> args = Xml2Json.xmppToMap(readmarkMessage.get("m_body").toString());
        String from = readmarkMessage.get("m_from").toString();
        LOGGER.info("processReadmarkMessage from :{}", from);
//        String from_host = readmarkMessage.get("from_host").toString();

//        String read_type =  "1";
//        HashMap<String, String> MsgMap = (HashMap<String, String>)args.get("message");
//        read_type = MsgMap.get("read_type");
//        HashMap<String, String> bodyMap = (HashMap<String, String>) args.get("body");
//        String bodys = bodyMap.get("content");
/*        switch (read_type) {
            case "1":
                handleSingleReadmark(bodys);
                break;
            case "2":
                handleMucReadmark(bodys,from,from_host);
                break;
            case "0":
                handleAllReamark(bodys,from,from_host);
                break;
            case "3":
                readeMarkManager.handleSingleReadmark(bodys, from,from_host, read_type);
                break;
            case "4":
                readeMarkManager.handleSingleReadmark(bodys,from,from_host, read_type);
                break;
            default:
                break;
        }*/
    }

//    private void handleSingleReadmark(String bodys)
//    {
//        ObjectMapper mapper = new ObjectMapper();
//
//        List<Map<String, String>> arrayList = null;
//        try {
//            arrayList = mapper.readValue(bodys, List.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for( Map<String,String> i : arrayList){
//            String id = i.get("id");
//            setReadMarkId(id);
//            setReadMarkId(getOtherMsgid(id));
//        }
//    }
//    private void handleMucReadmark(String bodys,String username,String from_host) {
//
//        ObjectMapper mapper = new ObjectMapper();
//        List<Map<String, Object>> arrayList = null;
//        try {
//            arrayList = mapper.readValue(bodys, List.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (Map<String, Object> i : arrayList) {
//            String id = i.get("id").toString();
//            String domain = i.get("domain").toString();
//            Long t = (Long) i.get("t");
//            iReadMarkDao.updateMucReadmark(t, id, username, from_host);
//
//        }
//    }
//    private void handleAllReamark(String bodys,String username,String host) {
//        Map<String, Object> args = JacksonUtils.string2Map(bodys);
//        long T = (long) args.get("T");
//        iReadMarkDao.updateAllSingle(username, host, T / 1000);
//        iReadMarkDao.updateAllMuc(username, host, T / 1000);
//    }

//    private String getOtherMsgid(String id) {
//        if(id.startsWith("consult-")) {
//            return id.substring(8);
//        } else {
//            return "consult-" + id;
//        }
//    }

}
