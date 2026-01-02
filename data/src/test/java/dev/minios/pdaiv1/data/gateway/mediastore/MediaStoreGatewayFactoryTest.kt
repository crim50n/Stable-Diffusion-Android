package dev.minios.pdaiv1.data.gateway.mediastore

import android.content.Context
import dev.minios.pdaiv1.core.common.extensions.shouldUseNewMediaStore
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MediaStoreGatewayFactoryTest {

    private val stubContext = mockk<Context>()
    private val stubFileProviderDescriptor = mockk<FileProviderDescriptor>()

    private val factory = MediaStoreGatewayFactory(
        context = stubContext,
        fileProviderDescriptor = stubFileProviderDescriptor,
    )

    @Before
    fun setUp() {
        mockkStatic(::shouldUseNewMediaStore)
    }

    @After
    fun tearDown() {
        unmockkStatic(::shouldUseNewMediaStore)
    }

    @Test
    fun `given app running on Android SDK 26 (O), expected factory returned instance of type MediaStoreGatewayOldImpl`() {
        every { shouldUseNewMediaStore() } returns false
        val actual = factory.invoke()
        Assert.assertEquals(true, actual is MediaStoreGatewayOldImpl)
    }

    @Test
    fun `given app running on Android SDK 31 (S), expected factory returned instance of type MediaStoreGatewayOldImpl`() {
        every { shouldUseNewMediaStore() } returns false
        val actual = factory.invoke()
        Assert.assertEquals(true, actual is MediaStoreGatewayOldImpl)
    }

    @Test
    fun `given app running on Android SDK 32 (S_V2), expected factory returned instance of type MediaStoreGatewayImpl`() {
        every { shouldUseNewMediaStore() } returns true
        val actual = factory.invoke()
        Assert.assertEquals(true, actual is MediaStoreGatewayImpl)
    }

    @Test
    fun `given app running on Android SDK 34 (UPSIDE_DOWN_CAKE), expected factory returned instance of type MediaStoreGatewayImpl`() {
        every { shouldUseNewMediaStore() } returns true
        val actual = factory.invoke()
        Assert.assertEquals(true, actual is MediaStoreGatewayImpl)
    }
}
