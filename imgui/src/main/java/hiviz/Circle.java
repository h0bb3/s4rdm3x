package hiviz;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import se.lnu.siq.s4rdm3x.experiments.metric.ByteCodeInstructions;
import se.lnu.siq.s4rdm3x.experiments.metric.LineCount;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;

public class Circle {
    Vec2 m_pos = null;
    private float m_radius = -1;
    Tree.TNode m_node;
    int [] m_bgColor = null;

    ArrayList<Circle> m_children = new ArrayList<>();

    public Circle(Tree.TNode a_node) {
        m_node = a_node;
    }

    public float getRadius() {
        return m_radius;
    }

    private boolean collision(ArrayList<Circle> a_candidates, Vec2 a_pos, float a_radius) {

        for (int cIx = 0; cIx < a_candidates.size(); cIx++) {
            Circle child = a_candidates.get(cIx);
            if (a_pos.minus(child.m_pos).length2() < (a_radius + child.m_radius) * (a_radius + child.m_radius)) {
                return true;
            }
        }

        return false;
    }

    public Circle draw(ImGuiWrapper a_imgui, Vec2 a_parentPos, float a_scale, final Vec2 a_selectionPos) {
        Circle ret = null;
        final int white = a_imgui.toColor(new Vec4(1., 1., 1., 1));
        final int lightGrey  = a_imgui.toColor(new Vec4(0.75, 0.75, 0.75, 1));
        int color = lightGrey;
        Vec2 pos = a_parentPos.plus(m_pos.times(a_scale));



        for (Circle c : m_children) {
            Circle inside = null;
            inside = c.draw(a_imgui, pos, a_scale, a_selectionPos);
            if (inside != null) {
                ret = inside;
            }
        }


        if (ret == null && a_imgui.isInside(pos, m_radius * a_scale, a_selectionPos)) {
            ret = this;
            color = white;
        }

        final int segments = 8 + (int)(m_radius * a_scale) / 5;

        if (m_bgColor != null) {
            a_imgui.addCircleFilled(pos, m_radius * a_scale, m_bgColor[0], segments);
        }

        a_imgui.addCircle(pos, m_radius * a_scale, color, segments, 2);

        if (m_node.childCount() == 0) {
            final Vec2 textLength = a_imgui.calcTextSize(m_node.getName(), false);
            if (textLength.getX() < m_radius * 2 * a_scale) {
                Vec2 textPos = pos.minus(textLength.times(0.5));
                a_imgui.addText(textPos, color, m_node.getName());
            }
        }


        return ret;
    }

    public void computeLayout(Vec2 a_center, int a_bgColor1, int a_bgColor2) {
        m_pos = a_center;
        computeLayout();

        // get all the leafs and order them according to the metric value
        // color those that are above some threshold (one sd?)
        ArrayList<Circle> leafs = new ArrayList<>();

        getLeafs(leafs);

        leafs.sort((a, b) -> { return (int)a.m_radius - (int)b.m_radius;});
        double[] metrics = new double[leafs.size()];

        for (int ix = 0; ix < metrics.length; ix++) {
            metrics [ix] = leafs.get(ix).getRadius();
        }

        double mean = stats.mean(metrics);
        double sd = stats.stdDev(metrics, mean);

        for (int ix = 0; ix < metrics.length; ix++) {
            if (metrics[ix] > mean) {
                leafs.get(ix).m_bgColor = new int[1];
                leafs.get(ix).m_bgColor[0] = a_bgColor1;

                if (metrics[ix] - mean > sd) {
                    leafs.get(ix).m_bgColor[0] = a_bgColor2;
                }
            }
        }
    }

    private void getLeafs(ArrayList<Circle> a_leafs) {
        if (m_children.size() == 0) {
            a_leafs.add(this);
        } else {

            for (Circle c : m_children) {
                c.getLeafs(a_leafs);
            }
        }
    }

    private void computeLayout() {
        if (m_children.size() == 0) {
            LineCount lc = new LineCount();
            ByteCodeInstructions bc = new ByteCodeInstructions();

            m_radius = 10.0f + (float)lc.compute((CNode)m_node.getObject(), bc) / 100.0f;

        } else {
            // radius can only be computed if children are laid out
            // but for this to work we need to position all children
            // and first their radii must first be computed
            for (Circle c : m_children) {
                c.computeLayout();
            }
            m_children.sort( (a, b) -> (int)b.m_radius - (int)a.m_radius);

            // put the first child in the middle
            if (m_children.size() > 2) {
                // https://stackoverflow.com/questions/30579299/trilateration-algorithm-to-position-3-circles-as-close-as-possible-without-overl
                m_children.get(0).m_pos = new Vec2(0, 0);
                m_children.get(1).m_pos = new Vec2((m_children.get(0).m_radius + m_children.get(1).m_radius), 0);

                float ar = m_children.get(0).m_radius;
                float br = m_children.get(1).m_radius;
                float cr = m_children.get(2).m_radius;

                Vec2 a = m_children.get(0).m_pos;
                Vec2 b = m_children.get(1).m_pos;

                float x = (ar * ar + 2*ar*cr + b.getX()*b.getX() - br*br - 2*br*cr) / (2 * b.getX());
                float y = (float)Math.sqrt((ar + cr) * (ar + cr) - x * x);

                m_children.get(2).m_pos = new Vec2(x, y);

                // calculate position of the smallest possible circle around all 3 circles
                // this is apparently called the appolonius problem or more specifically the external Soddy circle
                // however there are cases when the size difference is too great and the A and B circles are enough to
                // encompass the C circle so we need to check for this first

                Vec2 mid;
                {
                    float radius = (m_children.get(0).m_radius + m_children.get(1).m_radius + 0) ;
                    Vec2 pos = new Vec2(-m_children.get(0).m_radius + radius, 0);

                    if (pos.minus(m_children.get(2).m_pos).length2() < (radius-cr)*(radius-cr)) {
                        mid = pos;
                    } else {
                        mid = solveApollonius(m_children.get(0), m_children.get(1), m_children.get(2), 1, 1, 1);
                    }
                }

                mid.times(-1, m_children.get(0).m_pos);
                m_children.get(1).m_pos.setX(b.getX() - mid.getX());
                m_children.get(1).m_pos.setY(-mid.getY());
                m_children.get(2).m_pos = new Vec2(x - mid.getX(), y - mid.getY());


            } else if (m_children.size() > 1) {

                // this is essentially a line that goes from -ar to br and the midpoint is the offset
                // as the tangent is always on y=0

                float length = 2 * (m_children.get(0).m_radius + m_children.get(1).m_radius + 0) ;
                float xoffset = -m_children.get(0).m_radius + length / 2;


                m_children.get(0).m_pos = new Vec2(-xoffset, 0);
                m_children.get(1).m_pos = new Vec2(m_children.get(0).m_radius + m_children.get(1).m_radius - xoffset, 0);

            } else {
                m_children.get(0).m_pos = new Vec2(0, 0);
            }


            if (m_children.size() == 1) {
                // if we only have one child we put it in the middle

                m_radius = m_children.get(0).m_radius + 5;
            } else {

                ArrayList<Circle> candidates = new ArrayList<>();

                candidates.add(m_children.get(0));
                if (m_children.size() > 2) {
                    candidates.add(m_children.get(2));
                    candidates.add(m_children.get(1));
                } else if (m_children.size() > 1) {
                    candidates.add(m_children.get(1));
                }

                computeChildPositions(candidates);


                float longestRadius = 0;
                for (Circle c : m_children) {
                    if (c.m_pos != null) {
                        float length = c.m_pos.length() + c.m_radius;
                        if (longestRadius < length) {
                            longestRadius = length;
                        }
                    }
                }

                m_radius = longestRadius;
            }
        }
    }

    private Vec2 solveApollonius(Circle c1, Circle c2, Circle c3, int s1, int s2, int s3) {
        // http://rosettacode.org/wiki/Problem_of_Apollonius#Java
        float x1 = c1.m_pos.getX();
        float y1 = c1.m_pos.getY();
        float r1 = c1.m_radius;
        float x2 = c2.m_pos.getX();
        float y2 = c2.m_pos.getY();
        float r2 = c2.m_radius;
        float x3 = c3.m_pos.getX();
        float y3 = c3.m_pos.getY();
        float r3 = c3.m_radius;

        //Currently optimized for fewest multiplications. Should be optimized for
        //readability
        float v11 = 2*x2 - 2*x1;
        float v12 = 2*y2 - 2*y1;
        float v13 = x1*x1 - x2*x2 + y1*y1 - y2*y2 - r1*r1 + r2*r2;
        float v14 = 2*s2*r2 - 2*s1*r1;

        float v21 = 2*x3 - 2*x2;
        float v22 = 2*y3 - 2*y2;
        float v23 = x2*x2 - x3*x3 + y2*y2 - y3*y3 - r2*r2 + r3*r3;
        float v24 = 2*s3*r3 - 2*s2*r2;

        float w12 = v12/v11;
        float w13 = v13/v11;
        float w14 = v14/v11;

        float w22 = v22/v21-w12;
        float w23 = v23/v21-w13;
        float w24 = v24/v21-w14;

        float P = -w23/w22;
        float Q = w24/w22;
        float M = -w12*P-w13;
        float N = w14 - w12*Q;

        float a = N*N + Q*Q - 1;
        float b = 2*M*N - 2*N*x1 + 2*P*Q - 2*Q*y1 + 2*s1*r1;
        float c = x1*x1 + M*M - 2*M*x1 + P*P + y1*y1 - 2*P*y1 - r1*r1;

        // Find a root of a quadratic equation. This requires the circle centers not
        // to be e.g. colinear
        float D = b*b-4*a*c;
        float rs = (float)(-b-Math.sqrt(D))/(2*a);
        float xs = M + N * rs;
        float ys = P + Q * rs;
        return new Vec2(xs,ys);
    }

    private void computeChildPositions(ArrayList<Circle> a_candidates) {
        // basically find all valid positions around all candidates and select the one closest to the center.
        for (Circle c1 : m_children) {
            if (c1.m_pos == null) {
                Vec2 bestPoint = null;
                float bestDist = 0;

                for (int cIx = 0; cIx < a_candidates.size(); cIx++) {
                    Circle c2 = a_candidates.get(cIx);
                    if (c1 != c2) {
                        // use the candidate as a base for positions

                        for (double angle = 0; angle < Math.PI * 2; angle += 0.037) {
                            Vec2 p = new Vec2(c2.m_pos.getX() + Math.cos(angle) * (c1.m_radius + c2.m_radius + 0),
                                    c2.m_pos.getY() + Math.sin(angle) * (c1.m_radius + c2.m_radius + 0));

                            if (!collision(a_candidates, p, c1.m_radius)) {
                                if (bestPoint != null) {
                                    float dist = p.length2();
                                    if (dist < bestDist) {
                                        bestPoint = p;
                                        bestDist = dist;
                                    }
                                } else {
                                    bestPoint = p;
                                    bestDist = p.length2();
                                }
                            }
                        }
                    }
                }
                c1.m_pos = bestPoint;
                a_candidates.add(c1);
            }
        }
    }

    public void addChild(Circle a_child) {
        m_children.add(a_child);
    }

    public Tree.TNode getNode() {
        return m_node;
    }
}