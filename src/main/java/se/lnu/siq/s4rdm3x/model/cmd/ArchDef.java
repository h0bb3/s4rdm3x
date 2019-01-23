package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.model.cmd.hugme.HuGMe;

import java.util.Map;

public class ArchDef {


    private Map<String, HuGMe.ArchDef> g_archs;

    public HuGMe.ArchDef getArchDef(String a_name) {
        return g_archs.get(a_name);
    }

    public void setArchDef(String a_name, HuGMe.ArchDef a_ad) {
        g_archs.put(a_name, a_ad);
    }

    static class Create {
        private String m_name;

        public Create(String a_adName) {
            m_name = a_adName;
        }

        public void run(ArchDef a_ad) {
            a_ad.setArchDef(m_name, new HuGMe.ArchDef());
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
            HuGMe.ArchDef ad = a_ad.getArchDef(m_adName);
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
            HuGMe.ArchDef ad = a_ad.getArchDef(m_adName);
            if (ad != null) {
                HuGMe.ArchDef.Component from = ad.getComponent(m_fromName);
                HuGMe.ArchDef.Component to = ad.getComponent(m_toName);
                if (from != null && to != null) {
                    from.addDependencyTo(to);
                }
            }
        }
    }

}
