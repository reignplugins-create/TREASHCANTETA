package com.trashcanmod;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(TrashCanMod.MODID)
public class TrashCanMod {
    public static final String MODID = "trashcan";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<Block> TRASH_CAN = BLOCKS.register("trash_can",
            () -> new TrashCanBlock(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(2.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistryObject<Item> TRASH_CAN_ITEM = ITEMS.register("trash_can",
            () -> new BlockItem(TRASH_CAN.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final RegistryObject<BlockEntityType<TrashCanBlockEntity>> TRASH_CAN_BE = BLOCK_ENTITIES.register("trash_can",
            () -> BlockEntityType.Builder.of(TrashCanBlockEntity::new, TRASH_CAN.get()).build(null));

    public static final RegistryObject<MenuType<TrashCanMenu>> TRASH_CAN_MENU = MENUS.register("trash_can",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TrashCanMenu(windowId, inv)));

    public TrashCanMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
    }
}
