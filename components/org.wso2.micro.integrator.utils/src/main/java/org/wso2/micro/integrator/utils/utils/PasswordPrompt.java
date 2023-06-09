/*
 *  Copyright (c) 2023, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.micro.integrator.utils.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A script for hiding the password input
 */
public class PasswordPrompt implements Runnable {
    private static final int PROMPT_CLEARANCE_LENGTH = 10;
    private volatile boolean done = false;
    private String prompt;
    private PrintWriter out;

    public PasswordPrompt(String prompt, PrintWriter out) {
        this.prompt = prompt;
        this.out = out;
    }

    public void run() {
        int priority = Thread.currentThread().getPriority();
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            String fullPrompt = "\r" + prompt + "          " + "\r" + prompt;
            StringBuffer clearline = new StringBuffer();
            clearline.append('\r');
            for (int i = prompt.length() + PROMPT_CLEARANCE_LENGTH; i >= 0; i--) {
                clearline.append(' ');
            }
            while (!done) {
                out.print(fullPrompt);
                out.flush();
                Thread.sleep(1);
            }
            out.print(clearline.toString());
            out.flush();
            out.println();
            out.flush();
        } catch (InterruptedException e) {
        } finally {
            Thread.currentThread().setPriority(priority);
        }
        prompt = null;
        out = null;
    }

    public String getPassword(BufferedReader in) throws IOException {
        Thread t = new Thread(this, "Password hiding thread");
        t.start();
        String password = in.readLine();
        done = true;
        try {
            t.join();
        } catch (InterruptedException e) {
        }
        return password;
    }
}