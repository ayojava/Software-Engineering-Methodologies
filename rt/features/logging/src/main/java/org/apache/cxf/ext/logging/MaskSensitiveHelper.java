/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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
package org.apache.cxf.ext.logging;

import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.message.Message;

public class MaskSensitiveHelper {
    private static final String ELEMENT_NAME_TEMPLATE = "-ELEMENT_NAME-";
    private static final String MATCH_PATTERN_XML_TEMPLATE = "<-ELEMENT_NAME->(.*?)</-ELEMENT_NAME->";
    private static final String MATCH_PATTERN_JSON_TEMPLATE = "\"-ELEMENT_NAME-\"[ \\t]*:[ \\t]*\"(.*?)\"";
    private static final String REPLACEMENT_PATTERN_XML_TEMPLATE = "<-ELEMENT_NAME->XXX</-ELEMENT_NAME->";
    private static final String REPLACEMENT_PATTERN_JSON_TEMPLATE = "\"-ELEMENT_NAME-\": \"XXX\"";

    private static final String XML_CONTENT = "xml";
    private static final String HTML_CONTENT = "html";
    private static final String JSON_CONTENT = "json";

    private final Set<String> sensitiveElementNames = new HashSet<>();

    public void addSensitiveElementNames(final Set<String> inSensitiveElementNames) {
        this.sensitiveElementNames.addAll(inSensitiveElementNames);
    }

    public String maskSensitiveElements(
            final Message message,
            final String originalLogString) {
        if (sensitiveElementNames.isEmpty()
                || message == null
                || (originalLogString == null)) {
            return originalLogString;
        }
        final String contentType = (String) message.get(Message.CONTENT_TYPE);
        if (contentType.toLowerCase().contains(XML_CONTENT)
                || contentType.toLowerCase().contains(HTML_CONTENT)) {
            return applyMasks(originalLogString, MATCH_PATTERN_XML_TEMPLATE, REPLACEMENT_PATTERN_XML_TEMPLATE);
        } else if (contentType.toLowerCase().contains(JSON_CONTENT)) {
            return applyMasks(originalLogString, MATCH_PATTERN_JSON_TEMPLATE, REPLACEMENT_PATTERN_JSON_TEMPLATE);
        }
        return originalLogString;
    }

    private String applyMasks(String originalLogString, String matchElementPattern, String replacementElementPattern) {
        String resultString = originalLogString;
        for (final String sensitiveName : sensitiveElementNames) {
            final String matchPattern = matchElementPattern.replaceAll(ELEMENT_NAME_TEMPLATE, sensitiveName);
            final String replacement = replacementElementPattern.replaceAll(ELEMENT_NAME_TEMPLATE, sensitiveName);
            resultString = resultString.replaceAll(matchPattern, replacement);
        }
        return resultString;
    }
}