import static imgui.ImguiKt.COL32;
import static org.junit.jupiter.api.Assertions.*;

import glm_.vec2.Vec2;
import imgui.ImGui;
import imgui.WindowFlag;
import imgui.internal.Rect;
import org.junit.jupiter.api.Test;

class HRootTest {



    static class ImGuiWrapper extends HRoot.ImGuiWrapper {
        public ImGuiWrapper() {
            super(null);
        }

        private Vec2 m_mouseDragStart;
        private Vec2 m_mousePos;
        private boolean m_isMouseDragging = false;

        public void setMousePos(float a_x, float a_y) {
            if (m_mousePos == null) {
                m_mousePos = new Vec2(a_x, a_y);
            } else {
              m_mousePos.setX(a_x);
              m_mousePos.setY(a_y);
            }
        }

        public void setMouseDragging(float a_x, float a_y, boolean a_start) {
            if (m_isMouseDragging == false && a_start == true) {
                m_mouseDragStart = new Vec2(a_x, a_y);
            }

            m_mousePos = new Vec2(a_x, a_y);
            m_isMouseDragging = a_start;
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

        public Vec2 calcTextSize(String a_str, boolean a_hideTextAfterDoubleHash) {
            return new Vec2(0, 0);
        }

        public void beginTooltip() {
        }

        public void endTooltip() {
        }

        public void text(String a_text) {
        }

        public Vec2 getMousePos() {
            return m_mousePos;
        }

        public Vec2 getMouseDragDelta(int a_button, float a_lockThreshold) {
            if (m_mouseDragStart != null) {
                return m_mousePos.minus(m_mouseDragStart);
            }
            return new Vec2(0, 0);
        }

        public boolean isMouseDragging(int a_button, float a_lockThreshold) {
            return m_isMouseDragging;
        }



        public void stopWindowDrag() {
        }
    }

    @org.junit.jupiter.api.Test
    void render() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

    }

    @org.junit.jupiter.api.Test
    void dragN2intoN1() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("n1");
        cut.add("n2");
        cut.add("n3");
        cut.add("n4");

        // start drag from n2
        imgui.setMouseDragging(37, 37, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        // drag into n1
        imgui.setMouseDragging(12, 12, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(12, 12, false);
        a = cut.render(r, imgui);
        assertNotNull(a);
        assertNotNull(a.m_hiearchyMove);
        assertEquals("n2", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("n1.n2", a.m_hiearchyMove.m_nodes.get(0).m_newName);
    }

    @Test
    void dragN1intoN2() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("n1");
        cut.add("n2");
        cut.add("n3");
        cut.add("n4");

        imgui.setMouseDragging(12, 12, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        imgui.setMouseDragging(37, 37, true);
        a = cut.render(r, imgui);

        imgui.setMouseDragging(37, 37, false);
        a = cut.render(r, imgui);
        assertNotNull(a);
        assertNotNull(a.m_hiearchyMove);
        assertEquals("n1", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("n2.n1", a.m_hiearchyMove.m_nodes.get(0).m_newName);
    }

    @Test
    void dragN3intoClient() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        // start drag from n3
        imgui.setMouseDragging(62, 62, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        // drag into n1
        imgui.setMouseDragging(37, 12, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(37, 12, false);
        a = cut.render(r, imgui);
        assertNotNull(a);
        assertNotNull(a.m_hiearchyMove);
        assertEquals("n3", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("client.n3", a.m_hiearchyMove.m_nodes.get(0).m_newName);
    }

    @Test
    void dragN1fromClient() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        // start drag from n3
        imgui.setMouseDragging(13, 13, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        // drag into n1
        imgui.setMouseDragging(60, 12, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(60, 12, false);
        a = cut.render(r, imgui);
        assertNotNull(a);
        assertNotNull(a.m_hiearchyMove);
        assertEquals("client.n1", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("n1", a.m_hiearchyMove.m_nodes.get(0).m_newName);
    }

    @Test
    void dragN1fromClient_2() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client");
        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        // start drag from n3
        imgui.setMouseDragging(13, 13, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        // drag into n1
        imgui.setMouseDragging(60, 12, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(60, 12, false);
        a = cut.render(r, imgui);
        assertNotNull(a);
        assertNotNull(a.m_hiearchyMove);
        assertEquals("client.n1", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("n1", a.m_hiearchyMove.m_nodes.get(0).m_newName);
    }

    @Test
    void dragClientintoN4() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        // start drag from n3
        imgui.setMouseDragging(37, 13, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        // drag into n1
        imgui.setMouseDragging(87, 87, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(87, 87, false);
        a = cut.render(r, imgui);
        assertNotNull(a);
        assertNotNull(a.m_hiearchyMove);
        assertEquals("client.n1", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("n4.client.n1", a.m_hiearchyMove.m_nodes.get(0).m_newName);
        assertEquals("client.n2", a.m_hiearchyMove.m_nodes.get(1).m_oldName);
        assertEquals("n4.client.n2", a.m_hiearchyMove.m_nodes.get(1).m_newName);
    }

    @Test
    void dragClientintoN4_2() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client");
        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        // start drag from n3
        imgui.setMouseDragging(37, 13, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        // drag into n1
        imgui.setMouseDragging(87, 87, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(87, 87, false);
        a = cut.render(r, imgui);
        assertNotNull(a);
        assertNotNull(a.m_hiearchyMove);
        assertEquals("client.n1", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("n4.client.n1", a.m_hiearchyMove.m_nodes.get(0).m_newName);
        assertEquals("client.n2", a.m_hiearchyMove.m_nodes.get(1).m_oldName);
        assertEquals("n4.client.n2", a.m_hiearchyMove.m_nodes.get(1).m_newName);
        assertEquals("client", a.m_hiearchyMove.m_nodes.get(2).m_oldName);
        assertEquals("n4.client", a.m_hiearchyMove.m_nodes.get(2).m_newName);
    }

    @Test
    void dragClientintoN1() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        // start drag from n3
        imgui.setMouseDragging(37, 13, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        // drag into n1
        imgui.setMouseDragging(17, 17, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(17, 17, false);
        a = cut.render(r, imgui);
        assertNull(a);
    }

    @Test
    void dragClientintoN1_2() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client");
        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        // start drag from n3
        imgui.setMouseDragging(37, 13, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        // drag into n1
        imgui.setMouseDragging(17, 17, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(17, 17, false);
        a = cut.render(r, imgui);
        assertNull(a);
    }

    @Test
    void dragClientintoServer() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("server.n3");          //50,50-75,75 62.5,62.5
        cut.add("server.n4");          //75,75-100,100 87.5,87.5

        imgui.setMouseDragging(37, 13, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        imgui.setMouseDragging(87, 63, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(87, 63, false);
        a = cut.render(r, imgui);
        assertEquals("client.n1", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("server.client.n1", a.m_hiearchyMove.m_nodes.get(0).m_newName);
        assertEquals("client.n2", a.m_hiearchyMove.m_nodes.get(1).m_oldName);
        assertEquals("server.client.n2", a.m_hiearchyMove.m_nodes.get(1).m_newName);
    }

    @Test
    void dragClientintoServer_2() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client");
        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("server.n3");          //50,50-75,75 62.5,62.5
        cut.add("server.n4");          //75,75-100,100 87.5,87.5
        cut.add("server");

        imgui.setMouseDragging(37, 13, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        imgui.setMouseDragging(87, 63, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(87, 63, false);
        a = cut.render(r, imgui);
        assertEquals("client.n1", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("server.client.n1", a.m_hiearchyMove.m_nodes.get(0).m_newName);
        assertEquals("client.n2", a.m_hiearchyMove.m_nodes.get(1).m_oldName);
        assertEquals("server.client.n2", a.m_hiearchyMove.m_nodes.get(1).m_newName);
        assertEquals("client", a.m_hiearchyMove.m_nodes.get(2).m_oldName);
        assertEquals("server.client", a.m_hiearchyMove.m_nodes.get(2).m_newName);
    }

    @Test
    void dragSubComponentChildIntoRoot() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client.n1");      //0,0-50 25.5, 25.5
        cut.add("client.n1.n2");   //10,45
        cut.add("client.n1.n2.n3"); //10,20
        cut.add("client.n1.n2.n4"); //20,45
        cut.add("n5");       // 50,100

        imgui.setMouseDragging(12, 30, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        imgui.setMouseDragging(30, 75, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(30, 75, false);
        a = cut.render(r, imgui);
        assertEquals("client.n1.n2.n3", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("n2.n3", a.m_hiearchyMove.m_nodes.get(0).m_newName);
        assertEquals("client.n1.n2.n4", a.m_hiearchyMove.m_nodes.get(1).m_oldName);
        assertEquals("n2.n4", a.m_hiearchyMove.m_nodes.get(1).m_newName);
        assertEquals("client.n1.n2", a.m_hiearchyMove.m_nodes.get(2).m_oldName);
        assertEquals("n2", a.m_hiearchyMove.m_nodes.get(2).m_newName);
    }

    @Test
    void dragSubComponentChildIntoParent() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        cut.add("client.n1");      //0,0-50 25.5, 25.5
        cut.add("client.n1.n2");   //10,45
        cut.add("client.n1.n2.n3"); //10,20
        cut.add("client.n1.n2.n4"); //20,45
        cut.add("client.n5");       // 50,100

        imgui.setMouseDragging(12, 30, true);
        HRoot.Action a = cut.render(r, imgui);
        assertEquals(null, a);

        imgui.setMouseDragging(30, 75, true);
        a = cut.render(r, imgui);

        // stop the drag in n1
        imgui.setMouseDragging(30, 75, false);
        a = cut.render(r, imgui);
        assertEquals("client.n1.n2.n3", a.m_hiearchyMove.m_nodes.get(0).m_oldName);
        assertEquals("client.n2.n3", a.m_hiearchyMove.m_nodes.get(0).m_newName);
        assertEquals("client.n1.n2.n4", a.m_hiearchyMove.m_nodes.get(1).m_oldName);
        assertEquals("client.n2.n4", a.m_hiearchyMove.m_nodes.get(1).m_newName);
        assertEquals("client.n1.n2", a.m_hiearchyMove.m_nodes.get(2).m_oldName);
        assertEquals("client.n2", a.m_hiearchyMove.m_nodes.get(2).m_newName);
    }



    @Test
    void getIndexOfFirstNonSimilarComponentInStr2() {
        HRoot cut = new HRoot();

        assertEquals(0, cut.getIndexOfFirstNonSimilarComponentInStr2("n1", "n2"));
        assertEquals(3, cut.getIndexOfFirstNonSimilarComponentInStr2("n1", "n1.n2"));
        assertEquals(5, cut.getIndexOfFirstNonSimilarComponentInStr2("n1.n2", "n1.n2"));
        assertEquals(6, cut.getIndexOfFirstNonSimilarComponentInStr2("n1.n2", "n1.n2.n3"));
        assertEquals(2, cut.getIndexOfFirstNonSimilarComponentInStr2("n1.n2", "n1"));

    }
}