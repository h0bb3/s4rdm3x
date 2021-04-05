package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapperEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GeneticClustering {

   private final int m_individualsPerGeneration;
   private final int m_maxGenerations;
   private final int m_maxThreads;
   private final double m_chanceOfMutation;
   private final int m_eliteIndividualMaxAge;
   private final int m_tournamentSize;
   private final int m_seed;


   public GeneticClustering(final int a_individualSPerGeneration, final int a_maxGenerations, final int a_maxThreads, final double a_chanceOfMutation, final int a_eliteIndividualMaxAge, final int a_tournamentSize, final int a_seed) {
      m_individualsPerGeneration = a_individualSPerGeneration;
      m_maxGenerations = a_maxGenerations;
      m_maxThreads = a_maxThreads;
      m_chanceOfMutation = a_chanceOfMutation;
      m_eliteIndividualMaxAge = a_eliteIndividualMaxAge;
      m_tournamentSize = a_tournamentSize;
      m_seed = a_seed;
   }

   class Individual implements Comparable<Individual> {
      CGraph m_graph;
      ArchDef m_arch;
      int m_eliteIndividualAge;
      double m_score = 0;

      // Create a new individual with a random mapping
      Individual(CGraph a_toBeCopied, ArchDef a_arch, Random a_r) {
         m_graph = a_toBeCopied.cloneNodes();
         m_arch = a_arch;
         m_eliteIndividualAge = 0;
         m_arch.getMappedNodes(m_graph.getNodes()).forEach( n ->
            //     m_arch.getMappedComponent(n).clusterToNode(n, ArchDef.Component.ClusteringType.Automatic)
            m_arch.getComponent(a_r.nextInt(m_arch.getComponentCount())).clusterToNode(n, ArchDef.Component.ClusteringType.Automatic)
         );
      }

      public Individual(Individual a_eliteIndividual) {
         m_graph = a_eliteIndividual.m_graph.cloneNodes();
         m_arch = a_eliteIndividual.m_arch;
         m_eliteIndividualAge = a_eliteIndividual.m_eliteIndividualAge + 1;
      }

      public Individual(Individual a_i1, Individual a_i2, Random a_r) {

         m_graph = a_i1.m_graph.cloneNodes();
         m_arch = a_i1.m_arch;
         m_eliteIndividualAge = 0;

         // all nodes currently clustered to i1 so do 50% as i2
         for(CNode i2Node : m_arch.getMappedNodes(a_i2.m_graph.getNodes())) {
            if (a_r.nextDouble() < 0.5) {
               CNode n = m_graph.getNodeByName(i2Node.getName());
               ArchDef.Component c = m_arch.getClusteredComponent(i2Node);
               c.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
            }
         }
      }

      public double getScore() {
         return m_score;
      }

      public int getEliteIndividualAge() {
         return m_eliteIndividualAge;
      }

      public void mutate(Random a_r, int a_count, int a_mappedNodeCount) {

         // fully new set of random genes
         //for (CNode n : m_arch.getMappedNodes(m_graph.getNodes())) {
         //   m_arch.getComponent(a_r.nextInt(m_arch.getComponentCount())).clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
         //}

         for (int i = 0; i < a_count; i++) {
            CNode n = selectRandomMappedNode(a_r, a_mappedNodeCount);
            m_arch.getComponent(a_r.nextInt(m_arch.getComponentCount())).clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
         }

      }

      private CNode selectRandomMappedNode(Random a_r, int a_mappedNodeCount) {
         int ix = a_r.nextInt(a_mappedNodeCount);
         for (CNode n : m_arch.getMappedNodes(m_graph.getNodes())) {
            if (ix == 0) {
               return n;
            }
            ix--;
         }

         return null;
      }

      public void eval(Random a_r) {

         // we set the same initial chance for every class regardless of the actual distribution
         // this way only the actual contents should affect the mapping.
         double [] initialDistribution = new double[m_arch.getComponentCount()];
         Arrays.fill(initialDistribution, 1.0/m_arch.getComponentCount());
         NBMapperEx c = new NBMapperEx(m_arch, false, false, false, true, true, 0, initialDistribution, 0.90);
         c.setMappingThreshold(0.9);
         c.doStemming(false);
         c.doWordCount(false);

         Iterable<CNode> mappedNodes = m_arch.getMappedNodes(m_graph.getNodes());

         final String changedClusteringTag = "changedClustering";
         int changedClustering = 0;
         int noClustering = 0;
         int unchangedClustering = 0;

         c.buildClassifier(m_graph);
         int correctlyClustered = 0;
         int erroneouslyClustered = 0;
         for (CNode n : mappedNodes) {
            // make the node an orphan
            ArchDef.Component original = m_arch.getClusteredComponent(n);
            original.removeClustering(n);
            c.clearAutoClusterings();
            c.run(m_graph);

            ArchDef.Component newComponent = m_arch.getClusteredComponent(n);
            original.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
            if (newComponent == null) {
               // no decision made reset to original
               // here we could go for the best clustering instead

               noClustering++;
            } else if (newComponent != original) {
               changedClustering++;
            } else {
               unchangedClustering++;
            }

            // this we cannot really know so it just for testing
            ArchDef.Component mapped = m_arch.getMappedComponent(n);
            if (mapped == original) {
               correctlyClustered++;
            } else {
               erroneouslyClustered++;
            }
         }
         System.out.println("");
         System.out.println("Changed clusterings: " + changedClustering);
         System.out.println("No clusterings: " + noClustering);

         m_score = (double)(unchangedClustering + noClustering) / (double)(unchangedClustering +  changedClustering + noClustering);
         // this is the full precision value
         //m_score = (double)correctlyClustered / (correctlyClustered + erroneouslyClustered);
      }

      @Override
      public int compareTo(Individual o) {
         return -Double.compare(getScore(), o.getScore());
      }

      public CGraph getCGraph() {
         return m_graph;
      }

      public Double getPrecision() {
         double clustered = 0, wrong = 0;
         // extract the data from the graph
         for (CNode n : m_arch.getMappedNodes(m_graph.getNodes())) {
            ArchDef.Component clusterComponent = m_arch.getClusteredComponent(n);
            if (clusterComponent != null) {
               clustered += 1;
               ArchDef.Component mappedComponent = m_arch.getMappedComponent(n);

               if (clusterComponent != mappedComponent) {
                  wrong += 1;
               }
            }
         }

         if (clustered < 1) {
            return 0.0;
         }
         return (clustered - wrong) / clustered;
      }
   }

   class Generation {
      Individual[] m_individuals;

      public Generation(CGraph a_graph, ArchDef a_arch, int a_individuals, Random a_r) {
         m_individuals = new Individual[a_individuals];
         for (int i = 0; i < m_individuals.length; i++) {
            m_individuals[i] = new Individual(a_graph, a_arch, a_r);
         }
      }

      // form the generation based on the best individuals from the previous generation
      public Generation(Generation a_prevGen, double a_chanceOfMutation, int a_mutationCount, int a_eliteIndividualMaxAge, int a_tournamentSize, Random a_r, int a_mappedNodeCount) {
         m_individuals = new Individual[a_prevGen.m_individuals.length];

         // elitism - but kill the elite individual if it has lived a long life
         int elitismOffset = a_prevGen.m_individuals[0].getEliteIndividualAge() < a_eliteIndividualMaxAge ? 1 : 0;
         if (elitismOffset > 0) {
            m_individuals[0] = new Individual(a_prevGen.m_individuals[0]);
         }

         for (int i = elitismOffset; i < m_individuals.length; i++) {
            Integer[] pop = tournament(a_tournamentSize, a_prevGen.m_individuals, a_r);
            Individual i1 = a_prevGen.m_individuals[pop[0]];
            Individual i2 = a_prevGen.m_individuals[pop[1]];

            m_individuals[i] = new Individual(i1, i2, a_r);
            if (a_r.nextDouble() < a_chanceOfMutation) {
               m_individuals[i].mutate(a_r, (int)(a_mappedNodeCount * 0.1), a_mappedNodeCount);
            }
         }
      }

      private Integer[] tournament(int a_tournamentSize, Individual[] a_population, Random a_r) {
         Integer[] tournament = new Integer[a_tournamentSize];
         Arrays.setAll(tournament, operand -> -1);

         for (int i = 0; i < tournament.length; i++) {
            tournament[i] = selectRandomIndividual(tournament, a_population, a_r);
         }

         // population is sorted with low indices having better score
         // therefore we can just sort the tournament so the lowest indices comes first
         Arrays.sort(tournament);

         return tournament;
      }

      private int selectRandomIndividual(Integer[] a_takenIndices, Individual[] a_population, Random a_r) {

         int ret = 0;
         boolean taken = false;
         do {
            ret = a_r.nextInt(a_population.length);
            taken = false;
            for (Integer i : a_takenIndices) {
               if (ret == i) {
                  taken = true;
                  break;
               }
            }

         } while (taken);

         return ret;
      }

      public void eval(int a_maxThreads, Random a_r) {

         if (a_maxThreads > 0) {
            evalThreads(a_maxThreads, a_r);
         } else {
            evalPlain(a_r);
         }

         Arrays.sort(m_individuals);
      }

      private void evalThreads(int a_maxThreads, Random a_r) {
         final int[] doneCount = {0};
         int startedThreads = 0;

         for (int i = 0; i < m_individuals.length; i++) {

            Individual indiv = m_individuals[i];
            Runnable r = new Runnable() {
               @Override
               public void run() {
                  indiv.eval(a_r);
                  System.out.println("\t\tIndividual Score: " + indiv.getScore());
                  doneCount[0]++;
               }
            };
            Thread t = new Thread(r);
            t.start();
            startedThreads++;
            // do not start too many threads
            while (startedThreads - doneCount[0] >= a_maxThreads) {
               try {
                  Thread.sleep(1000);
               } catch (InterruptedException e) {

               }
            }
         }

         while(doneCount[0] != m_individuals.length) {
            try {
               Thread.sleep(500);
            } catch (InterruptedException e) {

            }
         }
      }

      private void evalPlain(Random a_r) {
         for (int i = 0; i < m_individuals.length; i++) {

            Individual indiv = m_individuals[i];
            indiv.eval(a_r);
            System.out.println("\t\tIndividual score: " + indiv.getScore());
         }
      }

      public Double getAverageScore() {
         double ret = 0;
         for(Individual i : m_individuals) {
            ret += i.getScore();
         }

         return ret / (m_individuals.length);
      }

      public Individual getBest() {
         return m_individuals[0];
      }

      public Iterable<Individual> getIndividuals() {
         return Arrays.asList(m_individuals);
      }
   }



   public Individual evolve(CGraph a_graph, ArchDef a_arch) {
      Random r = new Random(m_seed);
      Generation currentGeneration = new Generation(a_graph, a_arch, m_individualsPerGeneration, r);

      //printParameters(a_individualsPerGeneration, a_maxGenerations, a_chanceOfMutation, a_mutationAmplitude, a_eliteIndividualMaxAge, a_tournamentSize);

      // inject the optimal solution in the first generation
        /*Individual specimen = currentGeneration.getIndividual(a_individualsPerGeneration - 1);
        MapperBase.DependencyWeights dw = new MapperBase.DependencyWeights(0.0);
        dw.setWeight(dmDependency.Type.values()[4], 1.0);
        dw.setWeight(dmDependency.Type.values()[1], 1.0);
        specimen.setWeights(dw);*/

      ArrayList<Double> popAverageScore = new ArrayList<>();
      ArrayList<Double> bestIndScore = new ArrayList<>();
      ArrayList<Double> bestPrecision = new ArrayList<>();
      Individual overallBest = currentGeneration.getBest();

      final int mappedNodeCount = a_arch.getMappedNodeCount(a_graph.getNodes());

      for (int g = 0; g < m_maxGenerations; g++) {
         java.lang.System.out.println("Evaluating generation: " + g);
         currentGeneration.eval(m_maxThreads, r);

         printIndividuals(currentGeneration.getIndividuals());

         java.lang.System.out.println("\tAverage score:\t" + currentGeneration.getAverageScore());
         java.lang.System.out.println("\tBest Individual score:\t" + currentGeneration.getBest().getScore());
         popAverageScore.add(currentGeneration.getAverageScore());
         bestIndScore.add((double)currentGeneration.getBest().getScore());
         bestPrecision.add(currentGeneration.getBest().getPrecision());
         //printIndividual(currentGeneration.getBest());
         if (overallBest.getScore() < currentGeneration.getBest().getScore()) {
            overallBest = currentGeneration.getBest();
            if (overallBest.getScore() > 0.99) {
               java.lang.System.out.println("Early break reached score is below 5% of the number of mapped nodes");
               break;
            }
         }

         if (g + 1 < m_maxGenerations) {
            currentGeneration = new Generation(currentGeneration, m_chanceOfMutation, 17, m_eliteIndividualMaxAge, m_tournamentSize, r, mappedNodeCount);
         }
      }
      java.lang.System.out.println("best\tpr\taverage");
      for (int i = 0; i < popAverageScore.size(); i++) {
         java.lang.System.out.println("" + bestIndScore.get(i) + "\t" + bestPrecision.get(i) + "\t" + popAverageScore.get(i));
      }

      java.lang.System.out.println(java.lang.System.lineSeparator() + "Evaluation done." + java.lang.System.lineSeparator() + "Weights for last generation best individual (this is probably the best solution):");
      java.lang.System.out.println("\t" + currentGeneration.getBest().getScore());
      //printIndividual(currentGeneration.getBest());

      java.lang.System.out.println(java.lang.System.lineSeparator() + "Weights for all generations best individual:");
      java.lang.System.out.println("\t" + overallBest.getScore());
      //printIndividual(overallBest);

      //printParameters(a_individualsPerGeneration, a_maxGenerations, a_chanceOfMutation, a_mutationAmplitude, a_eliteIndividualMaxAge, a_tournamentSize);


      return overallBest;
   }

   private void printIndividuals(Iterable<Individual> a_individuals) {
      java.lang.System.out.println("score\tprecision");
      for (Individual i : a_individuals) {
         java.lang.System.out.println(i.getScore() + "\t" + i.getPrecision());
      }
   }
}
