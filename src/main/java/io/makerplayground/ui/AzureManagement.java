package io.makerplayground.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class AzureManagement {
    public static class LoginTask extends Task<List<String>> {
        private StringProperty code = new SimpleStringProperty();

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C", "az login --use-device-code");
                Process p = pb.start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    String line = s.nextLine();
                    Platform.runLater(() -> code.set(line));
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                List<String> subscription = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    subscription.add(node.get(i).get("id").asText());
                }

                p.waitFor();
                return subscription;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getCode() {
            return code.get();
        }

        public StringProperty codeProperty() {
            return code;
        }
    }

    public static class LogOutTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C", "az logout");
                Process p = pb.start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                p.waitFor();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getCode() {
            return error.get();
        }

        public StringProperty codeProperty() {
            return error;
        }
    }

    public static class SubscriptionTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "account", "list")));
                Process p = pb.start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> subscription = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    subscription.add(node.get(i).get("id").asText());
                }
                return subscription;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }

    public static class ResourceGroupListTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "group", "list")));
                Process p = pb.start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> name = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    name.add(node.get(i).get("name").asText());
                }
                return name;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }

    public static class ResourceGroupCreateTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String resourceGroupName;
        private final String location;

        public ResourceGroupCreateTask(String resourceGroupName, String location) {
            this.resourceGroupName = resourceGroupName;
            this.location = location;
        }

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "group", "create", "-l", location, "-n", resourceGroupName)));
                Process p = pb.start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> status = new ArrayList<>();
                status.add(node.get("properties").get("provisioningState").asText());

                return status;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }

    public static class ResourceGroupDeleteTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String resourceGroupName;

        public ResourceGroupDeleteTask(String resourceGroupName) {
            this.resourceGroupName = resourceGroupName;
        }

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "group", "delete", "-n", resourceGroupName, "-y")));
                Process p = pb.start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                System.out.println(node);
                p.waitFor();
                //No return data
                List<String> status = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    status.add(node.get(i).get("properties").get(0).get("provisioningState").asText());
                }
                return status;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }

    public static class CognitiveCreateTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String cognitiveName;
        private final String resourceGroupName;
        private final String location;
        private final String kind;
        private final String sku;

        public CognitiveCreateTask(String cognitiveName, String resourceGroupName, String location, String kind, String sku) {
            this.cognitiveName = cognitiveName;
            this.resourceGroupName = resourceGroupName;
            this.location = location;
            this.kind = kind;
            this.sku = sku;
        }

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "cognitiveservices", "account", "create", "-n", cognitiveName, "-g", resourceGroupName,
                                "-l", location, "--kind", kind, "--sku", sku, "--yes")));
                Process p = pb.start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    StringBuilder sb = new StringBuilder();
                    if (s.hasNextLine()) {
                        while (s.hasNextLine()) {
                            sb.append(s.nextLine()).append("\n");
                        }
                    }
                    Platform.runLater(() -> error.set(String.valueOf(sb)));
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> status = new ArrayList<>();
                status.add(node.get("provisioningState").asText());
                return status;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }

    public static class CognitiveListTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "cognitiveservices", "account", "list")));
                Process p = pb.start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNextLine()) {
                        StringBuilder sb = new StringBuilder();
                        while (s.hasNextLine()) {
                            sb.append(s.nextLine());
                            System.out.println(sb);
                        }
                        Platform.runLater(() -> error.set(String.valueOf(sb)));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> status = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    status.add(node.get(i).get("name").asText());
                }
                return status;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }


    public static class IotHubCreateTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String iotHubName;
        private final String resourceGroupName;
        private final String location;
        private final String sku;

        public IotHubCreateTask(String iotHubName, String resourceGroupName, String location, String sku) {
            this.iotHubName = iotHubName;
            this.resourceGroupName = resourceGroupName;
            this.location = location;
            this.sku = sku;
        }

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "iot", "hub", "create", "-n", iotHubName, "-g", resourceGroupName,
                                "-l", location, "--sku", sku)));
                Process p = pb.start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    StringBuilder sb = new StringBuilder();
                    if (s.hasNextLine()) {
                        while (s.hasNextLine()) {
                            sb.append(s.nextLine()).append("\n");
                        }
                    }
                    Platform.runLater(() -> error.set(String.valueOf(sb)));
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                System.out.println(node);
                p.waitFor();
                List<String> status = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    status.add(node.get(i).get("provisioningState").asText());
                }
                return status;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }

    public static class IotHubListTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "iot", "hub", "list")));
                Process p = pb.start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    StringBuilder sb = new StringBuilder();
                    if (s.hasNextLine()) {
                        while (s.hasNextLine()) {
                            sb.append(s.nextLine()).append("\n");
                        }
                    }
                    Platform.runLater(() -> error.set(String.valueOf(sb)));
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> status = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    status.add(node.get(i).get("name").asText());
                }
                return status;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }

    public static class IotHubShowConnectionStringTask extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String iotHubName;

        public IotHubShowConnectionStringTask(String iotHubName) {
            this.iotHubName = iotHubName;
        }

        @Override
        protected List<String> call() throws Exception {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "iot", "hub", "show-connection-string", "-n", iotHubName)));
                Process p = pb.start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    StringBuilder sb = new StringBuilder();
                    if (s.hasNextLine()) {
                        while (s.hasNextLine()) {
                            sb.append(s.nextLine()).append("\n");
                        }
                    }
                    Platform.runLater(() -> error.set(String.valueOf(sb)));
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> connectionString = new ArrayList<>();
                connectionString.add(node.get("cs").asText());
                return connectionString;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }
}



