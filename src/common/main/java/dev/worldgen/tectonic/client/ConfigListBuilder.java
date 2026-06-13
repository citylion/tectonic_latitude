package dev.worldgen.tectonic.client;

import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.config.state.ConfigState;
import dev.worldgen.tectonic.config.state.object.NoiseState;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static dev.worldgen.tectonic.config.state.ConfigState.Caves.*;
import static dev.worldgen.tectonic.config.state.ConfigState.Continents.*;
import static dev.worldgen.tectonic.config.state.ConfigState.General.*;
import static dev.worldgen.tectonic.config.state.ConfigState.GlobalTerrain.*;
import static dev.worldgen.tectonic.config.state.ConfigState.Islands.*;
import static dev.worldgen.tectonic.config.state.ConfigState.Oceans.*;
import static dev.worldgen.tectonic.config.state.ConfigState.LatitudeTemperature.*;
import static dev.worldgen.tectonic.config.state.ConfigState.Experimental.*;

public interface ConfigListBuilder {
    void addCategory(DisplayMode displayMode, String name, Font font);
    void addBoolean(DisplayMode displayMode, String name, Consumer<Boolean> setter, boolean getter, boolean defaultValue);
    void addInteger(DisplayMode displayMode, String name, double min, double max, double step, Consumer<Integer> setter, double getter, double defaultValue);
    void addDouble(DisplayMode displayMode, String name, double min, double max, double step, Consumer<Double> setter, double getter, double defaultValue);
    
    default void addOverlay(DisplayMode displayMode, String name, Consumer<Boolean> setter, boolean getter, boolean defaultValue) {
        this.addBoolean(displayMode, name, setter, getter, defaultValue);
    }
    default void addNoise(DisplayMode displayMode, String name, NoiseState state, NoiseState defaultState) {
        this.addDouble(displayMode, name + "_scale", 0, 1, 0.01, value -> state.scale = value, state.scale, defaultState.scale);
        this.addDouble(displayMode, name + "_multiplier", 0, 5, 0.1, value -> state.multiplier = value, state.multiplier, defaultState.multiplier);
        this.addDouble(displayMode, name + "_offset", -1, 1, 0.01, value -> state.offset = value, state.offset, defaultState.offset);
    }

    default void build(Font font) {
        ConfigState state = ConfigHandler.getState();

        this.addCategory(DisplayMode.MOD_ONLY, "general", font);
        this.addBoolean(DisplayMode.MOD_ONLY, "mod_enabled", bool -> state.general.modEnabled = bool, state.general.modEnabled, MOD_ENABLED);
        this.addInteger(DisplayMode.MOD_ONLY, "snow_start_offset", 0, 256, 1, value -> state.general.snowStartOffset = value, state.general.snowStartOffset, SNOW_START_OFFSET);

        this.addCategory(DisplayMode.ALL, "global_terrain", font);
        this.addDouble(DisplayMode.ALL, "vertical_scale", 0.75, 15, 0.005, value -> state.globalTerrain.verticalScale = value, state.globalTerrain.verticalScale, VERTICAL_SCALE);
        this.addDouble(DisplayMode.ALL, "elevation_boost", 0, 1, 0.01, value -> state.globalTerrain.elevationBoost = value, state.globalTerrain.elevationBoost, ELEVATION_BOOST);
        this.addInteger(DisplayMode.ALL, "min_y", -2032, -64, 16, value -> state.globalTerrain.heightLimits.minY = value, state.globalTerrain.heightLimits.minY, HEIGHT_LIMITS.minY);
        this.addInteger(DisplayMode.ALL, "max_y", 256, 2032, 16, value -> state.globalTerrain.heightLimits.maxY = value, state.globalTerrain.heightLimits.maxY, HEIGHT_LIMITS.maxY);
        this.addOverlay(DisplayMode.ALL, "ultrasmooth", bool -> state.globalTerrain.ultrasmooth = bool, state.globalTerrain.ultrasmooth, ULTRASMOOTH);

        this.addCategory(DisplayMode.ALL, "continents", font);
        this.addDouble(DisplayMode.ALL, "ocean_offset", -2, 0, 0.01, value -> state.continents.oceanOffset = value, state.continents.oceanOffset, OCEAN_OFFSET);
        this.addDouble(DisplayMode.ALL, "continents_scale", 0.01, 1, 0.01, value -> state.continents.continentsScale = value, state.continents.continentsScale, CONTINENTS_SCALE);
        this.addDouble(DisplayMode.ALL, "erosion_scale", 0.01, 1, 0.01, value -> state.continents.erosionScale = value, state.continents.erosionScale, EROSION_SCALE);
        this.addDouble(DisplayMode.ALL, "ridge_scale", 0.01, 2, 0.01, value -> state.continents.ridgeScale = value, state.continents.ridgeScale, RIDGE_SCALE);
        this.addBoolean(DisplayMode.ALL, "underground_rivers", bool -> state.continents.undergroundRivers = bool, state.continents.undergroundRivers, UNDERGROUND_RIVERS);
        this.addBoolean(DisplayMode.MOD_ONLY, "river_lanterns", bool -> state.continents.riverLanterns = bool, state.continents.riverLanterns, RIVER_LANTERNS);
        this.addBoolean(DisplayMode.MOD_ONLY, "river_ice", bool -> state.continents.riverIce = bool, state.continents.riverIce, RIVER_ICE);
        this.addDouble(DisplayMode.ALL, "flat_terrain_skew", -1, 1, 0.01, value -> state.continents.flatTerrainSkew = value, state.continents.flatTerrainSkew, FLAT_TERRAIN_SKEW);
        this.addBoolean(DisplayMode.ALL, "rolling_hills", bool -> state.continents.rollingHills = bool, state.continents.rollingHills, ROLLING_HILLS);
        this.addBoolean(DisplayMode.ALL, "jungle_pillars", bool -> state.continents.junglePillars = bool, state.continents.junglePillars, JUNGLE_PILLARS);

        this.addCategory(DisplayMode.ALL, "islands", font);
        this.addBoolean(DisplayMode.ALL, "islands_enabled", bool -> state.islands.enabled = bool,  state.islands.enabled, ENABLED);
        this.addNoise(DisplayMode.ALL, "noise", state.islands.noise, NOISE);

        this.addCategory(DisplayMode.ALL, "oceans", font);
        this.addDouble(DisplayMode.ALL, "ocean_depth", -10, -0.05, 0.01, value -> state.oceans.oceanDepth = value, state.oceans.oceanDepth, OCEAN_DEPTH);
        this.addDouble(DisplayMode.ALL, "deep_ocean_depth", -10, -0.05, 0.01, value -> state.oceans.deepOceanDepth = value, state.oceans.deepOceanDepth, DEEP_OCEAN_DEPTH);
        this.addInteger(DisplayMode.MOD_ONLY, "monument_offset", -60, 0, 1, value -> state.oceans.monumentOffset = value, state.oceans.monumentOffset, MONUMENT_OFFSET);
        this.addBoolean(DisplayMode.MOD_ONLY, "remove_frozen_ocean_ice", bool -> state.oceans.removeFrozenOceanIce = bool, state.oceans.removeFrozenOceanIce, REMOVE_FROZEN_OCEAN_ICE);

        this.addCategory(DisplayMode.ALL, "biomes", font);
        this.addNoise(DisplayMode.ALL, "temperature", state.biomes.temperature, NoiseState.DEFAULT);
        this.addNoise(DisplayMode.ALL, "vegetation", state.biomes.vegetation, NoiseState.DEFAULT);

        this.addCategory(DisplayMode.ALL, "latitude_temperature", font);
        this.addInteger(DisplayMode.ALL, "cutoff", 0, 64000, 500, value -> state.latitudeTemperature.cutoff = value, state.latitudeTemperature.cutoff, CUTOFF);
        this.addDouble(DisplayMode.ALL, "max_delta", 0, 2, 0.05, value -> state.latitudeTemperature.maxDelta = value, state.latitudeTemperature.maxDelta, MAX_DELTA);
        this.addBoolean(DisplayMode.ALL, "subtract_lon", bool -> state.latitudeTemperature.subtractLon = bool, state.latitudeTemperature.subtractLon, SUBTRACT_LON);
        this.addDouble(DisplayMode.ALL, "subtraction_taper_factor", 0, 2, 0.05, value -> state.latitudeTemperature.subtractionTaperFactor = value, state.latitudeTemperature.subtractionTaperFactor, SUBTRACTION_TAPER_FACTOR);

        this.addCategory(DisplayMode.ALL, "caves", font);
        this.addDouble(DisplayMode.ALL, "depth_cutoff_start", -0.1, 1, 0.1, value -> state.caves.depthCutoffStart = value, state.caves.depthCutoffStart, DEPTH_CUTOFF_START);
        this.addDouble(DisplayMode.ALL, "depth_cutoff_size", 0, 1, 0.1, value -> state.caves.depthCutoffSize = value, state.caves.depthCutoffSize, DEPTH_CUTOFF_SIZE);
        this.addBoolean(DisplayMode.ALL, "cheese_enabled", bool -> state.caves.cheeseEnabled = bool, state.caves.cheeseEnabled, CHEESE_ENABLED);
        this.addDouble(DisplayMode.ALL, "cheese_additive", -0.5, 0.5, 0.01, value -> state.caves.cheeseAdditive = value, state.caves.cheeseAdditive, CHEESE_ADDITIVE);
        this.addBoolean(DisplayMode.ALL, "noodle_enabled", bool -> state.caves.noodleEnabled = bool, state.caves.noodleEnabled, NOODLE_ENABLED);
        this.addDouble(DisplayMode.ALL, "noodle_additive", -0.25, 0.25, 0.025, value -> state.caves.noodleAdditive = value, state.caves.noodleAdditive, NOODLE_ADDITIVE);
        this.addBoolean(DisplayMode.ALL, "spaghetti_enabled", bool -> state.caves.spaghettiEnabled = bool, state.caves.spaghettiEnabled, SPAGHETTI_ENABLED);
        this.addOverlay(DisplayMode.ALL, "carvers_enabled", bool -> state.caves.carversEnabled = bool, state.caves.carversEnabled, CARVERS_ENABLED);
        this.addBoolean(DisplayMode.ALL, "lava_tunnels", bool -> state.globalTerrain.lavaTunnels = bool, state.globalTerrain.lavaTunnels, LAVA_TUNNELS);
        this.addBoolean(DisplayMode.MOD_ONLY, "ore_fix", bool -> state.caves.oreFix = bool, state.caves.oreFix, ORE_FIX);
        
        this.addCategory(DisplayMode.MOD_ONLY, "experimental", font);
        this.addBoolean(DisplayMode.MOD_ONLY, "alternate_erosion_scaling", bool -> state.experimental.alternateErosionScaling = bool, state.experimental.alternateErosionScaling, ALTERNATE_EROSION_SCALING);
        this.addBoolean(DisplayMode.MOD_ONLY, "alternate_continents_scaling", bool -> state.experimental.alternateContinentsScaling = bool, state.experimental.alternateContinentsScaling, ALTERNATE_CONTINENTS_SCALING);
    }
    
    static Component text(String name) {
        return Component.literal(name);
    }
    
    static String config(String name) {
        return "config.tectonic." + name;
    }
    
    static String category(String name) {
        return config("category." + name);
    }
    
    static String option(String name) {
        return config("option." + name);
    }
    
    enum DisplayMode {
        ALL,
        MOD_ONLY;
    }
}
