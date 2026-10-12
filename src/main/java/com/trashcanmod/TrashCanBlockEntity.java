package com.trashcanmod;

import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A 54-slot (double chest sized) container. Every item stack sits in its slot for
 * 10 seconds and is then deleted. Any change to a slot (adding, removing, moving)
 * restarts that slot's 10 second timer, so you never lose something sooner than
 * 10 seconds after you last touched it.
 *
 * Because this is a normal Container, hoppers can feed it too.
 */
public class TrashCanBlockEntity extends RandomizableContainerBlockEntity {
    public static final int SIZE = 54;
    public static final int LIFETIME_TICKS = 200; // 10 seconds at 20 ticks per second

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    // Server-only bookkeeping, not saved: after a restart every stack just gets a fresh 10 seconds.
    private final ItemStack[] seen = new ItemStack[SIZE];
    private final int[] timers = new int[SIZE];

    // Sent to the open GUI so it can draw the countdown bars (in half-second steps).
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 0 || index >= SIZE) {
                return 0;
            }
            return (timers[index] + TrashCanMenu.STEP_TICKS - 1) / TrashCanMenu.STEP_TICKS;
        }

        @Override
        public void set(int index, int value) {
            // read-only
        }

        @Override
        public int getCount() {
            return SIZE;
        }
    };

    public TrashCanBlockEntity(BlockPos pos, BlockState state) {
        super(TrashCanMod.TRASH_CAN_BE.get(), pos, state);
        Arrays.fill(seen, ItemStack.EMPTY);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TrashCanBlockEntity be) {
        be.tickItems();
    }

    private void tickItems() {
        boolean cleared = false;
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) {
                seen[i] = ItemStack.EMPTY;
                timers[i] = 0;
            } else if (!ItemStack.matches(stack, seen[i])) {
                // New stack, or the stack changed: (re)start the countdown.
                seen[i] = stack.copy();
                timers[i] = LIFETIME_TICKS;
            } else if (--timers[i] <= 0) {
                items.set(i, ItemStack.EMPTY);
                seen[i] = ItemStack.EMPTY;
                timers[i] = 0;
                cleared = true;
            }
        }
        if (cleared) {
            setChanged();
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> newItems) {
        this.items = newItems;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.trashcan.trash_can");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new TrashCanMenu(id, inventory, this, dataAccess);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
    }
}
