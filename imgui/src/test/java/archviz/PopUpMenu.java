package archviz;

import glm_.vec2.Vec2;
import imgui.ImGui;
import imgui.internal.Rect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class PopUpMenu extends DragNDrop {

    private static class ImGuiWrapper extends archviz.ImGuiWrapper {

        private Vec2 m_mousePos = new Vec2();
        private String m_activeMenuLabel = null;

        public ImGuiWrapper() {
            super(mock(ImGui.class, new ImGuiMock()));
        }

        public void setMousePos(Vec2 a_pos) {
            m_mousePos.setX(a_pos.getX());
            m_mousePos.setY(a_pos.getY());
        }

        public Vec2 getMousePos() {
            return m_mousePos;
        }

        public void setActivePopUpMenu(String a_popUpMenuLabel) {
            m_activeMenuLabel = a_popUpMenuLabel;
        }

        @Override
        public boolean beginPopupContextWindow(String a_strId, int a_mouseButton, boolean a_alsoOverItems) {
            return  m_activeMenuLabel != null;
        }

        @Override
        public boolean menuItem(String a_label, String a_shortCut, boolean a_selected, boolean a_enabled) {
            if (a_enabled) {
                if (m_activeMenuLabel != null && m_activeMenuLabel.contentEquals(a_label)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Test
    void addParentAtRoot() {
        HRoot cut = new HRoot();

        HNode c = cut.add("client");

        HRoot.Action a = addParent(cut, c);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client", "virtual_1.client")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.getPairs()));
    }

    @Test
    void addParentOfVirtualAtRoot() {
        HRoot cut = new HRoot();

        HNode c = cut.add("client.n1").m_parent;

        HRoot.Action a = addParent(cut, c);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.n1", "virtual_2.client.n1")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.getPairs()));
    }

    @Test
    void addParentOfVirtualAtVirtual() {
        HRoot cut = new HRoot();

        HNode c = cut.add("client.component.n1").m_parent;

        HRoot.Action a = addParent(cut, c);

        HRoot.Action.NodeNamePair[] expected = {new HRoot.Action.NodeNamePair("client.component.n1", "client.virtual_3.component.n1")};

        Assertions.assertTrue(checkPairSetEquality(expected, a.m_hiearchyMove.getPairs()));
    }

    HRoot.Action addParent(HRoot a_sut, HNode a_source) {
        Rect r = new Rect(0, 0, 100, 100);
        HRoot.State s = new HRoot.State();

        ImGuiWrapper imgui = new ImGuiWrapper();

        a_sut.render(r, imgui, s);

        imgui.setMousePos(getSafePos(a_source));

        HRoot.Action a = a_sut.render(r, imgui, s);
        assertEquals(null, a);

        imgui.setActivePopUpMenu("Add Parent");
        a = a_sut.render(r, imgui, s);
        assertNotNull(a);

        return a;
    }
}
