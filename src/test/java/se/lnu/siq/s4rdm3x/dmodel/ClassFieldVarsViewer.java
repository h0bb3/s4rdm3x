package se.lnu.siq.dmodel;

import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import se.lnu.siq.s4rdm3x.dmodel.classes.Test3;

import java.io.InputStream;

/**
 * Created by tohto on 2017-04-24.
 */
public class ClassFieldVarsViewer {

    public static class FieldPrinterVisitor extends ClassVisitor {

        public FieldPrinterVisitor(int api) {
            super(api);
        }


        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            System.out.println("Field: " + desc + " " + name);
            if (signature != null) {
                new SignatureReader(signature).accept(new SignatureVisitor(Opcodes.ASM4) {
                    public void visitClassType(String name) {
                        System.out.println("\tvisitClassType: " + name);
                    }
                });
            } else {

                System.out.println("\tType.getType: " + Type.getType(desc).getClassName());
            }
            super.visitField(access, name, desc, signature, value);

            return null;
        }


    }



    public static void main(String[] args) throws Exception{
        InputStream in = Test3.class.getResourceAsStream("/tests/classes/Test3.class");
        ClassReader classReader = new ClassReader(in);
        classReader.accept(new ClassFieldVarsViewer.FieldPrinterVisitor(Opcodes.ASM4), 0);

    }
}
