package top.sducraft.helpers.rule.CreeperLogHelper;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CreeperLogHelper {
    public record CreeperLogEntry(long gameTime, String PlayerName, Vec3 creeperPosition,@Nullable Vec3 targetPlayerPosition) { }

    public record CreeperDamageLogEntry(long gameTime, String PlayerName, Vec3 creeperPosition, Vec3 damageSourcePosition) { }
}
