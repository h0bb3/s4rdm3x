package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stemmers.Stemmer;

import java.util.ArrayList;

public abstract class IRMapperBase extends MapperBase {

    protected ArchDef m_arch;

    protected IRMapperBase(ArchDef a_arch, boolean a_doManualMapping) {
        super(a_doManualMapping);
        m_arch = a_arch;
    }

    protected java.util.ArrayList<CNode> getOrphanNodes(CGraph a_g) {

        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (m_arch.getMappedComponent(n) != null && m_arch.getClusteredComponent(n) == null) {
                ret.add(n);
            }
        }

        return ret;
    }

    protected java.util.ArrayList<CNode> getInitiallyMappedNodes(CGraph a_g) {
        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (m_arch.getMappedComponent(n) != null && m_arch.getClusteredComponent(n) != null && m_arch.getClusteredComponent(n).getClusteringType(n) == ArchDef.Component.ClusteringType.Initial) {
                ret.add(n);
            }
        }

        return ret;
    }

    public String deCamelCase(String a_string, int a_minLength, weka.core.stemmers.Stemmer a_stemmer) {
        String ret = "";
        for (int i = 0; i < 10; i++) {
            a_string = a_string.replace("" + i, " ");
        }
        a_string = a_string.replace("_", " ");
        a_string = a_string.replace("-", " ");
        for (String p : a_string.split(" ")) {
            // https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
            for (String w : p.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
                w = w.toLowerCase();
                if (w.length() >= a_minLength && !w.contains("$")) {
                    if (a_stemmer != null ) {
                        w = a_stemmer.stem(w);
                    }

                    if (w.equals("tmp")) {
                        w = "temp";
                    }

                    ret += w + " ";
                }
            }
        }

        return ret.trim();
    }

    protected Stemmer getStemmer() {
        weka.core.stemmers.Stemmer stemmer = null;
        stemmer = new weka.core.stemmers.SnowballStemmer();
        do {
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }
        } while (!((SnowballStemmer) stemmer).stemmerTipText().contains("english"));  // when using multiple threads this is apparently needed...
        return stemmer;
    }
}
