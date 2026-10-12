package com.trashcanmod;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Reuses vanilla's double chest texture and adds a countdown bar under every occupied slot. */
public class TrashCanScreen extends AbstractContainerScreen<TrashCanMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public TrashCanScreen(TrashCanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 114 + 6 * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        this.renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(pose, x, y, 0, 0, this.imageWidth, 6 * 18 + 17);
        this.blit(pose, x, y + 6 * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    // Drawn after the items, in GUI-relative coordinates.
    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        super.renderLabels(pose, mouseX, mouseY);
        for (int i = 0; i < TrashCanMenu.SIZE; i++) {
            Slot slot = this.menu.slots.get(i);
            if (!slot.hasItem()) {
                continue;
            }
            int steps = this.menu.getRemainingSteps(i);
            if (steps <= 0) {
                continue;
            }
            int width = Math.max(1, steps * 16 / TrashCanMenu.MAX_STEPS);
            int color = steps > 10 ? 0xFF55FF55 : (steps > 5 ? 0xFFFFFF55 : 0xFFFF5555);
            // 1 pixel tall, in the slot's bottom border row so it doesn't cover the item.
            fill(pose, slot.x, slot.y + 16, slot.x + width, slot.y + 17, color);
        }
    }
}
