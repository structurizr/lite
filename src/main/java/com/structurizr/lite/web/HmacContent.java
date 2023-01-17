package com.structurizr.lite.web;

class HmacContent {

    private String[] strings;

    public HmacContent(String... strings) {
        this.strings = strings;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String string : strings) {
            buf.append(string);
            buf.append("\n");
        }

        return buf.toString();
    }

}
