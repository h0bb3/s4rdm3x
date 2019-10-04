package experimenting;

import glm_.vec2.Vec2;
import gui.ImGuiWrapper;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class BoxPlot {

    HashMap<Integer, ArrayList<Double>> m_dataCategories = new HashMap<>();

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
        Vec2 offset = a_imgui.imgui().getCurrentWindow().getPos();
        size.minus(10, size);

        for (Map.Entry<Integer, ArrayList<Double>> es : m_dataCategories.entrySet()) {
            int xOffset = 10;
            ArrayList<Double> data = es.getValue();

            double median = data.get(data.size() / 2);

            Vec2 start = new Vec2(xOffset, size.getY() * median);
            Vec2 end = start.plus(new Vec2(10, 0));

            a_imgui.addLine(offset.plus(start), offset.plus(end), es.getKey(), 2.0f);

            xOffset += 20;

        }
    }
}
