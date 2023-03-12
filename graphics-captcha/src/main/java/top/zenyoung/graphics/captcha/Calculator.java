package top.zenyoung.graphics.captcha;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Stack;

/**
 * 数学表达式计算工具类
 *
 * @author young
 */
public class Calculator {
    /**
     * 后缀式栈
     */
    private final Stack<String> postfixStack = new Stack<>();
    /**
     * 运用运算符ASCII码-40做索引的运算符优先级
     */
    private final int[] operatPriority = new int[]{0, 3, 2, 1, -1, 1, 0, 2};

    /**
     * 计算表达式的值
     *
     * @param expression 表达式
     * @return 计算结果
     */
    public static double conversion(@Nonnull final String expression) {
        return (new Calculator()).calculate(expression);
    }

    /**
     * 按照给定的表达式计算
     *
     * @param expression 要计算的表达式例如:5+12*(3+5)/7
     * @return 计算结果
     */
    public double calculate(@Nonnull final String expression) {
        prepare(transform(expression));
        final Stack<String> resultStack = new Stack<>();
        // 将后缀式栈反转
        Collections.reverse(postfixStack);
        // 参与计算的第一个值，第二个值和算术运算符
        String firstValue, secondValue, currentOp;
        while (!postfixStack.isEmpty()) {
            currentOp = postfixStack.pop();
            // 如果不是运算符则存入操作数栈中
            if (!isOperator(currentOp.charAt(0))) {
                currentOp = currentOp.replace("~", "-");
                resultStack.push(currentOp);
            } else {
                // 如果是运算符则从操作数栈中取两个值和该数值一起参与运算
                secondValue = resultStack.pop();
                firstValue = resultStack.pop();
                // 将负数标记符改为负号
                firstValue = firstValue.replace("~", "-");
                secondValue = secondValue.replace("~", "-");
                final BigDecimal tempResult = calculate(firstValue, secondValue, currentOp.charAt(0));
                resultStack.push(tempResult.toString());
            }
        }
        return Double.parseDouble(resultStack.pop());
    }

    /**
     * 数据准备阶段将表达式转换成为后缀式栈
     *
     * @param expression 表达式
     */
    private void prepare(@Nonnull final String expression) {
        final char opLeft = '(', opRight = ')', opComma = ',';
        final Stack<Character> opStack = new Stack<>();
        // 运算符放入栈底元素逗号，此符号优先级最低
        opStack.push(opComma);
        final char[] arr = expression.toCharArray();
        // 当前字符的位置
        int currentIndex = 0;
        // 上次算术运算符到本次算术运算符的字符的长度便于或者之间的数值
        int count = 0;
        // 当前操作符和栈顶操作符
        char currentOp, peekOp;
        for (int i = 0; i < arr.length; i++) {
            currentOp = arr[i];
            // 如果当前字符是运算符
            if (isOperator(currentOp)) {
                if (count > 0) {
                    // 取两个运算符之间的数字
                    postfixStack.push(new String(arr, currentIndex, count));
                }
                peekOp = opStack.peek();
                // 遇到反括号则将运算符栈中的元素移除到后缀式栈中直到遇到左括号
                if (currentOp == opRight) {
                    while (opStack.peek() != opLeft) {
                        postfixStack.push(String.valueOf(opStack.pop()));
                    }
                    opStack.pop();
                } else {
                    while (currentOp != opLeft && peekOp != opComma && compare(currentOp, peekOp)) {
                        postfixStack.push(String.valueOf(opStack.pop()));
                        peekOp = opStack.peek();
                    }
                    opStack.push(currentOp);
                }
                count = 0;
                currentIndex = i + 1;
            } else {
                count++;
            }
        }
        // 最后一个字符不是括号或者其他运算符的则加入后缀式栈中
        if (count > 1 || (count == 1 && !isOperator(arr[currentIndex]))) {
            postfixStack.push(new String(arr, currentIndex, count));
        }
        while (opStack.peek() != opComma) {
            // 将操作符栈中的剩余的元素添加到后缀式栈中
            postfixStack.push(String.valueOf(opStack.pop()));
        }
    }

    /**
     * 判断是否为算术符号
     *
     * @param c 字符
     * @return 是否为算术符号
     */
    private boolean isOperator(final char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' || c == '%';
    }

    /**
     * 利用ASCII码-40做下标去算术符号优先级
     *
     * @param cur  下标
     * @param peek peek
     * @return 优先级，如果cur高或相等，返回true，否则false
     */
    private boolean compare(char cur, char peek) {
        final char rate = '%';
        // 如果是peek优先级高于cur，返回true，默认都是peek优先级要低
        final int offset = 40;
        if (cur == rate) {
            // %优先级最高
            cur = 47;
        }
        if (peek == rate) {
            // %优先级最高
            peek = 47;
        }
        return operatPriority[peek - offset] >= operatPriority[cur - offset];
    }

    /**
     * 按照给定的算术运算符做计算
     *
     * @param firstValue  第一个值
     * @param secondValue 第二个值
     * @param currentOp   算数符，只支持'+'、'-'、'*'、'/'、'%'
     * @return 结果
     */
    private BigDecimal calculate(@Nonnull final String firstValue, @Nonnull final String secondValue, final char currentOp) {
        final BigDecimal first = new BigDecimal(firstValue), second = new BigDecimal(secondValue);
        BigDecimal result;
        switch (currentOp) {
            case '+':
                result = first.add(second);
                break;
            case '-':
                result = first.subtract(second);
                break;
            case '*':
                result = first.multiply(second);
                break;
            case '/':
                try {
                    result = first.divide(second, RoundingMode.HALF_UP);
                } catch (ArithmeticException ex) {
                    result = BigDecimal.ZERO;
                }
                break;
            case '%':
                result = first.remainder(second);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + currentOp);
        }
        return result;
    }

    /**
     * 将表达式中负数的符号更改
     *
     * @param expression 例如-2+-1*(-3E-2)-(-1) 被转为 ~2+~1*(~3E~2)-(~1)
     * @return 转换后的字符串
     */
    private static String transform(@Nonnull final String expression) {
        final String source = expression.endsWith("=") ? expression.substring(0, expression.length() - 1) : expression;
        final char[] arr = source.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '-') {
                if (i == 0) {
                    arr[i] = '~';
                } else {
                    char c = arr[i - 1];
                    if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == 'E' || c == 'e') {
                        arr[i] = '~';
                    }
                }
            }
        }
        if (arr.length > 1 && arr[0] == '~' && arr[1] == '(') {
            arr[0] = '-';
            return "0" + new String(arr);
        } else {
            return new String(arr);
        }
    }
}
