package se.lnu.siq.s4rdm3x.dmodel.classes;

/**
 * Created by tohto on 2017-05-01.
 */
public class ExceptionTest {

    public void TrowsException() throws Exception {
        throw new NumberFormatException("This a test");
    }

    public void catchesException() {

        try {
            double f = 10.0 / 0.0;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
