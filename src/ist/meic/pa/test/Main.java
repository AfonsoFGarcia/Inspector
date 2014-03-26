package ist.meic.pa.test;

public class Main {

    public static void main(String[] args) {
        TotalCoise c = new TotalCoise(10);
        c.b = "Olá!";

        new ist.meic.pa.Inspector().inspect(c);
    }
}
