package com.skilles.cannacraft.blocks.weedRack;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

public class WeedRackEntityRenderer implements BlockEntityRenderer<WeedRackEntity> {

    public WeedRackEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(WeedRackEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemStack itemStack = entity.getStack(0);
        Direction dir = entity.getCachedState().get(WeedRack.FACING);
        int k = (int)entity.getPos().asLong();
        int progress = entity.processTime;
        int j = light - 200;
        if (!itemStack.isEmpty()) {
            matrices.push();
            switch(dir) {
                case NORTH:
                    matrices.translate(0.5F, 0.4375F, 0.96875F);
                    break;
                case SOUTH:
                    matrices.translate(0.5F, 0.4375F, 0.03125F);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180F));
                    break;
                case EAST:
                    matrices.translate(0.03125F, 0.4375F, 0.5F);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270F));
                    break;
                case WEST:
                    matrices.translate(0.96875F, 0.4375F, 0.5F);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90F));
                    break;
                default:

            }

            itemRenderer.renderItem(itemStack, ModelTransformation.Mode.FIXED, j + progress * 2, overlay, matrices, vertexConsumers, k);
            matrices.pop();
        }
    }
}
