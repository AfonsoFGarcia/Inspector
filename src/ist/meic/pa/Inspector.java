package ist.meic.pa;

import ist.meic.pa.utils.PrimitiveWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class Inspector {

    Object inspectTarget;

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
            } else if (command[0].equals("a") && command.length == 2) {
                useApacheLibrary(command[1]);
            } else if (command[0].equals("h")) {
                printHelp();
            } else if (command[0].equals("q")) {
                return;
            } else {
                System.err.println("Invalid command.");
            }
        }
    }

    private void printHelp() {
        System.err.println("Available commands:");
        System.err.println("* a <value>: Changes if the Apache Commons Lang is used or not. Accepted values: true or false.");
        System.err.println("* i <parameter>: Displays the current value of a parameter of the object being inspected.");
        System.err.println("* m <parameter> <value>: Modified the value of a parameter of the object being inspected.");
        System.err
                .println("* c <method> <parameters>: Calls the method <method> with parameters <parameters> on the object being inspected.");
        System.err.println("* h: Displays the inspector help menu.");
        System.err.println("* q: Quits the inspector.");
    }

    private void useApacheLibrary(String command) {
        Boolean useLibrary = new Boolean(command);
        PrimitiveWrapper.useApacheLibrary(useLibrary);
        if (useLibrary) {
            System.err.println("Using Apache Commons Lang library");
        } else {
            System.err.println("Using Stack Overflow solution");
        }
    }

    private HashMap<Integer, Method> getMethods(String method, String[] command, Class<?> inspectClass, Integer index) {
        if (inspectClass.getName().equals("java.lang.Object")) {
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
                }
            } else if (methods.size() > 1) {
                System.err.println("Choose a method:");

                Integer index = 0;
                for (Method m : methods.values()) {
                    System.err.println(index++ + ": " + m.toString());
                }
                System.err.print("> ");

                String methodNum = System.console().readLine();

                Object[] args = castArguments(command, methods.get(Integer.parseInt(methodNum)).getParameterTypes());
                methods.get(Integer.parseInt(methodNum)).setAccessible(true);
                ret = methods.get(Integer.parseInt(methodNum)).invoke(inspectTarget, args);

                if (ret != null) {
                    System.err.println(ret.toString());
                }
            } else {
                System.err.println("The method " + method + " does not exist in the inspected class.");
            }
        } catch (SecurityException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("The arguments passed do not match the declared types.");
        } catch (InvocationTargetException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
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
        value = value.replace(',', '.');
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        } else if (value.startsWith("'") && value.endsWith("'")) {
            return value.charAt(1);
        } else {
            try {
                return parameter.getConstructor(new Class[] { String.class }).newInstance(value);
            } catch (NoSuchMethodException e) {
                return PrimitiveWrapper.getWrapper(parameter).getConstructor(new Class[] { String.class }).newInstance(value);
            }
        }
    }

    private void modifyValue(String parameter, String value, Class<?> inspectClass) {
        try {
            HashMap<Integer, Field> classFields = getFields(parameter, inspectClass, 0);
            if (classFields.size() == 1) {
                classFields.get(0).set(inspectTarget, castValue(value, classFields.get(0).getType()));
            } else if (classFields.size() > 1) {
                Integer index = 0;
                for (Field f : classFields.values()) {
                    System.err.print(index++ + ": ");
                    printField(f.getDeclaringClass(), f);
                }
                System.err.print("> ");
                Integer fieldValue = Integer.parseInt(System.console().readLine());
                classFields.get(fieldValue).set(inspectTarget, castValue(value, classFields.get(fieldValue).getType()));
            } else {
                throw new NoSuchFieldException();
            }
        } catch (NoSuchFieldException e) {
            System.err.println("The class does not have the field " + parameter);
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("The arguments passed do not match the declared type.");
            return;
        } catch (IllegalAccessException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
            return;
        } catch (InstantiationException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
            return;
        } catch (InvocationTargetException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
            return;
        } catch (NoSuchMethodException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
            return;
        } catch (SecurityException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
            return;
        }
        printObjectProperties();
    }

    private void inspectValue(String parameter, Class<?> inspectClass) {
        try {
            HashMap<Integer, Field> classFields = getFields(parameter, inspectClass, 0);
            if (classFields.size() == 1) {
                printField(classFields.get(0).getClass(), classFields.get(0));
            } else if (classFields.size() > 1) {
                Integer index = 0;
                for (Field f : classFields.values()) {
                    System.err.print(index++ + ": ");
                    printField(f.getDeclaringClass(), f);
                }
                System.err.print("> ");
                Integer fieldValue = Integer.parseInt(System.console().readLine());
                printField(classFields.get(fieldValue).getDeclaringClass(), classFields.get(fieldValue));
            } else {
                throw new NoSuchFieldException();
            }
        } catch (NoSuchFieldException e) {
            if (!(inspectClass.getSuperclass().getName().equals("java.lang.Object"))) {
                inspectValue(parameter, inspectClass.getSuperclass());
            } else {
                System.err.println("The class does not have the field " + parameter);
            }
            return;
        } catch (SecurityException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        }
    }

    private HashMap<Integer, Field> getFields(String parameter, Class<?> inspectClass, Integer index)
            throws NoSuchFieldException, SecurityException {
        if (inspectClass.getName().equals("java.lang.Object")) {
            return new HashMap<Integer, Field>();
        } else {
            HashMap<Integer, Field> fields = new HashMap<Integer, Field>();

            for (Field f : inspectClass.getDeclaredFields()) {
                if (f.getName().equals(parameter)) {
                    fields.put(index++, f);
                }
            }

            fields.putAll(getFields(parameter, inspectClass.getSuperclass(), index));

            return fields;
        }
    }

    private void printObjectProperties() {
        Class<?> inspectClass = inspectTarget.getClass();
        try {
            System.err.print(inspectTarget.toString() + " is an instance of class ");
            System.err.println(inspectClass.getName());
            System.err.println("----------");
            printFields(inspectClass);
            System.err.println("----------");
            printObjectMethods(inspectClass);
        } catch (IllegalArgumentException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        }
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
