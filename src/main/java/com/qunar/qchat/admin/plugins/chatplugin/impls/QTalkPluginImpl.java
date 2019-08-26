package com.qunar.qchat.admin.plugins.chatplugin.impls;


import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.annotation.routingdatasource.DataSources;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.model.responce.BaseResponce;
import com.qunar.qchat.admin.plugins.chatplugin.BaseChatPlugin;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.EjabdUtil;
import com.qunar.qchat.admin.util.HttpClientUtils;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.SeatOnlineState;
import com.qunar.qchat.admin.vo.third.QTalkStatus;
import com.qunar.qchat.admin.vo.third.QTalkStatusRet;
import com.qunar.qchat.admin.vo.third.QTalkStatusSearchParam;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;

import java.util.List;
import java.util.Map;

public class QTalkPluginImpl extends BaseChatPlugin {


    public QTalkPluginImpl() {
        setDataSources();
    }

    private List<SeatOnlineState> convertToSeatStatusList(QTalkStatusRet qTalkStatusRet) {
        if (qTalkStatusRet.getData() != null && qTalkStatusRet.getData().length < 1) {
            return null;
        }
        List<SeatOnlineState> seatOnlineStates = Lists.newArrayList();
        for (QTalkStatus qTalkStatus : qTalkStatusRet.getData()) {
            if (!StringUtils.equals(qTalkStatus.getDomain(), QChatConstant.QTALK_HOST)) {
                continue;
            }
            List<Map<String, String>> ul = qTalkStatus.getUl();
            if (ul == null || ul.size() < 1) {
                continue;
            }
            for (Map<String, String> map : ul) {
                if (MapUtils.isEmpty(map)) {
                    continue;
                }

                SeatOnlineState seatOnlineState = new SeatOnlineState();
                seatOnlineState.setStrId(EjabdUtil.makeSureUserJid(map.get("u"),QChatConstant.QTALK_HOST));
                seatOnlineState.setOnlineState(OnlineState.of(map.get("o").toLowerCase()));
                seatOnlineStates.add(seatOnlineState);
            }
        }
        return seatOnlineStates;
    }
    @Override
    public List<SeatOnlineState> getUsersOnlineStatus(List<String> strIdList) {
        if (CollectionUtil.isEmpty(strIdList)) {
            return null;
        }

        List<String> userids = Lists.transform(strIdList, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return EjabdUtil.makeSureUserid(s);
            }
        });

        assignSeatAppender.info("查询qtalk状态：");
        try {
            List<QTalkStatusSearchParam> params = Lists.newArrayList();
            params.add(QTalkStatusSearchParam.builder().domain(QChatConstant.QTALK_HOST).users(userids).build());
            String response = HttpClientUtils.httpsPostJson("", JacksonUtil.obj2String(params));

            if (!Strings.isNullOrEmpty(response)) {
                QTalkStatusRet qTalkStatusRet = JacksonUtil.string2Obj(response, QTalkStatusRet.class);
                if (qTalkStatusRet != null && qTalkStatusRet.isRet()) {
                    return convertToSeatStatusList(qTalkStatusRet);
                }
            }
        } catch (Exception e) {
            assignSeatAppender.error("getUsersOnlineStatus", e);
        }
        return null;
    }

    @Override
    public BusiReturnResult checkUserExist(String p) {
        BusiReturnResult result = new BusiReturnResult();
        result.setRet(true);
        result.setCode(BusiResponseCodeEnum.SUCCESS.getCode());

        Map<String, Object> map = Maps.newHashMap();
        map.put("userName", EjabdUtil.makeSureUserJid(p,QChatConstant.QTALK_HOST));
        result.setData(map);
        return result;
    }
    @Override
    public boolean sendThirdMessage(String from, String to, String message) {
        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to))
            return false;
        from = EjabdUtil.makeSureUserJid(from,QChatConstant.QTALK_HOST);
        to = EjabdUtil.makeSureUserJid(to,QChatConstant.QTALK_HOST);


        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, from);
        param.put(QChatConstant.Note.TO, to);
        param.put(QChatConstant.Note.MESSAGE,message);
        param.put("system", "vs_qchat_admin");

        String response = null;

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

        from = EjabdUtil.makeSureUserJid(from,QChatConstant.QTALK_HOST);
        to = EjabdUtil.makeSureUserJid(to,QChatConstant.QTALK_HOST);

        Map<String,String> precenceParam = Maps.newHashMap();
        precenceParam.put("from",from);
        precenceParam.put("to",to);
        precenceParam.put("category","99");
        precenceParam.put("data",body);

        String response = null;



        if (Strings.isNullOrEmpty(response)){
            BaseResponce responce = JacksonUtil.string2Obj(response,BaseResponce.class);
            if (null!=responce && responce.isRet())
                return true;
        }
        return false;
    }


    private void setDataSources() {
        super.setDataSources(DataSources.QCADMIN_SLAVE);
    }
}
