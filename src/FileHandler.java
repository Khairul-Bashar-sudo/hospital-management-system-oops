import java.io.*;
import java.util.*;


class FileHandler {
public static void saveList(List<String> list, String filename) {
try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
for (String s : list) pw.println(s);
} catch (Exception e) { ConsoleColors.printlnError("Error saving " + filename + ": " + e.getMessage()); }
}


public static List<String> loadList(String filename) {
List<String> out = new ArrayList<>();
try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
String line;
while ((line = br.readLine()) != null) out.add(line);
} catch (Exception ignored) {}
return out;
}
}
