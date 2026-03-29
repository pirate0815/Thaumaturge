package dev.overgrown.aspectslib.resonance;

import dev.overgrown.aspectslib.aspects.data.AspectData;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResonanceCalculator {
    public static ResonanceResult calculate(AspectData data) {
        double totalRU = 0;
        double amplificationFactor = 1.0;
        double barrierCost = 0;

        Map<Identifier, Integer> aspects = data.getMap();

        for (Map.Entry<Identifier, Integer> entry : aspects.entrySet()) {
            Identifier aspectId = entry.getKey();
            int amount = entry.getValue();

            // Get all resonance relationships for this aspect
            List<Resonance> resonances = ResonanceManager.RESONANCE_MAP.getOrDefault(
                    aspectId, Collections.emptyList()
            );

            for (Resonance resonance : resonances) {
                if (resonance == null) continue;

                // Find the other aspect in the pair
                Identifier otherId = resonance.aspect1().equals(aspectId) ?
                        resonance.aspect2() : resonance.aspect1();

                if (aspects.containsKey(otherId)) {
                    int otherAmount = aspects.get(otherId);

                    if (resonance.type() == Resonance.Type.AMPLIFYING) {
                        // Amplification: average of both amounts * factor
                        double boost = ((amount + otherAmount) / 2.0) * resonance.factor();
                        amplificationFactor += boost;
                        totalRU += boost;
                    } else { // OPPOSING
                        // Barrier cost: min amount * factor
                        double barrier = Math.min(amount, otherAmount) * resonance.factor();
                        barrierCost += barrier;
                        totalRU -= barrier;
                    }
                }
            }
        }

        return new ResonanceResult(totalRU, amplificationFactor, barrierCost);
    }

    public record ResonanceResult(double totalRU, double amplificationFactor, double barrierCost) {}
}