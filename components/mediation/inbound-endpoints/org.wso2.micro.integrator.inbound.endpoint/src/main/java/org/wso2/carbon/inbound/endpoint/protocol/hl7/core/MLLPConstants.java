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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.core;

import java.nio.charset.Charset;

public class MLLPConstants {

    public static final byte[] CR = { 0x0D };
    public static final byte[] HL7_TRAILER = { 0x1C, CR[0] };
    public static final byte[] HL7_HEADER = { 0x0B };

    // default charset
    public final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public final static String MLLP_CONTEXT = "HL7_MLLP_CONTEXT";

    public final static String PARAM_HL7_PORT = "inbound.hl7.Port";

    public final static String PARAM_HL7_AUTO_ACK = "inbound.hl7.AutoAck";

    public final static String PARAM_HL7_TIMEOUT = "inbound.hl7.TimeOut";

    public final static int DEFAULT_HL7_TIMEOUT = 10000;

    public final static String PARAM_HL7_PRE_PROC = "inbound.hl7.MessagePreProcessor";

    public final static String HL7_PRE_PROC_PARSER_CLASS = "HL7_PRE_PROC_PARSER_CLASS";

    public final static String INBOUND_PARAMS = "HL7_INBOUND_PARAMS";

    public final static String INBOUND_HL7_BUFFER_FACTORY = "INBOUND_HL7_BUFFER_FACTORY";

    public final static String PARAM_HL7_CHARSET = "inbound.hl7.CharSet";

    public final static String HL7_CHARSET_DECODER = "HL7_CHARSET_DECODER";

    public final static String PARAM_HL7_VALIDATE = "inbound.hl7.ValidateMessage";

    public final static String HL7_REQ_PROC = "HL7_REQ_PROCESSOR";

    public final static String PARAM_HL7_BUILD_RAW_MESSAGE = "inbound.hl7.BuildInvalidMessages";

    public final static String PARAM_HL7_PASS_THROUGH_INVALID_MESSAGES = "inbound.hl7.PassThroughInvalidMessages";

    public final static String HL7_ID_GENERATOR = "hl7_id_generator";

    public final static String HL7_INBOUND_MSG_ID = "HL7_INBOUND_MSG_ID";

    public final static String HL7_INBOUND_TENANT_DOMAIN = "HL7_INBOUND_TENANT_DOMAIN";

    public static class TCPConstants {

        public final static String IO_THREAD_COUNT = "io_thread_count";

        public final static String CONNECT_TIMEOUT = "connect_timeout";

        public final static String TCP_NO_DELAY = "tcp_no_delay";

        public final static String SO_KEEP_ALIVE = "so_keep_alive";

        public final static String SO_TIMEOUT = "so_timeout";

        public final static String SELECT_INTERVAL = "select_interval";

        public final static String SHUTDOWN_GRACE_PERIOD = "shutdown_grace_period";

        public final static String SO_RCVBUF = "so_rcvbuf";

        public final static String SO_SNDBUF = "so_sndbuf";

        public final static String WORKER_THREADS_CORE = "worker_threads_core";

        public final static int WORKER_THREADS_CORE_DEFAULT = 100;

    }
}
