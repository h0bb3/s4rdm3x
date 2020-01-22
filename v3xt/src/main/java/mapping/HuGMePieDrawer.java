package mapping;

import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import hiviz.Tree;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

import static java.lang.Math.sin;

public class HuGMePieDrawer  implements Tree.TNodeVisitor {

    class CurvePoints {
        Vec2 m_start;
        Vec2 m_startControl;
        Vec2 m_end;
        Vec2 m_endControl;
        CNode m_node;
    }

    double m_fromAngle, m_toAngle;
    double m_radius;
    double m_innerRadius;

    ImGuiWrapper m_imgui;
    HNode.VisualsManager m_nvm;

    Vec2 m_center;
    Vec2 m_mousePos;
    float m_mousePosAngle;


    public ArrayList<CurvePoints> m_curvePoints = null;

    HuGMePieDrawer(ImGuiWrapper a_imgui, HNode.VisualsManager a_nvm, double a_fromAngle, double a_toAngle, double a_radius, double a_innerRadius, Vec2 a_center, Vec2 a_mousePos, float a_mousePosAngle) {
        m_fromAngle = a_fromAngle;
        m_toAngle = a_toAngle;
        m_radius = a_radius;
        m_curvePoints = new ArrayList<>();
        m_imgui = a_imgui;
        m_nvm = a_nvm;
        m_center = a_center;
        m_innerRadius = a_innerRadius;
        m_mousePos = a_mousePos;
        m_mousePosAngle = a_mousePosAngle;
    }


    protected HuGMePieDrawer(ImGuiWrapper a_imgui, HNode.VisualsManager a_nvm, double a_fromAngle, double a_toAngle, double a_radius, double a_innerRadius, ArrayList<CurvePoints> a_points, Vec2 a_center, Vec2 a_mousePos, float a_mousePosAngle) {
        m_fromAngle = a_fromAngle;
        m_toAngle = a_toAngle;
        m_radius = a_radius;
        m_curvePoints = a_points;
        m_imgui = a_imgui;
        m_nvm = a_nvm;
        m_center = a_center;
        m_innerRadius = a_innerRadius;
        m_mousePos = a_mousePos;
        m_mousePosAngle = a_mousePosAngle;
    }

    @Override
    public void visit(Tree.TNode a_node) {

        final int white = m_imgui.toColor(new Vec4(1., 1., 1., 1));
        final int black = m_imgui.toColor(new Vec4(0., 0., 0., 1));

        final boolean isRoot = a_node.getName() == null;
        double angleSpan = m_toAngle - m_fromAngle;

        class ChildCounterVisitor implements Tree.TNodeVisitor {

            public int m_childCount = -1; // -1 since the first parent node is counted in visit

            @Override
            public void visit(Tree.TNode a_node1) {
                m_childCount += 1;
                for (Tree.TNode c : a_node1.children()) {
                    c.accept(this);
                }
            }
        }

        ChildCounterVisitor childCounter = new ChildCounterVisitor();

        final double angleBorder = isRoot ? 0.0 : 0.0;
        childCounter.visit(a_node);
        double angleDelta = (angleSpan - 2 * angleBorder) / childCounter.m_childCount;


        if (!isRoot) {

            int segments = Math.max(8, childCounter.m_childCount * 8);
            CNode n = null;

            if (m_nvm.hasBGColor(a_node.getFullName())) {
                Vec4 bgColor = m_nvm.getBGColor(a_node.getFullName());
                m_imgui.addFilledCircleSegment(m_center, (float) m_radius, m_imgui.toColor(bgColor), segments, (float) m_fromAngle, (float) m_toAngle);
                m_imgui.addCircleSegment(m_center, (float) m_radius, white, segments, (float) m_fromAngle, (float) m_toAngle, 2);
                m_imgui.addCircleSegment(m_center, (float) m_innerRadius, white, segments, (float) m_fromAngle, (float) m_toAngle, 2);

                Vec2 p1 = new Vec2(Math.cos(m_fromAngle) * m_radius, sin(m_fromAngle) * m_radius).plus(m_center);
                Vec2 p2 = new Vec2(Math.cos(m_toAngle) * m_radius, sin(m_toAngle) * m_radius).plus(m_center);
                Vec2 p3 = new Vec2(Math.cos(m_fromAngle) * m_innerRadius, sin(m_fromAngle) * m_innerRadius).plus(m_center);
                Vec2 p4 = new Vec2(Math.cos(m_toAngle) * m_innerRadius, sin(m_toAngle) * m_innerRadius).plus(m_center);

                m_imgui.addLine(p1, p3, white, 2);
                m_imgui.addLine(p2, p4, white, 2);
            } else {
                // we now have a code node... or the unmapped category...
                n = (CNode) (a_node.getObject());
                if (n != null) {

                    String name = m_imgui.getLongestSubString(n.getLogicName(), (float) (m_radius - m_innerRadius - 10), "\\.");
                    Vec2 textSize = m_imgui.calcTextSize(name, false);

                    float midAngle = (float) (m_fromAngle + (m_toAngle - m_fromAngle) / 2);

                    Vec2 p1 = new Vec2(Math.cos(midAngle) * (m_radius - textSize.getX()), sin(midAngle) * (m_radius - textSize.getX())).plus(m_center);
                    Vec2 textEndPos = m_imgui.text(name, p1, white, (float) midAngle);


                    Vec2 endControl = new Vec2(Math.cos(midAngle) * m_innerRadius, Math.sin(midAngle) * m_innerRadius).plus(m_center);

                    Vec2 start = m_center;

                    Vec2 centerControl = new Vec2(start);

                    if (m_center.getY() > p1.getY()) {
                        centerControl.setY(start.getY() - 150);
                    } else {
                        centerControl.setY(start.getY() + 150);
                    }

                    CurvePoints cp = new CurvePoints();
                    cp.m_start = start;
                    cp.m_end = textEndPos;
                    cp.m_startControl = centerControl;
                    cp.m_endControl = endControl;
                    cp.m_node = n;
                    m_curvePoints.add(cp);

                }
            }

            if (m_mousePosAngle > m_fromAngle && m_mousePosAngle < m_toAngle) {
                if (m_imgui.isInside(m_center, (float) m_radius, m_mousePos) && !m_imgui.isInside(m_center, (float) m_innerRadius, m_mousePos)) {
                    //mouseHoverNode[0] = n;
                    m_imgui.beginTooltip();
                    m_imgui.text(a_node.getFullName());
                    m_imgui.endTooltip();
                }
            }


        }

        double childRadius = a_node.concreteChildCount() == 0 && a_node.childCount() == 1 ? m_radius : m_radius - 20;
        double fromAngle = m_fromAngle + angleBorder;
        for (Tree.TNode c : a_node.children()) {

            childCounter.m_childCount = -1;
            childCounter.visit(c);

            double angle = angleDelta * (childCounter.m_childCount + 1);
            double toAngle = fromAngle + angle;

            c.accept(new HuGMePieDrawer(m_imgui, m_nvm, fromAngle, toAngle, childRadius, m_innerRadius, m_curvePoints, m_center, m_mousePos, m_mousePosAngle));

            fromAngle += angle;
        }
    }
}

