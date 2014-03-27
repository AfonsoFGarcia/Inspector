package ist.meic.pa.test;

public class Coise implements InterCoise {
    public String a;
    public String b;

    public void setA(Integer a) {
        System.out.println("Setting a to " + a);
        this.a = a.toString();
    }

    @SuppressWarnings("unused")
    private String somaTodos(int x, int y, int z) {
        return new Integer(x + y + z).toString();
    }

    @Override
    public void escreveCoisas(int i, String s, char c, boolean t, long l, float f, double d, byte b) {
        System.out.println(i + " " + s + " " + c + " " + t);
        System.out.println(l + " " + f + " " + d + " " + b);
    }
}