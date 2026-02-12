package com.example.kairos_mobile.presentation.widget

import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.example.kairos_mobile.R
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 위젯에서 바로 캡처 입력하는 투명 다이얼로그 Activity
 * 홈 화면 위에 플로팅 입력창을 표시하고, 입력 후 즉시 저장 + 닫기
 */
@AndroidEntryPoint
class QuickCaptureActivity : ComponentActivity() {

    @Inject
    lateinit var submitCaptureUseCase: SubmitCaptureUseCase

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 루트 컨테이너 (배경 탭하면 닫기)
        val root = FrameLayout(this).apply {
            setOnClickListener { finish() }
        }

        // 입력 카드 컨테이너
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(12), dp(12))
            setBackgroundResource(R.drawable.widget_quick_capture_bg)
            elevation = dp(8).toFloat()
            // 카드 클릭 이벤트 소비 (배경 닫기 방지)
            isClickable = true
        }

        // 입력 필드
        editText = EditText(this).apply {
            hint = "무엇이든 캡처하세요..."
            setHintTextColor(0xFF888888.toInt())
            setTextColor(resolveTextColor())
            textSize = 16f
            background = null
            isSingleLine = false
            maxLines = 4
            imeOptions = EditorInfo.IME_ACTION_SEND
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    submitCapture()
                    true
                } else false
            }
        }
        val editParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        card.addView(editText, editParams)

        // 전송 버튼
        val sendButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_send)
            setColorFilter(resolveAccentColor())
            background = null
            contentDescription = "전송"
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { submitCapture() }
        }
        val btnParams = LinearLayout.LayoutParams(dp(44), dp(44))
        card.addView(sendButton, btnParams)

        // 카드 위치: 하단에서 약간 위
        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
            setMargins(dp(16), 0, dp(16), dp(80))
        }
        root.addView(card, cardParams)

        setContentView(root)

        // 키보드 자동 표시
        editText.requestFocus()
        editText.postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    /**
     * 캡처 제출 후 Activity 종료
     */
    private fun submitCapture() {
        val text = editText.text.toString().trim()
        if (text.isBlank()) {
            finish()
            return
        }

        // 키보드 숨기기
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                submitCaptureUseCase(text = text, source = CaptureSource.WIDGET)
                runOnUiThread {
                    Toast.makeText(
                        this@QuickCaptureActivity,
                        "캡처 완료",
                        Toast.LENGTH_SHORT
                    ).show()
                    // 위젯 갱신
                    WidgetUpdateHelper.updateCaptureWidget(this@QuickCaptureActivity)
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@QuickCaptureActivity,
                        "저장 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 포커스 잃으면 닫기 (홈 버튼 등)
        if (isFinishing) return
        finish()
    }

    /** 시스템 테마에 따른 텍스트 색상 */
    private fun resolveTextColor(): Int {
        val nightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            0xCCFFFFFF.toInt() // 다크 모드: 80% white
        } else {
            0xFF111111.toInt() // 라이트 모드
        }
    }

    /** 시스템 테마에 따른 액센트 색상 */
    private fun resolveAccentColor(): Int {
        val nightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            0xCCFFFFFF.toInt()
        } else {
            0xFF111111.toInt()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
