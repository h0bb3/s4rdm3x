package se.lnu.siq.s4rdm3x.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;

public class CSVFile {

    private Path m_fp;
    private int m_headerCount = 0;

    public CSVFile(Path a_fp) {
        m_fp = Paths.get(a_fp.toUri());
    }

    public void setHeaderCount() throws IOException {
        m_headerCount = getHeader().size();
    }

    private class TxtRow {
        public String m_txtRow;
        public int m_cellCount = 0;

        TxtRow(Iterable<String> a_strings) {
            m_txtRow = "";
            for (String s : a_strings) {
                m_txtRow += s + "\t";
                m_cellCount++;
            }
            m_txtRow = m_txtRow.substring(0, m_txtRow.length() - 1);
            m_txtRow += "\r\n";
        }
    }

    public void writeHeader(Iterable<String> a_strings)  throws IOException {
        if (m_headerCount != 0) {
            throw new IllegalArgumentException("Header already written.");
        }

        TxtRow tr = new TxtRow(a_strings);
        m_headerCount = tr.m_cellCount;
        if (m_headerCount == 0) {
            throw new IllegalArgumentException("Header empty.");
        }

        write(m_fp, tr.m_txtRow.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
    }

    public void writeRows(Iterable<Iterable<String>> a_strings) throws IOException {
        String fatString = "";

        for (Iterable<String> strings : a_strings) {
            TxtRow tr = new TxtRow(strings);
            if (tr.m_cellCount != m_headerCount) {
                throw new IllegalArgumentException("Amount of strings do not match the amount of headers: " + tr.m_cellCount + " !=  " + m_headerCount);
            }
            fatString += tr.m_txtRow;
        }

        // remove last rowbreak
        fatString = fatString.substring(0, fatString.length() - 1);
        write(m_fp, fatString.getBytes(), StandardOpenOption.APPEND);
    }

    public void writeRow(Iterable<String> a_strings) throws IOException {

        TxtRow tr = new TxtRow(a_strings);

        if (tr.m_cellCount != m_headerCount) {
            throw new IllegalArgumentException("Amount of strings do not match the amount of headers: " + tr.m_cellCount + " !=  " + m_headerCount);
        }

        write(m_fp, tr.m_txtRow.getBytes(), StandardOpenOption.APPEND);
    }

    public ArrayList<String> getHeader() throws IOException {
        List<String> lines = readAllLines(m_fp);
        String header = lines.get(0);


        return getRow(lines.get(0));

    }

    private ArrayList<String> getRow(String a_row) {
        String[] strings = a_row.split("\\t");
        ArrayList<String> ret = new ArrayList<>();

        for (int i = 0; i < strings.length; i++) {
            ret.add(strings[i].trim());
        }

        return ret;
    }
}
