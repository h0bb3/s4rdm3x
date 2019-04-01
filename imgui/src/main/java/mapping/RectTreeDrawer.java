package mapping;

import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import hiviz.Tree;
import org.jetbrains.annotations.NotNull;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;
import java.util.Comparator;

class RectTreeDrawer implements Tree.TNodeVisitor {
    public Iterable<LeafNodeDrawData> getDrawData() {
        return m_drawData;
    }

    private class TreeSorter implements Comparator<Tree.TNode> {

        private RectTreeDrawer m_fanCalculator;
        private Iterable<CNode> m_targetNodes;

        public TreeSorter(RectTreeDrawer a_fanCalculator, Iterable<CNode> a_targetNodes) {
            m_fanCalculator = a_fanCalculator;
            m_targetNodes = a_targetNodes;
        }

        @Override
        public int compare(Tree.TNode o1, Tree.TNode o2) {
            return getFan(o2) - getFan(o1);
        }

        int getFan(Tree.TNode a_node) {
            int fan = 0;

            if (a_node.childCount() > 0) {
                for (Tree.TNode c : a_node.children()) {
                    fan += getFan(c);
                }
            } else {
                fan = m_fanCalculator.getFan((CNode)a_node.getObject(), m_targetNodes);
            }

            return fan;
        }
    }

    public class LeafNodeDrawData {
        Tree.TNode m_node;
        Vec2 m_topLeft;
        Vec2 m_bottomRight;
        float m_yOffset = 0;    // this is a temporary variable to remember offsets in some cases
        int m_fan;

        public Vec2 calcMiddleRight() {
            Vec2 ret = new Vec2((float)m_bottomRight.getX(), m_topLeft.getY() + getHeight() / 2);

            return ret;
        }

        public Vec2 calcMiddleLeft() {

            Vec2 ret = new Vec2((float)m_topLeft.getX(), m_topLeft.getY() + getHeight() / 2);

            return ret;
        }

        public float getHeight() {
            return m_bottomRight.getY() - m_topLeft.getY();
        }
    }

    public enum Align {
        Left,
        Right,
        Center;
    }

    public enum FanType {
        In,
        Out,
        InOut;
    }

    ImGuiWrapper m_imgui;
    HNode.VisualsManager m_nvm;
    Vec2 m_topLeft;
    float m_width;
    float m_height;
    Iterable<CNode> m_targetNodes;
    private Align m_alignment;
    ArrayList<LeafNodeDrawData> m_drawData;
    FanType m_fanType;


    RectTreeDrawer(ImGuiWrapper a_imgui, HNode.VisualsManager a_nvm, Vec2 a_topLeft, float a_width, Iterable<CNode> a_targetNodes, Align a_alignment, FanType a_fanType) {
        m_nvm = a_nvm;
        m_imgui = a_imgui;
        m_topLeft = new Vec2(a_topLeft);
        m_width = a_width;
        m_height = 0;
        m_targetNodes = a_targetNodes;
        m_alignment = a_alignment;
        m_drawData = new ArrayList<>();
        m_fanType = a_fanType;
    }

    private RectTreeDrawer(ImGuiWrapper a_imgui, HNode.VisualsManager a_nvm, Vec2 a_topLeft, float a_width, Iterable<CNode> a_targetNodes, Align a_alignment, ArrayList<LeafNodeDrawData> a_drawData, FanType a_fanType) {
        m_nvm = a_nvm;
        m_imgui = a_imgui;
        m_topLeft = new Vec2(a_topLeft);
        m_width = a_width;
        m_height = 0;
        m_targetNodes = a_targetNodes;
        m_alignment = a_alignment;
        m_drawData = a_drawData;
        m_fanType = a_fanType;
    }

    int getFan(CNode a_node, Iterable<CNode>a_targets) {
        if (a_node == null) {
            return 0;
        }
        int fanIn = 0;
        int fanOut = 0;

        for (CNode a_target : a_targets) {
            fanOut += a_node.getDependencyCount(a_target);
            fanIn += a_target.getDependencyCount(a_node);
        }

        if (m_fanType == FanType.In) {
            return fanIn;
        } else if (m_fanType == FanType.Out) {
            return fanOut;
        } else {
            return fanIn + fanOut;
        }
    }

    float getHeight(Tree.TNode a_node) {
        float height = 0;

        if (a_node.childCount() == 0) {

            height = m_imgui.getTextLineHeightWithSpacing() + m_imgui.getTextLineHeightWithSpacing() * getFan((CNode)a_node.getObject(), m_targetNodes) / 10.0f;
        } else {
            height += m_imgui.getTextLineHeightWithSpacing();
            for (Tree.TNode n : a_node.children()) {
                height += getHeight(n);
            }
        }
        return height;
    }

    @Override
    public void visit(Tree.TNode a_node) {
        final int white = m_imgui.toColor(new Vec4(1., 1., 1., 1));

        Vec2 pos = new Vec2(m_topLeft);
        Vec2 textOffset = new Vec2(0, 0);

        m_height = getHeight(a_node);

        if (m_nvm.hasBGColor(a_node.getFullName())) {
            m_imgui.addRectFilled(m_topLeft, m_topLeft.plus(new Vec2(m_width, m_height)), m_imgui.toColor(m_nvm.getBGColor(a_node.getFullName())), 0, 0);
        }
        if (a_node.childCount() > 0 && a_node.getName() != null) {
            m_imgui.addRect(m_topLeft, m_topLeft.plus(new Vec2(m_width, m_height)), white, 0, 0, 1);
        }

        if (a_node.getName() != null) {



            final int fan = getFan((CNode)a_node.getObject(), m_targetNodes);
            float oldScale = m_imgui.imgui().getCurrentWindow().getFontWindowScale();
            m_imgui.imgui().setWindowFontScale(oldScale + fan / 10.0f);

            if (a_node.childCount() == 0) {

                if (m_drawData.size() % 2 == 0) {
                    final int lighten = m_imgui.toColor(new Vec4(1., 1., 1., 0.15));

                    m_imgui.addRectFilled(m_topLeft, m_topLeft.plus(new Vec2(m_width, m_height)), lighten, 0, 0);
                }

                Vec2 textSize = m_imgui.calcTextSize(a_node.getName(), false);

                // right align the text
                if (m_alignment == Align.Right) {
                    textOffset.setX(m_width - 5 - textSize.getX());
                } else if (m_alignment == Align.Center) {
                    textOffset.setX((m_width - textSize.getX()) / 2.0f);
                } else {
                    textOffset.setX(5);
                }

                LeafNodeDrawData drawData = new LeafNodeDrawData();
                drawData.m_node = a_node;
                drawData.m_topLeft = m_topLeft;
                drawData.m_bottomRight = m_topLeft.plus(new Vec2(m_width, m_height));
                drawData.m_fan = fan;
                m_drawData.add(drawData);

            } else {
                if (m_alignment == Align.Left) {
                    Vec2 textSize = m_imgui.calcTextSize(a_node.getName(), false);

                    // right align the text
                    textOffset.setX(m_width - 5 - textSize.getX());
                    textOffset.setY(2);
                } else {
                    textOffset.setX(2);
                    textOffset.setY(2);
                }
            }


            m_imgui.addText(pos.plus(textOffset), white, a_node.getName());
            pos.setY(pos.getY() + m_imgui.getTextLineHeightWithSpacing());
            m_imgui.imgui().setWindowFontScale(oldScale);
        }

        a_node.sort(new TreeSorter(this, m_targetNodes));
        for (Tree.TNode c : a_node.children()) {
            final int indent = a_node.getName() != null ? 17 : 0;
            RectTreeDrawer rtd;
            if (m_alignment == Align.Right) {
                rtd = new RectTreeDrawer(m_imgui, m_nvm, pos.plus(new Vec2(indent, 0)), m_width - indent, m_targetNodes, m_alignment, m_drawData, m_fanType);
            } else if (m_alignment == Align.Center) {
                rtd = new RectTreeDrawer(m_imgui, m_nvm, pos, m_width, m_targetNodes, m_alignment, m_drawData, m_fanType);
            } else {
                rtd = new RectTreeDrawer(m_imgui, m_nvm, pos, m_width - indent, m_targetNodes, m_alignment, m_drawData, m_fanType);
            }
            c.accept(rtd);
            pos.setY(pos.getY() + rtd.getHeight());
        }
    }

    public float getHeight() {
        return m_height;
    }
};
