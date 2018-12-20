package se.lnu.siq.s4rdm3x.cmd;

public class SetEdgeAttr {
    private String m_key;
    private String m_value;
    private String m_edgeTag;

    private SetEdgeAttr(String a_key, String a_value, String a_edgeTag) {
        m_key = a_key;
        m_value = a_value;
        m_edgeTag = a_edgeTag;
    }

    /*public void run(Graph a_g) {

        AttributeUtil au = new AttributeUtil();
        for (Edge e : a_g.getEachEdge()) {
            if (au.hasAnyTag(e, m_edgeTag)) {
                if (m_value != null) {
                    e.setAttribute(m_key, m_value);
                } else {
                    e.removeAttribute(m_key);
                }
            }
        }
    }*/

}
