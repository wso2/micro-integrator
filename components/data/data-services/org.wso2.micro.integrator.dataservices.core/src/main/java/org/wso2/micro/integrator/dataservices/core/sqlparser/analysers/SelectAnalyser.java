/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.dataservices.core.sqlparser.analysers;

import org.wso2.micro.integrator.dataservices.core.sqlparser.LexicalConstants;
import org.wso2.micro.integrator.dataservices.core.sqlparser.SQLParserUtil;

import java.util.Queue;

public class SelectAnalyser extends KeyWordAnalyzer {

    private Queue<String> tempQueue;

    public SelectAnalyser(Queue<String> tempQueue) {
        this.tempQueue = tempQueue;
    }

    /**
     * This method analyses a queue of string tokens associated with the sql keyword SELECT, which
     * is produced by the Lexical Analyser and embeds the proper syntaxes correspond to each of the
     * tokens in the input token queue recursively.
     */
    public void analyseStatement() {
        StringBuilder sb;
        if (SQLParserUtil.isAggregateFunction(tempQueue.peek()) ||
                SQLParserUtil.isStringFunction(tempQueue.peek())) {
            if (SQLParserUtil.isAggregateFunction(tempQueue.peek())) {

                String aggFunction = tempQueue.poll();

                /* handling aggregated functions */
                syntaxQueue.add(LexicalConstants.AGGREGATEFUNC);
                syntaxQueue.add(aggFunction);

                if (!tempQueue.isEmpty()) {
                    analyseStatement();
                }

            } else {
                String strFunction = tempQueue.poll();

                /* handling string functions */
                syntaxQueue.add(LexicalConstants.STRINGFUNC);
                syntaxQueue.add(strFunction);

                if (!tempQueue.isEmpty()) {
                    analyseStatement();
                }
            }
            /* handling left bracket */
        } else if (tempQueue.peek().equals(LexicalConstants.LEFT_BRACKET)) {
            tempQueue.poll();
            syntaxQueue.add(LexicalConstants.START_OF_LBRACKET);
            if (!tempQueue.isEmpty()) {
                analyseStatement();
            }
            /* handling right bracket */
        } else if (tempQueue.peek().equals(LexicalConstants.RIGHT_BRACKET)) {
            tempQueue.poll();
            syntaxQueue.add(LexicalConstants.START_OF_RBRACKET);
            if (!tempQueue.isEmpty()) {
                analyseStatement();
            }
            /* handling single quotations */
        } else if (tempQueue.peek().equals(LexicalConstants.SINGLE_QUOTATION)) {
            sb = new StringBuilder();
            tempQueue.poll();
            while (!tempQueue.peek().equals(LexicalConstants.SINGLE_QUOTATION)) {
                sb.append(tempQueue.poll());
            }
            syntaxQueue.add(LexicalConstants.OPVALUE);
            syntaxQueue.add(sb.toString());
            tempQueue.poll();

            if (!tempQueue.isEmpty()) {
                analyseStatement();
            }
            /* handling comma seperated values */
        } else if (tempQueue.peek().equals(LexicalConstants.COMMA)) {
            tempQueue.poll();
            if (!tempQueue.isEmpty()) {
                analyseStatement();
            }

            /* handling AS keyword */
        } else if (tempQueue.peek().equalsIgnoreCase(LexicalConstants.AS)) {
            syntaxQueue.clear();
            tempQueue.poll();
            if (!SQLParserUtil.getDelimiters().contains(tempQueue.peek()) ||
                    !SQLParserUtil.getKeyWords().contains(tempQueue.peek()) ||
                    !SQLParserUtil.getOperators().contains(tempQueue.peek())) {
                sb = new StringBuilder();
                while (!tempQueue.isEmpty() && !tempQueue.peek().equals(LexicalConstants.COMMA)) {
                    sb.append(tempQueue.poll());
                }
                syntaxQueue.add(LexicalConstants.COLUMN);
                syntaxQueue.add(sb.toString());

                if (!tempQueue.isEmpty()) {
                    analyseStatement();
                }
            }
        } else {
            String strRef = tempQueue.poll();
            if (!tempQueue.isEmpty()) {
                if (tempQueue.peek().equals(LexicalConstants.DOT)) {

                    syntaxQueue.add(LexicalConstants.TABLE);
                    syntaxQueue.add(strRef);
                    tempQueue.poll();

                    if (!SQLParserUtil.getDelimiters().contains(tempQueue.peek()) &&
                            !SQLParserUtil.getKeyWords().contains(tempQueue.peek()) &&
                            !SQLParserUtil.getOperators().contains(tempQueue.peek())) {

                        String columnRef = tempQueue.poll();
                        syntaxQueue.add(LexicalConstants.COLUMN);
                        syntaxQueue.add(columnRef);

                        if (!tempQueue.isEmpty()) {
                            if (tempQueue.peek().equals(LexicalConstants.COMMA)) {
                                tempQueue.poll();
                                analyseStatement();
                            } else if (tempQueue.peek().equals(LexicalConstants.RIGHT_BRACKET)) {
                                if (!tempQueue.isEmpty()) {
                                    analyseStatement();
                                }
                            }
                        }
                    } else {
                        System.out.println("Error-not string!");
                    }
                } else if (tempQueue.peek().equals(LexicalConstants.COMMA)) {

                    syntaxQueue.add(LexicalConstants.COLUMN);
                    syntaxQueue.add(strRef);
                    tempQueue.poll();
                    analyseStatement();
                } else if (tempQueue.peek().equals(LexicalConstants.AS)) {

                    syntaxQueue.add(LexicalConstants.COLUMN);
                    syntaxQueue.add(strRef);
                    tempQueue.poll();
                    syntaxQueue.add(LexicalConstants.ASCOLUMN);
                    syntaxQueue.add(tempQueue.poll());
                    if (!tempQueue.isEmpty()) {
                        analyseStatement();
                    }

                } else {
                    syntaxQueue.add(LexicalConstants.COLUMN);
                    syntaxQueue.add(strRef);
                    if (!tempQueue.isEmpty()) {
                        analyseStatement();
                    }
                }
            } else {
                syntaxQueue.add(LexicalConstants.COLUMN);
                syntaxQueue.add(strRef);
            }
        }
    }

}
