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
        // Two wild seedlings prevent a tiny chosen gene pool from collapsing
        // into eight nearly identical flowers.
        next.add(FlowerGenome.child(parents.get(random.nextInt(parents.size())),FlowerGenome.random(random),random,.28f));
        next.add(FlowerGenome.random(random));
        candidates.clear(); candidates.addAll(next);
        for(int i=0;i<selected.length;i++) selected[i]=false;
        generation++;
    }

    private boolean isDistinct(FlowerGenome candidate,List<FlowerGenome> others,float geneticMinimum,float visualMinimum) {
        for(FlowerGenome other:others)
            if(candidate.distanceTo(other)<geneticMinimum || candidate.visualDistanceTo(other)<visualMinimum)return false;
        return true;
    }

    public void reset() {
        candidates.clear(); garden.clear(); generation=1;
        for(int i=0;i<POPULATION;i++) candidates.add(FlowerGenome.random(random));
        for(int i=0;i<selected.length;i++) selected[i]=false;
    }
}
