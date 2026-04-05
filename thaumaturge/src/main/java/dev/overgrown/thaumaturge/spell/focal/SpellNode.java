package dev.overgrown.thaumaturge.spell.focal;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single node in the spell tree. Each node references a {@link SpellComponentDefinition}
 * and may have child nodes subject to the definition's allowed-children rules.
 * Nodes also carry mutable parameter values that can be fine-tuned in the focal manipulator.
 */
public class SpellNode {
    private final Identifier definitionId;
    private final List<SpellNode> children = new ArrayList<>();
    private final Map<String, Float> parameterValues = new HashMap<>();

    public SpellNode(Identifier definitionId) {
        this.definitionId = definitionId;
        initDefaults();
    }

    private void initDefaults() {
        SpellComponentDefinition def = getDefinition();
        if (def != null) {
            for (ParameterDef p : def.parameters()) {
                parameterValues.put(p.key(), p.defaultValue());
            }
        }
    }

    public Identifier getDefinitionId() {
        return definitionId;
    }

    public SpellComponentDefinition getDefinition() {
        return FocalComponentRegistry.get(definitionId);
    }

    public List<SpellNode> getChildren() {
        return children;
    }

    public void addChild(SpellNode child) {
        children.add(child);
    }

    public boolean removeChild(SpellNode child) {
        return children.remove(child);
    }

    // ── Parameters ──────────────────────────────────────────────────────────

    public Map<String, Float> getParameterValues() {
        return parameterValues;
    }

    public float getParameter(String key) {
        return parameterValues.getOrDefault(key, 0f);
    }

    public void setParameter(String key, float value) {
        SpellComponentDefinition def = getDefinition();
        if (def == null) return;
        for (ParameterDef p : def.parameters()) {
            if (p.key().equals(key)) {
                parameterValues.put(key, Math.max(p.min(), Math.min(p.max(), value)));
                return;
            }
        }
    }

    // ── Child acceptance ────────────────────────────────────────────────────

    /**
     * Returns true if a component of the given type can be added as a child of this node.
     * Checks both type-based allowed-children and socket compatibility.
     */
    public boolean canAcceptChild(SpellComponentType childType) {
        SpellComponentDefinition def = getDefinition();
        return def != null && def.allowedChildren().contains(childType);
    }

    /**
     * Returns true if the given child definition's required sockets are
     * satisfied by this node's provided sockets.
     */
    public boolean canAcceptChildDef(SpellComponentDefinition childDef) {
        if (!canAcceptChild(childDef.type())) return false;
        SpellComponentDefinition def = getDefinition();
        if (def == null) return false;
        return def.provides().containsAll(childDef.requires());
    }

    // ── Complexity ──────────────────────────────────────────────────────────

    /**
     * Computes the total complexity of this node and all descendants.
     * Each repeated use of the same definition ID gets a 50% cumulative penalty.
     * Parameter values above their defaults add additional complexity.
     */
    public int computeComplexity() {
        Map<Identifier, Integer> usageCount = new HashMap<>();
        return computeComplexity(usageCount);
    }

    private int computeComplexity(Map<Identifier, Integer> usageCount) {
        SpellComponentDefinition def = getDefinition();
        if (def == null) return 0;

        int count = usageCount.getOrDefault(definitionId, 0);
        usageCount.put(definitionId, count + 1);

        // Base cost with 50% repeat penalty per prior use
        double cost = def.baseComplexity() * (1.0 + 0.5 * count);

        // Parameter complexity: additional cost for values above default
        for (ParameterDef p : def.parameters()) {
            float value = parameterValues.getOrDefault(p.key(), p.defaultValue());
            float delta = value - p.defaultValue();
            if (delta > 0) {
                cost += delta * p.complexityCostPerUnit();
            }
        }

        int total = (int) Math.ceil(cost);

        for (SpellNode child : children) {
            total += child.computeComplexity(usageCount);
        }
        return total;
    }

    // ── NBT Serialization ───────────────────────────────────────────────────

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("Id", definitionId.toString());

        if (!parameterValues.isEmpty()) {
            NbtCompound params = new NbtCompound();
            for (Map.Entry<String, Float> entry : parameterValues.entrySet()) {
                params.putFloat(entry.getKey(), entry.getValue());
            }
            nbt.put("Params", params);
        }

        if (!children.isEmpty()) {
            NbtList childList = new NbtList();
            for (SpellNode child : children) {
                childList.add(child.toNbt());
            }
            nbt.put("Children", childList);
        }
        return nbt;
    }

    public static SpellNode fromNbt(NbtCompound nbt) {
        Identifier id = Identifier.tryParse(nbt.getString("Id"));
        if (id == null) return null;

        SpellNode node = new SpellNode(id);

        // Load saved parameter values (overwriting defaults)
        if (nbt.contains("Params")) {
            NbtCompound params = nbt.getCompound("Params");
            for (String key : params.getKeys()) {
                node.parameterValues.put(key, params.getFloat(key));
            }
        }

        if (nbt.contains("Children")) {
            NbtList childList = nbt.getList("Children", 10); // 10 = NbtCompound
            for (int i = 0; i < childList.size(); i++) {
                SpellNode child = fromNbt(childList.getCompound(i));
                if (child != null) {
                    node.addChild(child);
                }
            }
        }
        return node;
    }

    /**
     * Collects all nodes in the tree (depth-first) into a flat list.
     */
    public List<SpellNode> flatten() {
        List<SpellNode> result = new ArrayList<>();
        flatten(result);
        return result;
    }

    private void flatten(List<SpellNode> result) {
        result.add(this);
        for (SpellNode child : children) {
            child.flatten(result);
        }
    }

    /**
     * Returns the depth of this node by walking up the tree from a given root.
     * Returns -1 if not found.
     */
    public static int depthOf(SpellNode root, SpellNode target) {
        return depthOf(root, target, 0);
    }

    private static int depthOf(SpellNode current, SpellNode target, int depth) {
        if (current == target) return depth;
        for (SpellNode child : current.children) {
            int found = depthOf(child, target, depth + 1);
            if (found >= 0) return found;
        }
        return -1;
    }

    /**
     * Returns the definition ID of the first EFFECT node found (depth-first),
     * or {@code null} if the tree contains no effects.
     */
    public Identifier findFirstEffect() {
        SpellComponentDefinition def = getDefinition();
        if (def != null && def.type() == SpellComponentType.EFFECT) {
            return definitionId;
        }
        for (SpellNode child : children) {
            Identifier found = child.findFirstEffect();
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Collects the definition IDs of all EFFECT nodes in the tree.
     */
    public List<Identifier> collectEffects() {
        List<Identifier> result = new ArrayList<>();
        collectEffects(result);
        return result;
    }

    private void collectEffects(List<Identifier> result) {
        SpellComponentDefinition def = getDefinition();
        if (def != null && def.type() == SpellComponentType.EFFECT) {
            if (!result.contains(definitionId)) {
                result.add(definitionId);
            }
        }
        for (SpellNode child : children) {
            child.collectEffects(result);
        }
    }

    /**
     * Collects the definition IDs of all MODIFIER nodes in the tree.
     */
    public List<Identifier> collectModifiers() {
        List<Identifier> result = new ArrayList<>();
        collectModifiers(result);
        return result;
    }

    private void collectModifiers(List<Identifier> result) {
        SpellComponentDefinition def = getDefinition();
        if (def != null && def.type() == SpellComponentType.MODIFIER) {
            if (!result.contains(definitionId)) {
                result.add(definitionId);
            }
        }
        for (SpellNode child : children) {
            child.collectModifiers(result);
        }
    }

    /**
     * Collects the definition IDs of all MEDIUM nodes in the tree.
     */
    public List<Identifier> collectMediums() {
        List<Identifier> result = new ArrayList<>();
        collectMediums(result);
        return result;
    }

    private void collectMediums(List<Identifier> result) {
        SpellComponentDefinition def = getDefinition();
        if (def != null && def.type() == SpellComponentType.MEDIUM
                && !definitionId.equals(FocalComponentRegistry.ROOT_ID)) {
            if (!result.contains(definitionId)) {
                result.add(definitionId);
            }
        }
        for (SpellNode child : children) {
            child.collectMediums(result);
        }
    }

    /**
     * Finds the parent of the given node in the tree rooted at this node.
     */
    public SpellNode findParent(SpellNode target) {
        for (SpellNode child : children) {
            if (child == target) return this;
            SpellNode found = child.findParent(target);
            if (found != null) return found;
        }
        return null;
    }
}
