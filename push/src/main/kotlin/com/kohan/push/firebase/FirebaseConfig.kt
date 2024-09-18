package com.kohan.push.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

@Configuration
class FirebaseConfig(
    @Value("\${kohan.push.firebase.secret.key.path}")
    private val firebaseConfigPath: String,
) {
    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccount = FileInputStream(firebaseConfigPath)
        val options =
            FirebaseOptions
                .builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
        return FirebaseApp.initializeApp(options)
    }
}
