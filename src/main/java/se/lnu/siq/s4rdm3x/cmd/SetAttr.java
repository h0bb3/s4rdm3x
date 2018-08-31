package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;

/**
 * Created by tohto on 2017-09-07.
 */
public class SetAttr {
    Selector.ISelector m_selection;
    String m_key;
    String m_value;

    public SetAttr(String a_key, String a_value, Selector.ISelector a_selection) {
        m_key = a_key;
        m_value = a_value;
        m_selection = a_selection;
    }

    public void run(Graph a_g) {
        for (Node n : a_g.getEachNode()) {
            if (m_selection.isSelected(n)) {
                n.setAttribute(m_key, m_value);
            }
        }
    }
}
