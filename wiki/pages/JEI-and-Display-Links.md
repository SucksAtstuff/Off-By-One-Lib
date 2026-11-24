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

**Recipe IDs.** If your category shows data-driven recipes, JEI shows each one's ID on the output's tooltip when advanced tooltips are on (F3+H), and uses it to save bookmarks. The category finds the ID by looking the recipe up in the client's recipes. If your entries wrap a recipe instead of being one (say, a sealed interface with a display-only entry alongside the real ones), override `recipeOf` to return it:

```java
@Override
protected Recipe<?> recipeOf(Entry entry) {
    return entry instanceof Entry.Melt melt ? melt.recipe() : null;   // null: display-only, no ID
}
```

## Display Link sources

Two Create Display Link sources built from functions, so most readouts are one line. Register them in Create's `DisplaySource` registry under your mod id (the id is saved on placed links, so never rename one) and attach them to your block entity type.

```java
// A number on one line
() -> new NumericLineDisplaySource<>(ctx -> battery(ctx), be -> Component.literal(be.charge() + " SU"))

// A percentage or progress bar
() -> new PercentDisplaySource<>(ctx -> battery(ctx), be -> be.charge() / (float) be.capacity())
```

The first function finds your block entity from the `DisplayLinkContext` (return null when it isn't there); the second reads it.
