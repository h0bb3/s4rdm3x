import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] a_args) {
        if (a_args.length != 1) {
            System.out.println("Supply directory of files to merge...");

        } else {

            try {

                Stream<Path> paths = Files.walk(Paths.get(a_args[0]));
                String [] globalHeader = null;
                int ix = 0;
                for (Path p : paths.filter(Files::isRegularFile).collect(Collectors.toList())) {
                    try {
                        List<String> lines = Files.readAllLines(p);
                        if (globalHeader == null) {
                            List<String> lines2 = Files.readAllLines(p);
                            System.out.println("globalId\t" + lines2.get(0));
                            globalHeader = readHeader(lines2);
                        }
                        ix = readFile(ix, lines, globalHeader);
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }


    public static int readFile(int a_ix, List<String> a_rows, String[] a_localHeader, String[] a_globalHeader) {

        int[] global2LocalHeader = new int[a_globalHeader.length];
        for(int ghIx = 0; ghIx < a_localHeader.length; ghIx++) {
            global2LocalHeader[ghIx] = -1;
            for (int lhIx = 0; lhIx < a_localHeader.length; lhIx++) {
                if (a_localHeader[lhIx].compareToIgnoreCase(a_globalHeader[ghIx]) == 0) {
                    global2LocalHeader[ghIx] = lhIx;
                    break;
                }
            }
        }

        for (String line : a_rows) {
            String outline = "" + a_ix;
            a_ix++;
            String[] parts = line.split("\t", -1);  // we may have empty cells i.e. a\t\tb -> [a][][b]
            for(int i = 0; i < global2LocalHeader.length; i++) {

                outline += "\t";
                if (global2LocalHeader[i] != -1) {
                    outline += parts[global2LocalHeader[i]];
                } else {
                    outline += "n/a";
                }
            }
            System.out.println(outline);

        }

        return a_ix;
    }

    public static int readFile(int a_ix, List<String> a_rows, String[] a_globalHeader) {
        String [] localHeader = readHeader(a_rows);
        return readFile(a_ix, a_rows, localHeader, a_globalHeader);
    }

    public static String[] readHeader(List<String> a_rows) {
        String headline = a_rows.get(0);
        a_rows.remove(0);
        String [] header = headline.split("\t");
        return header;
    }
}
