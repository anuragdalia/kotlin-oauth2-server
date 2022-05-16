package nl.myndocs.oauth2.ktor.feature

import io.ktor.util.AttributeKey
import nl.myndocs.oauth2.config.Configuration
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.ktor.feature.config.KtorConfiguration
import nl.myndocs.oauth2.ktor.feature.request.KtorCallContext
import io.ktor.server.application.*

class Oauth2ServerFeature(configuration: Configuration) {
    val callRouter = configuration.callRouter

    companion object Feature : BaseApplicationPlugin<ApplicationCallPipeline, KtorConfiguration, Oauth2ServerFeature> {
        override val key = AttributeKey<Oauth2ServerFeature>("Oauth2ServerFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: KtorConfiguration.() -> Unit): Oauth2ServerFeature {
            val ktorConfiguration = KtorConfiguration()
            configure(ktorConfiguration)
            val configuration =
                ConfigurationBuilder.build(configure as ConfigurationBuilder.Configuration.() -> Unit, ktorConfiguration)


            val feature = Oauth2ServerFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Features) {
                val ktorCallContext = KtorCallContext(call)

                if (configuration.callRouter.authorizeEndpoint == ktorCallContext.path) {
                    ktorConfiguration.authenticationCallback(call, feature.callRouter)
                }

                if (
                    arrayOf(
                        configuration.callRouter.tokenEndpoint,
                        configuration.callRouter.tokenInfoEndpoint
                    ).contains(ktorCallContext.path)
                ) {
                    feature.callRouter.route(ktorCallContext)
                }
            }

            return feature
        }
    }
}
