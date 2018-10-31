package se.lnu.siq.s4rdm3x.dmodel.classes;

import java.util.ArrayList;

public abstract class AbstractClassTest {

    private final ArrayList<String> m_strings = new ArrayList<>();

    private static final String MOVE_ONE_GROUP = StaticTest.getString("Please select exactly one group to move.");

    public abstract void anAbstractMethod();

    public void aConcreteMethod(boolean a_doPrint) {
        if (a_doPrint) {
            System.out.println("Hello World!");
        }
    }

}
