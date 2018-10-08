package archviz;

import static org.junit.jupiter.api.Assertions.*;

import glm_.vec2.Vec2;
import imgui.internal.Rect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class DragNDrop {



    static class ImGuiWrapper extends ImGuiNullWrapper {

        private Vec2 m_mouseDragStart;
        private Vec2 m_mousePos = new Vec2(0, 0);
        private boolean m_isMouseDragging = false;

        public void setMousePos(float a_x, float a_y) {
            if (m_mousePos == null) {
                m_mousePos = new Vec2(a_x, a_y);
            } else {
              m_mousePos.setX(a_x);
              m_mousePos.setY(a_y);
            }
        }

        public void setMousePos(final Vec2 a_pos) {
            m_mousePos = new Vec2(a_pos);
        }

        public void setMouseDragging(final Vec2 a_pos, boolean a_start) {
            setMouseDragging(a_pos.getX(), a_pos.getY(), a_start);
        }

        public void setMouseDragging(float a_x, float a_y, boolean a_start) {
            if (m_isMouseDragging == false && a_start == true) {
                m_mouseDragStart = new Vec2(a_x, a_y);
            }

            m_mousePos = new Vec2(a_x, a_y);
            m_isMouseDragging = a_start;
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

        public Vec2 getMousePos() {
            return m_mousePos;
        }

        public boolean isInside(Rect a_rect, Vec2 a_pos) {
            return a_rect.contains(a_pos);
        }

        public boolean isInside(Vec2 a_center, float a_radius, Vec2 a_pos) {
            return a_center.minus(a_pos).length2() <= a_radius * a_radius;
        }
    }

    @org.junit.jupiter.api.Test
    void render() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        HRoot.Action a = cut.render(r, imgui, new HRoot.State());
        assertEquals(null, a);

    }

    @org.junit.jupiter.api.Test
    void dragN2intoN1() {
        HRoot cut = new HRoot();

        HNode n1 = cut.add("n1");
        HNode n2 = cut.add("n2");
        cut.add("n3");
        cut.add("n4");

        HRoot.Action a = doDrag(cut, n2, n1);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("n2", "n1.n2")};
        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));


    }

    @Test
    void dragN1intoN2() {
        HRoot cut = new HRoot();

        HNode n1 = cut.add("n1");
        HNode n2 = cut.add("n2");
        cut.add("n3");
        cut.add("n4");

        HRoot.Action a = doDrag(cut, n1, n2);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("n1", "n2.n1")};
        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    @Test
    void dragN3intoClient() {
        HRoot cut = new HRoot();

        HNode client = cut.add("client.n1").m_parent;   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        HNode n3 = cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5


        HRoot.Action a = doDrag(cut, n3, client);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("n3", "client.n3")};
        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    @Test
    void dragN1fromClient() {
        HRoot cut = new HRoot();

        HNode c_n1 = cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        HRoot.Action a = doDrag(cut, c_n1, null);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1", "n1")};
        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    @Test
    void dragN1fromClient_2() {
        HRoot cut = new HRoot();

        cut.add("client");
        HNode n1 = cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5


        HRoot.Action a = doDrag(cut, n1, null);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1", "n1")};
        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    @Test
    void dragClientintoN4() {
        HRoot cut = new HRoot();

        HNode client = cut.add("client.n1").m_parent;
        cut.add("client.n2");
        cut.add("n3");
        HNode n4 = cut.add("n4");

        HRoot.Action a = doDrag(cut, client, n4);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1", "n4.client.n1"),
                new HRoot.Action.NodeNamePair("client.n2", "n4.client.n2")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    @Test
    void dragClientintoN4_2() {
        HRoot cut = new HRoot();

        HNode client = cut.add("client");
        cut.add("client.n1");
        cut.add("client.n2");
        cut.add("n3");
        HNode n4 = cut.add("n4");

        HRoot.Action a = doDrag(cut, client, n4);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1", "n4.client.n1"),
                new HRoot.Action.NodeNamePair("client.n2", "n4.client.n2"),
                new HRoot.Action.NodeNamePair("client", "n4.client")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    @Test
    void dragAbstractParentintoChild() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        HNode c_n1 = cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        HNode client = cut.add("client.n2").m_parent;   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        HRoot.Action a = doDrag(cut, client, c_n1);

        assertNull(a);
    }

    @Test
    void dragParentintoChild() {
        HRoot cut = new HRoot();
        Rect r = new Rect(0, 0, 100, 100);

        ImGuiWrapper imgui = new ImGuiWrapper();

        HNode client = cut.add("client");
        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        HNode c_n2 = cut.add("client.n2");   //25,25-50,50 37.5,37.5
        cut.add("n3");          //50,50-75,75 62.5,62.5
        cut.add("n4");          //75,75-100,100 87.5,87.5

        HRoot.Action a = doDrag(cut, client, c_n2);

        assertNull(a);
    }

    @Test
    void dragClientintoServer() {
        HRoot cut = new HRoot();

        cut.add("client.n1");   //0,0-25,25 12.5, 12.5
        HNode client = cut.add("client.n2").m_parent;
        cut.add("server.n3");
        HNode server = cut.add("server.n4").m_parent;

        HRoot.Action a = doDrag(cut, client, server);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1", "server.client.n1"),
                new HRoot.Action.NodeNamePair("client.n2", "server.client.n2")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    private Vec2 getSafePos(HNode a_node) {
        if (a_node.m_children.size() > 0) {
            return a_node.m_rect.getTl().plus(2);
        }

        return a_node.m_rect.getCenter();
    }

    @Test
    void dragClientintoServer_2() {
        HRoot cut = new HRoot();

        HNode client = cut.add("client");
        cut.add("client.n1");
        cut.add("client.n2");
        cut.add("server.n3");
        cut.add("server.n4");
        HNode server = cut.add("server");

        HRoot.Action a = doDrag(cut, client, server);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1", "server.client.n1"),
                new HRoot.Action.NodeNamePair("client.n2", "server.client.n2"),
                new HRoot.Action.NodeNamePair("client", "server.client")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    @Test
    void dragSubComponentChildIntoRoot() {
        HRoot cut = new HRoot();

        cut.add("client.n1");      //0,0-50 25.5, 25.5
        HNode n2 = cut.add("client.n1.n2");   //10,45
        cut.add("client.n1.n2.n3"); //10,20
        cut.add("client.n1.n2.n4"); //20,45
        cut.add("n5");       // 50,100

        HRoot.Action a = doDrag(cut, n2, null);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1.n2.n3", "n2.n3"),
                new HRoot.Action.NodeNamePair("client.n1.n2.n4", "n2.n4"),
                new HRoot.Action.NodeNamePair("client.n1.n2", "n2")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    boolean checkPairSetEquality(HRoot.Action.NodeNamePair[] a_expected, ArrayList<HRoot.Action.NodeNamePair> a_actual) {


        for (HRoot.Action.NodeNamePair exp : a_expected) {
            boolean found = false;
            for (HRoot.Action.NodeNamePair actual : a_actual) {
                if (exp.m_oldName.contentEquals(actual.m_oldName) && exp.m_newName.contentEquals(actual.m_newName)) {
                    found = true;
                }
            }
            if (!found) {
                System.out.println("Could not find Expected pair: " + exp.m_oldName + "-" + exp.m_newName + " in Actual");
                return false;
            }
        }


        for (HRoot.Action.NodeNamePair actual : a_actual) {
            boolean found = false;
            for (HRoot.Action.NodeNamePair exp : a_expected) {
                if (exp.m_oldName.contentEquals(actual.m_oldName) && exp.m_newName.contentEquals(actual.m_newName)) {
                    found = true;
                }
            }
            if (!found) {
                System.out.println("Could not find Actual pair: " + actual.m_oldName + "-" + actual.m_newName + " in Expected (Actual contains more elements)");
                return false;
            }
        }


        return true;
    }

    HRoot.Action doDrag(HRoot a_sut, HNode a_source, HNode a_target) {
        Rect r = new Rect(0, 0, 100, 100);
        HRoot.State s = new HRoot.State();

        ImGuiWrapper imgui = new ImGuiWrapper();

        a_sut.render(r, imgui, s);

        imgui.setMouseDragging(getSafePos(a_source), true);
        HRoot.Action a = a_sut.render(r, imgui, s);
        assertEquals(null, a);

        Vec2 dest = a_target == null ? new Vec2(-1, -1) : getSafePos(a_target);

        imgui.setMouseDragging(dest, true);
        a = a_sut.render(r, imgui, s);
        assertEquals(null, a);

        imgui.setMouseDragging(dest, false);
        a = a_sut.render(r, imgui, s);

        return a;
    }

    @Test
    void dragParentConcreteNodeIntoRoot() {
        HRoot cut = new HRoot();

        // drag n2 into client
        cut.add("client.n1");
        cut.add("client.n1.n2");
        HNode clientConcrete = cut.add("client").m_children.get(0);
        cut.add("client.n1.n2.n3");
        cut.add("client.n1.n2.n4");
        cut.add("client.n5");

        HRoot.Action a = doDrag(cut, clientConcrete, null);
        assertEquals(0, a.m_hiearchyMove.m_nodes.size());

    }

    @Test
    void dragSubComponentChildIntoParent() {
        HRoot cut = new HRoot();

        // drag n2 into client
        HNode client = cut.add("client.n1").m_parent;
        HNode c_n1_n2 = cut.add("client.n1.n2");
        cut.add("client.n1.n2.n3");
        cut.add("client.n1.n2.n4");
        cut.add("client.n5");

        HRoot.Action a = doDrag(cut, c_n1_n2, client);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1.n2", "client.n2"),
                                                new HRoot.Action.NodeNamePair("client.n1.n2.n3", "client.n2.n3"),
                                                new HRoot.Action.NodeNamePair("client.n1.n2.n4", "client.n2.n4")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }

    @Test
    void dragAbstractSubComponentToRoot() {
        HRoot cut = new HRoot();

        // drag n2 into client
        HNode n2 = cut.add("n2");
        HNode client = cut.add("n2.client.n1").m_parent;

        assertEquals(true, n2.isConcreteNode());
        assertEquals(false, n2.isAbstract());

        HRoot.Action a = doDrag(cut, client, null);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("n2.client.n1", "client.n1")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.m_nodes));
    }



    @Test
    void getIndexOfFirstNonSimilarComponentInStr2() {
        HRoot cut = new HRoot();

        Assertions.assertEquals(0, cut.getIndexOfFirstNonSimilarComponentInStr2("n1", "n2"));
        Assertions.assertEquals(3, cut.getIndexOfFirstNonSimilarComponentInStr2("n1", "n1.n2"));
        Assertions.assertEquals(5, cut.getIndexOfFirstNonSimilarComponentInStr2("n1.n2", "n1.n2"));
        Assertions.assertEquals(6, cut.getIndexOfFirstNonSimilarComponentInStr2("n1.n2", "n1.n2.n3"));
        Assertions.assertEquals(2, cut.getIndexOfFirstNonSimilarComponentInStr2("n1.n2", "n1"));

    }
}