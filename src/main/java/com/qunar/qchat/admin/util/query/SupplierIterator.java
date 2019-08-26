package com.qunar.qchat.admin.util.query;

import com.qunar.qchat.admin.model.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by yinmengwang on 17-5-27.
 */
@Slf4j
public class SupplierIterator implements InputIterator {

    private Iterator<Supplier> supplierIterator;
    private Supplier curSupplier;

    public SupplierIterator(Iterator<Supplier> supplierIterator) {
        this.supplierIterator = supplierIterator;
    }

    @Override
    public long weight() {
        return 1;
    }

    @Override
    public BytesRef payload() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oot = new ObjectOutputStream(bos);
            oot.writeObject(curSupplier);
            oot.close();
            return new BytesRef(bos.toByteArray());
        } catch (IOException e) {
            log.error("写入curSupplier对象出错", e);
        }
        return null;
    }

    @Override
    public boolean hasPayloads() {
        return true;
    }

    @Override
    public Set<BytesRef> contexts() {
        return null;
    }

    @Override
    public boolean hasContexts() {
        return false;
    }

    @Override
    public BytesRef next() throws IOException {
        if (supplierIterator.hasNext()) {
            curSupplier = supplierIterator.next();
            try {
                return new BytesRef(curSupplier.getName().getBytes("UTF8"));
            } catch (Exception e) {
                log.error("SupplierIterator.next方法出错", e);
            }
        }
        return null;
    }
}
