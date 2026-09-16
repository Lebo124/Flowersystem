package nl.leobode.sprookjestuin;

import java.util.Random;

/** A flower's inheritable properties, independent from Android drawing code. */
public final class FlowerGenome {
    public static final int GENE_COUNT = 26;
    public final float[] genes;

    private FlowerGenome(float[] genes) { this.genes = genes; }

    public static FlowerGenome random(Random r) {
        float[] g = new float[GENE_COUNT];
        for (int i = 0; i < g.length; i++) g[i] = r.nextFloat();
        return new FlowerGenome(g);
    }

    public static FlowerGenome child(FlowerGenome a, FlowerGenome b, Random r, float mutationRate) {
        float[] g = new float[GENE_COUNT];
        for (int i = 0; i < g.length; i++) {
            g[i] = r.nextBoolean() ? a.genes[i] : b.genes[i];
            if (r.nextFloat() < mutationRate) {
                // Most mutations are gentle; rare larger leaps keep the garden surprising.
                float strength = r.nextFloat() < .15f ? .42f : .14f;
                g[i] = clamp(g[i] + (float) r.nextGaussian() * strength);
            }
        }
        return new FlowerGenome(g);
    }

    public FlowerGenome copy() { return new FlowerGenome(genes.clone()); }
    public float distanceTo(FlowerGenome other) {
        float total=0;
        for(int i=0;i<genes.length;i++) total+=Math.abs(genes[i]-other.genes[i]);
        return total/genes.length;
    }
    private static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }
}
