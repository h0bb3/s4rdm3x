import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.ImGui;
import imgui.WindowFlag;
import imgui.internal.Rect;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static imgui.ImguiKt.COL32;

public class GraphView {


    private boolean[] m_doFreeze = {false};
    private float[] m_collisionRadiusMultiplier = {2};
    private float[] m_collisionForceMultiplier = {8};
    private float[] m_scale = {1};

    private Vec2 m_lastSize = new Vec2();

    static class Particle {
        Vec2 m_pos = new Vec2();
        Vec2 m_force = new Vec2();
        Vec2 m_v = new Vec2();
        Vec2 m_a = new Vec2();
        float m_mass = 2.0f;
        float m_radius = 8.0f;
        boolean m_isAlive = false;
        protected String m_name;
        int m_color = COL32(175, 175, 175, 255);

        public void setName(String a_name) {
            m_name = a_name;
        }

        public void update(float a_dt) {
            m_force.div(m_mass, m_a);
            m_v = m_v.plus(m_a.times(a_dt));
            m_force.setX(m_v.getX() /** m_v.getX()*/ * -1.5f);
            m_force.setY(m_v.getY() /** m_v.getY()*/ * -1.5f);
            //m_force.set(0, 0);
            m_pos.plus(m_v.times(a_dt), m_pos);
        }

        public void render(ImGuiWrapper a_imgui, Vec2 a_offset, float a_scale) {
            a_imgui.addCircle(a_offset.plus(m_pos.times(a_scale)), m_radius * a_scale, m_color, 4, 1.0f);
            m_isAlive = false;
        }
    }

    static class FileParticle extends Particle {

        Vec2 m_size = new Vec2(m_radius, m_radius);

        Particle m_attractedTo = null;

        private static Vec2 g_tl = new Vec2(), g_br = new Vec2();
        private static Vec2 g_p = new Vec2();

        public void setSize(Vec2 a_size) {
            m_size.setX(a_size.getX() / 2);
            m_size.setY(a_size.getY() / 2);
            m_radius = a_size.length();
        }

        public void render(ImGuiWrapper a_imgui, Vec2 a_offset, float a_scale) {

            m_pos.times(a_scale, g_p).plus(a_offset, g_p);

            g_tl.setX(g_p.getX() - m_size.getX());
            g_tl.setY(g_p.getY() - m_size.getY());

            g_br.setX(g_p.getX() + m_size.getX());
            g_br.setY(g_p.getY() + m_size.getY());

            a_imgui.addRect(g_tl, g_br, m_color, 0, 0, 1);
            a_imgui.addText(g_tl, m_color, m_name);
            //a_imgui.addCircle(a_offset.plus(m_pos), m_radius, m_color, 4, 1.0f);
            //a_imgui.getWindowDrawList().addLine(a_offset.plus(m_pos), a_offset.plus(m_pos.plus(m_force)), m_color, 1);
            m_isAlive = false;
        }
    }

    HashMap<Integer, FileParticle> m_nodes = new HashMap<>();

    ArrayList<FileParticle> m_arNodes = new ArrayList<>();

    FileParticle[] m_fileParticles = new FileParticle[0];
    Particle[] m_componentParticles = new Particle[0];


    void doGraphView(ImGui a_imgui, Iterable<Node> a_nodes, HuGMe.ArchDef a_arch, archviz.HNode.VisualsManager a_nvm, float a_dt) {
        ImGuiWrapper imgui = new ImGuiWrapper(a_imgui);

        imgui.imgui().beginColumns("graphviewcolumns", 2, 0);


        imgui.imgui().checkbox("Freeze Updates", m_doFreeze);
        imgui.imgui().sliderFloat("Collision Radius", m_collisionRadiusMultiplier, 1, 20, "%.2f", 1);
        imgui.imgui().sliderFloat("Collision Force", m_collisionForceMultiplier, 1, 50, "%.2f", 1);
        imgui.imgui().sliderFloat("Scale", m_scale, 0.001f, 10, "%.2f", 1);

        final float scale = m_scale[0];

        imgui.imgui().nextColumn();

        AttributeUtil au = new AttributeUtil();

        for (int cIx = 0; cIx < a_arch.getComponentCount(); cIx++) {
            HuGMe.ArchDef.Component c = a_arch.getComponent(cIx);

            if (m_componentParticles.length < cIx + 1) {
                //m_arNodes.ensureCapacity(n.getIndex() + 1);
                m_componentParticles = Arrays.copyOf(m_componentParticles, cIx + 1);
                m_componentParticles[cIx] = new Particle();

                initParticle(m_componentParticles[cIx], c.getName(), imgui.toColor(a_nvm.getBGColor(c.getName())));
                System.out.println(c.getName());


                setFileParticleAttractions(a_nodes, m_componentParticles[cIx], c);
            }

            Particle cp = m_componentParticles[cIx];
            cp.m_isAlive = true;

            // colors may have changed
            if (cp.m_color != imgui.toColor(a_nvm.getBGColor(c.getName()))) {
                cp.m_color = imgui.toColor(a_nvm.getBGColor(c.getName()));

                for (int fpIx = 0; fpIx < m_fileParticles.length; fpIx++) {
                    if (m_fileParticles[fpIx].m_attractedTo == cp) {
                        m_fileParticles[fpIx].m_color = cp.m_color;
                    }
                }
            }

            // component has changed name or a new component has been added/loaded
            if (cp.m_name != c.getName()) {
                initParticle(cp, c.getName(), imgui.toColor(a_nvm.getBGColor(c.getName())));

                for (int fpIx = 0; fpIx < m_fileParticles.length; fpIx++) {
                    if (m_fileParticles[fpIx].m_attractedTo == cp) {
                        m_fileParticles[fpIx].m_attractedTo = null;
                    }
                }

                setFileParticleAttractions(a_nodes, cp, c);
            }

        }

        for(Node n : a_nodes) {
            FileParticle gn;
            if (m_fileParticles.length < n.getIndex() + 1) {
                //m_arNodes.ensureCapacity(n.getIndex() + 1);
                m_fileParticles = Arrays.copyOf(m_fileParticles, n.getIndex() + 1);
                String name = "";
                for (dmClass c : au.getClasses(n)) {
                    if (!c.isInner()) {
                        name = c.getClassName();
                    }
                }
                Vec2 size = imgui.imgui().calcTextSize(name, false);
                gn = new FileParticle();
                initParticle(gn, name);
                gn.setSize(size);

                for (int cIx = 0; cIx < a_arch.getComponentCount(); cIx++) {
                    HuGMe.ArchDef.Component c = a_arch.getComponent(cIx);

                    if (c.isMappedTo(n)) {
                        gn.m_attractedTo = m_componentParticles[cIx];
                        gn.m_color = m_componentParticles[cIx].m_color;
                    }
                }


                m_fileParticles[n.getIndex()] = gn;
                System.out.println("Allocated memory: " + m_fileParticles.length);
            } else {
                gn = m_fileParticles[n.getIndex()];
            }

            gn.m_isAlive = true;

            if (m_doFreeze[0] != true) {
                if (gn.m_attractedTo != null) {
                    Vec2 toAttraction = gn.m_attractedTo.m_pos.minus(gn.m_pos);
                    toAttraction.times(0.25f, toAttraction);
                    gn.m_force.plus(toAttraction, gn.m_force);
                }
            }
        }

        if (m_doFreeze[0] != true) {
            computeIntraParticleCollisionForces(m_componentParticles);
            computeIntraParticleCollisionForces(m_fileParticles);
        }

        float minX = 1000, minY = 1000;
        float maxX = -1000, maxY = -1000;
        float x, y;
        for (int p1Ix = 0; p1Ix < m_fileParticles.length; p1Ix++) {
            FileParticle p = m_fileParticles[p1Ix];
            if (m_doFreeze[0] != true) {
                p.update(a_dt);
            }
            x = p.m_pos.getX();
            y = p.m_pos.getY();

            if (x - p.m_radius < minX) {
                minX = x - p.m_radius;
            } else if (x + p.m_radius > maxX) {
                maxX = x + p.m_radius;
            }

            if (y - p.m_radius < minY) {
                minY = y - p.m_radius;
            } else if (y + p.m_radius> maxY) {
                maxY = y + p.m_radius;
            }
        }

        Vec2 size = new Vec2((maxX - minX) * scale, (maxY - minY) * scale);


        Vec2 offset = size.times(0.5f);


        Vec2 columnSize = new Vec2(imgui.imgui().getColumnWidth(1) - 10, (float)imgui.imgui().getWindowContentRegionMax().getY() - 40);

        m_lastSize.setX(columnSize.getX());
        m_lastSize.setY(columnSize.getY());

        imgui.imgui().beginChild("particles_parent", columnSize, true, WindowFlag.HorizontalScrollbar.getI());

        imgui.imgui().beginChild("particles", size, true, 0);

        Vec2 winPos = imgui.imgui().getCurrentWindow().getParentWindow().getPos();
        Rect winRect = new Rect(winPos, winPos.plus(imgui.imgui().getCurrentWindow().getParentWindow().getSizeContents()));
        Vec2 mousePos = imgui.getMousePos();
        if (imgui.isInside(winRect, mousePos)) {
            if (imgui.isMouseDragging(0, 0)) {
                imgui.stopWindowDrag();
                imgui.imgui().getCurrentWindow().getParentWindow().setScroll(imgui.imgui().getCurrentWindow().getParentWindow().getScroll().plus(imgui.imgui().getIo().getMouseDelta().negate()));
            }
        }

        offset = offset.plus(imgui.imgui().getCurrentWindow().getPos());

        imgui.addCircle(imgui.imgui().getWindowPos(), 10, COL32(175, 255, 175, 255), 16, 1.0f);
        imgui.addCircle(offset, 10, COL32(255, 175, 175, 255), 16, 1.0f);

        imgui.addText(offset, COL32(255, 175, 175, 255), "Particules Be Here");

        renderParticles(m_fileParticles, imgui, offset, scale);
        renderParticles(m_componentParticles, imgui, offset, scale);
        imgui.imgui().endChild();
        imgui.imgui().endChild();

        imgui.imgui().endColumns();
    }

    private void setFileParticleAttractions(Iterable<Node> a_nodes, Particle a_source, HuGMe.ArchDef.Component a_c) {
        for(Node n : a_nodes) {
            if (n.getIndex() < m_fileParticles.length) {
                if (a_c.isMappedTo(n)) {
                    m_fileParticles[n.getIndex()].m_attractedTo = a_source;
                    m_fileParticles[n.getIndex()].m_color = a_source.m_color;
                }
            }
        }
    }

    private void renderParticles(Particle[] a_particles, ImGuiWrapper a_imgui, Vec2 offset, float a_scale) {
        for (int p1Ix = 0; p1Ix < a_particles.length; p1Ix++) {
            Particle p = a_particles[p1Ix];
            p.render(a_imgui, offset, a_scale);
        }
    }

    private void computeIntraParticleCollisionForces(Particle [] a_particles) {
        Vec2 p12p2 = new Vec2();
        for (int p1Ix = 0; p1Ix < a_particles.length - 1; p1Ix++) {
            Particle p1 = a_particles[p1Ix];
            if (p1.m_isAlive) {

                for (int p2Ix = p1Ix + 1; p2Ix < a_particles.length; p2Ix++) {
                    Particle p2 = a_particles[p2Ix];
                    if (p2.m_isAlive) {

                        p2.m_pos.minus(p1.m_pos, p12p2);
                        if (p12p2.length2() < m_collisionRadiusMultiplier[0] * (p1.m_radius + p2.m_radius) * (p1.m_radius + p2.m_radius)) {

                            //Vec2 f = calcSpringForce(0.1f, 0.1f, 200, p1.m_pos, p2.m_pos, p1.m_v, p2.m_v);
                            float length = p12p2.length();
                            p12p2.div(length, p12p2);
                            //p12p2.normalizeAssign();

                            p12p2.times(m_collisionForceMultiplier[0] * (1.0f / length), p12p2);

                            p2.m_force.plus(p12p2, p2.m_force);
                            p1.m_force.plus(p12p2.negate(), p1.m_force);
                        }

                        //Vec2 p1ToP2 = p1.m_pos.minus(p2.m_pos);
                        //float length = p1ToP2.length();
                    }
                }
            }
        }
    }

    private void initParticle(Particle a_p, String a_name, int a_color) {
        initParticle(a_p, a_name);
        a_p.m_color = a_color;
    }

    private void initParticle(Particle a_p, String a_name) {
        a_p.m_pos.setX((float)Math.random() * m_lastSize.getX() - m_lastSize.getX() / 2);
        a_p.m_pos.setY((float)Math.random() * m_lastSize.getX() - m_lastSize.getX() / 2);
        a_p.setName(a_name);
    }

}
