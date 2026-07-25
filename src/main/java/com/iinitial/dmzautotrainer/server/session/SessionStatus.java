package com.iinitial.dmzautotrainer.server.session;

public record SessionStatus (
        boolean allowed,
        long sessionSecondsRemaining,
        long cooldownSecondsRemaining
) { }