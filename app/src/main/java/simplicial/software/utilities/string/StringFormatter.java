package simplicial.software.utilities.string;

public class StringFormatter {
    public static void fillStringWithPositiveDoubleFast(char[] text, int offset, double value,
                                                        int placesLeft, int placesRight) {
        value += 0.5 / Math.pow(10, placesRight);
        int textIndex = placesLeft + offset;
        int intValue = (int) value;
        int reduced;

        while (placesLeft > 0) {
            reduced = intValue / 10;
            text[placesLeft - 1 + offset] = (char) (intValue - reduced * 10 + '0');
            placesLeft--;
            intValue = reduced;
        }

        if (placesRight > 0) {
            text[textIndex] = '.';
            textIndex++;
        }

        while (placesRight > 0) {
            reduced = ((int) (value)) * 10;
            value *= 10;
            intValue = (int) value;
            text[textIndex] = (char) (intValue - reduced + '0');
            textIndex++;
            placesRight--;
        }
    }
}
