
package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;

import javax.management.AttributeList;
import java.util.ArrayList;
import java.util.Collection;

public class ArchDef {

    public void setComponentName(Component a_c, String a_name, Iterable<CNode> a_nodesToRemap) {

        // Todo: check so that names are unique
        a_c.m_name = a_name;
        a_c.mapToNodes(a_nodesToRemap);

    }

    public void clear() {
        m_components = new ArrayList<>();
    }

    public void removeComponent(Component a_component) {
        m_components.remove(a_component);
    }

    public int getComponentIx(Component a_component) {
        int ix = 0;
        for (Component c : m_components) {
            if (c == a_component) {
                return ix;
            }
            ix++;
        }
        return -1;
    }

    public Collection<? extends CNode> getClusteredNodes(Iterable<CNode> a_nodes) {
        ArrayList<CNode> ret = new ArrayList<>();

        for (CNode n : a_nodes) {
            Component c = getClusteredComponent(n);
            if (c != null) {
                ret.add(n);
            }
        }

        return ret;
    }

    public Collection<? extends CNode> getClusteredNodes(Iterable<CNode> a_nodes, Component.ClusteringType a_type) {
        ArrayList<CNode> ret = new ArrayList<>();

        for (CNode n : a_nodes) {
            Component c = getClusteredComponent(n);
            if (c != null && c.getClusteringType(n) == a_type) {
                ret.add(n);
            }
        }

        return ret;
    }

   public void addComponent(String a_name, Iterable<String> a_keyWords) {
       Component c = addComponent(a_name);
       a_keyWords.forEach(k -> c.m_keyWords.add(k));
   }

   public static class Component {
      private ArrayList<String> m_keyWords;
      private String m_name;
      private ArrayList<Component> m_allowedDependenciesTo;

      public int getAllowedDependencyCount() {
            return m_allowedDependenciesTo.size();
        }

      public enum ClusteringType {
         None,
         Manual,
         Automatic,
         ManualFailed,
         Initial
      }

      public Component.ClusteringType getClusteringType(CNode a_n) {
         String type = a_n.getClusteringType();
         if (type.contentEquals(Component.ClusteringType.Manual.toString())) {
             return Component.ClusteringType.Manual;
         }
         if (type.contentEquals(Component.ClusteringType.Automatic.toString())) {
             return Component.ClusteringType.Automatic;
         }
         if (type.contentEquals(Component.ClusteringType.ManualFailed.toString())) {
             return Component.ClusteringType.ManualFailed;
         }
         if (type.contentEquals(Component.ClusteringType.Initial.toString())) {
             return Component.ClusteringType.Initial;
         }

         return Component.ClusteringType.None;
      }

      public Component(String a_name) {
         m_name = a_name;
         m_allowedDependenciesTo = new ArrayList<>();
         m_keyWords = new ArrayList<>();
      }

      public Iterable<String> getKeywords() {
         return m_keyWords;
      }

      public void addDependencyTo(Component a_component) {
         if (a_component != this) {
             m_allowedDependenciesTo.add(a_component);
         }
      }

      public void removeDependencyTo(Component a_component) {
      m_allowedDependenciesTo.remove(a_component);
      }

      public boolean allowedDependency(Component a_component) {
         return a_component == this  || m_allowedDependenciesTo.contains(a_component);
      }

      public String getClusterName() {
      return m_name + "_c";
      }

      public String getName() {
      return m_name;
      }

      public void removeClustering(CNode a_n) {
      a_n.setClustering("", "");
      }

      public void clusterToNode(CNode a_n, Component.ClusteringType a_ct) {
         a_n.setClustering(getName(), a_ct.toString());
      }

      public void unmap(CNode a_n) {
      a_n.setMapping("");
      }

      public void mapToNode(CNode a_n) {
      a_n.setMapping(m_name);
      }

      public boolean isMappedTo(CNode a_n) {
      return a_n.getMapping().contentEquals(m_name);
      }

      public boolean isClusteredTo(CNode a_n) {
      return a_n.getClusteringComponentName().contentEquals(m_name);
      }

      public Iterable<CNode> getMappedNodes(Iterable<CNode> a_nodes) {
         ArrayList<CNode> ret = new ArrayList<>();
         for (CNode n : a_nodes) {
             if (isMappedTo(n)) {
                 ret.add(n);
             }
         }
         return ret;
      }

      public void mapToNodes(Iterable<CNode> a_nodesToRemap) {
         for (CNode n : a_nodesToRemap) {
             mapToNode(n);
         }
      }

      public int mapToNodes(CGraph a_g, Selector.ISelector a_selector) {
         int mapped = 0;
         for (CNode n : a_g.getNodes(a_selector)) {
             mapped++;
             mapToNode(n);
         }

         return mapped;
      }
    }

    private ArrayList<Component> m_components = new ArrayList<>();

    public void addComponent(Component a_c) {
        //TODO: check duplicate component names
        m_components.add(a_c);
    }

    public Component addComponent(String a_componentName)
    {
        Component c = new Component(a_componentName);
        addComponent(c);
        return c;
    }

    protected String[] getComponentNames() {
        String [] ret = new String[m_components.size()];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = m_components.get(i).getName();
        }
        return ret;
    }

    protected String[] getClusterNames() {
        String [] ret = new String[m_components.size()];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = m_components.get(i).getClusterName();
        }
        return ret;
    }

    public int getComponentCount() {
        return m_components.size();
    }

    public Component getComponent(int a_ix) {
        return m_components.get(a_ix);
    }
    public Component getComponent(String a_name) {
        for(Component c : m_components) {
            if (c.getName().compareTo(a_name) == 0) {
                return c;
            }
        }

        return null;
    }

    public Iterable<CNode> getUnmappedNodes(Iterable<CNode> a_nodes) {
        ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_nodes) {
            if (n.getMapping().length() == 0) {
                ret.add(n);
            }
        }
        return ret;
    }

    public Iterable<CNode> getMappedNodes(Iterable<CNode> a_nodes) {
        ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_nodes) {
            if (n.getMapping().length() > 0) {
                ret.add(n);
            }
        }
        return ret;
    }

    public void cleanNodeClusters(Iterable<CNode> a_nodes, boolean a_keepInitial) {
        for (CNode n : a_nodes) {
            if (!a_keepInitial || (getClusteredComponent(n) != null && getClusteredComponent(n).getClusteringType(n) != Component.ClusteringType.Initial))
            n.setClustering("", "");
        }
    }

    public Component getClusteredComponent(CNode a_n) {
        for(Component c : m_components) {
            if (c.isClusteredTo(a_n)) {
                return c;
            }
        }
        return null;
    }

    public Component getMappedComponent(CNode a_n) {
        for(Component c : m_components) {
            if (c.isMappedTo(a_n)) {
                return c;
            }
        }
        return null;
    }

    public int getMappedNodeCount(Iterable<CNode> a_nodes) {
        int count = 0;
        for (CNode n : a_nodes) {
            if (getMappedComponent(n) != null) {
                count++;
            }
        }
        return count;
    }

    public int getMappedNodeCount(Iterable<CNode> a_nodes, Component a_to) {
        int count = 0;
        for (CNode n : a_nodes) {
            if (getMappedComponent(n) == a_to) {
                count++;
            }
        }
        return count;
    }

    public int getClusteredNodeCount(Iterable<CNode> a_nodes) {

        int count = 0;
        for (CNode n : a_nodes) {
            Component c = getClusteredComponent(n);
            if (c != null) {
                count++;
            }
        }
        return count;
    }

    public Iterable<Component> getComponents() {
        return m_components;
    }
}
