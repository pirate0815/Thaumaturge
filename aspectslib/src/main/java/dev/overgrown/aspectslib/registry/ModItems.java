package dev.overgrown.aspectslib.registry;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.aspects.item.AspectShardItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems {
    private static final Map<String, Item> ASPECT_SHARDS = new LinkedHashMap<>();
    public static final RegistryKey<ItemGroup> ASPECT_SHARDS_GROUP =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, AspectsLib.identifier("aspect_shards"));

    public static void initialize() {
        registerAspectShards();
        registerItemGroup();
    }

    private static void registerAspectShards() {
        registerShard("aer");
        registerShard("aqua");
        registerShard("ignis");
        registerShard("ordo");
        registerShard("perditio");
        registerShard("terra");
        registerShard("gelum");
        registerShard("lux");
        registerShard("metallum");
        registerShard("mortuus");
        registerShard("motus");
        registerShard("permutatio");
        registerShard("potentia");
        registerShard("vacuos");
        registerShard("victus");
        registerShard("vitreus");
        registerShard("bestia");
        registerShard("exanimis");
        registerShard("herba");
        registerShard("instrumentum");
        registerShard("praecantatio");
        registerShard("spiritus");
        registerShard("tenebrae");
        registerShard("vinculum");
        registerShard("volatus");
        registerShard("alienis");
        registerShard("alkimia");
        registerShard("auram");
        registerShard("aversio");
        registerShard("cognitio");
        registerShard("desiderium");
        registerShard("fabrico");
        registerShard("humanus");
        registerShard("machina");
        registerShard("praemunio");
        registerShard("sensus");
        registerShard("vitium");
        registerShard("fames");
    }

    private static void registerShard(String aspectName) {
        Identifier id = AspectsLib.identifier(aspectName + "_aspect_shard");
        Item item = new AspectShardItem(aspectName, new FabricItemSettings().maxCount(64));
        Registry.register(Registries.ITEM, id, item);
        ASPECT_SHARDS.put(aspectName, item);
        AspectsLib.LOGGER.info("Registered aspect shard: {}", id);
    }

    private static void registerItemGroup() {
        ItemGroup group = FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.aspectslib.aspect_shards"))
                .icon(() -> new ItemStack(ASPECT_SHARDS.get("aer")))
                .entries((displayContext, entries) -> {
                    for (Item item : ASPECT_SHARDS.values()) {
                        entries.add(item);
                    }
                })
                .build();

        Registry.register(Registries.ITEM_GROUP, ASPECT_SHARDS_GROUP, group);
        AspectsLib.LOGGER.info("Registered item group: {}", ASPECT_SHARDS_GROUP.getValue());
    }

    public static Item getAspectShard(String aspectName) {
        return ASPECT_SHARDS.get(aspectName);
    }
}