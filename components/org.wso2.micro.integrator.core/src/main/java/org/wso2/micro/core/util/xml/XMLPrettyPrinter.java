/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.micro.core.util.xml;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.util.StringUtils;
/*
 * 
 */

public class XMLPrettyPrinter {

    private InputStream in;
    private boolean xmlFormat;
    private boolean numericEnc;
    private boolean done = false;
    private String encoding = "UTF-8";

    private static Log log = LogFactory.getLog(org.wso2.micro.core.util.xml.XMLPrettyPrinter.class);

    public XMLPrettyPrinter(InputStream in, boolean format, boolean numeric, String encoding) {
        this.in = in;
        xmlFormat = format;
        numericEnc = numeric;
        if (encoding != null) {
            this.encoding = encoding;
        }
    }

    public XMLPrettyPrinter(InputStream in) {
        this(in, true, false, null);
    }

    public XMLPrettyPrinter(InputStream in, String encoding) {
        this(in, true, false, encoding);
    }

    public String xmlFormat() {
        StringBuffer out = new StringBuffer();
        try {
            byte[] buffer = new byte[524288];
            byte[] tmpbuffer = new byte[1048576];
            String message = null;
            int saved = 0;
            int len;
            int i1, i2;
            int i;
            int tabWidth = 3;
            boolean atMargin = true;
            int thisIndent = -1,
                    nextIndent = -1,
                    previousIndent = -1;

            //if ( inSocket  != null ) inSocket.setSoTimeout( 10 );
            //if ( outSocket != null ) outSocket.setSoTimeout( 10 );


            a:
            for (; ;) {
                if (done) {
                    break;
                }
                //try{
                //len = in.available();
                //}catch(Exception e){len=0;}
                len = buffer.length;
                // Used to be 1, but if we block it doesn't matter
                // however 1 will break with some servers, including apache
                if (len == 0) {
                    len = buffer.length;
                }
                if (saved + len > buffer.length) {
                    len = buffer.length - saved;
                }
                int len1 = 0;

                while (len1 == 0) {
                    try {
                        len1 = in.read(buffer, saved, len);
                    }
                    catch (Exception ex) {
                        if (done && saved == 0) {
                            break a;
                        }
                        len1 = -1;
                        break;
                    }
                }
                len = len1;

                if (len == -1 && saved == 0) {
                    break;
                }
                if (len == -1) {
                    done = true;
                }

                if (xmlFormat) {
                    // Do XML Formatting
                    boolean inXML = false;
                    int bufferLen = saved;

                    if (len != -1) {
                        bufferLen += len;
                    }
                    i1 = 0;
                    i2 = 0;
                    saved = 0;
                    for (; i1 < bufferLen; i1++) {
                        // Except when we're at EOF, saved last char
                        if (len != -1 && i1 + 1 == bufferLen) {
                            saved = 1;
                            break;
                        }
                        thisIndent = -1;
                        if (buffer[i1] == '<' && buffer[i1 + 1] != '/') {
                            previousIndent = nextIndent++;
                            thisIndent = nextIndent;
                            inXML = true;
                        }
                        if (buffer[i1] == '<' && buffer[i1 + 1] == '/') {
                            if (previousIndent > nextIndent) {
                                thisIndent = nextIndent;
                            }
                            previousIndent = nextIndent--;
                            inXML = true;
                        }
                        if (buffer[i1] == '/' && buffer[i1 + 1] == '>') {
                            previousIndent = nextIndent--;
                            inXML = true;
                        }
                        if (thisIndent != -1) {
                            if (thisIndent >= 0) {
                                tmpbuffer[i2++] = (byte) '\n';
                            }
                            for (i = tabWidth * thisIndent; i > 0; i--) {
                                tmpbuffer[i2++] = (byte) ' ';
                            }
                        }
                        atMargin = (buffer[i1] == '\n' || buffer[i1] == '\r');

                        if (!inXML || !atMargin) {
                            tmpbuffer[i2++] = buffer[i1];
                        }
                    }
                    message = new String(tmpbuffer, 0, i2, encoding);
                    if (numericEnc) {
                        out.append(StringUtils.escapeNumericChar(message));
                    } else {
                        out.append(StringUtils.unescapeNumericChar(message));
                    }

                    // Shift saved bytes to the beginning
                    for (i = 0; i < saved; i++) {
                        buffer[i] = buffer[bufferLen - saved + i];
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            log.error("XML Pretty Printer needs a higher buffer size to handle this process definition. ", ex);
        } catch (Exception e) {
            log.error("XML Pretty Printer failed. ", e);
        }
        return out.toString();
    }

}
