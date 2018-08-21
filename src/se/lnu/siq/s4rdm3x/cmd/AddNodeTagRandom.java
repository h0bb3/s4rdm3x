package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tohto on 2018-04-20.
 */
public class AddNodeTagRandom {

    String m_nodeTag;
    Selector.ISelector m_selection;
    double m_percentToUse;

    public static java.util.Random g_r = new Random(17);

    public AddNodeTagRandom(String a_nodeTag, Selector.ISelector a_selection, double a_percentToUse) {
        m_nodeTag = a_nodeTag;
        m_selection = a_selection;
        m_percentToUse = a_percentToUse;
        if (m_percentToUse < 0.0f) {
            m_percentToUse = 0;
        } else if (m_percentToUse > 1.0) {
            m_percentToUse = 1.0;
        }
    }

    public void run(Graph a_g) {
        AttributeUtil au = new AttributeUtil();
        java.util.ArrayList<Node> selected = new ArrayList<>();

        for (Node n : a_g.getEachNode()) {
            if (m_selection.isSelected(n)) {
                selected.add(n);
            }
        }

        int elementsToRemove = selected.size() - (int)(selected.size() * m_percentToUse);
        if (elementsToRemove >= selected.size()) {
            elementsToRemove = selected.size() - 1;
        }

        while (elementsToRemove > 0) {
            selected.remove(Math.abs(g_r.nextInt()) % selected.size());
            elementsToRemove--;
        }

        for (Node n:selected) {
            au.addTag(n, m_nodeTag);
        }

    }
}
