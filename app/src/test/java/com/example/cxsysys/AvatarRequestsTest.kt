package com.example.cxsysys

import com.example.cxsysys.utils.AvatarRequests
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test

class AvatarRequestsTest {
    private val base = "https://example.test/planting/api/v1/"
    private val avatar = base + "files/avatar-photo.jpg"

    @Test
    fun currentUserRequestUsesTrustedFileAndHeaderOnly() {
        val request = requireNotNull(AvatarRequests.forCurrentUser(avatar, 12, 12, "session-token", base))

        assertEquals(avatar, request.url.toString())
        assertEquals("GET", request.method)
        assertEquals("session-token", request.header("token"))
        assertEquals("no-store", request.header("Cache-Control"))
        assertNull(request.url.query)
        assertNull(request.header("Authorization"))
        assertNotNull(AvatarRequests.trustedUrl(base + "files/avatar/photo.png", base))
        assertNotNull(AvatarRequests.trustedUrl("https://example.test:443/planting/api/v1/files/photo.jpg", base))
    }

    @Test
    fun otherUsersAndMissingCredentialsNeverCreateRequests() {
        assertNull(AvatarRequests.forCurrentUser(avatar, 12, 13, "session-token", base))
        assertNull(AvatarRequests.forCurrentUser(avatar, 0, 0, "session-token", base))
        for (token in listOf(null, "", " ", "invalid\r\ntoken")) {
            assertNull(AvatarRequests.forCurrentUser(avatar, 12, 12, token, base))
        }
    }

    @Test
    fun untrustedOriginsAndNonFilePathsAreRejected() {
        for (url in listOf(
            "http://example.test/planting/api/v1/files/photo.jpg",
            "https://other.test/planting/api/v1/files/photo.jpg",
            "https://example.test.evil.test/planting/api/v1/files/photo.jpg",
            "https://example.test:444/planting/api/v1/files/photo.jpg",
            "https://user:password@example.test/planting/api/v1/files/photo.jpg",
            "https://example.test/files/photo.jpg",
            base + "user",
            base + "files-elsewhere/photo.jpg",
            base + "files/",
            base + "files//photo.jpg",
            base + "files/photo.jpg?token=secret",
            base + "files/photo.jpg?",
            base + "files/photo.jpg#fragment",
            "files/photo.jpg",
            "file:///planting/api/v1/files/photo.jpg",
            " " + avatar,
            avatar + " "
        )) {
            assertNull("Should reject $url", AvatarRequests.trustedUrl(url, base))
        }
    }

    @Test
    fun traversalAndEncodedSeparatorsAreRejectedBeforeNormalization() {
        for (path in listOf(
            "files/../files/photo.jpg",
            "files/./photo.jpg",
            "files/%2e%2e/files/photo.jpg",
            "files/%2E/photo.jpg",
            "files/.%2e/files/photo.jpg",
            "files/%252e%252e/files/photo.jpg",
            "files/avatar%2f..%2fphoto.jpg",
            "files/avatar%5cphoto.jpg",
            "files/avatar\\photo.jpg",
            "files/photo%00.jpg",
            "files/photo%0a.jpg"
        )) {
            assertNull("Should reject $path", AvatarRequests.trustedUrl(base + path, base))
        }
    }

    @Test
    fun avatarClientDoesNotForwardTokenThroughRedirects() {
        val secondHop = MockWebServer()
        val firstHop = MockWebServer()
        val client = AvatarRequests.newClient()
        try {
            secondHop.start()
            firstHop.start()
            secondHop.enqueue(MockResponse().setResponseCode(200))
            firstHop.enqueue(MockResponse().setResponseCode(302).addHeader("Location", secondHop.url("/other")))
            val localBase = firstHop.url("/api/").toString()
            val request = requireNotNull(AvatarRequests.forCurrentUser(localBase + "files/photo.jpg", 12, 12, "avatar-test-token", localBase))

            client.newCall(request).execute().use { assertEquals(302, it.code) }

            assertFalse(client.followRedirects)
            assertFalse(client.followSslRedirects)
            assertNull(client.cache)
            assertTrue(client.interceptors.isEmpty())
            assertTrue(client.networkInterceptors.isEmpty())
            assertEquals("avatar-test-token", requireNotNull(firstHop.takeRequest(1, TimeUnit.SECONDS)).getHeader("token"))
            assertEquals(0, secondHop.requestCount)
        } finally {
            firstHop.shutdown()
            secondHop.shutdown()
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
        }
    }

    @Test
    fun fileResponseIsReadAsOriginalBytes() {
        val server = MockWebServer()
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a)
        val client = AvatarRequests.newClient()
        try {
            server.start()
            server.enqueue(MockResponse().setHeader("Content-Type", "image/png").setBody(Buffer().write(bytes)))
            val localBase = server.url("/api/").toString()
            val request = requireNotNull(AvatarRequests.forCurrentUser(localBase + "files/photo.png", 12, 12, "avatar-test-token", localBase))
            client.newCall(request).execute().use { response ->
                assertArrayEquals(bytes, requireNotNull(response.body).bytes())
            }
        } finally {
            server.shutdown()
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
        }
    }
}
