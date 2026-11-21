# Machine Helpers

Small pieces most machines need.

## Saving tanks

```java
FluidTankNBTHelper.save(tag, "water", waterTank);
FluidTankNBTHelper.load(tag, "water", waterTank);
```

## Hoppers and pipes that can't pull inputs back out

`AutomationItemHandler` wraps an inventory and limits which slots automation can insert into and extract from. Expose it as the block's item capability and keep the full inventory for the machine itself.

```java
// Slot 0 takes input, slot 1 gives output
IItemHandler automation = AutomationItemHandler.inputOutput(inventory, 0, 1);
// Or any rule
new AutomationItemHandler(inventory, slot -> slot < 2, (slot, stack) -> slot == 3);
```

## Right-clicking a machine

```java
// A bucket fills or empties the tanks; anything else goes into the slot if it fits
return MachineInteractions.useItem(level, player, hand, stack, be, be.fluidHandler(), be.inventory, 0);

// Empty hand: hand over the first of these slots that holds something
return MachineInteractions.takeOut(level, player, be, be.inventory, 2, 1);
```

## Goggle lines

`GoggleLines` formats tank lines in Create's goggle style, with labels from your lang keys. Make one per mod:

```java
// Labels from "create.mymod.tooltip.<key>"
public static final GoggleLines GOGGLES = new GoggleLines("create", "mymod.tooltip.");

GOGGLES.tank(tooltip, "water", waterTank, ChatFormatting.AQUA);   // Water: 1000 / 8000 mB
GOGGLES.namedTank(tooltip, "output", outputTank, ChatFormatting.GOLD); // Output: Biofuel, amount below
GOGGLES.label("header").forGoggles(tooltip);                       // any other line
```

`EnergyFormat.fe(1500)`, `feRate(20)` and `abbreviate(12000)` format FE amounts.

## Generators

For a `GeneratingKineticBlockEntity` with a clockwise / counter-clockwise option like Create's windmill:

```java
direction = GeneratorHelper.rotationDirection(this, 12, (state, face) -> face.getAxis().isHorizontal());
behaviours.add(direction);
// in getGeneratedSpeed()
return GeneratorHelper.directed(64, direction);
```

Generators must also set `lastCapacityProvided` in `calculateAddedStressCapacity()`, or the network gains phantom stress capacity on reload.

## Half shafts

`HalfShaftRenderer` draws a spinning shaft nub out of one face (the bottom by default; override `shaftSide(state)`). Don't also register Create's `SingleAxisRotatingVisual::shaft` Flywheel visualizer for these blocks: it draws a full-length shaft that pokes out of both ends.

## Machine sounds

Subclass `MachineSoundInstance` with your sound and a volume, and start it from the block entity's client tick:

```java
private MySoundInstance sound;

public void tickAudio() {
    sound = MachineSoundInstance.keepPlaying(level, sound, getBlockState().getValue(LIT),
            () -> new MySoundInstance(this));
}
```

It plays while the block's `LIT` property is true (override `isRunning()` otherwise), stops past 20 blocks, and stays silent in Ponder scenes. Sound files must be mono: Minecraft plays stereo files at full volume from any distance.

## Plants

`PlantGrowth.isPlant(state)`, `isGrowing(state)` and `boost(level, positions, bonus)` (extra random ticks; a bonus of 1.0 grows twice as fast) are shared by greenhouse and grow lamp style blocks.
