package net.succ.offbyonelib.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes KubeJS recipe schemas ({@code data/<modid>/kubejs/recipe_schema/<type>.json}), which give
 * an addon's recipe types script syntax like {@code event.recipes.mymod.pressing(result, input)}
 * with builder functions. Plain JSON that KubeJS reads, so the addon needs no KubeJS dependency.
 *
 * <pre>{@code
 * protected void addSchemas() {
 *     schema("pressing")
 *             .key("result", Role.OUTPUT, ITEM_STACK)
 *             .key("ingredient", Role.INPUT, INGREDIENT)
 *             .key("processing_time", Role.INPUT, TICKS).optional(100).alwaysWrite().functionNames("processingTime");
 * }
 * }</pre>
 *
 * Keys are the recipe JSON's field names, in the order the default constructor takes them
 * (optional ones last). Component types are KubeJS's: see the constants and {@link #list},
 * {@link #intRange}, {@link #floatRange}.
 */
public abstract class KubeRecipeSchemaProvider implements DataProvider {

    public static final String ITEM_STACK = "item_stack";
    public static final String INGREDIENT = "ingredient";
    /** An ingredient with a count, written flat: {@code {"item": ..., "count": 4}}. */
    public static final String FLAT_SIZED_INGREDIENT = "flat_sized_ingredient";
    public static final String FLUID_STACK = "fluid_stack";
    /** A fluid stack that may be empty (for optional keys). */
    public static final String OPTIONAL_FLUID_STACK = "optional_fluid_stack";
    /** A duration in ticks; scripts can also write "5s". */
    public static final String TICKS = "ticks";
    public static final String BOOLEAN = "boolean";
    public static final String POSITIVE_INT = "positive_int";
    public static final String NON_NEGATIVE_INT = "non_negative_int";
    public static final String NON_NEGATIVE_FLOAT = "non_negative_float";
    private static final String INT = "int";
    private static final String FLOAT = "float";

    public enum Role { INPUT, OUTPUT, OTHER }

    private final PackOutput output;
    private final String modId;
    private final Map<String, Schema> schemas = new LinkedHashMap<>();

    protected KubeRecipeSchemaProvider(PackOutput output, String modId) {
        this.output = output;
        this.modId = modId;
    }

    protected abstract void addSchemas();

    /** Starts the schema for {@code <modid>:<recipeType>}. */
    protected Schema schema(String recipeType) {
        Schema schema = new Schema();
        schemas.put(recipeType, schema);
        return schema;
    }

    public static JsonElement list(Object component) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "list");
        json.add("component", toJson(component));
        return json;
    }

    public static JsonElement intRange(int min, int max) {
        JsonObject json = new JsonObject();
        json.addProperty("type", INT);
        json.addProperty("min", min);
        json.addProperty("max", max);
        return json;
    }

    public static JsonElement floatRange(float min, float max) {
        JsonObject json = new JsonObject();
        json.addProperty("type", FLOAT);
        json.addProperty("min", min);
        json.addProperty("max", max);
        return json;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        schemas.clear();
        addSchemas();
        List<CompletableFuture<?>> writes = new ArrayList<>();
        schemas.forEach((type, schema) -> writes.add(DataProvider.saveStable(cache, schema.toJson(),
                output.getOutputFolder(PackOutput.Target.DATA_PACK)
                        .resolve(modId).resolve("kubejs/recipe_schema").resolve(type + ".json"))));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "KubeJS recipe schemas: " + modId;
    }

    private static JsonElement toJson(Object value) {
        if (value instanceof JsonElement json) return json;
        if (value instanceof Number number) return new JsonPrimitive(number);
        if (value instanceof Boolean bool) return new JsonPrimitive(bool);
        return new JsonPrimitive(value.toString());
    }

    public static final class Schema {
        private final List<Key> keys = new ArrayList<>();
        private final List<List<String>> constructors = new ArrayList<>();
        private final List<String> unique = new ArrayList<>();

        /** Adds a key; the {@code Key} methods below apply to the last key added. */
        public Key key(String name, Role role, Object type) {
            Key key = new Key(this, name, role, type);
            keys.add(key);
            return key;
        }

        /** An extra constructor, taking these keys in order (KubeJS adds the default one). */
        public Schema constructor(String... keyNames) {
            constructors.add(List.of(keyNames));
            return this;
        }

        /** Keys that identify a recipe, used for its generated id (usually the result). */
        public Schema unique(String... keyNames) {
            unique.addAll(List.of(keyNames));
            return this;
        }

        private JsonObject toJson() {
            JsonObject json = new JsonObject();
            JsonArray keyArray = new JsonArray();
            keys.forEach(key -> keyArray.add(key.toJson()));
            json.add("keys", keyArray);
            if (!unique.isEmpty()) {
                JsonArray array = new JsonArray();
                unique.forEach(array::add);
                json.add("unique", array);
            }
            if (!constructors.isEmpty()) {
                JsonArray array = new JsonArray();
                for (List<String> constructor : constructors) {
                    JsonObject object = new JsonObject();
                    JsonArray names = new JsonArray();
                    constructor.forEach(names::add);
                    object.add("keys", names);
                    array.add(object);
                }
                json.add("constructors", array);
            }
            return json;
        }
    }

    public static final class Key {
        private final Schema schema;
        private final String name;
        private final Role role;
        private final Object type;
        private JsonElement optional;
        private boolean defaultOptional;
        private boolean alwaysWrite;
        private final List<String> functionNames = new ArrayList<>();

        private Key(Schema schema, String name, Role role, Object type) {
            this.schema = schema;
            this.name = name;
            this.role = role;
            this.type = type;
        }

        /** Optional, with this default (a number, string, boolean or JSON). */
        public Key optional(Object defaultValue) {
            this.optional = KubeRecipeSchemaProvider.toJson(defaultValue);
            return this;
        }

        /** Optional, defaulting to the component's empty value (an empty fluid, say). */
        public Key optional() {
            this.defaultOptional = true;
            return this;
        }

        /** Write the value even when it's the default, for recipe fields that aren't optional. */
        public Key alwaysWrite() {
            this.alwaysWrite = true;
            return this;
        }

        /** Builder function names scripts can call to set this key, like {@code processingTime}. */
        public Key functionNames(String... names) {
            functionNames.addAll(List.of(names));
            return this;
        }

        /** Back to the schema, to chain the next key. */
        public Key key(String name, Role role, Object type) {
            return schema.key(name, role, type);
        }

        public Schema constructor(String... keyNames) {
            return schema.constructor(keyNames);
        }

        public Schema unique(String... keyNames) {
            return schema.unique(keyNames);
        }

        private JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("role", role.name().toLowerCase());
            json.add("type", KubeRecipeSchemaProvider.toJson(type));
            if (optional != null) json.add("optional", optional);
            if (defaultOptional) json.addProperty("default_optional", true);
            if (alwaysWrite) json.addProperty("always_write", true);
            if (!functionNames.isEmpty()) {
                JsonArray array = new JsonArray();
                functionNames.forEach(array::add);
                json.add("function_names", array);
            }
            return json;
        }
    }
}
