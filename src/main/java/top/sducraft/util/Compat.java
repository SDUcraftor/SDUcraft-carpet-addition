package top.sducraft.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
//#if MC >= 260100
import net.minecraft.world.item.ItemStackTemplate;
//#endif

import java.util.Set;

/**
 * 跨版本兼容层。
 *
 * <p>把散落在各处的 {@code //#if} 分支收敛到这一个文件里，
 * 其余代码统一调用这里的静态方法，不必再关心版本差异。
 *
 * <p>已有的版本边界（全项目仅这三种）：
 * <ul>
 *     <li>{@code MC >= 260100} —— authlib 的 {@code GameProfile} 由 class 变为 record，
 *         访问器从 {@code getName()} 变为 {@code name()}；{@code Entity.getTags()}
 *         改名为 {@code entityTags()}；{@code HoverEvent.ShowItem} 改为接收
 *         {@code ItemStackTemplate}。</li>
 *     <li>{@code MC >= 260200} —— {@code EntityType} 枚举改名并挪到 {@code EntityTypes}。</li>
 *     <li>{@code MC >= 260300} —— {@code setInvulnerable} 改名为 {@code setPermanentlyInvulnerable}。</li>
 * </ul>
 *
 * <p><b>注意</b>：Mixin 里的 {@code @Shadow} 方法声明不能走本类，
 * 它必须与目标类的方法签名逐字对应，请继续使用行内 {@code //#if}。
 */
public final class Compat {

    private Compat() {
    }

    /**
     * 取游戏档案里的玩家名。
     *
     * <p>1.21.7 的 authlib 里 {@code GameProfile} 是普通类，用 {@code getName()}；
     * 26.1+ 改为 record，访问器变成 {@code name()}。
     */
    public static String gameProfileName(GameProfile profile) {
        //#if MC >= 260100
        return profile.name();
        //#else
        //$$ return profile.getName();
        //#endif
    }

    /**
     * 取实体身上的标签集合。
     *
     * <p>26.1 起 {@code Entity.getTags()} 更名为 {@code entityTags()}。
     */
    public static Set<String> entityTags(Entity entity) {
        //#if MC >= 260100
        return entity.entityTags();
        //#else
        //$$ return entity.getTags();
        //#endif
    }

    /**
     * 构造物品悬浮提示。
     *
     * <p>26.1 起 {@code HoverEvent.ShowItem} 不再直接收 {@code ItemStack}，
     * 需要先转成 {@code ItemStackTemplate}。
     */
    public static HoverEvent itemHover(ItemStack stack) {
        //#if MC >= 260100
        return new HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(stack));
        //#else
        //$$ return new HoverEvent.ShowItem(stack);
        //#endif
    }

    /**
     * 判断实体是否带有指定标签。
     *
     * <p>纯语法糖，避免各处重复写 {@code entityTags(e).contains(...)}。
     */
    public static boolean hasTag(Entity entity, String tag) {
        return entityTags(entity).contains(tag);
    }
}
