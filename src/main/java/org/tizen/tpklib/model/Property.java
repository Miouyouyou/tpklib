/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.model;

public class Property {
    public String key;
    public String value;

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}

