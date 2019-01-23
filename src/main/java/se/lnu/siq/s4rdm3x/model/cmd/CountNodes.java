package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

/**
 * Created by tohto on 2018-04-27.
 */
public class CountNodes {
    private Selector.ISelector m_selection;
    public int m_count = 0;

    public CountNodes(Selector.ISelector a_selection) {
        m_selection = a_selection;
    }

    public void run(CGraph a_g) {

        for (CNode n : a_g.getNodes(m_selection)) {
            m_count++;
        }
    }
}
