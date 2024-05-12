package parsers.query;

import ast.ASTNode;

import java.util.Arrays;

public class Result<T> {

    private final T parsed;
    private final String rest;
    private final boolean matched;

    /**
     * Constructs a Result with the parsed object, the remaining string, and the match status.
     *
     * @param parsed The result of the parsing.
     * @param rest The remaining part of the string after parsing.
     * @param matched True if the parsing was successful, false otherwise.
     */
    public Result(T parsed, String rest, boolean matched) {
        this.parsed = parsed;
        this.rest = rest;
        this.matched = matched;
    }

    /**
     * Returns the parsed object.
     *
     * @return The parsed object.
     */
    public T parsed() {
        return parsed;
    }

    /**
     * Returns the remaining string after parsing, trimmed of leading and trailing whitespace.
     *
     * @return The trimmed remaining string.
     */
    public String rest() {
        return rest.trim();
    }

    /**
     * Returns true if the parsing was successful.
     *
     * @return True if the parsing matched, false otherwise.
     */
    public boolean isParsed() {
        return matched;
    }

    /**
     * Throws an exception with the provided error message if parsing was not successful.
     *
     * @param errMessage The error message to include in the exception if parsing failed.
     *                   The substring "$res" in errMessage will be replaced with the `parsed.toString()` value.
     */
    public void errorIfNotParsed(String errMessage) {
        if (!matched) {
            String message = errMessage.replace("$res", "\"" + parsed.toString() + "\"");
            RuntimeException exception = new RuntimeException(message);
            StackTraceElement[] trace = exception.getStackTrace();
            exception.setStackTrace(Arrays.copyOfRange(trace, 1, trace.length));
            throw exception;
        }
    }

    /**
     * Returns a string representation of this Result.
     *
     * @return A string describing this Result, including its parsed object, remaining string, and match status.
     *         If the parsed object is an instance of ASTNode, its queryString will also be included.
     */
    @Override
    public String toString() {
        String str = "parser.query.ParserResult{" +
                     "parsed=" + parsed +
                     ", rest='" + rest + '\'' +
                     ", matched=" + matched +
                     '}';
        if (parsed instanceof ASTNode) {
            str += "\nqueryString='" + ((ASTNode<?>) parsed).queryString() + "'";
        }
        return str;
    }

}
