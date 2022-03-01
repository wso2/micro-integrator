/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.esb.integration.common.utils.servers;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * SSHD based SFTP server implementation for EI testing.
 */
public class SftpServerRunner {

    private final SftpServer sftpServer;

    public SftpServerRunner(int port, String ftpFolderPath, String userName, String password) {
        this.sftpServer = new SftpServer(port, ftpFolderPath, userName, password);
    }

    public SftpServerRunner(SshServer sshServer) {
        this.sftpServer = new SftpServer(sshServer);
    }

    public void start() {
        Thread thread = new Thread(sftpServer);
        thread.start();
    }

    public void stop(){
        sftpServer.stop();
    }

    private class SftpServer implements Runnable {

        private final Log LOGGER = LogFactory.getLog(SftpServer.class);
        private SshServer sshd = SshServer.setUpDefaultServer();

        SftpServer(int port, String path, String ftpUser, String ftpPassword) {

            sshd.setPort(port);
            sshd.setSubsystemFactories(
                    Arrays.<NamedFactory<Command>>asList(new SftpSubsystemFactory()));
            sshd.setCommandFactory(new ScpCommandFactory());
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
            sshd.setFileSystemFactory(new VirtualFileSystemFactory(Paths.get(path)));
            sshd.setPasswordAuthenticator((username, password, session) -> StringUtils.equals(username, ftpUser)
                                                                           && StringUtils.equals(password,
                                                                                                 ftpPassword));
        }

        SftpServer(SshServer sshServer) {
            this.sshd = sshServer;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting SFTP server on port {}" + sshd.getPort());
                sshd.start();
            } catch (IOException e) {
                LOGGER.error("Error starting SFTP server", e);
            }
        }

        private void stop() {
            try {
                sshd.stop();
            } catch (IOException e) {
                LOGGER.error("Error stopping SFTP server", e);
            }
        }
    }
}
