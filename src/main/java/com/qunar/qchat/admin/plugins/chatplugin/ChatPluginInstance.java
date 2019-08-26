package com.qunar.qchat.admin.plugins.chatplugin;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.plugins.chatplugin.impls.QChatPluginImpl;
import com.qunar.qchat.admin.plugins.chatplugin.impls.QTalkPluginImpl;

import java.util.Map;

public final class ChatPluginInstance {
    private static ChatPluginInstance instance;
    private Map<String,IChatPlugin> plgins;
    private final Object lockObject = new Object();


    public synchronized static ChatPluginInstance getInstance(){
        if (null == instance)
            instance = new ChatPluginInstance();
        return instance;
    }

    private ChatPluginInstance() {
        plgins = Maps.newHashMap();
    }

    public IChatPlugin getChatPlugin(String domainhost){
        if (Strings.isNullOrEmpty(domainhost))
            return null;

        if (plgins.containsKey(domainhost))
            return plgins.get(domainhost);

        synchronized (lockObject) {
            IChatPlugin plugin = null;
            do {
                if (QChatConstant.QTALK_HOST.equalsIgnoreCase(domainhost)){
                    plugin = new QTalkPluginImpl();
                    break;
                }

                if (QChatConstant.QCHAR_HOST.equalsIgnoreCase(domainhost)){
                    plugin = new QChatPluginImpl();
                    break;
                }
            } while (false);
            if (null != plugin){
                plgins.put(domainhost,plugin);
                return plugin;
            }
        }
        return null;
    }

}
