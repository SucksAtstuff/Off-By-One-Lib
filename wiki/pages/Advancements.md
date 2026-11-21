# Advancements

`AddonAdvancement` builds Create-style advancements in datagen, with title and description lang keys (`advancement.<modid>.<id>` and `.desc`) and frame types from quiet tasks to challenges.

```java
public static final List<AddonAdvancement> ENTRIES = new ArrayList<>();
private static final ResourceLocation BACKGROUND =
        ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/salt_block.png");

private static AddonAdvancement create(String id, UnaryOperator<AddonAdvancement.Builder> b) {
    AddonAdvancement advancement = new AddonAdvancement(MODID, id, BACKGROUND, b);
    ENTRIES.add(advancement);
    return advancement;
}

public static final AddonAdvancement ROOT = create("root", b -> b
        .icon(ModItems.SALT.get())
        .title("Powered by Nature")
        .description("Collect Salt")
        .whenIconCollected());

public static final AddonAdvancement SEALED = create("greenhouse_sealed", b -> b
        .icon(ModBlocks.GREENHOUSE_CONTROLLER.get())
        .title("Under Glass")
        .description("Seal a greenhouse")
        .after(ROOT)
        .special(AddonAdvancement.TaskType.EXPERT)
        .withCustomTrigger(ModTriggers.GREENHOUSE_SEALED));
```

The advancement with id `root` gets the tab background. Save the entries in order (parents first) from a `DataProvider` with `save(consumer, registries)`, and add `provideLang` to your lang provider.

## Triggers

`SimpleTrigger` is a criterion with no conditions: register one per event and fire it with `trigger(player)`.

```java
public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> GREENHOUSE_SEALED =
        TRIGGERS.register("greenhouse_sealed", SimpleTrigger::new);

ModTriggers.GREENHOUSE_SEALED.get().trigger(player);
```

## Task types

| Type | Frame | Toast | Chat | Hidden |
|---|---|---|---|---|
| `SILENT` | task | no | no | no |
| `NORMAL` | task | yes | no | no |
| `NOISY` | task | yes | yes | no |
| `EXPERT` | goal | yes | yes | no |
| `SECRET` | goal | yes | yes | yes |
| `CHALLENGE` | challenge | yes | yes | no |
