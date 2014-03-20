package ist.meic.pa;

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
        String[] methodArgs = new String[command.length - 2];
        System.arraycopy(command, 2, methodArgs, 0, command.length - 2);
    }

    private void modifyValue(String parameter, String value) {
        // TODO Auto-generated method stub

    }

    private void inspectValue(String parameter) {
        // TODO Auto-generated method stub

    }

    private void printObjectProperties() {
        // TODO Auto-generated method stub
    }
}
