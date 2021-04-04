package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.*;

import java.util.Random;

public class NNNBExperimentRun extends NBMapperExperimentRun {

   private CGraph m_graph;
   private Random m_rand;

   public NNNBExperimentRun(boolean a_doUseManualMapping, Data a_irData, ExperimentRunner.RandomBoolVariable a_doWordCount, ExperimentRunner.RandomDoubleVariable a_threshold) {
      super(a_doUseManualMapping, a_irData, a_doWordCount, a_threshold);
      m_graph = null;
   }

   @Override
   public ExperimentRunData.BasicRunData createNewRunData(Random a_rand) {
      m_graph = null;
      m_rand = a_rand;
      return super.createNewRunData(a_rand);
   }

   @Override
   public boolean runClustering(CGraph a_g, ArchDef arch) {
      if (m_graph == null) {  // first we run the name mapping
         //iterativeClustering(a_g, arch);
         //geneticClustering(a_g, arch);

         m_graph = a_g.cloneNodes();

         Iterable<CNode> mappedNodes = arch.getMappedNodes(m_graph.getNodes());

         // remove the initial mapping from the graph and from the data
         arch.cleanNodeClusters(mappedNodes, false);
         m_exData.clearInitialClustering();

         NBFileMapper mapper = new NBFileMapper(1.99);
         mapper.buildClassifier(arch);

         int tp = 0;
         int fp = 0;
         int fn = 0;
         for (CNode n : mappedNodes) {
            ArchDef.Component suggested = mapper.suggest(n);
            if (suggested != null) {
               suggested.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
               m_exData.addAutoClusteredNode(n);
               if (suggested == arch.getMappedComponent(n)) {
                  tp++;
               } else {
                  fp++;
               }
            } else {
               fn++;
            }
         }

         m_exData.m_totalManuallyClustered = 0;
         m_exData.m_totalAutoWrong = fp;
         m_exData.m_totalFailedClusterings = 0;

         System.out.println("tp: " + tp);
         System.out.println("fp: " + fp);
         System.out.println("precision: " + (double)tp / (double)(tp + fp));
         System.out.println("recall: " + (double)tp / (double)(tp + fn));

         // this actually seems to be good enough for a starting set.
         // we could try to improve further by actually removing some stuff.

         return true;
      } else {

         NBMapper c = new NBMapper(arch, doUseManualMapping(), m_exData.m_doUseCDA, m_exData.m_doUseNodeText, m_exData.m_doUseNodeName, m_exData.m_doUseArchComponentName, m_exData.m_minWordSize, null, m_thresholdValue);
         c.setMappingThreshold(m_thresholdValue);
         c.doStemming(m_exData.m_doStemming);
         c.doWordCount(m_exData.m_doWordCount);
         c.run(m_graph);

         m_exData.m_totalManuallyClustered += c.m_manuallyMappedNodes;
         m_exData.m_totalAutoWrong += c.m_autoWrong;
         m_exData.m_totalFailedClusterings += c.m_failedMappings;

         c.getAutoClusteredNodes().forEach(n -> m_exData.addAutoClusteredNode(n));

         if (c.getAutoClusteredOrphanCount() + c.m_manuallyMappedNodes == 0) {
            return true;
         }
      }

      m_exData.m_iterations++;
      return false;
   }

   private void geneticClustering(CGraph a_g, ArchDef arch) {
      GeneticClustering gc = new GeneticClustering(9, 1000, 9, 3.0/9.0, 10, 3, 171717);
      GeneticClustering.Individual individual = gc.evolve(a_g, arch);

      m_graph = individual.getCGraph();

      // extract the data from the graph
      for (CNode n : arch.getMappedNodes(m_graph.getNodes())) {
         ArchDef.Component clusterComponent = arch.getClusteredComponent(n);
         if (clusterComponent != null) {
            ArchDef.Component mappedComponent = arch.getMappedComponent(n);

            if (clusterComponent != mappedComponent) {
               m_exData.m_totalAutoWrong++;
            }
            m_exData.addAutoClusteredNode(n);
         }
      }
   }

   private void iterativeClustering(CGraph a_g, ArchDef arch) {
      // we need to work on a copy of the graph and not the graph argument
      m_graph = a_g.cloneNodes();

      Iterable<CNode> mappedNodes = arch.getMappedNodes(m_graph.getNodes());

      // remove the initial mapping from the graph and from the data
      arch.cleanNodeClusters(mappedNodes, false);
      m_exData.clearInitialClustering();

      // now we assign the clustering randomly
      final String changedClusteringTag = "changedClustering";
      mappedNodes.forEach(n -> {
        arch.getComponent(m_rand.nextInt(arch.getComponentCount())).clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
         n.setMetric(changedClusteringTag, 0);
      });

      NBMapper c = new NBMapper(arch, doUseManualMapping(), true, false, true, true, 0, null, m_thresholdValue);
      c.setMappingThreshold(m_thresholdValue);
      c.doStemming(m_exData.m_doStemming);
      c.doWordCount(m_exData.m_doWordCount);

      // this basically needs to run in a genetic optimizer...
      int changedClustering = 0;
      int noClustering = 0;
      final int iterations = 10;
      for (int i = 0; i < iterations; i++) {
         changedClustering = 0;
         noClustering = 0;
         for (CNode n : mappedNodes) {
            // make the node an orphan
            ArchDef.Component original = arch.getClusteredComponent(n);
            original.removeClustering(n);
            c.clearAutoClusterings();
            c.run(m_graph);
            System.out.print(".");

            ArchDef.Component newComponent = arch.getClusteredComponent(n);
            if (newComponent ==  null) {
               // no decision made

               if (i < iterations - 1) {
                  // assign a new random component
                  if (m_rand.nextDouble() < 0.33) {
                     arch.getComponent(m_rand.nextInt(arch.getComponentCount())).clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
                  } else {
                     original.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
                  }
               }
               noClustering++;
            } else if (newComponent != original) {
               changedClustering++;
               n.setMetric(changedClusteringTag, n.getMetric(changedClusteringTag) + 1);
            }
         }
         System.out.println("");
         System.out.println("Changed clusterings: " + changedClustering);
         System.out.println("No clusterings: " + noClustering);
      }


      int autoWrong = 0;
      for (CNode n : mappedNodes) {
         if (n.getMetric(changedClusteringTag) > iterations / 4) {
            ArchDef.Component comp = arch.getClusteredComponent(n);
            if (comp != null) {
               comp.removeClustering(n);
            }
         } else {
            //m_exData.addInitialClusteredNode(n);
            m_exData.addAutoClusteredNode(n);
            if (!n.getClusteringComponentName().equals(n.getMapping())) {
               autoWrong++;
            }
         }
      }

      m_exData.m_totalManuallyClustered += c.m_manuallyMappedNodes;
      m_exData.m_totalAutoWrong  += autoWrong;
      m_exData.m_totalFailedClusterings  += c.m_failedMappings;

      m_exData.m_initialClusteringPercent = c.getAutoClusteredOrphanCount() / (double)m_exData.m_totalMapped;

      // add to auto clustered nodes and initial node set
      //c.getAutoClusteredNodes().forEach(n -> {/*m_exData.addAutoClusteredNode(n)*/; m_exData.addInitialClusteredNode(n);});
   }

   @Override
   protected ExperimentRun subClone() {
      return new NNNBExperimentRun(doUseManualMapping(), getData(), m_doWordCount, m_threshold);
   }
}
