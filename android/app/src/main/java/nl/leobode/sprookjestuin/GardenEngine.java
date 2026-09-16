package nl.leobode.sprookjestuin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GardenEngine {
    public static final int POPULATION = 8;
    private final Random random = new Random();
    public final List<FlowerGenome> candidates = new ArrayList<>();
    public final List<FlowerGenome> garden = new ArrayList<>();
    public final boolean[] selected = new boolean[POPULATION];
    public int generation = 1;
    public int lastPollinator = -1; // 0 bee, 1 butterfly, 2 moth

    public GardenEngine() { reset(); }

    public void toggle(int index) {
        if (index < 0 || index >= POPULATION) return;
        if (!selected[index] && selectedCount() >= 3) return;
        selected[index] = !selected[index];
    }

    public int selectedCount() { int n=0; for (boolean s : selected) if(s)n++; return n; }

    public void evolve() {
        List<FlowerGenome> parents = new ArrayList<>();
        for (int i=0;i<POPULATION;i++) if(selected[i]) parents.add(candidates.get(i));
        if (parents.isEmpty()) return;
        for (FlowerGenome p : parents) {
            garden.add(p.copy());
            if (garden.size() > 40) garden.remove(0);
        }
        List<FlowerGenome> next = new ArrayList<>();
        // Keep the strongest personal choice recognizable.
        next.add(parents.get(0).copy());
        float mutationRate=parents.size()==1?.34f:parents.size()==2?.25f:.19f;
        while(next.size()<POPULATION-2) {
            FlowerGenome a=parents.get(random.nextInt(parents.size()));
            FlowerGenome b=parents.get(random.nextInt(parents.size()));
            FlowerGenome child=null;
            for(int attempt=0;attempt<8;attempt++) {
                child=FlowerGenome.child(a,b,random,mutationRate);
                if(isDistinct(child,next,.105f,.32f)) break;
            }
            next.add(child);
        }
        // One pollinator cross can bring an older garden trait back.
        next.add(FlowerGenome.child(parents.get(random.nextInt(parents.size())),FlowerGenome.random(random),random,.28f));
        next.add(pollinatedSeedling(parents));
        candidates.clear(); candidates.addAll(next);
        for(int i=0;i<selected.length;i++) selected[i]=false;
        generation++;
    }

    private FlowerGenome pollinatedSeedling(List<FlowerGenome> fallback) {
        if(garden.size()<2) { lastPollinator=-1; return FlowerGenome.random(random); }
        lastPollinator=random.nextInt(3);
        FlowerGenome a=weightedGardenChoice(lastPollinator,null);
        FlowerGenome b=weightedGardenChoice(lastPollinator,a);
        return FlowerGenome.child(a,b,random,.22f);
    }

    private FlowerGenome weightedGardenChoice(int insect,FlowerGenome exclude) {
        float total=0;
        for(FlowerGenome g:garden) if(g!=exclude) total+=attraction(g,insect);
        float pick=random.nextFloat()*Math.max(.001f,total);
        for(FlowerGenome g:garden) if(g!=exclude) { pick-=attraction(g,insect); if(pick<=0)return g; }
        return garden.get(random.nextInt(garden.size()));
    }

    private float attraction(FlowerGenome g,int insect) {
        float[] x=g.genes;
        if(insect==0) return .2f+x[15]*.9f+(1-Math.abs(x[7]-.12f))*1.1f; // bee: open heart, warm
        if(insect==1) return .2f+x[9]*.8f+x[10]*.7f+x[13]*.5f;           // butterfly: large, vivid
        return .2f+x[14]*1.1f+(1-x[13])*.55f+x[16]*.45f;               // moth: pale, luminous
    }

    private boolean isDistinct(FlowerGenome candidate,List<FlowerGenome> others,float geneticMinimum,float visualMinimum) {
        for(FlowerGenome other:others)
            if(candidate.distanceTo(other)<geneticMinimum || candidate.visualDistanceTo(other)<visualMinimum)return false;
        return true;
    }

    public void reset() {
        candidates.clear(); garden.clear(); generation=1; lastPollinator=-1;
        for(int i=0;i<POPULATION;i++) candidates.add(FlowerGenome.random(random));
        for(int i=0;i<selected.length;i++) selected[i]=false;
    }
}
