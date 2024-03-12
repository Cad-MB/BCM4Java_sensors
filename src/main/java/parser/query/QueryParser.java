package parser.query;

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
import requests.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryParser {

    Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    // "bool" bExp cont|"gather" gather cont
    public Result<Query> parseQuery(String inputStr) {
        Result<String> p = Helpers.parseWord(inputStr);

        switch (p.parsed().toLowerCase()) {
            case "bool":
                Result<BExp> bExp = parseBExp(p.rest());
                assert bExp != null;

                Result<Cont> cont = parseCont(bExp.rest());

                BQuery bQuery = new BQuery(bExp.parsed(), cont.parsed());
                return new Result<>(bQuery, cont.rest(), true);
            case "gather":
                Result<Gather> gather = parseGather(p.rest());
                assert gather != null;

                Result<Cont> _cont = parseCont(gather.rest());


                GQuery gQuery = new GQuery(gather.parsed(), _cont.parsed());
                return new Result<>(gQuery, _cont.rest(), true);
            default:
                throw new RuntimeException("invalid query type: " + p.parsed());
        }
    }

    // ()
    public Result<BExp> parseBExp(String inputStr) {
        String[] ops = new String[]{ "and", "or", }; // +not
        String inputStart = Helpers.firstParen(inputStr);


        // not
        Result<String> notRes = Helpers.parseKeyword(inputStart, "not");
        if (notRes.matched()) {
            Result<BExp> expRes = parseBExp(notRes.rest());
            return new Result<>(
                new NotBExp(expRes.parsed()),
                expRes.rest(),
                true
            );
        }

        // cExp
        Result<CExp> cExpRes = parseCExp(inputStart);
        Result<String> op = Helpers.parseKeywords(cExpRes.rest(), ops);
        if (!op.matched() && cExpRes.matched()) {
            String rest = Helpers.lastParen(op);
            return new Result<>(new CExpBExp(cExpRes.parsed()), rest, true);
        }

        // sensor id
        Result<String> sensorId = Helpers.parseWord(inputStart);
        if (sensorId.parsed().startsWith("@")) {
            return new Result<>(new SBExp(sensorId.parsed().substring(1)), sensorId.rest(), true);
        }


        // and - or
        Result<BExp> left;
        if (cExpRes.matched()) {
            left = new Result<>(new CExpBExp(cExpRes.parsed()), cExpRes.rest(), true);
        } else {
            left = parseBExp(inputStart);
        }
        op = Helpers.parseKeywords(left.rest(), ops);
        Result<BExp> right = parseBExp(op.rest());

        String rest = Helpers.lastParen(right);
        switch (op.parsed()) {
            case "and":
                return new Result<>(
                    new AndBExp(left.parsed(), right.parsed()),
                    rest,
                    true
                );
            case "or":
                return new Result<>(
                    new OrBExp(left.parsed(), right.parsed()),
                    rest,
                    true
                );
            default:
                throw new RuntimeException("unable to parse " + inputStr + " as bExp");
        }
    }

    private Result<Cont> parseCont(String inputStr) {
        String inputStart = Helpers.firstParen(inputStr);

        Result<String> typeRes = Helpers.parseWord(inputStart);
        switch (typeRes.parsed().toLowerCase()) {
            case "empty":
                String restE = Helpers.lastParen(typeRes);
                return new Result<>(new ECont(), restE, true);
            case "dir":
                Result<Dirs> dirRes = parseDirs(typeRes.rest());
                Result<String> nbJmpRes = Helpers.parseWord(dirRes.rest());
                int nbJmp = Integer.parseInt(nbJmpRes.parsed());

                String restD = Helpers.lastParen(nbJmpRes);
                return new Result<>(new DCont(dirRes.parsed(), nbJmp), restD, true);
            case "flood":
                Result<Base> baseRes = parseBase(typeRes.rest());
                Result<String> distanceRes = Helpers.parseWord(baseRes.rest());
                double distance = Double.parseDouble(distanceRes.parsed());

                String restF = Helpers.lastParen(distanceRes);
                return new Result<>(new FCont(baseRes.parsed(), distance), restF, true);
            default:
                throw new RuntimeException("invalid continuation type: " + typeRes.parsed());
        }
    }

    public Result<Gather> parseGather(String inputStr) {
        String inputStart = Helpers.firstParen(inputStr);
        ArrayList<String> sensorIds = new ArrayList<>();

        Result<String> result = Helpers.parseWord(inputStart);
        while (result.matched() && result.parsed().startsWith("@")) {
            sensorIds.add(result.parsed().substring(1));
            result = Helpers.parseWord(result.rest());
        }

        Gather gather = new FGather(sensorIds.get(0));
        for (int i = 1; i < sensorIds.size(); i++) {
            gather = new RGather(sensorIds.get(i), gather);
        }

        String rest = Helpers.lastParen(result);
        return new Result<>(gather, rest, true);
    }

    public Result<Base> parseBase(String inputStr) {
        String inputStart = Helpers.firstParen(inputStr);

        Result<String> res1 = Helpers.parseWord(inputStart);
        if (res1.parsed().equalsIgnoreCase("this")) {
            String rest = Helpers.lastParen(res1);
            return new Result<>(new RBase(), rest, true);
        }
        Result<String> res2 = Helpers.parseWord(res1.rest());
        double x = Double.parseDouble(res1.parsed());
        double y = Double.parseDouble(res2.parsed());

        String rest = Helpers.lastParen(res2);
        return new Result<>(new ABase(new Position(x, y)), rest, true);
    }

    public Result<Dirs> parseDirs(String inputStr) {
        HashMap<String, Direction> dirMap = new HashMap<>();
        dirMap.put("nw", Direction.NW);
        dirMap.put("ne", Direction.NE);
        dirMap.put("sw", Direction.SW);
        dirMap.put("se", Direction.SE);

        String inputStart = Helpers.firstParen(inputStr);
        String[] keywords = { "nw", "ne", "sw", "se" };
        Result<String> res1 = Helpers.parseKeywords(inputStart, keywords);
        res1.errorIfNotMatched("cannot parse $res as direction");

        List<Direction> directions = new ArrayList<>();
        directions.add(dirMap.get(res1.parsed()));

        Result<String> currResult = Helpers.parseKeywords(res1.rest(), keywords);
        while (currResult.matched()) {
            directions.add(dirMap.get(currResult.parsed()));
            currResult = Helpers.parseKeywords(currResult.rest(), keywords);
        }

        if (directions.size() > 1) {
            directions = directions.stream().distinct().collect(Collectors.toList());
            Dirs dir = new FDirs(directions.get(0));
            for (int i = 1; i < directions.size(); i++) {
                dir = new RDirs(directions.get(i), dir);
            }

            String rest = Helpers.lastParen(currResult);
            return new Result<>(dir, rest, true);
        } else {
            String rest = Helpers.lastParen(res1);
            return new Result<>(new FDirs(directions.get(0)), rest, true);
        }
    }

    // ()
    public Result<CExp> parseCExp(String inputStr) {
        String inputStart = Helpers.firstParen(inputStr);
        String[] ops = new String[]{ "=", ">=", ">", "<=", "<" };

        Result<Rand> left = parseRand(inputStart);
        if (!left.matched()) {
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

        String rest = Helpers.lastParen(right);
        return new Result<>(cExp, rest, true);
    }

    public Result<Rand> parseRand(String inputStr) {
        Result<String> result = Helpers.parseWord(inputStr);
        if (!result.matched()) {
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

