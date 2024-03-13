package parser.query;

import ast.ASTNode;

import java.util.Arrays;

public class Result<T> {

    private final T parsed;
    private final String rest;
    private final boolean matched;

    public Result(T parsed, String rest, boolean matched) {
        this.parsed = parsed;
        this.rest = rest;
        this.matched = matched;
    }

    public T parsed() {
        return parsed;
    }

    public String rest() {
        return rest;
    }

    public boolean isParsed() {
        return matched;
    }

    public void errorIfNotParsed(String errMessage) {
        if (!matched) {
            String message = errMessage.replace("$res", "\"" + parsed.toString() + "\"");
            RuntimeException exception = new RuntimeException(message);
            StackTraceElement[] trace = exception.getStackTrace();
            exception.setStackTrace(Arrays.copyOfRange(trace, 1, trace.length));
            throw exception;
        }
    }

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
