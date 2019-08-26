package com.qunar.qchat.admin.util.query;

import com.google.common.base.Function;
import com.qunar.qchat.admin.model.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.util.BytesRef;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by yinmengwang on 17-5-27.
 */
@Slf4j
public class SupplierQueryUtil {

    private static Function<Lookup.LookupResult, Supplier> toSupplier = new Function<Lookup.LookupResult, Supplier>() {
        @Override
        public Supplier apply(Lookup.LookupResult result) {
            if (result == null)
                return null;
            BytesRef bytesRef = result.payload;
            Supplier supplier = (Supplier) deserialize(bytesRef.bytes);
            return supplier;
        }
    };

//    public static List<Supplier> lookup(AnalyzingInfixSuggester suggester, String name) {
//        try {
//            List<Lookup.LookupResult> results = suggester.lookup(name, Config.TRANSFER_SUGGEST_LENGTH, true, false);
//            if (CollectionUtil.isEmpty(results)) {
//                return null;
//            }
//            return Lists.transform(results, toSupplier);
//        } catch (Exception e) {
//            log.error("查找supplier出错,query:{}", name, e);
//        }
//        return null;
//    }

    public static Object deserialize(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0)
            return null;
        InputStream in = new ByteArrayInputStream(byteArray);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(in);
            Object o = ois.readObject();
            return o;
        } catch (Exception e) {
            log.info("Deserialize by jdk exception ", e);
        }
        return null;
    }
}
