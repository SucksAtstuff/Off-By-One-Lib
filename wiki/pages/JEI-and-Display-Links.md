# JEI and Display Links

## JEI categories

`AddonRecipeCategory` lays out a JEI category the same way every time: the machine as the icon, the title from `jei.<namespace>.category.<path>` of the recipe type, and an animated arrow between inputs and outputs. It only loads with JEI, so use it from your JEI plugin only.

```java
public class HeatingCategory extends AddonRecipeCategory<HeatingRecipe> {
    public static final RecipeType<HeatingRecipe> TYPE = type(MODID, "heating", HeatingRecipe.class);

    public HeatingCategory(IGuiHelper gui) {
        super(gui, TYPE, ModBlocks.HEATER.get(), 120, 50);  // width, arrow x
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HeatingRecipe recipe, IFocusGroup focuses) { ... }
}
```

`drawText(graphics, text, x, y)` draws wrapped grey text and returns the y for the next line; override `arrowY(recipe)` to move the arrow.

## Display Link sources

Two Create Display Link sources built from functions, so most readouts are one line. Register them in Create's `DisplaySource` registry under your mod id (the id is saved on placed links, so never rename one) and attach them to your block entity type.

```java
// A number on one line
() -> new NumericLineDisplaySource<>(ctx -> battery(ctx), be -> Component.literal(be.charge() + " SU"))

// A percentage or progress bar
() -> new PercentDisplaySource<>(ctx -> battery(ctx), be -> be.charge() / (float) be.capacity())
```

The first function finds your block entity from the `DisplayLinkContext` (return null when it isn't there); the second reads it.
