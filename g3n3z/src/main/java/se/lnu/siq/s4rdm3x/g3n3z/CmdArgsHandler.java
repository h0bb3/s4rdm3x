package se.lnu.siq.s4rdm3x.g3n3z;

public class CmdArgsHandler {
    private String [] m_args;

    CmdArgsHandler(String [] a_args) {

        m_args = a_args;
    }

    public String getArgumentString(String a_key) {
        int ix = getNextArgumentIx(a_key);
        if (ix >= 1) {
            return m_args[ix];
        }
        return "";
    }

    public boolean getArgumentBool(String a_key, boolean a_default) {
        for (String s : m_args) {
            if (s.compareTo(a_key) == 0) {
                return true;
            }
        }

        return false;
    }

    private int getNextArgumentIx(String a_key) {
        for (int ix = 0; ix < m_args.length -1; ix++) {
            if (a_key.compareTo(m_args[ix]) == 0) {
                return ix + 1;
            }
        }
        return -1;
    }

    public int getArgumentInt(String a_key, int a_default) {
        int ix = getNextArgumentIx(a_key);
        if (ix >= 1) {
            return Integer.parseInt(m_args[ix]);
        }
        return a_default;
    }

    public int count() {
        return m_args == null ? 0 : m_args.length;
    }

    public double getArgumentDouble(String a_key, double a_default) {
        int ix = getNextArgumentIx(a_key);
        if (ix >= 1) {
            return Double.parseDouble(m_args[ix]);
        }
        return a_default;
    }
}

