package com.kohan.push.config

import com.kohan.push.scheduler.FCMTokenCleanupScheduler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class SchedulerConfig(
    private val fcmTokenCleanupScheduler: FCMTokenCleanupScheduler
) {
    @Bean
    fun tokenCleanupTask(): Runnable {
        return Runnable { fcmTokenCleanupScheduler.deleteExpiredTokens() }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun scheduleTokenCleanupTask() {
        tokenCleanupTask().run()
    }
}