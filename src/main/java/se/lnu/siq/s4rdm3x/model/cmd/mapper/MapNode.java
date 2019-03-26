package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;

import java.util.ArrayList;

public class MapNode {

    public interface IMapNodeListener {
        void preMapNode(CNode a_node, ArchDef.Component a_mapTo);
        void postMapNode(CNode a_node, ArchDef.Component a_mapTo);
    }

    ArchDef.Component m_mapTo;
    Selector.ISelector m_nodeSelector;


    private static ArrayList<IMapNodeListener> g_listeners = new ArrayList<>();

    public static void subscribe(IMapNodeListener a_l) {
        g_listeners.remove(a_l);
        g_listeners.add(a_l);
    }

    public static void unsubscribe(IMapNodeListener a_l) {
        g_listeners.remove(a_l);
    }

    public MapNode(ArchDef.Component a_mapTo, Selector.ISelector a_nodes) {
        m_mapTo = a_mapTo;
        m_nodeSelector = a_nodes;
    }

    public void run(CGraph a_g) {
        String mapTo = "";
        if (m_mapTo != null) {
            mapTo = m_mapTo.getName();
        }

        for (CNode n : a_g.getNodes(m_nodeSelector)) {
            g_listeners.forEach(iMapNodeListener -> iMapNodeListener.preMapNode(n, m_mapTo));
            n.setMapping(mapTo);
            g_listeners.forEach(iMapNodeListener -> iMapNodeListener.postMapNode(n, m_mapTo));
        }
    }
}
