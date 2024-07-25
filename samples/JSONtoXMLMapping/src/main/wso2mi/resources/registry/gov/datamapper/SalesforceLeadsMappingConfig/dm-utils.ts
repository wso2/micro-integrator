/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

// ########################### Arithmetic Operators ###########################

/**
 * Calculates the sum of multiple numbers.
 * @param numbers - The numbers to sum.
 * @returns The sum of the numbers.
 */
export function sum(number1: number, ...number2: number[]): number {
    return [number1, ...number2].reduce((acc, curr) => acc + curr, 0);
}

/**
 * Finds the maximum number from a list of numbers.
 * @param numbers - The numbers to find the maximum from.
 * @returns The maximum number.
 */
export function max(number1: number, ...number2: number[]): number {
    return Math.max(number1, ...number2);
}

/**
 * Finds the minimum number from a list of numbers.
 * @param numbers - The numbers to find the minimum from.
 * @returns The minimum number.
 */
export function min(number1: number, ...number2: number[]): number {
    return Math.min(number1, ...number2);
}

/**
 * Calculates the average of multiple numbers.
 * @param numbers - The numbers to average.
 * @returns The average of the numbers.
 */
export function average(number1: number, ...number2: number[]): number {
    const numbers=[number1, ...number2];
    return numbers.reduce((acc, curr) => acc + curr, 0) / numbers.length;
}

/**
 * Finds the ceiling of a number.
 * @param num - The number to find the ceiling of.
 * @returns The ceiling of the number.
 */
export function ceiling(num: number): number {
    return Math.ceil(num);
}

/**
 * Finds the floor of a number.
 * @param num - The number to find the floor of.
 * @returns The floor of the number.
 */
export function floor(num: number): number {
    return Math.floor(num);
}

/**
 * Rounds a number to the nearest integer.
 * @param num - The number to round.
 * @returns The rounded number.
 */
export function round(num: number): number {
    return Math.round(num);
}

// ########################### Type Conversion Operators ###########################

/**
 * Converts a string to a number.
 * @param str - The string to convert.
 * @returns The number.
 */
export function toNumber(str: string): number {
    return Number(str);
}

/**
 * Converts a string to a boolean.
 * @param str - The string to convert.
 * @returns The boolean value.
 */
export function toBoolean(str: string): boolean {
    return str.toLowerCase() === 'true';
}

/**
 * Converts a number to a string.
 * @param num - The number to convert.
 * @returns The string representation of the number.
 */
export function numberToString(num: number): string {
    return num.toString();
}

/**
 * Converts a boolean to a string.
 * @param bool - The boolean to convert.
 * @returns The string representation of the boolean.
 */
export function booleanToString(bool: boolean): string {
    return bool.toString();
}

// ########################### String Operators ###########################

/**
 * Concatenates two or more strings.
 * @param strings - The strings to concatenate.
 * @returns The concatenated string.
 */
export function concat(string1: string, ...string2: string[]): string {
    return [string1, ...string2].join('');
}

/**
 * Splits a string into an array of substrings based on a specified separator.
 * @param str - The string to split.
 * @param separator - The separator to use for splitting.
 * @returns An array of substrings.
 */
export function split(str: string, separator: string): string[] {
    return str.split(separator);
}

/**
 * Converts a string to uppercase.
 * @param str - The string to convert.
 * @returns The uppercase string.
 */
export function toUppercase(str: string): string {
    return str.toUpperCase();
}

/**
 * Converts a string to lowercase.
 * @param str - The string to convert.
 * @returns The lowercase string.
 */
export function toLowercase(str: string): string {
    return str.toLowerCase();
}

/**
 * Returns the length of a string.
 * @param str - The string to get the length of.
 * @returns The length of the string.
 */
export function stringLength(str: string): number {
    return str.length;
}

/**
 * Checks if a string starts with a specified prefix.
 * @param str - The string to check.
 * @param prefix - The prefix to check for.
 * @returns True if the string starts with the prefix, false otherwise.
 */
export function startsWith(str: string, prefix: string): boolean {
    return str.startsWith(prefix);
}

/**
 * Checks if a string ends with a specified suffix.
 * @param str - The string to check.
 * @param suffix - The suffix to check for.
 * @returns True if the string ends with the suffix, false otherwise.
 */
export function endsWith(str: string, suffix: string): boolean {
    return str.endsWith(suffix);
}


/**
 * Extracts a substring from a string based on the specified start and end indices.
 * @param str - The string to extract the substring from.
 * @param startIndex - The index to start extracting from.
 * @param endIndex - The index to end extracting at.
 * @returns The extracted substring.
 */
export function substring(str: string, startIndex: number, endIndex: number): string {
    return str.substring(startIndex, endIndex);
}

/**
 * Trims whitespace from both ends of a string.
 * @param str - The string to trim.
 * @returns The trimmed string.
 */
export function trim(str: string): string {
    return str.trim();
}

/**
 * Replaces the first occurrence of a target string with another string.
 * @param str - The original string.
 * @param target - The target string to replace.
 * @param replacement - The string to replace the target with.
 * @returns The string with the first occurrence of the target replaced by the replacement.
 */
export function replaceFirst(str: string, target: string, replacement: string): string {
    return str.replace(target, replacement);
}

/**
 * Checks if a string matches a specified regular expression.
 * @param str - The string to check.
 * @param regex - The regular expression to match against.
 * @returns True if the string matches the regular expression, false otherwise.
 */
export function match(str: string, regex: RegExp): boolean {
    return regex.test(str);
}
