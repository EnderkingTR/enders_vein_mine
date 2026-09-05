package enderveinmine.logic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.TagKey;

import java.util.Set;
import java.util.stream.Collectors;

public class BlockUtils {

    // A list of tag paths that we consider valid for grouping blocks together
    private static final Set<String> VALID_GROUPING_TAGS = Set.of(
        "ores", "logs", "leaves", "dirts", "sand", "stones", "cobblestones", 
        "glass_blocks", "glass_panes", "base_stone_overworld", "base_stone_nether",
        "dirt", "logs_that_burn", "planks", "wooden_fences", "wooden_doors",
        "wooden_trapdoors", "wooden_stairs", "wooden_slabs", "terracotta"
    );

    public static boolean isSameBlockGroup(BlockState targetState, BlockState currentState) {
        if (targetState.getBlock() == currentState.getBlock()) return true;

        Set<TagKey<Block>> targetTags = targetState.getTags().collect(Collectors.toSet());
        Set<TagKey<Block>> currentTags = currentState.getTags().collect(Collectors.toSet());

        for (TagKey<Block> tag : targetTags) {
            if (currentTags.contains(tag)) {
                String namespace = tag.location().getNamespace();
                String path = tag.location().getPath();
                
                // Allow grouping if they share a relevant 'c' (conventional) or 'minecraft' tag
                if (namespace.equals("c") || namespace.equals("minecraft")) {
                    String mainPath = path.split("/")[0];
                    if (VALID_GROUPING_TAGS.contains(mainPath) || VALID_GROUPING_TAGS.contains(path)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
