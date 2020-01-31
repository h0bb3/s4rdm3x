package gui;

import kotlin.reflect.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class JavaProperty<T> implements KMutableProperty0<T> {

    private T[] m_object;

    public JavaProperty(T[] a_object) {
        m_object = a_object;
    }

    @Override
    public void set(T t) {
        m_object[0] = t;
    }

    @NotNull
    @Override
    public Setter<T> getSetter() {
        return null;
    }

    @Override
    public T get() {
        return m_object[0];
    }

    @Nullable
    @Override
    public Object getDelegate() {
        return null;
    }

    @NotNull
    @Override
    public KProperty0.Getter<T> getGetter() {
        return null;
    }

    @Override
    public T invoke() {
        return m_object[0];
        //return null;
    }

    @Override
    public boolean isLateinit() {
        return false;
    }

    @Override
    public boolean isConst() {
        return false;
    }

    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @NotNull
    @Override
    public List<KParameter> getParameters() {
        return null;
    }

    @NotNull
    @Override
    public KType getReturnType() {
        return null;
    }

    @NotNull
    @Override
    public List<KTypeParameter> getTypeParameters() {
        return null;
    }

    @Override
    public T call(Object... objects) {
        return null;
    }

    @Override
    public T callBy(Map<KParameter, ?> map) {
        return null;
    }

    @Nullable
    @Override
    public KVisibility getVisibility() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @NotNull
    @Override
    public List<Annotation> getAnnotations() {
        return null;
    }

    @Override
    public boolean isSuspend() {
        return false;
    }

    public static class FloatProperty extends JavaProperty<Float> {
        private final float m_min;
        private final float m_max;

        public FloatProperty(float a_min, float a_max, Float[] m_object) {
            super(m_object);
            m_min = a_min;
            m_max = a_max;
        }

        @Override
        public void set(Float a_value) {
            if (a_value < m_min) {
                a_value = m_min;
            } else if (a_value > m_max) {
                a_value = m_max;
            }
            super.set(a_value);
        }
    }
}
