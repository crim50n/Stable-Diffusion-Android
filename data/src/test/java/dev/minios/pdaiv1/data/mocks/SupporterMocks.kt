@file:Suppress("DEPRECATION")

package dev.minios.pdaiv1.data.mocks

import dev.minios.pdaiv1.domain.entity.Supporter
import java.util.Date

val mockSupporters = listOf(
    Supporter(
        id = 5598,
        name = "NZ",
        date = Date(1998, 5, 5),
        message = "I always wanted support you ❤",
    ),
)
