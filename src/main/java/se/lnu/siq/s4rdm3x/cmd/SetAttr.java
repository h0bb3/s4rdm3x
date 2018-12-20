package se.lnu.siq.s4rdm3x.cmd;

import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

/**
 * Created by tohto on 2017-09-07.
 */
public class SetAttr {
    Selector.ISelector m_selection;
    String m_key;
    String m_value;

    private SetAttr(String a_key, String a_value, Selector.ISelector a_selection) {
        m_key = a_key;
        m_value = a_value;
        m_selection = a_selection;
    }

    public void run(CGraph a_g) {
        for (CNode n : a_g.getNodes(m_selection)) {
            //n.setAttribute(m_key, m_value);
        }
    }
}
