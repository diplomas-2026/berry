package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.company.product.mobile.BuildConfig

@Composable
fun MediaFrame(
    url: String?,
    placeholderTitle: String,
    placeholderSubtitle: String? = null,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f,
    rounded: Dp = 18.dp
) {
    val shape = RoundedCornerShape(rounded)
    val resolvedUrl = normalizeMediaUrl(url)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(shape)
    ) {
        if (resolvedUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = placeholderTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!placeholderSubtitle.isNullOrBlank()) {
                        Text(
                            text = placeholderSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            AsyncImage(
                model = resolvedUrl,
                contentDescription = placeholderTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun normalizeMediaUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    if (url.startsWith("http://") || url.startsWith("https://")) return url
    val base = BuildConfig.API_BASE_URL.trimEnd('/')
    return if (url.startsWith("/")) "$base$url" else "$base/$url"
}
