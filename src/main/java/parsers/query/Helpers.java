package parsers.query;

public class Helpers {

    /**
     * Parses the first word from the input string. A word is considered to be a sequence of alphanumeric characters,
     * including '+' and '@' and a few other special characters, starting from the beginning of the string.
     *
     * @param input The string from which to parse the word.
     * @return A Result object containing the parsed word, the rest of the string, and a boolean indicating if the parse was successful.
     */
    public static Result<String> parseWord(String input) {
        if (input.isEmpty()) {
            return new Result<>("", input, false);
        }

        if (input.startsWith(" ")) {
            input = input.trim();
        }

        String wordChars = "+-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890@";

        int wordEnd = 0;
        while (true) {
            if (wordEnd == input.length()) break;
            char c = input.charAt(wordEnd);
            if (wordChars.indexOf(c) == -1) break;
            wordEnd++;
        }

        String parsed = input.substring(0, wordEnd);
        String rest = input.substring(wordEnd);

        boolean matched = !parsed.isEmpty() && !parsed.contains("(") && !parsed.contains(")");

        return new Result<>(parsed.trim(), rest.trim(), matched);
    }

    /**
     * Tries to parse any of the specified keywords from the beginning of the input string.
     *
     * @param input The string from which to parse the keyword.
     * @param keywords An array of keywords to match against the beginning of the input string.
     * @return A Result object containing the parsed keyword, the rest of the string after the keyword, and a boolean indicating if the parse was successful.
     */
    public static Result<String> parseKeywords(String input, String[] keywords) {
        if (input.isEmpty()) {
            return new Result<>("", input, false);
        }
        for (String kw : keywords) {
            if (input.toLowerCase().startsWith(kw.toLowerCase())) {
                String rest = input.substring(kw.length());
                return new Result<>(kw, rest.trim(), true);
            }
        }
        return new Result<>("", input, false);
    }

    /**
     * Tries to parse a specified keyword from the beginning of the input string.
     *
     * @param input The string from which to parse the keyword.
     * @param keyword The keyword to match against the beginning of the input string.
     * @return A Result object containing the parsed keyword, the rest of the string after the keyword, and a boolean indicating if the parse was successful.
     */
    public static Result<String> parseKeyword(String input, String keyword) {
        if (input.isEmpty()) {
            return new Result<>("", input, false);
        }
        if (input.toLowerCase().startsWith(keyword.toLowerCase())) {
            String rest = input.substring(keyword.length());
            return new Result<>(keyword, rest.trim(), true);
        }
        return new Result<>("", input, false);
    }

}
