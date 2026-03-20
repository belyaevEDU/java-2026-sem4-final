package org.example;

import org.example.apiInteraction.ApiClient;

public class Main {
    static void main() {
        ApiClient apiClient = new ApiClient();
        apiClient.call();
    }
}
