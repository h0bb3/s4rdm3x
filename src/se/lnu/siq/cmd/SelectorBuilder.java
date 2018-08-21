package se.lnu.siq.asm_gs_test.cmd;

import java.util.ArrayList;

/**
 * Created by tohto on 2017-08-24.
 */
public class SelectorBuilder {

    public enum TokenType {
        UNASS,
        LP,
        RP,
        BIN_OP,
        UN_OP,
        STATEMENT,
    }

    private class Token {


        public String m_str = "";
        public TokenType m_type = TokenType.UNASS;

        public void setType() {
            if (strIs("(")) {
                m_type = TokenType.LP;
            } else if (strIs(")")) {
                m_type = TokenType.RP;
            } else if (strIs("AND") || strIs("OR")) {
                m_type = TokenType.BIN_OP;
            } else if (strIs("NOT")) {
                m_type = TokenType.UN_OP;
            } else {
                m_type = TokenType.STATEMENT;
            }
        }

        private boolean strIs(String a_str) {
            return m_str.compareTo(a_str) == 0;
        }


    }

    private int m_currentTokenIx;

    public Selector.ISelector buildFromString(String a_expression) {

        Tokenizer tokenizer = new Tokenizer();
        ArrayList<Token>tokens = tokenizer.tokenize(a_expression);

        m_currentTokenIx = 0;
        /*Selector.ISelector s = handleToken(tokens);
        while (m_currentTokenIx < tokens.size() && s != null) {
            s = handleToken(s, tokens);
        }
        return s;*/
        return handleTokens(tokens);

    }

    private Selector.ISelector handleTokens(ArrayList<Token> a_tokens) {
        Selector.ISelector left = handleToken(a_tokens);

        while (m_currentTokenIx < a_tokens.size() && left != null) {
            Selector.ISelector s = handleToken(left, a_tokens);
            if (s == left) {
                // this happens when we reach a ) i.e. no more expressions should be
                // added to the current selector.
                return left;
            } else {
                left = s;
            }
        }
        return left;
    }

    private Selector.ISelector handleToken(Selector.ISelector a_left, ArrayList<Token> a_tokens) {
        Token t = a_tokens.get(m_currentTokenIx);

        m_currentTokenIx++;
        switch (t.m_type) {
            case BIN_OP:
                if (t.m_str.compareTo("AND") == 0) {
                    return new Selector.And(a_left, handleToken(a_tokens));
                } else if (t.m_str.compareTo("OR") == 0) {
                    return new Selector.Or(a_left, handleToken(a_tokens));
                }
                break;
            case RP:
                return a_left;

        }

        System.out.println("Syntax Error: " + t.m_type + ":" + t.m_str);
        return null;
    }

    private Selector.ISelector handleToken(ArrayList<Token> a_tokens) {
        Token t = a_tokens.get(m_currentTokenIx);

        m_currentTokenIx++;
        switch (t.m_type) {
            case LP:
                return handleTokens(a_tokens);
            case STATEMENT:
                String strs[] = t.m_str.split(":");
                if (strs.length == 2) {
                    if (strs[0].compareTo("tag") == 0) {
                        return new Selector.Tag(strs[1]);
                    } else if (strs[0].compareTo("pkg") == 0) {
                        return new Selector.Pkg(strs[1]);
                    } else if (strs[0].compareTo("edgTo") == 0) {
                        return new Selector.EdgTo(strs[1]);
                    }
                }
            case UN_OP:
                return new Selector.Not(handleToken(a_tokens));
        }
        System.out.println("Syntax Error: " + t.m_type + ":" + t.m_str);
        return null;
    }

    private class Tokenizer {
       ArrayList<Token> tokenize(String a_expression) {
           ArrayList<Token> tokens = new ArrayList<>();
           Token t = new Token();

           for (char c : a_expression.toCharArray()) {
               switch (c) {
                   case ')':
                   case '(':
                       if (t.m_str.length() > 0) {
                           t.setType();
                           tokens.add(t);
                           t = new Token();
                       }
                       t.m_str = "" + c;
                       t.setType();
                       tokens.add(t);
                       t = new Token();

                       break;
                   case ' ':
                       if (t.m_str.length() > 0) {
                           t.setType();
                           tokens.add(t);
                           t = new Token();
                       }
                       break;
                   default:
                       t.m_str += c;
               }
           }

           if (t.m_str.length() > 0) {
               t.setType();
               tokens.add(t);
           }

           return tokens;
       }
    }
}
