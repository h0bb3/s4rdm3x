package gui;

import glm_.vec2.Vec2;
import imgui.internal.Rect;

public class ZoomWindow {

    private Vec2 m_areaSize;
    private Vec2 m_ulOffset; // upper left corner offset in local coordinates.
    private Vec2 m_scroll = new Vec2(0, 0);
    private Vec2 m_windowPos;

    private float m_scale = 1.0f;
    private float m_oldScale = 1.0f;


    public void setScale(float a_scale) {
        m_oldScale = a_scale;
        m_scale = a_scale;
    }

    public float getScale() {
        return m_scale;
    }

    public boolean begin(ImGuiWrapper a_imgui, Vec2 a_clientSize, Vec2 a_areaSize) {

        final float outerBorder = 50;

        a_imgui.imgui().beginChild("ZoomWindow_outer", a_clientSize, true, 0);

        a_clientSize.minus(outerBorder, a_clientSize);
        a_imgui.imgui().beginChild("ZoomWindow_inner", a_clientSize, true, 0);

        m_windowPos = a_imgui.imgui().getCurrentWindow().getPos();

        //m_ulOffset = ((a_clientSize.minus(a_clientSize)).times(0.5)).plus(a_areaSize.times(0.5));
        m_ulOffset = a_clientSize.times(0.5);

        if (m_areaSize == null) {
            m_areaSize = new Vec2(a_areaSize);

            // 0, 0 is in the middle of m_areaSize
            m_ulOffset = ((a_clientSize.minus(m_areaSize)).times(0.5)).plus(m_areaSize.times(0.5));
        } else if (m_areaSize.minus(a_areaSize).length2() > 1) {
            Vec2 oldOffset = new Vec2(m_ulOffset);

            Vec2 mousePos = new Vec2(a_imgui.getMousePos());
            Vec2 oldMousePos = mousePos.minus(m_windowPos.plus(m_scroll));

            Vec2 deltaScale = (a_areaSize.minus(m_areaSize)).div(m_areaSize);

            m_areaSize = new Vec2(a_areaSize);
            //m_ulOffset = a_clientSize.plus(m_areaSize).times(0.5);


            // this scales the client area around the absolute center 0,0
            //m_ulOffset = ((a_clientSize.minus(m_areaSize)).times(0.5)).plus(m_areaSize.times(0.5));

            // so we now need to move it so the old old offset is maintained
            //Vec2 deltaPos = (m_ulOffset.minus(oldOffset)).times(deltaScale);
            //m_ulOffset = m_ulOffset.plus(deltaPos);



            //m_ulOffset = m_ulOffset.plus(m_ulOffset.times(m_scale - m_oldScale));

            //m_scroll = m_scroll.plus(m_scroll.times(deltaScale));


            // https://stackoverflow.com/questions/2916081/zoom-in-on-a-point-using-scale-and-translate
            // mousePos is now in local coordinates
            // scroll needs to be changed so that mousePos coordinates are the same after scaling
            // i.e. mousePos = a_imgui.getMousePos() - windowPos - m_ulOffset - m_scroll
            //Vec2 newMousePos = oldMousePos.times(m_scale - m_oldScale);


            //m_scroll = m_scroll.plus(newMousePos.minus(oldMousePos));
        }



        if (a_imgui.isInside(new Rect(m_windowPos, m_windowPos.plus(a_clientSize)), a_imgui.getMousePos())){
            if (a_imgui.isMouseDragging(0, 0)) {
                a_imgui.stopWindowDrag();
                Vec2 scroll = a_imgui.imgui().getIo().getMouseDelta();

                //m_scroll.plus(scroll, m_scroll);
                m_scroll = m_scroll.plus(scroll.times(1.0f / m_scale));
            }
        }
        return true;
    }

    public void end(ImGuiWrapper a_imgui) {
        a_imgui.imgui().endChild();
        a_imgui.imgui().endChild();
    }

    public Vec2 getULOffset() {
        return m_windowPos.plus(m_ulOffset.plus(m_scroll.times(m_scale)));
    }
}
