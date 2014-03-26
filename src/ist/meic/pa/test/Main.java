package ist.meic.pa.test;

public class Main {

    public static void main(String[] args) {
        TotalCoise c = new TotalCoise();
        c.a = 10;
        c.b = "Olá!";

        new ist.meic.pa.Inspector().inspect(c);
    }
}
