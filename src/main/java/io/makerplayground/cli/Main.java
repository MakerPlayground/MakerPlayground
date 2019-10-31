package io.makerplayground.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "allio", mixinStandardHelpOptions = true, version = "0.5", description = "end-to-end platform for IoT")
public class Main implements Callable<Integer> {

    @Command(name = "init", mixinStandardHelpOptions = true, description = "Initialize a new project")
    private static class InitCommand implements Callable<Integer> {
        @Option(names = {"--directory", "-d"}, description = "Path to a directory to initialize new project")
        private String directory;

        @Override
        public Integer call() throws Exception {
            System.out.print("init ");
            System.out.println(directory);
            return 0;
        }
    }

    @Command(name = "device", mixinStandardHelpOptions = true, description = "Manage devices in the project",
            subcommands = {DeviceAddCommand.class, DeviceAssignCommand.class, DeviceRemoveCommand.class})
    private static class DeviceCommand implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            CommandLine.usage(this, System.out);
            return 0;
        }
    }

    @Command(name = "add", mixinStandardHelpOptions = true, description = "Add a new device into the project")
    private static class DeviceAddCommand implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.print("add");
            return 0;
        }
    }

    @Command(name = "assign", mixinStandardHelpOptions = true, description = "Assign actual device and/or port")
    private static class DeviceAssignCommand implements Callable<Integer> {
        @Option(names = {"--name", "-n"}, description = "Name of the device to be assigned")
        private String name;

        @Option(names = {"--all"}, description = "Assign all devices")
        private boolean all;

        @Option(names = {"--auto", "-a"}, description = "Assign devices and ports automatically")
        private boolean auto;

        @Option(names = {"--clear", "-c"}, description = "Clear device and port assignment")
        private boolean clear;

        @Override
        public Integer call() throws Exception {
            System.out.print("assign ");
            System.out.print(name);
            System.out.print(all);
            System.out.print(auto);
            System.out.println(clear);
            return 0;
        }
    }

    @Command(name = "remove", mixinStandardHelpOptions = true, description = "Remove device from the project")
    private static class DeviceRemoveCommand implements Callable<Integer> {
        @Option(names = {"--name", "-n"}, description = "Name of the device to be removed")
        private String name;

        @Option(names = {"--all"}, description = "Remove all devices")
        private boolean all;

        @Override
        public Integer call() throws Exception {
            System.out.print("remove ");
            System.out.print(name);
            System.out.print(all);
            return 0;
        }
    }

    @Command(name = "upload", mixinStandardHelpOptions = true, description = "Upload the project to the board")
    private static class UploadCommand implements Callable<Integer> {
        @Option(names = {"--port", "-p"}, description = "Upload port e.g. COM1, /dev/ttyUSB0")
        private String port;

        @Override
        public Integer call() throws Exception {
            System.out.print("upload ");
            System.out.print(port);
            return 0;
        }
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("calling just command. do nothing / show help");
        return 0;
    }

    public static void main(String[] args) {
        // List of all support commands
        // allio init
        // allio init --directory ...
        // allio device add
        // allio device assign --name led1 --auto
        // allio device assign --all --auto
        // allio device remove
        // allio device upload
        // allio device upload --port COM1

        new CommandLine(new Main())
                .addSubcommand(new InitCommand())
                .addSubcommand(new DeviceCommand())
                .addSubcommand(new UploadCommand())
                .execute("device");
    }
}