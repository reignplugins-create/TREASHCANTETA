package com.trashcanmod;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TrashCanMenu extends AbstractContainerMenu {
    public static final int SIZE = 54;               // 6 rows of 9, same as a double chest
    public static final int STEP_TICKS = 10;         // countdown is synced in half-second steps
    public static final int MAX_STEPS = TrashCanBlockEntity.LIFETIME_TICKS / STEP_TICKS;

    private final Container container;
    private final ContainerData data;

    // Client side constructor (the real contents are synced from the server).
    public TrashCanMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(SIZE), new SimpleContainerData(SIZE));
    }

    // Server side constructor.
    public TrashCanMenu(int id, Inventory playerInventory, Container container, ContainerData data) {
        super(TrashCanMod.TRASH_CAN_MENU.get(), id);
        checkContainerSize(container, SIZE);
        checkContainerDataCount(data, SIZE);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        // Same layout as vanilla's 6 row chest GUI.
        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        int yOffset = (6 - 4) * 18;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + yOffset));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161 + yOffset));
        }

        this.addDataSlots(data);
    }

    /** Remaining time for a trash can slot, in half-second steps (0 = nothing / not started). */
    public int getRemainingSteps(int slotIndex) {
        return data.get(slotIndex);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index < SIZE) {
                // trash can -> player inventory
                if (!this.moveItemStackTo(stack, SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, SIZE, false)) {
                // player inventory -> trash can
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
