package it.cynomys.cfmandroid.view

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun CameraLiveScreen(
    cameraUrl: String,
    navController: NavController
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var useWebView by remember { mutableStateOf(cameraUrl.contains("player.php")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Camera View") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (useWebView) {
                IPCamWebView(
                    url = cameraUrl,
                    modifier = Modifier.fillMaxSize(),
                    onLoadingChanged = { loading -> isLoading = loading }
                )
            } else {
                VideoPlayerView(
                    context = context,
                    videoUrl = cameraUrl,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        // Fallback to WebView if direct stream fails
                        useWebView = true
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun IPCamWebView(
    url: String,
    modifier: Modifier = Modifier,
    onLoadingChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoadingChanged(false)
                    }
                }

                loadUrl(url)
            }
        },
        modifier = modifier
    )
}

@Composable
fun VideoPlayerView(
    context: Context,
    videoUrl: String,
    modifier: Modifier = Modifier,
    onError: () -> Unit = {}
) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoPlayer", "Playback error", error)
                        onError()
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> Log.d("VideoPlayer", "Player ready")
                            Player.STATE_BUFFERING -> Log.d("VideoPlayer", "Player buffering")
                            Player.STATE_ENDED -> Log.d("VideoPlayer", "Player ended")
                            Player.STATE_IDLE -> Log.d("VideoPlayer", "Player idle")
                        }
                    }
                })
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            }
        },
        modifier = modifier
    )
}