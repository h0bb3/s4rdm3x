package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.algorithm.PageRank;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CNode;


public class Rank extends Metric {

    public String getName() {
        return "Rank";
    }
    public void assignMetric(Iterable<CNode> a_nodes) {

        Graph ranks = new MultiGraph("rank_graph");


        for (CNode n : a_nodes) {
            ranks.addNode(n.getFileName());
        }

        int edgeId = 0;
        for (CNode n : a_nodes) {

            for (CNode m : a_nodes) {
                if (n != m) {
                    for (dmDependency d : n.getDependencies(m)) {
                        for (Integer l : d.lines()) {
                            ranks.addEdge("" + edgeId, n.getFileName(), m.getFileName(), true);
                            edgeId++;
                        }
                    }
                }
            }

        }

        PageRank pageRank = new PageRank();
        //pageRank.setVerbose(true);
        pageRank.setPrecision(0.0000001);
        pageRank.setDampingFactor(0.85);
        pageRank.init(ranks);

        for (CNode n : a_nodes) {
            double rank = pageRank.getRank(ranks.getNode(n.getFileName()));

            n.setMetric(getName(), rank);
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {

    }

    /*private Iterable<String> getTargetIds(CNode a_source, Iterable<CNode> a_nodes) {
        ArrayList<String> ret = new ArrayList<>();

        for(dmClass c : a_source.getClasses()) {
            for (dmDependency d : c.getDependencies()) {
                CNode target = findNode(d.getTarget(), a_nodes);
                if (target !=  null) {
                    for (int i = 0; i < d.getCount(); i++) {
                        ret.add(target.getFileName());
                    }
                }
            }
        }

        return ret;
    }*/

    /*private Node findNode(dmClass a_target, Iterable<Node> a_nodes, AttributeUtil a_au) {
        for (Node n : a_nodes) {
            for (dmClass target : a_au.getClasses(n)) {
                if (a_target == target) {
                    return n;
                }
            }
        }

        return null;
    }*/

}
