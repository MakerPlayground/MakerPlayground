package io.makerplayground.ui;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import java.io.IOException;
import java.io.UnsupportedEncodingException; 
import java.util.*;

public class AzureRestAPI {
    public static class GetToken extends Task<List<String>> {
        private final String tenantId;
        private final String clientId;
        private final String clientSecret;

        public GetToken(String tenantId, String clientId, String clientSecret) {
            this.tenantId = tenantId;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }

        @Override
        protected List<String> call() {
            String url = "https://login.microsoftonline.com/" + tenantId + "/oauth2/token";

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
            urlParameters.add(new BasicNameValuePair("client_id", clientId));
            urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));
            urlParameters.add(new BasicNameValuePair("resource", "https://management.azure.com/"));

            try {
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpResponse response = client.execute(post);
                System.out.println("Response Code : "
                        + response.getStatusLine().getStatusCode());

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.readTree(response.getEntity().getContent());

                return List.of(node.get("access_token").asText());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Collections.emptyList();
        }
    }

    String createIotHubInput(String inputName, String iotHubName, String iotHubPrimaryKey) {
        return "      \"inputs\":[    \n" +
                "         {    \n" +
                "            \"name\": \"" + inputName + "\",  \n" +
                "            \"properties\":{    \n" +
                "               \"type\":\"stream\",  \n" +
                "               \"serialization\":{    \n" +
                "                  \"type\":\"JSON\",  \n" +
                "                  \"properties\":{    \n" +
                "                     \"encoding\":\"UTF8\"  \n" +
                "                  }  \n" +
                "               },  \n" +
                "               \"datasource\":{    \n" +
                "                  \"type\":\"Microsoft.Devices/IotHubs\",  \n" +
                "                  \"properties\":{    \n" +
                "                     \"iotHubNamespace\":\"" + iotHubName + "\",  \n" +
                "                     \"sharedAccessPolicyName\":\"iothubowner\",  \n" +
                "                     \"sharedAccessPolicyKey\":\"" + iotHubPrimaryKey + "\"\n" +
                "                  }\n" +
                "               }\n" +
                "            }\n" +
                "         }\n" +
                "       ],\n";
    }

    String createTransformation(String queryString) {
        return "       \"transformation\":{    \n" +
                "         \"name\":\"ProcessSampleData\",  \n" +
                "         \"properties\":{    \n" +
                "            \"streamingUnits\":1,  \n" +
                "            \"query\":\"" + queryString + "\"\n" +
                "           }  \n" +
                "      }, \n";
    }

    String createSqlDatabaseOutput(String outputName, String serverName, String databaseName, String tableName, String username, String password) {
        return "      \"outputs\":[    \n" +
                "         {    \n" +
                "            \"name\":\"" + outputName + "\",  \n" +
                "            \"properties\":{    \n" +
                "               \"datasource\":{    \n" +
                "                  \"type\":\"Microsoft.Sql/Server/Database\",  \n" +
                "                  \"properties\":{    \n" +
                "                     \"server\":\"" + serverName + ".database.windows.net\",  \n" +
                "                     \"database\":\"" + databaseName + "\",  \n" +
                "                     \"table\":\"" + tableName + "\",  \n" +
                "                     \"user\":\"" + username + "\",  \n" +
                "                     \"password\":\"" + password + "\"  \n" +
                "                  }  \n" +
                "               }  \n" +
                "            }  \n" +
                "         }  \n" +
                "      ]  \n";
    }

    String createBlobStorageOutput(String outputName, String accountName, String accountKey, String containerName, String type, String delimiter) {
        return "      \"outputs\":[    \n" +
                "         {    \n" +
                "            \"name\":\"" + outputName + "\",  \n" +
                "            \"properties\":{    \n" +
                "               \"datasource\":{    \n" +
                "                  \"type\":\"Microsoft.Storage/Blob\",  \n" +
                "                  \"properties\":{    \n" +
                "                     \"storageAccounts\":[ \n" +
                "                           {\n" +
                "                           \"accountName\":\"" + accountName + "\",  \n" +
                "                           \"accountKey\":\"" + accountKey + "\",  \n" +
                "                           }\n" +
                "                      ],\n" +
                "                     \"container\":\"" + containerName + "\",  \n" +
                "                     \"pathPattern\":\"{date}{time}\"  \n" +
                "                      }  \n" +
                "                  },  \n" +
                "                  \"serialization\":{  \n" +
                "                  \"type\":\"CSV\", \n" +
                "                  \"properties\":{ \n" +
                "                       \"fieldDelimiter\":\"" + delimiter + "\", \n" +
                "                       \"encoding\":\"UTF8\" \n" +
                "                       }\n" +
                "               }  \n" +
                "            }  \n" +
                "         }  \n" +
                "      ]  \n";
    }

    String createBlobStorageOutput(String outputName, String accountName, String accountKey, String containerName, String type) {
        return "      \"outputs\":[    \n" +
                "         {    \n" +
                "            \"name\":\"" + outputName + "\",  \n" +
                "            \"properties\":{    \n" +
                "               \"datasource\":{    \n" +
                "                  \"type\":\"Microsoft.Storage/Blob\",  \n" +
                "                  \"properties\":{    \n" +
                "                     \"storageAccounts\":[ \n" +
                "                           {\n" +
                "                           \"accountName\":\"" + accountName + "\",  \n" +
                "                           \"accountKey\":\"" + accountKey + "\",  \n" +
                "                           }\n" +
                "                      ],\n" +
                "                     \"container\":\"" + containerName + "\",  \n" +
                "                     \"pathPattern\":\"{date}{time}\"  \n" +
                "                      }  \n" +
                "                  },  \n" +
                "                  \"serialization\":{  \n" +
                "                  \"type\":\"Json\", \n" +
                "                  \"properties\":{ \n" +
                "                       \"encoding\":\"UTF8\" \n" +
                "                       }\n" +
                "               }  \n" +
                "            }  \n" +
                "         }  \n" +
                "      ]  \n";
    }


    String createStreamAnalyticJson(String location, String iotHubInput, String transformation, String output) {
        return "{    \n" +
                "   \"location\":\"" + location + "\",  \n" +
                "   \"properties\":{    \n" +
                "      \"sku\":{    \n" +
                "         \"name\":\"standard\"  \n" +
                "      },  \n" +
                "      \"eventsOutOfOrderPolicy\":\"drop\",  \n" +
                "      \"eventsOutOfOrderMaxDelayInSeconds\":10,  \n" +
                "      \"compatibilityLevel\": \"1.1\",\n" +
                iotHubInput + transformation + output +
                "   }  \n" +
                "}";
    }


    public static class CreateStreamAnalyticJob extends Task<List<String>> {
        private final String subscriptionId;
        private final String resourceGroupName;
        private final String jobName;
        private final String accessToken;
        private final String jsonData;

        public CreateStreamAnalyticJob(String subscriptionId, String resourceGroupName, String jobName, String accessToken, String jsonData) {
            this.subscriptionId = subscriptionId;
            this.resourceGroupName = resourceGroupName;
            this.jobName = jobName;
            this.accessToken = accessToken;
            this.jsonData = jsonData;
        }

        @Override
        protected List<String> call() {
            String url = "https://management.azure.com/subscriptions/" + subscriptionId + "/resourcegroups/" + resourceGroupName + "/" +
                    "providers/Microsoft.StreamAnalytics/streamingjobs/" + jobName + "?api-version=2015-11-01";
            System.out.println(url);
            HttpClient client = HttpClientBuilder.create().build();
            HttpPut httpPut = new HttpPut(url);
            httpPut.addHeader("Authorization", "Bearer " + accessToken);
            StringEntity requestEntity = new StringEntity(jsonData, ContentType.APPLICATION_JSON);
            httpPut.setEntity(requestEntity);

            try {
                HttpResponse response = client.execute(httpPut);
                System.out.println("Response Code : "
                        + response.getStatusLine().getStatusCode());

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.readTree(response.getEntity().getContent());
                System.out.println(node);

                return Collections.emptyList();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }
    }
}
