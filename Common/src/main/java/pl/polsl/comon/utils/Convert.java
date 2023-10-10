package pl.polsl.comon.utils;

public final class Convert {
    public static long convertToSeconds(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty.");
        }

        input = input.replaceAll("\\s", "").toLowerCase();

        try {
            if (input.endsWith("s")) {
                return Long.parseLong(input.substring(0, input.length() - 1));
            } else if (input.endsWith("m")) {
                final long minutes = Long.parseLong(input.substring(0, input.length() - 1));
                return minutes * 60;
            } else if (input.endsWith("h")) {
                final long hours = Long.parseLong(input.substring(0, input.length() - 1));
                return hours * 3600;
            } else {
                throw new IllegalArgumentException("Invalid input format. Use 'Xs', 'Xm', or 'Xh'.");
            }
        } catch (final NumberFormatException e) {throw new IllegalArgumentException("Invalid input format. Use numeric values.");
        }
    }
}
