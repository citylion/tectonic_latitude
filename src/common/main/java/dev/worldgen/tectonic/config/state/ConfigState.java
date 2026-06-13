package dev.worldgen.tectonic.config.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.state.object.HeightLimits;
import dev.worldgen.tectonic.config.state.object.NoiseState;

public class ConfigState {
    public static final String NOTICE = "Tectonic has a config screen that explains every setting if you go to the in-game mod menu! You can also find config info here -> https://github.com/Apollounknowndev/tectonic/wiki/Config";
    public static final int MINOR_VERSION = 1;
    public static final Codec<ConfigState> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("__notice").orElse(NOTICE).forGetter(state -> NOTICE),
        Codec.INT.fieldOf("minor_version").orElse(0).forGetter(state -> MINOR_VERSION),
        General.CODEC.fieldOf("general").forGetter(state -> state.general),
        GlobalTerrain.CODEC.fieldOf("global_terrain").orElse(GlobalTerrain.DEFAULT).forGetter(state -> state.globalTerrain),
        Continents.CODEC.fieldOf("continents").orElse(Continents.DEFAULT).forGetter(state -> state.continents),
        Islands.CODEC.fieldOf("islands").orElse(Islands.DEFAULT).forGetter(state -> state.islands),
        Oceans.CODEC.fieldOf("oceans").orElse(Oceans.DEFAULT).forGetter(state -> state.oceans),
        Biomes.CODEC.fieldOf("biomes").orElse(Biomes.DEFAULT).forGetter(state -> state.biomes),
        Caves.CODEC.fieldOf("caves").orElse(Caves.DEFAULT).forGetter(state -> state.caves),
        LatitudeTemperature.CODEC.fieldOf("latitude_temperature").orElse(LatitudeTemperature.DEFAULT).forGetter(state -> state.latitudeTemperature),
        Experimental.CODEC.fieldOf("experimental").orElse(Experimental.DEFAULT).forGetter(state -> state.experimental)
    ).apply(instance, ConfigState::create));
    public static final Codec<ConfigState> CODEC = Codec.withAlternative(BASE_CODEC, V2ConfigState.CODEC, V2ConfigState::upgrade);

    public General general;
    public GlobalTerrain globalTerrain;
    public Continents continents;
    public Islands islands;
    public Oceans oceans;
    public Biomes biomes;
    public Caves caves;
    public LatitudeTemperature latitudeTemperature;
    public Experimental experimental;

    public static ConfigState create(String ignored, int minorVersion, General general, GlobalTerrain globalTerrain, Continents continents, Islands islands, Oceans oceans, Biomes biomes, Caves caves, LatitudeTemperature latitudeTemperature, Experimental experimental) {
        return new ConfigState(minorVersion, general, globalTerrain, continents, islands, oceans, biomes, caves, latitudeTemperature, experimental);
    }

    public ConfigState(int minorVersion, General general, GlobalTerrain globalTerrain, Continents continents, Islands islands, Oceans oceans, Biomes biomes, Caves caves, LatitudeTemperature latitudeTemperature, Experimental experimental) {
        this.general = general;
        this.globalTerrain = globalTerrain;
        this.continents = continents;
        this.islands = islands;
        this.oceans = oceans;
        this.biomes = biomes;
        this.caves = caves;
        this.latitudeTemperature = latitudeTemperature;
        this.experimental = experimental;

        if (minorVersion < 1 && this.globalTerrain.ultrasmooth) {
            this.globalTerrain.heightLimits = HeightLimits.INCREASED_HEIGHT;
        }
    }

    public double getValue(String option) {
        return switch (option) {
            case "vertical_scale" -> this.globalTerrain.verticalScale;
            case "elevation_boost" -> this.globalTerrain.elevationBoost;
            case "min_y" -> this.globalTerrain.heightLimits.minY;
            case "max_y" -> this.globalTerrain.heightLimits.maxY;
            case "lava_tunnels" -> this.globalTerrain.lavaTunnels ? 1 : 0;

            case "ocean_offset" -> this.continents.oceanOffset;
            case "alternate_erosion_scale" -> this.continents.erosionScale * 4;
            case "underground_rivers" -> this.continents.undergroundRivers ? -1 : 0;
            case "flat_terrain_skew" -> this.continents.flatTerrainSkew;
            case "rolling_hills" -> this.continents.rollingHills ? 1 : 0;
            case "jungle_pillars" -> this.continents.junglePillars ? 1 : 0;

            case "ocean_depth" -> this.oceans.oceanDepth;
            case "deep_ocean_depth" -> this.oceans.deepOceanDepth;

            case "depth_cutoff_start" -> this.caves.depthCutoffStart;
            case "depth_cutoff_size" -> this.caves.depthCutoffSize;
            case "cheese_enabled" -> this.caves.cheeseEnabled ? 1 : 0;
            case "cheese_additive" -> this.caves.cheeseAdditive;

            case "noodle_enabled" -> this.caves.noodleEnabled ? 1 : 0;
            case "noodle_additive" -> this.caves.noodleAdditive;

            default -> 0;
        };
    }

    public NoiseState getNoiseState(String option) {
        return switch (option) {
            case "continents" -> new NoiseState(continents.continentsScale, 1, 0, this.experimental.alternateErosionScaling);
            case "island" -> this.islands.noise;
            case "erosion" -> new NoiseState(continents.erosionScale, 1, 0, this.experimental.alternateErosionScaling);
            case "ridge" -> new NoiseState(continents.ridgeScale, 1, 0);
            case "temperature" -> this.biomes.temperature;
            case "vegetation" -> this.biomes.vegetation;
            default -> throw new IllegalArgumentException("Unknown noise state option");
        };
    }

    public boolean test(String key) {
        return switch (key) {
            case "disable_islands" -> !this.islands.enabled && this.continents.oceanOffset < -0.49;
            case "increased_height" -> this.globalTerrain.heightLimits.maxY > 320;
            case "ultrasmooth" -> this.globalTerrain.ultrasmooth;
            case "remove_frozen_ocean_ice" -> this.oceans.removeFrozenOceanIce;
            case "river_lanterns" -> this.continents.riverLanterns;
            case "river_ice" -> this.continents.riverIce;
            case "ore_fix" -> this.caves.oreFix;
            case "no_carvers" -> !this.caves.carversEnabled;
            default -> false;
        };
    }


    public static class General {
        public static final boolean MOD_ENABLED = true;
        public static final int SNOW_START_OFFSET = 128;

        public static final General DEFAULT = new General(MOD_ENABLED, SNOW_START_OFFSET);
        public static final Codec<General> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("mod_enabled").forGetter(general -> general.modEnabled),
            Codec.INT.fieldOf("snow_start_offset").orElse(SNOW_START_OFFSET).forGetter(general -> general.snowStartOffset)
        ).apply(instance, General::new));

        public boolean modEnabled;
        public int snowStartOffset;

        public General(boolean modEnabled, int snowStartOffset) {
            this.modEnabled = modEnabled;
            this.snowStartOffset = snowStartOffset;
        }
    }

    public static class GlobalTerrain {
        public static final double VERTICAL_SCALE = 1.125;
        public static final double ELEVATION_BOOST = 0;
        public static final HeightLimits HEIGHT_LIMITS = HeightLimits.DEFAULT;
        public static final boolean LAVA_TUNNELS = true;
        public static final boolean ULTRASMOOTH = false;

        public static final GlobalTerrain DEFAULT = new GlobalTerrain(VERTICAL_SCALE, ELEVATION_BOOST, HEIGHT_LIMITS, LAVA_TUNNELS, ULTRASMOOTH);
        public static final Codec<GlobalTerrain> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("vertical_scale").orElse(VERTICAL_SCALE).forGetter(globalTerrain -> globalTerrain.verticalScale),
            Codec.DOUBLE.fieldOf("elevation_boost").orElse(ELEVATION_BOOST).forGetter(globalTerrain -> globalTerrain.elevationBoost),
            HeightLimits.FULL_CODEC.orElse(HEIGHT_LIMITS).forGetter(globalTerrain -> globalTerrain.heightLimits),
            Codec.BOOL.fieldOf("lava_tunnels").orElse(LAVA_TUNNELS).forGetter(globalTerrain -> globalTerrain.lavaTunnels),
            Codec.BOOL.fieldOf("ultrasmooth").orElse(ULTRASMOOTH).forGetter(globalTerrain -> globalTerrain.ultrasmooth)
        ).apply(instance, GlobalTerrain::new));

        public double verticalScale;
        public double elevationBoost;
        public HeightLimits heightLimits;
        public boolean lavaTunnels;
        public boolean ultrasmooth;

        public GlobalTerrain(double verticalScale, double elevationBoost, HeightLimits heightLimits, boolean lavaTunnels, boolean ultrasmooth) {
            this.verticalScale = verticalScale;
            this.elevationBoost = elevationBoost;
            this.heightLimits = heightLimits;
            this.lavaTunnels = lavaTunnels;
            this.ultrasmooth = ultrasmooth;
        }
    }

    public static class Continents {
        public static final double OCEAN_OFFSET = -0.8;
        public static final double CONTINENTS_SCALE = 0.13;
        public static final double EROSION_SCALE = 0.25;
        public static final double RIDGE_SCALE = 0.25;
        public static final boolean UNDERGROUND_RIVERS = true;
        public static final boolean RIVER_LANTERNS = true;
        public static final boolean RIVER_ICE = false;
        public static final double FLAT_TERRAIN_SKEW = 0.1;
        public static final boolean ROLLING_HILLS = true;
        public static final boolean JUNGLE_PILLARS = true;

        public static final Continents DEFAULT = new Continents(OCEAN_OFFSET, CONTINENTS_SCALE, EROSION_SCALE, RIDGE_SCALE, UNDERGROUND_RIVERS, RIVER_LANTERNS, RIVER_ICE, FLAT_TERRAIN_SKEW, ROLLING_HILLS, JUNGLE_PILLARS);
        public static final Codec<Continents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("ocean_offset").orElse(OCEAN_OFFSET).forGetter(continents -> continents.oceanOffset),
            Codec.DOUBLE.fieldOf("continents_scale").orElse(CONTINENTS_SCALE).forGetter(continents -> continents.continentsScale),
            Codec.DOUBLE.fieldOf("erosion_scale").orElse(EROSION_SCALE).forGetter(continents -> continents.erosionScale),
            Codec.DOUBLE.fieldOf("ridge_scale").orElse(RIDGE_SCALE).forGetter(continents -> continents.ridgeScale),
            Codec.BOOL.fieldOf("underground_rivers").orElse(UNDERGROUND_RIVERS).forGetter(continents -> continents.undergroundRivers),
            Codec.BOOL.fieldOf("river_lanterns").orElse(RIVER_LANTERNS).forGetter(continents -> continents.riverLanterns),
            Codec.BOOL.fieldOf("river_ice").orElse(RIVER_ICE).forGetter(continents -> continents.riverIce),
            Codec.DOUBLE.fieldOf("flat_terrain_skew").orElse(FLAT_TERRAIN_SKEW).forGetter(continents -> continents.flatTerrainSkew),
            Codec.BOOL.fieldOf("rolling_hills").orElse(ROLLING_HILLS).forGetter(continents -> continents.rollingHills),
            Codec.BOOL.fieldOf("jungle_pillars").orElse(JUNGLE_PILLARS).forGetter(continents -> continents.junglePillars)
        ).apply(instance, Continents::new));

        public double oceanOffset;
        public double continentsScale;
        public double erosionScale;
        public double ridgeScale;
        public boolean undergroundRivers;
        public boolean riverLanterns;
        public boolean riverIce;
        public double flatTerrainSkew;
        public boolean rollingHills;
        public boolean junglePillars;

        public Continents(double oceanOffset, double continentsScale, double erosionScale, double ridgeScale, boolean undergroundRivers, boolean riverLanterns, boolean riverIce, double flatTerrainSkew, boolean rollingHills, boolean junglePillars) {
            this.oceanOffset = oceanOffset;
            this.continentsScale = continentsScale;
            this.erosionScale = erosionScale;
            this.ridgeScale = ridgeScale;
            this.undergroundRivers = undergroundRivers;
            this.riverLanterns = riverLanterns;
            this.riverIce = riverIce;
            this.flatTerrainSkew = flatTerrainSkew;
            this.rollingHills = rollingHills;
            this.junglePillars = junglePillars;
        }
    }

    public static class Islands {
        public static final boolean ENABLED = true;
        public static final NoiseState NOISE = new NoiseState(0.11, 1, 0);

        public static final Islands DEFAULT = new Islands(ENABLED, NOISE);
        public static final Codec<Islands> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").orElse(ENABLED).forGetter(islands -> islands.enabled),
            NoiseState.codec("noise").orElse(new NoiseState(0.11, 1, 0)).forGetter(islands -> islands.noise)
        ).apply(instance, Islands::new));

        public boolean enabled;
        public NoiseState noise;

        public Islands(boolean enabled, NoiseState noise) {
            this.enabled = enabled;
            this.noise = noise;
        }
    }

    public static class Oceans {
        public static final double OCEAN_DEPTH = -0.22;
        public static final double DEEP_OCEAN_DEPTH = -0.45;
        public static final int MONUMENT_OFFSET = -30;
        public static final boolean REMOVE_FROZEN_OCEAN_ICE = false;

        public static final Oceans DEFAULT = new Oceans(OCEAN_DEPTH, DEEP_OCEAN_DEPTH, MONUMENT_OFFSET, REMOVE_FROZEN_OCEAN_ICE);
        public static final Codec<Oceans> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("ocean_depth").orElse(OCEAN_DEPTH).forGetter(oceans -> oceans.oceanDepth),
            Codec.DOUBLE.fieldOf("deep_ocean_depth").orElse(DEEP_OCEAN_DEPTH).forGetter(oceans -> oceans.deepOceanDepth),
            Codec.INT.fieldOf("monument_offset").orElse(MONUMENT_OFFSET).forGetter(oceans -> oceans.monumentOffset),
            Codec.BOOL.fieldOf("remove_frozen_ocean_ice").orElse(REMOVE_FROZEN_OCEAN_ICE).forGetter(oceans -> oceans.removeFrozenOceanIce)
        ).apply(instance, Oceans::new));

        public double oceanDepth;
        public double deepOceanDepth;
        public int monumentOffset;
        public boolean removeFrozenOceanIce;

        public Oceans(double oceanDepth, double deepOceanDepth, int monumentOffset, boolean removeFrozenOceanIce) {
            this.oceanDepth = oceanDepth;
            this.deepOceanDepth = deepOceanDepth;
            this.monumentOffset = monumentOffset;
            this.removeFrozenOceanIce = removeFrozenOceanIce;
        }
    }

    public static class Biomes {
        public static final Biomes DEFAULT = new Biomes(NoiseState.DEFAULT, NoiseState.DEFAULT);
        public static final Codec<Biomes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NoiseState.codec("temperature").orElse(NoiseState.DEFAULT).forGetter(biomes -> biomes.temperature),
            NoiseState.codec("vegetation").orElse(NoiseState.DEFAULT).forGetter(biomes -> biomes.vegetation)
        ).apply(instance, Biomes::new));

        public NoiseState temperature;
        public NoiseState vegetation;

        public Biomes(NoiseState temperature, NoiseState vegetation) {
            this.temperature = temperature;
            this.vegetation = vegetation;
        }
    }

    public static class Caves {
        public static final double DEPTH_CUTOFF_START = 0.1;
        public static final double DEPTH_CUTOFF_SIZE = 0.1;
        public static final boolean CHEESE_ENABLED = true;
        public static final double CHEESE_ADDITIVE = 0.27;
        public static final boolean NOODLE_ENABLED = true;
        public static final double NOODLE_ADDITIVE = -0.075;
        public static final boolean SPAGHETTI_ENABLED = true;
        public static final boolean CARVERS_ENABLED = true;
        public static final boolean ORE_FIX = false;

        public static final Caves DEFAULT = new Caves(DEPTH_CUTOFF_START, DEPTH_CUTOFF_SIZE, CHEESE_ENABLED, CHEESE_ADDITIVE, NOODLE_ENABLED, NOODLE_ADDITIVE, SPAGHETTI_ENABLED, CARVERS_ENABLED, ORE_FIX);
        public static final Codec<Caves> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("depth_cutoff_start").orElse(DEPTH_CUTOFF_START).forGetter(caves -> caves.depthCutoffStart),
            Codec.DOUBLE.fieldOf("depth_cutoff_size").orElse(DEPTH_CUTOFF_SIZE).forGetter(caves -> caves.depthCutoffSize),
            Codec.BOOL.fieldOf("cheese_enabled").orElse(CHEESE_ENABLED).forGetter(caves -> caves.cheeseEnabled),
            Codec.DOUBLE.fieldOf("cheese_additive").orElse(CHEESE_ADDITIVE).forGetter(caves -> caves.cheeseAdditive),
            Codec.BOOL.fieldOf("noodle_enabled").orElse(NOODLE_ENABLED).forGetter(caves -> caves.noodleEnabled),
            Codec.DOUBLE.fieldOf("noodle_additive").orElse(NOODLE_ADDITIVE).forGetter(caves -> caves.noodleAdditive),
            Codec.BOOL.fieldOf("spaghetti_enabled").orElse(SPAGHETTI_ENABLED).forGetter(caves -> caves.spaghettiEnabled),
            Codec.BOOL.fieldOf("carvers_enabled").orElse(CARVERS_ENABLED).forGetter(caves -> caves.carversEnabled),
            Codec.BOOL.fieldOf("ore_fix").orElse(ORE_FIX).forGetter(caves -> caves.oreFix)
        ).apply(instance, Caves::new));

        public double depthCutoffStart;
        public double depthCutoffSize;
        public boolean cheeseEnabled;
        public double cheeseAdditive;
        public boolean noodleEnabled;
        public double noodleAdditive;
        public boolean spaghettiEnabled;
        public boolean carversEnabled;
        public boolean oreFix;

        public Caves(double depthCutoffStart, double depthCutoffSize, boolean cheeseEnabled, double cheeseAdditive, boolean noodleEnabled, double noodleAdditive, boolean spaghettiEnabled, boolean carversEnabled, boolean oreFix) {
            this.depthCutoffStart = depthCutoffStart;
            this.depthCutoffSize = depthCutoffSize;
            this.cheeseEnabled = cheeseEnabled;
            this.cheeseAdditive = cheeseAdditive;
            this.noodleEnabled = noodleEnabled;
            this.noodleAdditive = noodleAdditive;
            this.spaghettiEnabled = spaghettiEnabled;
            this.carversEnabled = carversEnabled;
            this.oreFix = oreFix;
        }
    }
    
    public static class LatitudeTemperature {
        public static final int CUTOFF = 16000;
        public static final double MAX_DELTA = 1.0;
        public static final boolean SUBTRACT_LON = true;
        public static final double SUBTRACTION_TAPER_FACTOR = 0.5;

        public static final LatitudeTemperature DEFAULT = new LatitudeTemperature(CUTOFF, MAX_DELTA, SUBTRACT_LON, SUBTRACTION_TAPER_FACTOR);
        public static final Codec<LatitudeTemperature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("cutoff").orElse(CUTOFF).forGetter(temp -> temp.cutoff),
            Codec.DOUBLE.fieldOf("max_delta").orElse(MAX_DELTA).forGetter(temp -> temp.maxDelta),
            Codec.BOOL.fieldOf("subtract_lon").orElse(SUBTRACT_LON).forGetter(temp -> temp.subtractLon),
            Codec.DOUBLE.fieldOf("subtraction_taper_factor").orElse(SUBTRACTION_TAPER_FACTOR).forGetter(temp -> temp.subtractionTaperFactor)
        ).apply(instance, LatitudeTemperature::new));

        public int cutoff;
        public double maxDelta;
        public boolean subtractLon;
        public double subtractionTaperFactor;

        public LatitudeTemperature(int cutoff, double maxDelta, boolean subtractLon, double subtractionTaperFactor) {
            this.cutoff = cutoff;
            this.maxDelta = maxDelta;
            this.subtractLon = subtractLon;
            this.subtractionTaperFactor = subtractionTaperFactor;
        }
    }

    public static class Experimental {
        public static final boolean ALTERNATE_EROSION_SCALING = false;
        public static final boolean ALTERNATE_CONTINENTS_SCALING = false;
        
        public static final Experimental DEFAULT = new Experimental(ALTERNATE_EROSION_SCALING, ALTERNATE_CONTINENTS_SCALING);
        public static final Codec<Experimental> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("alternate_erosion_scaling", ALTERNATE_EROSION_SCALING).forGetter(e -> e.alternateErosionScaling),
            Codec.BOOL.optionalFieldOf("alternate_continents_scaling", ALTERNATE_CONTINENTS_SCALING).forGetter(e -> e.alternateContinentsScaling)
        ).apply(i, Experimental::new));
        
        public boolean alternateErosionScaling;
        public boolean alternateContinentsScaling;
        
        public Experimental(boolean alternateErosionScaling, boolean alternateContinentsScaling) {
            this.alternateErosionScaling = alternateErosionScaling;
            this.alternateContinentsScaling = alternateContinentsScaling;
        }
    }
}
