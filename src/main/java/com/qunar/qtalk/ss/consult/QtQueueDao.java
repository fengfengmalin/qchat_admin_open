package com.qunar.qtalk.ss.consult;


import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qtalk.ss.utils.common.database.DatabaseHelper ;
import com.qunar.qtalk.ss.utils.common.database.DbConfig ;
import com.qunar.qtalk.ss.utils.common.database.QtalkDbManager ;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.consult.entity.*;
import com.qunar.qtalk.ss.sift.entity.CSR;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class QtQueueDao {

    private static final Logger logger = LoggerFactory.getLogger(QtQueueDao.class);

//    public List<QtSessionKey> getSeatsServiceHistory(String qunarName) {
//
//        return null;
//    }

//    public List<QtSessionItem> getServiceSessionHistory(String userName, long shopId, String productId) {
//        return null;
//    }

    public LinkedList<Map.Entry<String, Timestamp>> lastDistributeTime(List<JID> users, long shopId) {
        Connection c;
        LinkedList<Map.Entry<String, Timestamp>> resultList = new LinkedList<>();
        try {

//            select max(distributed_time) as time, seat_name from queue_mapping where shop_id = ? and seat_name in('axzbfwx5053@ejabhost2', 'asdfsdf')  group by seat_name order by time desc;

            String sql = "select max(distributed_time) as time, seat_name from queue_mapping where shop_id = ? and seat_name in(";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);

            StringBuilder sb = new StringBuilder();

            for (JID user : users) {
                sb.append(String.format("'%s',", DatabaseHelper.antiSQLInjection(user.toBareJID())));
            }

            String usersString = sb.substring(0, sb.length() - 1);

            sql = sql + usersString + ") group by seat_name order by time asc;";


            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setLong(1, shopId);

            ResultSet result = stmt.executeQuery();

            while (result.next()) {
                Timestamp timeStamp = result.getTimestamp(1);
                String user = result.getString(2);

                Map<String, Timestamp> value = new HashedMap();
                value.put(user, timeStamp);

                resultList.add(value.entrySet().iterator().next());
            }

            result.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("lastDistributeTime failed.", e);
        }
        return resultList;
    }

    public void updateInServiceSeatInfo(CSR csr, QtSessionKey key) {
        Connection c;
        try {

            String sql = "update queue_mapping set seat_id = ?, seat_name = ?, distributed_time = now(), product_id= ? where customer_name = ? " +
                    "and shop_id = ?;";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setLong(1, csr.getId());
            stmt.setString(2, csr.getQunarName().toBareJID());
            stmt.setString(3, key.getProductId());
            stmt.setString(4, key.getUserName().toBareJID());
            stmt.setLong(5, key.getShopId());

            int result = stmt.executeUpdate();

            logger.info("updateinfo and result is {},  1:{}, 2:{}, 3:{}, 4:{}, 5:{}", result,
                    csr.getId(), csr.getQunarName().toBareJID(), key.getUserName().toBareJID(),
                    key.getShopId(), key.getProductId());

            stmt.close();
            if (!c.getAutoCommit())
                c.commit();
        } catch (SQLException e) {
            logger.error("updateInServiceSeatInfo failed.", e);
        }
    }

    public void transformCsrUpdateSeat(Long shopId, Long csrId, String conditionCsrName, String updateCsrName, String customName) {

        try {
            String sql = "update queue_mapping set seat_id=?, seat_name=? where customer_name=? and seat_name=? and shop_id=?;";

            Connection c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);
            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setLong(1, csrId);
            stmt.setString(2, updateCsrName);
            stmt.setString(3, customName);
            stmt.setString(4, conditionCsrName);
            stmt.setLong(5, shopId);

            int result = stmt.executeUpdate();
            logger.info("transformCsrUpdateSeat result:{}", result);
            stmt.close();
            if (!c.getAutoCommit())
                c.commit();

        } catch (SQLException e) {
            logger.error("transformCsrUpdateSeat error", e);
        }
    }

    public QtSessionKey closeSession(JID user, long shopId, JID seatName) {
        Connection c;
        boolean autoCommit;
        QtSessionKey key = new QtSessionKey(user, shopId);
        try {
            String sql = "insert into session_mapping(customer_name, shop_id, product_id, session_id, seat_id, seat_name, status, request_count, distributed_time, inqueue_time, last_ack_time)" +
                    " select customer_name, shop_id, product_id, session_id, seat_id, seat_name, status, request_count, distributed_time, inqueue_time, last_ack_time from queue_mapping " +
                    "where customer_name = ? and shop_id = ? and seat_name = ? returning session_id, product_id;";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);
            autoCommit = c.getAutoCommit();

            if (autoCommit)
                c.setAutoCommit(false);

            String sessionId = null;
            String productId = null;

            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setString(1, user.toBareJID());
            stmt.setLong(2, shopId);
            stmt.setString(3, seatName.toBareJID());

            ResultSet result = stmt.executeQuery();

            if (result.next()) {
                sessionId = result.getString(1);
                productId = result.getString(2);
            }
            key.setProductId(productId);

            // stmt.close();

            sql = "delete from queue_mapping where session_id = ?;";

            stmt = c.prepareStatement(sql);

            stmt.setString(1, sessionId);

            stmt.executeUpdate();
            stmt.close();

            c.commit();

            if (autoCommit)
                c.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("finishSession failed.", e);
        }
        return key;
    }

    public void removeQueueUser(List<QueueUser> queueUsers) {
        Connection c;
//        QtSessionItem value = null;
        boolean autoCommit;
        try {

            String sql = "delete from queue_mapping where session_id in (";

            StringBuilder sb = new StringBuilder();

            for (QueueUser user : queueUsers) {
                sb.append(String.format("'%s',", DatabaseHelper.antiSQLInjection(user.getSessionId())));
            }

            String usersString = sb.substring(0, sb.length() - 1);

            sql = sql + usersString + ");";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);
            autoCommit = c.getAutoCommit();

            if (!autoCommit)
                c.setAutoCommit(true);

            PreparedStatement stmt = c.prepareStatement(sql);

            if (stmt.executeUpdate() == queueUsers.size()) {
                logger.debug("removeQueueUser success");
            } else {
                throw new SQLException("sql failed!", sql);
            }
            stmt.close();
            c.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            logger.error("getConfig failed.", e);
        }
    }

    public Collection<QtQueueItem> getTimeoutQtQueueItem(Timestamp timeout) {
        Connection c;


        Collection<QtQueueItem> returnValue = null;

        try {
            String sql = "select * from queue_mapping where seat_id = 0 and last_ack_time <= ?;";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setTimestamp(1, timeout);
            ResultSet result = stmt.executeQuery();

            returnValue = buildQueueList(result).values();

            result.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("getConfig failed.", e);
        }
        return returnValue;
    }


    public Collection<QtSessionItem> getTimeoutSessions(Timestamp timeout) {
        Connection c;
        List<QtSessionItem> timeoutedSessions = new ArrayList<>();
        try {
            String sql = "select * from queue_mapping where seat_id > 0 and last_ack_time <= ? AND status = ?;";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setTimestamp(1, timeout);
            stmt.setInt(2, QtQueueStatus.SeatLast.code);
            ResultSet result = stmt.executeQuery();

            while (result.next()) {

                String stringValue = result.getString("customer_name");

                if (StringUtils.isNotEmpty(stringValue) && !stringValue.contains("@")) {
                    stringValue = String.format("%s@%s", stringValue,  QChatConstant.DEFAULT_HOST);
                }
                JID user = JID.parseAsJID(stringValue);
                long longValue = result.getLong("shop_id");
                stringValue = result.getString("product_id");
                QtSessionItem item = new QtSessionItem(user, longValue, stringValue);

                bindQueueItemInfo(item, result);

//                stringValue = result.getString("session_id");
//                item.setSessionId(stringValue);
//
//                Timestamp lastAckTime = result.getTimestamp("last_ack_time");
//
//                item.setLastAckTime(lastAckTime);

                timeoutedSessions.add(item);
            }

            result.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("getConfig failed.", e);
        }
        return timeoutedSessions;
    }

    public void removeTimeoutSessions(Collection<QtSessionItem> sessionList) {
        Connection c;
        boolean autoCommit;
        try {
            String sql = "delete from queue_mapping where session_id in (";

            StringBuilder sb = new StringBuilder();

            for (QtSessionItem user : sessionList) {
                sb.append(String.format("'%s',", DatabaseHelper.antiSQLInjection(user.getSessionId())));
            }

            String usersString = sb.substring(0, sb.length() - 1);

            sql = sql + usersString + ");";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);
            autoCommit = c.getAutoCommit();

            if (autoCommit)
                c.setAutoCommit(false);


            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.executeUpdate();
            c.commit();
            stmt.close();
            c.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            logger.error("removeTimeoutSessions failed.", e);
        }
    }

    public void removeTimeoutQueues(Collection<QtQueueItem> queueList) {
        Connection c;
        try {
            String sql = "delete from queue_mapping where session_id in (";

            StringBuilder sb = new StringBuilder();

            for (QtQueueItem item : queueList) {
                QueueUser user;
                while ((user = item.popQueueUser()) != null) {
                    sb.append(String.format("'%s',", DatabaseHelper.antiSQLInjection(user.getSessionId())));
                }
            }

            String usersString = sb.substring(0, sb.length() - 1);

            sql = sql + usersString + ");";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.executeUpdate();

            stmt.close();
        } catch (SQLException e) {
            logger.error("removeTimeoutQueues failed.", e);
        }
    }

    public Collection<QtSessionItem> finishSessions(Timestamp timeout) {
        Connection c;
        Collection<QtSessionItem> sessionList = null;

        try {
            String sql = "insert into session_mapping(customer_name, shop_id, product_id, session_id, seat_id, status, request_count, distributed_time, inqueue_time, last_ack_time, seat_name)" +
                    " select customer_name, shop_id, product_id, session_id, seat_id, status, request_count, distributed_time, inqueue_time, last_ack_time, seat_name from queue_mapping " +
                    "where seat_id <> 0 and last_ack_time <= ? returning customer_name, shop_id, product_id, session_id, seat_id, status, request_count, distributed_time, inqueue_time, last_ack_time, seat_name;";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setTimestamp(1, timeout);

            ResultSet result = stmt.executeQuery();
            sessionList = buildSessionList(result);
            stmt.close();
        } catch (SQLException e) {
            logger.error("finishSession failed.", e);
        }
        return sessionList;
    }

    public Collection<QtSessionItem> getValidSessions(Timestamp timeout) {
        return getValidSessions(timeout, null);
    }


    public Collection<QtSessionItem> getValidSessions(Timestamp timeout1, Timestamp timeout2) {
        Connection c;

        Collection<QtSessionItem> returnValue = null;
        try {
            String sql;
            if (timeout2 == null)
                sql = " select * from queue_mapping where seat_id > 0 and last_ack_time >= ?;";
            else
                sql = " select * from queue_mapping where seat_id > 0 and last_ack_time >= ? and last_ack_time <= ?;";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setTimestamp(1, timeout1);

            if (timeout2 != null)
                stmt.setTimestamp(2, timeout2);

            ResultSet result = stmt.executeQuery();

            returnValue = buildSessionList(result);

            stmt.close();
        } catch (SQLException e) {
            logger.error("getValidSessions failed. {} {} ", timeout1, timeout2, e);
        }
        return returnValue;
    }

    public Collection<QtQueueItem> getValidQueues(Timestamp timedout) {
        Connection c;

        Collection<QtQueueItem> returnValue = null;

        try {
            String sql = "select * from queue_mapping where seat_id = 0 and last_ack_time >= ?;";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setTimestamp(1, timedout);
            ResultSet result = stmt.executeQuery();

            returnValue = buildQueueList(result).values();

            result.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("getConfig failed.", e);
        }
        return returnValue;
    }

    public void updateMsgLog(QtMessageLog log) {
        Connection c;
        boolean autoCommit;
        try {
            String sql = "UPDATE queue_mapping set last_ack_time = ?, status = ? where customer_name = ? and  shop_id = ?;";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);

            autoCommit = c.getAutoCommit();
            if (autoCommit)
                c.setAutoCommit(false);
            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setTimestamp(1, new Timestamp(log.getTime()));
            stmt.setInt(2, log.getIsCustomerMsg() ? QtQueueStatus.CustomerLast.getCode() : QtQueueStatus.SeatLast.getCode());
            stmt.setString(3, log.getUser().toBareJID());
            stmt.setLong(4, log.getShopId());

            stmt.executeUpdate();
            c.commit();

            stmt.close();

            if (autoCommit)
                c.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            logger.error("updateMsgLog failed.", e);
        }
    }

    public void saveNoneRealtoMessage(Collection<QtUnSentMessage> messages) {
        Connection c;
        try {
            String sql = "INSERT INTO queue_saved_message(customer_name, shop_id, message) VALUES (?, ?, ?);";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);

            for (QtUnSentMessage msg : messages) {
                stmt.setString(1, msg.getUserName().toBareJID());
                stmt.setLong(2, msg.getShopId());
                PGobject jsonObject = new PGobject();
                jsonObject.setType("jsonb");
                jsonObject.setValue(msg.getMessage());

                stmt.setObject(3, jsonObject);
                stmt.addBatch();
            }

            stmt.executeBatch();

            if (!c.getAutoCommit())
                c.commit();
            stmt.close();
        } catch (SQLException e) {
            logger.error("saveNoneRealtoMessage failed.", e);
        }
    }

    public Collection<QtUnSentMessage> getUnsentMessage(long shopId, JID jid) {
        Connection c;

        List<QtUnSentMessage> lists = new ArrayList<>();
        try {
            String sql = "select * from queue_saved_message where customer_name = ? and shop_id = ? ORDER BY id ASC;";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setString(1, jid.toBareJID());
            stmt.setLong(2, shopId);

            ResultSet var = stmt.executeQuery();


            while (var.next()) {
                QtUnSentMessage message = new QtUnSentMessage();
                String stringValue = var.getString("message_id");
                message.setMessageId(stringValue);

                stringValue = var.getString("customer_name");
                message.setUserName(JID.parseAsJID(stringValue));

                long longValue = var.getLong("shop_id");
                message.setShopId(longValue);

                PGobject jsonObject = (PGobject) var.getObject("message");
                if (jsonObject != null && jsonObject.getType().equalsIgnoreCase("jsonb")) {
                    message.setMessage(jsonObject.getValue());
                }
                lists.add(message);
            }
            var.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("saveNoneRealtoMessage failed.", e);
        }
        return lists;
    }

    public void deleteUnSentMessages(Collection<QtUnSentMessage> messages) {

        Connection c;
        try {
            String sql = "delete from queue_saved_message where message_id in (";

            sql = DatabaseHelper.makeInCollecitonSql(sql, messages, o -> {
                QtUnSentMessage msg = (QtUnSentMessage) o;
                return msg.getMessageId();
            });

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.executeUpdate();

            if (!c.getAutoCommit())
                c.commit();

            stmt.close();
        } catch (SQLException e) {
            logger.error("saveNoneRealtoMessage failed.", e);
        }
    }

    public void releseSessionItems(List<String> sessionList) {
        Connection c;
        try {
            String sql = "update queue_mapping set status = ? where session_id in (";

            sql = DatabaseHelper.makeInCollecitonSql(sql, sessionList, o -> {
                String msg = (String) o;
                return msg;
            });

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);

            logger.info("releseSessionItems, sql is {}", sql);

            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setInt(1, QtQueueStatus.SeatReleased.code);
            stmt.executeUpdate();

            if (!c.getAutoCommit())
                c.commit();
            stmt.close();
        } catch (SQLException e) {
            logger.error("releseSessionItems failed.", e);
        }
    }


    private static class Holder {
        private static final QtQueueDao INSTANCE = new QtQueueDao();
    }

    public static QtQueueDao getInstance() {
        return QtQueueDao.Holder.INSTANCE;
    }


    public QtSessionItem getConfig(QtSessionKey key) {
        Connection c;
        QtSessionItem value = null;
        boolean autoCommit;
        try {
            String sql = "select * from queue_mapping where customer_name = ? and shop_id = ?";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);
            autoCommit = c.getAutoCommit();

            if (autoCommit)
                c.setAutoCommit(false);


            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setString(1, key.getUserName().toBareJID());
            stmt.setLong(2, key.getShopId());

            ResultSet result = stmt.executeQuery();

            while (result.next()) {

                value = new QtSessionItem(key.getUserName(), key.getShopId());

                bindQueueItemInfo(value, result);
            }

            result.close();
            stmt.close();
            c.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            logger.error("getConfig failed.", e);
        }

        return value;
    }

    public QueueUser addNewQueue(QtSessionKey key) {
        Connection c;

        QueueUser queueUser = null;

        boolean autoCommit;

        try {
            String sql = "INSERT INTO queue_mapping(customer_name, shop_id, product_id) VALUES (?, ?, ?) ON CONFLICT (customer_name, shop_id)" +
                    "DO UPDATE SET request_count = queue_mapping.request_count + 1, inqueue_time = now(), product_id=? RETURNING request_count, inqueue_time, last_ack_time, session_id;";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueMasterConfig, sql);
            autoCommit = c.getAutoCommit();

            if (!autoCommit)
                c.setAutoCommit(true);

            PreparedStatement stmt = c.prepareStatement(sql);

            String productId = key.getProductId();

            stmt.setString(1, key.getUserName().toBareJID());
            stmt.setLong(2, key.getShopId());
            stmt.setString(3, productId == null ? QtSessionItem.DEFAULT_PRODUCTID : productId);
            stmt.setString(4, productId == null ? QtSessionItem.DEFAULT_PRODUCTID : productId);
            ResultSet result = stmt.executeQuery();

            while (result.next()) {
                queueUser = new QueueUser();
                queueUser.setJid(key.getUserName());
                int requestcount = result.getInt("request_count");
                queueUser.setRequestCount(requestcount);

                Timestamp timeStamp = result.getTimestamp("last_ack_time");
                queueUser.setLastAckTime(timeStamp);

                timeStamp = result.getTimestamp("inqueue_time");
                queueUser.setInQueueTime(timeStamp);

                String sessionId = result.getString("session_id");
                queueUser.setSessionId(sessionId);
            }
            // c.commit();

            stmt.close();


        } catch (SQLException e) {
            logger.error("addNewQueue failed.", e);
        }
        return queueUser;
    }

    public QtQueueItem getQueue(QtQueueKey queueKey) {

        Connection c;

        QtQueueItem queueItem = new QtQueueItem(queueKey);

        try {
            String sql = "select * from queue_mapping where shop_id = ? and seat_id= 0;";

            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);

            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setLong(1, queueKey.getShopId());
//            stmt.setString(2,
//                    StringUtils.isNotEmpty(queueKey.getProductId()) ? queueKey.getProductId() : QtSessionItem.DEFAULT_PRODUCTID);

            ResultSet result = stmt.executeQuery();

            while (result.next()) {

                QueueUser qu = new QueueUser();

                String stringValue = result.getString("customer_name");

                if (StringUtils.isNotEmpty(stringValue) && !stringValue.contains("@")) {
                    stringValue = String.format("%s@%s", stringValue,  QChatConstant.DEFAULT_HOST);
                }

                qu.setJid(JID.parseAsJID(stringValue));


                stringValue = result.getString("session_id");
                qu.setSessionId(stringValue);

                queueItem.addSessionItem(qu);
            }
            stmt.close();
        } catch (SQLException e) {
            logger.error("getQueue failed.", e);
        }
        return queueItem;
    }

    protected Collection<QtSessionItem> buildSessionList(ResultSet result) throws SQLException {
        List<QtSessionItem> sessionList = new ArrayList<>();
        while (result.next()) {
            long longvalue = result.getLong("shop_id");
            String stringValue = result.getString("product_id");
            String productId = stringValue;
            stringValue = result.getString("customer_name");
            if (StringUtils.isNotEmpty(stringValue) && !stringValue.contains("@"))
                stringValue = String.format("%s@%s", stringValue, QChatConstant.DEFAULT_HOST);

            QtSessionItem item = new QtSessionItem(JID.parseAsJID(stringValue), longvalue, productId);

            bindQueueItemInfo(item, result);

            sessionList.add(item);
        }
        return sessionList;
    }

    private Map<QtQueueKey, QtQueueItem> buildQueueList(ResultSet result) throws SQLException {
        Map<QtQueueKey, QtQueueItem> maps = new Hashtable<>();
        while (result.next()) {
            String stringValue = result.getString("customer_name");

            if (StringUtils.isNotEmpty(stringValue) && !stringValue.contains("@")) {
                stringValue = String.format("%s@%s", stringValue, QChatConstant.DEFAULT_HOST);
            }
            JID user = JID.parseAsJID(stringValue);

            long longValue = result.getLong("shop_id");
            stringValue = result.getString("product_id");

            QtQueueKey key = new QtQueueKey(longValue, QtSessionItem.DEFAULT_PRODUCTID);

            QtQueueItem item = maps.get(key);

            if (item == null) {
                item = new QtQueueItem(key);
                maps.put(key, item);
            }

            QueueUser qu = new QueueUser();
            qu.setJid(user);
            stringValue = result.getString("session_id");
            qu.setSessionId(stringValue);
            item.addSessionItem(qu);
        }
        return maps;
    }

    private void bindQueueItemInfo(QtSessionItem item, ResultSet result) throws SQLException {
        Timestamp timestamp;
        timestamp = result.getTimestamp("last_ack_time");
        item.setLastAckTime(timestamp);

        timestamp = result.getTimestamp("inqueue_time");
        item.setInQueueTime(timestamp);

        long longValue;

        longValue = result.getLong("request_count");
        item.setRequestCount(longValue);

        int intvalue = result.getInt("status");
        item.setStatus(intvalue);

        longValue = result.getLong("seat_id");

        if (longValue > 0)
            item.setSeatId(longValue);

        String stringValue;
        stringValue = result.getString("customer_name");
        item.setUserName(JID.parseAsJID(stringValue));

        stringValue = result.getString("session_id");
        item.setSessionId(stringValue);

        stringValue = result.getString("seat_name");

        if (StringUtils.isNotEmpty(stringValue)) {
            if (!stringValue.contains("@")) {
                stringValue = String.format("%s@%s", stringValue, QChatConstant.DEFAULT_HOST);
            }
            item.setSeatQunarName(JID.parseAsJID(stringValue));
        }

    }
    public static Long inQueue() {
        Connection c;
        Long count = 0L;
        try {
            String sql = "select count(1) from queue_mapping where seat_id <> 0;";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);
            Statement statement = c.createStatement();
            ResultSet result = statement.executeQuery(sql);
            while (result.next()) {
                count = result.getLong("count");
            }
            statement.close();
        } catch (SQLException e) {
            logger.error("finishSession failed.", e);
        }
        return count;
    }

    public String getConsultStatus(String customerName, long shopId, String seatName, Timestamp timestamp) {
        Connection c;
        String productId = null;
        try {
            String sql = "select product_id from queue_mapping where customer_name=? and shop_Id=? and seat_name = ? and status=? and last_ack_time<?;";
            c = QtalkDbManager.getInstance().getConnection(DbConfig.QtQueueSlaveConfig, sql);
            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setString(1, customerName);
            stmt.setLong(2, shopId);
            stmt.setString(3, seatName);
            stmt.setInt(4, QtQueueStatus.CustomerLast.getCode());
            stmt.setTimestamp(5, timestamp);
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                productId = result.getString("product_id");
            }
            stmt.close();
        } catch (SQLException e) {
            logger.error("getConsultStatus failed.", e);
        }
        return productId;
    }

    public static void main(String[] args) {
        Long aLong = inQueue();
        System.out.println(aLong);
    }
}
