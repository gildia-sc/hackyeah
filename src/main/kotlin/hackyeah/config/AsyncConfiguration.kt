package hackyeah.config

import io.github.jhipster.async.ExceptionHandlingAsyncTaskExecutor
import io.github.jhipster.config.JHipsterProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.config.ScheduledTaskRegistrar

import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfiguration(private val jHipsterProperties: JHipsterProperties) : AsyncConfigurer, SchedulingConfigurer {

    private val log = LoggerFactory.getLogger(AsyncConfiguration::class.java)

    @Bean(name = arrayOf("taskExecutor"))
    override fun getAsyncExecutor(): Executor? {
        log.debug("Creating Async Task Executor")
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = jHipsterProperties.async.corePoolSize
        executor.maxPoolSize = jHipsterProperties.async.maxPoolSize
        executor.setQueueCapacity(jHipsterProperties.async.queueCapacity)
        executor.setThreadNamePrefix("hackyeah-Executor-")
        return ExceptionHandlingAsyncTaskExecutor(executor)
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler? {
        return SimpleAsyncUncaughtExceptionHandler()
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        taskRegistrar.setScheduler(scheduledTaskExecutor())
    }

    @Bean
    fun scheduledTaskExecutor(): Executor {
        return Executors.newScheduledThreadPool(jHipsterProperties.async.corePoolSize)
    }
}
