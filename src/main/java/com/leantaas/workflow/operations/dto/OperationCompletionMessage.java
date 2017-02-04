package com.leantaas.workflow.operations.dto;

import com.amazonaws.util.StringUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class OperationCompletionMessage implements Map<String, String> {
    private HashMap<String, String> wrappedHashMap = new HashMap<>();

    public OperationCompletionMessage(String operationName) {
        if (StringUtils.isNullOrEmpty(operationName)) {
            throw new IllegalArgumentException("operation name cannot be null or empty");
        }
        wrappedHashMap.put("operationName", operationName);
    }

    @Override
    public int size() {
        return wrappedHashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return wrappedHashMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        String keyStr = (String) key;
        return wrappedHashMap.containsKey(keyStr) ;
    }

    @Override
    public boolean containsValue(Object value) {
        String valueStr = (String) value;
        return wrappedHashMap.containsValue(valueStr);
    }

    @Override
    public String get(Object key) {
        return wrappedHashMap.get(key);
    }

    @Override
    public String put(String key, String value) {
        return wrappedHashMap.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return wrappedHashMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        wrappedHashMap.putAll(m);
    }

    @Override
    public void clear() {
        wrappedHashMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return wrappedHashMap.keySet();
    }

    @Override
    public Collection<String> values() {
        return wrappedHashMap.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return wrappedHashMap.entrySet();
    }
}
