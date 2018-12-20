package se.lnu.siq.s4rdm3x.cmd;

import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

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

    public void run(CGraph a_g) {
        AttributeUtil au = new AttributeUtil();
        java.util.ArrayList<CNode> selected = new ArrayList<>();

        for (CNode n : a_g.getNodes(m_selection)) {
            selected.add(n);

        }

        int elementsToRemove = selected.size() - (int)(selected.size() * m_percentToUse);
        if (elementsToRemove >= selected.size()) {
            elementsToRemove = selected.size() - 1;
        }

        while (elementsToRemove > 0) {
            selected.remove(Math.abs(g_r.nextInt()) % selected.size());
            elementsToRemove--;
        }

        for (CNode n : selected) {
            n.addTag(m_nodeTag);
        }

    }
}
