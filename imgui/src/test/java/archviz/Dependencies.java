package archviz;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class Dependencies {

    @Test
    void test1() {
        HRoot cut = new HRoot();

        HNode n1 = cut.add("n1");
        HNode n2 = cut.add("n2");

        cut.addDependency("n1", "n2");
        assertEquals(n1.m_dependencies.get(0), n2);

    }

    @Test
    void test2() {
        HRoot cut = new HRoot();

        HNode n1 = cut.add("n1");
        HNode n2 = cut.add("n2");

        cut.addDependency("n2", "n1");

        assertEquals(n2.m_dependencies.get(0), n1);
    }

    @Test
    void test3() {
        HRoot cut = new HRoot();

        HNode n1 = cut.add("n1");
        HNode client = cut.add("client.n2").m_parent;
        cut.add("client.n3");

        cut.addDependency("n1", "client.n2");
        cut.addDependency("n1", "client.n3");

        assertEquals(n1.m_dependencies.get(0), client);
    }

    @Test
    void root_2_concreteContainer() {
        HRoot cut = new HRoot();

        HNode n1 = cut.add("n1");
        HNode client = cut.add("client");
        cut.add("client.n2");
        cut.add("client.n3");

        cut.addDependency("n1", "client");

        assertEquals(n1.m_dependencies.get(0), client.getConcreteRepresentation());
    }

    @Test
    void concreteContainer_child_2_concreteContainer() {
        HRoot cut = new HRoot();

        HNode n1 = cut.add("client.n1");
        HNode client = cut.add("client");
        cut.add("client.n2");
        cut.add("client.n3");

        cut.addDependency("client.n1", "client");

        assertEquals(n1.m_dependencies.get(0), client.getConcreteRepresentation());
    }

    @Test
    void concreteContainer_container_child_2_concreteContainer() {
        HRoot cut = new HRoot();

        HNode client = cut.add("client");
        HNode n1 = cut.add("client.container.n1");

        cut.addDependency("client.container.n1", "client");

        assertEquals(n1.m_dependencies.get(0), client.getConcreteRepresentation());
    }

    @Test
    void concreteContainer_2_concreteContainer_container_child() {
        HRoot cut = new HRoot();

        HNode client = cut.add("client");
        HNode n1 = cut.add("client.container.n1");

        cut.addDependency("client", "client.container.n1");

        assertEquals(client.getConcreteRepresentation().m_dependencies.get(0), n1);
    }

    @Test
    void root_2_all_concreteContainer_children() {
        HRoot cut = new HRoot();

        HNode n1 = cut.add("n1");
        HNode client = cut.add("client");
        cut.add("client.n2");
        cut.add("client.n3");


        cut.addDependency("n1", "client");
        cut.addDependency("n1", "client.n2");
        cut.addDependency("n1", "client.n3");

        assertEquals(n1.m_dependencies.get(0), client);
    }
}
