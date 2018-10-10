import archviz.HRoot;
import glm_.vec2.Vec2;
import glm_.vec2.Vec2i;
import glm_.vec4.Vec4;
import imgui.*;
import imgui.impl.ImplGL3;
import imgui.impl.LwjglGlfw;
import imgui.internal.Rect;
import kotlin.Unit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.GUIConsole;
import se.lnu.siq.s4rdm3x.StringCommandHandler;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import uno.glfw.GlfwWindow;
import uno.glfw.windowHint;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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

        // testing
        //sch.execute("load_arch data/systems/teammates/teammates-system_model.txt", graph).forEach(str -> guic.println(str));

        HuGMe.ArchDef theArch = new HuGMe.ArchDef();
        //theArch.addComponent("client.part1.part1_1");

        //theArch.addComponent("global");
        theArch.addComponent("part1");
        theArch.addComponent("part2");

        //theArch.addComponent("client");


        /*theArch.addComponent("optional");
        theArch.addComponent("compilers");
        theArch.addComponent("condition");
        theArch.addComponent("rmic");
        theArch.addComponent("cvslib");
        theArch.addComponent("email");
        theArch.addComponent("repository");
        theArch.addComponent("taskdefs");
        theArch.addComponent("listener");
        theArch.addComponent("types");*/
        //theArch.addComponent("ant");
        //theArch.addComponent("ant.util");
        /*theArch.addComponent("zip");
        theArch.addComponent("tar");
        theArch.addComponent("mail");
        theArch.addComponent("bzip2");*/


       /*
        try {
            FontConfig fc = new FontConfig();
            byte[] bytes;
            Class c = getClass();
            ClassLoader cl = c.getClassLoader();
            InputStream is = cl.getResourceAsStream("imgui/src/main/resources/fonts/Roboto-Medium.ttf");
            bytes =  Files.readAllBytes(Paths.get("data/visuals/fonts/Roboto-Medium.ttf"));
            char [] chars = new char[bytes.length];
            for (int ix = 0; ix < chars.length; ix++) {
                chars[ix] = (char)bytes[ix];
            }

            Font f = imgui.getIo().getFonts().addFontFromMemoryTTF(chars, 8, fc,new int[] {0x0020, 0x00FF} );
            imgui.setCurrentFont(f);
           // System.out.println("" + bytes[0]);
        } catch (Exception e) {
            System.out.println(e);
        }
        */


        window.loop(() -> {

            if (guic.hasInput()) {

                String in = guic.popInput();
                sch.execute(in, graph).forEach(str -> guic.println(str));
            }

            mainLoop(sch.getArchDef() != null ? sch.getArchDef() : theArch);
            return Unit.INSTANCE;
        });

        guic.close();
        lwjglGlfw.shutdown();
        ContextKt.destroy(ctx);

        window.destroy();
        glfw.terminate();
    }

    private float[] f = {1f};
    private Vec4 clearColor = new Vec4(0.45f, 0.55f, 0.6f, 1f);
    private boolean[] showAnotherWindow = {true};
    private int[] counter = {0};

    HRoot.State m_vizState = new HRoot.State();

    private void getRectTopLeft(int a_index, Vec2 a_offset, Vec2 a_size, Vec2 a_outTopLeftCorner) {
        a_offset.plus(a_size.times(a_index, a_outTopLeftCorner), a_outTopLeftCorner);
    }

    private boolean equalToLevel(final int a_level, HuGMe.ArchDef.Component a_c1, HuGMe.ArchDef.Component a_c2) {
        String [] names1 = getLevels(a_c1);
        String [] names2 = getLevels(a_c2);
        if (a_level < names1.length && a_level < names2.length) {
            for (int ix = 0; ix <= a_level; ix++) {
                if (!names1[ix].contentEquals(names2[ix])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private String [] getLevels(HuGMe.ArchDef.Component a_c) {
        return a_c.getName().split("\\.");
    }

    private void drawComponentRects2(ImGui a_imgui, HuGMe.ArchDef.Component[] a_components) {
        Rect r = imgui.getCurrentWindow().getInnerClipRect();
        Vec2 tl = new Vec2();
        Vec2 size = new Vec2(r.getWidth() / a_components.length, r.getHeight() / a_components.length);
        Vec2 br = new Vec2();
        br = br.plus(tl);
        int maxLevel = 0;

        for (HuGMe.ArchDef.Component c : a_components) {
            if (maxLevel < getLevels(c).length) {
                maxLevel = getLevels(c).length;
            }
        }
        for (int level = 0; level < maxLevel; level++) {

            for (int ix = 0; ix < a_components.length;) {
                HuGMe.ArchDef.Component component = a_components[ix];

                String[] names = getLevels(component);
                if (names.length < maxLevel - level) {
                    ix++;
                    continue;
                }

                getRectTopLeft(ix, r.getTl(), size, tl);

                int add = 0;
                while(ix + add < a_components.length && equalToLevel(level, component, a_components[ix + add])) {
                    add++;
                }
                if (add == 0) {
                    add = 1;
                }

                getRectTopLeft(ix + add, r.getTl(), size, br);

                a_imgui.getWindowDrawList().addRectFilled(tl, br, COL32(75, 75, 75, 255), 1, 0);
                a_imgui.getWindowDrawList().addRect(tl, br, COL32(175, 175, 175, 255), 1, 0, 2.0f);

                String name = component.getName();

                if (names.length > level) {
                    name = names[names.length - level - 1];
                }
                Vec2 textSize = a_imgui.calcTextSize(name, false);
                textSize.div(2);
                Vec2 textPos = tl.plus(size.div(2).minus(textSize.div(2)));

                a_imgui.getWindowDrawList().addText(textPos, COL32(175, 175, 175, 255), name.toCharArray(), name.length());
                ix += add;
            }
        }
    }

    private void drawComponentRects(ImGui a_imgui, HuGMe.ArchDef.Component[] a_components) {
        Rect r = imgui.getCurrentWindow().getInnerClipRect();
        Vec2 tl = new Vec2(r.getTl());
        Vec2 size = new Vec2(r.getWidth() / a_components.length, r.getHeight() / a_components.length);
        Vec2 br = new Vec2(size);
        br = br.plus(tl);


        for (HuGMe.ArchDef.Component component : a_components) {
            a_imgui.getWindowDrawList().addRectFilled(tl, br, COL32(75, 75, 75, 255), 1, 0);
            a_imgui.getWindowDrawList().addRect(tl, br, COL32(175, 175, 175, 255), 1, 0, 2.0f);

            String name = component.getName();
            name = name.replace(".", ".\n");
            Vec2 textSize = a_imgui.calcTextSize(name, false);
            textSize.div(2);
            Vec2 textPos = tl.plus(size.div(2).minus(textSize.div(2)));

            a_imgui.getWindowDrawList().addText(textPos, COL32(175, 175, 175, 255), name.toCharArray(), name.length());
            tl.setX(br.getX());
            tl.setY(br.getY());
            br = tl.plus(size);
        }
    }

    private void drawComponentDependencies(ImGui a_imgui, HuGMe.ArchDef.Component[] a_components) {
        Rect r = imgui.getCurrentWindow().getInnerClipRect();
        Vec2 size = new Vec2(r.getWidth() / a_components.length, r.getHeight() / a_components.length);
        for (int fIx = 0; fIx < a_components.length; fIx++) {
            for (int tIx = 0; tIx < a_components.length; tIx++) {
                if (fIx != tIx && a_components[fIx].allowedDependency(a_components[tIx])) {
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

    private void drawComponentDependencies2(ImGui a_imgui, HuGMe.ArchDef.Component[] a_components) {
        Rect r = imgui.getCurrentWindow().getInnerClipRect();
        Vec2 size = new Vec2(r.getWidth() / a_components.length, r.getHeight() / a_components.length);
        for (int fIx = 0; fIx < a_components.length; fIx++) {
            for (int tIx = 0; tIx < a_components.length; tIx++) {
                if (fIx != tIx && a_components[fIx].allowedDependency(a_components[tIx])) {
                    final int color = COL32(175, 175, 175, 255);
                    if (fIx < tIx) {
                        float x, y;
                        x = r.getTl().getX() + size.getX() * (fIx + 1);
                        y = r.getTl().getY() + size.getY() * (fIx + 1) - size.getY() / (a_components.length - fIx) * (tIx - fIx);
                        Vec2 start = new Vec2(x, y);

                        x = r.getTl().getX() + size.getX() * tIx + size.getX() / (tIx + 1 ) * (tIx - fIx);

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
                        y = r.getTl().getY() + size.getY() * fIx + size.getY() / (fIx + 1) * (fIx - tIx);
                        Vec2 start = new Vec2(x, y);

                        x = r.getTl().getX() + size.getX() * (tIx + 1) - size.getX() / (a_components.length - tIx) * (fIx - tIx);

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

    private void mainLoop(HuGMe.ArchDef a_arch) {
        // Start the Dear ImGui frame
        lwjglGlfw.newFrame();

        imgui.text("Hello, world!");                                // Display some text (you can use a format string too)
        imgui.sliderFloat("float", f, 0.25f, 5f, "%.3f", 1f);       // Edit 1 float using a slider from 0.0f to 1.0f
        imgui.getFont().setScale(f[0]);
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
                HRoot root = new HRoot();
                for (int ix = 0; ix < components.length; ix++) {
                    components[ix] = a_arch.getComponent(ix);
                    root.add(components[ix].getName());
                }

                for (int sIx = 0; sIx < components.length; sIx++) {
                    for (int dIx = 0; dIx < components.length; dIx++) {
                        if (components[sIx].allowedDependency(components[dIx])) {
                            root.addDependency(components[sIx].getName(), components[dIx].getName());
                        }
                    }
                }

                //Arrays.sort(components, (o1, o2) -> o2.getAllowedDependencyCount() - o1.getAllowedDependencyCount());

                Rect r = imgui.getCurrentWindow().getContentsRegionRect();
                Vec2 tl = new Vec2(r.getTl());
                Vec2 size = new Vec2(r.getWidth() / components.length, r.getHeight() / components.length);


                HRoot.Action action = root.render(r, imgui, m_vizState);

                if (action != null && action.m_addComponent != null) {
                    a_arch.addComponent(action.m_addComponent);
                }

                if (action != null && action.m_deletedComponents != null) {
                    for (String cName : action.m_deletedComponents) {
                        a_arch.removeComponent(a_arch.getComponent(cName));
                    }
                }

                // we need to do resorting before renaming
                if (action != null && action.m_nodeOrder != null) {
                    ArrayList<HuGMe.ArchDef.Component> newOrder = new ArrayList<>();

                    for(String name : action.m_nodeOrder) {
                        newOrder.add(a_arch.getComponent(name));
                    }
                    a_arch.clear();
                    for(HuGMe.ArchDef.Component c : newOrder) {
                        a_arch.addComponent(c);
                    }
                }

                // add dependenices
                if (action != null && action.m_addDependenices != null) {
                    for(HRoot.Action.NodeNamePair pair : action.m_addDependenices.m_nodes) {
                        HuGMe.ArchDef.Component sC = a_arch.getComponent(pair.m_oldName);
                        HuGMe.ArchDef.Component tC = a_arch.getComponent(pair.m_newName);
                        if (tC == null) {
                            System.out.println("Could not find component named: " + pair.m_newName);
                        } else if (sC == null) {
                            System.out.println("Could not find component named: " + pair.m_oldName);
                        } else {
                            sC.addDependencyTo(tC);
                        }
                    }
                }

                // remove dependenices
                if (action != null && action.m_removeDependencies != null) {
                    for(HRoot.Action.NodeNamePair pair : action.m_removeDependencies.m_nodes) {
                        HuGMe.ArchDef.Component sC = a_arch.getComponent(pair.m_oldName);
                        HuGMe.ArchDef.Component tC = a_arch.getComponent(pair.m_newName);
                        if (tC == null) {
                            System.out.println("Could not find component named: " + pair.m_newName);
                        } else if (sC == null) {
                            System.out.println("Could not find component named: " + pair.m_oldName);
                        } else {
                            sC.removeDependencyTo(tC);
                        }
                    }
                }

                // node renaming
                if (action != null && action.m_hiearchyMove != null) {
                    for(HRoot.Action.NodeNamePair pair : action.m_hiearchyMove.m_nodes) {
                        HuGMe.ArchDef.Component c = a_arch.getComponent(pair.m_oldName);
                        if (c == null) {
                            System.out.println("Could not find component named: " + pair.m_oldName);
                        } else {
                            a_arch.setComponentName(c, pair.m_newName);
                        }
                        //a_arch.setComponentName(c, "dret");
                        //System.out.println("dret");
                    }
                }


                //drawComponentRects2(imgui, components);
                //drawComponentDependencies2(imgui, components);


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
