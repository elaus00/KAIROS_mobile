package com.flit.app.presentation.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import com.flit.app.R
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
import com.flit.app.domain.model.CaptureSource
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.model.ThemePreference
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.capture.SubmitCaptureUseCase
import com.flit.app.domain.usecase.settings.PreferenceKeys
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * 위젯에서 바로 캡처 입력하는 투명 다이얼로그 Activity
 * 키보드 위 영역을 채우는 넓은 입력 카드
 */
@AndroidEntryPoint
class QuickCaptureActivity : ComponentActivity() {

    @Inject
    lateinit var submitCaptureUseCase: SubmitCaptureUseCase

    @Inject
    lateinit var userPreferenceRepository: UserPreferenceRepository

    private lateinit var editText: EditText
    private lateinit var doneButton: TextView
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreference = loadThemePreference()
        val fontPreference = loadCaptureFontPreference()
        val captureTextSizeSp = (fontPreference.captureFontSize - 2).coerceAtLeast(14)
        val fontScale = fontPreference.bodyFontSize / FontSizePreference.MEDIUM.bodyFontSize.toFloat()

        // 사용자 설정(테마/글씨 크기) 기반 UI 스타일 로드
        val colors = resolveThemeColors(isDarkMode(themePreference))

        // 투명 Activity에서 키보드 인셋을 직접 처리
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 루트 컨테이너 (배경 탭하면 닫기)
        val root = FrameLayout(this).apply {
            setBackgroundColor(colors.scrim)
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
            hint = null
            setHintTextColor(colors.hint)
            setTextColor(colors.text)
            textSize = captureTextSizeSp.toFloat()
            background = null
            isSingleLine = false
            minLines = 6
            maxLines = 18
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

        // 좌측: 브랜드 워드마크 (Sora 폰트 + 채움 도트)
        val soraTypeface = ResourcesCompat.getFont(this, R.font.sora_variable)
        val brandView = FlitBrandView(this, soraTypeface, 14f * fontScale, colors.brandText)
        val brandParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        toolbar.addView(brandView, brandParams)
        toolbar.addView(
            View(this),
            LinearLayout.LayoutParams(0, 0, 1f)
        )

        // 우측: "완료" 버튼
        doneButton = TextView(this).apply {
            text = "완료"
            textSize = 14f * fontScale
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colors.buttonText)
            gravity = Gravity.CENTER
            minimumHeight = dp(40)
            setPadding(dp(24), dp(0), dp(24), dp(0))
            background = GradientDrawable().apply {
                setColor(colors.buttonBackground)
                cornerRadius = dp(20).toFloat()
            }
            setOnClickListener { submitCapture() }
        }
        val doneParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            dp(40)
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
            setMargins(dp(16), dp(16), dp(16), dp(0))
        }
        card.minimumHeight = dp(240)
        root.addView(card, cardParams)

        setContentView(root)

        // 키보드와 최소 16dp 여백 보장 — EditText 최대 높이 동적 제한
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val statusTop = systemBarsInsets.top
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBottom = systemBarsInsets.bottom
            val bottomInset = maxOf(imeBottom, navBottom)

            cardParams.topMargin = statusTop + dp(16)
            card.layoutParams = cardParams

            if (view.height > 0) {
                val maxCardHeight = view.height - cardParams.topMargin - bottomInset - dp(16)
                editText.maxHeight = (maxCardHeight - dp(40)).coerceAtLeast(dp(120))
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
    private fun resolveThemeColors(isDark: Boolean): ThemeColors {
        return if (isDark) {
            ThemeColors(
                cardBackground = 0xFF1E1E1E.toInt(),   // 순수 검정 회피 (HIG 2.8)
                cardBorder = 0xFF333333.toInt(),         // 경계선 대비 강화
                text = 0xDEFFFFFF.toInt(),               // 87% white (Material 기준)
                hint = 0x99FFFFFF.toInt(),               // 60% white
                scrim = 0x73000000.toInt(),              // 배경 위젯/홈 요소 시각 분리
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
                scrim = 0x66000000.toInt(),               // 배경 위젯/홈 요소 시각 분리
                brandText = 0x61000000.toInt(),           // 38% black
                buttonBackground = 0xFF1A1A1A.toInt(),
                buttonText = 0xFFFFFFFF.toInt()           // 대비 ~17:1
            )
        }
    }

    private fun loadThemePreference(): ThemePreference = runBlocking {
        runCatching { userPreferenceRepository.getThemePreference().first() }
            .getOrDefault(ThemePreference.SYSTEM)
    }

    private fun loadCaptureFontPreference(): FontSizePreference = runBlocking {
        runCatching {
            val size = userPreferenceRepository.getString(
                PreferenceKeys.KEY_CAPTURE_FONT_SIZE,
                FontSizePreference.MEDIUM.name
            )
            FontSizePreference.fromString(size)
        }.getOrDefault(FontSizePreference.MEDIUM)
    }

    /** 사용자 설정 + 시스템 상태를 반영한 다크 모드 여부 */
    private fun isDarkMode(themePreference: ThemePreference): Boolean {
        if (themePreference == ThemePreference.LIGHT) return false
        if (themePreference == ThemePreference.DARK) return true

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
        val scrim: Int,
        val brandText: Int,
        val buttonBackground: Int,
        val buttonText: Int
    )

    /**
     * "Flit" 텍스트 + 채움 도트(●)를 그리는 커스텀 View
     * Compose FlitWordmark와 동일한 브랜드 표현 (MINIMUM 사이즈 기반)
     */
    private class FlitBrandView(
        context: android.content.Context,
        soraTypeface: Typeface?,
        private val textSizeSp: Float,
        private val color: Int
    ) : View(context) {

        private val density = context.resources.displayMetrics.density

        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = soraTypeface ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)
            this.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, textSizeSp, context.resources.displayMetrics
            )
            this.color = this@FlitBrandView.color
            letterSpacing = 0.08f  // ≈ 1sp / 12sp
        }

        // 도트 크기/간격/오프셋: MINIMUM 프리셋 기준 (2.5dp / 0.5dp / 0.5dp)
        private val dotRadius = 1.25f * density
        private val dotGap = 0.5f * density
        private val dotBaselineOffset = 0.5f * density

        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = this@FlitBrandView.color
            style = Paint.Style.FILL
        }

        private val textWidth = textPaint.measureText("Flit")
        private val fontMetrics = textPaint.fontMetrics

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val w = (textWidth + dotGap + dotRadius * 2).toInt() + paddingLeft + paddingRight
            val h = (fontMetrics.descent - fontMetrics.ascent).toInt() + paddingTop + paddingBottom
            setMeasuredDimension(w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val baseline = paddingTop - fontMetrics.ascent

            // "Flit" 텍스트
            canvas.drawText("Flit", paddingLeft.toFloat(), baseline, textPaint)

            // 채움 도트 — 베이스라인 높이에 중심
            val dotCx = paddingLeft + textWidth + dotGap + dotRadius
            val dotCy = baseline - dotRadius + dotBaselineOffset
            canvas.drawCircle(dotCx, dotCy, dotRadius, dotPaint)
        }
    }
}
