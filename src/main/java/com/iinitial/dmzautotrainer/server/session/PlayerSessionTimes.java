package com.iinitial.dmzautotrainer.server.session;

public record PlayerSessionTimes(
        long sessionGrantedAt,
        long sessionEndsAt,
        long cooldownEndsAt
) { }
