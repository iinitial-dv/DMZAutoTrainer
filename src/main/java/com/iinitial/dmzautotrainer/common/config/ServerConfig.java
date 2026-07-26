package com.iinitial.dmzautotrainer.common.config;

public class ServerConfig {
    public boolean enableSessions = false;
    public int sessionDuration = 900;
    public int sessionCooldown = 900;

    public boolean isSessionsEnabled() { return enableSessions; }

    public int getSessionDuration() { return sessionDuration; }

    public int getSessionCooldown() { return sessionCooldown; }
}
