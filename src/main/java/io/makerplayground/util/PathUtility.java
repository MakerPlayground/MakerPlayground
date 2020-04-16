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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PathUtility {
    // workspace directory for storing generated project folder
    static public final String MP_WORKSPACE = System.getProperty("user.home") + File.separator + ".makerplayground";
    // program installation directory
    static public final String MP_INSTALLDIR = new File("").getAbsoluteFile().getPath();

    /**
     * Get command for executing platformio
     * @return command for executing platformio on the current platform or Optional.empty()
     */
    public static Optional<List<String>> getPlatformIOCommand() {
        List<List<String>> command = List.of(
                List.of(MP_INSTALLDIR + File.separator + "dependencies" + File.separator + "python-3.7.7" + File.separator + "python", "-m", "platformio"),     // integrated python of our windows installer
                List.of(System.getProperty("user.home") + "/.platformio/penv/bin/platformio"),   // virtualenv created by official platformio installation script
                List.of("python", "-m", "platformio"),          // default python in user's system path
                List.of("/usr/bin/python", "-m", "platformio")  // internal python of macOS and Linux
        );

        for (List<String> c : command) {
            try {
                Process p = new ProcessBuilder(c).redirectErrorStream(true).start();
                // read from an input stream to prevent the child process from stalling
                try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String readLine;
                    while ((readLine = processOutputReader.readLine()) != null) {
//                        System.out.println(readLine);
                    }
                }
                if (p.waitFor(5, TimeUnit.SECONDS) && (p.exitValue() == 0)) {
                    return Optional.of(c);
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
        List<String> path = List.of(MP_INSTALLDIR + File.separator + "dependencies" + File.separator + "platformio"   // default path for Windows installer
                , "/Library/Application Support/MakerPlayground/platformio");       // default path for macOS installer
        return path.stream().filter(s -> new File(s).exists()).findFirst();
    }
}
