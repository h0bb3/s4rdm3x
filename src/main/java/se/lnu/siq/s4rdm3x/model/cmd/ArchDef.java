package se.lnu.siq.s4rdm3x.model.cmd;

import java.util.Map;

public class ArchDef {


    private Map<String, se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef> g_archs;

    public se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef getArchDef(String a_name) {
        return g_archs.get(a_name);
    }

    public void setArchDef(String a_name, se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef a_ad) {
        g_archs.put(a_name, a_ad);
    }

    static class Create {
        private String m_name;

        public Create(String a_adName) {
            m_name = a_adName;
        }

        public void run(ArchDef a_ad) {
            a_ad.setArchDef(m_name, new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef());
        }
    }

    static class AddComponent {
        private String m_adName;
        private String m_cName;

        public AddComponent(String a_adName, String a_cName) {
            m_adName = a_adName;
            m_cName = a_cName;
        }

        public void run(ArchDef a_ad) {
            se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef ad = a_ad.getArchDef(m_adName);
            if (ad != null) {
                ad.addComponent(m_cName);
            }
        }
    }

    static class AddComponentDependency {
        String m_adName;
        String m_fromName;
        String m_toName;

        public void run(ArchDef a_ad) {
            se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef ad = a_ad.getArchDef(m_adName);
            if (ad != null) {
                se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component from = ad.getComponent(m_fromName);
                se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component to = ad.getComponent(m_toName);
                if (from != null && to != null) {
                    from.addDependencyTo(to);
                }
            }
        }
    }

}
