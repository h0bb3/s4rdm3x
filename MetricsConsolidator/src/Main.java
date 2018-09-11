import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.application.Platform.exit;

public class Main {

    //types;Association Between Types (ABT);NbTypesUsed;NbTypesUsingMe;Rank;# ByteCode instructions;ByteCode Cyclomatic Complexity (BCCC);# Fields;# Methods;# Children;Cyclomatic Complexity (CC);Lack of Cohesion Of Methods (LCOM);LCOM Henderson-Sellers (LCOMHS);Level;Full Name
    static class Row {
        String m_type = "";
        String m_fullName = "";
        int m_abt = 0; // Association Between Types
        int m_typesUsed = 0;
        int m_typesUsingMe = 0;
        double m_rank = 0;
        int m_bcInstr = 0;
        int m_bcCC = 0;
        int m_fields = 0;
        int m_methods = 0;
        int m_children = 0;
        double m_lcom = 0;
        double m_lcomHS = 0;

        // order of this one needs to be synched with the order we parse in (i.e. field order).
        private static final String[] g_headers = {"types", "Full Name", "Association Between Types (ABT)", "NbTypesUsed", "NbTypesUsingMe", "Rank", "# ByteCode instructions", "ByteCode Cyclomatic Complexity (BCCC)", "# Fields", "# Methods", "# Children", "Lack of Cohesion Of Methods (LCOM)", "LCOM Henderson-Sellers (LCOMHS)"};

        public static final int g_fieldCount = g_headers.length;

        public void fromStrings(String[] a_data, int[] a_order) {

            if (a_data.length < g_fieldCount || a_order.length != g_fieldCount) {
                throw new IllegalArgumentException("Wrong argument array size, should be: " + g_fieldCount);
            }
            for(int i:a_order) {
                if (i < 0) {
                    throw new IllegalArgumentException("Error in a_order array index found: " + i);
                }
            }

            // remove spaces
            for (int ix = 0; ix < a_data.length; ix++) {
                a_data[ix] = a_data[ix].replace(" ", "");
                a_data[ix] = a_data[ix].replace("N/A", "0");
            }

            {
                int ix = 0;
                m_type = a_data[a_order[ix++]];
                m_fullName = a_data[a_order[ix++]];
                m_abt = Integer.parseInt(a_data[a_order[ix++]]);
                m_typesUsed = Integer.parseInt(a_data[a_order[ix++]]);
                m_typesUsingMe = Integer.parseInt(a_data[a_order[ix++]]);
                m_rank = Double.parseDouble(a_data[a_order[ix++]]);
                m_bcInstr = Integer.parseInt(a_data[a_order[ix++]]);
                m_bcCC = Integer.parseInt(a_data[a_order[ix++]]);
                m_fields = Integer.parseInt(a_data[a_order[ix++]]);
                m_methods = Integer.parseInt(a_data[a_order[ix++]]);
                m_children = Integer.parseInt(a_data[a_order[ix++]]);
                m_lcom = Double.parseDouble(a_data[a_order[ix++]]);
                m_lcomHS = Double.parseDouble(a_data[a_order[ix++]]);
            }
        }

        public String getHeader() {
            String sep = "\t";
            String ret = "";
            for(String h : g_headers) {
                if (ret.length() > 0) {
                    ret += sep;
                }
                ret += h;
            }

            return ret;
        }

        public String toString() {
            String sep = "\t";
            return m_type + sep + m_fullName + sep + m_abt + sep + m_typesUsed + sep + m_typesUsingMe + sep + m_rank  + sep + m_bcInstr  + sep + m_bcCC + sep + m_fields + sep + m_methods + sep + m_children + sep + m_lcom + sep +m_lcomHS;
        }

        private int findInStrings(String[] a_strings, String a_str) {
            for (int ix = 0; ix < a_strings.length; ix++) {
                if (a_strings[ix].equalsIgnoreCase(a_str)) {
                    return ix;
                }
            }

            return -1;
        }

        public boolean isInnerType() {
            return m_fullName.contains("$");
        }

        public String getFileName() {
            String fileName = m_fullName.replace('.', '/') + ".java";
            if (!isInnerType()) {
                return fileName;
            } else {
                return fileName.substring(0, m_fullName.indexOf('$')) + ".java";
            }
        }

        public int[] getHeaderOrder(String[] a_header) {
            int[] order = new int[g_fieldCount];

            for (int ix = 0; ix < g_fieldCount; ix++) {
                order[ix] = findInStrings(a_header, g_headers[ix]);
            }

            return order;
        }
    }

    private static Row consolidateRows(ArrayList<Row> a_rows) {
        if (a_rows.size() == 1) {
            return a_rows.get(0);
        }
        Row ret = new Row();
        for (Row r : a_rows) {
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
                ArrayList<Row> rows = new ArrayList<>();
                int [] globalHeader = null;
                int ix = 0;
                List<String> lines = Files.readAllLines(fp);
                for (String line : lines) {
                    String parts[] = line.split(";");
                    if (globalHeader == null) {
                        Row r = new Row();

                        globalHeader = r.getHeaderOrder(parts);
                    } else {
                        Row r = new Row();
                        r.fromStrings(parts, globalHeader);
                        rows.add(r);
                    }
                }


                // we now have all the rows as metrics
                // now we find all rows for every file
                Map<String, ArrayList<Row>> fileRowsMap = new HashMap<>();
                for (Row r : rows) {
                    ArrayList<Row> perFileRows;
                    if (fileRowsMap.containsKey(r.getFileName())) {
                        perFileRows = fileRowsMap.get(r.getFileName());
                    } else {
                        perFileRows = new ArrayList<>();
                        fileRowsMap.put(r.getFileName(), perFileRows);

                    }
                    perFileRows.add(r);
                }

                // now we consolidate the rows
                Map<String, Row> consolidated = new HashMap<>();
                for (String key : fileRowsMap.keySet()) {
                    consolidated.put(key, consolidateRows(fileRowsMap.get(key)));
                }

                // now we print the rows
                Row r = new Row();
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
