package net.succ.offbyonelib.advancement;

import net.succ.offbyonelib.advancement.SimpleTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * A Create-style advancement for datagen: title and description lang keys
 * ({@code advancement.<modid>.<id>} and {@code .desc}), task types for the frame and toast, and
 * criteria for collecting items or firing a {@link SimpleTrigger}.
 *
 * <pre>{@code
 * public static final List<AddonAdvancement> ENTRIES = new ArrayList<>();
 * static AddonAdvancement create(String id, UnaryOperator<AddonAdvancement.Builder> b) {
 *     AddonAdvancement adv = new AddonAdvancement(MODID, id, BACKGROUND, b);
 *     ENTRIES.add(adv);
 *     return adv;
 * }
 * }</pre>
 * Save them parents first from a DataProvider with {@link #save}, and add {@link #provideLang}
 * to the lang provider.
 */
public class AddonAdvancement {

    private final String modId;
    private final ResourceLocation background;
    private final String langPrefix;

    private final Advancement.Builder mcBuilder = Advancement.Builder.advancement();
    private AddonAdvancement parent;
    private final Builder builder = new Builder();
    AdvancementHolder datagenResult;
    private final String id;
    private String title;
    private String description;

    /** {@code background} is the tab background texture, used only on the advancement with id "root". */
    public AddonAdvancement(String modId, String id, ResourceLocation background, UnaryOperator<Builder> configurator) {
        this.modId = modId;
        this.background = background;
        this.langPrefix = "advancement." + modId + ".";
        this.id = id;
        configurator.apply(this.builder);
    }

    private String titleKey() { return langPrefix + id; }
    private String descriptionKey() { return titleKey() + ".desc"; }

    public void save(Consumer<AdvancementHolder> consumer, HolderLookup.Provider registries) {
        if (parent != null)
            mcBuilder.parent(parent.datagenResult);

        mcBuilder.display(
                builder.icon,
                Component.translatable(titleKey()),
                Component.translatable(descriptionKey()),
                id.equals("root") ? background : null,
                builder.type.advancementType,
                builder.type.toast,
                builder.type.announce,
                builder.type.hide
        );

        datagenResult = mcBuilder.save(consumer,
                ResourceLocation.fromNamespaceAndPath(modId, id).toString());
    }

    public void provideLang(BiConsumer<String, String> langOut) {
        langOut.accept(titleKey(), title);
        langOut.accept(descriptionKey(), description);
    }


    public enum TaskType {
        SILENT(AdvancementType.TASK,  false, false, false),
        NORMAL(AdvancementType.TASK,  true,  false, false),
        NOISY( AdvancementType.TASK,  true,  true,  false),
        EXPERT(AdvancementType.GOAL,  true,  true,  false),
        SECRET(AdvancementType.GOAL,  true,  true,  true),
        // Purple challenge frame: keep it for the one biggest goal so it stands out.
        CHALLENGE(AdvancementType.CHALLENGE, true, true, false);

        final AdvancementType advancementType;
        final boolean toast, announce, hide;

        TaskType(AdvancementType type, boolean toast, boolean announce, boolean hide) {
            this.advancementType = type;
            this.toast = toast;
            this.announce = announce;
            this.hide = hide;
        }
    }

    public class Builder {
        private TaskType type = TaskType.NORMAL;
        private int keyIndex = 0;
        private ItemStack icon = ItemStack.EMPTY;

        Builder() {}

        public Builder special(TaskType type) {
            this.type = type;
            return this;
        }

        public Builder after(AddonAdvancement other) {
            AddonAdvancement.this.parent = other;
            return this;
        }

        public Builder icon(ItemLike item) {
            this.icon = new ItemStack(item);
            return this;
        }

        public Builder title(String title) {
            AddonAdvancement.this.title = title;
            return this;
        }

        public Builder description(String desc) {
            AddonAdvancement.this.description = desc;
            return this;
        }

        public Builder whenIconCollected() {
            mcBuilder.addCriterion(String.valueOf(keyIndex++),
                    InventoryChangeTrigger.TriggerInstance.hasItems(icon.getItem()));
            return this;
        }

        public Builder whenItemCollected(ItemLike item) {
            mcBuilder.addCriterion(String.valueOf(keyIndex++),
                    InventoryChangeTrigger.TriggerInstance.hasItems(item));
            return this;
        }

        public Builder withCustomTrigger(DeferredHolder<CriterionTrigger<?>, SimpleTrigger> holder) {
            mcBuilder.addCriterion(String.valueOf(keyIndex++),
                    holder.get().createCriterion(new SimpleTrigger.Instance()));
            return this;
        }
    }
}
