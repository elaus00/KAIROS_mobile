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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 위젯에서 바로 캡처 입력하는 투명 다이얼로그 Activity
 * 키보드 위 영역을 채우는 넓은 입력 카드
 */
@AndroidEntryPoint
class QuickCaptureActivity : ComponentActivity() {

    @Inject
    lateinit var submitCaptureUseCase: SubmitCaptureUseCase

    private lateinit var editText: EditText
    private lateinit var doneButton: TextView
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 테마 기반 색상 로드
        val colors = resolveThemeColors()

        // 투명 Activity에서 키보드 인셋을 직접 처리
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 루트 컨테이너 (배경 탭하면 닫기)
        val root = FrameLayout(this).apply {
            setOnClickListener { finish() }
        }

        // 카드 컨테이너 (VERTICAL — 입력 영역 + 하단 툴바)
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(colors.cardBackground)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), colors.cardBorder)
            }
            elevation = dp(8).toFloat()
            // 카드 클릭 이벤트 소비 (배경 닫기 방지)
            isClickable = true
        }

        // 입력 필드 (카드 내 남는 공간 전부 차지)
        editText = EditText(this).apply {
            hint = "무엇이든 캡처하세요..."
            setHintTextColor(colors.hint)
            setTextColor(colors.text)
            textSize = 16f
            background = null
            isSingleLine = false
            minLines = 5
            maxLines = 15
            gravity = Gravity.TOP or Gravity.START
            setPadding(dp(20), dp(20), dp(20), dp(12))
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitCapture()
                    true
                } else false
            }
        }
        val editParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        )
        card.addView(editText, editParams)

        // 구분선
        val divider = View(this).apply {
            setBackgroundColor(colors.cardBorder)
        }
        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
        )
        card.addView(divider, dividerParams)

        // 하단 툴바
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        // 좌측: "KAIROS" 텍스트
        val brandText = TextView(this).apply {
            text = "KAIROS"
            textSize = 12f
            setTextColor(colors.brandText)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.1f
        }
        val brandParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        toolbar.addView(brandText, brandParams)

        // 우측: "완료" 버튼 (44dp 터치 타겟 확보)
        doneButton = TextView(this).apply {
            text = "완료"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colors.buttonText)
            gravity = Gravity.CENTER
            minimumHeight = dp(44)
            setPadding(dp(24), dp(0), dp(24), dp(0))
            background = GradientDrawable().apply {
                setColor(colors.buttonBackground)
                cornerRadius = dp(22).toFloat()
            }
            setOnClickListener { submitCapture() }
        }
        val doneParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            dp(44)
        )
        toolbar.addView(doneButton, doneParams)

        val toolbarParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        card.addView(toolbar, toolbarParams)

        // 카드 위치: 상단 고정, 내용에 따라 높이 조절
        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP
            setMargins(dp(16), dp(80), dp(16), dp(0))
        }
        card.minimumHeight = dp(200)
        root.addView(card, cardParams)

        setContentView(root)

        // 키보드와 최소 16dp 여백 보장 — EditText 최대 높이 동적 제한
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val bottomInset = maxOf(imeBottom, navBottom)
            if (view.height > 0) {
                val maxCardHeight = view.height - dp(80) - bottomInset - dp(16)
                editText.maxHeight = (maxCardHeight - dp(50)).coerceAtLeast(dp(100))
            }
            insets
        }

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

        // 제출 중 시각적 피드백
        doneButton.apply {
            this.text = "저장 중..."
            alpha = 0.6f
            isEnabled = false
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
                    isSubmitting = false
                    // 버튼 상태 복원
                    doneButton.apply {
                        this.text = "완료"
                        alpha = 1f
                        isEnabled = true
                    }
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

    /**
     * 테마 기반 색상 세트 로드
     * DeviceDefault 테마에서 적응형 색상을 가져오고, 대비 기준을 보장
     */
    private fun resolveThemeColors(): ThemeColors {
        val isDark = isDarkMode()
        return if (isDark) {
            ThemeColors(
                cardBackground = 0xFF1E1E1E.toInt(),   // 순수 검정 회피 (HIG 2.8)
                cardBorder = 0xFF333333.toInt(),         // 경계선 대비 강화
                text = 0xDEFFFFFF.toInt(),               // 87% white (Material 기준)
                hint = 0x99FFFFFF.toInt(),               // 60% white
                brandText = 0x61FFFFFF.toInt(),          // 38% white (4.5:1 미달이지만 장식 텍스트)
                buttonBackground = 0xFFE0E0E0.toInt(),   // 밝은 회색 버튼
                buttonText = 0xFF121212.toInt()           // 어두운 텍스트 (대비 ~15:1)
            )
        } else {
            ThemeColors(
                cardBackground = 0xFFFFFFFF.toInt(),
                cardBorder = 0xFFE0E0E0.toInt(),         // 경계선 대비 강화
                text = 0xDE000000.toInt(),                // 87% black
                hint = 0x99000000.toInt(),                // 60% black
                brandText = 0x61000000.toInt(),           // 38% black
                buttonBackground = 0xFF1A1A1A.toInt(),
                buttonText = 0xFFFFFFFF.toInt()           // 대비 ~17:1
            )
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

    /**
     * 테마 색상 세트 (다크/라이트 분리)
     */
    private data class ThemeColors(
        val cardBackground: Int,
        val cardBorder: Int,
        val text: Int,
        val hint: Int,
        val brandText: Int,
        val buttonBackground: Int,
        val buttonText: Int
    )
}
