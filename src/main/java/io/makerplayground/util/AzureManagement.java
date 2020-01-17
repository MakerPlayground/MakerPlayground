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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.*;

public class AzureManagement {

    private static List<String> buildCommand(String... cmd){
        List<String> cli = new ArrayList<>();
        if(OSInfo.getOs() == OSInfo.OS.WINDOWS){
            cli.add("cmd.exe");
            cli.add("/C");
        }
        cli.addAll(List.of(cmd));
        return cli;
    }

    public static class LogInTask extends Task<List<AzureSubscription>> {
        @Override
        protected List<AzureSubscription> call() {
            try {
                List<String> command = buildCommand("az", "login"/*, "--use-device-code"*/);
                Process p = new ProcessBuilder(command).start();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(p.getInputStream());
                List<AzureSubscription> subscription = new ArrayList<>();
                for (JsonNode node : root) {
                    subscription.add(new AzureSubscription(node.get("name").asText(), node.get("id").asText()
                            , node.get("tenantId").asText(), node.get("user").get("name").asText()));
                }
                p.waitFor();
                return subscription;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }
    }

    public static class LogOutTask extends Task<Void> {
        private StringProperty error = new SimpleStringProperty("");

        @Override
        protected Void call() {
            try {
                List<String> command = buildCommand("az", "logout");
                Process p = new ProcessBuilder(command).start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                p.waitFor();

                command = buildCommand("az", "account", "clear");
                p = new ProcessBuilder(command).start();
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
            return null;
        }

        public String getErrorMessage() {
            return error.get();
        }

        public StringProperty errorMessageProperty() {
            return error;
        }
    }

    public static class ServicePrinciple extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "ad", "sp", "create-for-rbac")));
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
                List<String> requiredParameters = new ArrayList<>();
                requiredParameters.add(node.get("appId").asText());
                requiredParameters.add(node.get("password").asText());
                requiredParameters.add(node.get("tenant").asText());
                return requiredParameters;
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

    public static class ListSubscriptionTask extends Task<List<AzureSubscription>> {
        private StringProperty error = new SimpleStringProperty("");

        @Override
        protected List<AzureSubscription> call() {
            try {
                List<String> command = buildCommand("az", "account", "list");
                Process p = new ProcessBuilder(command).start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(p.getInputStream());
                List<AzureSubscription> subscription = new ArrayList<>();
                for (JsonNode node : root) {
                    subscription.add(new AzureSubscription(node.get("name").asText(), node.get("id").asText()
                            , node.get("tenantId").asText(), node.get("user").get("name").asText()));
                }
                p.waitFor();
                return subscription;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getErrorMessage() {
            return error.get();
        }

        public StringProperty errorMessageProperty() {
            return error;
        }
    }

    public static class ResourceGroupListTask extends Task<List<AzureResourceGroup>> {
        private final AzureSubscription subscription;
        private StringProperty error = new SimpleStringProperty("");

        public ResourceGroupListTask(AzureSubscription subscription) {
            this.subscription = subscription;
        }

        @Override
        protected List<AzureResourceGroup> call() {
            try {
                List<String> command = buildCommand("az", "group", "list", "--subscription",subscription.getId());
                Process p = new ProcessBuilder(command).start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(p.getInputStream());
                List<AzureResourceGroup> resourceGroups = new ArrayList<>();
                for (JsonNode node : root) {
                    resourceGroups.add(new AzureResourceGroup(node.get("name").asText(), node.get("location").asText()));
                }
                p.waitFor();
                return resourceGroups;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getErrorMessage() {
            return error.get();
        }

        public StringProperty errorMessageProperty() {
            return error;
        }
    }

    public static class ResourceGroupCreate extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String resourceGroupName;
        private final String location;

        public ResourceGroupCreate(String resourceGroupName, String location) {
            this.resourceGroupName = resourceGroupName;
            this.location = location;
        }

        @Override
        protected List<String> call() {
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
                return List.of(node.get("properties").get("provisioningState").asText());
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

    public static class ResourceGroupDelete extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String resourceGroupName;

        public ResourceGroupDelete(String resourceGroupName) {
            this.resourceGroupName = resourceGroupName;
        }

        @Override
        protected List<String> call() {
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

    public static class CognitiveCreate extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String cognitiveName;
        private final String resourceGroupName;
        private final String location;
        private final String kind;
        private final String sku;

        public CognitiveCreate(String cognitiveName, String resourceGroupName, String location, String kind, String sku) {
            this.cognitiveName = cognitiveName;
            this.resourceGroupName = resourceGroupName;
            this.location = location;
            this.kind = kind;
            this.sku = sku;
        }

        @Override
        protected List<String> call() {
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
                return List.of(node.get("provisioningState").asText());
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

    public static class CognitiveListTask extends Task<List<AzureCognitiveServices>> {
        private final AzureSubscription subscription;
        private final AzureResourceGroup resourceGroup;
        private StringProperty error = new SimpleStringProperty();

        public CognitiveListTask(AzureSubscription subscription, AzureResourceGroup resourceGroup) {
            this.subscription = subscription;
            this.resourceGroup = resourceGroup;
        }

        @Override
        protected List<AzureCognitiveServices> call() {
            try {
                List<String> command = buildCommand("az", "cognitiveservices", "account", "list", "--subscription"
                        , subscription.getId(), "--resource-group", resourceGroup.getName());
                Process p = new ProcessBuilder(command).start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(p.getInputStream());
                List<AzureCognitiveServices> cognitiveName = new ArrayList<>();
                for (JsonNode node : root) {
                    cognitiveName.add(new AzureCognitiveServices(node.get("name").asText(), node.get("location").asText()));
                }
                p.waitFor();
                return cognitiveName;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getErrorMesage() {
            return error.get();
        }

        public StringProperty errorMesageProperty() {
            return error;
        }
    }

    public static class CognitiveKeyListTask extends Task<AzureCognitiveServices> {
        private StringProperty error = new SimpleStringProperty();
        private final AzureSubscription subscription;
        private final AzureResourceGroup resourceGroup;
        private final AzureCognitiveServices cognitive;

        public CognitiveKeyListTask(AzureSubscription subscription, AzureResourceGroup resourceGroup, AzureCognitiveServices cognitive) {
            this.subscription = subscription;
            this.resourceGroup = resourceGroup;
            this.cognitive = cognitive;
        }

        @Override
        protected AzureCognitiveServices call() {
            try {
                List<String> command = buildCommand("az", "cognitiveservices", "account", "keys", "list"
                        , "--subscription", subscription.getId(), "--resource-group", resourceGroup.getName()
                        , "--name", cognitive.getName());
                Process p = new ProcessBuilder(command).start();
                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(p.getInputStream());
                p.waitFor();
                return new AzureCognitiveServices(cognitive.getName(), cognitive.getLocation(), root.get("key1").asText(), root.get("key2").asText());
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getErrorMesage() {
            return error.get();
        }

        public StringProperty errorMesageProperty() {
            return error;
        }
    }


    public static class IotHubCreate extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String iotHubName;
        private final String resourceGroupName;
        private final String location;
        private final String sku;

        public IotHubCreate(String iotHubName, String resourceGroupName, String location, String sku) {
            this.iotHubName = iotHubName;
            this.resourceGroupName = resourceGroupName;
            this.location = location;
            this.sku = sku;
        }

        @Override
        protected List<String> call() {
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
                return List.of(node.get("provisioningState").asText());
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

    public static class IotHubListTask extends Task<List<AzureIoTHub>> {
        private StringProperty error = new SimpleStringProperty();
        private final AzureSubscription subscription;
        private final AzureResourceGroup resourceGroup;

        public IotHubListTask(AzureSubscription subscription, AzureResourceGroup resourceGroup) {
            this.subscription = subscription;
            this.resourceGroup = resourceGroup;
        }

        @Override
        protected List<AzureIoTHub> call() {
            try {
                List<String> command = buildCommand("az", "iot", "hub", "list", "--subscription", subscription.getId()
                        , "--resource-group", resourceGroup.getName());
                Process p = new ProcessBuilder(command).start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(p.getInputStream());
                List<AzureIoTHub> status = new ArrayList<>();
                for (JsonNode node : root)
                {
                    status.add(new AzureIoTHub(node.get("name").asText()));
                }
                p.waitFor();
                return status;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getErrorMessage() {
            return error.get();
        }

        public StringProperty errorMessageProperty() {
            return error;
        }
    }

    public static class IotHubDeviceListTask extends Task<List<AzureIoTHubDevice>> {
        private StringProperty error = new SimpleStringProperty();
        private final AzureIoTHub iotHub;
        private final AzureSubscription subscription;
        private final AzureResourceGroup resourceGroup;

        public IotHubDeviceListTask(AzureIoTHub iotHub, AzureSubscription subscription, AzureResourceGroup resourceGroup) {
            this.iotHub = iotHub;
            this.subscription = subscription;
            this.resourceGroup = resourceGroup;
        }

        @Override
        protected List<AzureIoTHubDevice> call() {
            try {
                List<String> command = buildCommand("az", "iot", "hub", "device-identity", "list"
                        , "--subscription", subscription.getId(), "--resource-group", resourceGroup.getName()
                        , "--hub-name", iotHub.getName());
                Process p = new ProcessBuilder(command).start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(p.getInputStream());
                List<AzureIoTHubDevice> status = new ArrayList<>();
                for (JsonNode node : root)
                {
                    status.add(new AzureIoTHubDevice(node.get("deviceId").asText()));
                }
                p.waitFor();
                return status;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public String getErrorMessage() {
            return error.get();
        }

        public StringProperty errorMessageProperty() {
            return error;
        }
    }

    public static class IotHubShowConnectionString extends Task<AzureIoTHubDevice> {
        private StringProperty error = new SimpleStringProperty();
        private final AzureSubscription azureSubscription;
        private final AzureResourceGroup resourceGroup;
        private final AzureIoTHub iotHub;
        private final AzureIoTHubDevice iotHubDevice;

        public IotHubShowConnectionString(AzureSubscription azureSubscription, AzureResourceGroup resourceGroup, AzureIoTHub iotHub, AzureIoTHubDevice iotHubDevice) {
            this.azureSubscription = azureSubscription;
            this.resourceGroup = resourceGroup;
            this.iotHub = iotHub;
            this.iotHubDevice = iotHubDevice;
        }

        @Override
        protected AzureIoTHubDevice call() {
            try {
                List<String> command = buildCommand("az", "iot", "hub", "device-identity", "show-connection-string"
                        , "--hub-name", iotHub.getName(), "--device-id", iotHubDevice.getName()
                        , "--subscription", azureSubscription.getId(), "--resource-group", resourceGroup.getName());
                Process p = new ProcessBuilder(command).start();

                try (Scanner s = new Scanner(p.getErrorStream())) {
                    if (s.hasNext()) {
                        String line = s.nextLine();
                        Platform.runLater(() -> error.set(line));
                    }
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                System.out.println(node.get("cs").asText());
                return new AzureIoTHubDevice(iotHubDevice.getName(), node.get("cs").asText());
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getError() {
            return error.get();
        }

        public StringProperty errorProperty() {
            return error;
        }
    }

    public static class SqlDatabaseCreate extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String databaseName;
        private final String serverName;
        private final String resourceGroupName;
        private final String tier;

        public SqlDatabaseCreate(String databaseName, String serverName, String resourceGroupName, String tier) {
            this.databaseName = databaseName;
            this.serverName = serverName;
            this.resourceGroupName = resourceGroupName;
            this.tier = tier;
        }

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "sql", "db", "create", "-n", databaseName, "-g", resourceGroupName, "-s", serverName, "-e", tier)));
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
                List<String> databaseName = new ArrayList<>();
                databaseName.add(node.get("name").asText());
                return databaseName;
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

    public static class SqlDatabaseList extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String serverName;
        private final String resourceGroupName;

        public SqlDatabaseList(String serverName, String resourceGroupName) {
            this.serverName = serverName;
            this.resourceGroupName = resourceGroupName;
        }

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "sql", "db", "list", "-g", resourceGroupName, "-s", serverName)));
                Process p = pb.start();

// Bug
//                try (Scanner s = new Scanner(p.getErrorStream())) {
//                    StringBuilder sb = new StringBuilder();
//                    if (s.hasNextLine()) {
//                        while (s.hasNextLine()) {
//                            sb.append(s.nextLine()).append("\n");
//                        }
//                    }
//                    Platform.runLater(() -> error.set(String.valueOf(sb)));
//                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> databaseName = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    databaseName.add(node.get(i).get("name").asText());
                }
                return databaseName;
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

    public static class SqlDatabaseShowConnectionString extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String serverName;
        private final String databaseName;

        public SqlDatabaseShowConnectionString(String serverName, String databaseName) {
            this.serverName = serverName;
            this.databaseName = databaseName;
        }

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "sql", "db", "show-connection-string", "-n", databaseName, "-s", serverName, "-c", "jdbc")));
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
                return List.of(node.asText());
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

    public static class SqlServerList extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "sql", "server", "list")));
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
                List<String> databaseName = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    databaseName.add(node.get(i).get("name").asText());
                }
                return databaseName;
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

    public static class SqlServerCreate extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String serverName;
        private final String resourceGroup;
        private final String location;
        private final String adminUser;
        private final String adminPassword;

        public SqlServerCreate(String serverName, String resourceGroup, String location, String adminUser, String adminPassword) {
            this.serverName = serverName;
            this.resourceGroup = resourceGroup;
            this.location = location;
            this.adminUser = adminUser;
            this.adminPassword = adminPassword;
        }

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "sql", "server", "create", "-n", serverName, "-g", resourceGroup, "-l", location, "-u", adminUser, "-p", adminPassword)));
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
                return List.of(node.get("name").asText());
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

    public static class StorageAccountList extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "storage", "account", "list")));
                Process p = pb.start();

                //Bug
//                try (Scanner s = new Scanner(p.getErrorStream())) {
//                    StringBuilder sb = new StringBuilder();
//                    if (s.hasNextLine()) {
//                        while (s.hasNextLine()) {
//                            sb.append(s.nextLine()).append("\n");
//                        }
//                    }
//                    Platform.runLater(() -> error.set(String.valueOf(sb)));
//                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(p.getInputStream());
                p.waitFor();
                List<String> storageName = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    storageName.add(node.get(i).get("name").asText());
                }
                return storageName;
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

    public static class StorageAccountCreate extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String storageAccountName;
        private final String resourceGroupName;

        public StorageAccountCreate(String storageAccountName, String resourceGroupName) {
            this.storageAccountName = storageAccountName;
            this.resourceGroupName = resourceGroupName;
        }

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "storage", "account", "create", "-n", storageAccountName, "-g", resourceGroupName)));
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
                return List.of(node.get("provisioningState").asText());
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

    public static class StorageContainerCreate extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String storageAccountName;
        private final String containerName;

        public StorageContainerCreate(String containerName, String storageAccountName) {
            this.storageAccountName = storageAccountName;
            this.containerName = containerName;
        }

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "storage", "container", "create", "-n", containerName, "--account-name", storageAccountName, "--fail-on-exist")));
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
                return List.of(node.get("created").asText());
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

    public static class StorageContainerList extends Task<List<String>> {
        private StringProperty error = new SimpleStringProperty();
        private final String storageAccountName;

        public StorageContainerList(String storageAccountName) {
            this.storageAccountName = storageAccountName;
        }

        @Override
        protected List<String> call() {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
                        ("az " + String.join(" ", "storage", "container", "list", "--account-name", storageAccountName)));
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
                List<String> containerName = new ArrayList<>();
                for (int i = 0; i < node.size(); i++) {
                    containerName.add(node.get(i).get("name").asText());
                }
                return containerName;
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



