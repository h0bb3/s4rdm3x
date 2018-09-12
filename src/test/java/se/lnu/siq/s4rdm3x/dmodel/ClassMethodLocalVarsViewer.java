package se.lnu.siq.s4rdm3x.dmodel;


import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;


import java.io.InputStream;


public class ClassMethodLocalVarsViewer {

    public static class MethodPrinterVisitor extends ClassVisitor{

        public MethodPrinterVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }


        public MethodPrinterVisitor(int api) {
            super(api);
        }


        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

            System.out.println("\nvisitMethod:" + name + ", " + desc + ", " + signature);

            MethodVisitor oriMv= new MethodVisitor(Opcodes.ASM4) {
                public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {

                    System.out.println("Local Variable: " + desc + " " + name);
                    if (signature != null) {
                        //System.out.println("Type.get: " + Type.(signature)[0].getClassName());
                        new SignatureReader(signature).accept(new SignatureVisitor(Opcodes.ASM4) {
                            public void visitClassType(String name) {
                                System.out.println("\tvisitClassType: " + name);
                            }
                        });
                    } else {

                        System.out.println("\tType.getType: " + Type.getType(desc).getClassName());
                    }
                    super.visitLocalVariable(name, desc, signature, start, end, index);
                }

                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    System.out.println("Method call on: " + owner + ". " + name + ", " + desc + ", " + itf);
                }

                //public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                    //super.visitFrame(type, nLocal, local, nStack, stack);
                //}

                //public void visitCode() {
                    //super.visitCode();
                //}
            };


            return oriMv;
            //return new TraceMethodVisitor(new Textifier());

        }


    }


    public static void main(String[] args) throws Exception{
        //InputStream in = InputStream.class.getResourceAsStream("/se/lnu/siq/asm_test1/classes/Test1.class");
        InputStream in = InputStream.class.getResourceAsStream("/se/lnu/siq/asm_test1/classes/SelfCall$SelfCall1.class");
        ClassReader classReader = new ClassReader(in);
        classReader.accept(new MethodPrinterVisitor(Opcodes.ASM4), 0);

    }

}
