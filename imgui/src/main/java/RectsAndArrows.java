import archviz.HRoot;
import glm_.vec2.Vec2;
import glm_.vec2.Vec2i;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.*;
import imgui.impl.ImplGL3;
import imgui.impl.LwjglGlfw;
import imgui.internal.Rect;
import kotlin.Unit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.GUIConsole;
import se.lnu.siq.s4rdm3x.StringCommandHandler;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.NodeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import uno.glfw.GlfwWindow;
import uno.glfw.windowHint;

import java.util.ArrayList;
import java.util.Arrays;

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

        TextEditState tes = new TextEditState();
        //tes.setBufSizeA();
        ctx.getInputTextState().setBufSizeA(2048);
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
        theArch.addComponent("client.part1");
        theArch.addComponent("client.part2");
        theArch.addComponent("server.part3");
        theArch.addComponent("server.part4");

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

       archviz.Command c = new archviz.Command(m_vizState);
       sch.addCommand(c);


        window.loop(() -> {

            if (guic.hasInput()) {

                String in = guic.popInput();
                sch.execute(in, graph).forEach(str -> guic.println(str));
            }

            mainLoop(sch.getArchDef() != null ? sch.getArchDef() : theArch, graph);
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
    private boolean[] showArchitecture = {true};
    private boolean[] showTreeView = {true};
    private int[] counter = {0};

    private int[] treeViewSelection = {0};
    private String[] treeViewRoots = {"", "", ""};

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

    private void mainLoop(HuGMe.ArchDef a_arch, Graph a_g) {
        // Start the Dear ImGui frame
        lwjglGlfw.newFrame();

        imgui.text("Hello, world!");                                // Display some text (you can use a format string too)
        imgui.sliderFloat("float", f, 0.25f, 5f, "%.3f", 1f);       // Edit 1 float using a slider from 0.0f to 1.0f
        imgui.getFont().setScale(f[0]);
        imgui.colorEdit3("clear color", clearColor, 0);               // Edit 3 floats representing a color

        imgui.checkbox("Architectural Structure", showArchitecture);
        imgui.checkbox("Tree View", showTreeView);

        if (imgui.button("Button", new Vec2()))                               // Buttons return true when clicked (NB: most widgets return true when edited/activated)
            counter[0]++;

        imgui.sameLine(0f, -1f);
        imgui.text("counter = $counter");

        imgui.text("Application average %.3f ms/frame (%.1f FPS)", 1_000f / io.getFramerate(), io.getFramerate());

        // 2. Show another simple window. In most cases you will use an explicit begin/end pair to name the window.
        if (showArchitecture[0]) {
            imgui.begin("Architectural Structure", showArchitecture, 0);
            doArchStructure(a_arch, treeViewRoots[0]);
            imgui.end();
        }

        if (showTreeView[0]) {
            if (imgui.begin("Tree Views", showTreeView, 0)) {
                hiviz.Tree tree = new hiviz.Tree();
                ArrayList<String> items = new ArrayList<>();
                items.add("Architecture");
                items.add("Classes");
                items.add("Files");

                if (imgui.combo("", treeViewSelection, items, items.size())) {
                }

                ImGuiWrapper iw = new ImGuiWrapper(imgui);
                treeViewRoots[treeViewSelection[0]] = iw.inputTextSingleLine("Root", treeViewRoots[treeViewSelection[0]]);

                switch (treeViewSelection[0]) {
                    case 0: {
                        for(HuGMe.ArchDef.Component c : a_arch.getComponents()) {
                            if (c.getName().startsWith(treeViewRoots[0]))
                            tree.addNode(c.getName());
                        }
                    } break;
                    case 1: {
                        AttributeUtil au = new AttributeUtil();
                        for (Node n : a_g.getEachNode()) {

                            HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);

                            for (dmClass c : au.getClasses(n)) {
                                if (!c.isInner()) {
                                    if (c.getName().startsWith(treeViewRoots[1])) {

                                        Tree.TNode tn = tree.addNode(c.getName());
                                        if (component != null) {
                                            tn.setName(tn.getName() + " (" + component.getName() + ")");
                                        }
                                    }
                                } else {
                                    // tree.addNode(c.getName().replace("$", ".") + " (inner class)");
                                }
                            }
                        }
                    } break;
                    case 2: {
                    } break;
                }


                //NodeUtil nu = new NodeUtil(a_g);



                tree.doTree(imgui);

                imgui.end();
            }
        }


        // Rendering
        gln.GlnKt.glViewport(window.getFramebufferSize());
        gln.GlnKt.glClearColor(clearColor);
        glClear(GL_COLOR_BUFFER_BIT);

        imgui.render();
        implGL3.renderDrawData(imgui.getDrawData());

        gln.GlnKt.checkError("loop", true); // TODO remove
    }


    private void doArchStructure(HuGMe.ArchDef a_arch, String a_rootComponentFilter) {
        if (a_arch !=  null) {

            HRoot root = new HRoot();
            for (HuGMe.ArchDef.Component c : a_arch.getComponents()) {

                if (c.getName().startsWith(a_rootComponentFilter)) {
                    root.add(c.getName());
                }
            }

            for (HuGMe.ArchDef.Component from : a_arch.getComponents()) {
                for (HuGMe.ArchDef.Component to : a_arch.getComponents()) {
                    if (from.allowedDependency(to)) {
                        if (from.getName().startsWith(a_rootComponentFilter) && to.getName().startsWith(a_rootComponentFilter)) {
                            root.addDependency(from.getName(), to.getName());
                        }
                    }
                }
            }

            //Arrays.sort(components, (o1, o2) -> o2.getAllowedDependencyCount() - o1.getAllowedDependencyCount());

            Rect r = imgui.getCurrentWindow().getContentsRegionRect();

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
                for(HRoot.Action.NodeNamePair pair : action.m_addDependenices.getPairs()) {
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
                for(HRoot.Action.NodeNamePair pair : action.m_removeDependencies.getPairs()) {
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
                for(HRoot.Action.NodeNamePair pair : action.m_hiearchyMove.getPairs()) {
                    HuGMe.ArchDef.Component c = a_arch.getComponent(pair.m_oldName);
                    if (c == null) {
                        System.out.println("Could not find component named: " + pair.m_oldName);
                    } else {
                        a_arch.setComponentName(c, pair.m_newName);
                    }
                }
            }
        }
    }
}
