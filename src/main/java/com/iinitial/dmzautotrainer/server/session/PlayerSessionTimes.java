package com.iinitial.dmzautotrainer.server.session;

public record PlayerSessionTimes(
        long sessionEndsAt,
        long cooldownEndsAt
) { }
