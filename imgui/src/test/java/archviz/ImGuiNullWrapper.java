package archviz;

import glm_.vec2.Vec2;

public class ImGuiNullWrapper extends ImGuiWrapper {

    public ImGuiNullWrapper() {
        super(null);
    }


    public void addRect(Vec2 a_tl, Vec2 a_br, int a_color, float a_rounding, int a_corners, float a_thickness) {
    }
    public void addRectFilled(Vec2 a_tl, Vec2 a_br, int a_color, float a_rounding, int a_corners) {
    }

    public void addText(Vec2 a_pos, int a_color, String a_text) {
    }

    public void addCircle(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_thickness) {
    }

    public void addLine(Vec2 a_p1, Vec2 a_p2, int a_color, float a_thickness) {
    }

    public void addDashedLine(Vec2 a_p1, Vec2 a_p2, int a_color, float a_thickness, float a_holeLength, float a_dashlength, DashContext a_dc) {
    }

    public Vec2 calcTextSize(String a_str, boolean a_hideTextAfterDoubleHash) {
        return new Vec2(0, 0);
    }

    public float getTextLineHeightWithSpacing() { return 0; }

    public void beginTooltip() {
    }

    public void endTooltip() {
    }

    public void text(String a_text) {
    }

    public Vec2 getMousePos() {
        return new Vec2(0, 0);
    }

    public Vec2 getMouseDragDelta(int a_button, float a_lockThreshold) {
        return new Vec2(0, 0);
    }

    public boolean isMouseDragging(int a_button, float a_lockThreshold) {
        return false;
    }

    boolean isMouseDoubleClicked(int a_button) {
        return false;
    }

    public InputTextStatus inputTextSingleLine(Vec2 a_pos, float a_width, String a_label, char[] a_buffer) {
        return InputTextStatus.Canceled;
    }

    boolean isMouseClicked(int a_button, boolean a_doRepeat) {
        return false;
    }

    public void stopWindowDrag() {    }
}
