package io.anyrtc.arboarddemo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.gyf.immersionbar.ImmersionBar
import io.anyrtc.arboard.ARBoardEnumerates.*
import io.anyrtc.arboarddemo.databinding.ActivityLoginBinding
import io.anyrtc.arboarddemo.utils.ScreenUtils
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    ScreenUtils.adapterScreen(this, 375, false)
    super.onCreate(savedInstanceState)
    ImmersionBar.with(this).fitsSystemWindows(true)
      .statusBarColor(R.color.statusBarColor).statusBarDarkFont(true)
      .navigationBarColor(R.color.white, 0.2f).navigationBarDarkIcon(true).init()
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val afterTextChanged: (text: Editable?) -> Unit = afterTextChangeListener@{
      binding.login.isChecked =
        binding.username.editableText?.length != 0 && binding.roomId.editableText?.length != 0
    }
    binding.username.addTextChangedListener(afterTextChanged = afterTextChanged)
    binding.roomId.addTextChangedListener(afterTextChanged = afterTextChanged)
    val rUid = Random.nextInt(100000, 999999)
    binding.username.setText(String.format("%s", "user$rUid"))

    binding.login.setOnClickListener {
      binding.login.isChecked = !binding.login.isChecked
      if (!binding.login.isChecked) {
        Toast.makeText(this, "请输入必填项", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      val username = binding.username.text.toString()
      val roomId = binding.roomId.text.toString()

      var regex = Regex("[^0-9A-Za-z]+")
      if (regex.find(username) != null) {
        Toast.makeText(this, "用户名不合法", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      regex = Regex("(\\d+\\D+)|(\\D+\\d+)")
      if (regex.find(username) == null) {
        Toast.makeText(this, "用户名只能输入数字和字母的组合", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      regex = Regex("[^\\d]+")
      if (regex.find(roomId) != null) {
        Toast.makeText(this, "房间号不合法", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }

      finish()
      startActivity(Intent(this, MainActivity::class.java).also {
        it.putExtra("uid", binding.username.text.toString())
        it.putExtra("channel", binding.roomId.text.toString())
      })
    }
  }

  override fun onDestroy() {
    ScreenUtils.resetScreen(this)
    super.onDestroy()
  }
}
