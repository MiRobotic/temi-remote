package sg.mirobotic.temiremote

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import com.bumptech.glide.Glide
import com.robotemi.sdk.NlpResult
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnRobotLiftedListener
import sg.mirobotic.temiremote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnRobotInteractionListener, OnRobotLiftedListener, Robot.NlpListener, Robot.AsrListener, Robot.ConversationViewAttachesListener {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val handler =  Handler(Looper.getMainLooper())

    private var robot: Robot? = null
    private lateinit var context: Context

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        robot = Robot.getInstance()

        robot?.addNlpListener(this)
        robot?.addAsrListener(this)

        setFace(R.drawable.g1)
        handler.postDelayed({
            speak("Hi, My name is temi!")
        }, 1000)
    }

    private fun setFace(face: Int) {

        Glide.with(context).load(face).into(binding.imgView)
        handler.postDelayed({
            Glide.with(context).load(R.drawable.g4).into(binding.imgView)
        }, 5000)

    }

    override fun onStart() {
        super.onStart()

        Log.e(TAG, "app started")

        val isKos = robot?.isSelectedKioskApp() ?: false

        Log.e(TAG, "isKos > $isKos")

        if (!isKos) {
            robot?.requestToBeKioskApp()
        }

        robot?.addOnRobotLiftedListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.e("MA","Key > $keyCode ${event?.device} ${event?.displayLabel} ${event?.action} ${event?.downTime}")
        binding.key.text = "Key: $keyCode | ${event?.device}"
        return super.onKeyDown(keyCode, event)
    }

    override fun onAsrResult(asrResult: String) {
        Log.e(TAG, "onAsrResult >> $asrResult")
        handler.postDelayed({
            setFace(R.drawable.g2)
        }, 2000)
        robot?.startDefaultNlu(asrResult)
    }

    override fun onConversationAttaches(isAttached: Boolean) {
        Log.e(TAG, "onConversationAttaches >> $isAttached")
    }

    override fun onNlpCompleted(nlpResult: NlpResult) {

        val params = nlpResult.params
        val keys = params.keys

        Log.e(TAG, "action >> ${nlpResult.action} | ${keys.size}")

        for (k in keys) {
            Log.d(TAG, "params: $k = ${params[k]}")
        }

    }

    override fun onRobotLifted(isLifted: Boolean, reason: String) {
        Log.e(TAG,"onRobotLifted")
        speak("Please put me down! else i'm calling police now!")
    }

    override fun speak(msg: String) {
        val ttsRequest = TtsRequest.create(msg, true)
        robot?.speak(ttsRequest)
        Log.e(TAG,"Speak $msg")
    }

    override fun onStop() {
        robot?.removeOnRobotLiftedListener(this)
        super.onStop()
    }

    override fun onDestroy() {
        robot?.removeNlpListener(this)
        robot?.removeAsrListener(this)

        super.onDestroy()
    }
}