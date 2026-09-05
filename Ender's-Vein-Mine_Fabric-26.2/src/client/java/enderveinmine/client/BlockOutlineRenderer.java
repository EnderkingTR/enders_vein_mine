package enderveinmine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import enderveinmine.config.VeinMineConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class BlockOutlineRenderer implements LevelRenderEvents.CollectSubmits {

    private final Set<BlockPos> cachedBlocks = new HashSet<>();
    private net.minecraft.world.phys.shapes.VoxelShape cachedShape = net.minecraft.world.phys.shapes.Shapes.empty();
    private BlockPos cachedOrigin = null;
    private BlockPos lastTargetPos = null;
    private boolean lastActiveState = false;
    private long lastCacheTime = 0;

    @Override
    public void collectSubmits(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        boolean isActive = VeinMineClient.isActive();
        boolean configEnabled = VeinMineConfig.INSTANCE.renderOutline;

        if (!isActive || !configEnabled) {
            lastActiveState = isActive;
            return;
        }

        HitResult hitResult = client.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            lastTargetPos = null;
            lastActiveState = isActive;
            return;
        }

        BlockPos targetPos = ((BlockHitResult) hitResult).getBlockPos();
        Level level = client.level;
        Player player = client.player;
        long currentTime = System.currentTimeMillis();

        if (lastTargetPos == null || !lastTargetPos.equals(targetPos) || !lastActiveState || (currentTime - lastCacheTime > 500)) {
            recalculateCache(level, player, targetPos);
            lastTargetPos = targetPos;
            lastActiveState = isActive;
            lastCacheTime = currentTime;
        }

        if (cachedBlocks.isEmpty()) return;

        PoseStack poseStack = context.poseStack();
        double camX = context.levelState().cameraRenderState.pos.x;
        double camY = context.levelState().cameraRenderState.pos.y;
        double camZ = context.levelState().cameraRenderState.pos.z;

        int colorArgb = 0xFFFFFFFF; // White opaque

        for (BlockPos pos : cachedBlocks) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            net.minecraft.world.phys.shapes.VoxelShape shape = state.getShape(level, pos);
            if (shape.isEmpty()) continue;

            PoseStack blockPoseStack = new PoseStack();
            blockPoseStack.last().set(poseStack.last());
            blockPoseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);
            
            context.submitNodeCollector().submitCustomGeometry(blockPoseStack, RenderTypes.debugFilledBox(), (pose, consumer) -> {
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    // Inflate slightly to avoid Z-fighting
                    float fMinX = (float) minX - 0.005f;
                    float fMinY = (float) minY - 0.005f;
                    float fMinZ = (float) minZ - 0.005f;
                    float fMaxX = (float) maxX + 0.005f;
                    float fMaxY = (float) maxY + 0.005f;
                    float fMaxZ = (float) maxZ + 0.005f;
                    
                    int r = 255;
                    int g = 255;
                    int b = 255;
                    int a = 64; // 25% opacity

                    // Bottom
                    consumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMinY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMinX, fMinY, fMaxZ).setColor(r, g, b, a);
                    // Top
                    consumer.addVertex(pose, fMinX, fMaxY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMaxY, fMinZ).setColor(r, g, b, a);
                    // North
                    consumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMinX, fMaxY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMaxY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(r, g, b, a);
                    // South
                    consumer.addVertex(pose, fMinX, fMinY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMinY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(r, g, b, a);
                    // West
                    consumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMinX, fMinY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMinX, fMaxY, fMinZ).setColor(r, g, b, a);
                    // East
                    consumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMaxY, fMinZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(r, g, b, a);
                    consumer.addVertex(pose, fMaxX, fMinY, fMaxZ).setColor(r, g, b, a);
                });
            });
        }
    }

    private void recalculateCache(Level level, Player player, BlockPos startPos) {
        cachedBlocks.clear();

        BlockState startState = level.getBlockState(startPos);
        if (startState.isAir()) return;

        int maxBlocks = VeinMineConfig.INSTANCE.maxBlocks;
        String shapeModeStr = VeinMineConfig.INSTANCE.shapeMode.name();
        String customShape = VeinMineConfig.INSTANCE.customShape;

        if ("SHAPED".equals(shapeModeStr)) {
            calculateShapedMine(level, startPos, startState, customShape, maxBlocks);
        } else if ("SHAPELESS".equals(shapeModeStr)) {
            calculateShapelessMine(level, startPos, startState, maxBlocks);
        } else {
            calculatePresetMine(level, player, startPos, startState, shapeModeStr, maxBlocks);
        }
    }

    private void calculateShapelessMine(Level level, BlockPos startPos, BlockState startState, int maxBlocks) {
        Block targetBlock = startState.getBlock();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);
        cachedBlocks.add(startPos);

        while (!queue.isEmpty() && cachedBlocks.size() < maxBlocks) {
            BlockPos current = queue.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        if (cachedBlocks.size() >= maxBlocks) break;
                        
                        BlockPos offset = current.offset(x, y, z);
                        if (!visited.add(offset)) continue;

                        BlockState state = level.getBlockState(offset);
                        if (state.getBlock() == targetBlock) {
                            cachedBlocks.add(offset);
                            queue.add(offset);
                        }
                    }
                    if (cachedBlocks.size() >= maxBlocks) break;
                }
                if (cachedBlocks.size() >= maxBlocks) break;
            }
        }
    }

    private void calculateShapedMine(Level level, BlockPos startPos, BlockState startState, String customShape, int maxBlocks) {
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

        cachedBlocks.add(startPos);

        for (int x = -hx; x <= hx; x++) {
            for (int y = -hy; y <= hy; y++) {
                for (int z = -hz; z <= hz; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    if (cachedBlocks.size() >= maxBlocks) return;

                    BlockPos pos = startPos.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    float hardness = state.getDestroySpeed(level, pos);

                    if (hardness >= 0 && hardness <= maxHardness && !state.isAir()) {
                        cachedBlocks.add(pos);
                    }
                }
            }
        }
    }

    private boolean isSameBlockGroup(Block target, Block stateBlock) {
        if (target == stateBlock) return true;
        Set<Block> stoneGroup = Set.of(
            net.minecraft.world.level.block.Blocks.STONE,
            net.minecraft.world.level.block.Blocks.COBBLESTONE,
            net.minecraft.world.level.block.Blocks.DEEPSLATE,
            net.minecraft.world.level.block.Blocks.ANDESITE,
            net.minecraft.world.level.block.Blocks.GRANITE,
            net.minecraft.world.level.block.Blocks.DIORITE,
            net.minecraft.world.level.block.Blocks.COBBLED_DEEPSLATE,
            net.minecraft.world.level.block.Blocks.TUFF
        );
        return stoneGroup.contains(target) && stoneGroup.contains(stateBlock);
    }

    private void calculatePresetMine(Level level, Player player, BlockPos startPos, BlockState startState, String mode, int maxBlocks) {
        Block targetBlock = startState.getBlock();
        net.minecraft.core.Direction viewDir = net.minecraft.core.Direction.orderedByNearest(player)[0];

        net.minecraft.core.Direction upDir = net.minecraft.core.Direction.UP;
        net.minecraft.core.Direction rightDir; 
        if (viewDir == net.minecraft.core.Direction.UP || viewDir == net.minecraft.core.Direction.DOWN) {
            net.minecraft.core.Direction playerFacing = player.getDirection();
            if (playerFacing.getAxis() == net.minecraft.core.Direction.Axis.Y) {
                playerFacing = net.minecraft.core.Direction.NORTH;
            }
            upDir = playerFacing.getOpposite(); // backward relative to player's horizontal looking
            rightDir = playerFacing.getClockWise();
        } else {
            rightDir = viewDir.getClockWise();
        }

        for (int depth = 0; depth < maxBlocks; depth++) {
            if (cachedBlocks.size() >= maxBlocks) break;

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
                    minR = 0; maxR = 0; // width 1
                    minU = 0; maxU = 1; // height 2
                    break;
                case "STAIRS_UP":
                    minR = 0; maxR = 0; // width 1
                    minU = 0; maxU = 2; // 3 blocks high
                    uOffset = depth; // go up 1 per depth
                    break;
                case "STAIRS_DOWN":
                    minR = 0; maxR = 0; // width 1
                    minU = 0; maxU = 2; // 3 blocks high
                    uOffset = -depth; // go down 1 per depth
                    break;
                case "TUNNEL_1X1":
                    minR = 0; maxR = 0;
                    minU = 0; maxU = 0;
                    break;
            }

            for (int r = minR; r <= maxR; r++) {
                for (int u = minU; u <= maxU; u++) {
                    if (cachedBlocks.size() >= maxBlocks) return;

                    BlockPos pos = startPos
                        .relative(viewDir, fOffset)
                        .relative(rightDir, r)
                        .relative(upDir, u + uOffset);
                    
                    BlockState state = level.getBlockState(pos);
                    
                    if (isSameBlockGroup(targetBlock, state.getBlock())) {
                        cachedBlocks.add(pos);
                    }
                }
            }
        }
    }
}
