package dev.worldgen.tectonic.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.config.state.object.NoiseState;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record ConfigNoise(NoiseHolder noise, DensityFunction shiftX, DensityFunction shiftZ, double scale, double multiplier, double offset, boolean smootherScaling, boolean isTemperature) implements DensityFunction {
    public static MapCodec<ConfigNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(df -> ""),
        NoiseHolder.CODEC.fieldOf("noise").forGetter(ConfigNoise::noise),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(ConfigNoise::shiftX),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(ConfigNoise::shiftZ)
    ).apply(instance, ConfigNoise::create));

    public static KeyDispatchDataCodec<ConfigNoise> CODEC_HOLDER = KeyDispatchDataCodec.of(DATA_CODEC);

    public static ConfigNoise create(String key, NoiseHolder noise, DensityFunction shiftX, DensityFunction shiftZ) {
        NoiseState state = ConfigHandler.getState().getNoiseState(key);
        if(key.equalsIgnoreCase("temperature")){
            return new ConfigNoise(noise, shiftX, shiftZ, state.scale, state.multiplier, state.offset, state.smootherScaling,true);
        }
        else{
            return new ConfigNoise(noise, shiftX, shiftZ, state.scale, state.multiplier, state.offset, state.smootherScaling,false);
        }

    }

    @Override
    public double compute(FunctionContext context) {
        double x;
        double z;
        if (smootherScaling) {
            x = (context.blockX() + shiftX.compute(context)) * scale;
            z = (context.blockZ() + shiftZ.compute(context)) * scale;
        } else {
            x = context.blockX() * scale + shiftX.compute(context);
            z = context.blockZ() * scale + shiftZ.compute(context);
        }
        return noise.getValue(x, 0, z) * multiplier + offset + getLatTempOffset(context.blockX(), context.blockZ());
    }

    //todo move to config
    private static int cutoff = 16000;
    private static double maxDelta = 1.0f; //the maximum amount of temperature change to be applied at north/south cutoffs.
    private static boolean subtractLon = true; //meaning that going farther west/east will limit this north/south forced temperature effect
    private static double subtractionTaperFactor = 0.5f;

    private double getLatTempOffset(double lon, double lat){//lat = z value of block coordinate
        if(!isTemperature){
            return 0;
        }
        if(subtractLon){
            lat = Math.max(0,lat-lon*subtractionTaperFactor);
        }
        //negative z -> more north
        //positive z more south
        double factor = oneMinusGausslike(lat/cutoff); //gets the distribution normalized to the cutoff value
        double offset = factor * maxDelta;
        if(lat < 0){
            return -offset;
        }
        else{
            return offset;
        }
    }

    /**
     * Returns a value between 0 and 1 in a distribution opposite of gaussian-like
     * f(0) ~ 0
     * f(0.25) ~ 0.117
     * f(0.5) ~ 0.39
     * f(0.75) ~.67
     * f(1) ~ ~.86
     * even function
     * 1 - guassian-like distribution
     * @return float
     */
    private double oneMinusGausslike(double x){
        final double k=2;//adjustable
        return (1.0d - (Math.exp(-0.5d*Math.pow(k*x,2))));
    }

    @Override
    public void fillArray(double[] doubles, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(doubles, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        if (this.smootherScaling) {
            return new ConfigNoise(visitor.visitNoise(noise), shiftX.mapAll(visitor), shiftZ.mapAll(visitor), scale, multiplier, offset, smootherScaling,isTemperature);
        }
        else if(this.isTemperature){
            return new ConfigNoise(visitor.visitNoise(noise), shiftX.mapAll(visitor), shiftZ.mapAll(visitor), scale, multiplier, offset, smootherScaling,isTemperature);
        }
        return DensityFunctions.add(
            DensityFunctions.mul(
                DensityFunctions.shiftedNoise2d(this.shiftX, this.shiftZ, this.scale, this.noise.noiseData()),
                DensityFunctions.constant(this.multiplier)
            ),
            DensityFunctions.constant(this.offset)
        ).mapAll(visitor);
    }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return noise.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
