package dev.overgrown.thaumaturge.item;

import dev.overgrown.aspectslib.aether.AetherDensity;
import dev.overgrown.aspectslib.aether.AetherDensityManager;
import dev.overgrown.aspectslib.aether.DynamicAetherDensityManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class AspectLensItem extends Item {
    private static final Identifier VITIUM_ASPECT = new Identifier("aspectslib", "vitium");

    public AspectLensItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.thaumaturge.aspect_lens.tooltip").formatted(Formatting.GRAY));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {
            // Get player position and biome information
            BlockPos pos = user.getBlockPos();

            // Get current aether density
            AetherDensity density = AetherDensityManager.getDensity(world, pos);

            // Get biome ID for dynamic modifications
            Identifier biomeId = world.getBiome(pos).getKey().orElseThrow().getValue();

            // Get dynamic modifications
            Map<Identifier, Double> modifications = DynamicAetherDensityManager.getModifications(biomeId);

            // Calculate vitium and total aspects
            double vitium = density.getDensity(VITIUM_ASPECT);
            double totalOtherAspects = 0.0;

            for (Map.Entry<Identifier, Double> entry : density.getDensities().entrySet()) {
                if (!entry.getKey().equals(VITIUM_ASPECT)) {
                    totalOtherAspects += entry.getValue();
                }
            }

            // Send information to player
            user.sendMessage(Text.literal("=== Aether Density Report ===").formatted(Formatting.GOLD));
            user.sendMessage(Text.literal("Biome: " + biomeId.toString()));

            // Display all aspects
            for (Map.Entry<Identifier, Double> entry : density.getDensities().entrySet()) {
                Formatting color = entry.getKey().equals(VITIUM_ASPECT) ? Formatting.RED : Formatting.GREEN;
                user.sendMessage(Text.literal(
                        String.format("%s: %.2f", entry.getKey().getPath(), entry.getValue())
                ).formatted(color));
            }

            // Display corruption status
            user.sendMessage(Text.literal("---").formatted(Formatting.GRAY));
            user.sendMessage(Text.literal(String.format("Vitium (Corruption): %.2f", vitium)).formatted(Formatting.RED));
            user.sendMessage(Text.literal(String.format("Total Other Aspects: %.2f", totalOtherAspects)).formatted(Formatting.GREEN));

            // Display corruption dominance
            if (vitium > totalOtherAspects) {
                user.sendMessage(Text.literal("Status: CORRUPTED").formatted(Formatting.DARK_RED));
            } else {
                user.sendMessage(Text.literal("Status: PURE").formatted(Formatting.DARK_GREEN));
            }

            // Display dynamic modifications if any
            if (modifications != null && !modifications.isEmpty()) {
                user.sendMessage(Text.literal("---").formatted(Formatting.GRAY));
                user.sendMessage(Text.literal("Dynamic Modifications:").formatted(Formatting.BLUE));

                for (Map.Entry<Identifier, Double> entry : modifications.entrySet()) {
                    String change = entry.getValue() >= 0 ? "+" : "";
                    user.sendMessage(Text.literal(
                            String.format("%s: %s%.2f", entry.getKey().getPath(), change, entry.getValue())
                    ));
                }
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    public static boolean hasLens(PlayerEntity player) {
        if (player == null) return false;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof AspectLensItem) {
                return true;
            }
        }
        return false;
    }
}