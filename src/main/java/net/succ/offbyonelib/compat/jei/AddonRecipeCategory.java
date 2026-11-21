package net.succ.offbyonelib.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

// Shared layout for JEI categories: the machine as the icon, a title from
// "jei.<namespace>.category.<path>" of the recipe type, and an animated arrow between inputs and
// outputs. Only loads when JEI is installed, so reference it from the addon's JEI plugin only.
public abstract class AddonRecipeCategory<T> implements IRecipeCategory<T> {

    private static final int DEFAULT_HEIGHT = 40;
    private static final int ARROW_Y = 12;
    private static final int TEXT_COLOR = 0x404040;
    private static final int TEXT_MARGIN = 5;

    private final RecipeType<T> recipeType;
    private final int width;
    private final int height;
    private final int arrowX;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    protected AddonRecipeCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, ItemLike machine, int width, int arrowX) {
        this(guiHelper, recipeType, machine, width, DEFAULT_HEIGHT, arrowX);
    }

    protected AddonRecipeCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, ItemLike machine, int width, int height, int arrowX) {
        this.recipeType = recipeType;
        this.width = width;
        this.height = height;
        this.arrowX = arrowX;
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(machine));
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
    }

    protected static <T> RecipeType<T> type(String modId, String id, Class<T> recipeClass) {
        return RecipeType.create(modId, id, recipeClass);
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + recipeType.getUid().getNamespace() + ".category." + recipeType.getUid().getPath());
    }

    @Override public int getWidth()  { return width; }
    @Override public int getHeight() { return height; }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, arrowX, arrowY(recipe));
    }

    /** Where the arrow goes; categories whose slots move with the recipe line it up with them. */
    protected int arrowY(T recipe) {
        return ARROW_Y;
    }

    /**
     * Draws grey text at (x, y), wrapped to the page width so long translations and big config
     * numbers can't run off the side, and returns the y below it for the next line.
     */
    protected int drawText(GuiGraphics guiGraphics, Component text, int x, int y) {
        Font font = Minecraft.getInstance().font;
        for (FormattedCharSequence line : font.split(text, Math.max(20, width - x - TEXT_MARGIN))) {
            guiGraphics.drawString(font, line, x, y, TEXT_COLOR, false);
            y += font.lineHeight + 1;
        }
        return y;
    }
}
