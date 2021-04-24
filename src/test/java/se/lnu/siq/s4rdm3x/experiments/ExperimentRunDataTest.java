package se.lnu.siq.s4rdm3x.experiments;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExperimentRunDataTest {


   @Test
   public void recall_precision_test_1() {
      ExperimentRunData.BasicRunData sut = new ExperimentRunData.BasicRunData();

      sut.addAutoClusteredNode(new CNode("A", 0) );
      sut.addAutoClusteredNode(new CNode("B", 1));
      sut.m_totalAutoWrong = 1;
      sut.m_totalMapped = 2;

      assertEquals(1.0, sut.calcAutoRecall());
      assertEquals(0.5, sut.calcAutoPrecision());
   }

   @Test
   public void recall_precision_test_2() {
      ExperimentRunData.BasicRunData sut = new ExperimentRunData.BasicRunData();

      sut.addAutoClusteredNode(new CNode("A", 0) );
      sut.m_totalAutoWrong = 0;
      sut.m_totalMapped = 2;

      assertEquals(0.5, sut.calcAutoRecall());
      assertEquals(1.0, sut.calcAutoPrecision());
   }
}
