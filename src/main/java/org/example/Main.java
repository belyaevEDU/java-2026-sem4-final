package org.example;

import org.example.apiInteraction.ApiClient;

public class Main {
    static void main() {
        ApiClient apiClient = new ApiClient();
        // Пересмотр всего ебаного ТЗ и перепис согласно ему
        apiClient.call();
    }
}
