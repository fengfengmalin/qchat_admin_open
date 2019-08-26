package com.qunar.qchat.admin.GuavaCache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.service.supplier.SupplierNewService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.query.SupplierIterator;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by yinmengwang on 17-5-27.
 */
@Slf4j
@Service
public class SuggestCacheService {

    Cache<String, AnalyzingInfixSuggester> supplierCache = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.MINUTES).build();

    private static final String key = "supplier.suggest.cache";

    @Resource
    private SupplierNewService supplierNewService;

    public AnalyzingInfixSuggester getSupplierSuggester() {
        try {
            return supplierCache.get(key, new Callable<AnalyzingInfixSuggester>() {
                @Override
                public AnalyzingInfixSuggester call() throws Exception {
                    return buildSupplierSuggester(key);
                }
            });
        } catch (Exception e) {
            log.error("从缓存中取SupplierSuggester对象出错", e);
        }
        return null;
    }

    private AnalyzingInfixSuggester buildSupplierSuggester(String key) {
        List<Supplier> allSupplierInfo = supplierNewService.getAllSupplierInfo();
        if (CollectionUtil.isNotEmpty(allSupplierInfo)) {
            try {
                RAMDirectory index = new RAMDirectory();
                StandardAnalyzer analyzer = new StandardAnalyzer();
                AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(index, analyzer);
                suggester.build(new SupplierIterator(allSupplierInfo.iterator()));
                return suggester;
            } catch (Exception e) {
                log.error("构建supplierSuggester索引出错 key:{}", key, e);
            }
        }
        return null;
    }
}
