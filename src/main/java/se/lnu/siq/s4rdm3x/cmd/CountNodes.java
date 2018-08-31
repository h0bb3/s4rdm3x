package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;

/**
 * Created by tohto on 2018-04-27.
 */
public class CountNodes {
    private Selector.ISelector m_selection;
    public int m_count = 0;

    public CountNodes(Selector.ISelector a_selection) {
        m_selection = a_selection;
    }

    public void run(Graph a_g) {
        for (Node n : a_g.getEachNode()) {
            if (m_selection.isSelected(n)) {
                m_count++;
            }
        }
    }
}
