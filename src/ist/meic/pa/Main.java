package ist.meic.pa;

public class Main {

    public static class Coise {
        public Integer a;
        public String b;

        public void setA(Integer a) {
            System.out.println("Setting a to " + a);
            this.a = a;
        }

        private int somaTodos(int x, int y, int z) {
            return x + y + z;
        }

        public void escreveCoisas(int i, String s, char c, boolean t, long l, float f, double d, byte b) {
            System.out.println(i + " " + s + " " + c + " " + t);
            System.out.println(l + " " + f + " " + d + " " + b);
        }
    }

    public static void main(String[] args) {
        Coise c = new Coise();
        c.a = 10;
        c.b = "Olá!";

        new ist.meic.pa.Inspector().inspect(c);
    }
}
