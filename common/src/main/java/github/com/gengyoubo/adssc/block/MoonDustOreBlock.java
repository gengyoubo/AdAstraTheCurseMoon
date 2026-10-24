package github.com.gengyoubo.adssc.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Moon dust ore remembers where it was generated, so its yield follows the ore rather than the player.
 * <p>
 * {@link #ORIGIN} is 0 for a block placed from a plain item, 1-3 for the outer, middle and inner Curse Satellites
 * and 4 for the Curse Moon. Worldgen writes the tier, and the loot table copies it onto silk-touched drops, so a
 * natural ore keeps its tier when it is mined with silk touch and placed again. The drop amounts per tier live in
 * {@code data/ad_ssc/loot_tables/blocks/moon_dust_ore.json}.
 */
public class MoonDustOreBlock extends DropExperienceBlock {
    public static final int MAX_ORIGIN = 4;
    public static final IntegerProperty ORIGIN = IntegerProperty.create("origin", 0, MAX_ORIGIN);

    public MoonDustOreBlock(Properties properties) {
        super(properties, UniformInt.of(2, 5));
        registerDefaultState(stateDefinition.any().setValue(ORIGIN, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIGIN);
    }
}
