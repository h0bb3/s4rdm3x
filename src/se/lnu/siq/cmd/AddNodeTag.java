package se.lnu.siq.asm_gs_test.cmd;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

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

    public void run(Graph a_g) {
        AttributeUtil au = new AttributeUtil();

        for (Node n : a_g.getEachNode()) {
            if (m_selection.isSelected(n)) {
                au.addTag(n, m_nodeTag);
            }
        }
    }

}
