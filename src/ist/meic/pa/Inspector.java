package ist.meic.pa;

import ist.meic.pa.utils.PrimitiveWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inspector {

    private Object inspectTarget;
    private Class<?> inspectClass;
    private boolean fullInspector;

    public Inspector() {
    }

    public void inspect(Object target) {
        setObject(target, target.getClass().isPrimitive() ? false : true, target.getClass());
        readCommands();
    }

    private void setObject(Object target, boolean fI, Class<?> cI) {
        inspectTarget = target;
        inspectClass = cI;
        fullInspector = fI;
        printObjectProperties();
    }

    private void readCommands() {
        while (true) {
            System.err.print("> ");
            String[] command = System.console().readLine().split(" ");

            if (command[0].equals("i") && command.length == 2 && fullInspector) {
                inspectValue(command[1], inspectTarget.getClass());
            } else if (command[0].equals("m") && command.length == 3 && fullInspector) {
                modifyValue(command[1], command[2], inspectTarget.getClass());
            } else if (command[0].equals("c") && command.length >= 2 && fullInspector) {
                callMethod(command[1], command);
            } else if (command[0].equals("q")) {
                return;
            } else {
                System.err.println("Invalid command.");
            }
        }
    }

    private List<Method> getMethods(String method, Integer numCommands) {
        ArrayList<Method> methods = new ArrayList<Method>();
        Class<?> inspectClass = inspectTarget.getClass();
        while (inspectClass != null) {
            Method[] mets = inspectClass.getDeclaredMethods();

            for (Method m : mets) {
                if (m.getParameterTypes().length == numCommands - 2 && m.getName().equals(method)) {
                    methods.add(m);
                }
            }
            inspectClass = inspectClass.getSuperclass();
        }
        return methods;
    }

    private void callMethod(String method, String[] command) {
        try {
            List<Method> methods = getMethods(method, command.length);
            if (methods.size() == 1) {
                callMethod(command, methods.get(0));
            } else if (methods.size() > 1) {
                for (Method m : methods) {
                    try {
                        callMethod(command, m);
                        return;
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                }
            } else {
                System.err.println("The method " + method + " does not exist in the inspected class.");
            }
        } catch (IllegalArgumentException e) {
            System.err.println("The arguments passed do not match the declared types.");
        } catch (Exception e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        }
    }

    private void callMethod(String[] command, Method m) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Object ret;
        Object[] args = castArguments(command, m.getParameterTypes());
        m.setAccessible(true);
        ret = m.invoke(inspectTarget, args);

        if (ret != null) {
            System.err.println(ret.toString());
            setObject(ret, !m.getReturnType().isPrimitive(), m.getReturnType());
        }
    }

    private Object[] castArguments(String[] command, Class<?>[] parameterTypes) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Object[] ret = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            ret[i] = castValue(command[i + 2], parameterTypes[i]);
        }

        return ret;
    }

    private Object castValue(String value, Class<?> parameter) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        } else if (value.startsWith("'") && value.endsWith("'")) {
            return value.charAt(1);
        } else if (parameter.equals(Integer.class) || parameter.equals(Integer.TYPE)) {
            try {
                return parameter.getConstructor(new Class[] { String.class }).newInstance(value);
            } catch (NoSuchMethodException e) {
                return PrimitiveWrapper.getWrapper(parameter).getConstructor(new Class[] { String.class }).newInstance(value);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void modifyValue(String parameter, String value, Class<?> inspectClass) {
        try {
            Field classField = getField(parameter, inspectClass);
            classField.set(inspectTarget, castValue(value, classField.getType()));
            printField(classField);
        } catch (NoSuchFieldException e) {
            if (!(inspectClass.getSuperclass().equals(Object.class))) {
                modifyValue(parameter, value, inspectClass.getSuperclass());
            } else {
                System.err.println("The class does not have the field " + parameter);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("The arguments passed do not match the declared type.");
        } catch (Exception e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        }
    }

    private void inspectValue(String parameter, Class<?> inspectClass) {
        try {
            Field classField = getField(parameter, inspectClass);
            printField(classField);
            setObject(classField.get(inspectTarget), !classField.getClass().isPrimitive(), classField.getClass());
        } catch (NoSuchFieldException e) {
            if (!(inspectClass.getSuperclass().equals(Object.class))) {
                inspectValue(parameter, inspectClass.getSuperclass());
            } else {
                System.err.println("The class does not have the field " + parameter);
            }
        } catch (Exception e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
        }
    }

    private Field getField(String parameter, Class<?> inspectClass) throws NoSuchFieldException {
        for (Field f : inspectClass.getDeclaredFields()) {
            if (f.getName().equals(parameter)) {
                return f;
            }
        }
        throw new NoSuchFieldException();
    }

    private void printObjectProperties() {
        try {
            printClass();
            if (fullInspector) {
                printSuperClasses();
                printInterfaces();
                printFields();
                printObjectMethods();
            }
            System.err.println("----------------------");
        } catch (Exception e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        }
    }

    private void printClass() {
        System.err.println("-----   CLASS    -----");
        System.err.print(inspectTarget.toString() + " is an instance of class ");
        System.err.println(inspectClass.getName());
    }

    private void printSuperClasses() {
        Map<Class<?>, Class<?>> superClasses = new HashMap<Class<?>, Class<?>>();
        Class<?> inspectClass = inspectTarget.getClass();

        while (!inspectClass.equals(Object.class)) {
            superClasses.put(inspectClass.getSuperclass(), inspectClass.getSuperclass());
            inspectClass = inspectClass.getSuperclass();
        }

        if (superClasses.size() > 0) {
            System.err.println("----- SUPER CLASS ----");
            for (Class<?> c : superClasses.values()) {
                System.err.println(c.getName());
            }
        }
    }

    private void printInterfaces() {
        HashMap<Class<?>, Class<?>> interfaces = new HashMap<Class<?>, Class<?>>();
        Class<?> inspectClass = inspectTarget.getClass();

        while (!inspectClass.equals(Object.class)) {
            for (Class<?> i : inspectClass.getInterfaces()) {
                interfaces.put(i, i);
            }
            inspectClass = inspectClass.getSuperclass();
        }

        if (interfaces.size() > 0) {
            System.err.println("----- INTERFACES -----");
            for (Class<?> i : interfaces.values()) {
                System.err.println(i.getName());
            }
        }
    }

    private void printFields() throws IllegalAccessException {
        System.err.println("----- PARAMETERS -----");

        Class<?> inspectClass = inspectTarget.getClass();
        HashMap<String, Field> fields = new HashMap<String, Field>();
        while (!inspectClass.equals(Object.class)) {
            for (Field f : inspectClass.getDeclaredFields()) {
                if (!fields.containsKey(f.getName())) {
                    fields.put(f.getName(), f);
                }
            }
            inspectClass = inspectClass.getSuperclass();
        }

        for (Field f : fields.values()) {
            printField(f);
        }
    }

    private void printField(Field f) throws IllegalAccessException {
        f.setAccessible(true);
        System.err.print(Modifier.toString(f.getModifiers()) + " ");
        System.err.print(f.getType().toString() + " ");
        System.err.print(f.getName() + " = ");
        System.err.println(f.get(inspectTarget).toString());
    }

    private void printObjectMethods() {
        System.err.println("-----  METHODS   -----");

        Class<?> inspectClass = inspectTarget.getClass();
        while (!inspectClass.equals(Object.class)) {
            for (Method m : inspectClass.getDeclaredMethods()) {
                System.err.println(m.toString());
            }
            inspectClass = inspectClass.getSuperclass();
        }
    }
}