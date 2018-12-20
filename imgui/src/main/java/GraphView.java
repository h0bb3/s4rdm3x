import glm_.vec2.Vec2;
import gui.ImGuiWrapper;
import imgui.ImGui;
import imgui.WindowFlag;
import imgui.internal.Rect;
import imgui.internal.Window;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
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
    private float[] m_attractionForceMultiplier = {1};

    private Particle m_dragParticle;

    private Vec2 m_lastSize = new Vec2(300, 300);

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

        public boolean contains(Vec2 a_pos, Vec2 a_tmp) {
            return m_pos.minus(a_pos, a_tmp).length2() < m_radius * m_radius;
        }
    }

    static class FileParticle extends Particle {

        Vec2 m_size = new Vec2(m_radius, m_radius);

        Particle m_attractedTo = null;

        private static Vec2 g_tl = new Vec2(), g_br = new Vec2();
        private static Vec2 g_p = new Vec2();

        public boolean m_drawRed;

        public void setSize(Vec2 a_size) {
            m_size.setX(a_size.getX() / 2);
            m_size.setY(a_size.getY() / 2);
            m_radius = a_size.length();
        }

        public void render(ImGuiWrapper a_imgui, Vec2 a_offset, float a_scale) {

            m_pos.times(a_scale, g_p).plus(a_offset, g_p);

            g_br.setX(g_p.getX() + m_size.getX());
            g_br.setY(g_p.getY() + m_size.getY());

            getTopLeft(g_p, g_tl, a_scale);
            getBottomRight(g_p, g_br, a_scale);


            int color = m_color;
            if (m_drawRed) {
                color = COL32(255, 100, 100, 255);;
                m_drawRed = false;
            }

            a_imgui.addRect(g_tl, g_br, color, 0, 0, 1);
            if (a_scale >= 1) {

                g_tl.plus(m_size.times(a_scale).minus(m_size), g_tl);

                a_imgui.addText(g_tl, m_color, m_name);
            }
            //a_imgui.addCircle(a_offset.plus(m_pos), m_radius, m_color, 4, 1.0f);
            //a_imgui.getWindowDrawList().addLine(a_offset.plus(m_pos), a_offset.plus(m_pos.plus(m_force)), m_color, 1);
            m_isAlive = false;
        }

        public boolean containsRect(Vec2 a_pos) {
            getTopLeft(m_pos, g_tl);
            getBottomRight(m_pos, g_br);

            return a_pos.getX() >= g_tl.getX() && a_pos.getX() <= g_br.getX() && a_pos.getY() >= g_tl.getY() && a_pos.getY() <= g_br.getY();
        }

        public void getTopLeft(Vec2 a_pos, Vec2 a_tl, float a_scale) {
            a_tl.setX(a_pos.getX() - m_size.getX() * a_scale);
            a_tl.setY(a_pos.getY() - m_size.getY() * a_scale);
        }

        public void getBottomRight(Vec2 a_pos, Vec2 a_br, float a_scale) {
            a_br.setX(a_pos.getX() + m_size.getX() * a_scale);
            a_br.setY(a_pos.getY() + m_size.getY() * a_scale);
        }

        public void getTopLeft(Vec2 a_pos, Vec2 a_tl) {
            a_tl.setX(a_pos.getX() - m_size.getX());
            a_tl.setY(a_pos.getY() - m_size.getY());
        }

        public void getBottomRight(Vec2 a_pos, Vec2 a_br) {
            a_br.setX(a_pos.getX() + m_size.getX());
            a_br.setY(a_pos.getY() + m_size.getY());
        }

        public void getTopLeft(Vec2 a_tl) {
            a_tl.setX(m_pos.getX() - m_size.getX());
            a_tl.setY(m_pos.getY() - m_size.getY());
        }

        public void getBottomRight(Vec2 a_br) {
            a_br.setX(m_pos.getX() + m_size.getX());
            a_br.setY(m_pos.getY() + m_size.getY());
        }

        public void getTopLeft(Vec2 a_tl, float a_scale) {
            a_tl.setX(m_pos.getX() - m_size.getX() * a_scale);
            a_tl.setY(m_pos.getY() - m_size.getY() * a_scale);
        }

        public void getBottomRight(Vec2 a_br, float a_scale) {
            a_br.setX(m_pos.getX() + m_size.getX() * a_scale);
            a_br.setY(m_pos.getY() + m_size.getY() * a_scale);
        }
    }

    HashMap<Integer, FileParticle> m_nodes = new HashMap<>();

    ArrayList<FileParticle> m_arNodes = new ArrayList<>();

    FileParticle[] m_fileParticles = new FileParticle[0];
    Particle[] m_componentParticles = new Particle[0];


    void doGraphView(ImGui a_imgui, Iterable<Node> a_nodes, HuGMe.ArchDef a_arch, archviz.HNode.VisualsManager a_nvm, float a_dt) {
        ImGuiWrapper imgui = new ImGuiWrapper(a_imgui);

        AttributeUtil au = new AttributeUtil();

        for (int cIx = 0; cIx < a_arch.getComponentCount(); cIx++) {
            HuGMe.ArchDef.Component c = a_arch.getComponent(cIx);

            if (m_componentParticles.length < cIx + 1) {
                //m_arNodes.ensureCapacity(n.getIndex() + 1);
                m_componentParticles = Arrays.copyOf(m_componentParticles, cIx + 1);
                m_componentParticles[cIx] = new Particle();

                initParticle(m_componentParticles[cIx], c.getName(), imgui.toColor(a_nvm.getBGColor(c.getName())), m_lastSize);
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
                initParticle(cp, c.getName(), imgui.toColor(a_nvm.getBGColor(c.getName())), m_lastSize);

                for (int fpIx = 0; fpIx < m_fileParticles.length; fpIx++) {
                    if (m_fileParticles[fpIx].m_attractedTo == cp) {
                        m_fileParticles[fpIx].m_attractedTo = null;
                    }
                }

                setFileParticleAttractions(a_nodes, cp, c);
            }

        }

        for (Node n : a_nodes) {
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
                initParticle(gn, name, m_lastSize);
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
                if (gn.m_attractedTo != null && gn != m_dragParticle) {
                    Vec2 toAttraction = gn.m_attractedTo.m_pos.minus(gn.m_pos);
                    toAttraction.times(m_attractionForceMultiplier[0], toAttraction);
                    gn.m_force.plus(toAttraction, gn.m_force);
                }
            }
        }

        if (m_doFreeze[0] != true) {
            computeIntraParticleCollisionForces(m_componentParticles);
            computeIntraParticleCollisionForces(m_fileParticles);
        }

        float minX = 10000, minY = 10000;
        float maxX = -10000, maxY = -10000;
        float x, y, r;
        for (int p1Ix = 0; p1Ix < m_fileParticles.length; p1Ix++) {
            FileParticle p = m_fileParticles[p1Ix];
            if (m_doFreeze[0] != true) {
                p.update(a_dt);
            }
            x = p.m_pos.getX() * m_scale[0];
            y = p.m_pos.getY() * m_scale[0];
            r = p.m_radius * m_scale[0];

            if (x - r < minX) {
                minX = x - r;
            } else if (x + r > maxX) {
                maxX = x + r;
            }

            if (y - r < minY) {
                minY = y - r;
            } else if (y + r > maxY) {
                maxY = y + r;
            }
        }

        Vec2 size = new Vec2((maxX - minX) , (maxY - minY));
        m_lastSize = size;

        imgui.imgui().beginColumns("graphviewcolumns", 2, 0);
        imgui.imgui().checkbox("Freeze Updates", m_doFreeze);
        imgui.imgui().sliderFloat("Collision Radius", m_collisionRadiusMultiplier, 1, 20, "%.2f", 1);
        imgui.imgui().sliderFloat("Collision Force", m_collisionForceMultiplier, 1, 1000, "%.2f", 1);
        imgui.imgui().sliderFloat("Attraction Force", m_attractionForceMultiplier, 0, 1, "%.2f", 1);
        //m_scale[0] = m_scale[0];
        imgui.imgui().sliderFloat("Scale", m_scale, 0.001f, 10, "%.2f", 1);

        imgui.imgui().nextColumn();



        Vec2 columnSize = new Vec2(imgui.imgui().getColumnWidth(1) - 10, (float) imgui.imgui().getContentRegionAvail().getY());


        imgui.imgui().beginChild("particles_parent", columnSize, true, WindowFlag.HorizontalScrollbar.getI());
        Window particlesParent = imgui.imgui().getCurrentWindow();


        columnSize = imgui.imgui().getContentRegionAvail();

        final boolean dragX = size.getX() <= columnSize.getX() ? false : true;
        final boolean dragY = size.getY() <= columnSize.getY() ? false : true;
        if (!dragX) {
            size.setX(columnSize.getX());
        }
        if (!dragY) {
            size.setY(columnSize.getY());
        }

        Vec2 offset = size.times(0.5f);

        imgui.imgui().beginChild("particles", size, true, 0);
        offset = offset.plus(imgui.imgui().getCurrentWindow().getPos());

        Vec2 winPos = particlesParent.getPos();
        Rect winRect = new Rect(winPos, winPos.plus(particlesParent.getSizeContents()));
        Vec2 mousePos = imgui.getMousePos();

        if (imgui.isInside(winRect, mousePos)) {
            if (imgui.isMouseDragging(0, 0)) {

                Vec2 mouseParticlePos = mousePos.minus(offset).div(m_scale[0]);
                if (m_dragParticle == null) {
                    Vec2 tmp = new Vec2();
                    for (int pIx = 0; pIx < m_componentParticles.length; pIx++) {
                        Particle p = m_componentParticles[pIx];

                        if (p.contains(mouseParticlePos, tmp)) {
                            imgui.stopWindowDrag();
                            m_dragParticle = p;
                            break;
                        }
                    }

                    if (m_dragParticle == null) {
                        for (int pIx = 0; pIx < m_fileParticles.length; pIx++) {
                            FileParticle p = m_fileParticles[pIx];

                            if (p.contains(mouseParticlePos, tmp) && p.containsRect(mouseParticlePos)) {

                                imgui.stopWindowDrag();

                                m_dragParticle = p;
                                break;
                            }
                        }
                    }

                } else {
                    imgui.stopWindowDrag();
                    m_dragParticle.m_pos.plus(imgui.imgui().getIo().getMouseDelta().div(m_scale[0]), m_dragParticle.m_pos);
                }

                if ((dragX || dragY) && m_dragParticle == null){
                    imgui.stopWindowDrag();
                    Vec2 drag = imgui.imgui().getIo().getMouseDelta().negate();
                    if (!dragX) {
                        drag.setX(0);
                    }
                    particlesParent.setScroll(particlesParent.getScroll().plus(drag));
                }
            } else if (!imgui.imgui().isMouseDown(0)) {
                m_dragParticle = null;
            }
        }



        imgui.addCircle(imgui.imgui().getWindowPos(), 10, COL32(175, 255, 175, 255), 16, 1.0f);
        imgui.addCircle(offset, 10, COL32(255, 175, 175, 255), 16, 1.0f);

        imgui.addText(offset, COL32(255, 175, 175, 255), "Particules Be Here");

        renderParticles(m_fileParticles, imgui, offset, m_scale[0]);
        renderParticles(m_componentParticles, imgui, offset, m_scale[0]);


        imgui.imgui().endChild();
        imgui.imgui().endChild();
        imgui.imgui().endColumns();

        imgui.beginTooltip();
        imgui.text("" + particlesParent.getScrollMaxX());
        imgui.endTooltip();
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

    private void computeIntraParticleCollisionForces(FileParticle [] a_particles) {
        Vec2 p12p2 = new Vec2();

        Vec2 tl1 = new Vec2();
        Vec2 br1 = new Vec2();
        Vec2 tl2 = new Vec2();
        Vec2 br2 = new Vec2();

        for (int p1Ix = 0; p1Ix < a_particles.length - 1; p1Ix++) {
            FileParticle p1 = a_particles[p1Ix];
            if (p1.m_isAlive && p1 != m_dragParticle) {

                for (int p2Ix = p1Ix + 1; p2Ix < a_particles.length; p2Ix++) {
                    FileParticle p2 = a_particles[p2Ix];
                    if (p2.m_isAlive && p2 != m_dragParticle) {

                        p2.m_pos.minus(p1.m_pos, p12p2);
                        if (p12p2.length2() < m_collisionRadiusMultiplier[0] * (p1.m_radius + p2.m_radius) * (p1.m_radius + p2.m_radius)) {

                            p1.getTopLeft(tl1, m_collisionRadiusMultiplier[0]);
                            p1.getBottomRight(br1, m_collisionRadiusMultiplier[0]);

                            p2.getTopLeft(tl2);
                            p2.getBottomRight(br2);

                            if (intersectRect(tl1, br1, tl2, br2)) {

                                p1.m_drawRed = true;
                                p2.m_drawRed = true;

                                //Vec2 f = calcSpringForce(0.1f, 0.1f, 200, p1.m_pos, p2.m_pos, p1.m_v, p2.m_v);
                                float length = p12p2.length();
                                p12p2.div(length, p12p2);
                                //p12p2.normalizeAssign();

                                p12p2.times(m_collisionForceMultiplier[0] * ((p1.m_radius + p2.m_radius) / length), p12p2);

                                p2.m_force.plus(p12p2, p2.m_force);
                                p1.m_force.plus(p12p2.negate(), p1.m_force);
                            }
                        }

                        //Vec2 p1ToP2 = p1.m_pos.minus(p2.m_pos);
                        //float length = p1ToP2.length();
                    }
                }
            }
        }
    }

    private boolean intersectRect(Vec2 a_tl1, Vec2 a_br1, Vec2 a_tl2, Vec2 a_br2) {
        return !(   a_tl2.getX() > a_br1.getX()
                ||  a_br2.getX() < a_tl1.getX()
                ||  a_tl2.getY() > a_br1.getY()
                ||  a_br2.getY() < a_tl1.getY());
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

    private void initParticle(Particle a_p, String a_name, int a_color, Vec2 a_size) {
        initParticle(a_p, a_name, a_size);
        a_p.m_color = a_color;
    }

    private void initParticle(Particle a_p, String a_name, Vec2 a_size) {
        a_p.m_pos.setX((float)Math.random() * a_size.getX() - a_size.getX() / 2);
        a_p.m_pos.setY((float)Math.random() * a_size.getX() - a_size.getX() / 2);
        a_p.setName(a_name);
    }

}
