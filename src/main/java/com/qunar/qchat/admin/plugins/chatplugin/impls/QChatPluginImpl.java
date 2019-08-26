package com.qunar.qchat.admin.plugins.chatplugin.impls;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.annotation.routingdatasource.DataSources;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.model.responce.BaseResponce;
import com.qunar.qchat.admin.plugins.chatplugin.BaseChatPlugin;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.SeatOnlineState;
import com.qunar.qtalk.ss.consult.SpringComponents;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import com.qunar.qtalk.ss.utils.SendMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QChatPluginImpl extends BaseChatPlugin {

    public QChatPluginImpl() {
        setDataSources();
    }

    @Override
    public List<SeatOnlineState> getUsersOnlineStatus(List<String> strIdList) {
        if (CollectionUtil.isEmpty(strIdList)) {
            return null;
        }

        OnlineState defaultState = OnlineState.OFFLINE;

        List<SeatOnlineState> retList = Lists.newArrayList();
        for (String userid : strIdList) {
            SeatOnlineState sosResult = new SeatOnlineState();
            sosResult.setStrId(userid);
            String userIdDomain = SendMessage.appendQCDomain(userid);

            boolean isOnline = SpringComponents.components.siftStrategyService.judgeOnline(JID.parseAsJID(userIdDomain));
            if (isOnline) {
                sosResult.setOnlineState(OnlineState.ONLINE);
            } else {
                sosResult.setOnlineState(defaultState);
            }
            retList.add(sosResult);
        }
        assignSeatAppender.info("getUsersOnlineStatus return result:{}", JacksonUtils.obj2String(retList));
        return retList;
    }

    private class CheckLoginNameIsNotExists {
        private boolean myResult;
        private String p;
        private String userName;
        private String key;
        private BusiReturnResult result;

        public CheckLoginNameIsNotExists(String p, BusiReturnResult result, String key) {
            this.p = p;
            this.result = result;
            this.key = key;
        }

        boolean is() {
            return myResult;
        }

        public String getUserName() {
            return userName;
        }

        public CheckLoginNameIsNotExists invoke() {
//            Map<String, ?> userMap = getUserInfoByLoginName(key, p);
//            List<Map<String, Object>> userInfoList = (List<Map<String, Object>>) userMap.get("data");
//            CollectionUtil.filterNull(userInfoList);
//            if (CollectionUtil.isEmpty(userInfoList)) {
//                result.setRet(false);
//                result.setCode(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode());
//                Map<String, Object> map = Maps.newHashMap();
//                map.put("userName", p);
//                result.setData(map);
//                myResult = true;
//                return this;
//            }
            userName = p;
            myResult = false;
            return this;
        }

        Map<String, ?> getUserInfoByLoginName(String key, String loginNameValue) {
            if (StringUtils.isEmpty(loginNameValue) || StringUtils.isEmpty(key)) {
                return null;
            }
            Map<String, String> formParams = new HashMap<>();
            formParams.put(key, loginNameValue);
            formParams.put("outEncrypt", "true");
            String jsonRes = HttpClientUtils.post(Config.USER_CENTER_INFO_URL, formParams);
            return JacksonUtil.string2Map(jsonRes);
        }
    }


    @Override
    public BusiReturnResult checkUserExist(String p) {
        String userName = null;
        BusiReturnResult result = new BusiReturnResult();
        // 判断输入是否邮箱
        if (StringCheckUtil.isEmail(p)) {
            // 判断邮箱在用户中心是否存在
            CheckLoginNameIsNotExists checkLoginNameIsNotExists = new CheckLoginNameIsNotExists(p, result, "email").invoke();
            if (checkLoginNameIsNotExists.is()) return result;
            userName = checkLoginNameIsNotExists.getUserName();
        }

        // 判断输入是否是手机号
        if (StringCheckUtil.isMobile(p)) {
            // 判断手机号在用户中心是否存在
            CheckLoginNameIsNotExists checkLoginNameIsNotExists = new CheckLoginNameIsNotExists(p, result, "mobile").invoke();
            if (checkLoginNameIsNotExists.is()) return result;
            userName = checkLoginNameIsNotExists.getUserName();
        }

        if (StringUtils.isEmpty(userName)) {
            // 判断用户名在用户中心是否存在
            CheckLoginNameIsNotExists checkLoginNameIsNotExists = new CheckLoginNameIsNotExists(p, result, "username").invoke();
            if (checkLoginNameIsNotExists.is()) return result;
            userName = checkLoginNameIsNotExists.getUserName();
        }

        result.setRet(true);
        result.setCode(BusiResponseCodeEnum.SUCCESS.getCode());

        Map<String, Object> map = Maps.newHashMap();
        map.put("userName", userName);
        result.setData(map);
        return result;
    }


    @Override
    public boolean sendThirdMessage(String from, String to, String message) {
        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to))
            return false;
        from = EjabdUtil.makeSureUserJid(from, QChatConstant.QCHAR_HOST);
        to = EjabdUtil.makeSureUserJid(to, QChatConstant.QCHAR_HOST);


        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, from);
        param.put(QChatConstant.Note.TO, to);
        param.put(QChatConstant.Note.MESSAGE, message);
        param.put("system", "vs_qchat_admin");
        String response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));

        assignSeatAppender.info("post url {} ;data : {}; res : {}", Config.SEND_NOTE_URL, JacksonUtils.obj2String(param), response);

        if (!Strings.isNullOrEmpty(response)) {
            BaseResponce qChatResult = JacksonUtils.string2Obj(response, BaseResponce.class);
            if (qChatResult != null && qChatResult.isRet()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sendThirePresence(String from, String to, String category, String body) {
        if (Strings.isNullOrEmpty(to))
            return false;

        from = EjabdUtil.makeSureUserJid(from, QChatConstant.QCHAR_HOST);
        to = EjabdUtil.makeSureUserJid(to, QChatConstant.QCHAR_HOST);

        Map<String, String> precenceParam = Maps.newHashMap();
        precenceParam.put("from", from);
        precenceParam.put("to", to);
        precenceParam.put("category", "99");
        precenceParam.put("data", body);

        String response = HttpClientUtils.newPostJson("", JacksonUtils.obj2String(precenceParam));

//        assignSeatAppender.info("post url {} ;data : {}; res : {}", Config.QCHAT_INNER_SEND_NOTIFY, JacksonUtils.obj2String(precenceParam), response);

        if (Strings.isNullOrEmpty(response)) {
            BaseResponce responce = JacksonUtil.string2Obj(response, BaseResponce.class);
            if (null != responce && responce.isRet())
                return true;
        }
        return false;
    }

    private void setDataSources() {
        super.setDataSources(DataSources.QCADMIN_SLAVE);
    }
}

