package com.qunar.qtalk.ss.sift.service.sortstragery;

import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.service.ISiftByAssignStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class RandomSift implements ISiftByAssignStrategy {
    @Override
    public CSR sift(List<CSR> csrList, Shop shop) {
        Random random = new Random();
        return csrList.get(random.nextInt(csrList.size()));
    }
}
