package parsers.query;

import ast.base.ABase;
import ast.base.Base;
import ast.base.RBase;
import ast.bexp.*;
import ast.cexp.*;
import ast.cont.Cont;
import ast.cont.DCont;
import ast.cont.ECont;
import ast.cont.FCont;
import ast.dirs.Dirs;
import ast.dirs.FDirs;
import ast.dirs.RDirs;
import ast.gather.FGather;
import ast.gather.Gather;
import ast.gather.RGather;
import ast.query.BQuery;
import ast.query.GQuery;
import ast.query.Query;
import ast.rand.CRand;
import ast.rand.Rand;
import ast.rand.SRand;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import sensor_network.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryParser {

    private static final Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * <ul>
     *  <li> "bool"   (bExp)   (cont) </li>
     *  <li> "gather" (gather) (cont) </li>
     * </ul>
     */
    public static Result<Query> parseQuery(String inputStr) {
        inputStr = inputStr.replace("(", " ");
        inputStr = inputStr.replace(")", " ");
        Result<String> p = Helpers.parseWord(inputStr);

        switch (p.parsed().toLowerCase()) {
            case "bool":
                Result<BExp> bExp = parseBExp(p.rest());
                Result<Cont> cont = parseCont(bExp.rest());

                BQuery bQuery = new BQuery(bExp.parsed(), cont.parsed());
                return new Result<>(bQuery, cont.rest(), true);
            case "gather":
                Result<Gather> gather = parseGather(p.rest());
                Result<Cont> _cont = parseCont(gather.rest());


                GQuery gQuery = new GQuery(gather.parsed(), _cont.parsed());
                return new Result<>(gQuery, _cont.rest(), true);
            default:
                throw new RuntimeException("invalid query type: " + p.parsed());
        }
    }

    /**
     * <ul>
     *  <li> "not" (cExp)      </li>
     *  <li> "@sensorId"       </li>
     *  <li> (cExp)            </li>
     *  <li> (cExp) and (cExp) </li>
     *  <li> (cExp) or  (cExp) </li>
     * </ul>
     */

    public static Result<BExp> parseBExp(String inputStr) {
        inputStr = inputStr.replace("(", " ");
        inputStr = inputStr.replace(")", " ");

        String[] ops = new String[]{ "and", "or", }; // +not

        // not
        Result<String> notRes = Helpers.parseKeyword(inputStr, "not");
        if (notRes.isParsed()) {
            Result<BExp> expRes = parseBExp(notRes.rest());
            return new Result<>(
                new NotBExp(expRes.parsed()),
                expRes.rest(),
                true
            );
        }

        // cExp
        Result<CExp> cExpRes = parseCExp(inputStr);
        Result<String> op = Helpers.parseKeywords(cExpRes.rest(), ops);
        if (!op.isParsed() && cExpRes.isParsed()) {
            return new Result<>(new CExpBExp(cExpRes.parsed()), op.rest(), true);
        }

        // sensor id
        Result<String> sensorId = Helpers.parseWord(inputStr);
        if (sensorId.parsed().startsWith("@")) {
            return new Result<>(new SBExp(sensorId.parsed().substring(1)), sensorId.rest(), true);
        }

        // and - or
        Result<BExp> left;
        if (cExpRes.isParsed()) {
            left = new Result<>(new CExpBExp(cExpRes.parsed()), cExpRes.rest(), true);
        } else {
            System.out.println(inputStr);
            left = parseBExp(inputStr);
        }
        op = Helpers.parseKeywords(left.rest(), ops);
        Result<BExp> right = parseBExp(op.rest());

        switch (op.parsed()) {
            case "and":
                return new Result<>(
                    new AndBExp(left.parsed(), right.parsed()),
                    right.rest(),
                    true
                );
            case "or":
                return new Result<>(
                    new OrBExp(left.parsed(), right.parsed()),
                    right.rest(),
                    true
                );
            default:
                throw new RuntimeException("unable to parse " + inputStr + " as bExp");
        }
    }

    /**
     * <ul>
     *  <li> "empty"                      </li>
     *  <li> "dir"   dirs nbJump:number   </li>
     *  <li> "flood" base distance:number </li>
     * </ul>
     */
    public static Result<Cont> parseCont(String inputStr) {
        inputStr = inputStr.replace("(", " ");
        inputStr = inputStr.replace(")", " ");

        Result<String> typeRes = Helpers.parseWord(inputStr);
        switch (typeRes.parsed().toLowerCase()) {
            case "empty":
                return new Result<>(new ECont(), typeRes.rest(), true);
            case "dir":
                Result<Dirs> dirRes = parseDirs(typeRes.rest());
                Result<String> nbJmpRes = Helpers.parseWord(dirRes.rest());
                int nbJmp = Integer.parseInt(nbJmpRes.parsed());

                return new Result<>(new DCont(dirRes.parsed(), nbJmp), nbJmpRes.rest(), true);
            case "flood":
                Result<Base> baseRes = parseBase(typeRes.rest());
                Result<String> distanceRes = Helpers.parseWord(baseRes.rest());
                double distance = Double.parseDouble(distanceRes.parsed());

                return new Result<>(new FCont(baseRes.parsed(), distance), distanceRes.rest(), true);
            default:
                throw new RuntimeException("invalid continuation type: " + typeRes.parsed());
        }
    }

    /**
     * <ul>
     *  <li> "gather"  @sensor1 @sensor2 ... </li>
     * </ul>
     */
    public static Result<Gather> parseGather(String inputStr) {
        inputStr = inputStr.replace("(", " ");
        inputStr = inputStr.replace(")", " ");

        ArrayList<String> sensorIds = new ArrayList<>();

        Result<String> result = Helpers.parseKeyword(inputStr, "@");
        while (result.isParsed()) {
            Result<String> sensorId = Helpers.parseWord(result.rest());
            sensorIds.add(sensorId.parsed());
            result = Helpers.parseKeyword(sensorId.rest(), "@");
        }

        Gather gather = new FGather(sensorIds.get(0));
        for (int i = 1; i < sensorIds.size(); i++) {
            gather = new RGather(sensorIds.get(i), gather);
        }

        return new Result<>(gather, result.rest(), true);
    }


    /**
     * <ul>
     *  <li> this              </li>
     *  <li> x:number y:number </li>
     * </ul>
     */
    public static Result<Base> parseBase(String inputStr) {
        inputStr = inputStr.replace("(", " ");
        inputStr = inputStr.replace(")", " ");

        Result<String> res1 = Helpers.parseWord(inputStr);
        if (res1.parsed().equalsIgnoreCase("this")) {
            return new Result<>(new RBase(), res1.rest(), true);
        }
        Result<String> res2 = Helpers.parseWord(res1.rest());
        double x = Double.parseDouble(res1.parsed());
        double y = Double.parseDouble(res2.parsed());

        return new Result<>(new ABase(new Position(x, y)), res2.rest(), true);
    }


    /**
     * <ul>
     *  <li> nw se ... </li>
     * </ul>
     */
    public static Result<Dirs> parseDirs(String inputStr) {
        inputStr = inputStr.replace("(", " ");
        inputStr = inputStr.replace(")", " ");

        HashMap<String, Direction> dirMap = new HashMap<>();
        dirMap.put("nw", Direction.NW);
        dirMap.put("ne", Direction.NE);
        dirMap.put("sw", Direction.SW);
        dirMap.put("se", Direction.SE);

        String[] keywords = { "nw", "ne", "sw", "se" };
        Result<String> res1 = Helpers.parseKeywords(inputStr, keywords);
        res1.errorIfNotParsed("cannot parse $res as direction");

        List<Direction> directions = new ArrayList<>();
        directions.add(dirMap.get(res1.parsed()));

        Result<String> currResult = Helpers.parseKeywords(res1.rest(), keywords);
        while (currResult.isParsed()) {
            directions.add(dirMap.get(currResult.parsed()));
            currResult = Helpers.parseKeywords(currResult.rest(), keywords);
        }

        if (directions.size() > 1) {
            directions = directions.stream().distinct().collect(Collectors.toList());
            Dirs dir = new FDirs(directions.get(0));
            for (int i = 1; i < directions.size(); i++) {
                dir = new RDirs(directions.get(i), dir);
            }

            return new Result<>(dir, currResult.rest(), true);
        } else {
            return new Result<>(new FDirs(directions.get(0)), res1.rest(), true);
        }
    }

    /**
     * <ul>
     *  <li> rand =  rand </li>
     *  <li> rand <= rand </li>
     *  <li> rand <  rand </li>
     *  <li> rand >= rand </li>
     *  <li> rand >  rand </li>
     * </ul>
     */
    public static Result<CExp> parseCExp(String inputStr) {
        String[] ops = new String[]{ "=", ">=", ">", "<=", "<" };

        Result<Rand> left = parseRand(inputStr);
        if (!left.isParsed()) {
            return new Result<>(null, inputStr, false);
        }
        Result<String> op = Helpers.parseKeywords(left.rest(), ops);
        Result<Rand> right = parseRand(op.rest());


        CExp cExp;
        switch (op.parsed()) {
            case "=":
                cExp = new EqCExp(left.parsed(), right.parsed());
                break;
            case ">=":
                cExp = new GeqCExp(left.parsed(), right.parsed());
                break;
            case ">":
                cExp = new GCExp(left.parsed(), right.parsed());
                break;
            case "<=":
                cExp = new LeqCExp(left.parsed(), right.parsed());
                break;
            case "<":
                cExp = new LCExp(left.parsed(), right.parsed());
                break;
            default:
                throw new RuntimeException("unrecognized operation: " + op.parsed());
        }

        return new Result<>(cExp, right.rest(), true);
    }

    /**
     * <ul>
     *  <li> @sensorId  </li>
     *  <li> double     </li>
     * </ul>
     */
    public static Result<Rand> parseRand(String inputStr) {
        Result<String> result = Helpers.parseWord(inputStr);
        if (!result.isParsed()) {
            return new Result<>(null, inputStr, false);
        }
        String parsed = result.parsed();

        if (parsed.startsWith("@")) {
            String sensorId = parsed.substring(1);
            return new Result<>(new SRand(sensorId), result.rest(), true);
        } else if (numericPattern.matcher(parsed).matches()) {
            double v = Double.parseDouble(parsed);
            return new Result<>(new CRand(v), result.rest(), true);
        }

        return new Result<>(null, inputStr, false);
    }

}

