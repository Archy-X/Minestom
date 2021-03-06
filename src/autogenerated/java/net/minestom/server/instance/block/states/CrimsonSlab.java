package net.minestom.server.instance.block.states;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;

/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(
        since = "forever",
        forRemoval = false
)
public final class CrimsonSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15055, "type=top", "waterlogged=true"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15056, "type=top", "waterlogged=false"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15057, "type=bottom", "waterlogged=true"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15058, "type=bottom", "waterlogged=false"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15059, "type=double", "waterlogged=true"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15060, "type=double", "waterlogged=false"));
    }
}
