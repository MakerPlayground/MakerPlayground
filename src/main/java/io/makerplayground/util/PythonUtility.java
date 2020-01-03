/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PythonUtility {
    // workspace directory for storing generated project folder
    public static final String MP_WORKSPACE;

    static {
        if (OSInfo.getOs() == OSInfo.OS.WINDOWS) {
            MP_WORKSPACE = System.getenv("ProgramData") + File.separator + "Maker Playground";
        } else if (OSInfo.getOs() == OSInfo.OS.MAC) {
            MP_WORKSPACE = "/Library/Application Support/MakerPlayground";
        } else {    // linux and other unix-liked
            MP_WORKSPACE = "/usr/share/makerplayground";
        }
    }

    /**
     * Get path to python with usable platformio installation
     * @return path to valid python installation or Optional.empty()
     */
    public static Optional<String> getPythonPath() {
        List<String> path = List.of(MP_WORKSPACE + File.separator + "python-2.7.13" + File.separator + "python"      // integrated python for windows version
                , "python"                  // python in user's system path
                , "/usr/bin/python");       // internal python of macOS and Linux which is used by platformio installation script

        for (String s : path) {
            try {
                Process p = new ProcessBuilder(s, "-m", "platformio").redirectErrorStream(true).start();
                // read from an input stream to prevent the child process from stalling
                try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String readLine;
                    while ((readLine = processOutputReader.readLine()) != null) {
//                        System.out.println(readLine);
                    }
                }
                if (p.waitFor(5, TimeUnit.SECONDS) && (p.exitValue() == 0)) {
                    return Optional.of(s);
                }
            } catch (IOException | InterruptedException e) {
                // do nothing as we expected the code to throw exception
            }
        }

        return Optional.empty();
    }

    /**
     * Get path to an integrated platformio home directory which is used for storing compilers and tools for each platform
     * @return path to the integrated platformio home directory or Optional.empty()
     */
    public static Optional<String> getIntegratedPIOHomeDirectory() {
        String path = MP_WORKSPACE + File.separator + "platformio";
        if (new File(path).exists()) {
            return Optional.of(path);
        } else {
            return Optional.empty();
        }
    }
}
