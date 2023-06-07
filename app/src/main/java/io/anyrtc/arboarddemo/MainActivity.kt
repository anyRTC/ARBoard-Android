package io.anyrtc.arboarddemo

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.gyf.immersionbar.ImmersionBar
import io.anyrtc.arboard.ARBoardEnumerates.*
import io.anyrtc.arboard.ARBoardHandler
import io.anyrtc.arboard.ARBoardKit
import io.anyrtc.arboard.ARBoardStructures.ARBoardAuthParam
import io.anyrtc.arboard.ARBoardStructures.ARBoardBaseParam
import io.anyrtc.arboarddemo.databinding.ActivityMainBinding
import io.anyrtc.arboarddemo.utils.ScreenUtils
import io.anyrtc.arboarddemo.widget.RoundBackgroundView
import io.anyrtc.arboarddemo.widget.SeekBarWidget

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private lateinit var kit: ARBoardKit
  private val mHandler = MyHandler()
  private var doingAnim = false

  private val colorTransition = SeekBarWidget.ColorTransition(
    Color.parseColor("#00000000"),
    Color.parseColor("#99000000")
  )

  private var recordGraffitiColor = Color.BLACK
  private var recordFontColor = Color.BLACK

  override fun onCreate(savedInstanceState: Bundle?) {
    ScreenUtils.adapterScreen(this, 375, false)
    super.onCreate(savedInstanceState)
    ImmersionBar.with(this).fitsSystemWindows(true)
      .statusBarColor(R.color.white).statusBarDarkFont(true)
      .navigationBarColor(R.color.white, 0.2f).navigationBarDarkIcon(true).init()
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val uid = intent.getStringExtra("uid")
    val channel = intent.getStringExtra("channel")

    val appId = resources.getString(R.string.whiteboard_app_id)
    val param = ARBoardBaseParam()
    param.config.ratio = "3:4"
    param.config.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_NONE
    kit = ARBoardKit(this, ARBoardAuthParam(appId, "", uid), channel, param, mHandler)
    showLoadingDialog()
    //kit = ARBoardKit(this, appId, mHandler)
    //kit.joinChannelByToken("123456", channel, uid) {
    //}
    val boardView = kit.arBoardView
    boardView.setBackgroundColor(Color.TRANSPARENT)
    binding.boardParent.addView(boardView)

    initWidget()
  }

  private var checkBoxCaches = CheckBoxCaches()
  private fun initWidget() = binding.run {
    val behavior = BottomSheetBehavior.from(toolsMenu)

    boardEditable.setOnCheckedChangeListener { _, isChecked ->
      kit.isDrawEnable = isChecked
      if (isChecked) enableGraffiti() else disableGraffiti()
    }
    radioMouse.isChecked = true

    boardParent.requestFocus()
    val radioGroupConfirmClick = View.OnClickListener {
      val _checkedToolType = checkBoxCaches.selectedTool
      if (_checkedToolType != null)
        kit.toolType = _checkedToolType
      if (-1 != checkBoxCaches.selectedImg)
        toolsPreview.setImageResource(checkBoxCaches.selectedImg)
      if (-1 != checkBoxCaches.selectedRadioGroup)
        checkBoxCaches.radioGroup = checkBoxCaches.selectedRadioGroup
      checkBoxCaches.currCheckedId = checkBoxCaches.selectedCheckedId

      checkBoxCaches.selectedTool = null
      checkBoxCaches.selectedImg = -1
      checkBoxCaches.selectedRadioGroup = -1

      behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    radioGroupTools.setOnCheckedChangeListener { _, checkedId ->
      if (checkedId == -1)
        return@setOnCheckedChangeListener

      radioGroupGraffiti.clearCheck()
      when (checkedId) {
        R.id.radio_mouse -> {
          //toolsPreview.setImageResource(R.drawable.mouse)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_NONE
          checkBoxCaches.selectedImg = R.drawable.mouse
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_NONE
        }
        R.id.radio_eraser -> {
          //toolsPreview.setImageResource(R.drawable.eraser)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_ERASER
          checkBoxCaches.selectedImg = R.drawable.eraser
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_ERASER
        }
        R.id.radio_laser -> {
          //toolsPreview.setImageResource(R.drawable.laser_pointer)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_LASER
          checkBoxCaches.selectedImg = R.drawable.laser_pointer
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_LASER
        }
        R.id.radio_text -> {
          //toolsPreview.setImageResource(R.drawable.text)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_TEXT
          checkBoxCaches.selectedImg = R.drawable.text
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_TEXT
        }
        R.id.radio_select -> {
          //toolsPreview.setImageResource(R.drawable.select_rectangle)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_POINT_SELECT
          checkBoxCaches.selectedImg = R.drawable.select_rectangle
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_POINT_SELECT
        }
      }
      checkBoxCaches.selectedRadioGroup = 0
      checkBoxCaches.selectedCheckedId = checkedId
      radioGroupTools.check(checkedId)
      toolsConfirm.setOnClickListener(radioGroupConfirmClick)
    }
    radioGroupGraffiti.setOnCheckedChangeListener { _, checkedId ->
      if (checkedId == -1)
        return@setOnCheckedChangeListener

      radioGroupTools.clearCheck()
      when (checkedId) {
        R.id.radio_pencil -> {
          //toolsPreview.setImageResource(R.drawable.menu_pencil)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_PEN
          checkBoxCaches.selectedImg = R.drawable.menu_pencil
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_PEN
        }
        R.id.radio_rectangle -> {
          //toolsPreview.setImageResource(R.drawable.select_rectangle)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_RECT
          checkBoxCaches.selectedImg = R.drawable.menu_rectangle
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_RECT
        }
        R.id.radio_oval -> {
          //toolsPreview.setImageResource(R.drawable.menu_oval)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_OVAL
          checkBoxCaches.selectedImg = R.drawable.menu_oval
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_OVAL
        }
        R.id.radio_arrows -> {
          //toolsPreview.setImageResource(R.drawable.menu_arrows)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_ARROW
          checkBoxCaches.selectedImg = R.drawable.menu_arrows
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_ARROW
        }
        R.id.radio_line -> {
          //toolsPreview.setImageResource(R.drawable.menu_line)
          //kit.toolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_LINE
          checkBoxCaches.selectedImg = R.drawable.menu_line
          checkBoxCaches.selectedTool = ARBoardToolType.AR_BOARD_TOOL_TYPE_LINE
        }
      }
      checkBoxCaches.selectedRadioGroup = 1
      checkBoxCaches.selectedCheckedId = checkedId
      radioGroupGraffiti.check(checkedId)
      toolsConfirm.setOnClickListener(radioGroupConfirmClick)
    }
    menuMask.setOnClickListener {
      changeMenuStatus(true)
    }
    menu.setOnClickListener {
      toggleMenuStatus()
    }

    seekRed.setOnProgressChangListener { progress ->
      redSeekNum.text = "$progress"
      refreshColors()
    }
    seekGreen.setOnProgressChangListener { progress ->
      greenSeekNum.text = "$progress"
      refreshColors()
    }
    seekBlue.setOnProgressChangListener { progress ->
      blueSeekNum.text = "$progress"
      refreshColors()
    }
    refreshColors()

    //behavior.peekHeight = resources.getDimension(R.dimen.dp44).toInt()
    behavior.peekHeight = 0
    behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED)
          behaviorMask.visibility = View.GONE
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (slideOffset > 0.0f && behaviorMask.visibility == View.GONE)
          behaviorMask.visibility = View.VISIBLE

        val color = colorTransition.getValue(slideOffset)
        behaviorMask.setBackgroundColor(color)
      }
    })
    behavior.state = BottomSheetBehavior.STATE_COLLAPSED

    toolsCancel.setOnClickListener {
      behavior.state = BottomSheetBehavior.STATE_COLLAPSED
      if (checkBoxCaches.radioGroup == 0) {
        radioGroupGraffiti.clearCheck()
        radioGroupTools.check(checkBoxCaches.currCheckedId)
      } else {
        radioGroupTools.clearCheck()
        radioGroupGraffiti.check(checkBoxCaches.currCheckedId)
      }
    }

    selectTools.setOnClickListener {
      colorGroup.visibility = View.GONE
      menuGroup.visibility = View.VISIBLE
      behavior.state = BottomSheetBehavior.STATE_EXPANDED

      toolsConfirm.setOnClickListener {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
      }
    }
    behaviorMask.setOnClickListener {
      behavior.state = BottomSheetBehavior.STATE_COLLAPSED
      if (checkBoxCaches.radioGroup == 0) {
        radioGroupGraffiti.clearCheck()
        radioGroupTools.check(checkBoxCaches.currCheckedId)
      } else {
        radioGroupTools.clearCheck()
        radioGroupGraffiti.check(checkBoxCaches.currCheckedId)
      }
    }

    val selectColorClick = View.OnClickListener { parent ->
      colorGroup.visibility = View.VISIBLE
      menuGroup.visibility = View.GONE
      behavior.state = BottomSheetBehavior.STATE_EXPANDED

      val colorPreviewView = (parent as ViewGroup).getChildAt(1) as RoundBackgroundView

      val previewColor = colorPreviewView.backgroundColor
      val redNum = Color.red(previewColor)
      val greenNum = Color.green(previewColor)
      val blueNum = Color.blue(previewColor)
      redSeekNum.text = "$redNum"
      greenSeekNum.text = "$greenNum"
      blueSeekNum.text = "$blueNum"

      seekRed.progress = redNum
      seekGreen.progress = greenNum
      seekBlue.progress = blueNum
      currentColor.backgroundColor = previewColor

      toolsConfirm.setOnClickListener {
        val color = refreshColors()
        val strColor = String.format("#%06X", 0xFFFFFF and color)

        when (parent.id) {
          R.id.graffiti_color -> kit.brushColor = strColor
          R.id.font_color -> kit.textColor = strColor
          else -> kit.backgroundColor = strColor
        }

        colorPreviewView.backgroundColor = color
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
      }
    }
    graffitiColor.setOnClickListener(selectColorClick)
    fontColor.setOnClickListener(selectColorClick)
    boardColor.setOnClickListener(selectColorClick)

    // buttons
    undo.setOnClickListener { kit.undo() }
    redo.setOnClickListener { kit.redo() }
    clearGraffiti.setOnClickListener { kit.clearDraws() }
    clearAll.setOnClickListener {
      kit.clear()
      boardColorPreview.backgroundColor = Color.WHITE
    }
    reset.setOnClickListener { kit.reset() }
    selectImage.setOnClickListener { showSetImageDialog() }
    add.setOnClickListener { kit.addBoard() }
    delete.setOnClickListener { kit.deleteBoard() }
    prevPage.setOnClickListener { kit.prevBoard() }// previous
    nextPage.setOnClickListener { kit.nextBoard() }

    // SeekBars
    graffitiSeek.setOnProgressChangListener {
      kit.brushThin = it
    }
    fontSizeSeek.setOnProgressChangListener {
      kit.textSize = it
    }
    boardScaleSeek.setOnProgressChangListener {
      kit.boardScale = it
    }
    exit.setOnClickListener {
      finish()
      startActivity(Intent(this@MainActivity, LoginActivity::class.java))
    }
    dialogParent.setOnClickListener { }
    imageDialogCancel.setOnClickListener { dismissSetImageDialog() }
    imageDialogConfirm.setOnClickListener {
      val src = imageDialogEdit.text.toString()
      if (src.isBlank()) {
        Toast.makeText(it.context, "不能为空", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      kit.setBackgroundImage(src, ARBoardFillMode.AR_BOARD_BACKGROUND_FILL_MODE_CONTAIN)
      dismissSetImageDialog()
    }
  }

  private fun dismissLoadingDialog() = binding.run {
    dialogParent.visibility = View.GONE
    loadingGroup.visibility = View.GONE
  }

  private fun showLoadingDialog() = binding.run {
    dialogParent.visibility = View.VISIBLE
    loadingGroup.visibility = View.VISIBLE
    imageDialogGroup.visibility = View.GONE
  }

  private fun dismissSetImageDialog() = binding.run {
    dialogParent.visibility = View.GONE
    imageDialogGroup.visibility = View.GONE
    imageDialogEdit.setText("")
  }

  private fun showSetImageDialog() = binding.run {
    dialogParent.visibility = View.VISIBLE
    imageDialogGroup.visibility = View.VISIBLE
    loadingGroup.visibility = View.GONE
  }

  private fun refreshColors(): Int {
    val color = Color.rgb(
      binding.seekRed.progress,
      binding.seekGreen.progress,
      binding.seekBlue.progress
    )
    binding.currentColor.backgroundColor = color
    return color
  }

  private fun toggleMenuStatus() {
    changeMenuStatus(binding.menuCard.visibility == View.VISIBLE)
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun disableGraffiti() {
    val setText = { v: TextView, c: Int ->
      v.setTextColor(c)
      v.setOnTouchListener { _, _ -> true }
    }
    val setSeek = { v: SeekBarWidget, fg: Int ->
      v.setProgressForegroundColor(fg, fg)
      v.setOnTouchListener { _, _ -> true }
    }
    binding.run {
      val grayColor = Color.parseColor("#999999")

      setText(toolsSelecter, grayColor)
      setText(painterTitle, grayColor)
      setText(fontColorTitle, grayColor)
      setText(graffitiThicknessTitle, grayColor)
      setText(fontSizeTitle, grayColor)

      selectTools.setOnTouchListener { _, _ -> true }
      graffitiColor.setOnTouchListener { _, _ -> true }
      fontColor.setOnTouchListener { _, _ -> true }

      recordGraffitiColor = graffitiColorPreview.backgroundColor
      graffitiColorPreview.backgroundColor = grayColor

      recordFontColor = fontColorPreview.backgroundColor
      fontColorPreview.backgroundColor = grayColor

      setSeek(graffitiSeek, grayColor)
      setSeek(fontSizeSeek, grayColor)
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun enableGraffiti() {
    val setText = { v: TextView, c: Int ->
      v.setTextColor(c)
      v.setOnTouchListener { _, _ -> false }
    }
    val setSeek = { v: SeekBarWidget, fg: Int ->
      v.setProgressForegroundColor(fg, fg)
      v.setOnTouchListener { _, _ -> false }
    }
    binding.run {
      val textColor = Color.parseColor("#666666")
      val seekBarBgColor = Color.parseColor("#294BFF")

      setText(toolsSelecter, textColor)
      setText(painterTitle, textColor)
      setText(fontColorTitle, textColor)
      setText(graffitiThicknessTitle, textColor)
      setText(fontSizeTitle, textColor)

      selectTools.setOnTouchListener { _, _ -> false }
      graffitiColor.setOnTouchListener { _, _ -> false }
      fontColor.setOnTouchListener { _, _ -> false }

      graffitiColorPreview.backgroundColor = recordGraffitiColor
      fontColorPreview.backgroundColor = recordFontColor

      setSeek(graffitiSeek, seekBarBgColor)
      setSeek(fontSizeSeek, seekBarBgColor)
    }
  }

  private fun changeMenuStatus(toDismiss: Boolean) {
    if ((binding.menuCard.visibility == View.VISIBLE) != toDismiss || doingAnim)
      return

    doingAnim = true
    if (!toDismiss) {
      binding.menuCard.visibility = View.VISIBLE
      binding.menuMask.visibility = View.VISIBLE
    }
    val floatArr = if (toDismiss) arrayOf(1.0f, 0.0f) else arrayOf(0.0f, 1.0f)
    val ofFloat = ObjectAnimator.ofFloat(binding.menuCard, "alpha", floatArr[0], floatArr[1])
    ofFloat.addListener(onEnd = {
      if (toDismiss) {
        binding.menuCard.visibility = View.GONE
        binding.menuMask.visibility = View.GONE
      }
      doingAnim = false
    })
    ofFloat.duration = 350L
    ofFloat.start()
  }

  private inner class MyHandler : ARBoardHandler {
    override fun onOccurError(p0: ARBoardErrorCode?, p1: String?) {
      Log.e("===", "onOccurError,\t\tp0=$p0, p1=$p1")
    }

    override fun connectionChangedToState(
      p0: ARBoardConnectionState?,
      p1: ARBoardConnectionChangedReason?
    ) {
    }

    override fun onAddBoard(p0: String?, p1: String?) {
    }

    override fun onDeleteBoard(p0: String?, p1: String?) {
    }

    override fun onGotoBoard(p0: String?, p1: String?) {
    }

    override fun onHistoryDataSyncCompleted() {
      // 初始化完成
      runOnUiThread {
        kit.brushColor = "#000000"
        kit.textColor = "#000000"
        val color = kit.backgroundColor
        binding.boardColorPreview.backgroundColor = Color.parseColor(color)
        dismissLoadingDialog()
      }
      //binding.boardColorPreview.backgroundColor = Color.parseColor(kit.backgroundColor)
    }

    override fun onUndoStatusChanged(p0: Boolean) {
    }

    override fun onRedoStatusChanged(p0: Boolean) {
    }

    override fun onSnapshot(p0: String?, p1: ARBoardSnapshotCode?) {
    }

    override fun onScaleChanged(p0: String?, p1: Int) {
    }

    override fun onBoardReset() {
    }

    override fun onBoardClear(p0: String?, p1: String?, p2: Boolean) {
    }

    override fun onBoardImageStatusChanged(
      boardId: String?,
      fileId: String?,
      status: ARBoardImageStatus?,
      src: String?,
      fillMode: ARBoardFillMode?
    ) {
      Log.e("===", "status=$status, src=$src, fillMode=$fillMode")
    }
  }

  override fun onBackPressed() {
    if (doingAnim)
      return
    if (binding.menuCard.visibility == View.VISIBLE) {
      toggleMenuStatus()
      return
    }
    super.onBackPressed()
  }

  override fun onDestroy() {
    ScreenUtils.resetScreen(this)
    kit.leaveChannel()
    super.onDestroy()
  }

  private data class CheckBoxCaches(
    var radioGroup: Int = 0,
    var currCheckedId: Int = R.id.radio_mouse,
    var currCheckImg: Int = R.drawable.mouse,
    var currCheckTool: ARBoardToolType = ARBoardToolType.AR_BOARD_TOOL_TYPE_NONE,
    var selectedRadioGroup: Int = -1,
    var selectedImg: Int = -1,
    var selectedTool: ARBoardToolType? = null,
    var selectedCheckedId: Int = -1
  )
}
