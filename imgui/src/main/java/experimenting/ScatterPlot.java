package experimenting;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;

import java.util.ArrayList;

public class ScatterPlot {

    public int dataCount() {
        return m_data.size();
    }

    public void clearData() {
        m_data.clear();
    }

    public static class Data {
        Vec2 m_point;
        int m_color;
        int m_id;

        Data(double a_x, double a_y, int a_id, int a_color) {
            m_point = new Vec2(a_x, a_y);
            m_id = a_id;
            m_color = a_color;
        }
    }

    private static int g_id = 0;
    private int m_id;

    ArrayList<Data> m_data = new ArrayList<>();
    Vec2 m_min;
    Vec2 m_max;

    // these are used for calculating the screenpos of a point
    Vec2 m_minMax;
    Vec2 m_offset;
    Vec2 m_size;

    public ScatterPlot() {
        m_id = g_id;
        g_id++;
    }

    public Vec2 toScreenPos(Vec2 a_pointIn, Vec2 a_screenPosOut) {
        // rescale according to min and max
        a_pointIn.minus(m_min, a_screenPosOut);
        a_screenPosOut.div(m_minMax, a_screenPosOut);
        a_screenPosOut.times(m_size, a_screenPosOut);
        a_screenPosOut.plus(m_offset, a_screenPosOut);

        return a_screenPosOut;
    }

    public void addData(double x, double y, int a_id, int a_color) {

        m_data.add(new Data(x, y, a_id, a_color));
        if (m_min == null) {
            m_min = new Vec2(x, y);
        } else {
            if (m_min.getX() > x) {
                m_min.setX((float)x);
            }
            if (m_min.getY() > y) {
                m_min.setY((float)y);
            }
        }

        if (m_max == null) {
            m_max = new Vec2(x, y);
        } else {
            if (m_max.getX() < x) {
                m_max.setX((float)x);
            }
            if (m_max.getY() < y) {
                m_max.setY((float)y);
            }
        }
    }

    void doPlot(ImGuiWrapper a_imgui, ArrayList<Data> a_selectedData) {
        Vec2 size = a_imgui.imgui().getContentRegionAvail();


        size.minus(0, a_imgui.getTextLineHeightWithSpacing(), size);

        a_imgui.imgui().beginChild("ScatterPlot##" + m_id, size, true, 0);
        Vec2 offset = a_imgui.imgui().getCurrentWindow().getPos();
        a_imgui.imgui().endChild(); // we end the child here as we do not use it for anything, also drawing children is done after drawing the parent.
        offset.plus(5, 5, offset);
        size.minus(10, size);

        //final int white = a_imgui.toColor(new Vec4(1, 1, 1, 1));
        //final int blue = a_imgui.toColor(new Vec4(0.25, 0.25, 1, 1));

        //a_imgui.addCircleFilled(offset, 2, blue, 6);

        // y coords are flipped in the plot
        offset.setY(offset.getY() + size.getY());

        //a_imgui.addCircleFilled(offset, 2, blue, 6);

        size.times(1, -1, size);
        Vec2 minMax = null;

        if (m_data.size() > 0) {
            minMax = m_max.minus(m_min);
            if (minMax.getX() == 0) {
                minMax.setX(1);
            }
            if (minMax.getY() == 0) {
                minMax.setY(1);
            }

            m_minMax = minMax;
            m_offset = offset;
            m_size = size;

            Vec2 point = new Vec2();
            for (int pIx = 0; pIx < m_data.size(); pIx++) {
                Data p = m_data.get(pIx);

                toScreenPos(p.m_point, point);
                a_imgui.addCircleFilled(point, 2, p.m_color, 6);

                if (a_selectedData != null && a_imgui.isInside(point, 4, a_imgui.getMousePos())) {
                    a_selectedData.add(p);
                }
            }
        }

        String coords = "";
        if (m_data.size() > 1 && a_imgui.isCurrentWindowActive()) {
            Vec2 mousePos = new Vec2(a_imgui.getMousePos());

            mousePos.minus(offset, mousePos);
            mousePos.div(size, mousePos);
            mousePos.times(minMax, mousePos);
            mousePos.plus(m_min, mousePos);

            coords = String.format("x:%.2f y:%.2f", + mousePos.getX(), mousePos.getY()).replace(",", ".");
        }


        if (coords != null) {
            a_imgui.text(coords);
        }
    }
}
