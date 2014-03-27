package ist.meic.pa;

import ist.meic.pa.utils.PrimitiveWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class Inspector {

    private Object inspectTarget;

    public Inspector() {
    }

    public void inspect(Object target) {
        inspectTarget = target;
        printObjectProperties();
        readCommands();
    }

    private void readCommands() {
        while (true) {
            System.err.print("> ");
            String[] command = System.console().readLine().split(" ");

            if (command[0].equals("i") && command.length == 2) {
                inspectValue(command[1], inspectTarget.getClass());
            } else if (command[0].equals("m") && command.length == 3) {
                modifyValue(command[1], command[2], inspectTarget.getClass());
            } else if (command[0].equals("c") && command.length >= 2) {
                callMethod(command[1], command, inspectTarget.getClass());
            } else if (command[0].equals("q")) {
                return;
            } else {
                System.err.println("Invalid command.");
            }
        }
    }

    private HashMap<Integer, Method> getMethods(String method, String[] command, Class<?> inspectClass, Integer index) {
        if (inspectClass == null) {
            return new HashMap<Integer, Method>();
        } else {
            Method[] mets = inspectClass.getDeclaredMethods();
            HashMap<Integer, Method> methods = new HashMap<Integer, Method>();

            for (Method m : mets) {
                if (m.getParameterTypes().length == command.length - 2 && m.getName().equals(method)) {
                    methods.put(index++, m);
                }
            }
            methods.putAll(getMethods(method, command, inspectClass.getSuperclass(), index));

            return methods;
        }
    }

    private void callMethod(String method, String[] command, Class<?> inspectClass) {
        try {
            Object ret = null;
            HashMap<Integer, Method> methods = getMethods(method, command, inspectClass, 0);

            if (methods.size() == 1) {
                Object[] args = castArguments(command, methods.get(0).getParameterTypes());
                methods.get(0).setAccessible(true);
                ret = methods.get(0).invoke(inspectTarget, args);

                if (ret != null) {
                    System.err.println(ret.toString());
                    if (!methods.get(0).getReturnType().isPrimitive()) {
                        inspectTarget = ret;
                        printObjectProperties();
                    }
                }
            } else if (methods.size() > 1) {
                for (Method m : methods.values()) {
                    try {
                        Object[] args = castArguments(command, m.getParameterTypes());
                        m.setAccessible(true);
                        ret = m.invoke(inspectTarget, args);

                        if (ret != null) {
                            System.err.println(ret.toString());
                            if (!m.getReturnType().isPrimitive()) {
                                inspectTarget = ret;
                                printObjectProperties();
                            }
                        }

                        return;
                    } catch (IllegalArgumentException e) {
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
            HashMap<Integer, Field> classFields = getFields(parameter, inspectClass, 0);
            if (classFields.size() == 1) {
                classFields.get(0).set(inspectTarget, castValue(value, classFields.get(0).getType()));
                printField(classFields.get(0).getDeclaringClass(), classFields.get(0));
            } else if (classFields.size() > 1) {
                System.err.println("Choose a field:");
                Boolean incorrectValue = true;
                Integer fieldValue = 0;
                while (incorrectValue) {
                    Integer index = 0;
                    for (Field f : classFields.values()) {
                        System.err.print(index++ + ": ");
                        printField(f.getDeclaringClass(), f);
                    }
                    System.err.print("> ");
                    fieldValue = Integer.parseInt(System.console().readLine());
                    if (fieldValue >= classFields.size()) {
                        System.err.println("Please select a correct value!");
                    } else {
                        incorrectValue = false;
                    }
                }
                classFields.get(fieldValue).set(inspectTarget, castValue(value, classFields.get(fieldValue).getType()));

            } else {
                throw new NoSuchFieldException();
            }
        } catch (NoSuchFieldException e) {
            if (!(inspectClass.getSuperclass().getName().equals("java.lang.Object"))) {
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
            HashMap<Integer, Field> classFields = getFields(parameter, inspectClass, 0);
            if (classFields.size() == 1) {
                printField(classFields.get(0).getDeclaringClass(), classFields.get(0));
            } else if (classFields.size() > 1) {
                for (Field f : classFields.values()) {
                    printField(f.getDeclaringClass(), f);
                }
            } else {
                throw new NoSuchFieldException();
            }
        } catch (NoSuchFieldException e) {
            if (!(inspectClass.getSuperclass().getName().equals("java.lang.Object"))) {
                inspectValue(parameter, inspectClass.getSuperclass());
            } else {
                System.err.println("The class does not have the field " + parameter);
            }
        } catch (Exception e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
        }
    }

    private HashMap<Integer, Field> getFields(String parameter, Class<?> inspectClass, Integer index)
            throws NoSuchFieldException, SecurityException {
        HashMap<Integer, Field> fields = new HashMap<Integer, Field>();

        for (Field f : inspectClass.getDeclaredFields()) {
            if (f.getName().equals(parameter)) {
                fields.put(index++, f);
            }
        }

        return fields;
    }

    private void printObjectProperties() {
        try {
            Class<?> inspectClass = inspectTarget.getClass();
            System.err.println("-----   CLASS    -----");
            System.err.print(inspectTarget.toString() + " is an instance of class ");
            System.err.println(inspectClass.getName());
            printSuperClasses();
            printInterfaces();
            System.err.println("----- PARAMETERS -----");
            printFields(inspectClass);
            System.err.println("-----  METHODS   -----");
            printObjectMethods(inspectClass);
            System.err.println("----------------------");
        } catch (Exception e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        }
    }

    private void printSuperClasses() {
        HashMap<Class<?>, Class<?>> superClasses = getSuperClasses(inspectTarget.getClass());
        if (superClasses.size() > 0) {
            System.err.println("----- SUPER CLASS ----");
            for (Class<?> c : superClasses.values()) {
                System.err.println(c.getName());
            }
        }
    }

    private HashMap<Class<?>, Class<?>> getSuperClasses(Class<?> inspectClass) {
        HashMap<Class<?>, Class<?>> returnMap = new HashMap<Class<?>, Class<?>>();
        if (!inspectClass.getName().equals("java.lang.Object")) {
            returnMap.put(inspectClass.getSuperclass(), inspectClass.getSuperclass());
            returnMap.putAll(getSuperClasses(inspectClass.getSuperclass()));
        }
        return returnMap;
    }

    private void printInterfaces() {
        HashMap<Class<?>, Class<?>> interfaces = getInterfaces(inspectTarget.getClass());
        if (interfaces.size() > 0) {
            System.err.println("----- INTERFACES -----");
            for (Class<?> i : interfaces.values()) {
                System.err.println(i.getName());
            }
        }
    }

    private HashMap<Class<?>, Class<?>> getInterfaces(Class<?> inspectClass) {
        HashMap<Class<?>, Class<?>> returnMap = new HashMap<Class<?>, Class<?>>();
        if (!inspectClass.getName().equals("java.lang.Object")) {
            for (Class<?> i : inspectClass.getInterfaces()) {
                returnMap.put(i, i);
            }
            returnMap.putAll(getInterfaces(inspectClass.getSuperclass()));
        }
        return returnMap;
    }

    private void printFields(Class<?> inspectClass) throws IllegalAccessException {
        while (!inspectClass.getName().equals("java.lang.Object")) {
            Field[] classFields = inspectClass.getDeclaredFields();

            for (Field f : classFields) {
                printField(inspectClass, f);
            }

            inspectClass = inspectClass.getSuperclass();
        }
    }

    private void printField(Class<?> inspectClass, Field f) throws IllegalAccessException {
        f.setAccessible(true);
        System.err.print(inspectClass.getSimpleName() + ": ");
        System.err.print(Modifier.toString(f.getModifiers()) + " ");
        System.err.print(f.getType().toString() + " ");
        System.err.print(f.getName() + " = ");
        System.err.println(getFieldValue(f));
    }

    private String getFieldValue(Field f) throws IllegalArgumentException, IllegalAccessException {
        return f.get(inspectTarget).toString();
    }

    private void printObjectMethods(Class<?> inspectClass) {
        if (inspectClass.getName().equals("java.lang.Object")) {
            return;
        } else {
            for (Method m : inspectClass.getDeclaredMethods()) {
                System.err.println(inspectClass.getSimpleName() + ": " + m.toString());
            }
            printObjectMethods(inspectClass.getSuperclass());
        }
    }
}
