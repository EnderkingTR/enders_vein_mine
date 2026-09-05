package enderveinmine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import enderveinmine.VeinMine;
import enderveinmine.config.VeinMineConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BlockOutlineRenderer {

    private static final Set<BlockPos> cachedBlocks = new HashSet<>();
    private static BlockPos lastTargetPos = null;
    private static boolean lastActiveState = false;
    private static long lastCacheTime = 0;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        boolean isActive = ClientSetup.isActive();
        boolean configEnabled = VeinMineConfig.INSTANCE.renderOutline.get();

        if (!isActive || !configEnabled) {
            lastActiveState = isActive;
            return;
        }

        net.minecraft.world.phys.HitResult hitResult = client.hitResult;
        if (hitResult == null || hitResult.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
            lastTargetPos = null;
            lastActiveState = isActive;
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos targetPos = blockHitResult.getBlockPos();
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

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = client.renderBuffers().bufferSource();
        Camera camera = client.gameRenderer.getMainCamera();

        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        poseStack.pushPose();

        float[] colors = hexToRgb("FFFFFF");
        float red = colors[0];
        float green = colors[1];
        float blue = colors[2];
        float alpha = 0.4f;
        float fillAlpha = 0.2f;

        // Draw lines
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        for (BlockPos pos : cachedBlocks) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            VoxelShape shape = state.getShape(level, pos);
            if (shape.isEmpty()) continue;

            PoseStack.Pose pose = poseStack.last();
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double x = pos.getX() - camX;
                double y = pos.getY() - camY;
                double z = pos.getZ() - camZ;

                float fMinX = (float) (x + minX - 0.005);
                float fMinY = (float) (y + minY - 0.005);
                float fMinZ = (float) (z + minZ - 0.005);
                float fMaxX = (float) (x + maxX + 0.005);
                float fMaxY = (float) (y + maxY + 0.005);
                float fMaxZ = (float) (z + maxZ + 0.005);

                consumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(red, green, blue, alpha).setNormal(1, 0, 0);
                consumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(red, green, blue, alpha).setNormal(1, 0, 0);
                consumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(red, green, blue, alpha).setNormal(0, 1, 0);
                consumer.addVertex(pose, fMinX, fMaxY, fMinZ).setColor(red, green, blue, alpha).setNormal(0, 1, 0);
                consumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(red, green, blue, alpha).setNormal(0, 0, 1);
                consumer.addVertex(pose, fMinX, fMinY, fMaxZ).setColor(red, green, blue, alpha).setNormal(0, 0, 1);

                consumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(red, green, blue, alpha).setNormal(-1, 0, 0);
                consumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(red, green, blue, alpha).setNormal(-1, 0, 0);
                consumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(red, green, blue, alpha).setNormal(0, -1, 0);
                consumer.addVertex(pose, fMaxX, fMinY, fMaxZ).setColor(red, green, blue, alpha).setNormal(0, -1, 0);
                consumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(red, green, blue, alpha).setNormal(0, 0, -1);
                consumer.addVertex(pose, fMaxX, fMaxY, fMinZ).setColor(red, green, blue, alpha).setNormal(0, 0, -1);

                consumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(red, green, blue, alpha).setNormal(0, -1, 0);
                consumer.addVertex(pose, fMinX, fMinY, fMaxZ).setColor(red, green, blue, alpha).setNormal(0, -1, 0);
                consumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(red, green, blue, alpha).setNormal(1, 0, 0);
                consumer.addVertex(pose, fMinX, fMaxY, fMinZ).setColor(red, green, blue, alpha).setNormal(1, 0, 0);

                consumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(red, green, blue, alpha).setNormal(0, 1, 0);
                consumer.addVertex(pose, fMaxX, fMaxY, fMinZ).setColor(red, green, blue, alpha).setNormal(0, 1, 0);
                consumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(red, green, blue, alpha).setNormal(0, 0, 1);
                consumer.addVertex(pose, fMaxX, fMinY, fMaxZ).setColor(red, green, blue, alpha).setNormal(0, 0, 1);
            });
        }

        // Draw filled quads
        VertexConsumer quadConsumer = bufferSource.getBuffer(RenderType.gui());
        for (BlockPos pos : cachedBlocks) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            VoxelShape shape = state.getShape(level, pos);
            if (shape.isEmpty()) continue;

            PoseStack.Pose pose = poseStack.last();
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double x = pos.getX() - camX;
                double y = pos.getY() - camY;
                double z = pos.getZ() - camZ;

                float fMinX = (float) (x + minX - 0.005);
                float fMinY = (float) (y + minY - 0.005);
                float fMinZ = (float) (z + minZ - 0.005);
                float fMaxX = (float) (x + maxX + 0.005);
                float fMaxY = (float) (y + maxY + 0.005);
                float fMaxZ = (float) (z + maxZ + 0.005);

                quadConsumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMinY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMinX, fMinY, fMaxZ).setColor(red, green, blue, fillAlpha);

                quadConsumer.addVertex(pose, fMinX, fMaxY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMaxY, fMinZ).setColor(red, green, blue, fillAlpha);

                quadConsumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMinX, fMaxY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMaxY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(red, green, blue, fillAlpha);

                quadConsumer.addVertex(pose, fMinX, fMinY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMinY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(red, green, blue, fillAlpha);

                quadConsumer.addVertex(pose, fMinX, fMinY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMinX, fMinY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMinX, fMaxY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMinX, fMaxY, fMinZ).setColor(red, green, blue, fillAlpha);

                quadConsumer.addVertex(pose, fMaxX, fMinY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMaxY, fMinZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMaxY, fMaxZ).setColor(red, green, blue, fillAlpha);
                quadConsumer.addVertex(pose, fMaxX, fMinY, fMaxZ).setColor(red, green, blue, fillAlpha);
            });
        }
        poseStack.popPose();
    }

    private static float[] hexToRgb(String hex) {
        hex = hex.replace("#", "");
        if (hex.length() == 6) {
            return new float[] {
                Integer.valueOf(hex.substring(0, 2), 16) / 255f,
                Integer.valueOf(hex.substring(2, 4), 16) / 255f,
                Integer.valueOf(hex.substring(4, 6), 16) / 255f
            };
        }
        return new float[]{1f, 1f, 1f};
    }

    private static void recalculateCache(Level level, Player player, BlockPos startPos) {
        cachedBlocks.clear();

        BlockState startState = level.getBlockState(startPos);
        if (startState.isAir()) return;

        int maxBlocks = VeinMineConfig.INSTANCE.maxBlocks.get();
        String shapeModeStr = VeinMineConfig.INSTANCE.shapeMode.get().name();
        String customShape = VeinMineConfig.INSTANCE.customShape.get();

        if ("SHAPED".equals(shapeModeStr)) {
            calculateShapedMine(level, startPos, startState, customShape, maxBlocks);
        } else if ("SHAPELESS".equals(shapeModeStr)) {
            calculateShapelessMine(level, startPos, startState, maxBlocks);
        } else {
            calculatePresetMine(level, player, startPos, startState, shapeModeStr, maxBlocks);
        }
    }

    private static void calculateShapelessMine(Level level, BlockPos startPos, BlockState startState, int maxBlocks) {
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

    private static void calculateShapedMine(Level level, BlockPos startPos, BlockState startState, String customShape, int maxBlocks) {
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

    private static boolean isSameBlockGroup(Block target, Block stateBlock) {
        if (target == stateBlock) return true;
        Set<Block> stoneGroup = Set.of(
            Blocks.STONE,
            Blocks.COBBLESTONE,
            Blocks.DEEPSLATE,
            Blocks.ANDESITE,
            Blocks.GRANITE,
            Blocks.DIORITE,
            Blocks.COBBLED_DEEPSLATE,
            Blocks.TUFF
        );
        return stoneGroup.contains(target) && stoneGroup.contains(stateBlock);
    }

    private static void calculatePresetMine(Level level, Player player, BlockPos startPos, BlockState startState, String mode, int maxBlocks) {
        Block targetBlock = startState.getBlock();
        Direction viewDir = Direction.orderedByNearest(player)[0];

        Direction upDir = Direction.UP;
        Direction rightDir; 
        if (viewDir == Direction.UP || viewDir == Direction.DOWN) {
            Direction playerFacing = player.getDirection();
            if (playerFacing.getAxis() == Direction.Axis.Y) {
                playerFacing = Direction.NORTH;
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
