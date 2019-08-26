package com.qunar.qtalk.ss.utils.common.database;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class PropertiesConfig {

    protected Properties properties;
    private ConcurrentHashMap<String, List<ValueChangedHandler>> changedMapping = new ConcurrentHashMap<>();
    private List<ValueChangedHandler> handlers = new ArrayList<>();
    public AsyncEventBus eventBus = new AsyncEventBus(PropertiesConfigManager.class.getName(), Executors.newFixedThreadPool(1));

    private static final Logger logger = LoggerFactory.getLogger(PropertiesConfig.class);

    protected PropertiesConfig(InputStream inputStream) throws IOException {
        initConfig(inputStream);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Enumeration<?> propertyNames() {
        return properties.propertyNames();
    }

    protected void initConfig(InputStream inputStream) throws IOException {
        properties = new Properties();
        properties.load(inputStream);
    }

    public static InputStream loadFile(String fileName) throws FileNotFoundException {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("load " + fileName + " error,file not found");
        }

        String originFile = fileName;

        if (!fileName.startsWith("/")) {
            fileName = "/" + fileName;
        }

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);

        if (inputStream == null) {
            //
            // for local debug
            File fs = new File(originFile);
            String fsPath = fs.getAbsolutePath();

            inputStream = new FileInputStream(fsPath);
        }

        return inputStream;
    }

    public PropertiesConfig(String file) throws IOException {

        InputStream inputStream = loadFile(file);
        initConfig(inputStream);
    }

    public void bindProperties(String key, ValueChangedHandler handler) {

        List<ValueChangedHandler> handlers = changedMapping.get(key);
        if (handlers == null) {
            handlers = new ArrayList<>();
        }

        if (!handlers.contains(handler))
            handlers.add(handler);

        changedMapping.put(key, handlers);

        fireChange(properties, key, handler);
    }

    private void fireChange(Properties properties, String key, ValueChangedHandler handler) {
        String oldValue = properties.getProperty(key);
        handler.valueChanged(oldValue);
    }

    public void commitChange(String key, String value) {
        List<ValueChangedHandler> methods = changedMapping.get(key);

        if (methods != null) {
            for (ValueChangedHandler handler : methods) {
                boolean result = handler.valueChanged(value);
                if (!result) {
                    logger.info("update failed.");
                }
            }
        }
        properties.setProperty(key, value);
    }

    private void fireChanges(Properties properties) {
        if (this.properties == properties) {
            //
            // 全量更新
            for (String key : properties.stringPropertyNames()) {
                String oldValue = properties.getProperty(key);
                commitChange(key, oldValue);
            }
        } else {


        }
    }

    public void bindProperties(String key, String defaultValue, ValueChangedHandler handler) {

        if (!handlers.contains(handler))
            handlers.add(handler);
    }

    private void changeConfig(String key, String newValue) {
        String oldValue = properties.getProperty(key);
        if ((StringUtils.isNotEmpty(oldValue) && (!oldValue.equalsIgnoreCase(newValue)))
                || (oldValue == null && newValue != null)
                || (newValue == null && oldValue != null)) {
            commitChange(key, newValue);
        }
    }

    public static Properties loadHotFile(String file) throws IOException {
        Properties p = new Properties();
        p.load(loadFile(file));
        return p;
    }

    public static void main(String[] args) {
        PropertiesConfig config = null;

        try {
            config = new PropertiesConfig("pgsql.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        config.changeConfig("qchat.master.url", "asdfffflknt");

        AtomicReference<String> oldValue = new AtomicReference<>("");


        config.bindProperties("qchat.master.url", newValue -> {
            oldValue.set(newValue);
            return true;
        });

        config.changeConfig("qchat.master.url", "abc111");


        config.changeConfig("asdf", "sadflihn4r5");


    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
