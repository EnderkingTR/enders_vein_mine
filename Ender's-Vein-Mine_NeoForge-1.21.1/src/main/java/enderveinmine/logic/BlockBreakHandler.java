package enderveinmine.logic;

import enderveinmine.PlayerState;
import enderveinmine.config.VeinMineConfig;
import enderveinmine.network.VeinMineConfigPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BlockBreakHandler {
    
    // Prevent recursive calls during our own block breaking
    private static final ThreadLocal<Boolean> IS_VEIN_MINING = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide() || !(event.getPlayer() instanceof ServerPlayer serverPlayer)) return;
        if (IS_VEIN_MINING.get()) return;

        PlayerState pState = PlayerState.getState(serverPlayer.getUUID());
        if (!pState.active) return;
        
        VeinMineConfigPayload config = pState.config;
        int maxBlocks = config != null ? config.maxBlocks() : VeinMineConfig.INSTANCE.maxBlocks.get();
        boolean damageTool = config != null ? config.damageTool() : VeinMineConfig.INSTANCE.damageTool.get();
        String shapeModeStr = config != null ? config.shapeMode() : VeinMineConfig.INSTANCE.shapeMode.get().name();
        String customShape = config != null ? config.customShape() : VeinMineConfig.INSTANCE.customShape.get();
        boolean directDrop = config != null ? config.directDropToInventory() : VeinMineConfig.INSTANCE.directDropToInventory.get();
        boolean preventToolBreak = config != null ? config.preventToolBreak() : VeinMineConfig.INSTANCE.preventToolBreak.get();
        boolean hungerDrain = config != null ? config.hungerDrain() : VeinMineConfig.INSTANCE.hungerDrain.get();

        if (hungerDrain && serverPlayer.getFoodData().getFoodLevel() <= 0) {
            return;
        }

        IS_VEIN_MINING.set(true);
        try {
            ItemStack tool = serverPlayer.getMainHandItem();
            if (preventToolBreak && damageTool && tool.isDamageableItem() && (tool.getMaxDamage() - tool.getDamageValue() <= 1)) {
                return;
            }

            ServerLevel level = (ServerLevel) event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = event.getState();

            if (breakBlock(level, serverPlayer, pos, state, tool, damageTool, directDrop)) {
                serverPlayer.awardStat(net.minecraft.stats.Stats.BLOCK_MINED.get(state.getBlock()));
                serverPlayer.causeFoodExhaustion(0.005F);
                if (hungerDrain) {
                    serverPlayer.causeFoodExhaustion(0.05F);
                }
                
                if ("SHAPED".equals(shapeModeStr)) {
                    handleShapedMine(level, serverPlayer, pos, state, customShape, maxBlocks, damageTool, directDrop, preventToolBreak, hungerDrain);
                } else if ("SHAPELESS".equals(shapeModeStr)) {
                    handleShapelessMine(level, serverPlayer, pos, state, maxBlocks, damageTool, directDrop, preventToolBreak, hungerDrain);
                } else {
                    handlePresetMine(level, serverPlayer, pos, state, shapeModeStr, maxBlocks, damageTool, directDrop, preventToolBreak, hungerDrain);
                }
                event.setCanceled(true);
            }
        } finally {
            IS_VEIN_MINING.set(false);
        }
    }

    private static void handleShapelessMine(ServerLevel level, ServerPlayer player, BlockPos startPos, BlockState startState, int maxBlocks, boolean damageTool, boolean directDrop, boolean preventToolBreak, boolean hungerDrain) {
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        
        queue.add(startPos);
        visited.add(startPos);
        
        int brokenCount = 1; 
        
        ItemStack tool = player.getMainHandItem();
        boolean checkTool = !tool.isEmpty();

        while (!queue.isEmpty() && brokenCount < maxBlocks) {
            BlockPos current = queue.poll();
            
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        if (brokenCount >= maxBlocks) break;
                        
                        BlockPos offset = current.offset(x, y, z);
                        if (!visited.add(offset)) continue;
                        
                        BlockState state = level.getBlockState(offset);
                        if (BlockUtils.isSameBlockGroup(startState, state)) {
                            if (preventToolBreak && damageTool && tool.isDamageableItem() && (tool.getMaxDamage() - tool.getDamageValue() <= 1)) {
                                return;
                            }
                            if (hungerDrain && player.getFoodData().getFoodLevel() <= 0) {
                                return;
                            }
                            if (breakBlock(level, player, offset, state, tool, damageTool, directDrop)) {
                                if (hungerDrain) {
                                    player.causeFoodExhaustion(0.05F);
                                }
                                brokenCount++;
                                queue.add(offset);
                                if (checkTool && tool.isEmpty()) break;
                            }
                        }
                    }
                    if (brokenCount >= maxBlocks || (checkTool && tool.isEmpty())) break;
                }
                if (brokenCount >= maxBlocks || (checkTool && tool.isEmpty())) break;
            }
        }
    }

    private static void handleShapedMine(ServerLevel level, ServerPlayer player, BlockPos startPos, BlockState startState, String customShape, int maxBlocks, boolean damageTool, boolean directDrop, boolean preventToolBreak, boolean hungerDrain) {
        float maxHardness = startState.getDestroySpeed(level, startPos);
        if (maxHardness < 0) return; 

        int dx = 3, dy = 3, dz = 3;
        try {
            String[] parts = customShape.toLowerCase().split("x");
            if (parts.length == 3) {
                dx = Integer.parseInt(parts[0].trim());
                dy = Integer.parseInt(parts[1].trim());
                dz = Integer.parseInt(parts[2].trim());
            }
        } catch (Exception ignored) {
        }
        
        int hx = dx / 2;
        int hy = dy / 2;
        int hz = dz / 2;
        
        int brokenCount = 1;
        ItemStack tool = player.getMainHandItem();
        boolean checkTool = !tool.isEmpty();

        for (int x = -hx; x <= hx; x++) {
            for (int y = -hy; y <= hy; y++) {
                for (int z = -hz; z <= hz; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; 
                    if (brokenCount >= maxBlocks) return;
                    
                    BlockPos pos = startPos.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    float hardness = state.getDestroySpeed(level, pos);
                    
                    if (hardness >= 0 && hardness <= maxHardness && !state.isAir()) {
                        if (preventToolBreak && damageTool && tool.isDamageableItem() && (tool.getMaxDamage() - tool.getDamageValue() <= 1)) {
                            return;
                        }
                        if (hungerDrain && player.getFoodData().getFoodLevel() <= 0) {
                            return;
                        }
                        if (breakBlock(level, player, pos, state, tool, damageTool, directDrop)) {
                            if (hungerDrain) {
                                player.causeFoodExhaustion(0.05F);
                            }
                            brokenCount++;
                            if (checkTool && tool.isEmpty()) return;
                        }
                    }
                }
            }
        }
    }

    private static void handlePresetMine(ServerLevel level, ServerPlayer player, BlockPos startPos, BlockState startState, String mode, int maxBlocks, boolean damageTool, boolean directDrop, boolean preventToolBreak, boolean hungerDrain) {
        net.minecraft.core.Direction viewDir = net.minecraft.core.Direction.orderedByNearest(player)[0];
        
        int brokenCount = 1;
        ItemStack tool = player.getMainHandItem();
        boolean checkTool = !tool.isEmpty();

        net.minecraft.core.Direction upDir = net.minecraft.core.Direction.UP;
        net.minecraft.core.Direction rightDir; 
        if (viewDir == net.minecraft.core.Direction.UP || viewDir == net.minecraft.core.Direction.DOWN) {
            net.minecraft.core.Direction playerFacing = player.getDirection();
            if (playerFacing.getAxis() == net.minecraft.core.Direction.Axis.Y) {
                playerFacing = net.minecraft.core.Direction.NORTH;
            }
            upDir = playerFacing.getOpposite(); 
            rightDir = playerFacing.getClockWise();
        } else {
            rightDir = viewDir.getClockWise();
        }

        for (int depth = 0; depth < maxBlocks; depth++) {
            if (brokenCount >= maxBlocks) break;

            int minR = 0, maxR = 0;
            int minU = 0, maxU = 0;
            int fOffset = depth;
            int uOffset = 0;
            
            switch (mode) {
                case "TUNNEL_3X3":
                    minR = -1; maxR = 1;
                    minU = -1; maxU = 1;
                    break;
                case "TUNNEL_2X1":
                    minR = 0; maxR = 0; 
                    minU = 0; maxU = 1; 
                    break;
                case "STAIRS_UP":
                    minR = 0; maxR = 0; 
                    minU = 0; maxU = 2; 
                    uOffset = depth; 
                    break;
                case "STAIRS_DOWN":
                    minR = 0; maxR = 0; 
                    minU = 0; maxU = 2; 
                    uOffset = -depth; 
                    break;
                case "TUNNEL_1X1":
                    minR = 0; maxR = 0;
                    minU = 0; maxU = 0;
                    break;
            }

            for (int r = minR; r <= maxR; r++) {
                for (int u = minU; u <= maxU; u++) {
                    if (depth == 0 && r == 0 && u == 0 && uOffset == 0) continue;

                    if (brokenCount >= maxBlocks) return;

                    BlockPos pos = startPos
                        .relative(viewDir, fOffset)
                        .relative(rightDir, r)
                        .relative(upDir, u + uOffset);
                    
                    BlockState state = level.getBlockState(pos);
                    
                    if (BlockUtils.isSameBlockGroup(startState, state)) {
                        if (preventToolBreak && damageTool && tool.isDamageableItem() && (tool.getMaxDamage() - tool.getDamageValue() <= 1)) {
                            return;
                        }
                        if (hungerDrain && player.getFoodData().getFoodLevel() <= 0) {
                            return;
                        }
                        if (breakBlock(level, player, pos, state, tool, damageTool, directDrop)) {
                            if (hungerDrain) {
                                player.causeFoodExhaustion(0.05F);
                            }
                            brokenCount++;
                            if (checkTool && tool.isEmpty()) return;
                        }
                    }
                }
            }
        }
    }

    private static boolean breakBlock(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, ItemStack tool, boolean damageTool, boolean directDrop) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        java.util.List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);
        
        Block block = state.getBlock();
        if (block instanceof net.minecraft.world.level.block.TurtleEggBlock) {
            for (net.minecraft.world.level.block.state.properties.Property<?> prop : state.getProperties()) {
                if (prop.getName().equals("eggs") && prop instanceof net.minecraft.world.level.block.state.properties.IntegerProperty intProp) {
                    int count = state.getValue(intProp);
                    for (ItemStack drop : drops) {
                        if (drop.is(net.minecraft.world.item.Items.TURTLE_EGG)) {
                            drop.setCount(count);
                        }
                    }
                }
            }
        } else if (block instanceof net.minecraft.world.level.block.SeaPickleBlock) {
            for (net.minecraft.world.level.block.state.properties.Property<?> prop : state.getProperties()) {
                if (prop.getName().equals("pickles") && prop instanceof net.minecraft.world.level.block.state.properties.IntegerProperty intProp) {
                    int count = state.getValue(intProp);
                    for (ItemStack drop : drops) {
                        if (drop.is(net.minecraft.world.item.Items.SEA_PICKLE)) {
                            drop.setCount(count);
                        }
                    }
                }
            }
        }
        
        boolean success = level.destroyBlock(pos, false, player);
        if (success) {
            if (directDrop) {
                for (ItemStack drop : drops) {
                    player.getInventory().add(drop);
                    if (!drop.isEmpty()) {
                        player.drop(drop, false);
                    }
                }
            } else {
                for (ItemStack drop : drops) {
                    Block.popResource(level, pos, drop);
                }
            }
            
            ((enderveinmine.mixin.BlockBehaviourAccessor) state.getBlock()).invokeSpawnAfterBreak(state, level, pos, tool, true);
            
            if (directDrop) {
                net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(pos).inflate(1.0);
                java.util.List<net.minecraft.world.entity.ExperienceOrb> orbs = level.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, box);
                for (net.minecraft.world.entity.ExperienceOrb orb : orbs) {
                    if (orb.tickCount <= 1) {
                        orb.setPos(player.getX(), player.getY(), player.getZ());
                    }
                }
            }
            
            if (damageTool && tool.isDamageableItem()) {
                tool.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
        }
        return success;
    }
}
