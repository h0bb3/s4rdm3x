package archviz;

import glm_.vec2.Vec2;
import imgui.ImGui;
import imgui.WindowFlag;

import static imgui.ImguiKt.COL32;

class ImGuiWrapper {
    public ImGui m_imGui;

    private static class DashContext {
        float m_remainingDrawLength;
        int m_ix;
    }

    public void addDashedCircle(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_thickness, float a_holeLength, float a_dashlength) {
        DashContext dc = new DashContext();
        double segmentStep = 2 * Math.PI / a_segments;
        double angle = 0;

        for (int segment = 0; segment < a_segments; segment++) {
            float x1 = (float)Math.cos(angle) * a_radius;
            float y1 = (float)Math.sin(angle) * a_radius;
            angle += segmentStep;

            float x2 = (float)Math.cos(angle) * a_radius;
            float y2 = (float)Math.sin(angle) * a_radius;

            Vec2 p1 = new Vec2(x1, y1).plus(a_center);
            Vec2 p2 = new Vec2(x2, y2).plus(a_center);

            addDashedLine(p1, p2, a_color, a_thickness, a_holeLength, a_dashlength, dc);
        }

        //m_imGui.getWindowDrawList().addCircle(a_center, a_radius, a_color, a_segments, a_thickness);
    }

    public void addDashedCircleSegment(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_startAngle, float a_endAngle, float a_thickness, float a_holeLength, float a_dashlength) {
        addDashedCircleSegment(a_center, a_radius, a_color, a_segments, a_startAngle, a_endAngle, a_thickness, a_holeLength, a_dashlength, new DashContext());
    }

    public void addDashedCircleSegment(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_startAngle, float a_endAngle, float a_thickness, float a_holeLength, float a_dashlength, DashContext a_dc) {
        double segmentStep = (a_endAngle - a_startAngle) / a_segments;
        double angle = a_startAngle;

        for (int segment = 0; segment < a_segments; segment++) {
            float x1 = (float)Math.cos(angle) * a_radius;
            float y1 = (float)Math.sin(angle) * a_radius;
            angle += segmentStep;

            float x2 = (float)Math.cos(angle) * a_radius;
            float y2 = (float)Math.sin(angle) * a_radius;

            Vec2 p1 = new Vec2(x1, y1).plus(a_center);
            Vec2 p2 = new Vec2(x2, y2).plus(a_center);

            addDashedLine(p1, p2, a_color, a_thickness, a_holeLength, a_dashlength, a_dc);
        }
    }

    public void addDashedRect(Vec2 a_tl, Vec2 a_br, int a_color, float a_thickness, float a_holeLength, float a_dashLength, float a_rounding) {
        Vec2 bl = new Vec2(a_tl.getX(), a_br.getY());
        Vec2 tr = new Vec2(a_br.getX(), a_tl.getY());
        Vec2 dX = new Vec2(a_rounding, 0);
        Vec2 dY = new Vec2(0, a_rounding);

        final int segments = (int)(a_rounding / 2);

        DashContext dc = new DashContext();

        addDashedLine(a_tl.plus(dX), tr.minus(dX), a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedCircleSegment(tr.minus(dX).plus(dY), a_rounding, a_color, segments, (float)Math.PI / 2 * 3, (float)Math.PI * 2, a_thickness, a_holeLength, a_dashLength, dc);

        addDashedLine(tr.plus(dY), a_br.minus(dY), a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedCircleSegment(a_br.minus(dX).minus(dY), a_rounding, a_color, segments, 0, (float)Math.PI / 2 * 1, a_thickness, a_holeLength, a_dashLength, dc);

        addDashedLine(a_br.minus(dX), bl.plus(dX), a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedCircleSegment(bl.plus(dX).minus(dY), a_rounding, a_color, segments, (float)Math.PI / 2 * 1, (float)Math.PI / 2 * 2, a_thickness, a_holeLength, a_dashLength, dc);

        addDashedLine(bl.minus(dY), a_tl.plus(dY), a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedCircleSegment(a_tl.plus(dX).plus(dY), a_rounding, a_color, segments, (float)Math.PI / 2 * 2, (float)Math.PI / 2 * 3, a_thickness, a_holeLength, a_dashLength, dc);


        //m_imGui.getWindowDrawList().addRect(a_tl, a_br, a_color, a_rounding, a_corners, a_thickness);
    }

    public void addDashedRect(Vec2 a_tl, Vec2 a_br, int a_color, float a_thickness, float a_holeLength, float a_dashLength) {
        Vec2 bl = new Vec2(a_tl.getX(), a_br.getY());
        Vec2 tr = new Vec2(a_br.getX(), a_tl.getY());

        DashContext dc = new DashContext();

        addDashedLine(a_tl, tr, a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedLine(tr, a_br, a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedLine(a_br, bl, a_color, a_thickness, a_holeLength, a_dashLength, dc);
        addDashedLine(bl, a_tl, a_color, a_thickness, a_holeLength, a_dashLength, dc);


        //m_imGui.getWindowDrawList().addRect(a_tl, a_br, a_color, a_rounding, a_corners, a_thickness);
    }

    public void addDashedLine(Vec2 a_p1, Vec2 a_p2, int a_color, float a_thickness, float a_holeLength, float a_dashlength, DashContext a_dc) {
        Vec2 dir = a_p2.minus(a_p1);
        float lineLength = dir.length();
        dir.div(lineLength, dir);

        float drawnLength = 0;

        // draw the remaining part of the previous command
        if (a_dc.m_remainingDrawLength > 0) {
            if(a_dc.m_ix % 2 == 0) {
                Vec2 p1 = a_p1.plus(dir.times(drawnLength));
                drawnLength += a_dc.m_remainingDrawLength;
                if (drawnLength < lineLength) {
                    Vec2 p2 = a_p1.plus(dir.times(drawnLength));
                    m_imGui.getWindowDrawList().addLine(p1, p2, a_color, a_thickness);
                    a_dc.m_ix++;
                } else {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    m_imGui.getWindowDrawList().addLine(p1, a_p2, a_color, a_thickness);
                    return;
                }
            } else {
                drawnLength += a_dc.m_remainingDrawLength;
                if (drawnLength >= lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    return;
                } else {
                    a_dc.m_ix++;
                }
            }
        }

        while(drawnLength < lineLength) {
            if (a_dc.m_ix % 2 == 0) {
                Vec2 p1 = a_p1.plus(dir.times(drawnLength));
                drawnLength += a_dashlength;
                if (drawnLength < lineLength) {
                    Vec2 p2 = a_p1.plus(dir.times(drawnLength));
                    m_imGui.getWindowDrawList().addLine(p1, p2, a_color, a_thickness);
                } else {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    m_imGui.getWindowDrawList().addLine(p1, a_p2, a_color, a_thickness);
                    return;
                }
            } else {
                drawnLength += a_holeLength;
                if (drawnLength >= lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    return;
                }
            }
            a_dc.m_ix++;
        }
    }

    public ImGuiWrapper(ImGui a_imgui) {
        m_imGui = a_imgui;
    }

    public void addRect(Vec2 a_tl, Vec2 a_br, int a_color, float a_rounding, int a_corners, float a_thickness) {

        m_imGui.getWindowDrawList().addRect(a_tl, a_br, a_color, a_rounding, a_corners, a_thickness);
    }
    public void addRectFilled(Vec2 a_tl, Vec2 a_br, int a_color, float a_rounding, int a_corners) {
        m_imGui.getWindowDrawList().addRectFilled(a_tl, a_br, a_color, a_rounding, a_corners);
    }

    public void addText(Vec2 a_pos, int a_color, String a_text) {
        m_imGui.getWindowDrawList().addText(a_pos, COL32(175, 175, 175, 255), a_text.toCharArray(), a_text.length());
    }

    public void addCircle(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_thickness) {
        m_imGui.getWindowDrawList().addCircle(a_center, a_radius, a_color, a_segments, a_thickness);
    }

    public void addLine(Vec2 a_p1, Vec2 a_p2, int a_color, float a_thickness) {
        m_imGui.getWindowDrawList().addLine(a_p1, a_p2, a_color, a_thickness);
    }

    public Vec2 calcTextSize(String a_str, boolean a_hideTextAfterDoubleHash) {
        return m_imGui.calcTextSize(a_str, a_hideTextAfterDoubleHash);
    }

    public void beginTooltip() {
        m_imGui.beginTooltip();
    }

    public void endTooltip() {
        m_imGui.endTooltip();
    }

    public void text(String a_text) {
        m_imGui.text(a_text);
    }

    public Vec2 getMousePos() {
        return m_imGui.getMousePos();
    }

    public Vec2 getMouseDragDelta(int a_button, float a_lockThreshold) {
        return m_imGui.getMouseDragDelta(a_button, 1.0f);
    }

    public boolean isMouseDragging(int a_button, float a_lockThreshold) {
        return m_imGui.isMouseDragging(a_button, a_lockThreshold);
    }

    public void stopWindowDrag() {
        m_imGui.getCurrentWindow().setFlags(WindowFlag.NoMove.or(m_imGui.getCurrentWindow().getFlags()));
    }
}
