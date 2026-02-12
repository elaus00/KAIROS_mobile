package com.example.kairos_mobile.presentation.widget

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 위젯에서 바로 캡처 입력하는 투명 다이얼로그 Activity
 * Notion 스타일 대형 카드: 키보드 위 영역을 채우는 넓은 입력 공간
 */
@AndroidEntryPoint
class QuickCaptureActivity : ComponentActivity() {

    @Inject
    lateinit var submitCaptureUseCase: SubmitCaptureUseCase

    private lateinit var editText: EditText
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDark = isDarkMode()

        // 루트 컨테이너 (배경 탭하면 닫기)
        val root = FrameLayout(this).apply {
            setOnClickListener { finish() }
        }

        // 카드 컨테이너 (VERTICAL — 입력 영역 + 하단 툴바)
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(if (isDark) 0xFF1A1A1A.toInt() else 0xFFFFFFFF.toInt())
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), if (isDark) 0xFF2A2A2A.toInt() else 0xFFEEEEEE.toInt())
            }
            elevation = dp(8).toFloat()
            // 카드 클릭 이벤트 소비 (배경 닫기 방지)
            isClickable = true
        }

        // 입력 필드 (카드 내 남는 공간 전부 차지)
        editText = EditText(this).apply {
            hint = "무엇이든 캡처하세요..."
            setHintTextColor(if (isDark) 0xFF666666.toInt() else 0xFFAAAAAA.toInt())
            setTextColor(resolveTextColor())
            textSize = 16f
            background = null
            isSingleLine = false
            minLines = 5
            maxLines = 15
            gravity = Gravity.TOP or Gravity.START
            setPadding(dp(20), dp(20), dp(20), dp(12))
            imeOptions = EditorInfo.IME_ACTION_NONE
        }
        val editParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        )
        card.addView(editText, editParams)

        // 구분선
        val divider = View(this).apply {
            setBackgroundColor(if (isDark) 0xFF2A2A2A.toInt() else 0xFFEEEEEE.toInt())
        }
        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
        )
        card.addView(divider, dividerParams)

        // 하단 툴바
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(10))
        }

        // 좌측: "KAIROS" 텍스트
        val brandText = TextView(this).apply {
            text = "KAIROS"
            textSize = 12f
            setTextColor(if (isDark) 0xFF555555.toInt() else 0xFFBBBBBB.toInt())
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.1f
        }
        val brandParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        toolbar.addView(brandText, brandParams)

        // 우측: "완료" 버튼
        val doneButton = TextView(this).apply {
            text = "완료"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (isDark) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(8), dp(20), dp(8))
            background = GradientDrawable().apply {
                setColor(if (isDark) 0xCCFFFFFF.toInt() else 0xFF111111.toInt())
                cornerRadius = dp(20).toFloat()
            }
            setOnClickListener { submitCapture() }
        }
        val doneParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        toolbar.addView(doneButton, doneParams)

        val toolbarParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        card.addView(toolbar, toolbarParams)

        // 카드 위치: 상단에서 시작, 키보드 위까지 채움 (adjustResize)
        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(dp(16), dp(80), dp(16), dp(0))
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
        if (isSubmitting) return
        val text = editText.text.toString().trim()
        if (text.isBlank()) {
            finish()
            return
        }

        isSubmitting = true

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
                    isSubmitting = false
                    Toast.makeText(
                        this@QuickCaptureActivity,
                        "저장 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // 완전히 가려질 때만 닫기 (권한 다이얼로그 등에서 유지)
        if (isFinishing) return
        finish()
    }

    /** 시스템 테마에 따른 텍스트 색상 */
    private fun resolveTextColor(): Int {
        return if (isDarkMode()) {
            0xCCFFFFFF.toInt() // 다크 모드: 80% white
        } else {
            0xFF111111.toInt() // 라이트 모드
        }
    }

    /** 다크 모드 여부 */
    private fun isDarkMode(): Boolean {
        val nightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
