package dev.minios.pdaiv1.domain.entity

/**
 * Scheduler types supported by Stable Diffusion and Flux models.
 * Flux models typically use specific schedulers like "simple" or "normal".
 *
 * @param alias The API value sent to the server.
 * @param displayName Human-readable name for UI.
 */
enum class Scheduler(
    val alias: String,
    val displayName: String,
) {
    AUTOMATIC("automatic", "Automatic"),
    UNIFORM("uniform", "Uniform"),
    KARRAS("karras", "Karras"),
    EXPONENTIAL("exponential", "Exponential"),
    POLYEXPONENTIAL("polyexponential", "Polyexponential"),
    SGM_UNIFORM("sgm_uniform", "SGM Uniform"),
    KL_OPTIMAL("kl_optimal", "KL Optimal"),
    ALIGN_YOUR_STEPS("align_your_steps", "Align Your Steps"),
    SIMPLE("simple", "Simple"),
    NORMAL("normal", "Normal"),
    DDIM("ddim_uniform", "DDIM Uniform"),
    BETA("beta", "Beta");

    companion object {
        /**
         * Schedulers recommended for Flux models.
         */
        val FLUX_RECOMMENDED = listOf(SIMPLE, NORMAL, BETA)

        /**
         * Get scheduler by alias, defaulting to AUTOMATIC if not found.
         */
        fun fromAlias(alias: String): Scheduler =
            entries.find { it.alias.equals(alias, ignoreCase = true) } ?: AUTOMATIC
    }
}
