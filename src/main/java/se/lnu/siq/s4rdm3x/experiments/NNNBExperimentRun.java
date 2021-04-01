package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapperEx;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NaiveNameMatcher;

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



      if (m_graph == null){  // first we run the name mapping
         // we need to work on a copy of the graph and not the graph argument
         m_graph = a_g.cloneNodes();

         Iterable<CNode> mappedNodes = arch.getMappedNodes(m_graph.getNodes());

         // remove the initial mapping from the graph and from the data
         arch.cleanNodeClusters(mappedNodes, false);
         m_exData.clearInitialClustering();

         // now we assign the clustering randomly
         mappedNodes.forEach(n -> {
           arch.getComponent(m_rand.nextInt(arch.getComponentCount())).clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
         });

         NBMapper c = new NBMapper(arch, doUseManualMapping(), true, false, true, true, 0, null, m_thresholdValue);
         c.setMappingThreshold(m_thresholdValue);
         c.doStemming(m_exData.m_doStemming);
         c.doWordCount(m_exData.m_doWordCount);

         // this basically needs to run in a genetic optimizer...
         int changedClustering = 0;
         int noClustering = 0;
         do {
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
                  // assign a new random component
                  arch.getComponent(m_rand.nextInt(arch.getComponentCount())).clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
                  noClustering++;
               } else if (newComponent != original) {
                  changedClustering++;
               }
            }
            System.out.println("");
            System.out.println("Changed clusterings: " + changedClustering);
            System.out.println("No clusterings: " + noClustering);
         } while(changedClustering > 10);

         // possibly we should now remove the clustering of all nodes that do not have a definitive clustering.
         // could probably just check the attractions...


         int autoWrong = 0;
         for (CNode n : mappedNodes) {
            if (!n.getClusteringComponentName().equals(n.getMapping())) {
               autoWrong++;
            }
         }

         m_exData.m_totalManuallyClustered += c.m_manuallyMappedNodes;
         m_exData.m_totalAutoWrong  += autoWrong;
         m_exData.m_totalFailedClusterings  += c.m_failedMappings;

         m_exData.m_initialClusteringPercent = c.getAutoClusteredOrphanCount() / (double)m_exData.m_totalMapped;

         // add to auto clustered nodes and initial node set
         c.getAutoClusteredNodes().forEach(n -> {/*m_exData.addAutoClusteredNode(n)*/; m_exData.addInitialClusteredNode(n);});
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

   @Override
   protected ExperimentRun subClone() {
      return new NNNBExperimentRun(doUseManualMapping(), getData(), m_doWordCount, m_threshold);
   }
}
