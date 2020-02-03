import experimenting.ExperimentsView;
import glm_.vec2.Vec2;
import glm_.vec2.Vec2i;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.*;
import imgui.classes.Context;
import imgui.classes.IO;
import imgui.font.Font;
import imgui.font.FontConfig;
import imgui.impl.gl.ImplGL3;
import imgui.impl.glfw.ImplGlfw;
import imgui.internal.classes.TextEditState;
import kotlin.Unit;
import org.lwjgl.system.MemoryStack;
import se.lnu.siq.s4rdm3x.GUIConsole;
import se.lnu.siq.s4rdm3x.StringCommandHandler;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.CGraph;
import uno.glfw.GlfwWindow;
import uno.glfw.VSync;
import uno.glfw.windowHint;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    // The window handle
    private GlfwWindow window;
    private uno.glfw.glfw glfw = uno.glfw.glfw.INSTANCE;
    private ImplGlfw  implGlfw;
    private ImplGL3  implGl3;
    private ImGui imgui = ImGui.INSTANCE;
    private IO io;
    private Context ctx;



    public static void main(String[] a_args) {
        Main pt = new Main();

        pt.run();
    }

    private Main() {

        glfw.init("3.3", windowHint.Profile.core, true);

        window = new GlfwWindow(1280, 720, "Visual 3xperiment Tool", NULL, new Vec2i(Integer.MIN_VALUE), true);
        window.init(true);

        glfw.setSwapInterval(VSync.ON);    // Enable vsync

        // Setup ImGui binding
        //setGlslVersion(330); // set here your desidered glsl version
        ctx = new Context(null);
        //io.configFlags = io.configFlags or ConfigFlag.NavEnableKeyboard  // Enable Keyboard Controls
        //io.configFlags = io.configFlags or ConfigFlag.NavEnableGamepad   // Enable Gamepad Controls
        //implGlfw.init(window, true, implGlfw..GlfwClientApi.OpenGL);

        TextEditState tes = new TextEditState();
        //tes.setBufSizeA();
        ctx.getInputTextState().setBufCapacityA(2048);
        io = imgui.getIo();

        imgui.styleColorsDark(null);


        implGlfw = new ImplGlfw(window, true, null);
        implGl3 = new ImplGL3();
    }

    private void run() {

        GUIConsole guic = new GUIConsole();
        StringCommandHandler sch = new StringCommandHandler();
        CGraph graph = new CGraph();

        ArchDef theArch = new ArchDef();

        try {
            // TODO: Load this as a resource maybe?
            /*FontConfig fc = new FontConfig();
            byte[] bytes;
            Class c = getClass();
            ClassLoader cl = c.getClassLoader();
            //InputStream is = cl.getResourceAsStream("imgui/src/main/resources/fonts/Roboto-Medium.ttf");
            bytes =  Files.readAllBytes(Paths.get("data/resources/fonts/Roboto-Medium.ttf"));
            char [] chars = new char[bytes.length];
            for (int ix = 0; ix < chars.length; ix++) {
                chars[ix] = (char)bytes[ix];
            }

            Font f = imgui.getIo().getFonts().addFontFromMemoryTTF(chars, 24, fc, new int[] {0x0020, 0x00FF} );
            imgui.setCurrentFont(f);*/
        } catch (Exception e) {
            System.out.println("Could not load font resource, using default");
        }


        window.loop((MemoryStack stack)  -> {

            if (guic.hasInput()) {

                String in = guic.popInput();
                sch.execute(in, graph).forEach(str -> guic.println(str));
            }

            mainLoop(sch.getArchDef() != null ? sch.getArchDef() : theArch, graph);

            return Unit.INSTANCE;
        });

        guic.close();
        implGlfw.shutdown();
        implGl3.shutdown();
        ctx.destroy();

        window.destroy();
        glfw.terminate();
    }

    private float[] f = {1f};
    private Vec4 clearColor = new Vec4(0.45f, 0.55f, 0.6f, 1f);
    private boolean[] showExperimentView = {true};
    private int[] counter = {0};

    private ExperimentsView m_experimentsView = new ExperimentsView();


    private void mainLoop(ArchDef a_arch, CGraph a_g) {
        // Start the Dear ImGui frame
        implGl3.newFrame();
        implGlfw.newFrame();
        imgui.newFrame();


        //imgui.showDemoWindow(showDemo);

        imgui.text("Application average %.3f ms/frame (%.1f FPS)", 1_000f / io.getFramerate(), io.getFramerate());


        if (showExperimentView[0]) {
            if (imgui.begin("Experiment View", showExperimentView, 0)) {

                m_experimentsView.doView(new ImGuiWrapper(imgui));
                String selectedNodeLogicName = m_experimentsView.getSelectedNodeLogicName();
                imgui.end();
            }
        }

        // Rendering
        imgui.render();
        gln.GlnKt.glViewport(window.getFramebufferSize());
        gln.GlnKt.glClearColor(clearColor);
        glClear(GL_COLOR_BUFFER_BIT);
        implGl3.renderDrawData(imgui.getDrawData());

        //gln.GlnKt.checkError("loop", true); // render errors only good when debugging.
    }
}
