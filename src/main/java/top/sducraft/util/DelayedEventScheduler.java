package top.sducraft.util;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DelayedEventScheduler {
    private static final List<ScheduledEvent> events = new ArrayList<>();

    private static class ScheduledEvent {
        int ticksRemaining;
        Runnable action;

        ScheduledEvent(int ticks, Runnable action) {
            this.ticksRemaining = ticks;
            this.action = action;
        }
    }

    public static void addScheduleEvent(int delayTicks, Runnable action) {
        events.add(new ScheduledEvent(delayTicks, action));
    }

    public static void tick(MinecraftServer server) {
        Iterator<ScheduledEvent> iter = events.iterator();
        while (iter.hasNext()) {
            ScheduledEvent e = iter.next();
            e.ticksRemaining--;
            if (e.ticksRemaining <= 0) {
                try {
                    e.action.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                iter.remove();
            }
        }
    }
}


