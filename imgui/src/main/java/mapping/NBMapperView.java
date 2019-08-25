package mapping;

import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.Col;
import imgui.WindowFlag;
import imgui.internal.ColumnsFlag;
import imgui.internal.Rect;
import imgui.internal.Window;
import org.w3c.dom.Attr;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunData;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMeManual;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapperManual;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.*;

public class NBMapperView extends MapperBaseView {

    NBMapperManual m_nbmapper;

    //private ArrayList<CNode> m_autoClusteredOrphans = new ArrayList<>();
    private double[] m_probabilityOfClass = null;
    private double m_threshold = 0.90;
    private boolean m_doStemming;
    private boolean m_doWordCount;
    private boolean m_doUseCDA;
    private boolean m_doUseNodeText;
    private boolean m_doUseNodeName;
    private boolean m_doUseArchComponentName;
    private int m_minWordLength;

    private int m_selectedRowIx = -1;
    private String m_selectedNodeName = "";

    public NBMapperView(List<CNode>a_mappedNodes, List<CNode>a_orphanNodes) {
        super(a_mappedNodes, a_orphanNodes);
    }

    public String getSelectedNodeName() {
        return m_selectedNodeName;
    }

    public String resetSelectedNodeName() {
        return m_selectedNodeName = "";
    }

    void doNBMapperParamsView(ImGuiWrapper a_imgui, ArchDef a_arch, HNode.VisualsManager a_nvm, Iterable<CNode>a_system) {

        // first we get the data
        // TODO: these should be fixed based on parameters in the view...
        NBMapper mapper = new NBMapper(null, m_doUseCDA, m_doUseNodeText, m_doUseNodeName, m_doUseArchComponentName, m_minWordLength);

        StringToWordVector filter = (StringToWordVector) mapper.getFilter();
        filter.setOutputWordCounts(m_doWordCount);
        Instances td = mapper.getTrainingData(m_selectedMappedNodes, a_arch, filter, m_doStemming ? new weka.core.stemmers.SnowballStemmer() : null);
        NBMapper.Classifier classifier = new NBMapper.Classifier();
        final boolean doAddRawArchitectureTrainingData = mapper.doAddRawArchitectureTrainingData();


        if (m_selectedMappedNodes.size() > 0 || doAddRawArchitectureTrainingData) {
            try {
                classifier.buildClassifier(td);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Attribute classAttribute = td.classAttribute();




        a_imgui.imgui().beginColumns("doNBMapperParamsView", 2, 0);

        a_imgui.imgui().beginChild("componensdistribution", new Vec2(a_imgui.imgui().getContentRegionAvailWidth(), a_imgui.imgui().getStyle().getFramePadding().getY() * 4 + (a_arch.getComponentCount() + 1) * (a_imgui.getTextLineHeightWithSpacing() + a_imgui.imgui().getStyle().getFramePadding().getY() * 2) ), true, 0);

        if (classifier.getProbabilityOfClass() != null) {
            if (a_imgui.button("Training Data", 0) || m_probabilityOfClass == null || m_probabilityOfClass.length != classifier.getProbabilityOfClass().length) {
                m_probabilityOfClass = new double[classifier.getProbabilityOfClass().length];
                for (int pIx = 0; pIx < classifier.getProbabilityOfClass().length; pIx++) {
                    m_probabilityOfClass[pIx] = classifier.getProbabilityOfClass()[pIx];
                }
            }

            a_imgui.imgui().sameLine(0);

            if (a_imgui.button("Uniform", 0)) {
                for (int pIx = 0; pIx < classifier.getProbabilityOfClass().length; pIx++) {
                    m_probabilityOfClass[pIx] = (float) 1 / classifier.getProbabilityOfClass().length;
                }
            }

            a_imgui.imgui().sameLine(0);

            if (a_imgui.button("System", 0)) {
                int[] nodeCounts = new int[a_arch.getComponentCount()];
                int sum = 0;

                for (CNode n : a_arch.getMappedNodes(a_system)) {
                    nodeCounts[a_arch.getComponentIx(a_arch.getMappedComponent(n))]++;
                    sum++;
                }


                for (int pIx = 0; pIx < classifier.getProbabilityOfClass().length; pIx++) {
                    m_probabilityOfClass[pIx] = (float) nodeCounts[pIx] / (float) sum;
                }
            }

            final float textMaxWidth = a_imgui.imgui().getContentRegionAvailWidth() / 3.0f - 5;
            for (int pIx = 0; pIx < classifier.getProbabilityOfClass().length; pIx++) {
                float[] prob = {(float) m_probabilityOfClass[pIx]};

                if (a_imgui.imgui().sliderFloat(a_imgui.getLongestSubString(td.classAttribute().value(pIx), textMaxWidth, "\\."), prob, 0, 1, "%.2f", 1)) {
                    m_probabilityOfClass[pIx] = prob[0];
                }
            }
        }
        a_imgui.imgui().endChild();


        {
            float [] threshold = {(float)m_threshold};
            if (a_imgui.imgui().sliderFloat("Probability Threshold", threshold, 0, 1, "%.2f", 1)) {
                m_threshold = threshold[0];
            }
        }


        m_doStemming = doCheckbox(a_imgui, "Use Word Stemming", m_doStemming);
        m_doUseCDA = doCheckbox(a_imgui, "Use CDA", m_doUseCDA);
        m_doUseNodeText = doCheckbox(a_imgui, "Use Code Text", m_doUseNodeText);
        m_doUseNodeName = doCheckbox(a_imgui, "Use Code Name", m_doUseNodeName);
        m_doUseArchComponentName = doCheckbox(a_imgui, "Use Architecture Name", m_doUseArchComponentName);

        m_doWordCount = doCheckbox(a_imgui, "Use Word Counts", m_doWordCount);


        if (a_imgui.button("NBMap me Plz", 150)) {
            //m_nbmapper = new NBMapperManual(a_arch, m_probabilityOfClass);
            m_nbmapper = new NBMapperManual(a_arch, null);

            //m_nbmapper.setClusteringThreshold(m_threshold);
            m_nbmapper.run(createGraph());

            setAutoClusteredNodes(m_nbmapper.m_clusteredElements, m_selectedOrphanNodes);


        }

        a_imgui.imgui().nextColumn();

        String[] rigthColHeadlines = new String[td.numAttributes()];
        float longestHeadline = 0;
        for (int attribIx = 0; attribIx < td.numAttributes(); attribIx++) {
            rigthColHeadlines[attribIx] = td.attribute(attribIx).name();
            Vec2 size = a_imgui.calcTextSize(rigthColHeadlines[attribIx], false);
            if (size.getX() > longestHeadline) {
                longestHeadline = size.getX();
            }
        }

        doTrainingDataTable(a_imgui, a_arch, a_nvm, td, classifier, doAddRawArchitectureTrainingData, rigthColHeadlines, (int) longestHeadline);


        a_imgui.imgui().endColumns();
    }

    private boolean doCheckbox(ImGuiWrapper a_imgui, String a_label, boolean a_isSelected) {
        boolean [] bool = {a_isSelected};
        a_imgui.imgui().checkbox(a_label, bool);
        return bool[0];
    }

    private void doTrainingDataTable(ImGuiWrapper a_imgui, ArchDef a_arch, HNode.VisualsManager a_nvm, Instances td, NBMapper.Classifier classifier, boolean doAddRawArchitectureTrainingData, String[] rigthColHeadlines, int longestHeadline) {
        class DataRow {
            public String m_name;
            public String m_mapping;
            public Instance m_data;
        }

        ArrayList<DataRow> rows = new ArrayList<>();
        {
            int iIx = 0;
            if (doAddRawArchitectureTrainingData) {

                for (ArchDef.Component c : a_arch.getComponents()) {
                    DataRow dr = new DataRow();
                    dr.m_name = "Component";
                    dr.m_mapping = c.getName();
                    dr.m_data = td.get(iIx++);
                    rows.add(dr);
                }
            }

            for (CNode n : m_selectedMappedNodes) {
                DataRow dr = new DataRow();
                dr.m_name = n.getLogicName();
                dr.m_mapping = n.getMapping();
                dr.m_data = td.get(iIx++);
                rows.add(dr);
            }
        }

        rows.sort((a, b)->a.m_mapping.compareTo(b.m_mapping));


        final int white = a_imgui.toColor(new Vec4(1, 1, 1, 1));
        final int white15 = a_imgui.toColor(new Vec4(1, 1, 1, 0.15));
        final int rightColCount = td.numAttributes();
        final int heightOffset = longestHeadline;
        final boolean childWindowBorder = false;
        boolean mouseInTable;
        Rect tableRowsClipRect = new Rect();

        Vec2 columnSize = new Vec2(a_imgui.imgui().getColumnWidth(-1) - 10, (float)a_imgui.imgui().getContentRegionAvail().getY());
        if (a_imgui.imgui().beginChild("NBMapperChildTableOuter", columnSize, childWindowBorder, 0)) {
            columnSize.plus(0, heightOffset, columnSize);
            a_imgui.imgui().beginChild("NBMapperChildTableInner", columnSize, childWindowBorder, 0);
            a_imgui.imgui().beginColumns("NBMapperTableOuterColumns", 2, 0);

            a_imgui.imgui().setCursorPosY(a_imgui.imgui().getCursorPosY() + heightOffset);

            //columnSize = new Vec2(a_imgui.imgui().getColumnWidth(0), (float)a_imgui.imgui().getContentRegionAvail().getY());
            columnSize = new Vec2(a_imgui.imgui().getContentRegionAvail());
            a_imgui.imgui().beginChild("NBMapperChildTableLeft", columnSize, childWindowBorder, WindowFlag.NoScrollbar.getI());

            // headline left column
            a_imgui.imgui().beginColumns("NBMapperTableLeftColumns", 2, 0);
            a_imgui.text("Node");
            a_imgui.imgui().nextColumn();
            a_imgui.text("Mapping");

            float [] leftHeadlineColWidths = {a_imgui.imgui().getColumnWidth(0), a_imgui.imgui().getColumnWidth(1)};
            a_imgui.imgui().endColumns();
            a_imgui.imgui().separator();

            // body -21 for the horizontal scrollbar on the right part
            columnSize = new Vec2((float)a_imgui.imgui().getContentRegionAvail().getX(), (float)a_imgui.imgui().getContentRegionAvail().getY() - 21);
            a_imgui.imgui().beginChild("NBMapperChildTableLeftBody", columnSize, childWindowBorder,0);


            columnSize = new Vec2((float)a_imgui.imgui().getContentRegionAvail().getX(), rows.size() * a_imgui.getTextLineHeightWithSpacing());
            final boolean scrollBar = columnSize.getY() > a_imgui.imgui().getContentRegionAvail().getY();
            a_imgui.imgui().beginChild("NBMapperChildTableLeftContents", columnSize, childWindowBorder, WindowFlag.NoScrollbar.getI());

            // mouse hovering table rows stuff
            mouseInTable = a_imgui.imgui().getCurrentWindow().getOuterRectClipped().contains(a_imgui.getMousePos());
            tableRowsClipRect.getTl().setX(a_imgui.imgui().getCurrentWindow().getOuterRectClipped().getTl().getX());

            for (int i = 0; i < rows.size(); i++) {
                DataRow row = rows.get(i);
                a_imgui.imgui().beginColumns("NBMapperTableLeftColumns", 2, ColumnsFlag.NoResize.getI());


                {
                    Vec2 tl = a_imgui.imgui().getCurrentWindow().getPos().plus(a_imgui.imgui().getCursorPos());
                    tl.plus(-10, 0, tl);

                    Vec2 br = tl.plus(leftHeadlineColWidths[0] + 10, a_imgui.getTextLineHeightWithSpacing());

                    if (a_nvm.hasBGColor(row.m_mapping)) {
                        a_imgui.addRectFilled(tl, br, a_imgui.toColor(a_nvm.getBGColor(row.m_mapping)), 0, 0);
                    }

                    renderOverlay(a_imgui, white15, i, m_selectedRowIx, tl, br);
                }

                a_imgui.text(a_imgui.getLongestSubString(row.m_name, leftHeadlineColWidths[0] - 13, "\\."));
                //a_imgui.imgui().selectable(a_imgui.getLongestSubString(row.m_name, leftHeadlineColWidths[0] - 13, "\\."), false, 0, new Vec2(a_imgui.imgui().getColumnWidth(a_imgui.imgui().getColumnIndex()), a_imgui.imgui().getFrameHeightWithSpacing()));
                a_imgui.imgui().setColumnWidth(0, leftHeadlineColWidths[0]);
                a_imgui.imgui().nextColumn();

                {
                    Vec2 tl = a_imgui.imgui().getCurrentWindow().getPos().plus(a_imgui.imgui().getCursorPos());
                    tl.plus(-10, 0, tl);

                    Vec2 br = tl.plus(leftHeadlineColWidths[1] + 10, a_imgui.getTextLineHeightWithSpacing());

                    if (a_nvm.hasBGColor(row.m_mapping)) {
                        a_imgui.addRectFilled(tl, br, a_imgui.toColor(a_nvm.getBGColor(row.m_mapping)), 0, 0);
                    }

                    renderOverlay(a_imgui, white15, i, m_selectedRowIx, tl, br);
                }



                a_imgui.text(a_imgui.getLongestSubString(row.m_mapping, leftHeadlineColWidths[1] - 6 - (scrollBar ? 18 : 0), "\\."));
                a_imgui.imgui().endColumns();
            }

            a_imgui.imgui().endChild();

            final float yScroll = a_imgui.imgui().getScrollY();

            a_imgui.imgui().endChild();

            a_imgui.imgui().endChild();

            a_imgui.imgui().nextColumn();


            a_imgui.imgui().setCursorPosY(a_imgui.imgui().getCursorPosY() + heightOffset);
            a_imgui.imgui().setCursorPosX(a_imgui.imgui().getColumnWidth(0) - 7);   // this seems to fix the rather large offset between the table left and right side parts


            float height = (rows.size() + 1) * a_imgui.getTextLineHeightWithSpacing() + 21;
            columnSize = new Vec2(a_imgui.imgui().getContentRegionAvailWidth(), a_imgui.imgui().getContentRegionAvail().getY() < height ? a_imgui.imgui().getContentRegionAvail().getY() : height);    // +16 for scrollbar
            a_imgui.imgui().beginChild("NBMapperChildTableRight", columnSize, childWindowBorder, WindowFlag.NoScrollbar.or(WindowFlag.AlwaysHorizontalScrollbar));
            Rect rightClipRect = new Rect(a_imgui.imgui().getCurrentWindow().getDrawList().getCurrentClipRect());

            //a_imgui.addRect(rightClipRect.getTl(), rightClipRect.getBr(), white, 0, 0, 2);

            final float colWidth = a_imgui.getTextLineHeightWithSpacing() * 2;
            a_imgui.imgui().beginColumns("NBMapperTableRightColumns", rightColCount, ColumnsFlag.NoResize.getI() | ColumnsFlag.NoForceWithinWindow.getI());
            Vec2 [] rightHeadlinePositions = new Vec2[rightColCount];

            for (int cIx = 0; cIx < rightColCount; cIx++) {
                a_imgui.imgui().setCursorPosY(a_imgui.imgui().getCursorPosY() + a_imgui.getTextLineHeightWithSpacing());
                rightHeadlinePositions[cIx] = a_imgui.imgui().getCurrentWindow().getPos().plus(a_imgui.imgui().getCursorPos()).plus(2 - a_imgui.imgui().getScrollX(), -a_imgui.getTextLineHeightWithSpacing() / 2);
                a_imgui.imgui().setColumnWidth(cIx, colWidth);
                a_imgui.imgui().nextColumn();
            }
            a_imgui.imgui().endColumns();

            a_imgui.imgui().separator();

            columnSize = new Vec2(rightColCount * colWidth, (float)a_imgui.imgui().getContentRegionAvail().getY());
            a_imgui.imgui().beginChild("NBMapperChildTableRightBody", columnSize, childWindowBorder,WindowFlag.NoScrollbar.getI());

            columnSize = new Vec2(rightColCount * colWidth,rows.size() * a_imgui.getTextLineHeightWithSpacing() );
            a_imgui.imgui().beginChild("NBMapperChildTableRightBodyContents", columnSize, childWindowBorder, WindowFlag.NoScrollbar.getI());



            int clipped = 0;

            int startRowIx, maxRows;
            float rowHeight = a_imgui.getTextLineHeightWithSpacing();
            Vec2 windowPos = a_imgui.imgui().getCurrentWindow().getPos();

            startRowIx = (int)((rightClipRect.getTl().getY() - windowPos.getY()) / rowHeight);
            if (startRowIx < 0) {
                startRowIx = 0;
            }
            maxRows = startRowIx + (int)(rightClipRect.getHeight() / rowHeight) + 1;
            if (maxRows > rows.size()) {
                maxRows = rows.size();
            }


            int startColIx = 0;
            int maxCols = rightColCount;
            startColIx = (int)((rightClipRect.getTl().getX() - windowPos.getX()) / colWidth);
            if (startColIx < 0) {
                startColIx = 0;
            }

            maxCols = startColIx + (int)(rightClipRect.getWidth() / colWidth) + 2;
            if (maxCols > rightColCount) {
               maxCols = rightColCount;
            }

            /*a_imgui.beginTooltip();
            a_imgui.text("Start row Ix: " + startRowIx);
            a_imgui.text("maxRows: " + maxRows);

            a_imgui.text("Start col Ix: " + startColIx);
            a_imgui.text("maxCols: " + maxCols);
            a_imgui.endTooltip();*/

            Vec2 wndTlScreen = a_imgui.imgui().getCurrentWindow().getPos();
            a_imgui.imgui().setCursorPosY(startRowIx * rowHeight);



            for (int i = startRowIx; i < maxRows; i++) {
                DataRow row = rows.get(i);
                Instance inst = row.m_data;
                //a_imgui.imgui().beginColumns("NBMapperTableRightColumns", rightColCount, ColumnsFlag.NoResize.getI() | ColumnsFlag.NoForceWithinWindow.getI());


                for (int cIx = startColIx; cIx < maxCols; cIx++) {
                    a_imgui.imgui().setCursorPosX(cIx * colWidth);
                    a_imgui.imgui().setCursorPosY(i * rowHeight);
                    boolean isInside = true;
                    {
                        Vec2 tl = a_imgui.imgui().getCurrentWindow().getPos().plus(a_imgui.imgui().getCursorPos());
                        tl.plus(0, 0, tl);

                        Vec2 br = tl.plus(colWidth, a_imgui.getTextLineHeightWithSpacing());

                        isInside = true;//rightClipRect.contains(tl) || rightClipRect.contains(br);

                        if (isInside) {

                            if (a_nvm.hasBGColor(row.m_mapping)) {
                               a_imgui.addRectFilled(tl, br, a_imgui.toColor(a_nvm.getBGColor(row.m_mapping)), 0, 0);
                            }

                            renderOverlay(a_imgui, white15, i, m_selectedRowIx, tl, br);

                        } else {
                            clipped++;
                        }
                    }

                    int count = (int)inst.value(cIx);

                    if (isInside && count > 0) {
                        a_imgui.text("" + count);
                    }
                    //a_imgui.imgui().sameLine((cIx + 1 - startRowIx) * colWidth, 0);
                    //a_imgui.imgui().setColumnWidth(cIx, colWidth);
                    //a_imgui.imgui().nextColumn();
                }
                a_imgui.imgui().newLine();
                //a_imgui.imgui().endColumns();
            }

            a_imgui.imgui().setCursorPosY(maxRows * rowHeight);

            /*a_imgui.beginTooltip();
            a_imgui.text("Clipped: " + clipped);
            a_imgui.text("window pos" + a_imgui.imgui().getCurrentWindow().getPos());
            a_imgui.endTooltip();*/




            Window w = a_imgui.imgui().getCurrentWindow();
            final float contentScreenY = w.getInnerClipRect().getMin().getY();
            if (!mouseInTable) {

                mouseInTable = w.getOuterRectClipped().contains(a_imgui.getMousePos());
            }
            tableRowsClipRect.getTl().setY(w.getOuterRectClipped().getTl().getY());
            tableRowsClipRect.getBr().setX(w.getOuterRectClipped().getBr().getX());
            tableRowsClipRect.getBr().setY(w.getOuterRectClipped().getBr().getY());



            a_imgui.imgui().endChild();


            a_imgui.imgui().setScrollY(yScroll);

            a_imgui.imgui().endChild();



            a_imgui.imgui().endChild();

            if (mouseInTable && a_imgui.isCurrentWindowInFocus()) {

                // now we need to find the row index...
                float mouseY = a_imgui.getMousePos().getY();
                int rowIx = (int)((mouseY - contentScreenY) / rowHeight);
                m_selectedRowIx = rowIx;
                if (a_imgui.isMouseClicked(0, false)) {
                    if (m_selectedRowIx >= 0 && m_selectedRowIx < rows.size())
                    m_selectedNodeName = rows.get(m_selectedRowIx).m_name;
                }
            } else {
                m_selectedRowIx = -1;
            }

            // we now draw the slanted headlines to avoid column and child window clipping
            {
                // we do accept some overdraw to avoid early clipping of texts
                final float angle = (float) (2 * Math.PI - Math.PI / 2.4);
                Vec2 maxTo = new Vec2(Math.cos(angle) * 250, Math.sin(angle) * 250);
                int slantedTExtColor = a_imgui.toColor(a_imgui.imgui().getStyleColorVec4(Col.Text));

                for (int hIx = 0; hIx < rightColCount; hIx++) {

                    // early bail if we are outside the window
                    Vec2 clipTo = maxTo.plus(rightHeadlinePositions[hIx]);

                    //a_imgui.addLine(rightHeadlinePositions[hIx], clipTo, white, 1);

                    if (a_imgui.isInsideClipRect(rightHeadlinePositions[hIx]) || a_imgui.isInsideClipRect(clipTo)) {
                        final float textLength = a_imgui.calcTextSize(rigthColHeadlines[hIx], false).getX();
                        Vec2 to = new Vec2(Math.cos(angle) * textLength, Math.sin(angle) * textLength);
                        to.plus(rightHeadlinePositions[hIx], to);
                        to.plus(5, 0, to);
                        a_imgui.text(rigthColHeadlines[hIx], rightHeadlinePositions[hIx], slantedTExtColor, angle);

                        if (a_imgui.isInsideClipRect(a_imgui.getMousePos()) && a_imgui.isInside(rightHeadlinePositions[hIx], to, a_imgui.getTextLineHeightWithSpacing() / 2.0, a_imgui.getMousePos())) {
                            a_imgui.beginTooltip();
                            a_imgui.text(rigthColHeadlines[hIx]);

                            for (int cIx = 0; cIx < a_arch.getComponentCount(); cIx++) {
                                a_imgui.text(a_arch.getComponent(cIx).getName() + ":" + weka.core.Utils.doubleToString(classifier.getProbabilityOfWord(hIx, cIx), 2));
                            }

                            a_imgui.endTooltip();

                            //System.out.println(classifier);
                        }
                    }
                    //a_imgui.addLine(rightHeadlinePositions[hIx], to, white, 1);
                }
            }
            a_imgui.imgui().endColumns();


            a_imgui.imgui().endChild();
            a_imgui.imgui().endChild();
        } else {
            m_selectedRowIx = -1;
        }
    }

    private void renderOverlay(ImGuiWrapper a_imgui, int a_color, int a_rowIx, int a_selectedRowIx, Vec2 a_cellTL, Vec2 a_cellBR) {
        if (a_rowIx % 2 == 0) {
            if (a_selectedRowIx == a_rowIx) {
                a_imgui.addRectFilled(a_cellTL, a_cellBR, a_color, 0, 0);
            }
            a_imgui.addRectFilled(a_cellTL, a_cellBR, a_color, 0, 0);
        } else if (a_selectedRowIx == a_rowIx) {
            a_imgui.addRectFilled(a_cellTL, a_cellBR, a_color, 0, 0);
            a_imgui.addRectFilled(a_cellTL, a_cellBR, a_color, 0, 0);
        }
    }


    public void setInitialParameters(ExperimentRunData.NBMapperData a_data) {
        m_threshold = a_data.m_threshold;
        m_doStemming = a_data.m_doStemming;
        m_doWordCount = a_data.m_doWordCount;
        m_doUseCDA = a_data.m_doUseCDA;
        m_doUseNodeText = a_data.m_doUseNodeText;
        m_doUseNodeName = a_data.m_doUseNodeName;
        m_doUseArchComponentName = a_data.m_doUseArchComponentName;
        m_minWordLength = a_data.m_minWordSize;
    }
}
