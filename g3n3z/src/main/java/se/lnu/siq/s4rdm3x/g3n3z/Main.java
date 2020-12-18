package se.lnu.siq.s4rdm3x.g3n3z;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.HuGMeExperimentRun;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;

import java.io.IOException;

public class Main {

    public static void main(String[] a_args) {
        HuGMeExperimentRun ex = null;
        java.lang.System.out.println("Hello genetic optimization world!");

        try {
            //FileBased system = new FileBased("data/systems/ProM6.9/ProM_6_9.sysmdl");
            FileBased system = new FileBased("data/systems/teammates/teammates.sysmdl");

            // load the system
            long seed = 171717;
            CGraph g = new CGraph();
            system.load(g);
            ArchDef a = system.createAndMapArch(g);
            Environment env = new Environment(g, a, seed);

            Individual best = env.evolve(40, 100);

            for (dmDependency.Type t: dmDependency.Type.values()) {
                java.lang.System.out.println(t + ":\t" + best.getDW(t));
            }
        } catch (IOException | System.NoMappedNodesException e) {
            e.printStackTrace();
        }
    }
}
