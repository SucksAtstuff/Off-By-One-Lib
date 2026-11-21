package studio.offbyone.lib.multiblock;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The item for a wide {@link MultiBlockFluidBE} tank: placed on the top or bottom of one, it
 * fills the whole layer at once, like Create's Fluid Tank. Sneak to place a single block.
 *
 * The block should stay silent for the extra blocks: see {@link #isFillingLayer}.
 */
public class MultiBlockLayerItem extends BlockItem {

    private static final String SILENCE_KEY = "offbyonelib:silence_layer_place";

    private final Supplier<? extends BlockEntityType<? extends MultiBlockFluidBE<?>>> type;

    public MultiBlockLayerItem(Block block, Supplier<? extends BlockEntityType<? extends MultiBlockFluidBE<?>>> type,
                               Properties properties) {
        super(block, properties);
        this.type = type;
    }

    /**
     * True while {@code entity} is filling a layer with this item. The block's getSoundType
     * should then return a silent sound (like Create's {@code FluidTankBlock.SILENCED_METAL}),
     * so placing a 3x3 layer plays one sound instead of nine.
     */
    public static boolean isFillingLayer(@Nullable Entity entity) {
        return entity instanceof Player player && player.getPersistentData().contains(SILENCE_KEY);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult result = super.place(ctx);
        if (result.consumesAction())
            tryMultiPlace(ctx);
        return result;
    }

    private void tryMultiPlace(BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || player.isShiftKeyDown()) return;
        Direction face = ctx.getClickedFace();
        if (!face.getAxis().isVertical()) return;

        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos placedOnPos = pos.relative(face.getOpposite());
        if (level.getBlockState(placedOnPos).getBlock() != getBlock()) return;

        MultiBlockFluidBE<?> partAt = ConnectivityHandler.partAt(type.get(), level, placedOnPos);
        if (partAt == null) return;

        MultiBlockFluidBE<?> controller = partAt.getControllerBE();
        if (controller == null) return;

        int width = controller.getWidth();
        if (width == 1) return;

        BlockPos startPos = face == Direction.DOWN
                ? controller.getBlockPos().below()
                : controller.getBlockPos().above(controller.getHeight());

        if (startPos.getY() != pos.getY()) return;

        ItemStack stack = ctx.getItemInHand();
        int toPlace = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockState state = level.getBlockState(startPos.offset(x, 0, z));
                if (state.getBlock() == getBlock()) continue;
                if (!state.canBeReplaced()) return;
                toPlace++;
            }
        }

        if (!player.isCreative() && stack.getCount() < toPlace) return;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos p = startPos.offset(x, 0, z);
                if (level.getBlockState(p).getBlock() == getBlock()) continue;
                player.getPersistentData().putBoolean(SILENCE_KEY, true);
                super.place(BlockPlaceContext.at(ctx, p, face));
                player.getPersistentData().remove(SILENCE_KEY);
            }
        }
    }
}
