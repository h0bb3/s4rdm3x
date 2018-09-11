package se.lnu.siq.metrics;

public class CSVRow {
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

    // order of this one needs to be synced with the order we parse in (i.e. field order).
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

    public double getMetric(String a_metric) {
        int ix = 2; // first two are just names
        if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_abt;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_typesUsed;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_typesUsingMe;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_rank;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_bcInstr;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_bcCC;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_fields;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_methods;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_children;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_lcom;
        } else if (a_metric.equalsIgnoreCase(g_headers[ix++])) {
            return m_lcomHS;
        }

        return -1;
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

    public String[] getMetricsArray() {
        String[] ret = new String[g_headers.length - 2];
        for (int i = 2; i < g_headers.length; i++) {
            ret[i-2] = g_headers[i];
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
