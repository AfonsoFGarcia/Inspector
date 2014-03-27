package ist.meic.pa.test;

public class Coise implements InterCoise {
    public Integer a;
    public String b;

    public void setA(Integer a) {
        System.out.println("Setting a to " + a);
        this.a = a;
    }

    @SuppressWarnings("unused")
    private int somaTodos(int x, int y, int z) {
        return x + y + z;
    }

    @Override
    public void escreveCoisas(int i, String s, char c, boolean t, long l, float f, double d, byte b) {
        System.out.println(i + " " + s + " " + c + " " + t);
        System.out.println(l + " " + f + " " + d + " " + b);
    }
}