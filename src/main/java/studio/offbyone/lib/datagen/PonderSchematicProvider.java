package studio.offbyone.lib.datagen;

import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

/**
 * Writes Ponder schematics built with {@link SceneStructure} to
 * {@code assets/<modid>/ponder/<path>.nbt}, so scenes are code instead of hand-saved structures.
 * Scenes load them with {@code scene.showBasePlate()} etc. as usual, by {@code "<path>"}.
 *
 * Multiblocks don't form in Ponder, so save formed ones with
 * {@link SceneStructure#addBlockWithNbt} (controller, size) or they show as separate blocks.
 */
public abstract class PonderSchematicProvider implements DataProvider {

    private final PackOutput output;
    private final String modId;

    protected PonderSchematicProvider(PackOutput output, String modId) {
        this.output = output;
        this.modId = modId;
    }

    /** Every schematic, by path (e.g. {@code "fermentation_vat/usage"}). */
    protected abstract Map<String, SceneStructure> schematics();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (var entry : schematics().entrySet())
            futures.add(save(cache, entry.getKey(), entry.getValue()));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache, String path, SceneStructure structure) {
        Path outputPath = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(modId)
                .resolve("ponder")
                .resolve(path + ".nbt");
        try {
            byte[] bytes = toGzipBytes(structure.build());
            cache.writeIfNeeded(outputPath, bytes, Hashing.sha256().hashBytes(bytes));
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static byte[] toGzipBytes(CompoundTag tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(baos))) {
            NbtIo.write(tag, dos);
        }
        return baos.toByteArray();
    }

    @Override
    public String getName() {
        return "Ponder Schematics: " + modId;
    }
}
