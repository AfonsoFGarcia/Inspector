package ist.meic.pa;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
            if (command[0].equals("q")) {
                return;
            } else if (command[0].equals("i")) {
                inspectValue(command[1]);
            } else if (command[0].equals("m")) {
                if (command.length != 3) {
                    System.err.println("Value was not specified!");
                    continue;
                }
                modifyValue(command[1], command[2]);
            } else if (command[0].equals("c")) {
                callMethod(command[1], command);
            } else {
                System.err.println("Invalid command.");
            }
        }
    }

    private void callMethod(String method, String[] command) {
        try {
            Class<?> inspectClass = inspectTarget.getClass();
            Object ret = null;
            Method[] mets = inspectClass.getDeclaredMethods();

            for (Method m : mets) {
                if (m.getParameterTypes().length == command.length - 2 && m.getName().equals(method)) {
                    Object[] args = castArguments(command, m.getParameterTypes());
                    m.setAccessible(true);
                    ret = m.invoke(inspectTarget, args);

                    if (ret != null) {
                        System.err.println(ret.toString());
                    }

                    return;
                }
            }
            System.err.println("The method " + method + " does not exist in the inspected class.");
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
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
        } else if (value.startsWith("0x")) {
            return Byte.parseByte(value.substring(2, value.length()), 16);
        } else {
            return parameter.getConstructor(new Class[] { String.class }).newInstance(value);
        }
    }

    private void modifyValue(String parameter, String value) {
        try {
            Field classField = getField(parameter);
            classField.set(inspectTarget, castValue(value, classField.getType()));
            printObjectProperties();
        } catch (NoSuchFieldException e) {
            System.err.println("The class does not have the field " + parameter);
        } catch (IllegalArgumentException e) {
            System.err.println("The arguments passed do not match the declared type.");
        } catch (IllegalAccessException e) {
            System.err.println("");
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void inspectValue(String parameter) {
        try {
            Field classField = getField(parameter);
            System.err.print(Modifier.toString(classField.getModifiers()) + " ");
            System.err.print(classField.getType().toString() + " ");
            System.err.print(classField.getName() + " = ");
            System.err.println(getFieldValue(classField));
        } catch (NoSuchFieldException e) {
            System.err.println("The class does not have the field " + parameter);
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

    private Field getField(String parameter) throws NoSuchFieldException, SecurityException {
        Class<?> inspectClass = inspectTarget.getClass();
        Field classField;
        classField = inspectClass.getDeclaredField(parameter);
        classField.setAccessible(true);
        return classField;
    }

    private void printObjectProperties() {
        try {
            Class<?> inspectClass = inspectTarget.getClass();

            System.err.print(inspectTarget.toString() + " is an instance of class ");
            System.err.println(inspectClass.getName());
            System.err.println("----------");

            Field[] classFields = inspectClass.getDeclaredFields();

            for (Field f : classFields) {
                f.setAccessible(true);
                System.err.print(Modifier.toString(f.getModifiers()) + " ");
                System.err.print(f.getType().toString() + " ");
                System.err.print(f.getName() + " = ");
                System.err.println(getFieldValue(f));
            }
        } catch (IllegalArgumentException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        }
    }

    private String getFieldValue(Field f) throws IllegalArgumentException, IllegalAccessException {
        return f.get(inspectTarget).toString();
    }
}
