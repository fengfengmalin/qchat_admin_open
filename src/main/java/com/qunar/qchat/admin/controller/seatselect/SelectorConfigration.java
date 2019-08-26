package com.qunar.qchat.admin.controller.seatselect;

import com.qunar.qchat.admin.model.BusinessEnum;
import lombok.Data;

@Data
public final class SelectorConfigration {
    private String busiSupplierId;
    private long supplierId;
    private BusinessEnum businessEnum;
    private String qunarName;
    private String productID;
    private String groupType;
    private String lastSeatName;

    private ISeatSelectorEvents events;

    private SelectorConfigration(Builder builder) {
        setBusiSupplierId(builder.busiSupplierId);
        setSupplierId(builder.supplierId);
        setBusinessEnum(builder.businessEnum);
        setQunarName(builder.qunarName);
        setProductID(builder.productID);
        setGroupType(builder.groupType);
        setLastSeatName(builder.lastSeatName);
        setEvents(builder.events);
    }

    public static final class Builder {
        private String busiSupplierId;
        private long supplierId;
        private BusinessEnum businessEnum;
        private String qunarName;
        private String productID;
        private String groupType;
        private String lastSeatName;
        private ISeatSelectorEvents events;

        public Builder() {
        }

        public Builder busiSupplierId(String val) {
            busiSupplierId = val;
            return this;
        }

        public Builder supplierId(long val) {
            supplierId = val;
            return this;
        }

        public Builder businessEnum(BusinessEnum val) {
            businessEnum = val;
            return this;
        }

        public Builder qunarName(String val) {
            qunarName = val;
            return this;
        }

        public Builder productID(String val) {
            productID = val;
            return this;
        }

        public Builder groupType(String val) {
            groupType = val;
            return this;
        }

        public Builder lastSeatName(String val) {
            lastSeatName = val;
            return this;
        }

        public Builder events(ISeatSelectorEvents val) {
            events = val;
            return this;
        }

        public SelectorConfigration build() {
            return new SelectorConfigration(this);
        }
    }
}
