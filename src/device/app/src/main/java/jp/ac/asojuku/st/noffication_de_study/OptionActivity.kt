package jp.ac.asojuku.st.noffication_de_study

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_option.*
import org.json.JSONObject

class OptionActivity : AppCompatActivity() {
    // Google SignIn
    private val rcSignIn = 7
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    private lateinit var spEditor: SharedPreferences.Editor
    private lateinit var spGetter: SharedPreferences

    private val noticeIntervalItems = arrayOf("5", "10", "15", "20", "25", "30")

    private var radioSelect = "four"
    private var interval = "5"
    private var startTime = "09:00"
    private var endTime = "21:00"

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option)

        spEditor = getSharedPreferences("user_data", Context.MODE_PRIVATE).edit()

        val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, noticeIntervalItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        OA_Noffication_Interval.adapter = adapter

        // SharedPreferencesから設定情報を取得し画面に反映
        spGetter = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        OA_NDS_Mode_BTN.isChecked = spGetter.getBoolean("NDS_check", false)
        OA_SDS_Mode_BTN.isChecked = spGetter.getBoolean("SDS_check", false)
        OA_Noffication_Time_Between1.text = spGetter.getString("NDS_Start", "09:00")
        OA_Noffication_Time_Between2.text = spGetter.getString("NDS_End", "21:00")
        OA_Noffication_Interval.setSelection(noticeIntervalItems.indexOf(spGetter.getString("NDS_Interval", "5")))
        when (spGetter.getString("way_radio_select", "four")) {
            "four" -> way_radio_group.check(R.id.way_four_radio_button)
            "two" -> way_radio_group.check(R.id.way_two_radio_button)
        }

        // Google SignIn
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onResume() {
        super.onResume()

        OA_Back_BTN.setSafeClickListener {
            pushEndButton()
        }

        // 通知の問題の出題方法
        way_radio_group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.way_four_radio_button -> radioSelect = "four"
                R.id.way_two_radio_button -> radioSelect = "two"
            }
        }

        // 出題開始時間の指定
        OA_Noffication_Time_Between1.setSafeClickListener {
            val nowTime = spGetter.getString("NDS_Start", "09:00")
            val nowTimeList: List<String> =
                if (nowTime.isNullOrEmpty()) listOf("9", "00") else nowTime.split(Regex(":"))
            TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minuteOfDay ->
                    val endTime = spGetter.getString("NDS_End", "21:00") as String
                    val endTimeList: List<String> = endTime.split(Regex(":"))
                    if (hourOfDay > Integer.parseInt(endTimeList[0]) ||
                        (hourOfDay == Integer.parseInt(endTimeList[0]) && minuteOfDay >= Integer.parseInt(endTimeList[1]))
                    ) {
                        Toast.makeText(this, "終了時間を超えています", Toast.LENGTH_LONG).show()
                    } else {
                        val time = String.format("%02d:%02d", hourOfDay, minuteOfDay)
                        OA_Noffication_Time_Between1.text = time
                        startTime = time
                    }
                }, nowTimeList[0].toInt(), nowTimeList[1].toInt(), true
            ).show()
        }

        // 出題終了時間の指定
        OA_Noffication_Time_Between2.setSafeClickListener {
            val nowTime = spGetter.getString("NDS_End", "21:00")
            val nowTimeList: List<String> =
                if (nowTime.isNullOrEmpty()) listOf("21", "00") else nowTime.split(Regex(":"))
            TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minuteOfDay ->
                    val startTime = spGetter.getString("NDS_Start", "09:00") as String
                    val startTimeList: List<String> = startTime.split(Regex(":"))
                    if (hourOfDay < Integer.parseInt(startTimeList[0]) ||
                        (hourOfDay == Integer.parseInt(startTimeList[0]) && minuteOfDay <= Integer.parseInt(
                            startTimeList[1]
                        ))
                    ) {
                        Toast.makeText(this, "開始時間を超えています", Toast.LENGTH_LONG).show()
                    } else {
                        val time = String.format("%02d:%02d", hourOfDay, minuteOfDay)
                        OA_Noffication_Time_Between2.text = time
                        endTime = time
                    }
                }, nowTimeList[0].toInt(), nowTimeList[1].toInt(), true
            ).show()
        }

        // 出題間隔の指定
        OA_Noffication_Interval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinner = parent as Spinner
                val select = spinner.selectedItem.toString()
                interval = select
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        // Google SignIn
        sign_in_button.setSafeClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, rcSignIn)
        }
    }

    // Google SignIn ->
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Googleアカウントのログインに失敗しました", Toast.LENGTH_LONG).show()
            }
        }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(this, "認証に失敗しました", Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "[${user.displayName}]さんでログインしています", Toast.LENGTH_LONG).show()
            ApiPostTask {
                if (it.isNullOrEmpty()) {
                    Toast.makeText(this, "APIとの通信に失敗しました", Toast.LENGTH_LONG).show()
                } else {
                    val jsonObject = JSONObject(it)
                    val status = jsonObject.getString("status")
                    if (status == "S00") {
                        // Firebaseからアカウント登録を取得してDBに登録
                        val userId = jsonObject.getJSONObject("data").getString("user_id")
                        val e: SharedPreferences.Editor = getSharedPreferences("user_data", MODE_PRIVATE).edit()
                        e.putString("user_id", userId).apply()
                    } else {
                        Toast.makeText(this, "ユーザー登録に失敗しました", Toast.LENGTH_LONG).show()
                    }
                }
            }.execute("add-user.php", hashMapOf("token" to user.uid).toString())
        }
    }
    // Google SignIn <-

    private fun pushEndButton() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.OA_back_dialog_title)
            .setPositiveButton(R.string.OA_back_dialog_button_positive) { _, _ ->
                spEditor.putBoolean("NDS_check", OA_NDS_Mode_BTN.isChecked).apply()
                spEditor.putBoolean("SDS_check", OA_SDS_Mode_BTN.isChecked).apply()
                spEditor.putString("way_radio_select", radioSelect).apply()
                spEditor.putString("NDS_Interval", interval).apply()
                spEditor.putString("NDS_Start", startTime).apply()
                spEditor.putString("NDS_End", endTime).apply()
                val serviceN = LocalNotificationScheduleService()
                serviceN.registerNotice(this)

                val serviceS = Intent(this, LocalNotificationForegroundService::class.java)
                if (OA_SDS_Mode_BTN.isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceS)
                    } else {
                        startService(serviceS)
                    }
                } else {
                    stopService(serviceS)
                }
                finish()
            }.setNegativeButton(R.string.OA_back_dialog_button_negative){_,_ ->
                finish()
            }
        builder.show()
    }

    override fun onBackPressed() {
        pushEndButton()
        return // 無理やりリターンされることでActivityがDestroyされることを防ぐ
        // 以下は必ず処理されない。この方法がどうなのかは微妙
        super.onBackPressed()
    }
}
