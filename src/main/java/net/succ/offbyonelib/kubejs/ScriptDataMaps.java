package net.succ.offbyonelib.kubejs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.slf4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One mod's {@code offbyonelib.kubejs.json}: the KubeJS event group its data maps go under, and
 * the data maps scripts can edit, by event name. Plain Java, so it never needs KubeJS:
 *
 * <pre>{@code
 * { "event_group": "MyModEvents",
 *   "data_maps": { "engineFuels": "mymod:engine_fuels" } }
 * }</pre>
 *
 * gives server scripts {@code MyModEvents.engineFuels(event => event.add('minecraft:lava', {...}))}.
 */
public record ScriptDataMaps(String modId, String eventGroup, Map<String, ResourceLocation> dataMaps) {

    public static final String FILE = "offbyonelib.kubejs.json";
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Every loaded mod's file. A broken file is logged and skipped. */
    public static List<ScriptDataMaps> scan() {
        List<ScriptDataMaps> found = new ArrayList<>();
        for (IModFileInfo info : ModList.get().getModFiles()) {
            Path path = info.getFile().findResource(FILE);
            if (!Files.exists(path) || info.getMods().isEmpty()) continue;
            String modId = info.getMods().getFirst().getModId();
            try (Reader reader = Files.newBufferedReader(path)) {
                found.add(parse(modId, JsonParser.parseReader(reader).getAsJsonObject()));
            } catch (Exception e) {
                LOGGER.error("Couldn't read {} from {}", FILE, modId, e);
            }
        }
        return found;
    }

    private static ScriptDataMaps parse(String modId, JsonObject json) {
        String group = json.get("event_group").getAsString();
        Map<String, ResourceLocation> maps = new LinkedHashMap<>();
        json.getAsJsonObject("data_maps").entrySet().forEach(entry ->
                maps.put(entry.getKey(), ResourceLocation.parse(entry.getValue().getAsString())));
        return new ScriptDataMaps(modId, group, maps);
    }
}
