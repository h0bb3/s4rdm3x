import glm_.vec2.Vec2;
import glm_.vec2.Vec2i;
import glm_.vec4.Vec4;
import imgui.Context;
import imgui.ContextKt;
import imgui.IO;
import imgui.ImGui;
import imgui.impl.ImplGL3;
import imgui.impl.LwjglGlfw;
import imgui.internal.DrawCornerFlag;
import imgui.internal.Rect;
import kotlin.Unit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.GUIConsole;
import se.lnu.siq.s4rdm3x.StringCommandHandler;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import uno.glfw.GlfwWindow;
import uno.glfw.windowHint;

import java.math.RoundingMode;

import static imgui.ImguiKt.COL32;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.system.MemoryUtil.NULL;

public class RectsAndArrows {

    // The window handle
    private GlfwWindow window;
    private uno.glfw.glfw glfw = uno.glfw.glfw.INSTANCE;
    private LwjglGlfw lwjglGlfw = LwjglGlfw.INSTANCE;
    private ImplGL3 implGL3 = ImplGL3.INSTANCE;
    private ImGui imgui = ImGui.INSTANCE;
    private IO io;
    private Context ctx;



    public static void main(String[] a_args) {
        RectsAndArrows pt = new RectsAndArrows();

        pt.run();
    }

    private RectsAndArrows() {
        glfw.init("3.3", windowHint.Profile.core, true);

        window = new GlfwWindow(1280, 720, "Dear ImGui Lwjgl OpenGL3 example", NULL, new Vec2i(Integer.MIN_VALUE), true);
        window.init(true);

        glfw.setSwapInterval(1);    // Enable vsync

        // Setup ImGui binding
        //setGlslVersion(330); // set here your desidered glsl version
        ctx = new Context(null);
        //io.configFlags = io.configFlags or ConfigFlag.NavEnableKeyboard  // Enable Keyboard Controls
        //io.configFlags = io.configFlags or ConfigFlag.NavEnableGamepad   // Enable Gamepad Controls
        lwjglGlfw.init(window, true, LwjglGlfw.GlfwClientApi.OpenGL);

        io = imgui.getIo();

        imgui.styleColorsDark(null);
    }

    private void run() {

        GUIConsole guic = new GUIConsole();
        StringCommandHandler sch = new StringCommandHandler();
        Graph graph = new MultiGraph("main_graph");
        window.loop(() -> {

            if (guic.hasInput()) {

                String in = guic.popInput();
                sch.execute(in, graph).forEach(str -> guic.println(str));
            }

            mainLoop(sch.getArchDef());
            return Unit.INSTANCE;
        });

        guic.close();
        lwjglGlfw.shutdown();
        ContextKt.destroy(ctx);

        window.destroy();
        glfw.terminate();
    }

    private float[] f = {0f};
    private Vec4 clearColor = new Vec4(0.45f, 0.55f, 0.6f, 1f);
    private boolean[] showAnotherWindow = {true};
    private int[] counter = {0};

    private void mainLoop(HuGMe.ArchDef a_arch) {


        // Start the Dear ImGui frame
        lwjglGlfw.newFrame();


        imgui.text("Hello, world!");                                // Display some text (you can use a format string too)
        imgui.sliderFloat("float", f, 0f, 1f, "%.3f", 1f);       // Edit 1 float using a slider from 0.0f to 1.0f
        imgui.colorEdit3("clear color", clearColor, 0);               // Edit 3 floats representing a color

        imgui.checkbox("Another Window", showAnotherWindow);

        if (imgui.button("Button", new Vec2()))                               // Buttons return true when clicked (NB: most widgets return true when edited/activated)
            counter[0]++;

        imgui.sameLine(0f, -1f);
        imgui.text("counter = $counter");

        imgui.text("Application average %.3f ms/frame (%.1f FPS)", 1_000f / io.getFramerate(), io.getFramerate());

        // 2. Show another simple window. In most cases you will use an explicit begin/end pair to name the window.
        if (showAnotherWindow[0]) {
            imgui.begin("Boxes And Lines", showAnotherWindow, 0);
            if (a_arch !=  null) {
                HuGMe.ArchDef.Component[] components = new HuGMe.ArchDef.Component[a_arch.getComponentCount()];
                for (int ix = 0; ix < components.length; ix++) {
                    components[ix] = a_arch.getComponent(ix);
                }
                Rect r = imgui.getCurrentWindow().getInnerClipRect();
                Vec2 tl = new Vec2(r.getTl());
                Vec2 size = new Vec2(r.getWidth() / components.length, r.getHeight() / components.length);
                Vec2 br = new Vec2(size);
                br = br.plus(tl);


                for (HuGMe.ArchDef.Component component : components) {
                    imgui.getWindowDrawList().addRectFilled(tl, br, COL32(75, 75, 75, 255), 0, 0);
                    imgui.getWindowDrawList().addRect(tl, br, COL32(175, 175, 175, 255), 0, 0, 2.0f);

                    Vec2 textSize = imgui.calcTextSize(component.getName(), false);
                    textSize.div(2);
                    Vec2 textPos = tl.plus(size.div(2).minus(textSize.div(2)));

                    imgui.getWindowDrawList().addText(textPos, COL32(175, 175, 175, 255), component.getName().toCharArray(), component.getName().length());
                    tl.setX(br.getX());
                    tl.setY(br.getY());
                    br = tl.plus(size);
                }

                for (int fIx = 0; fIx < components.length; fIx++) {
                    for (int tIx = 0; tIx < components.length; tIx++) {
                        if (fIx != tIx && components[fIx].allowedDependency(components[tIx])) {
                            final int color = COL32(175, 175, 175, 255);
                            if (fIx < tIx) {
                                float x, y;
                                x = r.getTl().getX() + size.getX() * (fIx + 1);
                                y = r.getTl().getY() + size.getY() * (fIx + 1) - size.getY() / 4;
                                Vec2 start = new Vec2(x, y);

                                x = r.getTl().getX() + size.getX() * tIx + size.getX() / 4;

                                Vec2 end = new Vec2(x, y);

                                imgui.getWindowDrawList().addLine(start, end, color, 1.0f);

                                y = r.getTl().getY() + size.getY() * tIx;
                                start = new Vec2(x, y);
                                imgui.getWindowDrawList().addLine(end, start, color, 1.0f);
                                x = start.getX();
                                y = start.getY();

                                y -= 10;
                                x -= 7;
                                Vec2 p1 = new Vec2(x, y);
                                x += 14;
                                Vec2 p2 = new Vec2(x, y);
                                imgui.getWindowDrawList().addLine(p1, start, color, 1.0f);
                                imgui.getWindowDrawList().addLine(p2, start, color, 1.0f);
                            } else {
                                float x, y;
                                x = r.getTl().getX() + size.getX() * (fIx);
                                y = r.getTl().getY() + size.getY() * fIx + size.getY() / 4;
                                Vec2 start = new Vec2(x, y);

                                x = r.getTl().getX() + size.getX() * (tIx + 1) - size.getX() / 4;

                                Vec2 end = new Vec2(x, y);

                                imgui.getWindowDrawList().addLine(start, end, color, 1.0f);

                                y = r.getTl().getY() + size.getY() * (tIx + 1);
                                start = new Vec2(x, y);
                                imgui.getWindowDrawList().addLine(end, start, color, 1.0f);

                                y += 10;
                                x -= 7;
                                Vec2 p1 = new Vec2(x, y);
                                x += 14;
                                Vec2 p2 = new Vec2(x, y);
                                imgui.getWindowDrawList().addLine(p1, start, color, 1.0f);
                                imgui.getWindowDrawList().addLine(p2, start, color, 1.0f);
                            }
                        }
                    }
                }
            }

            //imgui.text("Hello from another window!");
            //if (imgui.button("Close Me", new Vec2()))
            //    showAnotherWindow[0] = false;
            imgui.end();
        }


        // Rendering
        gln.GlnKt.glViewport(window.getFramebufferSize());
        gln.GlnKt.glClearColor(clearColor);
        glClear(GL_COLOR_BUFFER_BIT);

        imgui.render();
        implGL3.renderDrawData(imgui.getDrawData());

        gln.GlnKt.checkError("loop", true); // TODO remove
    }
}
