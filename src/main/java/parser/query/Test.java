package parser.query;

import ast.query.Query;

public class Test {

    public static void main(String[] args) {
        QueryParser parser = new QueryParser();
        String number = "200";
        String sensor = "@temp";

        System.out.println(parser.parseRand(number));
        System.out.println(parser.parseRand(sensor));
        System.out.println(parser.parseCExp(String.format("(%s = %s)", number, sensor)));
        Result<Query> b = parser.parseQuery("bool ((not (20 = 20)) and (@humidity < 20)) (dir (sw ne se) 2)");
        Result<Query> g = parser.parseQuery("gather @temp (flood (10 20) 2)");
        System.out.println(b);
        System.out.println(g);
    }

}
