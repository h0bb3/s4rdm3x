package se.lnu.siq.s4rdm3x.dmodel;

import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.util.Textifier;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.objectweb.asm.Opcodes.GOTO;


/**
 * Created by tohto on 2017-04-26.
 *
 * Info about the JVM
 * http://web.cs.ucla.edu/~msb/cs239-tutorial/
 * https://www.beyondjava.net/blog/java-programmers-guide-java-byte-code/
 *
 */
public class ASMdmProjectBuilder extends ClassVisitor {
    private dmProject m_project;
    private dmClass m_currentClass;
    private int m_currentLine;

    private int m_methodArgCount;

    private int m_tabs;
    private boolean m_doPrint;

    private static final int g_opcodes = Opcodes.ASM5;


    private void println(String a_str) {
        if (m_doPrint) {
            for (int i = 0; i < m_tabs; i++) {
                System.out.print("\t");
            }
            System.out.println(a_str);
        }
    }



    public ASMdmProjectBuilder() {
        super(g_opcodes);

        m_project = new dmProject();
        m_currentClass = null;
        m_tabs = 0;
        m_doPrint = true;
    }

    public ASMdmProjectBuilder(dmProject a_project) {
        super(g_opcodes);

        m_project = a_project;
        m_currentClass = null;
        m_tabs = 0;
        m_doPrint = true;
    }

    public void dontPrint() {
        m_doPrint = false;
    }

    public dmProject getProject() {
        return m_project;
    }

    private void addDependency(String a_targetName, dmDependency.Type a_type, dmClass.Method a_method) {

        // remove array markers: []
        a_targetName = stripArrayBrackets(a_targetName);

        a_targetName = a_targetName.replace('/', '.');
        dmClass destClass = m_project.addClass(a_targetName);
        if (destClass != null) {
            if (a_method == null) {
                m_currentClass.addDependency(destClass, a_type, m_currentLine);
            } else {
                m_currentClass.addDependency(destClass, a_type, m_currentLine, a_method);
            }

        }
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        name = name.replace('/', '.');
        m_tabs = 0;
        println("Class: " + name);
        m_tabs = 1;
        m_currentLine = 1;

        m_currentClass = m_project.addClass(name);
        if (m_currentClass != null) {
            if (superName != null) {
                println("Extends: " + superName);
                addDependency(superName, dmDependency.Type.Extends, null);
            }
            if (interfaces != null) {
                for (String interfaceName : interfaces) {
                    println("Implements: " + interfaceName);
                    addDependency(interfaceName, dmDependency.Type.Implements, null);
                }
            }
        }
        // todo add dependencies for extends and implements
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (m_currentClass != null) {

            if ((access & Opcodes.ACC_SYNTHETIC) > 0) {
                println("skipping synthetic field");
                return  null;
            }

            m_currentClass.incFieldCount();
            m_currentClass.addText(name);
            if (value != null) {
                m_currentClass.addText(value.toString());
            }

            if ((access & Opcodes.ACC_STATIC) > 0 && (access & Opcodes.ACC_FINAL) > 0) {
                println("Constant Field");
                m_project.addConstant(value, m_currentClass);
                //return  null;
            }

            println("Field: " + desc + " " + name);
            if (signature != null) {
                new SignatureReader(signature).accept(new SignatureVisitor(g_opcodes) {
                    public void visitClassType(String name) {
                        addDependency(name, dmDependency.Type.Field, null);
                        println("visitClassType: " + name);
                    }
                });
            } else {

                println("Type.getType: " + Type.getType(desc).getClassName());
                addDependency(Type.getType(desc).getClassName(), dmDependency.Type.Field, null);
            }

        }

        super.visitField(access, name, desc, signature, value);
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (m_currentClass != null) {
            m_tabs = 1;
            m_methodArgCount = 1;   // this is the first argument
            println("Method: " + access + " " + name + " " + desc);
            m_tabs = 2;

            dmClass.Method m = m_currentClass.addMethod(name, (access & Opcodes.ACC_ABSTRACT) != 0, (access & Opcodes.ACC_SYNTHETIC) != 0);

            // Skip synthetic methods that are not lambda expressions...
            if (false && (access & Opcodes.ACC_SYNTHETIC) > 0 && name.startsWith("access$")) {
                println("Skippnig Synthetic method");
                return null;
            } else {

                if ((access & Opcodes.ACC_STATIC) > 0) {
                    m_methodArgCount = 0;  // we do not have this in static methods
                    /*if (name.compareTo("<clinit>") == 0) {
                        println("Static Initializer");
                        return new MethodVisitor(g_opcodes) {
                            public void visitLdcInsn(Object cst) {
                                // current class has a constant probably static final
                                // this object should be saved so we can trace constants
                                //m_project.addConstant(cst, m_currentClass);
                                println("static initialzer visitLdcInsn: " + cst.getClass().getName() + ", toString: " + cst.toString());
                            }
                        };
                    }*/
                }



                {
                    Textifier t = new Textifier();
                    StringWriter sw = new StringWriter();

                    t.visitMethod(access, name, desc, signature, exceptions);

                    t.print(new PrintWriter(sw));

                    println("\ttextifier: " + sw.toString());
                }

                if (exceptions != null) {
                    for (String e : exceptions) {
                        println("\tThrows: " + e);
                        addDependency(e, dmDependency.Type.Throws, m);
                    }
                }

                if (signature == null) {
                    println("\tType.getReturnType: " + Type.getReturnType(desc).getClassName());
                    addDependency(Type.getReturnType(desc).getClassName(), dmDependency.Type.Returns, m);
                    //println("\tType.getReturnType: " + Type.getReturnType(desc).getClassName());
                    //addDependency(Type.getReturnType(desc).getClassName(), dmDependency.Type.Returns);


                    Type args[] = Type.getArgumentTypes(desc);
                    if (args != null) {
                        for (Type t : args) {

                            if (m_currentClass.isInner() && m_methodArgCount == 1 && name.compareTo("<init>") == 0) {
                                // first argument in inner class is a reference to the containing class generated by the compiler
                                println("\tSkipping constructor argument: " + t.getClassName());
                            } else {

                                println("\tType.getArgumentTypes[x]: " + t.getClassName());
                                addDependency(t.getClassName(), dmDependency.Type.Argument, m);
                            }
                            m_methodArgCount++;
                        }
                    }

                } else {
                    println("Method Signature: " + signature);
                    final boolean[] returnAdded = {false};
                    new SignatureReader(signature).accept(new SignatureVisitor(g_opcodes) {

                        public SignatureVisitor visitReturnType() {

                            return new SignatureVisitor(g_opcodes) {


                                public void visitClassType(String name) {
                                    addDependency(name, dmDependency.Type.Returns, m);
                                    println("\tvisitReturnType - visitClassType: " + name);
                                    returnAdded[0] = true;

                                }
                            };
                        }

                        public SignatureVisitor visitParameterType() {
                            return new SignatureVisitor(g_opcodes) {

                                public void visitClassType(String name) {
                                    addDependency(name, dmDependency.Type.Argument, m);
                                    println("\tvisitArgumentType - visitClassType: " + name);
                                    m_methodArgCount++;
                                }
                            };
                        }
                    });

                    if (!returnAdded[0]) {
                        println("\tType.getReturnType: " + Type.getReturnType(desc).getClassName());
                        addDependency(Type.getReturnType(desc).getClassName(), dmDependency.Type.Returns, m);
                    }
                }
            }

            return new MethodVisitor(g_opcodes) {

                private void addLocalVar(String a_className, int a_index) {
                    if (a_index < m_methodArgCount) {
                        //addDependency(a_className, dmDependency.Type.ArgumentUse);
                        println("addArgumentUse (skipped)" + a_className + ", index: " + a_index + ", arg count: " + m_methodArgCount);
                    } else {
                        addDependency(a_className, dmDependency.Type.LocalVar, m);
                        println("addLocalVar: " + a_className);
                    }
                }

                @Override
                public void visitIincInsn(int var, int increment) {
                    m.incInstructionCount();
                }

                @Override
                public void visitLdcInsn(Object cst) {
                    m.incInstructionCount();
                    println("visitLdcInsn: " + cst.getClass().getName() + ", toString: " + cst.toString());
                    {
                        Textifier t = new Textifier();
                        StringWriter sw = new StringWriter();
                        t.visitLdcInsn(cst);
                        t.print(new PrintWriter(sw));
                        println("\ttextifier: " + sw.toString());
                    }
                    m_project.addConstantDependency(cst, m_currentClass, m_currentLine);
                    m_currentClass.addText(cst.toString());
                }

                @Override
                public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                    m_tabs = 3;

                    // apparently arguments gets a visit as a local variable too.
                    // these are filtered, the use of local variables (and arguments) is thus more complicated
                    // possibly we need to maintain an entire table

                    if (name.compareTo("this") != 0 && !name.startsWith("this$")) {   // this$X represents containing classes
                        println("Local Variable: " + desc + " " + name + " " + index);
                        m_tabs = 4;
                        if (signature != null) {
                            //Sys.out.println("Type.get: " + Type.(signature)[0].getClassName());
                            new SignatureReader(signature).accept(new SignatureVisitor(g_opcodes) {
                                public void visitClassType(String name) {
                                    addLocalVar(name, index);
                                }
                            });
                            m_currentClass.addText(name);
                        } else {

                            if (name.charAt(name.length() - 1) == '$') {
                                println("Skipping internal arr$ local variable");
                            } else {
                                println("Type.getType: " + Type.getType(desc).getClassName());
                                addLocalVar(Type.getType(desc).getClassName(), index);
                                m_currentClass.addText(name);
                            }
                        }
                    } else {
                        println("Local Variable (skipped): " + desc + " " + name + " " + index);
                    }
                    super.visitLocalVariable(name, desc, signature, start, end, index);
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    m.incInstructionCount();



                    owner = owner.replace('/', '.');
                    println("Field Instruction: " + owner + " " + name + " " + desc);
                    {
                        Textifier t = new Textifier();
                        StringWriter sw = new StringWriter();
                        t.visitFieldInsn(opcode, owner, name, desc);
                        t.print(new PrintWriter(sw));
                        println("\ttextifier: " + sw.toString());
                    }
                    if (name.startsWith("this$")) { // this$X represents containing class
                        //m.useField(name);
                        println("skipped field");
                        return;
                    }
                    //String ownerName = Type.getType(owner).getClassName();
                    if (owner.compareTo(m_currentClass.getName()) == 0) {
                        // in this case we have a field that corresponds to the type
                        m.useField(name);
                        addDependency(Type.getType(desc).getClassName(), dmDependency.Type.OwnFieldUse, m);
                    } else {
                        // in this case we have a field in some other type (owner) that is used
                        addDependency(owner, dmDependency.Type.FieldUse, m);
                    }
                }

                @Override
                public void visitLineNumber(int line, Label start) {
                    if (m_currentClass != null) {
                        m_currentClass.incLineCount();
                    }
                    m_currentLine = line;
                }

                @Override
                public void visitVarInsn(int opcode, int var) {
                    m.incInstructionCount();
                    println("visitVarInsn: " + var);
                    Textifier t = new Textifier();
                    StringWriter sw = new StringWriter();

                    t.visitVarInsn(opcode, var);

                    t.print(new PrintWriter(sw));

                    println("\ttextifier: " + sw.toString());

                    /*for(Object s : t.getText()) {
                        println("textifier" + t.toString());
                    }*/

                }

                @Override
                public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
                    //m.incInstructionCount(); // Possibly not counted by JArchitect
                    println("visitTableSwitchInsn: " + min + ":" + max);
                    for (int i = 0; i < max - min + 1; i++) {
                        m.incBranchStatementCount();
                    }
                }

                @Override
                public void visitIntInsn(int var1, int var2) {
                    m.incInstructionCount();
                    println("visitIntInsn: " + var1 + " " + var2);
                    Textifier t = new Textifier();
                    StringWriter sw = new StringWriter();

                    t.visitIntInsn(var1, var2);

                    t.print(new PrintWriter(sw));

                    println("\ttextifier: " + sw.toString());
                    m_currentClass.addText("" + var2);
                }

                @Override
                public void visitInsn(int opcode) {
                    m.incInstructionCount();
                }

                @Override
                public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
                    // TODO: should this one be counted in branching in some way
                    //m.incInstructionCount(); Possibly not counted by JArchitect
                }

                @Override
                // some info about the arrays in the jvm and asm: http://www.egtry.com/java/bytecode/asm/array
                public void visitMultiANewArrayInsn(String var1, int var2) {
                    m.incInstructionCount();
                    println("visitMultiANewArrayInsn: ");
                    {
                        Textifier t = new Textifier();
                        StringWriter sw = new StringWriter();

                        t.visitMultiANewArrayInsn(var1, var2);

                        t.print(new PrintWriter(sw));

                        println("\ttextifier: " + sw.toString());
                    }

                    String owner = var1.replace('/', '.');
                    Type t = Type.getObjectType(owner);
                    if (t != null && t.getClassName() != null) {
                        owner = t.getClassName();
                        println("Type.getMethodType: " + owner);
                    }
                    owner = stripArrayBrackets(owner);
                    addDependency(owner, dmDependency.Type.ConstructorCall, m);

                }

                @Override
                public void visitTypeInsn(int var1, String var2) {

                    println("visitTypeInsn: " + var2);
                    {
                        Textifier t = new Textifier();
                        StringWriter sw = new StringWriter();

                        t.visitTypeInsn(var1, var2);

                        t.print(new PrintWriter(sw));

                        println("\ttextifier: " + sw.toString());
                    }

                    // array creation on type
                    // add constructor dependency
                    if (var1 == Opcodes.ANEWARRAY) {
                        String owner = var2.replace('/', '.');
                        Type t = Type.getObjectType(owner);
                        if (t != null && t.getClassName() != null) {
                            owner = t.getClassName();
                            println("Type.getMethodType: " + owner);
                        }
                        owner = stripArrayBrackets(owner);
                        addDependency(owner, dmDependency.Type.ConstructorCall, m);
                    } else {
                        m.incInstructionCount();
                    }

                }

                @Override
                public void visitJumpInsn(int a_opCode, Label a_label) {
                    m.incInstructionCount();
                    if (a_opCode != GOTO) {
                        m.incBranchStatementCount();
                    }
                    println("visitJumpInstruction");

                    Textifier t = new Textifier();
                    StringWriter sw = new StringWriter();

                    t.visitJumpInsn(a_opCode, a_label);

                    t.print(new PrintWriter(sw));

                    println("\ttextifier: " + sw.toString());

                }

                @Override
                public void visitParameter(String name, int access) {
                    println("visitParameter: " + name);
                }

                @Override
                public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                    println("visitTryCatchBlock: " + type);
                    m.incBranchStatementCount();
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    m_tabs = 5;
                    m.incInstructionCount();

                    owner = owner.replace('/', '.');
                    Type t = Type.getObjectType(owner);
                    if (t != null && t.getClassName() != null) {
                        owner = t.getClassName();
                        println("Type.getMethodType: " + owner);
                    }

                    // if owner is an array we depend on the mother type instead
                    owner = stripArrayBrackets(owner);

                    // skipp all calls on objects of same class...
                    if (m_currentClass.getName().compareTo(owner) != 0) {

                        println("Method call on: " + owner + "." + name + "(" + desc + ")");
                        if (name.startsWith("access$")) {
                            addDependency(owner, dmDependency.Type.FieldUse, m);
                        } else {
                            if (name.contentEquals("<init>")) {
                                addDependency(owner, dmDependency.Type.ConstructorCall, m);
                            } else {
                                addDependency(owner, dmDependency.Type.MethodCall, m);
                            }
                        }
                    } else if (name.contentEquals("<init>")) {
                        // we track dependencies to self if they are constructor calls
                        addDependency(owner, dmDependency.Type.ConstructorCall, m);
                    }
                }

                @Override
                public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                    m.incInstructionCount();
                }

            };
        }
        return null;
    }

    private String stripArrayBrackets(String a_str) {
        if (a_str.endsWith("[]")) {
            //owner = owner.substring(0, owner.length() - 2);
            a_str = a_str.substring(0, a_str.indexOf('['));
        }

        return a_str;
    }
}
