package com.qunar.qchat.admin.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Created by hongwu.yang on 2015年10月14日.
 */
@SuppressWarnings("serial")
public class NotifyDumperDataSourceTransactionManager extends DataSourceTransactionManager {
	private final Logger logger = LoggerFactory.getLogger(NotifyDumperDataSourceTransactionManager.class);
	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		logger.debug("NotifyDumperDataSourceTransactionManager begin");
		super.doCleanupAfterCompletion(transaction);
	}
}
