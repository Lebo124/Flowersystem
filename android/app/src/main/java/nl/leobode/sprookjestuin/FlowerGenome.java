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
    /** Distance between the traits a person notices first, rather than all genes equally. */
    public float visualDistanceTo(FlowerGenome other) {
        float hue=Math.abs(genes[7]-other.genes[7]); hue=Math.min(hue,1-hue);
        float petals=Math.abs((5+(int)(genes[5]*8))-(5+(int)(other.genes[5]*8)))/8f;
        float layers=Math.abs((1+(int)(genes[6]*3))-(1+(int)(other.genes[6]*3)))/3f;
        float stem=Math.abs((int)(genes[18]*4)-(int)(other.genes[18]*4))/3f;
        float leaves=Math.abs((int)(genes[24]*3)-(int)(other.genes[24]*3))/2f;
        return hue*1.8f+petals*.8f+layers*.65f+Math.abs(genes[9]-other.genes[9])*.55f+
                Math.abs(genes[10]-other.genes[10])*.45f+Math.abs(genes[15]-other.genes[15])*.45f+stem*.25f+leaves*.2f;
    }
    private static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }
}
