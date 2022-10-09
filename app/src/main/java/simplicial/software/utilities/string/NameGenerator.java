package simplicial.software.utilities.string;

import java.util.Set;

public class NameGenerator {
    public static String generateUniqueName(String base, Set<String> currentObjects) {
        String name;
        int counter = 1;
        boolean duplicate;
        do {
            name = base + " " + counter;
            duplicate = currentObjects.contains(name);
            counter++;
        } while (duplicate);
        return name;
    }
}
