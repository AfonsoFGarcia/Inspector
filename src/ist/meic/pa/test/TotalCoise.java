package ist.meic.pa.test;

public class TotalCoise extends Coise {
    @SuppressWarnings("unused")
    private boolean c;
    public Integer a;

    public TotalCoise(Integer a) {
        super.a = a.toString();
        this.a = a;
    }

    public void setC(boolean c) {
        this.c = c;
        System.out.println("Oi");
    }

    public void setA(Long a) {
        System.out.println("Setting Long a to " + a);
        this.a = a.intValue();
    }

    public void setC(String s) {
        this.c = Boolean.parseBoolean(s);
    }

    @Override
    public void escreveCoisas(int i, String s, char c) {
        System.out.println(c + " " + s + " " + i);
    }
}
