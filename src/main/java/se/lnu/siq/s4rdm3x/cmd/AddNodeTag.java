package se.lnu.siq.s4rdm3x.cmd;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

/**
 * Created by tohto on 2017-09-06.
 */
public class AddNodeTag {

    String m_nodeTag;
    Selector.ISelector m_selection;

    public AddNodeTag(String a_nodeTag, Selector.ISelector a_selection) {
        m_nodeTag = a_nodeTag;
        m_selection = a_selection;
    }

    public void run(CGraph a_g) {
        for (CNode n : a_g.getNodes(m_selection)) {
            n.addTag(m_nodeTag);
        }
    }

}
