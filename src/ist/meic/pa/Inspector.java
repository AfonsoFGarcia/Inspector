package ist.meic.pa;

import java.lang.reflect.Field;
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
                modifyValue(command[1], command[2]);
            } else if (command[0].equals("c")) {
                callMethod(command[1], command);
            } else {
                System.err.println("Invalid command.");
            }
        }
    }

    private void callMethod(String method, String[] command) {

    }

    private void modifyValue(String parameter, String value) {
        Field classField;
        try {
            classField = getField(parameter);
        } catch (NoSuchFieldException e) {
            System.err.println("The class does not have the field " + parameter);
            return;
        }
        try {
            classField.set(inspectTarget, Integer.parseInt(value));
        } catch (IllegalArgumentException e) {
            System.err.println("The inspector only supports modifying fields of type Integer");
        } catch (IllegalAccessException e) {
            System.err.println("");
        }
    }

    private void inspectValue(String parameter) {
        Field classField;

        try {
            classField = getField(parameter);
        } catch (NoSuchFieldException e) {
            System.err.println("The class does not have the field " + parameter);
            return;
        } catch (SecurityException e) {
            System.err.println("The class does not have the field " + parameter);
            return;
        }

        System.err.print(Modifier.toString(classField.getModifiers()) + " ");
        System.err.print(classField.getType().toString() + " ");
        System.err.print(classField.getName() + " = ");
        System.err.println(getFieldValue(classField));
    }

    private Field getField(String parameter) throws NoSuchFieldException, SecurityException {
        Class<?> inspectClass = inspectTarget.getClass();
        Field classField;
        classField = inspectClass.getDeclaredField(parameter);
        classField.setAccessible(true);
        return classField;
    }

    private void printObjectProperties() {
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
    }

    private String getFieldValue(Field f) {
        try {
            return f.get(inspectTarget).toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
