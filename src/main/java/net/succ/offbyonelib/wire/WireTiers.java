package net.succ.offbyonelib.wire;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every wire tier, from every mod. Register tiers during mod construction or registration,
 * before any world loads.
 */
public final class WireTiers {

    private static final Map<ResourceLocation, WireTier> TIERS = new LinkedHashMap<>();

    private WireTiers() {}

    public static synchronized WireTier register(WireTier tier) {
        WireTier existing = TIERS.putIfAbsent(tier.id(), tier);
        if (existing != null && existing != tier)
            throw new IllegalStateException("Wire tier " + tier.id() + " is already registered");
        return tier;
    }

    public static Collection<WireTier> all() {
        return Collections.unmodifiableCollection(TIERS.values());
    }

    /**
     * The tier saved as {@code name}. Wires saved before tiers had namespaces store only the
     * path ("copper"), which matches the first registered tier with that path. An unknown tier
     * (its mod was removed) falls back to the first registered tier, so the wire stays.
     */
    public static WireTier byName(String name) {
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id != null && name.contains(":") && TIERS.containsKey(id)) return TIERS.get(id);
        for (WireTier tier : TIERS.values())
            if (tier.id().getPath().equals(name)) return tier;
        if (TIERS.isEmpty()) throw new IllegalStateException("No wire tiers registered");
        return TIERS.values().iterator().next();
    }
}
