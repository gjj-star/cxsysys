package com.example.cxsysys.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.cxsysys.utils.AvatarRequests
import com.example.cxsysys.utils.TokenManager

@Composable
fun UserAvatar(
    avatarUrl: String?,
    userId: Int,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.Gray,
    iconSize: Dp = 32.dp
) {
    val sessionVersion by TokenManager.sessionVersion.collectAsState()
    val context = LocalContext.current.applicationContext

    key(sessionVersion, userId, avatarUrl) {
        val request = remember {
            if (userId <= 0) null else runCatching {
                AvatarRequests.forCurrentUser(
                    avatarUrl = avatarUrl,
                    userId = userId,
                    sessionUserId = TokenManager.getUserId(),
                    token = TokenManager.getToken()
                )
            }.getOrNull()
        }
        var loaded by remember { mutableStateOf(false) }

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            if (!loaded) {
                Icon(Icons.Default.Person, contentDescription = null, tint = iconTint, modifier = Modifier.size(iconSize))
            }
            if (request != null) {
                val imageLoader = remember {
                    ImageLoader.Builder(context)
                        .okHttpClient(AvatarRequests.client)
                        .memoryCache(null)
                        .diskCache(null)
                        .build()
                }
                DisposableEffect(imageLoader) {
                    onDispose { imageLoader.shutdown() }
                }
                val model = remember {
                    ImageRequest.Builder(context)
                        .data(request.url.toString())
                        .headers(request.headers)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build()
                }
                AsyncImage(
                    model = model,
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    onLoading = { loaded = false },
                    onSuccess = { loaded = true },
                    onError = { loaded = false }
                )
            }
        }
    }
}
