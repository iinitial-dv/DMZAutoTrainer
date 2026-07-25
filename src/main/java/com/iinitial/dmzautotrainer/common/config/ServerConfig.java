package com.iinitial.dmzautotrainer.common.config;

public class ServerConfig {
    public static boolean enableSessions = false;
    // durations in seconds
    public static int     sessionDuration = 900;
    public static int     sessionCooldown = 900;

    public static boolean getSessionsEnabled () { return enableSessions; }

    public static int     getSessionDuration () { return sessionDuration; }

    public static int     getSessionCooldown () { return sessionCooldown; }
}
