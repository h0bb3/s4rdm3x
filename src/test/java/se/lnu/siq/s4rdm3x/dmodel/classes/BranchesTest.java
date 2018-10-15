package se.lnu.siq.s4rdm3x.dmodel.classes;

public class BranchesTest {

    public void if_(int a_arg) {
        if (a_arg > 17) {
            System.out.println("Over 17");
        }
    }

    public void if_and(int a_arg1, int a_arg2) {
        if (a_arg1 > 17 && a_arg2 > 17) {
            System.out.println("Over 17");
        }
    }

    public void if_or(int a_arg1, int a_arg2) {
        if (a_arg1 > 17 || a_arg2 > 17) {
            System.out.println("Over 17");
        }
    }

    public void question(int a_arg) {
        String txt = a_arg > 17 ? "Over 17" : "not over 17";
        System.out.println(txt);
    }

    public void if_else(int a_arg) {
        if (a_arg > 17) {
            System.out.println("Over 17");
        } else {
            System.out.println("Under or equals 17");
        }
    }

    public void if_elseif_else(int a_arg) {
        if (a_arg > 17) {
            System.out.println("Over 17");
        } else if (a_arg == 17){
            System.out.println("Equals 17");
        } else {
            System.out.println("Under 17");
        }
    }

    public void for_(int a_arg) {
        for (int i = 0; i < a_arg; i++) {
            System.out.println("argh");
        }
    }

    public void while_(int a_arg) {
        while(a_arg > 0) {
            System.out.println("argh");
            a_arg--;
        }
    }

    public void do_while(int a_arg) {
        do {
            System.out.println("argh");
            a_arg--;
        } while (a_arg > 0);
    }

    public void catch_() {
        try {
            double f = 10.0 / 0.0;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switch_(int a_arg) {
        switch(a_arg) {
            case 0:
            case 1:
            case 2:
            case 3:
                System.out.println("0-3");
                break;
            case 4:
                System.out.println("4");
            default:
                System.out.println("Over 4");

        }
    }
}
