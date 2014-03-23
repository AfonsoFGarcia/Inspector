package ist.meic.pa;

public class Main {

    public static class Coise {
        public Integer a;
        public String b;

        public void setB(String a) {
            System.out.println("Setting a to " + a);
            this.b = a;
        }
    }

    public static void main(String[] args) {
        Coise c = new Coise();
        c.a = 10;
        c.b = "Olá!";

        new ist.meic.pa.Inspector().inspect(c);
    }
}
