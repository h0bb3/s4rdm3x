package se.lnu.siq.s4rdm3x.cmd;

import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Exporter {

    private String m_fileName;
    private Selector.ISelector m_selector;

    public Exporter(String a_fileName, Selector.ISelector a_selector) {
        m_fileName = a_fileName;
        m_selector = a_selector;
    }

    public void run(CGraph a_g) {
        AttributeUtil au = new AttributeUtil();
        BufferedWriter writer = null;
        try {
            File exportFile = new File(m_fileName);
            writer = new BufferedWriter(new FileWriter(exportFile));
            final String newLine = "\r\n";

            for (CNode n : a_g.getNodes(m_selector)) {

                writer.write(n.getFileName() + newLine);
                for (dmClass c : n.getClasses()) {
                    writer.write("\t" + c.getName() + newLine);
                    for (dmDependency d : c.getDependencies()) {
                        writer.write("\t\t" + d.getType() + " " + d.getCount() + " " + d.getTarget().getName() + newLine);
                    }
                }

            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {

                writer.close();
            } catch (Exception e) {
            }
        }
    }
}
