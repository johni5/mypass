package com.del.mypass.utils;

public enum SystemEnv {

    APP_HOME_DIR("app.home.dir"),
    APP_LOG_DIR("app.log.dir");

    private String name;

    SystemEnv(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String read() {
        return System.getProperty(name);
    }


}
