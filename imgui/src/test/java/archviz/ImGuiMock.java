package archviz;

import glm_.vec2.Vec2;
import imgui.DrawList;
import imgui.internal.Window;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

public class ImGuiMock implements Answer<Object> {

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        if( invocation.getMethod().getReturnType().equals(Vec2.class)){
            return new Vec2(0, 0);
        } else if (invocation.getMethod().getReturnType().equals(DrawList.class)) {
            return mock(DrawList.class);
        } else if (invocation.getMethod().getReturnType().equals(Window.class)) {
            return mock(Window.class);
        } else {
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        }
    }
}
