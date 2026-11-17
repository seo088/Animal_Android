package com.animalloo.ui.compatibility;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.animalloo.R;
import com.animalloo.databinding.ActivityPetCompatibilityBinding;

/** Compares the guardian's and dog's five daily-life preference axes. */
public class PetCompatibilityActivity extends AppCompatActivity {
    private static final String[] AXES = {"활동량", "사회성", "정서 표현", "생활 리듬", "함께하는 놀이"};
    private static final Question[] QUESTIONS = {
            q("보호자 질문", "퇴근 후 집에 오면 가장 먼저 하고 싶은 건?", "바로 밖으로 나가 산책하거나 운동하기", "가볍게 스트레칭하면서 집 안에서 움직이기", "소파에 누워서 편하게 쉬기"),
            q("보호자 질문", "친구 모임에서 나는 어떤 타입?", "누구와도 금방 친해지는 인싸", "친한 사람이랑만 깊게 대화하는 스타일", "모임보다는 혼자 있는 게 편한 집순이·집돌이"),
            q("보호자 질문", "기분이 좋거나 속상할 때 표현 방식은?", "표정이나 행동으로 바로 티가 남", "말로는 안 하지만 분위기로 전달됨", "속으로 삭이는 편, 잘 안 드러남"),
            q("보호자 질문", "여행 스타일은?", "계획표 빽빽하게 짜서 움직이는 타입", "큰 틀만 정하고 상황에 맞게 조절하는 타입", "무계획이 계획! 발길 닿는 대로 가는 타입"),
            q("보호자 질문", "반려동물과 놀 때 가장 즐거운 순간은?", "같이 뛰어놀면서 에너지 발산할 때", "새로운 장난감이나 놀이를 가르칠 때", "나란히 누워서 조용히 쉴 때"),
            q("반려동물 질문", "산책 나가면 우리 강아지 에너지 레벨은?", "미친 듯이 달리고 또 달림, 끝없는 체력", "적당히 뛰다가 냄새 맡으며 산책 즐김", "느긋하게 걷거나 금방 놀고 싶어함"),
            q("반려동물 질문", "낯선 사람이나 다른 강아지를 만나면?", "꼬리 흔들며 먼저 다가가는 반가움 폭발", "잠깐 관찰하다가 조심스럽게 인사함", "뒤로 숨거나 보호자 다리 뒤에 붙음"),
            q("반려동물 질문", "보호자가 기뻐하거나 슬퍼할 때 반응은?", "바로 달려와 핥거나 온몸으로 반응", "옆에 와서 조용히 앉아 있어줌", "별다른 반응 없이 자기 할 일 함"),
            q("반려동물 질문", "밥 시간이나 산책 루틴이 달라지면?", "정확히 시간 맞춰 요구하고 예민해짐", "좀 기다리다가 슬슬 알려줌", "달라져도 크게 신경 안 쓰는 편"),
            q("반려동물 질문", "놀이 시간에 보여주는 놀이 스타일은?", "공 물어오기, 터그놀이 등 격한 놀이 선호", "노즈워크나 간식 장난감 등 두뇌 놀이 좋아함", "배 보여주고 눕거나 스킨십 놀이 위주")
    };

    private ActivityPetCompatibilityBinding binding;
    private final int[] answers = new int[QUESTIONS.length];
    private int questionIndex;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityPetCompatibilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resetAnswers();
        binding.btnCompatibilityBack.setOnClickListener(v -> finish());
        binding.btnCompatibilityPrevious.setOnClickListener(v -> { if (questionIndex > 0) { questionIndex--; renderQuestion(); } });
        binding.btnCompatibilityNext.setOnClickListener(v -> advance());
        binding.btnCompatibilityRetry.setOnClickListener(v -> resetTest());
        renderQuestion();
    }

    private void renderQuestion() {
        Question question = QUESTIONS[questionIndex];
        binding.layoutCompatibilityQuestion.setVisibility(View.VISIBLE);
        binding.layoutCompatibilityResult.setVisibility(View.GONE);
        binding.btnCompatibilityNext.setVisibility(View.VISIBLE);
        binding.btnCompatibilityRetry.setVisibility(View.GONE);
        binding.tvCompatibilityCategory.setText(question.category);
        binding.tvCompatibilityQuestion.setText(question.text);
        binding.tvCompatibilityProgress.setText(getString(R.string.pet_mbti_progress_format, questionIndex + 1, QUESTIONS.length));
        binding.btnCompatibilityPrevious.setVisibility(questionIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        binding.btnCompatibilityNext.setText(questionIndex == QUESTIONS.length - 1 ? R.string.pet_mbti_result : R.string.pet_mbti_next);
        boolean answered = answers[questionIndex] >= 0;
        binding.btnCompatibilityNext.setEnabled(answered);
        binding.btnCompatibilityNext.setAlpha(answered ? 1f : .45f);
        renderSteps();
        binding.layoutCompatibilityOptions.removeAllViews();
        for (int i = 0; i < question.options.length; i++) addOption(i, question.options[i]);
    }

    private void renderSteps() {
        binding.layoutCompatibilitySteps.removeAllViews();
        for (int i = 0; i < QUESTIONS.length; i++) {
            TextView step = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(8), 1f);
            if (i > 0) params.setMarginStart(dp(2));
            step.setLayoutParams(params);
            step.setBackgroundResource(i <= questionIndex ? R.drawable.bg_match_step_active : R.drawable.bg_match_step_idle);
            binding.layoutCompatibilitySteps.addView(step);
        }
    }

    private void addOption(int answerIndex, String label) {
        RadioButton option = new RadioButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(64));
        if (answerIndex > 0) params.topMargin = dp(10);
        option.setLayoutParams(params);
        option.setPadding(dp(16), 0, dp(12), 0);
        option.setText(label);
        option.setTextSize(15);
        option.setTextColor(ContextCompat.getColor(this, R.color.color_text_primary));
        option.setButtonTintList(ContextCompat.getColorStateList(this, R.color.color_test_primary));
        boolean selected = answers[questionIndex] == answerIndex;
        option.setChecked(selected);
        option.setBackgroundResource(selected ? R.drawable.bg_match_option_selected : R.drawable.bg_match_option);
        option.setOnClickListener(v -> { answers[questionIndex] = answerIndex; renderQuestion(); });
        binding.layoutCompatibilityOptions.addView(option);
    }

    private void advance() {
        if (answers[questionIndex] < 0) return;
        if (questionIndex < QUESTIONS.length - 1) { questionIndex++; renderQuestion(); } else showResult();
    }

    private void showResult() {
        CompatibilityResult result = calculateResult();
        binding.layoutCompatibilityQuestion.setVisibility(View.GONE);
        binding.layoutCompatibilityResult.setVisibility(View.VISIBLE);
        binding.btnCompatibilityPrevious.setVisibility(View.GONE);
        binding.btnCompatibilityNext.setVisibility(View.GONE);
        binding.btnCompatibilityRetry.setVisibility(View.VISIBLE);
        binding.tvCompatibilityScore.setText(getString(R.string.compatibility_score_format, result.score));
        binding.tvCompatibilityGrade.setText(result.grade);
        binding.tvCompatibilityDescription.setText(result.description);
        binding.tvCompatibilityMatch.setText(getString(R.string.compatibility_match_format, result.bestAxis));
        binding.tvCompatibilityAdjust.setText(getString(R.string.compatibility_adjust_format, result.adjustAxis));
        binding.tvCompatibilityTip.setText(result.tip);
        binding.scrollCompatibility.post(() -> binding.scrollCompatibility.fullScroll(View.FOCUS_UP));
    }

    private CompatibilityResult calculateResult() {
        int totalDifference = 0, best = 0, adjust = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int axis = 0; axis < AXES.length; axis++) {
            int difference = Math.abs(answers[axis] - answers[axis + 5]);
            totalDifference += difference;
            if (difference < min) { min = difference; best = axis; }
            if (difference > max) { max = difference; adjust = axis; }
        }
        int score = Math.round(100f - (totalDifference / 10f * 100f));
        if (score >= 90) return result(score, "운명적 소울메이트", "서로의 생활 리듬과 반응이 자연스럽게 맞아, 말하지 않아도 통하는 순간이 많은 조합이에요. 지금의 케어 방식을 유지하며 가끔 새로운 활동을 더해 보세요.", "평소 좋아하는 산책·놀이를 꾸준히 함께해 보세요.", best, adjust);
        if (score >= 75) return result(score, "찰떡 콤비", "중요한 생활 패턴이 잘 맞는 편이에요. 서로의 속도를 존중하면 편안하고 즐거운 일상을 만들 수 있어요.", "잘 맞는 활동을 주간 루틴으로 만들어 보세요.", best, adjust);
        if (score >= 60) return result(score, "함께 성장하는 짝꿍", "몇 가지 차이는 있지만, 서로를 알아 가는 재미가 큰 조합이에요. 다른 부분은 부담 없이 짧은 경험부터 맞춰 보세요.", "차이가 큰 활동은 시간과 강도를 조금씩 조절해 보세요.", best, adjust);
        if (score >= 40) return result(score, "조율이 필요한 사이", "좋아하는 방식이 다른 부분이 보여요. 틀린 것이 아니라 선호가 다른 것이니, 반려견의 신호를 보며 생활 리듬을 맞춰 가면 돼요.", "산책과 놀이 시간을 나누고 쉬는 시간을 충분히 주세요.", best, adjust);
        return result(score, "서로를 배우는 첫걸음", "생활 성향이 꽤 다른 조합이에요. 보호자의 기대보다 반려견의 안전과 휴식을 먼저 두고, 작은 성공 경험을 쌓아 가세요.", "낯선 활동은 강요하지 말고 필요하면 전문가 상담도 고려해 보세요.", best, adjust);
    }

    private CompatibilityResult result(int score, String grade, String description, String tip, int best, int adjust) { return new CompatibilityResult(score, grade, description, tip, AXES[best], AXES[adjust]); }
    private void resetTest() { resetAnswers(); questionIndex = 0; renderQuestion(); binding.scrollCompatibility.post(() -> binding.scrollCompatibility.fullScroll(View.FOCUS_UP)); }
    private void resetAnswers() { for (int i = 0; i < answers.length; i++) answers[i] = -1; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static Question q(String category, String text, String... options) { return new Question(category, text, options); }
    private static class Question { final String category, text; final String[] options; Question(String category, String text, String[] options) { this.category = category; this.text = text; this.options = options; } }
    private static class CompatibilityResult { final int score; final String grade, description, tip, bestAxis, adjustAxis; CompatibilityResult(int score, String grade, String description, String tip, String bestAxis, String adjustAxis) { this.score = score; this.grade = grade; this.description = description; this.tip = tip; this.bestAxis = bestAxis; this.adjustAxis = adjustAxis; } }
}
