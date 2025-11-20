package org.jarApiAutomation.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil {

    public static void executeShellCmd(String shellCmd) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", shellCmd});
            process.waitFor();
        } catch (Exception e) {
            log.error("Error executing command: {}", shellCmd, e);
        }
    }
}