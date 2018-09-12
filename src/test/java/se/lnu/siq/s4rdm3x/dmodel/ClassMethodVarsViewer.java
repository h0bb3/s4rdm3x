package se.lnu.siq.s4rdm3x.dmodel;

import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import se.lnu.siq.s4rdm3x.dmodel.classes.Test2;

import java.io.InputStream;



/**
 * Created by tohto on 2017-04-24.
 */
public class ClassMethodVarsViewer {

    public static class MethodVarsPrinterVisitor extends ClassVisitor {

        public MethodVarsPrinterVisitor(int api) {
            super(api);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

            System.out.println("\n" + name + " " + desc);
            if (signature == null) {
                System.out.println("\tType.getReturnType: " + Type.getReturnType(desc).getClassName());
                Type args[] = Type.getArgumentTypes(desc);
                if (args != null) {
                    for (Type t : args) {
                        System.out.println("\tType.getArgumentTypes[x]: " + t.getClassName());
                    }
                }
            } else {
                new SignatureReader(signature).accept(new SignatureVisitor(Opcodes.ASM4) {
                    public void visitClassType(String name) {
                        System.out.println("\tvisitClassType: " + name);
                    }
                });
            }

            return null;
        }
    };

    public static void main(String[] args) throws Exception{
        //InputStream in = Test2.class.getResourceAsStream("/se/lnu/siq/asm_test1/tests/classes/Test2.class");
        InputStream in = Test2.class.getResourceAsStream("/tests/classes/SelfCall.class");
        ClassReader classReader = new ClassReader(in);
        classReader.accept(new ClassMethodVarsViewer.MethodVarsPrinterVisitor(Opcodes.ASM4), 0);

    }

}
