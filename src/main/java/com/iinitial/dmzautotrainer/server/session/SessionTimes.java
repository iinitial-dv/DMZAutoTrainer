package com.iinitial.dmzautotrainer.server.session;

public record SessionTimes (
        long sessionEndsAt,
        long cooldownEndsAt
) { }
