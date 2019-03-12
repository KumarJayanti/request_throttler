package com.spiritsoft.throttle.model;

import java.util.ArrayList;
import java.util.List;

/**
 * To be efficient this should be a Splay Tree so most recently used entries are on top ?
 * For now its a simple tree implementation.
 */
public class ConfigurationTree {

    private List<ConfigurationTree> children = new ArrayList<>();
    private String name;
    private int value;

    public ConfigurationTree(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public ConfigurationTree addChild(String name, int value) {
        ConfigurationTree t = new ConfigurationTree(name, value);
        children.add(t);
        return t;
    }

    public List<ConfigurationTree> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }


    public int getValue() {
        return value;
    }
}

