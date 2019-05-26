/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jkiss.dbeaver.model.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Point;

import java.util.Locale;

/**
 * Text utils
 */
public class TextUtils {

    public static boolean isEmptyLine(IDocument document, int line)
            throws BadLocationException {
        IRegion region = document.getLineInformation(line);
        if (region == null || region.getLength() == 0) {
            return true;
        }
        String str = document.get(region.getOffset(), region.getLength());
        return str.trim().length() == 0;
    }

    public static int getOffsetOf(IDocument document, int line, String pattern)
        throws BadLocationException {
        IRegion region = document.getLineInformation(line);
        if (region == null || region.getLength() == 0) {
            return -1;
        }
        String str = document.get(region.getOffset(), region.getLength());
        return str.indexOf(pattern);
    }

    /**
     * Gets text size.
     * x: maximum line length
     * y: number of lines
     * @param text    source text
     * @return size
     */
    public static Point getTextSize(String text) {
        int length = text.length();
        int maxLength = 0;
        int lineCount = 1;
        int lineLength = 0;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\n':
                    maxLength = Math.max(maxLength, lineLength);
                    lineCount++;
                    lineLength = 0;
                    break;
                case '\r':
                    break;
                case '\t':
                    lineLength += 4;
                    break;
                default:
                    lineLength++;
                    break;
            }
        }
        maxLength = Math.max(maxLength, lineLength);
        return new Point(maxLength, lineCount);
    }

    /**
     * Copied from Apache FuzzyScore  implementation.
     * One point is given for every matched character. Subsequent matches yield two bonus points. A higher score
     * indicates a higher similarity.
     */
    public static int fuzzyScore(CharSequence term, CharSequence query) {
        return fuzzyScore(term, query, Locale.getDefault());
    }

    public static int fuzzyScore(CharSequence term, CharSequence query, Locale locale) {
        if (term == null || query == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        // fuzzy logic is case insensitive. We normalize the Strings to lower
        // case right from the start. Turning characters to lower case
        // via Character.toLowerCase(char) is unfortunately insufficient
        // as it does not accept a locale.
        final String termLowerCase = term.toString().toLowerCase(locale);
        final String queryLowerCase = query.toString().toLowerCase(locale);

        // the resulting score
        int score = 0;

        // the position in the term which will be scanned next for potential
        // query character matches
        int termIndex = 0;

        // index of the previously matched character in the term
        int previousMatchingCharacterIndex = Integer.MIN_VALUE;
        int sequenceScore = 0;

        for (int queryIndex = 0; queryIndex < queryLowerCase.length(); queryIndex++) {
            final char queryChar = queryLowerCase.charAt(queryIndex);

            boolean termCharacterMatchFound = false;
            for (; termIndex < termLowerCase.length(); termIndex++) {
                final char termChar = termLowerCase.charAt(termIndex);

                if (queryChar == termChar) {
                    // simple character matches result in one point
                    score++;
                    if (termIndex == 0) {
                        // First character
                        score += 4;
                    } else if (!Character.isLetter(termLowerCase.charAt(termIndex - 1))) {
                        // Previous character was a divider
                        score += 2;
                    }

                    // subsequent character matches further improve
                    // the score.
                    if (previousMatchingCharacterIndex + 1 == termIndex) {
                        if (sequenceScore == 0) {
                            sequenceScore = 4;
                        } else {
                            sequenceScore *= 2;
                        }
                        score += sequenceScore;
                    } else {
                        sequenceScore = 0;
                    }

                    previousMatchingCharacterIndex = termIndex;
                    termIndex++;

                    // we can leave the nested loop. Every character in the
                    // query can match at most one character in the term.
                    termCharacterMatchFound = true;
                    break;
                }
            }
            if (!termCharacterMatchFound) {
                return 0;
            }
        }

        return score;
    }

}
