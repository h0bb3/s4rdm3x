package se.lnu.siq.s4rdm3x.dmodel;

import java.util.ArrayList;
import java.util.Arrays;

public class dmFile {

   public String getName() {
      return m_name;
   }

   public StringBuilder getFullName(String a_delimiter) {
      if (m_parent != null) {
         return m_parent.getFullName(a_delimiter).append(a_delimiter).append(m_name);
      }

      return new StringBuilder(m_name);
   }

   public static class dmDirectory extends dmFile {
      ArrayList<dmFile> m_files = new ArrayList<>();
      ArrayList<dmDirectory> m_subDirs = new ArrayList<>();


      public dmDirectory(String a_dirName) {
         super(a_dirName, null);
      }

      public dmDirectory(String a_dirName, dmDirectory a_parentDir) {
         super(a_dirName, a_parentDir);
      }

      public dmFile findFile(String[] a_parts) {
         if (a_parts.length == 1) {
            for (dmFile f : m_files) {
               if (f.getName().equals(a_parts[0])) {
                  return f;
               }
            }
            return null;
         } else {
            return findFile(Arrays.copyOfRange(a_parts, 1, a_parts.length));
         }
      }

      public dmFile createFile(String[] a_parts) {
         if (a_parts.length == 1) {
            for (dmFile f : m_files) {
               if (f.getName().equals(a_parts[0])) {
                  return f;
               }
            }
            dmFile f = new dmFile(a_parts[0], this);
            m_files.add(f);
            return f;
         } else {
            dmDirectory parent = null;
            for (dmDirectory d : m_subDirs) {
               if (a_parts[0].equals(d.getName())) {
                  parent = d;
                  break;
               }
            }

            if (parent == null) {
               parent = new dmDirectory(a_parts[0], this);
               m_subDirs.add(parent);
            }

            return parent.createFile(Arrays.copyOfRange(a_parts, 1, a_parts.length));
         }
      }

      @Override
      public dmDirectory getRoot() {
         dmDirectory root = super.getRoot();

         return root == null ? this : root;
      }

      public Iterable<dmFile> getFiles() {
         return m_files;
      }

      public Iterable<dmDirectory> getDirectories() {
         return m_subDirs;
      }

      public int fileCount() {
         return m_files.size();
      }

      public int directoryCount() {
         return m_subDirs.size();
      }
   }

   public dmDirectory getRoot() {
      if (m_parent != null) {
         return m_parent.getRoot();
      }

      return null;
   }

   public dmFile(String a_fileName, dmDirectory a_parentDir) {
      m_name = a_fileName;
      m_parent = a_parentDir;
   }
   protected String m_name;
   protected dmDirectory m_parent;
}
