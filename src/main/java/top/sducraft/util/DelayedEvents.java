package top.sducraft.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DelayedEvents {
    public static final TickEventHandler START_SERVER_TICK = new TickEventHandler();
    public static final TickEventHandler END_SERVER_TICK = new TickEventHandler();

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(START_SERVER_TICK::tick);
        ServerTickEvents.END_SERVER_TICK.register(END_SERVER_TICK::tick);
    }

    public static class TickEventHandler {
        private final List<ScheduledEvent> events = new ArrayList<>();

        public void register(int delayTicks, ScheduledServerCallback action) {
            events.add(new ScheduledEvent(delayTicks, action));
        }

        private void tick(MinecraftServer server) {
            List<ScheduledEvent> snapshot = new ArrayList<>(events);
            List<ScheduledEvent> toRemove = new ArrayList<>();

            for (ScheduledEvent e : snapshot) {
                e.ticksRemaining--;
                if (e.ticksRemaining <= 0) {
                    try {
                        e.action.run(server);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    toRemove.add(e);
                }
            }
            events.removeAll(toRemove);
        }
    }

    private static class ScheduledEvent {
        int ticksRemaining;
        ScheduledServerCallback action;

        ScheduledEvent(int ticks, ScheduledServerCallback action) {
            this.ticksRemaining = ticks;
            this.action = action;
        }
    }

    @FunctionalInterface
    public interface ScheduledServerCallback {
        void run(MinecraftServer server) throws URISyntaxException, IOException;
    }
}


