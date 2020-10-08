/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.mi.registry.migration.utils;

import java.util.List;
import java.util.Objects;

/**
 * Class is responsible for generate the test summary table.
 */
public final class DataTable {

    private static final String EMPTY = "(empty)";
    private static final String ANSI_COLORS = "\u001B\\[[;\\d]*m";
    private static final String TABLE_EDGES = "+-+-+";

    private final List<String[]> data;
    private final int columns;
    private final int[] columnWidths;
    private final int emptyWidth;

    /**
     * Constructor method of the class.
     *
     * @param data list of row data of the table
     */
    private DataTable(List<String[]> data) {
        this.data = data;

        columns = data.get(0).length;
        columnWidths = new int[columns];
        for (int row = 0; row < data.size(); row++) {
            String[] rowData = data.get(row);
            if (rowData.length != columns) {
                throw new IllegalArgumentException(
                        String.format("Row %s's %s columns != %s columns", row + 1, rowData.length, columns));
            }
            for (int column = 0; column < columns; column++) {
                for (String rowDataLine : rowData[column].split("\\n")) {
                    String rowDataWithoutColor = rowDataLine.replaceAll(ANSI_COLORS, "");
                    columnWidths[column] = Math.max(columnWidths[column], rowDataWithoutColor.length());
                }
            }
        }

        // Account for column dividers and their spacing.
        int newEmptyWidth = 3 * (columns - 1);
        for (int columnWidth : columnWidths) {
            newEmptyWidth += columnWidth;
        }
        this.emptyWidth = newEmptyWidth;

        // Make sure we're wide enough for the empty text.
        if (newEmptyWidth < EMPTY.length()) {
            columnWidths[columns - 1] += EMPTY.length() - newEmptyWidth;
        }
    }

    /**
     * Create a new table with row data.
     *
     * @param data list of row data of the table
     * @return full table as a string
     */
    public static String getTable(List<String[]> data) {
        // check, throw and assignment in a single standard call
        Objects.requireNonNull(data, "Summary data must not be null!");
        return new DataTable(data).toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (data.isEmpty()) {
            printDivider(builder, TABLE_EDGES);
            builder.append('|').append(pad(emptyWidth, EMPTY)).append("|\n");
            printDivider(builder, "+---+");
        } else {
            for (int row = 0; row < data.size(); row++) {
                printDivider(builder, row == 0 ? "+-+-+" : TABLE_EDGES);
                printData(builder, data.get(row));
            }
            printDivider(builder, TABLE_EDGES);
        }
        return builder.toString();
    }

    /**
     * Method of print divider of the table.
     *
     * @param out    string builder
     * @param format format type
     */
    private void printDivider(StringBuilder out, String format) {
        for (int column = 0; column < columns; column++) {
            out.append(column == 0 ? format.charAt(0) : format.charAt(2));
            out.append(pad(columnWidths[column], "").replace(' ', format.charAt(1)));
        }
        out.append(format.charAt(4)).append('\n');
    }

    /**
     * Method of print data of the table.
     *
     * @param out  string builder
     * @param data list of row data of the table
     */
    private void printData(StringBuilder out, String[] data) {
        for (int line = 0, lines = 1; line < lines; line++) {
            for (int column = 0; column < columns; column++) {
                out.append('|');
                String[] cellLines = data[column].split("\\n");
                lines = Math.max(lines, cellLines.length);
                String cellLine = line < cellLines.length ? cellLines[line] : "";
                out.append(pad(columnWidths[column], cellLine));
            }
            out.append("|\n");
        }
    }

    /**
     * Method of padding for the table.
     *
     * @param width padding width
     * @param data  data of the cell
     */
    private static String pad(int width, String data) {
        return String.format(" %1$-" + width + "s ", data);
    }
}
