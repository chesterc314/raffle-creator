package co.za.chester.rafflecreator.rafflecreator

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import gr.net.maroulis.library.EasySplashScreen



class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val easySplashScreenView: View = EasySplashScreen(this)
                .withFullScreen()
                .withTargetActivity(MainActivity::class.java)
                .withSplashTimeOut(4000)
                .withBackgroundResource(android.R.color.white)
                .withFooterText("Copyright ™ 2018")
                .withLogo(R.mipmap.raffle_creator_logo)
                .create()

        setContentView(easySplashScreenView)
    }
}
