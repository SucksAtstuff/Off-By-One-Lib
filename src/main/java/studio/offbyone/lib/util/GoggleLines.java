package studio.offbyone.lib.util;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

/**
 * Goggle tooltip lines for tanks, with labels from one addon's lang keys.
 *
 * <pre>{@code
 * // labels from "create.mymod.tooltip.<key>", Create's own style
 * public static final GoggleLines GOGGLES = new GoggleLines("create", "mymod.tooltip.");
 * GOGGLES.tank(tooltip, "water", waterTank, ChatFormatting.AQUA);
 * }</pre>
 */
public class GoggleLines {

    private final String namespace;
    private final String keyPrefix;

    /** Labels are translated from {@code <namespace>.<keyPrefix><langKey>}. */
    public GoggleLines(String namespace, String keyPrefix) {
        this.namespace = namespace;
        this.keyPrefix = keyPrefix;
    }

    /** The label for {@code langKey}, to build other goggle lines with the same keys. */
    public LangBuilder label(String langKey, Object... args) {
        return new LangBuilder(namespace).translate(keyPrefix + langKey, args);
    }

    /** "Label: 1000 / 8000 mB". */
    public void tank(List<Component> tooltip, String langKey, int amount, int capacity, ChatFormatting color) {
        tankLine(langKey, amount, capacity, color).forGoggles(tooltip, 1);
    }

    public void tank(List<Component> tooltip, String langKey, FluidTank tank, ChatFormatting color) {
        tank(tooltip, langKey, tank.getFluidAmount(), tank.getCapacity(), color);
    }

    /** The same line, unsent, for callers that add to it before showing it. */
    public LangBuilder tankLine(String langKey, int amount, int capacity, ChatFormatting color) {
        return label(langKey)
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(amount).text(" / " + capacity + " mB").style(color).component());
    }

    /**
     * Two lines for a tank that can hold different fluids: "Label: Fluid Name", then its
     * amount indented below. Shows nothing while the tank is empty.
     */
    public void namedTank(List<Component> tooltip, String langKey, FluidTank tank, ChatFormatting nameColor) {
        if (tank.isEmpty()) return;
        label(langKey)
                .style(ChatFormatting.GRAY)
                .add(tank.getFluid().getHoverName().copy().withStyle(nameColor))
                .forGoggles(tooltip, 1);
        CreateLang.number(tank.getFluidAmount())
                .text(" / " + tank.getCapacity() + " mB")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 2);
    }
}
