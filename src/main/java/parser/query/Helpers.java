package parser.query;

public class Helpers {

    public static String firstParen(String input) {
        return Helpers.parseKeyword(input, "(").rest();
    }

    public static <T> String lastParen(Result<T> result) {
        return Helpers.parseKeyword(result.rest(), ")").rest();
    }

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
