# Multiblock Tanks

Tanks that join into one big multiblock like Create's Fluid Tank: up to 3x3 wide (by default) and as tall as you allow, with one controller holding the contents.

## The block entity

Extend `MultiBlockFluidBE`. It handles the controller, size, syncing and saving the shape; you add the tanks and implement the rest of Create's `IMultiBlockEntityContainer.Fluid`.

```java
public class VatBlockEntity extends MultiBlockFluidBE<VatBlockEntity> {
    public VatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VAT.get(), pos, state, VatBlockEntity.class);
    }

    @Override
    protected void updatePosition() { /* the multiblock changed shape: resize tanks, re-scan */ }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveMultiblockNBT(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadMultiblockNBT(tag);
    }
}
```

Non-controller blocks should hand capabilities and goggles to `getControllerBE()`. Override `getMaxWidth()` or `getMaxLength(axis, width)` for other size limits.

## The block

Form and split the multiblock with Create's `ConnectivityHandler`:

```java
@Override
public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
    if (old.getBlock() == state.getBlock() || moved || level.isClientSide) return;
    if (level.getBlockEntity(pos) instanceof VatBlockEntity be) ConnectivityHandler.formMulti(be);
}

@Override
public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
    if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof VatBlockEntity be)
        ConnectivityHandler.splitMulti(be);
    super.onRemove(state, level, pos, newState, moved);
}
```

## Placing a whole layer

Give the block a `MultiBlockLayerItem`: clicking the top or bottom of a wide tank fills the whole layer at once, like Create's Fluid Tank (sneak to place one block).

```java
REG.blockWithItem("vat", () -> new VatBlock(...),
        block -> new MultiBlockLayerItem(block, ModBlockEntities.VAT, new Item.Properties()));
```

So nine blocks don't play nine sounds, return a silent sound from the block while a layer fills:

```java
@Override
public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
    return MultiBlockLayerItem.isFillingLayer(entity) ? FluidTankBlock.SILENCED_METAL
            : super.getSoundType(state, level, pos, entity);
}
```

## Connected textures

`TankModel` joins textures across a formed tank (side, top and inside sheets); `CasingModel` joins casings in every direction. Make the shifts with `SpriteShifts`, which expects a `<texture>_connected.png` sheet next to each texture, laid out like Create's:

```java
public static final CTSpriteShiftEntry VAT = SpriteShifts.rectangle(id("block/vat/vat"));
public static final CTSpriteShiftEntry VAT_TOP = SpriteShifts.rectangle(id("block/vat/vat_top"));
public static final CTSpriteShiftEntry VAT_INNER = SpriteShifts.rectangle(id("block/vat/vat_inner"));
public static final CTSpriteShiftEntry CASING = SpriteShifts.omni(id("block/casing/casing"));
```

Wrap the baked models in `ModelEvent.ModifyBakingResult`: `new TankModel(model, VAT, VAT_TOP, VAT_INNER)` and `new CasingModel(model, CASING, block -> block == ModBlocks.CASING.get())`.

## Gauges

`TankGaugeRenderer` draws a water gauge on the tank's sides. Subclass it with the gauge and dial partial models, the smallest width that shows it, and which tank it reads.
