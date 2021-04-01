package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;
import java.util.HashMap;

public class NaiveNameMatcher extends MapperBase {

   public class StringTreeNode {
      String m_str;
      OrphanNode m_leaf;

      HashMap<String, StringTreeNode> m_children;

      StringTreeNode() {
         m_children = new HashMap<>();
      }

      StringTreeNode(String a_str) {
         m_str = a_str;
      }

      StringTreeNode(String a_str, OrphanNode a_leaf) {
         m_str = a_str;
         m_leaf = a_leaf;
      }

      public void add(OrphanNode o) {
         String [] parts = o.get().getFileNameParts();
         add(parts, 0, o);
      }

      private void add(String[] parts, final int partIx, OrphanNode o) {
         String part = parts[partIx];
         if (parts.length == partIx) {
            m_children.put(part, new StringTreeNode(part, o));
         } else {
            StringTreeNode n = m_children.get(part);
            if (n == null) {
               n = new StringTreeNode(part);
            }
            n.add(parts, partIx + 1, o);
         }
      }
   }


   public int m_autoWrong = 0;

   public NaiveNameMatcher(boolean a_doManualMapping, ArchDef a_arch) {
      super(a_doManualMapping, a_arch);
   }

   public void run(CGraph a_g) {
      ArrayList<OrphanNode> unmapped = getOrphanNodes(a_g);

      StringTreeNode root = new StringTreeNode();

      for (OrphanNode o : unmapped) {
         root.add(o);
      }


      for (OrphanNode o : unmapped) {
         naiveNameMapping(o);

         //ArchDef.Component autoClusteredTo = doAutoMapping(o, m_arch, 2.0);
         ArchDef.Component autoClusteredTo = doAutoMappingAbsThreshold(o, m_arch, 0.99);
         //ArchDef.Component autoClusteredTo = doAutoMapping(o, m_arch);   // too restrictive for teammates and sh3d, works well for jabref and argouml
         if (autoClusteredTo != null) {
            addAutoClusteredOrphan(o);
            if (autoClusteredTo != m_arch.getMappedComponent(o.get())) {
               m_autoWrong++;
            }

         } else if (doManualMapping()) {
            boolean clustered = manualMapping(o, m_arch);
            if (clustered == false) {
               m_failedMappings++;
            }
         }
      }
   }



   private void naiveNameMapping(OrphanNode o) {
      double attractions[] = new double[m_arch.getComponentCount()];
      for (int i = 0; i < m_arch.getComponentCount(); i++) {
         attractions[i] = getComplexNameSimilarity(m_arch.getComponent(i).getName(), o.get());
      }

      o.setAttractions(attractions);
   }

   private double getComplexNameSimilarity(String complexName, CNode cNode) {
      double score = 0;
      String [] complexNameParts = complexName.split("\\.");
      if (complexNameParts.length == 1) {
         return getHierarchicalNameSimilarity(complexName, cNode);
      } else {
         for (String part : complexNameParts) {
            score += getHierarchicalNameSimilarity(part, cNode);
         }
      }

      return score;
   }

   // hierarchical
   private double getHierarchicalNameSimilarity(String name, CNode cNode) {
      double score = 0;
      String fileName = cNode.getFileName();
      String [] parts = cNode.getFileNameParts();

      // give a higher score for lower level matches
      for (int pIx = 0; pIx < parts.length; pIx++) {
         String part = parts[pIx];
         // strip file extension for last part
         if (pIx == parts.length - 1) {
            part = part.substring(0, part.lastIndexOf("."));
         }
         if (part.equalsIgnoreCase(name)) {
            score += 1 + pIx;
         }
      }

      return score;
   }

   private double getNameSimilarity(String name, CNode cNode) {
      double score = 0;
      String fileName = cNode.getFileName();

      // just increase score with 1 for every substring match
      while (fileName.indexOf(name) >= 0) {
         score += 1;
         fileName = fileName.substring(fileName.indexOf(name) + name.length());
      }

      return score;
   }
}
