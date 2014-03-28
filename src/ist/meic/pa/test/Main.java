package ist.meic.pa.test;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting main");
        TotalCoise c = new TotalCoise(10);
        c.b = "Olá!";
        System.out.println("Calling inspector");
        new ist.meic.pa.Inspector().inspect(c);
        System.out.println("Ending main");
    }
}
