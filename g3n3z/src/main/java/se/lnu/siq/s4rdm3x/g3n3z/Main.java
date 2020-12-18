package se.lnu.siq.s4rdm3x.g3n3z;

import se.lnu.siq.s4rdm3x.experiments.HuGMeExperimentRun;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.io.IOException;

public class Main {

    public static void main(String[] a_args) {
        HuGMeExperimentRun ex = null;
        java.lang.System.out.println("g3n3z: optimization of dependency weights using a genetic algorithm approach.");
        java.lang.System.out.println("\tg3n3z is a part of s4rdm3x tobias-dv-lnu.github.io/s4rdm3x/");

        final CmdArgsHandler args = new CmdArgsHandler(a_args);
        final String sysmdl = args.getArgumentString("-system");
        final int individuals = args.getArgumentInt("-individuals", 30);
        final int generations = args.getArgumentInt("-generations", 100);
        final int threads = args.getArgumentInt("-threads", 0);
        final double mutation = args.getArgumentDouble("-mutationChance", 0.2);
        final double amplitude = args.getArgumentDouble("-mutationAmplitude", 0.2);
        final int eliteIndividualMaxAge = args.getArgumentInt("-maxAge", 2);
        final int tournamentSize = args.getArgumentInt("-tournamentSize", 4);
        final int seed = args.getArgumentInt("-seed", (int)java.lang.System.nanoTime());


        final boolean argsError = sysmdl.length() == 0 ||
                individuals < 5 ||
                generations < 1 ||
                threads < 0 ||
                eliteIndividualMaxAge > generations;
        if (argsError) {
            java.lang.System.out.println("Arguments Error");
            printUsage();
            java.lang.System.exit(-1);
        }


        try {
            FileBased system = new FileBased(sysmdl);
           //FileBased system = new FileBased("data/systems/ProM6.9/ProM_6_9.sysmdl");
            //FileBased system = new FileBased("data/systems/teammates/teammates.sysmdl");

            // load the system
            CGraph g = new CGraph();
            system.load(g);
            ArchDef a = system.createAndMapArch(g);
            Environment env = new Environment(g, a, seed, system.getName());

            Individual best = env.evolve(individuals, generations, threads, mutation, amplitude, eliteIndividualMaxAge, tournamentSize);

        } catch (IOException | System.NoMappedNodesException e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        java.lang.System.out.println("Usage");
        java.lang.System.out.println("-system [path to .sysmdl file. Mandatory]");
        java.lang.System.out.println("-threads [number of threads per experiment. Optional: default is 1]");
        java.lang.System.out.println("-individuals [number of individuals, 5+. Optional: default is 30]");
        java.lang.System.out.println("-generations [number of generations, 1+. Optional: default is 100]");
        java.lang.System.out.println("-mutationChance [the chance an individual will have one weight mutated, 0-1. Optional: default is 0.2]");
        java.lang.System.out.println("-mutationAmplitude [how much a weight will maximally affected in the case of a mutation, 0-1. Optional: 0.2]");
        java.lang.System.out.println("-maxAge [the maximum generations an elite individual will survive, 0-[generations]. Optional: default is 2]");
        java.lang.System.out.println("-maxAge [the maximum number of individuals in a tournament, 0-[individuals]. Optional: default is 4]");
        java.lang.System.out.println("-seed [the maximum generations an elite individual will survive. Optional: default is based on system time]");
    }
}
