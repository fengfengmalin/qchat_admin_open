package com.qunar.qchat.admin.service.third.impl.modules;

import java.util.List;

public class RbtSuggestionListJson {
    public String content;
    public String listTips;
    public ListArea listArea;
    public List<Item> hints;

    public static class ListArea {
        public String type;
        public ListAreaStyle style;
        public List<Item> items;
    }

    public static class ListAreaStyle {
        public int defSize ;
    }

    public static class Item{
        public String text;
        public ItemEvent event;
    }

    public static class ItemEvent{
        public String url;
        public String type;
        public String msgText;
    }
}