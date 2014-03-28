package ist.meic.pa;

import ist.meic.pa.history.History;
import ist.meic.pa.utils.PrimitiveWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class Inspector.
 */
public class Inspector {

    private Object inspectTarget;
    private Class<?> inspectClass;
    private boolean fullInspector;

    public Inspector() {
    }

    /**
     * Gets the inspect target.
     * 
     * @return the inspect target
     */
    public Object getInspectTarget() {
        return inspectTarget;
    }

    /**
     * Sets the inspect target.
     * 
     * @param inspectTarget the new inspect target
     */
    public void setInspectTarget(Object inspectTarget) {
        this.inspectTarget = inspectTarget;
    }

    /**
     * Gets the inspect class.
     * 
     * @return the inspect class
     */
    public Class<?> getInspectClass() {
        return inspectClass;
    }

    /**
     * Sets the inspect class.
     * 
     * @param inspectClass the new inspect class
     */
    public void setInspectClass(Class<?> inspectClass) {
        this.inspectClass = inspectClass;
    }

    /**
     * Checks if is full inspector.
     * 
     * @return true, if is full inspector
     */
    public boolean isFullInspector() {
        return fullInspector;
    }

    /**
     * Sets the full inspector.
     * 
     * @param fullInspector the new full inspector
     */
    public void setFullInspector(boolean fullInspector) {
        this.fullInspector = fullInspector;
    }

    /**
     * Inspects.
     * 
     * @param target the target object
     */
    public void inspect(Object target) {
        setObject(target, target.getClass().isPrimitive() ? false : true, target.getClass());
        readCommands();
    }

    /**
     * Sets the target object.
     * 
     * @param target the target object
     * @param fI true, if the object isn't a primitive type
     * @param cI the object's class (before autoboxing)
     */
    private void setObject(Object target, boolean fI, Class<?> cI) {
        History.save(this);
        inspectTarget = target;
        inspectClass = cI;
        fullInspector = fI;
        printObjectProperties();
    }

    /**
     * Read commands.
     */
    private void readCommands() {
        while (true) {
            System.err.print("> ");
            String[] command = System.console().readLine().split(" ");

            if (command[0].equals("i") && command.length == 2 && fullInspector) {
                inspectField(command[1], inspectTarget.getClass());
            } else if (command[0].equals("m") && command.length == 3 && fullInspector) {
                modifyFieldValue(command[1], command[2], inspectTarget.getClass());
            } else if (command[0].equals("c") && command.length >= 2 && fullInspector) {
                callMethod(command[1], command);
            } else if (command[0].equals("q")) {
                History.createNewStack();
                return;
            } else if (command[0].equals("b")) {
                if (History.rollback(this)) {
                    printObjectProperties();
                } else {
                    System.err.println("There's no previous state to go back to.");
                }

            } else {
                System.err.println("Invalid command.");
            }
        }
    }

    /**
     * Gets the methods with the same name and number of parameters that aren't overriden.
     * 
     * @param method the method name
     * @param numParameters the number of parameters
     * @return the requested methods
     */
    private List<Method> getMethods(String method, Integer numParameters) {
        ArrayList<Method> methods = new ArrayList<Method>();
        Class<?> inspectClass = inspectTarget.getClass();
        while (inspectClass != null) {
            Method[] mets = inspectClass.getDeclaredMethods();

            for (Method m : mets) {
                if (m.getParameterTypes().length == numParameters - 2 && m.getName().equals(method) && !isOverriden(methods, m)) {
                    methods.add(m);
                }
            }
            inspectClass = inspectClass.getSuperclass();
        }
        return methods;
    }

    /**
     * Gets all the methods that aren't overriden.
     * 
     * @return the methods
     */
    private List<Method> getMethods() {
        ArrayList<Method> methods = new ArrayList<Method>();
        Class<?> inspectClass = inspectTarget.getClass();
        while (!inspectClass.equals(Object.class)) {
            Method[] mets = inspectClass.getDeclaredMethods();

            for (Method m : mets) {
                if (!isOverriden(methods, m)) {
                    methods.add(m);
                }
            }
            inspectClass = inspectClass.getSuperclass();
        }
        return methods;
    }

    /**
     * Checks if a method is overriden by any in the list.
     * 
     * @param parents the list of currently obtained methods
     * @param toCheck the method to check
     * @return true, if is overriden by any method in the list
     */
    private boolean isOverriden(List<Method> parents, Method toCheck) {
        for (Method m : parents) {
            if (isOverriden(m, toCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a method is overriden by the parent method.
     * This function is implemented using the code provided in http://stackoverflow.com/a/12134003/2116967
     * 
     * @param parent the parent method
     * @param toCheck the method to check
     * @return true, if is overriden by the parent method
     */
    private boolean isOverriden(Method parent, Method toCheck) {
        if (toCheck.getDeclaringClass().isAssignableFrom(parent.getDeclaringClass())
                && parent.getName().equals(toCheck.getName())) {
            Class<?>[] params1 = parent.getParameterTypes();
            Class<?>[] params2 = toCheck.getParameterTypes();
            if (params1.length == params2.length) {
                for (int i = 0; i < params1.length; i++) {
                    if (!params1[i].equals(params2[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Calls the method.
     * 
     * @param method the name of the method to call
     * @param arguments the arguments that will be used to call the method
     */
    private void callMethod(String method, String[] arguments) {
        try {
            List<Method> methods = getMethods(method, arguments.length);
            if (methods.size() == 1) {
                callMethod(arguments, methods.get(0));
            } else if (methods.size() > 1) {
                for (Method m : methods) {
                    try {
                        callMethod(arguments, m);
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

    /**
     * Calls the method. Auxiliary to callMethod(String method, String[] arguments).
     * 
     * @param arguments the arguments that will be used to call the method
     * @param m the method that will be called
     */
    private void callMethod(String[] arguments, Method m) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Object ret;
        Object[] args = castArguments(arguments, m.getParameterTypes());
        m.setAccessible(true);
        ret = m.invoke(inspectTarget, args);

        if (ret != null) {
            System.err.println(ret.toString());
            setObject(ret, !m.getReturnType().isPrimitive(), m.getReturnType());
        }
    }

    /**
     * Casts the given arguments to match the method declaration.
     * 
     * @param parameters the parameters to cast
     * @param parameterTypes the parameter types to cast
     * @return array of casted objects
     */
    private Object[] castArguments(String[] parameters, Class<?>[] parameterTypes) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Object[] ret = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            ret[i] = castValue(parameters[i + 2], parameterTypes[i]);
        }

        return ret;
    }

    /**
     * Casts a value.
     * 
     * @param value the value to be casted
     * @param parameter the parameter to cast
     * @return the casted object
     */
    private Object castValue(String value, Class<?> parameter) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        } else if (value.startsWith("'") && value.endsWith("'")) {
            return value.charAt(1);
        } else if (parameter.equals(Integer.class) || parameter.equals(Integer.TYPE)) {
            return PrimitiveWrapper.getWrapper(parameter).getConstructor(new Class[] { String.class }).newInstance(value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Modifies the field's value.
     * 
     * @param field the field to modify
     * @param value the new value
     * @param inspectClass the class where the field will be checked
     */
    private void modifyFieldValue(String field, String value, Class<?> inspectClass) {
        try {
            Field classField = getField(field, inspectClass);
            classField.set(inspectTarget, castValue(value, classField.getType()));
            printField(classField);
        } catch (NoSuchFieldException e) {
            if (!(inspectClass.getSuperclass().equals(Object.class))) {
                modifyFieldValue(field, value, inspectClass.getSuperclass());
            } else {
                System.err.println("The class does not have the field " + field);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("The arguments passed do not match the declared type.");
        } catch (Exception e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
            e.printStackTrace();
        }
    }

    /**
     * Inspects a field and changes the inspectTarget to it.
     * 
     * @param field the field to be inspected
     * @param inspectClass the class where the field will be checked
     */
    private void inspectField(String field, Class<?> inspectClass) {
        try {
            Field classField = getField(field, inspectClass);
            printField(classField);
            setObject(classField.get(inspectTarget), !classField.getClass().isPrimitive(), classField.getClass());
        } catch (NoSuchFieldException e) {
            if (!(inspectClass.getSuperclass().equals(Object.class))) {
                inspectField(field, inspectClass.getSuperclass());
            } else {
                System.err.println("The class does not have the field " + field);
            }
        } catch (Exception e) {
            System.err.println("An exception was caught while trying to run the method. Printing it's stack trace.");
        }
    }

    /**
     * Gets the field.
     * 
     * @param field the field
     * @param inspectClass the class where the field will be checked
     * @return the requested field
     */
    private Field getField(String field, Class<?> inspectClass) throws NoSuchFieldException {
        for (Field f : inspectClass.getDeclaredFields()) {
            if (f.getName().equals(field)) {
                return f;
            }
        }
        throw new NoSuchFieldException();
    }

    /**
     * Prints the object properties.
     */
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

    /**
     * Prints the class.
     */
    private void printClass() {
        System.err.println("-----   CLASS    -----");
        System.err.print(inspectTarget.toString() + " is an instance of class ");
        System.err.println(inspectClass.getName());
    }

    /**
     * Prints the superclasses.
     */
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

    /**
     * Prints the interfaces.
     */
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

    /**
     * Prints the fields.
     */
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

    /**
     * Prints the field.
     * 
     * @param f the requested field
     */
    private void printField(Field f) throws IllegalAccessException {
        f.setAccessible(true);
        System.err.print(Modifier.toString(f.getModifiers()) + " ");
        System.err.print(f.getType().toString() + " ");
        System.err.print(f.getName() + " = ");
        Object value = f.get(inspectTarget);
        System.err.println(value == null ? "null" : value.toString());
    }

    /**
     * Prints the object methods.
     */
    private void printObjectMethods() {
        System.err.println("-----  METHODS   -----");

        for (Method m : getMethods()) {
            System.err.println(m.toString());
        }
    }
}