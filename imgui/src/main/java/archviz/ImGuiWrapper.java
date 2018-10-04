package archviz;

import glm_.vec2.Vec2;
import imgui.ImGui;
import imgui.WindowFlag;

import static imgui.ImguiKt.COL32;

class ImGuiWrapper {
    public ImGui m_imGui;

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
