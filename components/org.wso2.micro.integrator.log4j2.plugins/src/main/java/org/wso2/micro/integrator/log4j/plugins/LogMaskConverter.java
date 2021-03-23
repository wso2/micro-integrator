/*
 * Copyright 2020 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.log4j.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(name = "LogMaskConverter", category = "Converter")
@ConverterKeys({"mm"})
/**
 * Log Masking converter to mask logs.
 * Converter key mm used to change existing message pattern in log4j2.properties
 */
public class LogMaskConverter extends LogEventPatternConverter {

    private static final Log log = LogFactory.getLog(LogMaskConverter.class);

    private static final LogMaskConverter INSTANCE = new LogMaskConverter();
    private static final String DEFAULT_MASKING_PATTERNS_FILE_NAME = "wso2-log-masking.properties";
    private static final String MASK_PATTERN = "mm";
    private static final String REPLACE_PATTERN = ".replace_pattern";
    private static final String REPLACER = ".replacer";

    private List<LogMaskInfoProvider> logMaskInfoProvider;
    private boolean isMaskAvailable = false;

    public static LogMaskConverter newInstance(String[] options) {
        return INSTANCE;
    }

    protected LogMaskConverter() {

        super(MASK_PATTERN, MASK_PATTERN);
        logMaskInfoProvider = new ArrayList<>();
        loadMaskingPatterns();
    }

    @Override
    public void format(LogEvent logEvent, StringBuilder stringBuilder) {

        String message = logEvent.getMessage().getFormat();

        // Check whether there are any masking patterns defined.
        if (this.isMaskAvailable) {
            Matcher matcher;
            Matcher replaceMatcher;

            for (LogMaskInfoProvider maskingInfo : logMaskInfoProvider) {
                Pattern pattern = maskingInfo.logMaskingPattern;
                matcher = pattern.matcher(message);
                StringBuffer stringBuffer = new StringBuffer();
                Pattern replacementPattern = maskingInfo.logReplacementPattern;
                while (matcher.find()) {
                    if (Objects.isNull(replacementPattern)) {
                        matcher.appendReplacement(stringBuffer, maskingInfo.logReplacementString);
                    } else {
                        String subStringToMask = message.substring(matcher.start(), matcher.end());
                        replaceMatcher = replacementPattern.matcher(subStringToMask);
                        subStringToMask = replaceMatcher.replaceAll(maskingInfo.logReplacementString);
                        matcher.appendReplacement(stringBuffer, subStringToMask);
                    }
                }
                matcher.appendTail(stringBuffer);
                message = stringBuffer.toString();
            }
        }
        stringBuilder.append(message);
    }

    /**
     * Method to get the masking patterns (regex) from the properties file.
     */
    private void loadMaskingPatterns() {

        String defaultFile = MicroIntegratorBaseUtils.getCarbonConfigDirPath() + File.separatorChar
                + DEFAULT_MASKING_PATTERNS_FILE_NAME;
        Properties properties = new Properties();
        InputStream propsStream = null;
        try {
            // If the masking file is not configured, load the configs from the default file.
            if (Files.exists(Paths.get(defaultFile))) {
                propsStream = new FileInputStream(defaultFile);
                properties.load(propsStream);

                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    if (((String) entry.getKey()).endsWith(REPLACE_PATTERN) || ((String) entry.getKey()).endsWith(
                            REPLACER)) {
                        continue;
                    }
                    String pattern = (String) entry.getValue();
                    String replacePattern = properties.getProperty(entry.getKey() + REPLACE_PATTERN);
                    String replacerPattern = properties.getProperty(entry.getKey() + REPLACER);
                    logMaskInfoProvider.add(new LogMaskInfoProvider(pattern, replacePattern, replacerPattern));
                    this.isMaskAvailable = true;
                }
            }
        } catch (IOException e) {
            // If the masking patterns cannot be loaded print an error message.
            log.error("Error loading the masking patterns, due to : " + e.getMessage(), e);
        } finally {
            if (propsStream != null) {
                try {
                    propsStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * This class represents the masking information.
     */
    public class LogMaskInfoProvider {

        private Pattern logMaskingPattern;
        private Pattern logReplacementPattern;
        private String logReplacementString = "*****";

        LogMaskInfoProvider(String logMaskingPattern, String logReplacementPattern, String logReplacementString) {
            this.logMaskingPattern = Pattern.compile(logMaskingPattern);
            if (Objects.nonNull(logReplacementPattern)) {
                this.logReplacementPattern = Pattern.compile(logReplacementPattern);
            }
            if (Objects.nonNull(logReplacementString)) {
                this.logReplacementString = logReplacementString;
            }
        }
    }
}
