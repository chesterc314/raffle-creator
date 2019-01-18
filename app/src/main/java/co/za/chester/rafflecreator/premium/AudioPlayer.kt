package co.za.chester.rafflecreator.premium

import android.content.Context
import android.media.MediaPlayer
import org.funktionale.option.Option

object AudioPlayer {
    fun play(context: Context, resId: Int, completePlayAction: () -> Unit) {
        var maybeMediaPlayer: Option<MediaPlayer> = Option.empty()
        fun stop() {
            maybeMediaPlayer.map { mediaPlayer ->
                mediaPlayer.stop()
                mediaPlayer.release()
                maybeMediaPlayer = Option.empty()
            }
        }
        stop()
        maybeMediaPlayer = Option.Some<MediaPlayer>(MediaPlayer.create(context, resId))
        maybeMediaPlayer.map { mediaPlayer ->
            mediaPlayer.setOnCompletionListener {
                stop()
                completePlayAction()
            }
            mediaPlayer.start()
        }
    }
}
