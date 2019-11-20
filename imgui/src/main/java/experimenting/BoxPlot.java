package experimenting;

import glm_.vec2.Vec2;
import gui.ImGuiWrapper;
import imgui.ImGui;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class BoxPlot {

    private static int g_id = 0;
    private int m_id;

    HashMap<Integer, ArrayList<Double>> m_dataCategories = new HashMap<>();

    public BoxPlot() {
        m_id = g_id;
        g_id++;
    }

    public void addData(double a_value, int a_id, int a_color) {

        ArrayList<Double> data;
        if (m_dataCategories.containsKey(a_color)) {
            data = m_dataCategories.get(a_color);
        } else {
            data = new ArrayList<>();
            m_dataCategories.put(a_color, data);
        }

        data.add(a_value);


        data.sort((a, b) -> {return Double.compare(a, b);});
    }

    public void doPlot(ImGuiWrapper a_imgui) {

        Vec2 size = a_imgui.imgui().getContentRegionAvail();

        a_imgui.imgui().beginChild("BoxPlot##" + m_id, size, true, 0);
        Vec2 offset = new Vec2(a_imgui.imgui().getCurrentWindow().getPos());
        size.minus(10, size);

        // y coords are flipped in the plot
        offset.setY(offset.getY() + size.getY());
        size.times(1, -1, size);

        Set<Map.Entry<Integer, ArrayList<Double>>> datas = m_dataCategories.entrySet();

        final double spacing = 5.0;
        double xOffset = spacing;
        double width = size.getX() / datas.size() - spacing;
        for (Map.Entry<Integer, ArrayList<Double>> es : datas) {

            ArrayList<Double> data = es.getValue();

            double _25 = data.get((int)(data.size() * 0.25));
            double _50 = data.get((int)(data.size() * 0.5));
            double _75 = data.get((int)(data.size() * 0.75));


            addHLine(a_imgui, offset, xOffset, size.getY() * _25, width, es.getKey(), 1.0f);
            addHLine(a_imgui, offset, xOffset, size.getY() * _50, width, es.getKey(), 2.0f);
            addHLine(a_imgui, offset, xOffset, size.getY() * _75, width, es.getKey(), 1.0f);

            // draw the whiskers
            double iqr = _75 - _25;
            double min = data.get(0);
            double x = xOffset + width * 0.5;
            for (int i = 1; min < _25 - iqr; i++) {
                a_imgui.addCircleFilled(offset.plus(new Vec2(x, size.getY() * min)), 2, es.getKey(), 9);
                min = data.get(i);
            }
            a_imgui.addLine(offset.plus(new Vec2(x, size.getY() * min)), offset.plus(new Vec2(x, size.getY() * _25)), es.getKey(), 1.0f);

            double max = data.get(data.size() - 1);
            for (int i = data.size() - 2; max > _75 + iqr; i--) {
                a_imgui.addCircleFilled(offset.plus(new Vec2(x, size.getY() * max)), 2, es.getKey(), 9);
                max = data.get(i);
            }
            a_imgui.addLine(offset.plus(new Vec2(x, size.getY() * max)), offset.plus(new Vec2(x, size.getY() * _75)), es.getKey(), 1.0f);

            xOffset += width + spacing;
        }

        a_imgui.imgui().endChild();
    }

    private void addHLine(ImGuiWrapper a_imgui, Vec2 a_screenOffset, double a_x, double a_y, double a_width, int a_color, float a_thickness) {
        Vec2 start = new Vec2(a_x, a_y);
        Vec2 end = start.plus(new Vec2(a_width, 0));
        a_imgui.addLine(a_screenOffset.plus(start), a_screenOffset.plus(end), a_color, a_thickness);
    }

    public void clearData() {
        m_dataCategories.clear();
    }
}
