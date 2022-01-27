package com.example.quartz.routes

import com.example.quartz.dto.QUARTZ_GROUP_NAME
import mu.KotlinLogging
import org.apache.camel.CamelContext
import org.apache.camel.Endpoint
import org.apache.camel.builder.endpoint.EndpointRouteBuilder
import org.apache.camel.component.quartz.QuartzComponent
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Component

@Component
class QuartzRoutes(
    private val schedulerFactoryBean: SchedulerFactoryBean,
    private val camelContext: CamelContext
) : EndpointRouteBuilder() {

    private val logger = KotlinLogging.logger { }


    fun getCamelQuartzEndpoint(groupName: String, triggerName: String, camelContext: CamelContext): Endpoint {
        val quartzComponent = QuartzComponent(camelContext)
        quartzComponent.scheduler = schedulerFactoryBean.scheduler
        quartzComponent.isInterruptJobsOnShutdown = true
        quartzComponent.isAutoStartScheduler = false

        val endpoint =
            quartzComponent.createEndpoint("quartz://$QUARTZ_GROUP_NAME$triggerName?stateful=true&durableJob=true&trigger.repeatInterval=3000&trigger.repeatCount=0&trigger.misfireInstruction=4")


        if (!camelContext.componentNames.contains("quartz")) {
            camelContext.addComponent("quartz", quartzComponent)
        }
        return endpoint
    }

    override fun configure() {

        val triggerName = "myTimerTrigger"
        val routeId = "${triggerName}_route"

        val endpoint = getCamelQuartzEndpoint(QUARTZ_GROUP_NAME, triggerName, camelContext)

        from(endpoint)
            .routeId(routeId)
            .process { exchange ->
                logger.info { "Simple trigger fired Connecting to Endpoint 1: $exchange" }
                Thread.sleep(240000) // mimic task
            }
            .end()

    }
}