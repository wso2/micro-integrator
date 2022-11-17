/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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

package org.wso2.micro.integrator.http.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class provides the base implementation for a Socket Server.
 */
public abstract class BackendServer extends Thread {

    protected static final Log log = LogFactory.getLog(BackendServer.class);
    protected String payload;
    private final ServerSocket serverSocket;

    protected void readInput(Socket socket) throws Exception {

        boolean isGETRequest = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("GET")) {
                isGETRequest = true;
            }
            if (line.trim().isEmpty() && isGETRequest) {
                break;
            }
            if (line.trim().equals("0")) {
                break;
            }
        }
    }

    protected abstract void writeOutput(Socket socket) throws Exception;

    public void setPayload(String payload) {

        this.payload = payload;
    }

    public void shutdown() {

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                log.info("Shutting down Backend Server...");
                serverSocket.close();
            } catch (IOException e) {
                //NO need to handle
            }
        }
    }

    public BackendServer(ServerSocket serverSocket) {

        this.serverSocket = serverSocket;
    }

    public void run() {

        try {
            while (serverSocket != null && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            log.error("Error running the Backend Server: " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {

        private final Socket clientSocket;

        public ClientHandler(Socket socket) {

            this.clientSocket = socket;
        }

        @Override
        public void run() {

            try {
                readInput(clientSocket);
                writeOutput(clientSocket);
            } catch (Exception e) {
                log.error("Error handling client instance in the Backend Server: " + e.getMessage());
            }
        }
    }
}
