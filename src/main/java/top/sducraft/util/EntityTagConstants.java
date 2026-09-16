package top.sducraft.util;

/**
 * 视觉化实体的标签常量。
 *
 * <p>这些字符串会被写到 {@code Display} 实体的 scoreboard tag 上，
 * 并在清理时按标签检索实体。原先散落在各个使用点，
 * 任意一处拼写不一致都会导致“生成得出来、清理不掉”，故集中到这里。
 *
 * <p>改动这些值等于改变已经落地的世界数据语义，<b>不要随意更名</b>：
 * 存档里可能已存在带旧标签的实体。
 */
public final class EntityTagConstants {

    private EntityTagConstants() {
    }

    /** /finditem 高亮方块用的临时展示实体。 */
    public static final String FINDITEM_HIGHLIGHT = "finditem_highlight";

    /** /allitem search 生成、600 tick 后自动消失的展示实体。 */
    public static final String ALLITEM_DISPLAY = "allitem_display";

    /** /allitem debug 生成的常驻展示实体。 */
    public static final String ALLITEM_DEBUG = "allitem_debug";

    /** litematica 材料清单生成的红/绿方块展示实体。 */
    public static final String MATERIAL = "material";

    /** 漏斗冷却可视化生成的文字展示实体。 */
    public static final String HOPPER_COOLDOWN_VISUALIZER = "hopperCooldownVisualizer";

    /** 苦力怕爆炸日志可视化，原定义在 CreeperLogVisualizer 内部。 */
    public static final String CREEPER_LOG_VISUALIZATION = "creeper_log_visualization";

    /** 标记实体不写入存档，由 DisableVisualizerEntitySave 消费。 */
    public static final String NO_SAVE = "noSave";
}
