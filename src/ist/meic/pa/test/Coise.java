package ist.meic.pa.test;

public class Coise implements InterCoise {
    public String a;
    public String b;

    public void setA(Integer a) {
        System.out.println("Setting a to " + a);
        this.a = a.toString();
    }

    @SuppressWarnings("unused")
    private int somaTodos(int x, int y, int z) {
        return x + y + z;
    }

    @Override
    public void escreveCoisas(int i, String s, char c) {
        System.out.println(i + " " + s + " " + c);
    }
}