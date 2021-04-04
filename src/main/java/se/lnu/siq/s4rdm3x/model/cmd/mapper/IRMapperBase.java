package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stemmers.Stemmer;

import java.util.ArrayList;


/**
 * Encapsulates basic functions usable for mappers based on information retrieval. Manages the states of what information should be used.
 */
public abstract class IRMapperBase extends MapperBase {

    private boolean m_doUseCDA;
    private boolean m_doUseNodeText;
    private boolean m_doUseNodeName;
    private boolean m_doUseArchComponentName;
    private int m_minWordLength = 3;

    /**
     * @param a_arch The architectural modules and their relations used in the mapping
     * @param a_doManualMapping true manual mapping is to be used
     * @param a_doUseCDA true if Concrete Dependency Attraction texts should be used
     * @param a_doUseNodeText   true if the node (code) text (includes parameter names, variable names, field names, and method names) should be used
     * @param a_doUseNodeName   true if the nodes name (e.g. class name) should be used
     * @param a_doUseArchComponentName  true if the architectural module names should be used
     * @param a_minWordLength   the minimum length of a word
     */
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


    /**
     * @param a_word a word to test
     * @return true of the word is considered a stop word, currenly only: "<init>", "<clinit>", "tmp", "temp"
     */
    boolean isStopWord(String a_word) {
        final String[] stopWords = {"<init>", "<clinit>", "tmp", "temp"};
        for (String stopWord : stopWords) {
            if (a_word.equalsIgnoreCase(stopWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param a_node the node to get ir data from
     * @param a_stemmer the stemmer to use (null if none)
     * @return a string with all ir words from a node (text and name) separated by space, words are de-camel-cased and stemmed
     */
    String getNodeWords(CNode a_node, Stemmer a_stemmer) {
        StringBuilder ret = new StringBuilder();

        if (m_doUseNodeText) {
            for (dmClass c : a_node.getClasses()) {
                for (String t : c.getTexts()) {
                    if (!isStopWord(t)) {
                        ret.append(deCamelCase(t, m_minWordLength, a_stemmer));
                        ret.append(" ");
                    }
                }
            }
        }

        if (m_doUseNodeName) {
            ret.append(deCamelCase(a_node.getLogicName().replace(".", " "), 0, a_stemmer));
        }

        return ret.toString().trim();
    }

    /**
     * @param a_c component to get words from (name and keywords)
     * @param a_stemmer stemmer to use (null if none)
     * @return a list of component words separated by space, de-camel-cased and stemmed
     */
    protected String getArchComponentWords(ArchDef.Component a_c, Stemmer a_stemmer) {
        StringBuilder ret = new StringBuilder();
        if (m_doUseArchComponentName) {
            ret.append(deCamelCase(a_c.getName(), 0, a_stemmer));
            for(String kw : a_c.getKeywords()) {
                ret.append(deCamelCase(kw, 0, a_stemmer));
            }

        }

        return ret.toString();
    }

    /**
     * Gets the CDA words for an orphan, that is hypothetically mapped to a certain module. The CDA words describe the dependencies you would get based on the modules involved
     * @param a_orphan the orphan to get CDA words for
     * @param a_component the module the orphan is hypothetically mapped to
     * @param a_mappedTargets the target nodes i.e. the already mapped nodes
     * @return a string of CDA words separated by space
     */
    protected String getUnmappedCDAWords(OrphanNode a_orphan, ArchDef.Component a_component, Iterable<ClusteredNode> a_mappedTargets) {
        if (m_doUseCDA) {
            return getDependencyStringFromNode(a_orphan.get(), a_component.getName(), a_mappedTargets) + " " + getDependencyStringToNode(a_orphan.get(), a_component.getName(), a_mappedTargets);
        }
        return "";
    }

    /**
     * Gets the CDA words for a mapped node. The CDA words describe the dependencies between the node and all targets on the module level.
     * @param a_source the source node
     * @param a_targets the collection of mapped target nodes
     * @return a string of CDA words separated by space
     */
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
        StringBuilder relations = new StringBuilder();

        for (ClusteredNode from : a_froms) {
            if (a_to != from.get()) {
                for (dmDependency d : from.getDependencies(a_to)) {
                    for (int i = 0; i < d.getCount(); i++) {
                        // to nodes should only be added if different components or there will be double counts
                        if (!from.getClusteringComponentName().equals(a_nodeComponentName)) {
                            relations.append(getComponentComponentRelationString(from.getClusteringComponentName(), d.getType(), a_nodeComponentName));
                            relations.append(" ");//from.getMapping().replace(".", "") + d.getType() + a_nodeComponentName.replace(".", "") + " ";
                        }
                    }
                }
            }
        }

        return relations.toString().trim();

    }

    private String getRelationType(dmDependency.Type a_relationType) {
        return a_relationType.toString();
        /*if (a_relationType == dmDependency.Type.Field) {
            return a_relationType.toString();
        } else if (a_relationType == dmDependency.Type.Extends || a_relationType == dmDependency.Type.Implements) {
            return "InheritsRealizes";
        }
        return "DependsOn";*/
    }

    protected String getComponentComponentRelationString(String a_from, dmDependency.Type a_relation, String a_to) {
        return a_from.replace(".", "") + getRelationType(a_relation) + a_to.replace(".", "");
    }

    private String getDependencyStringFromNode(CNode a_from, String a_nodeComponentName, Iterable<ClusteredNode> a_tos) {
        StringBuilder relations = new StringBuilder();
        for (ClusteredNode to : a_tos) {
            if (!to.equals(a_from)) {
                for (dmDependency d : a_from.getDependencies(to.get())) {
                    for (int i = 0; i < d.getCount(); i++) {
                        relations.append(getComponentComponentRelationString(a_nodeComponentName, d.getType(), to.getClusteringComponentName()));
                        relations.append(" ");
                    }
                }
            }
        }

        return relations.toString().trim();
    }


    /**
     * @param a_string A camel cased string to split into stemmed words above the min length
     * @param a_minLength  word length threshold before stemming
     * @param a_stemmer stemmer to use on word (null if no stemmer)
     * @return a string will all words separated by space
     */
    public String deCamelCase(String a_string, int a_minLength, weka.core.stemmers.Stemmer a_stemmer) {
        StringBuilder ret = new StringBuilder();
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

                    ret.append(w);
                    ret.append(" ");
                }
            }
        }

        return ret.toString().trim();
    }

    /**
     * @return a new instance of the weka SnowballStemmer. This special creation seems to be needed when using snowball in a multithreaded environment.
     */
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
