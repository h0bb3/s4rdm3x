package archviz;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import imgui.*;
import imgui.internal.Rect;

import static imgui.ImguiKt.COL32;

class ImGuiWrapper {
    public ImGui m_imGui;

    public boolean beginPopupContextWindow(String a_strId, int a_mouseButton, boolean a_alsoOverItems) {
        return m_imGui.beginPopupContextWindow(a_strId, a_mouseButton, a_alsoOverItems);
    }

    public void closeCurrentPopup() {
        m_imGui.closeCurrentPopup();
    }

    public void endPopup() {
        m_imGui.endPopup();
    }

    protected static class DashContext {
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
                if (drawnLength > lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    m_imGui.getWindowDrawList().addLine(p1, a_p2, a_color, a_thickness);
                    return;
                } else {
                    Vec2 p2 = a_p1.plus(dir.times(drawnLength));
                    m_imGui.getWindowDrawList().addLine(p1, p2, a_color, a_thickness);
                    a_dc.m_ix++;

                }
            } else {
                drawnLength += a_dc.m_remainingDrawLength;
                if (drawnLength > lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    return;
                } else {
                    a_dc.m_ix++;
                }
            }
        }

        while(true) {
            if (a_dc.m_ix % 2 == 0) {
                Vec2 p1 = a_p1.plus(dir.times(drawnLength));
                drawnLength += a_dashlength;
                if (drawnLength > lineLength) {
                    a_dc.m_remainingDrawLength = drawnLength - lineLength;
                    m_imGui.getWindowDrawList().addLine(p1, a_p2, a_color, a_thickness);
                    return;
                } else {
                    Vec2 p2 = a_p1.plus(dir.times(drawnLength));
                    m_imGui.getWindowDrawList().addLine(p1, p2, a_color, a_thickness);
                }
            } else {
                drawnLength += a_holeLength;
                if (drawnLength > lineLength) {
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

    public enum InputTextStatus {
        Editing,
        Done,
        Canceled
    }

    public InputTextStatus inputTextSingleLine(Vec2 a_pos, float a_width, String a_label, char[] a_buffer) {
        Vec2 textPos = a_pos.minus(m_imGui.getWindowPos());

        textPos.minus(m_imGui.getStyle().getFramePadding(), textPos);

        m_imGui.setCursorPos(textPos);
        m_imGui.pushItemWidth(a_width + m_imGui.getStyle().getFramePadding().getX() * 1 + m_imGui.getFontSize());   // we need to be a bit wider than *2 so that imgui does not make wonky things with the text x position

        boolean ret = m_imGui.inputText("", a_buffer, InputTextFlag.EnterReturnsTrue.getI());

        // GLFW_KEY_KP_ENTER   335
        if (m_imGui.isKeyDown(335)) {
            ret = true;
        }
        m_imGui.popItemWidth();
        if (ret) {

            return InputTextStatus.Done;
        }

        if (m_imGui.isAnyItemActive()) {
            return InputTextStatus.Editing;
        }

        return InputTextStatus.Canceled;
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

    public float getTextLineHeightWithSpacing() {
        return m_imGui.getTextLineHeightWithSpacing();
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

    boolean isMouseDoubleClicked(int a_button) {
        return m_imGui.isMouseDoubleClicked(a_button);
    }

    boolean isMouseClicked(int a_button, boolean a_doRepeat) {
        return m_imGui.isMouseClicked(a_button, a_doRepeat);
    }

    boolean isMouseDown(int a_button) {
        return m_imGui.isMouseDown(a_button);
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

    public boolean isInside(Vec2 a_center, float a_radius, Vec2 a_pos) {
        if (m_imGui.getCurrentWindow().isActiveAndVisible() && m_imGui.isWindowHovered(HoveredFlag.RootWindow)) {
            return a_center.minus(a_pos).length2() <= a_radius * a_radius;
        }
        return false;
    }

    public boolean isInside(Rect a_rect, Vec2 a_pos) {
        if (m_imGui.getCurrentWindow().isActiveAndVisible() && m_imGui.isWindowHovered(HoveredFlag.RootWindow)) {
            /*Vec4 clipRect = m_imGui.getCurrentWindow().getDrawList().getCurrentClipRect();
            if (clipRect != null) {
                return a_rect.contains(a_pos) && new Rect(clipRect).contains(a_pos);
            }*/
            return a_rect.contains(a_pos);
        }

        return false;
    }

    public boolean button(String a_text, float a_width) {
        return m_imGui.button(a_text, new Vec2(a_width, 0));
    }
}
