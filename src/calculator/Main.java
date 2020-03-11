package calculator;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final String ASSIGN = ".*=.*";
    public static final String COMMAND = "/.*";
    public static final String EXPRESSION = "[()]*[-+]?\\w+[()]*(\\s*[()]*([-+/*]+[()]*\\s*[()]*[-+]?\\w+[()]*)?)*[()]*"; //(ONLY addition/subtraction will work)
    public static final String SHOW = "[a-zA-Z]+";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Pattern comm = Pattern.compile(COMMAND);
        Pattern expr = Pattern.compile(EXPRESSION);
        Pattern assg = Pattern.compile(ASSIGN);
        Pattern show = Pattern.compile(SHOW);
        Matcher commMatcher;
        Matcher exprMatcher;
        Matcher assgMatcher;
        Matcher showMatcher;
        HashMap<String, BigInteger> vars = new HashMap<>();

        while (true) {
            String line = in.nextLine();        // Input
            commMatcher = comm.matcher(line);   // Is command?
            exprMatcher = expr.matcher(line);   // Or expression?
            assgMatcher = assg.matcher(line);
            showMatcher = show.matcher(line);

            switch (line) {
                case "/exit":
                    System.out.println("Bye!");
                    return;
                case "/help":
                    System.out.println("Calculating addition or subtraction");
                    break;
                case "":
                    break;
                default:
                    if (commMatcher.matches()) {
                        System.out.println("Unknown command");
                    }
//                    else if (exprMatcher.matches()) {
//                        calculateExpr(line, vars);
//                    }
                    else if (showMatcher.matches()) {
                        showVariable(line, vars);
                    }
                    else if (assgMatcher.matches()) {
                        assignVariable(line, vars);
                        for (var elem : vars.entrySet()) {
//                            System.out.println("CHECK: " + elem.getKey() + " - key|" + elem.getValue() + " - val");
                        }
                    }
                    else {
                        if (isValid(line) && exprMatcher.matches()) {
                            calculateExpr(line, vars);
                        } else {
                            System.out.println("Invalid expression");
                        }
                    }
            }
        }
    }

    private static boolean isValid(String line) {
        ArrayDeque<Character> brackets = new ArrayDeque<>();

        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '(') {
                brackets.offerFirst(line.charAt(i));
            } else if (line.charAt(i) == ')') {
                if (!brackets.isEmpty()) {
                    brackets.pollFirst();
                } else return false;
            }
        }
        if (brackets.isEmpty())
            return true;
        else
            return false;
    }

    private static void showVariable(String line, HashMap<String, BigInteger> vars) {
        if (vars.containsKey(line)) {
            System.out.println(vars.get(line));
        } else {
            System.out.println("Unknown variable");
        }
    }

    private static void assignVariable(String line, HashMap<String, BigInteger> vars) {
        String[] elems = line.split("=");

        if (elems.length != 2) {
            System.out.println("Invalid assignment");
            return;
        }
        for (int i = 0; i < elems.length; i++)
            elems[i] = elems[i].trim();
        if (!elems[0].matches("[a-zA-Z]+")) {
            System.out.println("Invalid identifier");
            return;
        }
        if (elems[1].matches("[0-9]+")) {
            vars.put(elems[0], new BigInteger(elems[1]));
        }
        else if (elems[1].matches("[a-zA-Z]+")) {
            if (vars.containsKey(elems[1])) {
                vars.put(elems[0], vars.get(elems[1]));
            } else {
                System.out.println("Unknown variable");
                return;
            }
        }
        else {
            System.out.println("Invalid assignment");
            return;
        }
    }

//    static void calculateExpr(String expr, HashMap<String, Integer> vars) {
//        String[] temp = expr.split("\\s+");
//        ArrayDeque<String> postfixExpr = infixToPostfix(expr);
//        ArrayDeque<Integer> result = new ArrayDeque<>();
//
//        int result = 0;
//        if (temp[0].matches("[0-9]+")) {
//            result = Integer.parseInt(temp[0]);
//        }
//        if (temp[0].matches("[a-zA-Z]+")) {
//            if (vars.containsKey(temp[0])) {
//                result = vars.get(temp[0]);
//            } else {
//                System.out.println("Unknown variable");
//                return;
//            }
//        }
//        for (int i = 1; i < temp.length; i += 2) {
//            int operand = 0; //Integer.parseInt(temp[i + 1]);
//            if (temp[i + 1].matches("[0-9]+")) {
//                operand = Integer.parseInt(temp[i + 1]);
//            } else if (temp[i + 1].matches("[a-zA-Z]+")) {
//                if (vars.containsKey(temp[i + 1])) {
//                    operand = vars.get(temp[i + 1]);
//                } else {
//                    System.out.println("Unknown variable");
//                    return;
//                }
//            }
//            result = minusOrPlus(temp[i]) ? result - operand : result + operand;
//        }
//        System.out.println(result);
//    }

    static void calculateExpr(String expr, HashMap<String, BigInteger> vars) {
        String[] temp = expr.split("\\s+");
        ArrayDeque<String> postfixExpr = infixToPostfix(expr);
        ArrayDeque<BigInteger> result = new ArrayDeque<>();

        while (!postfixExpr.isEmpty()) {
            String buff = postfixExpr.pollLast();
            if (buff.matches("[0-9]+")) {
                result.offerFirst(new BigInteger(buff));
            } else if (buff.matches("[a-zA-Z]+")) {
                if (vars.containsKey(buff)) {
                    result.offerFirst(vars.get(buff));
                } else {
                    System.out.println("Unknown variable");
                    return;
                }
            } else {
                try {
                    BigInteger scnd = result.pollFirst(), frst = result.pollFirst();
                    char oper = buff.charAt(0);
                    switch (oper) {
                        case '+':
                            result.offerFirst(frst.add(scnd));
                            break;
                        case '-':
                            result.offerFirst(frst.subtract(scnd));
                            break;
                        case '*':
                            result.offerFirst(frst.multiply(scnd));
                            break;
                        case '/':
                            result.offerFirst(frst.divide(scnd));
                            break;
                    }
                } catch (NullPointerException exp) {
                    System.out.println("Invalid expression");
                    return;
                }
            }
        }
        if (result.size() == 1) {
            System.out.println(result.getFirst());
        } else {
            System.out.println("Invalid expression");
        }
    }

    static boolean minusOrPlus(String operation) {
        operation = operation.replaceAll("-{2}", "+");
        return operation.contains("-");
    }

    public static ArrayDeque<String> infixToPostfix(String infix) { //Reverse Polish Notation
        ArrayDeque<String> postfix = new ArrayDeque<>();
        ArrayDeque<String> stack = new ArrayDeque<>();

        for (int i = 0; i < infix.length(); i++) {
            char sym = infix.charAt(i);
            if (isOperator(sym)) {
                int k = i;
                while (infix.charAt(k) == '-' || infix.charAt(k) == '+') k++;
                if (sym == '-' || sym == '+') {
                    sym = minusOrPlus(infix.substring(i, k == i ? k + 1 : k)) ? '-' : '+';
                }
                i = k == i ? i : k - 1;
                while (!stack.isEmpty() && checkPrecedence(sym, stack.peekFirst().charAt(0))) {
                    postfix.offerFirst(stack.pollFirst());
                }
                stack.offerFirst(Character.toString(sym));
            } else if (sym == '(') {
                stack.offerFirst(Character.toString(sym));
            } else if (sym == ')') {
                while (!stack.isEmpty() && !"(".equals(stack.peekFirst())) {
                    postfix.offerFirst(stack.pollFirst());
                }
                stack.pollFirst();
            } else if (sym >= '0' && sym <= '9') {
//                Scanner scan = new Scanner(infix.substring(i));
                Pattern patt = Pattern.compile("\\d+");
                Matcher matc = patt.matcher(infix.substring(i));
                if (matc.find()) {
                    //int val = Integer.parseInt(matc.group());
                    postfix.offerFirst(matc.group());
                    while (i + 1 < infix.length() && infix.charAt(i + 1) >= '0' && infix.charAt(i + 1) <= '9') i++;
                }
            } else if ((sym >= 'a' && sym <= 'z') ||
                    (sym >= 'A' && sym <= 'Z')) {
                Pattern patt = Pattern.compile("[a-zA-Z]+");
                Matcher matc = patt.matcher(infix.substring(i));
                if (matc.find()) {
                    String val = matc.group();
                    postfix.offerFirst(val);
                    while (i + 1 < infix.length() && ((infix.charAt(i + 1) >= 'a' && infix.charAt(i + 1) <= 'z') ||
                            (infix.charAt(i + 1) >= 'A' && infix.charAt(i + 1) <= 'Z'))) i++;
                }
            }
        }
        while (!stack.isEmpty()) {
            postfix.offerFirst(stack.pollFirst());
        }
        return postfix;
    }

    public static boolean isOperator(char c){
        if(c == '+' || c == '-' || c == '*' || c =='/')
            return true;
        return false;
    }

    public static boolean checkPrecedence(char c1, char c2){
        if((c2 == '+' || c2 == '-') && (c1 == '+' || c1 == '-'))
            return true;
        else if((c2 == '*' || c2 == '/') &&
                (c1 == '+' || c1 == '-' || c1 == '*' || c1 == '/'))
            return true;
        else
            return false;
    }
}