package top.sducraft.helpers.rule.chunkLoadHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import java.util.Comparator;

public class RegistTicket {
    public static final TicketType<ChunkPos> FAKE_PEACE_TICKET_TYPE =TicketType.create("fakepeace", Comparator.comparingLong(ChunkPos::toLong),100);
    public static final TicketType<ChunkPos> ENDER_PEARL = TicketType.create("ender_pearl", Comparator.comparingLong(ChunkPos::toLong), 40);

    public static void addfakepeaceticket(ServerLevel level,ChunkPos pos) {
        level.getChunkSource().addRegionTicket(FAKE_PEACE_TICKET_TYPE,pos,3,pos);
        level.resetEmptyTime();
    }
}
