package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stemmers.Stemmer;

import java.util.ArrayList;

public abstract class IRMapperBase extends MapperBase {

    private boolean m_doUseCDA;
    private boolean m_doUseNodeText;
    private boolean m_doUseNodeName;
    private boolean m_doUseArchComponentName;
    private int m_minWordLength = 3;

    protected IRMapperBase(ArchDef a_arch, boolean a_doManualMapping, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength) {
        super(a_doManualMapping, a_arch);

        m_doUseCDA = a_doUseCDA;
        m_doUseNodeText = a_doUseNodeText;
        m_doUseNodeName = a_doUseNodeName;
        m_doUseArchComponentName = a_doUseArchComponentName;
        m_minWordLength = a_minWordLength;
    }

    boolean doUseCDA() {
        return m_doUseCDA;
    }

    boolean doUseNodeText() {
        return m_doUseNodeText;
    }

    boolean doUseNodeName() {
        return m_doUseNodeName;
    }

    boolean doUseArchComponentName() {
        return m_doUseArchComponentName;
    }

    boolean isStopWord(String a_word) {
        final String[] stopWords = {"<init>", "<clinit>", "tmp", "temp"};
        for (String stopWord : stopWords) {
            if (a_word.equalsIgnoreCase(stopWord)) {
                return true;
            }
        }
        return false;
    }

    String getNodeWords(CNode a_node, Stemmer a_stemmer) {
        String ret = "";
        if (m_doUseNodeText) {
            for (dmClass c : a_node.getClasses()) {
                for (String t : c.getTexts()) {
                    if (!isStopWord(t)) {
                        ret += deCamelCase(t, m_minWordLength, a_stemmer) + " ";
                    }
                }
            }
        }

        if (m_doUseNodeName) {
            ret += deCamelCase(a_node.getLogicName().replace(".", " "), 0, a_stemmer);
        }

        ret = ret.trim();

        return ret;
    }

    protected String getArchComponentWords(ArchDef.Component a_c, Stemmer a_stemmer) {
        String ret = "";
        if (m_doUseArchComponentName) {
            ret = deCamelCase(a_c.getName(), 0, a_stemmer);
        }

        return ret;
    }

    protected String getUnmappedCDAWords(OrphanNode a_orphan, ArchDef.Component a_component, Iterable<ClusteredNode> a_mappedTargets) {
        if (m_doUseCDA) {
            return getDependencyStringFromNode(a_orphan.get(), a_component.getName(), a_mappedTargets) + " " + getDependencyStringToNode(a_orphan.get(), a_component.getName(), a_mappedTargets);
        }
        return "";
    }

    protected String getMappedCDAWords(ClusteredNode a_source, Iterable<ClusteredNode>a_targets) {
        if (m_doUseCDA) {
            return getDependencyStringFromNode(a_source.get(), a_targets) + " " + getDependencyStringToNode(a_source.get(), a_targets);
        }
        return "";
    }

    private String getDependencyStringToNode(CNode a_to, Iterable<ClusteredNode> a_froms) {
        return getDependencyStringToNode(a_to, a_to.getClusteringComponentName(), a_froms);
    }

    private String getDependencyStringFromNode(CNode a_from, Iterable<ClusteredNode>a_tos) {
        return getDependencyStringFromNode(a_from, a_from.getClusteringComponentName(), a_tos);
    }

    private String getDependencyStringToNode(CNode a_to, String a_nodeComponentName, Iterable<ClusteredNode> a_froms) {
        String relations = "";
        for (ClusteredNode from : a_froms) {
            if (a_to != from.get()) {
                for (dmDependency d : from.getDependencies(a_to)) {
                    for (int i = 0; i < d.getCount(); i++) {
                        relations += getComponentComponentRelationString(from.getClusteringComponentName(), d.getType(), a_nodeComponentName) + " ";//from.getMapping().replace(".", "") + d.getType() + a_nodeComponentName.replace(".", "") + " ";
                    }
                }
            }
        }

        relations = relations.trim();

        return relations;

    }

    protected String getRelationType(dmDependency.Type a_relationType) {
        if (a_relationType == dmDependency.Type.Field) {
            return a_relationType.toString();
        } else if (a_relationType == dmDependency.Type.Extends || a_relationType == dmDependency.Type.Implements) {
            return "InheritsRealizes";
        }
        return "DependsOn";
    }

    protected String getComponentComponentRelationString(String a_from, dmDependency.Type a_relation, String a_to) {
        return a_from.replace(".", "") + getRelationType(a_relation) + a_to.replace(".", "");
    }

    private String getDependencyStringFromNode(CNode a_from, String a_nodeComponentName, Iterable<ClusteredNode> a_tos) {
        String relations = "";
        for (ClusteredNode to : a_tos) {
            if (!to.equals(a_from)) {
                for (dmDependency d : a_from.getDependencies(to.get())) {
                    for (int i = 0; i < d.getCount(); i++) {
                        relations += getComponentComponentRelationString(a_nodeComponentName, d.getType(), to.getClusteringComponentName()) + " ";
                    }
                }
            }
        }

        relations = relations.trim();

        return relations;
    }


    public String deCamelCase(String a_string, int a_minLength, weka.core.stemmers.Stemmer a_stemmer) {
        String ret = "";
        for (int i = 0; i < 10; i++) {
            a_string = a_string.replace("" + i, " ");
        }
        a_string = a_string.replace("_", " ");
        a_string = a_string.replace("-", " ");
        a_string = a_string.replace(".", " ");
        for (String p : a_string.split(" ")) {
            // https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
            for (String w : p.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
                w = w.toLowerCase();
                if (w.length() >= a_minLength && !w.contains("$")) {
                    if (a_stemmer != null ) {
                        w = a_stemmer.stem(w);
                    }

                    ret += w + " ";
                }
            }
        }

        return ret.trim();
    }

    protected synchronized Stemmer getStemmer() {
        SnowballStemmer stemmer = null;


        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {}

        stemmer = new weka.core.stemmers.SnowballStemmer();

        do {
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }
        } while (!stemmer.stemmerTipText().contains("english"));  // when using multiple threads this is apparently needed...
        return stemmer;
    }
}
