package com.qunar.qtalk.ss.session.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by MSI on 2017/9/28.
 */

@Service
public  class ReadedMarkManager {

//    @Autowired
//    private IReadMarkDao iReadMarkDao;

    private Map<Integer, ArrayBlockingQueue<String>> readedspool;
    private Map<Integer, ArrayBlockingQueue<String>> receivedspool;

//    private static final Logger LOGGER = LoggerFactory.getLogger(ReadedMarkManager.class);


    @PostConstruct
    void init() {
        startThreads();
    }

    ScheduledExecutorService service;

    public ReadedMarkManager() {
        service = Executors.newScheduledThreadPool(5);
        readedspool = new HashMap<Integer, ArrayBlockingQueue<String>>();
        receivedspool = new HashMap<Integer, ArrayBlockingQueue<String>>();
    }

    public void startThreads() {
        for (int i = 0; i < 7; i++) {
            receivedspool.put(i, new ArrayBlockingQueue<String>(500));
            readedspool.put(i, new ArrayBlockingQueue<String>(500));

            // service.scheduleWithFixedDelay(new NSingleReadMarkThread(receivedspool, readedspool, i, iReadMarkDao), 5, 5, TimeUnit.SECONDS);
        }
    }

//    public void handleSingleReadmark(String bodys,String username,String from_host, String read_type) {
//        handleSingleReadmarkIds(bodys, read_type);
//    }


//    private void handleSingleReadmarkIds(String bodys, String read_type) {
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
//            setReadMarkId(id, read_type);
//            setReadMarkId(getOtherMsgid(id), read_type);
//        }
//    }

//    private String getOtherMsgid(String id) {
//        if(id.startsWith("consult-")) {
//            return id.substring(8);
//        } else {
//            return "consult-" + id;
//        }
//    }

//    private void setReadMarkId(String id, String read_type)
//    {
//        int index = id.hashCode();
//        index = index % 5;
//        if (index <0){
//            index = index * -1;
//        }
//
//        ArrayBlockingQueue<String>  arrayBlockingQueue = null;
//        switch (read_type) {
//            case "3":
//                arrayBlockingQueue = receivedspool.get(index);
//                break;
//            case "4":
//                arrayBlockingQueue = readedspool.get(index);
//                break;
//            default:
//                break;
//        }
//
//        if (arrayBlockingQueue != null && !arrayBlockingQueue.contains(id)){
//            LOGGER.info("bodys id :{} index {}, read_type {}",id, index, read_type);
//            arrayBlockingQueue.add(id);
//        }
//    }
}