package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.algorithm.PageRank;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.util.ArrayList;

public class Rank extends Metric {

    public String getName() {
        return "Rank";
    }
    public void assignMetric(Iterable<Node> a_nodes) {

        Graph ranks = new MultiGraph("rank_graph");


        for (Node n : a_nodes) {
            ranks.addNode(n.getId());
        }

        AttributeUtil au = new AttributeUtil();

        int edgeId = 0;
        for (Node n : a_nodes) {
            for (String targetId : getTargetIds(n, a_nodes, au)) {
                ranks.addEdge("" + edgeId, n.getId(), targetId, true);
                edgeId++;
            }
        }

        PageRank pageRank = new PageRank();
        //pageRank.setVerbose(true);
        pageRank.setPrecision(0.0000001);
        pageRank.setDampingFactor(0.85);
        pageRank.init(ranks);

        for (Node n : a_nodes) {
            double rank = pageRank.getRank(ranks.getNode(n.getId()));

            setMetric(n, rank);
        }
    }
    public void reassignMetric(Iterable<Node> a_nodes) {

    }

    private Iterable<String> getTargetIds(Node a_source, Iterable<Node> a_nodes, AttributeUtil a_au) {
        ArrayList<String> ret = new ArrayList<>();

        for(dmClass c : a_au.getClasses(a_source)) {
            for (dmDependency d : c.getDependencies()) {
                Node target = findNode(d.getTarget(), a_nodes, a_au);
                if (target !=  null) {
                    for (int i = 0; i < d.getCount(); i++) {
                        ret.add(target.getId());
                    }
                }
            }
        }

        return ret;
    }

    private Node findNode(dmClass a_target, Iterable<Node> a_nodes, AttributeUtil a_au) {
        for (Node n : a_nodes) {
            for (dmClass target : a_au.getClasses(n)) {
                if (a_target == target) {
                    return n;
                }
            }
        }

        return null;
    }

}
