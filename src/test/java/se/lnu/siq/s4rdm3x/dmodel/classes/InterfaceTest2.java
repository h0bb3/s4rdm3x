package se.lnu.siq.s4rdm3x.dmodel.classes;

public interface InterfaceTest2 {

    void fileUpdated();

    default void fileRemoved() {
        System.out.println("Interfaces with default methods?! WTF!");
    };
}
