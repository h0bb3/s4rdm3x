package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBFileMapper;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NaiveNameMatcher;

import java.util.Random;

public class NNMapperExperimentRun extends ExperimentRun {
   ExperimentRunData.NaiveNameData m_data;

   public NNMapperExperimentRun(boolean a_doUseManualMapping) {
      super(a_doUseManualMapping);
   }

   @Override
   public ExperimentRunData.BasicRunData createNewRunData(Random m_rand) {
      m_data = new ExperimentRunData.NaiveNameData();
      return m_data;
   }

   @Override
   public boolean runClustering(CGraph a_g, ArchDef arch) {
      CGraph graph = a_g.cloneNodes();

      Iterable<CNode> mappedNodes = arch.getMappedNodes(graph.getNodes());

      // remove the initial mapping from the graph and from the data
      arch.cleanNodeClusters(mappedNodes, false);
      m_data.clearInitialClustering();

      NBFileMapper mapper = new NBFileMapper(1.99);
      mapper.buildClassifier(arch);

      int tp = 0;
      int fp = 0;
      int fn = 0;
      for (CNode n : mappedNodes) {
         ArchDef.Component suggested = mapper.suggest(n);
         if (suggested != null) {
            suggested.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
            m_data.addAutoClusteredNode(n);
            if (suggested == arch.getMappedComponent(n)) {
               tp++;
            } else {
               fp++;
            }
         } else {
            fn++;
         }
      }

      m_data.m_totalManuallyClustered = 0;
      m_data.m_totalAutoWrong = fp;
      m_data.m_totalFailedClusterings = 0;



      return true;
   }

   @Override
   protected ExperimentRun subClone() {
      return new NNMapperExperimentRun(doUseManualMapping());
   }
}
