package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.common.config.TrainingConfig;
import com.dragonminez.common.training.GravityLogic;
import com.dragonminez.common.training.MinigameEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GravityAutomation {
    static final int HORIZON = 70;
    private static final double[] MARGINS = {2.0, 1.0, 0.0};

    public static void tick(GravityLogic gravity, InputSink sink) {
        if (gravity.isFinished()) return;

        if (decide(gravity)) sink.tap(MinigameEvent.ACTION, 0f);
    }

    static boolean decide(GravityLogic gravity) {
        boolean bestFlap = false;
        int bestDepth = -1;
        for (double margin : MARGINS) {
            Planner planner = new Planner(gravity, margin);
            planner.plan();
            if (planner.depth >= HORIZON) return planner.flap;
            if (planner.depth > bestDepth) {
                bestDepth = planner.depth;
                bestFlap = planner.flap;
            }
        }
        return bestFlap;
    }

    private static final class Planner {
        private final double gravity;
        private final double flapVelocity;
        private final double maxFall;
        private final double scroll;
        private final double groundTop = GravityLogic.VIEW_HEIGHT - GravityLogic.GROUND_HEIGHT;
        private final double margin;
        private final double startY;
        private final double startVelocity;
        private final double startPodWorldX;
        private final List<GravityLogic.Pillar> pillars = new ArrayList<>();
        private final Map<Integer, Integer> memo = new HashMap<>();

        boolean flap;
        int depth;

        Planner(GravityLogic logic, double margin) {
            TrainingConfig.GravityConfig cfg = logic.cfg();
            this.gravity = cfg.getGravityAcceleration();
            this.flapVelocity = cfg.getFlapVelocity();
            this.maxFall = cfg.getMaxFallSpeed();
            this.scroll = logic.scrollSpeed();
            this.margin = margin;
            this.startY = logic.podY();
            this.startVelocity = logic.velocity();
            this.startPodWorldX = logic.worldX() + GravityLogic.PLAYER_X;

            double from = startPodWorldX - 60;
            double to = startPodWorldX + HORIZON * scroll + 60;
            for (GravityLogic.Pillar pillar : logic.pillars()) {
                if (!pillar.hit && pillar.x >= from && pillar.x <= to) pillars.add(pillar);
            }
        }

        void plan() {
            boolean flapFirst = prefersFlap(0, startY, startVelocity);
            int first = reached(0, startY, startVelocity, flapFirst);
            int second = reached(0, startY, startVelocity, !flapFirst);
            if (second > first) {
                flap = !flapFirst;
                depth = second;
            } else {
                flap = flapFirst;
                depth = first;
            }
        }

        private int reached(int step, double y, double velocity, boolean doFlap) {
            double v = doFlap ? -flapVelocity : velocity;
            v = Math.min(maxFall, v + gravity);
            y += v;
            if (y < GravityLogic.POD_HITBOX_RADIUS) {
                y = GravityLogic.POD_HITBOX_RADIUS;
                v = Math.max(0, v);
            }
            if (y + GravityLogic.POD_HITBOX_RADIUS > groundTop) return step;
            if (collides(startPodWorldX + (step + 1) * scroll, y)) return step;
            return search(step + 1, y, v);
        }

        private int search(int step, double y, double velocity) {
            if (step >= HORIZON) return HORIZON;

            int key = (step << 18) | (Math.min(1023, (int) Math.round(y * 4)) << 8) | Math.max(0, Math.min(255, (int) Math.round(velocity * 10) + 128));
            Integer known = memo.get(key);
            if (known != null) return known;

            boolean flapFirst = prefersFlap(step, y, velocity);
            int best = reached(step, y, velocity, flapFirst);
            if (best < HORIZON) best = Math.max(best, reached(step, y, velocity, !flapFirst));
            memo.put(key, best);
            return best;
        }

        private boolean prefersFlap(int step, double y, double velocity) {
            double podWorldX = startPodWorldX + step * scroll;
            GravityLogic.Pillar next = null;
            double reach = GravityLogic.CAP_WIDTH / 2.0 + GravityLogic.POD_HITBOX_RADIUS + 1;
            for (GravityLogic.Pillar pillar : pillars) {
                if (pillar.x + reach < podWorldX) continue;
                if (next == null || pillar.x < next.x) next = pillar;
            }
            double target = next == null ? groundTop / 2.0 : next.gapCenter;
            double predicted = y + Math.min(maxFall, velocity + gravity);
            return predicted > target + 10;
        }

        private boolean collides(double podWorldX, double y) {
            for (GravityLogic.Pillar pillar : pillars) {
                if (Math.abs(pillar.x - podWorldX) > 60) continue;
                if (GravityLogic.collides(pillar, podWorldX, y)) return true;
                if (margin > 0 && (GravityLogic.collides(pillar, podWorldX, y - margin) || GravityLogic.collides(pillar, podWorldX, y + margin))) return true;
            }
            return false;
        }
    }
}