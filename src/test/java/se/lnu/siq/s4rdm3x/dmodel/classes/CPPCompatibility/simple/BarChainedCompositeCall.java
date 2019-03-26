package se.lnu.siq.s4rdm3x.dmodel.classes.CPPCompatibility.simple;

public class BarChainedCompositeCall {
    private Foo m_foo = new Foo();
    public int doSomeStuff() {
        return calculateSomeStuff(m_foo.getFuu().getMagicNumber());
    }

    protected int calculateSomeStuff(int a_number) {
        return a_number += 17;
    }

}
