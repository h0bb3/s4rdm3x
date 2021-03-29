package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
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
      NaiveNameMatcher nnm = new NaiveNameMatcher(doUseManualMapping(), arch);

      nnm.run(a_g);

      m_data.m_totalManuallyClustered += nnm.m_manuallyMappedNodes;
      m_data.m_totalAutoWrong  += nnm.m_autoWrong;
      m_data.m_totalFailedClusterings  += nnm.m_failedMappings;

      nnm.getAutoClusteredNodes().forEach(n -> m_data.addAutoClusteredNode(n));

      return true;
   }

   @Override
   protected ExperimentRun subClone() {
      return new NNMapperExperimentRun(doUseManualMapping());
   }
}
