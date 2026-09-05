package enderveinmine.logic;

import enderveinmine.PlayerState;
import enderveinmine.config.VeinMineConfig;
import enderveinmine.network.VeinMineConfigPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.Event;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BlockInteractHandler {

    private static final ThreadLocal<Boolean> IS_VEIN_INTERACTING = ThreadLocal.withInitial(() -> false);

    private static boolean isHarvestableCrop(BlockState state, Level level, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof net.minecraft.world.level.block.CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block instanceof net.minecraft.world.level.block.NetherWartBlock) {
            return state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE) >= 3;
        }
        if (block instanceof net.minecraft.world.level.block.CocoaBlock) {
            return state.getValue(net.minecraft.world.level.block.CocoaBlock.AGE) >= 2;
        }
        if (block instanceof net.minecraft.world.level.block.SugarCaneBlock
                || block instanceof net.minecraft.world.level.block.CactusBlock) {
            BlockState stateBelow = level.getBlockState(pos.below());
            return stateBelow.getBlock() == block;
        }
        return false;
    }

    private static boolean harvestCrop(ServerPlayer player, Level level, BlockPos pos, BlockState state,
            ItemStack tool) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel))
            return false;
        Block block = state.getBlock();

        PlayerState pState = PlayerState.getState(player.getUUID());
        boolean directDrop = false;
        if (pState != null && pState.config != null) {
            directDrop = pState.config.directDropToInventory();
        } else {
            directDrop = VeinMineConfig.INSTANCE.directDropToInventory.get();
        }

        if (block instanceof net.minecraft.world.level.block.CropBlock crop) {
            java.util.List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, null, player, tool);
            boolean seedRemoved = false;
            for (ItemStack drop : drops) {
                if (!seedRemoved && drop.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                        && blockItem.getBlock() == crop) {
                    drop.shrink(1);
                    seedRemoved = true;
                }
                if (!drop.isEmpty()) {
                    if (directDrop) {
                        player.getInventory().add(drop);
                        if (!drop.isEmpty()) {
                            player.drop(drop, false);
                        }
                    } else {
                        Block.popResource(level, pos, drop);
                    }
                }
            }
            level.setBlockAndUpdate(pos, crop.getStateForAge(0));
            return true;
        }

        if (block instanceof net.minecraft.world.level.block.NetherWartBlock) {
            java.util.List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, null, player, tool);
            boolean seedRemoved = false;
            for (ItemStack drop : drops) {
                if (!seedRemoved && drop.is(net.minecraft.world.item.Items.NETHER_WART)) {
                    drop.shrink(1);
                    seedRemoved = true;
                }
                if (!drop.isEmpty()) {
                    if (directDrop) {
                        player.getInventory().add(drop);
                        if (!drop.isEmpty()) {
                            player.drop(drop, false);
                        }
                    } else {
                        Block.popResource(level, pos, drop);
                    }
                }
            }
            level.setBlockAndUpdate(pos, state.setValue(net.minecraft.world.level.block.NetherWartBlock.AGE, 0));
            return true;
        }

        if (block instanceof net.minecraft.world.level.block.CocoaBlock) {
            java.util.List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, null, player, tool);
            boolean seedRemoved = false;
            for (ItemStack drop : drops) {
                if (!seedRemoved && drop.is(net.minecraft.world.item.Items.COCOA_BEANS)) {
                    drop.shrink(1);
                    seedRemoved = true;
                }
                if (!drop.isEmpty()) {
                    if (directDrop) {
                        player.getInventory().add(drop);
                        if (!drop.isEmpty()) {
                            player.drop(drop, false);
                        }
                    } else {
                        Block.popResource(level, pos, drop);
                    }
                }
            }
            level.setBlockAndUpdate(pos, state.setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 0));
            return true;
        }

        if (block instanceof net.minecraft.world.level.block.SugarCaneBlock
                || block instanceof net.minecraft.world.level.block.CactusBlock) {
            java.util.List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, null, player, tool);
            for (ItemStack drop : drops) {
                if (directDrop) {
                    player.getInventory().add(drop);
                    if (!drop.isEmpty()) {
                        player.drop(drop, false);
                    }
                } else {
                    Block.popResource(level, pos, drop);
                }
            }
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            return true;
        }

        return false;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer serverPlayer))
            return;
        if (IS_VEIN_INTERACTING.get())
            return;

        PlayerState pState = PlayerState.getState(serverPlayer.getUUID());
        if (!pState.active)
            return;

        InteractionHand hand = event.getHand();
        ItemStack tool = serverPlayer.getItemInHand(hand);

        BlockPos startPos = event.getPos();
        Level level = event.getLevel();
        BlockState startState = level.getBlockState(startPos);
        BlockHitResult hitResult = event.getHitVec();

        IS_VEIN_INTERACTING.set(true);
        try {
            boolean isCrop = isHarvestableCrop(startState, level, startPos);
            InteractionResult initialResult = InteractionResult.PASS;
            boolean consumed = false;

            if (isCrop) {
                if (harvestCrop(serverPlayer, level, startPos, startState, tool)) {
                    consumed = true;
                    initialResult = InteractionResult.SUCCESS;
                }
            } else {
                if (tool.isEmpty())
                    return;
                UseOnContext context = new UseOnContext(serverPlayer, hand, hitResult);
                initialResult = tool.useOn(context);
                consumed = initialResult.consumesAction();
            }

            if (consumed) {
                VeinMineConfigPayload config = pState.config;
                int maxBlocks = config != null ? config.maxBlocks() : VeinMineConfig.INSTANCE.maxBlocks.get();
                String shapeModeStr = config != null ? config.shapeMode() : VeinMineConfig.INSTANCE.shapeMode.get().name();
                String customShape = config != null ? config.customShape() : VeinMineConfig.INSTANCE.customShape.get();

                if ("SHAPED".equals(shapeModeStr)) {
                    handleShapedInteract(serverPlayer, level, hand, hitResult, startState, customShape, maxBlocks);
                } else if ("SHAPELESS".equals(shapeModeStr)) {
                    handleShapelessInteract(serverPlayer, level, hand, hitResult, startState, maxBlocks);
                } else {
                    handlePresetInteract(serverPlayer, level, hand, hitResult, startState, shapeModeStr, maxBlocks);
                }

                event.setCanceled(true);
                event.setCancellationResult(initialResult);
            }
        } finally {
            IS_VEIN_INTERACTING.set(false);
        }
    }

    private static boolean interactBlock(ServerPlayer player, Level level, InteractionHand hand,
            BlockHitResult hitResult, BlockPos offset, BlockState targetState) {
        BlockState state = level.getBlockState(offset);
        if (BlockUtils.isSameBlockGroup(targetState, state)) {
            if (isHarvestableCrop(state, level, offset)) {
                return harvestCrop(player, level, offset, state, player.getItemInHand(hand));
            }

            ItemStack tool = player.getItemInHand(hand);
            if (tool.isEmpty())
                return false;

            BlockHitResult offsetHit = new BlockHitResult(
                    hitResult.getLocation().add(offset.getX() - hitResult.getBlockPos().getX(),
                            offset.getY() - hitResult.getBlockPos().getY(),
                            offset.getZ() - hitResult.getBlockPos().getZ()),
                    hitResult.getDirection(),
                    offset,
                    hitResult.isInside());
            UseOnContext offsetContext = new UseOnContext(player, hand, offsetHit);
            InteractionResult result = tool.useOn(offsetContext);
            return result.consumesAction();
        }
        return false;
    }

    private static void handleShapelessInteract(ServerPlayer player, Level level, InteractionHand hand,
            BlockHitResult hitResult, BlockState targetState, int maxBlocks) {
        BlockPos startPos = hitResult.getBlockPos();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        int interactedCount = 1;
        ItemStack tool = player.getItemInHand(hand);
        boolean checkTool = !tool.isEmpty();

        while (!queue.isEmpty() && interactedCount < maxBlocks) {
            BlockPos current = queue.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0)
                            continue;
                        if (interactedCount >= maxBlocks)
                            return;

                        BlockPos offset = current.offset(x, y, z);
                        if (!visited.add(offset))
                            continue;

                        if (interactBlock(player, level, hand, hitResult, offset, targetState)) {
                            interactedCount++;
                            queue.add(offset);
                            if (checkTool && tool.isEmpty())
                                return;
                        }
                    }
                }
            }
        }
    }

    private static void handleShapedInteract(ServerPlayer player, Level level, InteractionHand hand,
            BlockHitResult hitResult, BlockState targetState, String customShape, int maxBlocks) {
        BlockPos startPos = hitResult.getBlockPos();
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

        int interactedCount = 1;
        ItemStack tool = player.getItemInHand(hand);
        boolean checkTool = !tool.isEmpty();

        for (int x = -hx; x <= hx; x++) {
            for (int y = -hy; y <= hy; y++) {
                for (int z = -hz; z <= hz; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    if (interactedCount >= maxBlocks)
                        return;

                    BlockPos offset = startPos.offset(x, y, z);

                    if (interactBlock(player, level, hand, hitResult, offset, targetState)) {
                        interactedCount++;
                        if (checkTool && tool.isEmpty())
                            return;
                    }
                }
            }
        }
    }

    private static void handlePresetInteract(ServerPlayer player, Level level, InteractionHand hand,
            BlockHitResult hitResult, BlockState targetState, String mode, int maxBlocks) {
        BlockPos startPos = hitResult.getBlockPos();
        net.minecraft.core.Direction viewDir = net.minecraft.core.Direction.orderedByNearest(player)[0];

        int interactedCount = 1;
        ItemStack tool = player.getItemInHand(hand);
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
            if (interactedCount >= maxBlocks)
                break;

            int minR = 0, maxR = 0;
            int minU = 0, maxU = 0;
            int fOffset = depth;
            int uOffset = 0;

            switch (mode) {
                case "TUNNEL_3X3":
                    minR = -1;
                    maxR = 1;
                    minU = -1;
                    maxU = 1;
                    break;
                case "TUNNEL_2X1":
                    minR = 0;
                    maxR = 0;
                    minU = 0;
                    maxU = 1;
                    break;
                case "STAIRS_UP":
                    minR = 0;
                    maxR = 0;
                    minU = 0;
                    maxU = 2;
                    uOffset = depth;
                    break;
                case "STAIRS_DOWN":
                    minR = 0;
                    maxR = 0;
                    minU = 0;
                    maxU = 2;
                    uOffset = -depth;
                    break;
                case "TUNNEL_1X1":
                    minR = 0;
                    maxR = 0;
                    minU = 0;
                    maxU = 0;
                    break;
            }

            for (int r = minR; r <= maxR; r++) {
                for (int u = minU; u <= maxU; u++) {
                    if (depth == 0 && r == 0 && u == 0 && uOffset == 0)
                        continue;
                    if (interactedCount >= maxBlocks)
                        return;

                    BlockPos offset = startPos
                            .relative(viewDir, fOffset)
                            .relative(rightDir, r)
                            .relative(upDir, u + uOffset);

                    if (interactBlock(player, level, hand, hitResult, offset, targetState)) {
                        interactedCount++;
                        if (checkTool && tool.isEmpty())
                            return;
                    }
                }
            }
        }
    }
}
