package nl.leobode.sprookjestuin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** A small gene-regulation and plant-development experiment, independent of Android UI. */
public final class DevelopmentEngine {
    public static final int PLANTS = 12, GENES = 24, SEASON_DAYS = 60;
    private final Random random = new Random();
    public final List<Plant> plants = new ArrayList<>();
    public int generation = 1, day = 1;
    public float sun = .62f, rain = .55f, shade = .20f;
    public boolean paused;

    public DevelopmentEngine() { seedRandom(); }

    public void tickDay() {
        if (paused) return;
        for (Plant p : plants) p.develop(sun, rain, shade);
        if (++day > SEASON_DAYS) nextGeneration();
    }

    public void setWeather(int mode) {
        if (mode == 0) { sun=.86f; rain=.26f; shade=.08f; }
        else if (mode == 1) { sun=.38f; rain=.90f; shade=.24f; }
        else { sun=.28f; rain=.48f; shade=.78f; }
    }

    public void reset() { generation=1; day=1; sun=.62f; rain=.55f; shade=.20f; paused=false; seedRandom(); }

    private void seedRandom() {
        plants.clear();
        for (int i=0;i<PLANTS;i++) plants.add(new Plant(randomGenome(), microSun(i), microWater(i)));
    }

    private void nextGeneration() {
        List<Plant> old = new ArrayList<>(plants);
        plants.clear();
        for (int i=0;i<PLANTS;i++) {
            Plant a=tournament(old), b=tournament(old);
            float[] dna=new float[GENES];
            for(int g=0;g<GENES;g++) {
                dna[g]=random.nextBoolean()?a.dna[g]:b.dna[g];
                if(random.nextFloat()<.16f) dna[g]=clamp(dna[g]+(float)random.nextGaussian()*.13f);
            }
            plants.add(new Plant(dna,microSun(i),microWater(i)));
        }
        generation++; day=1;
    }

    private Plant tournament(List<Plant> pop) {
        Plant a=pop.get(random.nextInt(pop.size())), b=pop.get(random.nextInt(pop.size()));
        return a.fitness()>=b.fitness()?a:b;
    }
    private float[] randomGenome(){float[] d=new float[GENES];for(int i=0;i<GENES;i++)d[i]=random.nextFloat();return d;}
    private float microSun(int i){return i%3==0?.78f:i%3==1?.56f:.34f;}
    private float microWater(int i){return i/4==0?.78f:i/4==1?.56f:.35f;}
    private static float clamp(float v){return Math.max(0,Math.min(1,v));}

    public static final class Plant {
        public final float[] dna;
        public final float localSun, localWater;
        // Regulatory substances. DNA never directly specifies a petal count or a colour.
        public float growthSignal=.14f, growthBrake=.10f, branchSignal=.08f;
        public float pigmentA=.08f, pigmentB=.08f, bloomSignal, stress;
        // Developed phenotype.
        public float age, height=.02f, leafMass=.02f, rootMass=.03f, bud, flower, seeds, health=1;

        Plant(float[] dna,float localSun,float localWater){this.dna=dna;this.localSun=localSun;this.localWater=localWater;}

        void develop(float globalSun,float rain,float shade) {
            age++;
            float light=clamp(globalSun*localSun*(1-shade*.72f));
            float water=clamp(rain*.62f+localWater*.38f-rootMass*.12f);
            float drought=Math.max(0,.38f-water), glare=Math.max(0,light-(.62f+dna[2]*.25f));
            stress=clamp(stress*.72f+drought*(1.25f-dna[3]) + glare*(1.12f-dna[4]));

            // Genes make and inhibit shared substances; environment opens or closes their switches.
            growthSignal=clamp(growthSignal*.70f + dna[0]*light*.24f + dna[1]*water*.18f - stress*.16f);
            growthBrake=clamp(growthBrake*.76f + dna[5]*(1-light)*.16f + stress*.26f);
            branchSignal=clamp(branchSignal*.78f + dna[6]*light*.12f + dna[7]*water*.10f - growthSignal*.05f);
            bloomSignal=clamp(bloomSignal*.80f + dna[8]*light*.17f + age/60f*dna[9]*.10f - stress*.17f);
            pigmentA=clamp(pigmentA*.84f + dna[10]*light*.12f + bloomSignal*dna[11]*.08f);
            pigmentB=clamp(pigmentB*.84f + dna[12]*water*.10f + stress*dna[13]*.08f);

            float energy=Math.max(0,light*(.3f+leafMass*.7f)*health);
            rootMass=clamp(rootMass+(.003f+dna[14]*.006f)*water*(1-growthBrake*.45f));
            leafMass=clamp(leafMass+energy*(.006f+dna[15]*.006f)*(1-growthBrake));
            height=clamp(height+growthSignal*energy*(.008f+dna[16]*.007f)*(1-growthBrake*.5f));
            bud=clamp(bud+(bloomSignal-.32f)*.025f*(.5f+energy));
            flower=clamp(flower+(bud-.46f)*.035f*(1-stress)-Math.max(0,age-53)*.008f);
            seeds=clamp(seeds+flower*energy*(.002f+dna[17]*.004f));
            health=clamp(health+.012f*water-.025f*stress);
        }

        public float fitness(){return seeds*2.4f+health*.35f+flower*.25f;}
        public int stage(){if(age<7)return 0;if(bud<.12f)return 1;if(flower<.12f)return 2;if(age<53)return 3;return 4;}
        public float hue(){return (pigmentA*.72f+pigmentB*.28f+dna[18]*.18f)%1f;}
        public int petals(){return 4+Math.round(clamp(branchSignal*.62f+dna[19]*.38f)*8);}
        public float petalRoundness(){return clamp(.18f+bloomSignal*.45f+dna[20]*.37f);}
        public float stemBend(){return (dna[21]-.5f)*.9f+stress*.25f;}
        public int leaves(){return 1+Math.round(leafMass*5+dna[22]*2);}
        public float glow(){return clamp(dna[23]*.55f+pigmentA*.25f+flower*.2f);}
    }
}
