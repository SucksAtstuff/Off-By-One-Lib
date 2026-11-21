package net.succ.offbyonelib.datagen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Ponder schematic (vanilla structure NBT) built in code, for {@link PonderSchematicProvider}.
 * Blocks are placed by id and block state properties, e.g.
 * {@code addBlock(2, 1, 2, "create:basin", "facing", "down")}. The default size is 5x8x5.
 */
public class SceneStructure {
    private final List<CompoundTag> palette = new ArrayList<>();
    private final List<CompoundTag> blocks = new ArrayList<>();
    private final Map<String, Integer> paletteIndex = new LinkedHashMap<>();
    private final int sizeX, sizeY, sizeZ;

    public SceneStructure() { this(5, 8, 5); }

    public SceneStructure(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public SceneStructure withBasePlate() {
        return withBasePlateExcept(-1, -1);
    }

    // A plate with one hole, for something set into the floor (e.g. a shaft)
    public SceneStructure withBasePlateExcept(int holeX, int holeZ) {
        int idx = getOrAddPalette("minecraft:smooth_stone", Map.of());
        for (int x = 0; x < sizeX; x++)
            for (int z = 0; z < sizeZ; z++)
                if (x != holeX || z != holeZ) placeBlock(idx, x, 0, z);
        return this;
    }

    public SceneStructure addBlock(int x, int y, int z, String name, String... properties) {
        return addBlockWithNbt(x, y, z, null, name, properties);
    }

    // With block entity data, e.g. to load a multiblock already formed.
    public SceneStructure addBlockWithNbt(int x, int y, int z, CompoundTag nbt, String name, String... properties) {
        Map<String, String> props = new LinkedHashMap<>();
        for (int i = 0; i < properties.length; i += 2)
            props.put(properties[i], properties[i + 1]);
        int idx = getOrAddPalette(name, props);
        placeBlock(idx, x, y, z);
        if (nbt != null) {
            CompoundTag data = nbt.copy();
            data.putString("id", name);
            blocks.get(blocks.size() - 1).put("nbt", data);
        }
        return this;
    }

    private int getOrAddPalette(String name, Map<String, String> properties) {
        String key = name + properties;
        return paletteIndex.computeIfAbsent(key, k -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Name", name);
            if (!properties.isEmpty()) {
                CompoundTag props = new CompoundTag();
                properties.forEach(props::putString);
                entry.put("Properties", props);
            }
            palette.add(entry);
            return palette.size() - 1;
        });
    }

    private void placeBlock(int paletteIdx, int x, int y, int z) {
        CompoundTag block = new CompoundTag();
        block.putInt("state", paletteIdx);
        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf(x));
        pos.add(IntTag.valueOf(y));
        pos.add(IntTag.valueOf(z));
        block.put("pos", pos);
        blocks.add(block);
    }

    public CompoundTag build() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("DataVersion", 3955);

        ListTag size = new ListTag();
        size.add(IntTag.valueOf(sizeX));
        size.add(IntTag.valueOf(sizeY));
        size.add(IntTag.valueOf(sizeZ));
        tag.put("size", size);

        ListTag paletteList = new ListTag();
        palette.forEach(paletteList::add);
        tag.put("palette", paletteList);

        ListTag blocksList = new ListTag();
        blocks.forEach(blocksList::add);
        tag.put("blocks", blocksList);

        tag.put("entities", new ListTag());
        return tag;
    }
}
