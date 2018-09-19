package se.lnu.siq.metrics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {


    private static CSVRow consolidateRows(ArrayList<CSVRow> a_rows) {
        if (a_rows.size() == 1) {
            return a_rows.get(0);
        }
        CSVRow ret = new CSVRow();
        for (CSVRow r : a_rows) {
            if (!r.isInnerType()) {
                ret.m_fullName = r.m_fullName;
                ret.m_type = r.m_type;
            }

            // use the maximum for the rank.
            if (ret.m_rank < r.m_rank) {
                ret.m_rank = r.m_rank;
            }

            // not sure about these ones
            ret.m_abt += r.m_abt;
            ret.m_typesUsed += r.m_typesUsed;
            ret.m_typesUsingMe += r.m_typesUsingMe;
            ret.m_lcom += r.m_lcom;
            ret.m_lcomHS += r.m_lcomHS;

            // these are size related so just adding would be fine
            ret.m_bcInstr += r.m_bcInstr;
            ret.m_bcCC += r.m_bcCC;
            ret.m_fields += r.m_fields;
            ret.m_methods += r.m_methods;
            ret.m_children += r.m_children;
        }

        return ret;
    }

    public static void main(String[] a_args) {
        if (a_args.length != 1) {
            System.out.println("Supply the file to consolidate...");

        } else {

            try {

                Path fp = Paths.get(a_args[0]);
                ArrayList<CSVRow> rows = new ArrayList<>();
                int [] globalHeader = null;
                int ix = 0;
                List<String> lines = Files.readAllLines(fp);
                for (String line : lines) {
                    String parts[] = line.split(";");
                    if (globalHeader == null) {
                        CSVRow r = new CSVRow();

                        globalHeader = r.getHeaderOrder(parts);
                    } else {
                        CSVRow r = new CSVRow();
                        r.fromStrings(parts, globalHeader);
                        rows.add(r);
                    }
                }


                // we now have all the rows as metrics
                // now we find all rows for every file
                Map<String, ArrayList<CSVRow>> fileRowsMap = new HashMap<>();
                for (CSVRow r : rows) {
                    ArrayList<CSVRow> perFileRows;
                    String filename = r.getFileName();
                    if (fileRowsMap.containsKey(filename)) {
                        perFileRows = fileRowsMap.get(filename);
                    } else {
                        perFileRows = new ArrayList<>();
                        fileRowsMap.put(filename, perFileRows);

                    }
                    perFileRows.add(r);
                }

                // now we consolidate the rows
                Map<String, CSVRow> consolidated = new HashMap<>();
                for (String key : fileRowsMap.keySet()) {
                    consolidated.put(key, consolidateRows(fileRowsMap.get(key)));
                }

                // now we print the rows
                CSVRow r = new CSVRow();
                System.out.println(r.getHeader());
                for (String key : consolidated.keySet()) {
                    System.out.println(consolidated.get(key).toString());
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }


    public static int readFile(int a_ix, List<String> a_rows, String[] a_localHeader, String[] a_globalHeader) {

        int[] global2LocalHeader = new int[a_globalHeader.length];
        for(int ghIx = 0; ghIx < a_localHeader.length; ghIx++) {
            global2LocalHeader[ghIx] = -1;
            for (int lhIx = 0; lhIx < a_localHeader.length; lhIx++) {
                if (a_localHeader[lhIx].compareToIgnoreCase(a_globalHeader[ghIx]) == 0) {
                    global2LocalHeader[ghIx] = lhIx;
                    break;
                }
            }
        }

        for (String line : a_rows) {
            String outline = "" + a_ix;
            a_ix++;
            String[] parts = line.split("\t");
            for(int i = 0; i < global2LocalHeader.length; i++) {

                outline += "\t";
                if (global2LocalHeader[i] != -1) {
                    outline += parts[global2LocalHeader[i]];
                } else {
                    outline += "n/a";
                }
            }
            System.out.println(outline);

        }

        return a_ix;
    }

    public static int readFile(int a_ix, List<String> a_rows, String[] a_globalHeader) {
        String [] localHeader = readHeader(a_rows);
        return readFile(a_ix, a_rows, localHeader, a_globalHeader);
    }

    public static String[] readHeader(List<String> a_rows) {
        String headline = a_rows.get(0);
        a_rows.remove(0);
        String [] header = headline.split("\t");
        return header;
    }
}
