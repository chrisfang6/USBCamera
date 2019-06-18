package net.chris.usbcamera

import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


class ShellKit {
    companion object {
        /**
         * Run shell command starting with 'adb'
         *
         * @param cmd command list to execute
         * @return  Any Output
         */
        fun adb(cmd: MutableList<String>): String {
            cmd.add(0, "adb")
            return command(cmd)
        }

        /**
         * Run shell command starting with 'adb'
         *
         * @param cmd command to execute ,tokenize with whitespace
         * @return  Any Output
         */
        fun adb(cmd: String): String {
            val splits = cmd.split(" ".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            return adb(splits)
        }

        /**
         * Run shell command starting with 'adb'
         *
         * @param cmd command string array to execute
         * @return  Any Output
         */
        fun adb(cmd: Array<String>): String {
            val cmds = emptyList<String>().toMutableList()
            cmds.add("adb")
            for (part in cmd) {
                cmds.add(part)
            }
            return command(cmds)
        }

        /**
         * Run shell command starting with 'adb shell'
         *
         * @param cmd command list to execute
         * @return running Any Output
         */
        fun adbShell(cmd: MutableList<String>): String {
            cmd.add(0, "shell")
            cmd.add(0, "adb")
            return command(cmd)
        }

        /**
         * Run shell command starting with 'adb shell'
         *
         * @param cmd command to execute ,tokenize with whitespace
         * @return  Any Output
         */
        fun adbShell(cmd: String): String {
            val splits = cmd.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return adbShell(splits)
        }

        /**
         * Run shell command starting with 'adb shell'
         *
         * @param cmd command string array to execute
         * @return  Any Output
         */
        fun adbShell(cmd: Array<String>): String {
            val cmds = emptyList<String>().toMutableList()
            cmds.add("adb")
            cmds.add("shell")
            for (part in cmd) {
                cmds.add(part)
            }
            return command(cmds)
        }

        /**
         * Execute a shell command and return its output
         *
         * @param command command to execute
         * @return process executed
         */
        fun command(command: List<String>): String {
            //set redirectErrorStream to be true to cross output streams
            val pb = ProcessBuilder(command).redirectErrorStream(true)
            var output = ""
            try {
                val process = pb.start()
                val outputHandler = IOThreadHandler(
                    process.inputStream
                )
                outputHandler.start()
                //wait for the process to stop
                process.waitFor()
                //in case the process stopped before the thread
                outputHandler.join()
                output = outputHandler.getOutput()
            } catch (e: Exception) {
                Timber.e(e)
            }

            return output
        }

        /**
         * Thread to drain the output of cmd running
         */
        private class IOThreadHandler internal constructor(private val inputStream: InputStream) : Thread() {
            private val output = StringBuilder()
            override fun run() {
                Scanner(InputStreamReader(inputStream)).use { br ->
                    var line: String?
                    while (br.hasNextLine()) {
                        line = br.nextLine()
                        output.append(line).append(System.getProperty("line.separator"))
                    }
                }
            }

            fun getOutput(): String {
                return output.toString()
            }
        }

        fun main(args: Array<String>) {
            val cmds = emptyList<String>().toMutableList()
            cmds.add("devices")
            println(adb(cmds))
            cmds.clear()
            cmds.add("getprop")
            cmds.add("ro.boot.serialno")
            println(adbShell(cmds))
        }
    }

}